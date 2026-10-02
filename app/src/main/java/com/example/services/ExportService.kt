package com.example.services

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.local.DecisionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ExportService(
    private val context: Context,
    private val dao: DecisionDao
) {

    /**
     * Directive 15: Reconstructs the SQLite WAL event log into a structured JSON payload:
     * - Timestamps are true UTC (with 'Z').
     * - Accurate finalDQS representation for selected decision and aggregate branch DQS.
     * - Strict branch filtering.
     * - Clean JSON object / array payload parsing.
     */
    suspend fun generateStructuredJsonExport(
        projectId: String = "alpha_core",
        branchId: String = "main",
        selectedDecisionId: String? = null
    ): String = withContext(Dispatchers.IO) {
        val decisions = if (branchId == "ALL") {
            dao.getAllDecisions().firstOrNull() ?: emptyList()
        } else {
            dao.getDecisionsByBranch(branchId).firstOrNull() ?: emptyList()
        }

        val events = if (branchId == "ALL") {
            dao.getRecentEvents().firstOrNull() ?: emptyList()
        } else {
            dao.getEventsForBranch(branchId).firstOrNull() ?: emptyList()
        }

        val edges = dao.getAllEdgesSync()

        val selectedDecision = if (selectedDecisionId != null) {
            dao.getDecisionByIdSync(selectedDecisionId)
        } else {
            decisions.firstOrNull()
        }

        val branchAvgDqs = if (decisions.isNotEmpty()) {
            val avg = decisions.map { it.dqsScore }.average()
            String.format(Locale.US, "%.2f", avg).toDouble()
        } else 0.0

        val targetDqs = selectedDecision?.dqsScore ?: branchAvgDqs

        // True UTC ISO-8601 formatting
        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val nowStr = utcFormat.format(Date())

        val rootJson = JSONObject()

        // 1. Metadata
        val metadataJson = JSONObject().apply {
            put("projectId", projectId)
            put("exportedAtUTC", nowStr)
            put("activeBranch", branchId)
            put("targetDecisionId", selectedDecision?.id ?: "N/A")
            put("targetDecisionDQS", targetDqs)
            put("aggregateBranchDQS", branchAvgDqs)
            put("storageEngine", "SQLite 3 WAL Mode (Append-Only Event Store)")
            put("totalBranchDecisions", decisions.size)
            put("totalBranchEvents", events.size)
            put("aiEvaluationMode", "SIMULATED / OFFLINE EVALUATION")
        }
        rootJson.put("metadata", metadataJson)

        // 2. Dependency Graph
        val graphArray = JSONArray()
        edges.forEach { edge ->
            val edgeObj = JSONObject().apply {
                put("from", edge.fromDecisionId)
                put("to", edge.toDecisionId)
                put("relationship", edge.relationship)
            }
            graphArray.put(edgeObj)
        }
        rootJson.put("dependencyGraph", graphArray)

        // 3. ADR Records
        val adrArray = JSONArray()
        decisions.forEach { dec ->
            val decObj = JSONObject().apply {
                put("id", dec.id)
                put("title", dec.title)
                put("problemStatement", dec.problemStatement)
                put("status", dec.status)
                put("complexityTier", dec.complexityTier)
                put("complexityScore", dec.complexityScore)
                put("dqsScore", dec.dqsScore)
                put("evidenceType", dec.evidenceType)
                put("selectedOption", dec.selectedOption ?: "N/A")
                put("digitalSignatureHash", dec.digitalSignatureHash ?: "PENDING_CEO_GATE")
                put("branchId", dec.branchId)
                put("scopeConfirmed", dec.scopeConfirmed)
                put("focusAreaConfirmed", dec.focusAreaConfirmed)
                put("constraintsConfirmed", dec.constraintsConfirmed)
                put("goalsConfirmed", dec.goalsConfirmed)
                put("admissionCertificate", if (dec.admissionCertificateJson != null) {
                    try { JSONObject(dec.admissionCertificateJson) } catch (e: Exception) { dec.admissionCertificateJson }
                } else JSONObject.NULL)
                put("approvedAt", dec.approvedAt ?: JSONObject.NULL)
                put("createdTimestamp", dec.createdTimestamp)
                put("updatedTimestamp", dec.updatedTimestamp)
            }
            adrArray.put(decObj)
        }
        rootJson.put("adrRecords", adrArray)

        // 4. Immutable Event Log
        val eventArray = JSONArray()
        events.forEach { evt ->
            val evtObj = JSONObject().apply {
                put("id", evt.id)
                put("decisionId", evt.decisionId)
                put("eventType", evt.eventType)
                put("dqsScore", evt.dqsScore)
                put("branchId", evt.branch_id)
                put("parentBranchId", evt.parent_branch_id ?: JSONObject.NULL)
                put("forkedAtEventId", evt.forked_at_event_id ?: JSONObject.NULL)
                put("timestamp", evt.timestamp)
                put("payload", try {
                    JSONObject(evt.payloadJson)
                } catch (e: Exception) {
                    try {
                        JSONArray(evt.payloadJson)
                    } catch (e2: Exception) {
                        evt.payloadJson
                    }
                })
            }
            eventArray.put(evtObj)
        }
        rootJson.put("immutableEventLog", eventArray)

        rootJson.toString(2)
    }

    /**
     * Share structured JSON Living Blueprint directly to Android Share Sheet
     */
    suspend fun shareLivingBlueprintJson(projectId: String, branchId: String, selectedDecisionId: String? = null) {
        val jsonPayload = generateStructuredJsonExport(projectId, branchId, selectedDecisionId)
        withContext(Dispatchers.Main) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, "KingMaker_Living_Blueprint_${branchId}.json")
                putExtra(Intent.EXTRA_TEXT, jsonPayload)
                type = "application/json"
            }
            val chooser = Intent.createChooser(sendIntent, "Share Living Blueprint JSON")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    /**
     * Copy JSON to clipboard
     */
    suspend fun copyJsonToClipboard(projectId: String, branchId: String, selectedDecisionId: String? = null) {
        val jsonPayload = generateStructuredJsonExport(projectId, branchId, selectedDecisionId)
        withContext(Dispatchers.Main) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Living Blueprint JSON", jsonPayload)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Living Blueprint JSON copied to clipboard (UTC export)", Toast.LENGTH_SHORT).show()
        }
    }
}
