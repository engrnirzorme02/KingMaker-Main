package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * KingMaker v7.0 Canonical Decision Lifecycle States.
 * Section 28 & 40:
 * DRAFT -> CONTEXT_REQUIRED -> FRAMING -> READY_FOR_DEBATE -> DEBATING -> CRITIQUE ->
 * SYNTHESIS -> HUMAN_REVIEW -> APPROVED -> ADR_PUBLISHED -> BLUEPRINT_UPDATED -> OUTCOME_TRACKING
 * Branching: REJECTED, DEFERRED, REVISION_REQUESTED
 */
object DecisionStatus {
    const val DRAFT = "DRAFT"
    const val CONTEXT_REQUIRED = "CONTEXT_REQUIRED"
    const val FRAMING = "FRAMING"
    const val READY_FOR_DEBATE = "READY_FOR_DEBATE"
    const val DEBATING = "DEBATING"
    const val CRITIQUE = "CRITIQUE"
    const val SYNTHESIS = "SYNTHESIS"
    const val HUMAN_REVIEW = "HUMAN_REVIEW"
    const val APPROVED = "APPROVED"
    const val ADR_PUBLISHED = "ADR_PUBLISHED"
    const val BLUEPRINT_UPDATED = "BLUEPRINT_UPDATED"
    const val OUTCOME_TRACKING = "OUTCOME_TRACKING"
    const val REJECTED = "REJECTED"
    const val DEFERRED = "DEFERRED"
    const val REVISION_REQUESTED = "REVISION_REQUESTED"

    // Legacy aliases for backward compatibility with existing tests
    const val D1_INTAKE = DRAFT
    const val D2_FRAMING = FRAMING
    const val D3_CONFIRMATION = READY_FOR_DEBATE
    const val D4_DEBATE = DEBATING
    const val D5_CRITIQUE = CRITIQUE
    const val D6_SYNTHESIS = SYNTHESIS
    const val D7_REVIEW = HUMAN_REVIEW
}

/**
 * KingMaker v7.0 Typed Epistemology Claims (Section 6 & 47).
 * - UNKNOWN is a valid first-class state.
 * - ASSUMPTION cannot be silently rendered as FACT.
 * - INFERENCE cannot become evidence merely because a model is confident.
 */
enum class ClaimType {
    FACT,
    CONSTRAINT,
    PREFERENCE,
    INFERENCE,
    RECOMMENDATION,
    RISK,
    ASSUMPTION,
    UNKNOWN,
    UNVERIFIED
}

// Backward compatibility alias for existing code
typealias ProvenanceType = ClaimType

/**
 * Section 46.2: Explicit Provenance Modes.
 * SIMULATED, REPLAY, PROVIDER_BACKED.
 * Must appear consistently in UI, never conflated.
 */
enum class ProvenanceMode {
    SIMULATED,
    REPLAY,
    PROVIDER_BACKED
}

/**
 * Section 58: Outcome Divergence Classification.
 */
enum class OutcomeDivergence {
    ALIGNED,
    MINOR_DRIFT,
    MAJOR_DRIFT,
    DECISION_INVALIDATION,
    INSUFFICIENT_DATA
}

/**
 * Section 52.4: Conflict Classes for Offline-to-Online Reconciliation.
 */
enum class ConflictClass {
    NON_OVERLAPPING,
    SAME_NODE_FIELD_CONFLICT,
    SAME_EDGE_CONFLICT,
    TOPOLOGY_CONFLICT,
    CYCLE_CONFLICT,
    POLICY_APPROVAL_INVALIDATION,
    HISTORICAL_SUPERSESSION
}

/**
 * 9-Dimensional Quality Vector (Section 9 & 48).
 * Replaces legacy single scalar DQS.
 */
data class QualityVector(
    val evidenceStrength: Double,     // 0.0 to 1.0 (grounded vs ungrounded)
    val frameCompleteness: Double,    // 0.0 to 1.0 (objective, non-goals, constraints, criteria)
    val constraintFit: Double,        // 0.0 to 1.0 (satisfies non-negotiable boundaries)
    val optionCoverage: Double,       // 0.0 to 1.0 (diversity of realistic alternatives)
    val reversibility: Double,        // 0.0 to 1.0 (two-way door vs one-way door)
    val riskExposure: Double,         // 0.0 to 1.0 (downside vulnerability, lower = safer)
    val disagreement: Double,         // 0.0 to 1.0 (extent of specialist dissent)
    val validationReadiness: Double,  // 0.0 to 1.0 (clarity of tests and falsifiability)
    val complexityPenalty: Double,    // 0.0 to 1.0 (cognitive & architectural bloat penalty)
    val compositeHeuristic: Double,   // Diagnostic summary only; never automatic approval
    val explanation: String
) {
    companion object {
        fun default(): QualityVector = QualityVector(
            evidenceStrength = 0.50,
            frameCompleteness = 0.50,
            constraintFit = 0.50,
            optionCoverage = 0.50,
            reversibility = 0.50,
            riskExposure = 0.50,
            disagreement = 0.30,
            validationReadiness = 0.50,
            complexityPenalty = 0.20,
            compositeHeuristic = 0.50,
            explanation = "Initial baseline quality assessment under PolicyVersion v7.0."
        )
    }
}

