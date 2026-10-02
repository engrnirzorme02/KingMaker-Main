package com.example.data.repository

import com.example.core.graph.CycleDetectionResult
import com.example.core.graph.DependencyGraphEngine
import com.example.core.graph.GraphEdge
import com.example.core.graph.SemanticGraphDiff
import com.example.core.graph.WhatIfSimulationResult
import com.example.core.math.AdmissionTestResult
import com.example.core.math.DQSInput
import com.example.core.math.DecisionMathEngine
import com.example.core.math.FinalizationCertificate
import com.example.data.local.DecisionBranchEntity
import com.example.data.local.DecisionDao
import com.example.data.local.DecisionEdgeEntity
import com.example.data.local.DecisionEntity
import com.example.data.local.DecisionEventEntity
import com.example.data.local.DecisionStatus
import com.example.data.local.OutcomeDivergence
import com.example.data.local.QualityVector
import com.example.data.local.ResolutionTaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

data class ForkResult(
    val branchId: String,
    val forkedDecisionId: String
)

class DecisionRepository(private val dao: DecisionDao) {

    fun getDecisionsForProject(projectId: String): Flow<List<DecisionEntity>> =
        dao.getDecisionsByProject(projectId)

    fun getAllDecisions(): Flow<List<DecisionEntity>> =
        dao.getAllDecisions()

    fun getDecisionsByBranch(branchId: String): Flow<List<DecisionEntity>> =
        dao.getDecisionsByBranch(branchId)

    fun getAllBranches(): Flow<List<DecisionBranchEntity>> =
        dao.getAllBranches()

    fun getDecisionById(id: String): Flow<DecisionEntity?> =
        dao.getDecisionById(id)

    suspend fun getDecisionByIdSync(id: String): DecisionEntity? = withContext(Dispatchers.IO) {
        dao.getDecisionByIdSync(id)
    }

    fun getEventsForDecision(decisionId: String): Flow<List<DecisionEventEntity>> =
        dao.getEventsForDecision(decisionId)

    fun getEventsForDecisionAndBranch(decisionId: String, branchId: String): Flow<List<DecisionEventEntity>> =
        dao.getEventsForDecisionAndBranch(decisionId, branchId)

    fun getAllEdges(): Flow<List<DecisionEdgeEntity>> =
        dao.getAllEdges()

    fun getPendingResolutionTasks(): Flow<List<ResolutionTaskEntity>> =
        dao.getPendingResolutionTasks()

    /**
     * D1 Intake Initialization:
     * Generates collision-safe UUID.
     * Computes deterministic canonical revision hash (RFC 8785) & QualityVector.
     * Appends immutable event to WAL.
     */
    suspend fun createDecisionStream(
        title: String,
        problemStatement: String,
        risk: Double,
        impact: Double,
        changeability: Double,
        budget: Double,
        projectId: String = "alpha_core",
        branchId: String = "main"
    ): String = withContext(Dispatchers.IO) {
        val id = "DEC-${UUID.randomUUID()}"
        val complexity = DecisionMathEngine.calculateComplexity(risk, impact, changeability, budget)
        val initialDqs = DecisionMathEngine.calculateNormalizedDQS(
            DQSInput(
                evidence = 0.50,
                trust = 0.70,
                riskMitigation = 0.40,
                uncertainty = 0.40,
                agreement = 0.50,
                critiqueResolved = 0.30,
                valueAlignment = 0.70
            )
        )

        val revisionHash = DecisionMathEngine.calculateRevisionHash(
            decisionId = id,
            title = title,
            problemStatement = problemStatement,
            options = listOf("Primary candidate", "Status quo"),
            constraints = emptyList(),
            criteria = listOf("Latency", "Cost", "Durability"),
            evidenceType = "HEURISTIC"
        )

        val initialQv = DecisionMathEngine.evaluateQualityVector(
            evidenceType = "HEURISTIC",
            hasConfirmedFraming = false,
            constraintsCount = 0,
            optionsCount = 2,
            changeability = changeability,
            risk = risk,
            specialistAgreement = 0.60,
            validationPass = false,
            complexityScore = complexity.score
        )

        val decision = DecisionEntity(
            id = id,
            title = title,
            problemStatement = problemStatement,
            status = DecisionStatus.D1_INTAKE,
            complexityScore = complexity.score,
            complexityTier = complexity.tier.name,
            dqsScore = initialDqs,
            risk = risk,
            impact = impact,
            changeability = changeability,
            budget = budget,
            projectId = projectId,
            evidenceType = "HEURISTIC",
            branchId = branchId,
            scopeConfirmed = false,
            focusAreaConfirmed = false,
            constraintsConfirmed = false,
            goalsConfirmed = false,
            revisionNumber = 1,
            revisionHash = revisionHash,
            qualityVectorJson = "{\"composite\": ${initialQv.compositeHeuristic}, \"explanation\": \"${initialQv.explanation}\"}",
            provenanceMode = "SIMULATED",
            policyVersion = "v7.0-personal"
        )

        val event = DecisionEventEntity(
            id = "EVT-${UUID.randomUUID()}",
            decisionId = id,
            eventType = "INTAKE_INITIALIZED",
            dqsScore = initialDqs,
            payloadJson = "{\"title\": \"$title\", \"tier\": \"${complexity.tier.name}\", \"complexity\": ${complexity.score}, \"dqs\": $initialDqs, \"revHash\": \"$revisionHash\"}",
            branch_id = branchId,
            revisionHash = revisionHash,
            policyVersion = "v7.0-personal"
        )

        dao.insertEvent(event)
        dao.upsertDecision(decision)
        id
    }

