package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object DecisionStatus {
    const val D1_INTAKE = "D1_INTAKE"
    const val D2_FRAMING = "D2_FRAMING"
    const val D3_CONFIRMATION = "D3_CONFIRMATION"
    const val D4_DEBATE = "D4_DEBATE"
    const val D5_CRITIQUE = "D5_CRITIQUE"
    const val D6_SYNTHESIS = "D6_SYNTHESIS"
    const val D7_REVIEW = "D7_REVIEW"
    const val APPROVED = "APPROVED"
}

enum class ProvenanceType {
    VERIFIED,
    EXPERT_INFERENCE,
    ASSUMPTION,
    UNVERIFIED
}

@Entity(
    tableName = "decisions",
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["status"]),
        Index(value = ["branchId"])
    ]
)
data class DecisionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val problemStatement: String,
    val status: String, // D1_INTAKE -> D2_FRAMING -> D3_CONFIRMATION -> D4_DEBATE -> D5_CRITIQUE -> D6_SYNTHESIS -> D7_REVIEW -> APPROVED
    val complexityScore: Double, // 0.0 to 1.0
    val complexityTier: String,  // LIGHT, STANDARD, RIGOROUS, MAXIMUM
    val dqsScore: Double,        // 0.0 to 1.0
    val risk: Double,
    val impact: Double,
    val changeability: Double,
    val budget: Double,
    val projectId: String,
    val evidenceType: String,    // AXIOMATIC, EMPIRICAL, HEURISTIC, ASSUMPTION, UNVERIFIED
    val selectedOption: String? = null,
    val digitalSignatureHash: String? = null,
    val approvedAt: Long? = null,
    val branchId: String = "main",
    val parentBranchId: String? = null,
    val forkedFromDecisionId: String? = null,

    // D3 Confirmation State Guards
    val scopeConfirmed: Boolean = false,
    val focusAreaConfirmed: Boolean = false,
    val constraintsConfirmed: Boolean = false,
    val goalsConfirmed: Boolean = false,

    // D6 Review Packet & D7 Finalization Certificate
    val reviewPacketJson: String? = null,
    val admissionCertificateJson: String? = null,

    val createdTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Append-Only Event Log for SQLite (WAL Mode).
 * Invariant 3: Decisions are never overwritten. Every state-changing action appends
 * an immutable revision event in local SQLite.
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
    val timestamp: Long = System.currentTimeMillis()
)

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
 * Directive 8 & Invariant 7: Resolution Task for Cycles.
 * Cycle detection never silently resolves automatically. It records a human Resolution Task.
 */
@Entity(tableName = "resolution_tasks")
data class ResolutionTaskEntity(
    @PrimaryKey val id: String,
    val cycleNodes: String,
    val weakestNodeId: String,
    val reason: String,
    val status: String = "PENDING", // PENDING, RESOLVED
    val resolutionNote: String? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val resolvedTimestamp: Long? = null
)
