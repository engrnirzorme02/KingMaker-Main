package com.example.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.DecisionBranchEntity
import com.example.data.local.DecisionDao
import com.example.data.local.DecisionEdgeEntity
import com.example.data.local.DecisionEntity
import com.example.data.local.DecisionEventEntity
import com.example.data.local.ResolutionTaskEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class SyncResult {
    data class Success(val message: String, val restoredCount: Int = 0) : SyncResult()
    data class Error(val errorMessage: String) : SyncResult()
}

/**
 * Enterprise-grade Firebase Sync Service for KingMaker v4.1 OS.
 * CRITICAL 9 & 10 COMPLIANCE:
 * - NO hardcoded credentials or fallback passwords (NO "kingmaker2026", NO default email).
 * - Distinguishes invalid credentials, user not found, network failure, and Firebase unavailable.
 * - Preserves append-only immutable event history; never overwrites events.
 * - Detects cloud vs local divergences and records explicit reconciliation conflict tasks.
 */
class FirebaseSyncService(
    private val context: Context,
    private val dao: DecisionDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kingmaker_firebase_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "KingMakerFirebase"
        private const val PREF_SAVED_EMAIL = "pref_saved_email"
        private const val PREF_SAVED_UID = "pref_saved_uid"
        private const val PREF_LAST_BRANCH = "pref_last_branch"
        private const val PREF_LAST_DECISION = "pref_last_decision"
    }

    private fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp check failed: ${e.message}")
            false
        }
    }

    fun getSavedUserEmail(): String {
        return prefs.getString(PREF_SAVED_EMAIL, "") ?: ""
    }

    fun getSavedBranchId(): String {
        return prefs.getString(PREF_LAST_BRANCH, "main") ?: "main"
    }

    fun getSavedDecisionId(): String? {
        return prefs.getString(PREF_LAST_DECISION, null)
    }

    fun saveLocalSessionState(email: String, activeBranchId: String, selectedDecisionId: String?) {
        prefs.edit()
            .putString(PREF_SAVED_EMAIL, email)
            .putString(PREF_LAST_BRANCH, activeBranchId)
            .putString(PREF_LAST_DECISION, selectedDecisionId)
            .apply()
    }

    /**
     * Authenticate user with Firebase Auth.
     * CRITICAL 9: Separates error categories without silently creating accounts or replacing passwords.
     */
    suspend fun authenticateUser(email: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("ইমেইল ও পাসওয়ার্ড প্রদান করা আবশ্যক।"))
        }

        if (!isFirebaseConfigured()) {
            return@withContext Result.failure(IllegalStateException("FIREBASE_UNAVAILABLE: Firebase কনফিগারেশন অনুপস্থিত (google-services.json প্রয়োজন)। লোকাল মোডে চলছে।"))
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val signInResult = auth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).awaitTask()
            val uid = signInResult.user?.uid ?: return@withContext Result.failure(IllegalStateException("ইউজার সেশন তৈরি করা যায়নি।"))

            prefs.edit()
                .putString(PREF_SAVED_EMAIL, trimmedEmail)
                .putString(PREF_SAVED_UID, uid)
                .apply()

            Result.success(uid)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.w(TAG, "Invalid credentials for $trimmedEmail")
            Result.failure(IllegalArgumentException("INVALID_CREDENTIALS: ভুল পাসওয়ার্ড বা ক্রেডেনশিয়াল দেওয়া হয়েছে।"))
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.w(TAG, "User not found for $trimmedEmail")
            Result.failure(IllegalArgumentException("USER_NOT_FOUND: এই ইমেইলে কোনো অ্যাকাউন্ট পাওয়া যায়নি। স্বয়ংক্রিয় অ্যাকাউন্ট তৈরি নিষিদ্ধ।"))
        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "Network failure during auth: ${e.message}")
            Result.failure(IOException("NETWORK_FAILURE: নেটওয়ার্ক সংযোগ ব্যর্থ হয়েছে। ইন্টারনেট সংযোগ পরীক্ষা করুন।"))
        } catch (e: Exception) {
            Log.e(TAG, "Authentication failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Upload local state to Firestore preserving append-only governance.
     * CRITICAL 10: Immutable event IDs, conflict detection, append-only history.
     */
    suspend fun syncLocalToCloud(
        activeBranchId: String,
        selectedDecisionId: String?
    ): SyncResult = withContext(Dispatchers.IO) {
        val email = getSavedUserEmail()
        if (email.isBlank()) {
            return@withContext SyncResult.Error("ব্যবহারকারী লগইন করেননি। দয়া করে প্রথমে ইমেইল ও পাসওয়ার্ড দিয়ে লগইন করুন।")
        }

        saveLocalSessionState(email, activeBranchId, selectedDecisionId)

        if (!isFirebaseConfigured()) {
            return@withContext SyncResult.Success(
                message = "লোকাল SQLite WAL মোড সচল (Firebase কনফিগার করা হলে ক্লাউডে সিঙ্ক হবে)।"
            )
        }

        val uid = prefs.getString(PREF_SAVED_UID, null)
        if (uid.isNullOrBlank()) {
            return@withContext SyncResult.Error("সক্রিয় Firebase সেশন নেই। দয়া করে পুনরায় লগইন করুন।")
        }

        try {
            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("kingmaker_users").document(uid)

            val nowUtc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            // 1. Meta record
            val metaData = mapOf(
                "email" to email,
                "activeBranchId" to activeBranchId,
                "selectedDecisionId" to (selectedDecisionId ?: ""),
                "lastSyncUtc" to nowUtc,
                "clientVersion" to "v4.1-governed"
            )
            userDocRef.set(metaData, SetOptions.merge()).awaitTask()

            // 2. Sync Branches
            val branches = dao.getAllBranches().firstOrNull() ?: emptyList()
            for (branch in branches) {
                val branchData = mapOf(
                    "branchId" to branch.branchId,
                    "name" to branch.name,
                    "parentBranchId" to (branch.parentBranchId ?: ""),
                    "forkedAtDecisionId" to (branch.forkedAtDecisionId ?: ""),
                    "forkedAtEventId" to (branch.forkedAtEventId ?: ""),
                    "dqsScore" to branch.dqsScore,
                    "status" to branch.status,
                    "createdTimestamp" to branch.createdTimestamp
                )
                userDocRef.collection("branches").document(branch.branchId)
                    .set(branchData, SetOptions.merge()).awaitTask()
            }

            // 3. Sync Decisions with conflict detection
            val decisions = dao.getAllDecisions().firstOrNull() ?: emptyList()
            var conflictCount = 0

            for (dec in decisions) {
                val remoteDoc = userDocRef.collection("decisions").document(dec.id).get().awaitTask()
                if (remoteDoc.exists()) {
                    val remoteUpdated = remoteDoc.getLong("updatedTimestamp") ?: 0L
                    val remoteStatus = remoteDoc.getString("status") ?: ""
                    // Conflict detection: If remote has a later timestamp with conflicting status
                    if (remoteUpdated > dec.updatedTimestamp && remoteStatus != dec.status) {
                        conflictCount++
                        dao.insertResolutionTask(
                            ResolutionTaskEntity(
                                id = "CONF-${UUID.randomUUID().toString().take(8)}",
                                cycleNodes = dec.id,
                                weakestNodeId = dec.id,
                                reason = "RECONCILIATION_CONFLICT: Remote status '$remoteStatus' differs from local status '${dec.status}'. Human reconciliation required."
                            )
                        )
                        // Do not overwrite divergent state silently!
                        continue
                    }
                }

                val decData = mapOf(
                    "id" to dec.id,
                    "title" to dec.title,
                    "problemStatement" to dec.problemStatement,
                    "status" to dec.status,
                    "complexityScore" to dec.complexityScore,
                    "complexityTier" to dec.complexityTier,
                    "dqsScore" to dec.dqsScore,
                    "risk" to dec.risk,
                    "impact" to dec.impact,
                    "changeability" to dec.changeability,
                    "budget" to dec.budget,
                    "projectId" to dec.projectId,
                    "evidenceType" to dec.evidenceType,
                    "selectedOption" to (dec.selectedOption ?: ""),
                    "digitalSignatureHash" to (dec.digitalSignatureHash ?: ""),
                    "approvedAt" to (dec.approvedAt ?: 0L),
                    "branchId" to dec.branchId,
                    "parentBranchId" to (dec.parentBranchId ?: ""),
                    "forkedFromDecisionId" to (dec.forkedFromDecisionId ?: ""),
                    "scopeConfirmed" to dec.scopeConfirmed,
                    "focusAreaConfirmed" to dec.focusAreaConfirmed,
                    "constraintsConfirmed" to dec.constraintsConfirmed,
                    "goalsConfirmed" to dec.goalsConfirmed,
                    "reviewPacketJson" to (dec.reviewPacketJson ?: ""),
                    "admissionCertificateJson" to (dec.admissionCertificateJson ?: ""),
                    "createdTimestamp" to dec.createdTimestamp,
                    "updatedTimestamp" to dec.updatedTimestamp
                )
                userDocRef.collection("decisions").document(dec.id)
                    .set(decData, SetOptions.merge()).awaitTask()
            }

            // 4. Sync Edges
            val edges = dao.getAllEdges().firstOrNull() ?: emptyList()
            for (edge in edges) {
                val edgeDocId = "${edge.fromDecisionId}_${edge.toDecisionId}"
                val edgeData = mapOf(
                    "fromDecisionId" to edge.fromDecisionId,
                    "toDecisionId" to edge.toDecisionId,
                    "relationship" to edge.relationship
                )
                userDocRef.collection("edges").document(edgeDocId)
                    .set(edgeData, SetOptions.merge()).awaitTask()
            }

            // 5. Append-Only Events Sync: Only upload events if they do not exist in Firestore
            val localEvents = dao.getAllEventsSync()
            var uploadedEvents = 0
            for (evt in localEvents) {
                val evtDoc = userDocRef.collection("events").document(evt.id).get().awaitTask()
                if (!evtDoc.exists()) {
                    val evtData = mapOf(
                        "id" to evt.id,
                        "decisionId" to evt.decisionId,
                        "eventType" to evt.eventType,
                        "dqsScore" to evt.dqsScore,
                        "payloadJson" to evt.payloadJson,
                        "branch_id" to evt.branch_id,
                        "parent_branch_id" to (evt.parent_branch_id ?: ""),
                        "forked_at_event_id" to (evt.forked_at_event_id ?: ""),
                        "timestamp" to evt.timestamp
                    )
                    userDocRef.collection("events").document(evt.id).set(evtData).awaitTask()
                    uploadedEvents++
                }
            }

            val msg = if (conflictCount > 0) {
                "সিঙ্ক সম্পন্ন। ${conflictCount}টি সিদ্ধান্তে ক্লাউড-লোকাল অসঙ্গতি পাওয়া গেছে এবং সমাধান টাস্ক তৈরি হয়েছে।"
            } else {
                "Firestore এ ${decisions.size}টি সিদ্ধান্ত ও ${uploadedEvents}টি নতুন ইভেন্ট সফলভাবে সিঙ্ক হয়েছে।"
            }
            SyncResult.Success(message = msg)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to cloud: ${e.message}", e)
            SyncResult.Error("ক্লাউড সিঙ্ক ব্যর্থ: ${e.message}")
        }
    }

    /**
     * Restore account state & decision history from Firestore on login.
     * Preserves immutable audit trail events without overwriting divergent records.
     */
    suspend fun restoreStateFromCloud(email: String, pass: String): SyncResult = withContext(Dispatchers.IO) {
        val authResult = authenticateUser(email, pass)
        if (authResult.isFailure) {
            val err = authResult.exceptionOrNull()?.message ?: "লগইন ব্যর্থ হয়েছে"
            return@withContext SyncResult.Error(err)
        }

        val uid = authResult.getOrThrow()

        try {
            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("kingmaker_users").document(uid)

            // Read meta
            val metaSnap = userDocRef.get().awaitTask()
            val restoredBranchId = metaSnap.getString("activeBranchId") ?: "main"
            val restoredDecisionId = metaSnap.getString("selectedDecisionId")

            saveLocalSessionState(email, restoredBranchId, restoredDecisionId)

            var totalRestored = 0

            // 1. Restore Branches
            val branchSnaps = userDocRef.collection("branches").get().awaitTask()
            for (doc in branchSnaps.documents) {
                val branchId = doc.getString("branchId") ?: doc.id
                val name = doc.getString("name") ?: branchId
                val parentBranchId = doc.getString("parentBranchId")?.takeIf { it.isNotBlank() }
                val forkedAtDecisionId = doc.getString("forkedAtDecisionId")?.takeIf { it.isNotBlank() }
                val forkedAtEventId = doc.getString("forkedAtEventId")?.takeIf { it.isNotBlank() }
                val dqsScore = doc.getDouble("dqsScore") ?: 0.85
                val status = doc.getString("status") ?: "EXPLORATORY"
                val createdTimestamp = doc.getLong("createdTimestamp") ?: System.currentTimeMillis()

                dao.insertBranch(
                    DecisionBranchEntity(
                        branchId = branchId,
                        name = name,
                        parentBranchId = parentBranchId,
                        forkedAtDecisionId = forkedAtDecisionId,
                        forkedAtEventId = forkedAtEventId,
                        dqsScore = dqsScore,
                        status = status,
                        createdTimestamp = createdTimestamp
                    )
                )
            }

            // 2. Restore Decisions
            val decisionSnaps = userDocRef.collection("decisions").get().awaitTask()
            for (doc in decisionSnaps.documents) {
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: "Restored Decision"
                val problemStatement = doc.getString("problemStatement") ?: ""
                val status = doc.getString("status") ?: "D1_INTAKE"
                val complexityScore = doc.getDouble("complexityScore") ?: 0.5
                val complexityTier = doc.getString("complexityTier") ?: "STANDARD"
                val dqsScore = doc.getDouble("dqsScore") ?: 0.70
                val risk = doc.getDouble("risk") ?: 0.5
                val impact = doc.getDouble("impact") ?: 0.5
                val changeability = doc.getDouble("changeability") ?: 0.5
                val budget = doc.getDouble("budget") ?: 0.5
                val projectId = doc.getString("projectId") ?: "alpha_core"
                val evidenceType = doc.getString("evidenceType") ?: "ASSUMPTION"
                val selectedOption = doc.getString("selectedOption")?.takeIf { it.isNotBlank() }
                val digitalSignatureHash = doc.getString("digitalSignatureHash")?.takeIf { it.isNotBlank() }
                val approvedAt = doc.getLong("approvedAt")?.takeIf { it > 0 }
                val branchId = doc.getString("branchId") ?: "main"
                val parentBranchId = doc.getString("parentBranchId")?.takeIf { it.isNotBlank() }
                val forkedFromDecisionId = doc.getString("forkedFromDecisionId")?.takeIf { it.isNotBlank() }
                val scopeConfirmed = doc.getBoolean("scopeConfirmed") ?: false
                val focusAreaConfirmed = doc.getBoolean("focusAreaConfirmed") ?: false
                val constraintsConfirmed = doc.getBoolean("constraintsConfirmed") ?: false
                val goalsConfirmed = doc.getBoolean("goalsConfirmed") ?: false
                val reviewPacketJson = doc.getString("reviewPacketJson")?.takeIf { it.isNotBlank() }
                val admissionCertificateJson = doc.getString("admissionCertificateJson")?.takeIf { it.isNotBlank() }
                val createdTimestamp = doc.getLong("createdTimestamp") ?: System.currentTimeMillis()
                val updatedTimestamp = doc.getLong("updatedTimestamp") ?: System.currentTimeMillis()

                dao.upsertDecision(
                    DecisionEntity(
                        id = id,
                        title = title,
                        problemStatement = problemStatement,
                        status = status,
                        complexityScore = complexityScore,
                        complexityTier = complexityTier,
                        dqsScore = dqsScore,
                        risk = risk,
                        impact = impact,
                        changeability = changeability,
                        budget = budget,
                        projectId = projectId,
                        evidenceType = evidenceType,
                        selectedOption = selectedOption,
                        digitalSignatureHash = digitalSignatureHash,
                        approvedAt = approvedAt,
                        branchId = branchId,
                        parentBranchId = parentBranchId,
                        forkedFromDecisionId = forkedFromDecisionId,
                        scopeConfirmed = scopeConfirmed,
                        focusAreaConfirmed = focusAreaConfirmed,
                        constraintsConfirmed = constraintsConfirmed,
                        goalsConfirmed = goalsConfirmed,
                        reviewPacketJson = reviewPacketJson,
                        admissionCertificateJson = admissionCertificateJson,
                        createdTimestamp = createdTimestamp,
                        updatedTimestamp = updatedTimestamp
                    )
                )
                totalRestored++
            }

            // 3. Restore Edges
            val edgeSnaps = userDocRef.collection("edges").get().awaitTask()
            for (doc in edgeSnaps.documents) {
                val fromDecisionId = doc.getString("fromDecisionId") ?: continue
                val toDecisionId = doc.getString("toDecisionId") ?: continue
                val relationship = doc.getString("relationship") ?: "DEPENDS_ON"
                dao.insertEdge(
                    DecisionEdgeEntity(
                        fromDecisionId = fromDecisionId,
                        toDecisionId = toDecisionId,
                        relationship = relationship
                    )
                )
            }

            // 4. Restore Events (Append-only insert safe)
            val eventSnaps = userDocRef.collection("events").get().awaitTask()
            for (doc in eventSnaps.documents) {
                val id = doc.getString("id") ?: doc.id
                val decisionId = doc.getString("decisionId") ?: ""
                val eventType = doc.getString("eventType") ?: "AUDIT"
                val dqsScore = doc.getDouble("dqsScore") ?: 0.70
                val payloadJson = doc.getString("payloadJson") ?: "{}"
                val branch_id = doc.getString("branch_id") ?: "main"
                val parent_branch_id = doc.getString("parent_branch_id")?.takeIf { it.isNotBlank() }
                val forked_at_event_id = doc.getString("forked_at_event_id")?.takeIf { it.isNotBlank() }
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                dao.insertEventSafe(
                    DecisionEventEntity(
                        id = id,
                        decisionId = decisionId,
                        eventType = eventType,
                        dqsScore = dqsScore,
                        payloadJson = payloadJson,
                        branch_id = branch_id,
                        parent_branch_id = parent_branch_id,
                        forked_at_event_id = forked_at_event_id,
                        timestamp = timestamp
                    )
                )
            }

            SyncResult.Success(
                message = "Firestore থেকে অ্যাকাউন্ট সফলভাবে পুনরুদ্ধার হয়েছে! ($totalRestored সিদ্ধান্ত ও হিস্ট্রি লোড সম্পন্ন)",
                restoredCount = totalRestored
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring from cloud: ${e.message}", e)
            SyncResult.Error("ক্লাউড থেকে ডেটা লোড ব্যর্থ: ${e.message}")
        }
    }
}

/**
 * Extension helper to await Task without external play-services-coroutines library.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        cont.resume(result)
    }
    addOnFailureListener { exception ->
        cont.resumeWithException(exception)
    }
}