@Entity(
    tableName = "decisions",
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["status"]),
        Index(value = ["branchId"]),
        Index(value = ["revisionHash"])
    ]
)
data class DecisionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val problemStatement: String,
    val status: String,
    val complexityScore: Double, // 0.0 to 1.0
    val complexityTier: String,  // T1_LIGHT, T2_STANDARD, T3_COMPREHENSIVE
    val dqsScore: Double,        // Legacy compatibility composite (0.0 to 1.0)
    val risk: Double,
    val impact: Double,
    val changeability: Double,
    val budget: Double,
    val projectId: String = "default-workspace",
    val evidenceType: String = "HEURISTIC",
    val selectedOption: String? = null,
    val digitalSignatureHash: String? = null,
    val approvedAt: Long? = null,
    val branchId: String = "main",
    val parentBranchId: String? = null,
    val forkedFromDecisionId: String? = null,

    // Framing & Consequential Guards (Steps 1-4)
    val scopeConfirmed: Boolean = false,
    val focusAreaConfirmed: Boolean = false,
    val constraintsConfirmed: Boolean = false,
    val goalsConfirmed: Boolean = false,

    // Review Packet & Certificate JSON
    val reviewPacketJson: String? = null,
    val admissionCertificateJson: String? = null,

    // KingMaker v7.0 Extensions
    val revisionNumber: Int = 1,
    val revisionHash: String? = null,
    val qualityVectorJson: String? = null,
    val preMortemRationale: String? = null,
    val approvalRationale: String? = null,
    val provenanceMode: String = "SIMULATED", // SIMULATED, REPLAY, PROVIDER_BACKED
    val policyVersion: String = "v7.0-personal",

    // Outcome tracking (Section 58)
    val expectedOutcome: String? = null,
    val observedOutcome: String? = null,
    val outcomeDivergence: String? = null, // ALIGNED, MINOR_DRIFT, MAJOR_DRIFT, DECISION_INVALIDATION
    val outcomeReviewDate: Long? = null,

    val createdTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Append-Only Event Log for SQLite (WAL Mode).
 * Invariant I-04 & I-06: Immutable governance events with revision hash and provenance.
 */
@Entity(
    tableName = "decision_events",
    indices = [
        Index(value = ["decisionId"]),
        Index(value = ["timestamp"]),
        Index(value = ["branch_id"])
    ]
)
data class DecisionEventEntity(
    @PrimaryKey val id: String,
    val decisionId: String,
    val eventType: String,
    val dqsScore: Double,
    val payloadJson: String,
    @ColumnInfo(name = "branch_id", defaultValue = "main")
    val branch_id: String = "main",
    @ColumnInfo(name = "parent_branch_id")
    val parent_branch_id: String? = null,
    @ColumnInfo(name = "forked_at_event_id")
    val forked_at_event_id: String? = null,
    val revisionHash: String? = null,
    val policyVersion: String = "v7.0-personal",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Typed Decision Graph Edges (Section 12 & 51).
 * Relationships: DEPENDS_ON, CONSTRAINS, SUPPORTS, SUPERSEDES, CONTRADICTS, DERIVED_FROM, AFFECTS
 */
@Entity(
    tableName = "decision_edges",
    primaryKeys = ["fromDecisionId", "toDecisionId"],
    indices = [Index(value = ["fromDecisionId"]), Index(value = ["toDecisionId"])]
)
data class DecisionEdgeEntity(
    val fromDecisionId: String,
    val toDecisionId: String,
    val relationship: String = "DEPENDS_ON"
)

/**
 * Git-Style Decision Branch Meta Entity.
 */
@Entity(tableName = "decision_branches")
data class DecisionBranchEntity(
    @PrimaryKey val branchId: String,
    val name: String,
    val parentBranchId: String? = null,
    val forkedAtDecisionId: String? = null,
    val forkedAtEventId: String? = null,
    val dqsScore: Double = 0.85,
    val status: String = "EXPLORATORY", // MAIN, EXPLORATORY, MERGED
    val createdTimestamp: Long = System.currentTimeMillis()
)

/**
 * Section 12 & 51: Resolution Task for Cycles.
 * Deduplicated by revisionHash + sorted nodeIds + sorted cycleEdgeIds.
 * Cycle detection never silently resolves; requires human authorization.
 */
@Entity(tableName = "resolution_tasks")
data class ResolutionTaskEntity(
    @PrimaryKey val id: String,
    val cycleNodes: String,
    val weakestNodeId: String,
    val reason: String,
    val status: String = "PENDING", // PENDING, RESOLVED
    val resolutionNote: String? = null,
    val proposedAction: String? = null,
    val cycleHash: String? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val resolvedTimestamp: Long? = null
)