    /**
     * D2 Framing & Taxonomy:
     * Updates problem framing, constraints, and recomputes canonical revision hash & quality vector.
     */
    suspend fun updateFramingAndTaxonomy(
        id: String,
        tagsJson: String,
        risk: Double,
        impact: Double,
        changeability: Double,
        budget: Double
    ) = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: return@withContext
        val complexity = DecisionMathEngine.calculateComplexity(risk, impact, changeability, budget)

        val evidenceScore = when (current.evidenceType.uppercase()) {
            "AXIOMATIC" -> 0.95
            "EMPIRICAL" -> 0.85
            "HEURISTIC" -> 0.65
            "ASSUMPTION" -> 0.45
            else -> 0.30
        }

        val updatedDqs = DecisionMathEngine.calculateNormalizedDQS(
            DQSInput(
                evidence = evidenceScore,
                trust = 0.75,
                riskMitigation = (1.0 - risk * 0.5).coerceIn(0.0, 1.0),
                uncertainty = 0.55,
                agreement = 0.60,
                critiqueResolved = 0.40,
                valueAlignment = 0.75
            )
        )

        val newRevHash = DecisionMathEngine.calculateRevisionHash(
            decisionId = id,
            title = current.title,
            problemStatement = current.problemStatement,
            options = listOf("Primary candidate", "Status quo"),
            constraints = listOf("Tags extracted from D2"),
            criteria = listOf("Latency", "Durability", "Cost"),
            evidenceType = current.evidenceType
        )

        val updatedQv = DecisionMathEngine.evaluateQualityVector(
            evidenceType = current.evidenceType,
            hasConfirmedFraming = true,
            constraintsCount = 2,
            optionsCount = 2,
            changeability = changeability,
            risk = risk,
            specialistAgreement = 0.70,
            validationPass = false,
            complexityScore = complexity.score
        )

        val updated = current.copy(
            status = DecisionStatus.D2_FRAMING,
            complexityScore = complexity.score,
            complexityTier = complexity.tier.name,
            dqsScore = updatedDqs,
            risk = risk,
            impact = impact,
            changeability = changeability,
            budget = budget,
            revisionNumber = current.revisionNumber + 1,
            revisionHash = newRevHash,
            qualityVectorJson = "{\"composite\": ${updatedQv.compositeHeuristic}, \"explanation\": \"${updatedQv.explanation}\"}",
            updatedTimestamp = System.currentTimeMillis()
        )

        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = "FRAMING_TAXONOMY_EXTRACTED",
                dqsScore = updatedDqs,
                payloadJson = tagsJson,
                branch_id = current.branchId,
                revisionHash = newRevHash,
                policyVersion = "v7.0-personal"
            )
        )
        dao.upsertDecision(updated)
    }

    /**
     * D3 Human Confirmation Gate:
     * D3 requires D2 Framing completed and explicit confirmation of:
     * scope, focus area, constraints, goals.
     */
    suspend fun confirmD3Gate(
        id: String,
        scopeConfirmed: Boolean,
        focusAreaConfirmed: Boolean,
        constraintsConfirmed: Boolean,
        goalsConfirmed: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: return@withContext false
        if (current.status != DecisionStatus.D2_FRAMING && current.status != DecisionStatus.D3_CONFIRMATION) {
            throw IllegalStateException("Pipeline Violation: D2 Framing must be completed before D3 Confirmation (Current state: ${current.status}).")
        }

        val allConfirmed = scopeConfirmed && focusAreaConfirmed && constraintsConfirmed && goalsConfirmed
        val newStatus = if (allConfirmed) DecisionStatus.D3_CONFIRMATION else DecisionStatus.D2_FRAMING

        val updated = current.copy(
            status = newStatus,
            scopeConfirmed = scopeConfirmed,
            focusAreaConfirmed = focusAreaConfirmed,
            constraintsConfirmed = constraintsConfirmed,
            goalsConfirmed = goalsConfirmed,
            updatedTimestamp = System.currentTimeMillis()
        )

        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = "D3_CONFIRMATION_EVALUATED",
                dqsScore = current.dqsScore,
                payloadJson = "{\"scopeConfirmed\": $scopeConfirmed, \"focusAreaConfirmed\": $focusAreaConfirmed, \"constraintsConfirmed\": $constraintsConfirmed, \"goalsConfirmed\": $goalsConfirmed, \"allConfirmed\": $allConfirmed}",
                branch_id = current.branchId,
                revisionHash = current.revisionHash,
                policyVersion = "v7.0-personal"
            )
        )
        dao.upsertDecision(updated)
        allConfirmed
    }

    /**
     * D4 Debate & D5 Critique Advancement:
     * Must have completed D3 human confirmation before opening War Room debate.
     */
    suspend fun advanceWarRoomRound(
        id: String,
        round: Int,
        newDqs: Double,
        summaryPayload: String
    ) = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: return@withContext
        if (!current.scopeConfirmed || !current.focusAreaConfirmed || !current.constraintsConfirmed || !current.goalsConfirmed || current.status == DecisionStatus.D1_INTAKE || current.status == DecisionStatus.D2_FRAMING) {
            throw IllegalStateException("State Guard Violation: Cannot advance to War Room without completing D3 Human Confirmation.")
        }

        val newStatus = when (round) {
            1 -> DecisionStatus.D4_DEBATE
            2 -> DecisionStatus.D5_CRITIQUE
            else -> DecisionStatus.D5_CRITIQUE
        }

        val updated = current.copy(
            status = newStatus,
            dqsScore = newDqs,
            updatedTimestamp = System.currentTimeMillis()
        )

        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = if (round == 1) "WAR_ROOM_DEBATE_ROUND_1" else "WAR_ROOM_CRITIQUE_ROUND_$round",
                dqsScore = newDqs,
                payloadJson = summaryPayload,
                branch_id = current.branchId,
                revisionHash = current.revisionHash,
                policyVersion = "v7.0-personal"
            )
        )
        dao.upsertDecision(updated)
    }

    suspend fun recordStopRuleTriggered(
        id: String,
        previousDqs: Double,
        currentDqs: Double
    ) = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: return@withContext
        val delta = currentDqs - previousDqs
        val payload = "{\"previousDqs\": $previousDqs, \"currentDqs\": $currentDqs, \"delta\": $delta, \"message\": \"Refinement stopped due to delta < 0.05. Human action required if unresolved issues remain.\"}"
        val updated = current.copy(
            status = DecisionStatus.D6_SYNTHESIS,
            dqsScore = currentDqs,
            updatedTimestamp = System.currentTimeMillis()
        )
        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = "STOP_RULE_TRIGGERED",
                dqsScore = currentDqs,
                payloadJson = payload,
                branch_id = current.branchId,
                revisionHash = current.revisionHash,
                policyVersion = "v7.0-personal"
            )
        )
        dao.upsertDecision(updated)
    }

    suspend fun synthesizeReviewPacket(
        id: String,
        reviewPacketJson: String,
        finalDqs: Double
    ) = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: return@withContext

        val updated = current.copy(
            status = DecisionStatus.D7_REVIEW,
            dqsScore = finalDqs,
            reviewPacketJson = reviewPacketJson,
            updatedTimestamp = System.currentTimeMillis()
        )

        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = "D6_SYNTHESIS_PACKET_GENERATED",
                dqsScore = finalDqs,
                payloadJson = reviewPacketJson,
                branch_id = current.branchId,
                revisionHash = current.revisionHash,
                policyVersion = "v7.0-personal"
            )
        )
        dao.upsertDecision(updated)
    }

    suspend fun evaluateAdmissionAndIssueCertificate(
        id: String
    ): FinalizationCertificate? = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: return@withContext null
        val isD6Done = current.status == DecisionStatus.D6_SYNTHESIS || current.status == DecisionStatus.D7_REVIEW || current.status == DecisionStatus.APPROVED
        val hasPacket = !current.reviewPacketJson.isNullOrBlank()

        val checks = DecisionMathEngine.evaluateAdmissionTest(
            title = current.title,
            problemStatement = current.problemStatement,
            selectedOption = current.selectedOption,
            risk = current.risk,
            impact = current.impact,
            dqsScore = current.dqsScore,
            evidenceType = current.evidenceType,
            isD6SynthesisCompleted = isD6Done,
            hasReviewPacket = hasPacket
        )

        if (!checks.allPassed || !isD6Done || !hasPacket) {
            dao.insertEvent(
                DecisionEventEntity(
                    id = "EVT-${UUID.randomUUID()}",
                    decisionId = id,
                    eventType = "ADMISSION_TEST_FAILED",
                    dqsScore = current.dqsScore,
                    payloadJson = "{\"checks\": \"FAILED\", \"notes\": \"${checks.specificityNote} | ${checks.actionabilityNote} | ${checks.valueNote}\"}",
                    branch_id = current.branchId,
                    revisionHash = current.revisionHash,
                    policyVersion = "v7.0-personal"
                )
            )
            return@withContext null
        }

        val certificate = DecisionMathEngine.generateFinalizationCertificate(
            decisionId = id,
            admissionChecks = checks,
            finalDqs = current.dqsScore,
            isD6SynthesisCompleted = isD6Done,
            hasReviewPacket = hasPacket
        ) ?: return@withContext null

        val certJson = "{\"certId\":\"${certificate.certificateId}\",\"hash\":\"${certificate.certificateHash}\",\"issuedAt\":${certificate.issuedAt}}"
        val updated = current.copy(
            admissionCertificateJson = certJson,
            updatedTimestamp = System.currentTimeMillis()
        )

        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = "FINALIZATION_CERTIFICATE_ISSUED",
                dqsScore = current.dqsScore,
                payloadJson = certJson,
                branch_id = current.branchId,
                revisionHash = current.revisionHash,
                policyVersion = "v7.0-personal"
            )
        )
        dao.upsertDecision(updated)
        certificate
    }

    suspend fun resolveSmartQueryTradeoff(
        id: String,
        selectedChoice: String,
        rationale: String
    ) = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: return@withContext

        val updatedDqs = DecisionMathEngine.calculateNormalizedDQS(
            DQSInput(
                evidence = if (current.evidenceType == "AXIOMATIC") 0.90 else 0.75,
                trust = 0.85,
                riskMitigation = 0.80,
                uncertainty = 0.75,
                agreement = 0.85,
                critiqueResolved = 0.80,
                valueAlignment = 0.85
            )
        )

        val updated = current.copy(
            status = DecisionStatus.D6_SYNTHESIS,
            dqsScore = updatedDqs,
            selectedOption = selectedChoice,
            updatedTimestamp = System.currentTimeMillis()
        )

        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = "SMART_QUERY_TRADE_OFF_RESOLVED",
                dqsScore = updatedDqs,
                payloadJson = "{\"selected\": \"$selectedChoice\", \"rationale\": \"$rationale\", \"recoveredTo\": \"D6_SYNTHESIS\"}",
                branch_id = current.branchId,
                revisionHash = current.revisionHash,
                policyVersion = "v7.0-personal"
            )
        )
        dao.upsertDecision(updated)
    }

    /**
     * Section 53.4 & Invariant I-01, I-02: Human CEO Gate Attestation.
     * Requires:
     * - Status must be D7_REVIEW.
     * - Finalization certificate must exist.
     * - Rationale must be provided (>= 30 chars).
     * - Pre-mortem rationale stored for T3 decisions.
     * - Generates immutable SHA-256 signature hash.
     */
    suspend fun executeCeoAttestationSignature(
        id: String,
        signerName: String = "Authorized CEO / Chief Architect",
        rationale: String = "Approved with validated empirical evidence and policy compliance.",
        preMortem: String? = null,
        selectedOption: String? = null
    ): String = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: throw IllegalArgumentException("Decision not found")

        if (current.status != DecisionStatus.D7_REVIEW) {
            throw IllegalStateException("CEO Gate Rejected: Approval is strictly restricted to decisions currently in D7_REVIEW stage (Current: ${current.status}).")
        }

        if (current.admissionCertificateJson.isNullOrBlank()) {
            throw IllegalStateException("CEO Gate Rejected: Finalization Certificate is missing. Admission checks must pass before approval.")
        }

        if (rationale.trim().length < 30) {
            throw IllegalStateException("CEO Gate Rejected: Approval rationale must be at least 30 characters explaining the executive commitment.")
        }

        val now = System.currentTimeMillis()
        val rawToHash = "KINGMAKER_v70:${current.id}:${current.revisionHash ?: "HEAD"}:$signerName:$now:$rationale"
        val hash = sha256(rawToHash)

        val approved = current.copy(
            status = DecisionStatus.APPROVED,
            digitalSignatureHash = hash,
            approvedAt = now,
            approvalRationale = rationale,
            preMortemRationale = preMortem ?: current.preMortemRationale,
            selectedOption = selectedOption ?: current.selectedOption,
            updatedTimestamp = now
        )

        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = "CEO_ATTESTATION_COMMITTED",
                dqsScore = current.dqsScore,
                payloadJson = "{\"signer\": \"$signerName\", \"attestationHash\": \"$hash\", \"rationale\": \"$rationale\", \"preMortem\": \"$preMortem\", \"state\": \"APPROVED_LOCKED\"}",
                branch_id = current.branchId,
                revisionHash = current.revisionHash,
                policyVersion = current.policyVersion
            )
        )
        dao.upsertDecision(approved)
        hash
    }

    /**
     * Governance rejection (Section 4 & 10).
     */
    suspend fun rejectDecision(id: String, rationale: String) = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: return@withContext
        val updated = current.copy(status = DecisionStatus.REJECTED, updatedTimestamp = System.currentTimeMillis())
        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = "GOVERNANCE_DECISION_REJECTED",
                dqsScore = current.dqsScore,
                payloadJson = "{\"action\": \"REJECTED\", \"rationale\": \"$rationale\"}",
                branch_id = current.branchId,
                revisionHash = current.revisionHash,
                policyVersion = current.policyVersion
            )
        )
        dao.upsertDecision(updated)
    }

    /**
     * Governance deferral (Section 4 & 10).
     */
    suspend fun deferDecision(id: String, rationale: String) = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: return@withContext
        val updated = current.copy(status = DecisionStatus.DEFERRED, updatedTimestamp = System.currentTimeMillis())
        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = "GOVERNANCE_DECISION_DEFERRED",
                dqsScore = current.dqsScore,
                payloadJson = "{\"action\": \"DEFERRED\", \"rationale\": \"$rationale\"}",
                branch_id = current.branchId,
                revisionHash = current.revisionHash,
                policyVersion = current.policyVersion
            )
        )
        dao.upsertDecision(updated)
    }

    /**
     * Section 58 & Invariant I-20: Outcome Observation Loop.
     * Never rewrites decision-time quality.
     */
    suspend fun recordOutcomeObservation(
        id: String,
        expected: String,
        observed: String,
        divergence: OutcomeDivergence,
        followUpAction: String
    ) = withContext(Dispatchers.IO) {
        val current = dao.getDecisionByIdSync(id) ?: return@withContext
        val now = System.currentTimeMillis()
        val updated = current.copy(
            expectedOutcome = expected,
            observedOutcome = observed,
            outcomeDivergence = divergence.name,
            outcomeReviewDate = now,
            updatedTimestamp = now
        )
        dao.insertEvent(
            DecisionEventEntity(
                id = "EVT-${UUID.randomUUID()}",
                decisionId = id,
                eventType = "OUTCOME_OBSERVATION_RECORDED",
                dqsScore = current.dqsScore, // Unchanged! Invariant I-20
                payloadJson = "{\"expected\": \"$expected\", \"observed\": \"$observed\", \"divergence\": \"${divergence.name}\", \"action\": \"$followUpAction\"}",
                branch_id = current.branchId,
                revisionHash = current.revisionHash,
                policyVersion = current.policyVersion
            )
        )
        dao.upsertDecision(updated)
    }

    suspend fun forkDecisionTimeline(
        sourceDecisionId: String,
        newBranchName: String,
        exploratoryTitle: String
    ): ForkResult = withContext(Dispatchers.IO) {
        val sourceDecision = dao.getDecisionByIdSync(sourceDecisionId)
            ?: throw IllegalArgumentException("Source decision $sourceDecisionId does not exist.")

        val eligibleStatuses = setOf(
            DecisionStatus.D4_DEBATE,
            DecisionStatus.D5_CRITIQUE,
            DecisionStatus.D6_SYNTHESIS,
            DecisionStatus.D7_REVIEW,
            DecisionStatus.APPROVED
        )
        if (sourceDecision.status !in eligibleStatuses) {
            throw IllegalStateException("Forking Governance Violation: Decisions can only be forked once reaching D4+ maturity (Current: ${sourceDecision.status}).")
        }

        val uniqueSuffix = UUID.randomUUID().toString().take(6)
        val rawBranch = newBranchName.lowercase().replace(" ", "-").replace("/", "-").removePrefix("exp-")
        val cleanBranchId = "exp-$rawBranch-$uniqueSuffix"
        val now = System.currentTimeMillis()

        val events = dao.getEventsForDecision(sourceDecisionId).firstOrNull() ?: emptyList()
        val forkedAtEventId = events.lastOrNull()?.id ?: "EVT-ROOT"

        val existingBranch = dao.getBranchById(cleanBranchId)
        if (existingBranch != null) {
            throw IllegalStateException("Branch ID collision: Branch $cleanBranchId already exists.")
        }

        val newBranch = DecisionBranchEntity(
            branchId = cleanBranchId,
            name = newBranchName,
            parentBranchId = sourceDecision.branchId,
            forkedAtDecisionId = sourceDecisionId,
            forkedAtEventId = forkedAtEventId,
            dqsScore = sourceDecision.dqsScore,
            status = "EXPLORATORY",
            createdTimestamp = now
        )
        dao.insertBranch(newBranch)

        val forkedDecisionId = "${sourceDecision.id}-FORK-${UUID.randomUUID()}"
        val forkedDecision = sourceDecision.copy(
            id = forkedDecisionId,
            title = "[FORK] $exploratoryTitle",
            status = DecisionStatus.D4_DEBATE,
            dqsScore = sourceDecision.dqsScore,
            branchId = cleanBranchId,
            parentBranchId = sourceDecision.branchId,
            forkedFromDecisionId = sourceDecisionId,
            digitalSignatureHash = null,
            approvedAt = null,
            admissionCertificateJson = null,
            createdTimestamp = now,
            updatedTimestamp = now
        )

        val forkEvent = DecisionEventEntity(
            id = "EVT-${UUID.randomUUID()}",
            decisionId = forkedDecisionId,
            eventType = "BRANCH_FORKED",
            dqsScore = forkedDecision.dqsScore,
            payloadJson = "{\"source\": \"$sourceDecisionId\", \"branch\": \"$cleanBranchId\", \"parent\": \"${sourceDecision.branchId}\", \"forkedDecisionId\": \"$forkedDecisionId\"}",
            branch_id = cleanBranchId,
            parent_branch_id = sourceDecision.branchId,
            forked_at_event_id = forkedAtEventId,
            timestamp = now
        )

        dao.insertEvent(forkEvent)
        dao.upsertDecision(forkedDecision)
        dao.insertEdge(DecisionEdgeEntity(fromDecisionId = sourceDecisionId, toDecisionId = forkedDecisionId, relationship = "FORKED_TO"))

        ForkResult(branchId = cleanBranchId, forkedDecisionId = forkedDecisionId)
    }

    suspend fun addDependencyEdge(fromId: String, toId: String, relationship: String = "DEPENDS_ON") = withContext(Dispatchers.IO) {
        dao.insertEdge(DecisionEdgeEntity(fromDecisionId = fromId, toDecisionId = toId, relationship = relationship))
    }

    suspend fun removeDependencyEdge(fromId: String, toId: String) = withContext(Dispatchers.IO) {
        dao.deleteEdge(fromId, toId)
    }

    suspend fun calculateRippleEffect(decisionId: String): List<String> = withContext(Dispatchers.IO) {
        val edges = dao.getAllEdgesSync().map { GraphEdge(it.fromDecisionId, it.toDecisionId, it.relationship) }
        DependencyGraphEngine.calculateRippleEffect(decisionId, edges)
    }

    /**
     * Section 51.3: What-if Simulation without database mutation.
     */
    suspend fun simulateEdgeOperation(
        fromId: String,
        toId: String,
        relationship: String = "DEPENDS_ON",
        action: String = "ADD"
    ): WhatIfSimulationResult = withContext(Dispatchers.IO) {
        val decisions = dao.getAllDecisions().firstOrNull() ?: emptyList()
        val allNodeIds = decisions.map { it.id }.toSet()
        val edges = dao.getAllEdgesSync().map { GraphEdge(it.fromDecisionId, it.toDecisionId, it.relationship) }
        val candidate = GraphEdge(from = fromId, to = toId, relationship = relationship)
        DependencyGraphEngine.simulateEdgeOperation(edges, allNodeIds, candidate, action)
    }

    /**
     * Section 52: Semantic Graph Diff for offline-to-online reconciliation.
     */
    suspend fun computeGraphReconciliationDiff(localEdges: List<GraphEdge>): SemanticGraphDiff = withContext(Dispatchers.IO) {
        val serverEdges = dao.getAllEdgesSync().map { GraphEdge(it.fromDecisionId, it.toDecisionId, it.relationship) }
        val decisions = dao.getAllDecisions().firstOrNull() ?: emptyList()
        val allNodeIds = decisions.map { it.id }.toSet()
        DependencyGraphEngine.computeGraphReconciliationDiff(serverEdges, localEdges, allNodeIds)
    }

    suspend fun detectCyclesAndRecordTasks(): CycleDetectionResult = withContext(Dispatchers.IO) {
        val decisions = dao.getAllDecisions().firstOrNull() ?: emptyList()
        val decisionMap = decisions.associateBy { it.id }
        val allNodeIds = decisionMap.keys
        val edges = dao.getAllEdgesSync().map { GraphEdge(it.fromDecisionId, it.toDecisionId, it.relationship) }

        val detection = DependencyGraphEngine.detectCycles(allNodeIds, edges)

        if (detection.hasCycle) {
            val evidenceMap = decisionMap.mapValues { (_, dec) -> Pair(dec.evidenceType, dec.dqsScore) }
            val weakestNodeId = DependencyGraphEngine.identifyWeakestEvidenceNode(detection.cyclicNodeIds, evidenceMap) ?: detection.cyclicNodeIds.first()

            val taskId = "TASK-CYCLE-${detection.deduplicationKey.takeLast(8)}"
            val task = ResolutionTaskEntity(
                id = taskId,
                cycleNodes = detection.cyclicNodeIds.joinToString(","),
                weakestNodeId = weakestNodeId,
                reason = "Tarjan SCC cyclic deadlock detected across nodes: ${detection.cycles.joinToString(" -> ")}. Node '$weakestNodeId' identified as having weakest evidence.",
                status = "PENDING",
                proposedAction = "Break dependency or reframe edge from '$weakestNodeId'",
                cycleHash = detection.deduplicationKey,
                createdTimestamp = System.currentTimeMillis()
            )
            dao.insertResolutionTask(task)

            decisionMap[weakestNodeId]?.let { weakestDec ->
                val adjusted = weakestDec.copy(evidenceType = "ASSUMPTION")
                dao.upsertDecision(adjusted)
                dao.insertEvent(
                    DecisionEventEntity(
                        id = "EVT-${UUID.randomUUID()}",
                        decisionId = weakestNodeId,
                        eventType = "CYCLE_WEAKEST_NODE_IDENTIFIED",
                        dqsScore = weakestDec.dqsScore,
                        payloadJson = "{\"cycle\": \"${detection.cyclicNodeIds.joinToString(",")}\", \"action\": \"EVIDENCE_MARKED_ASSUMPTION\", \"taskId\": \"$taskId\"}",
                        branch_id = weakestDec.branchId,
                        revisionHash = weakestDec.revisionHash,
                        policyVersion = weakestDec.policyVersion
                    )
                )
            }
        }

        detection
    }

    suspend fun resolveCycleTask(taskId: String, resolutionNote: String, breakFromId: String? = null, breakToId: String? = null) = withContext(Dispatchers.IO) {
        dao.resolveTask(taskId, resolutionNote)
        if (breakFromId != null && breakToId != null) {
            dao.deleteEdge(breakFromId, breakToId)
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
