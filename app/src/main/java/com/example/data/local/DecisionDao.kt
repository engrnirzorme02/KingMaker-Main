package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DecisionDao {
    @Query("SELECT * FROM decisions WHERE projectId = :projectId ORDER BY updatedTimestamp DESC")
    fun getDecisionsByProject(projectId: String): Flow<List<DecisionEntity>>

    @Query("SELECT * FROM decisions WHERE projectId = :projectId AND branchId = :branchId ORDER BY updatedTimestamp DESC")
    fun getDecisionsByProjectAndBranch(projectId: String, branchId: String): Flow<List<DecisionEntity>>

    @Query("SELECT * FROM decisions ORDER BY updatedTimestamp DESC")
    fun getAllDecisions(): Flow<List<DecisionEntity>>

    @Query("SELECT * FROM decisions WHERE branchId = :branchId ORDER BY updatedTimestamp DESC")
    fun getDecisionsByBranch(branchId: String): Flow<List<DecisionEntity>>

    @Query("SELECT * FROM decisions WHERE id = :id")
    fun getDecisionById(id: String): Flow<DecisionEntity?>

    @Query("SELECT * FROM decisions WHERE id = :id")
    suspend fun getDecisionByIdSync(id: String): DecisionEntity?

    /**
     * Materialized current-state projection updated only in conjunction
     * with an append-only event in decision_events.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDecision(decision: DecisionEntity)

    // Append-Only immutable events with Branching
    @Insert
    suspend fun insertEvent(event: DecisionEventEntity)

    @Query("SELECT * FROM decision_events WHERE decisionId = :decisionId ORDER BY timestamp ASC")
    fun getEventsForDecision(decisionId: String): Flow<List<DecisionEventEntity>>

    @Query("SELECT * FROM decision_events WHERE decisionId = :decisionId AND branch_id = :branchId ORDER BY timestamp ASC")
    fun getEventsForDecisionAndBranch(decisionId: String, branchId: String): Flow<List<DecisionEventEntity>>

    @Query("SELECT * FROM decision_events WHERE branch_id = :branchId ORDER BY timestamp ASC")
    fun getEventsForBranch(branchId: String): Flow<List<DecisionEventEntity>>

    @Query("SELECT * FROM decision_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEvents(): Flow<List<DecisionEventEntity>>

    @Query("SELECT * FROM decision_events ORDER BY timestamp ASC")
    suspend fun getAllEventsSync(): List<DecisionEventEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEventSafe(event: DecisionEventEntity)

    // Demo Data Purge Queries
    @Query("DELETE FROM decisions WHERE id IN ('ADR-001', 'ADR-002', 'ADR-003', 'ADR-EXP-01')")
    suspend fun deleteDemoDecisions()

    @Query("DELETE FROM decision_edges WHERE fromDecisionId IN ('ADR-001', 'ADR-002', 'ADR-003', 'ADR-EXP-01') OR toDecisionId IN ('ADR-001', 'ADR-002', 'ADR-003', 'ADR-EXP-01')")
    suspend fun deleteDemoEdges()

    @Query("DELETE FROM decision_events WHERE decisionId IN ('ADR-001', 'ADR-002', 'ADR-003', 'ADR-EXP-01')")
    suspend fun deleteDemoEvents()

    @Query("DELETE FROM decision_branches WHERE branchId = 'exp-dynamo-paxos-eval'")
    suspend fun deleteDemoBranches()

    @Query("DELETE FROM resolution_tasks WHERE weakestNodeId IN ('ADR-001', 'ADR-002', 'ADR-003', 'ADR-EXP-01')")
    suspend fun deleteDemoTasks()

    @androidx.room.Transaction
    suspend fun purgeDemoData() {
        deleteDemoDecisions()
        deleteDemoEdges()
        deleteDemoEvents()
        deleteDemoBranches()
        deleteDemoTasks()
    }

    @Query("DELETE FROM decisions")
    suspend fun clearAllDecisions()

    @Query("DELETE FROM decision_edges")
    suspend fun clearAllEdges()

    @Query("DELETE FROM decision_events")
    suspend fun clearAllEvents()

    @Query("DELETE FROM resolution_tasks")
    suspend fun clearAllTasks()

    // Graph Edges
    @Query("SELECT * FROM decision_edges")
    fun getAllEdges(): Flow<List<DecisionEdgeEntity>>

    @Query("SELECT * FROM decision_edges")
    suspend fun getAllEdgesSync(): List<DecisionEdgeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEdge(edge: DecisionEdgeEntity)

    @Query("DELETE FROM decision_edges WHERE fromDecisionId = :fromId AND toDecisionId = :toId")
    suspend fun deleteEdge(fromId: String, toId: String)

    @Query("DELETE FROM decision_edges WHERE (fromDecisionId = :nodeA AND toDecisionId = :nodeB) OR (fromDecisionId = :nodeB AND toDecisionId = :nodeA)")
    suspend fun deleteBidirectionalEdge(nodeA: String, nodeB: String)

    // Git-Style Branches: Uniqueness enforced; never silently destroy existing branch
    @Query("SELECT * FROM decision_branches ORDER BY createdTimestamp ASC")
    fun getAllBranches(): Flow<List<DecisionBranchEntity>>

    @Query("SELECT * FROM decision_branches WHERE branchId = :branchId")
    suspend fun getBranchById(branchId: String): DecisionBranchEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBranch(branch: DecisionBranchEntity): Long

    // Resolution Tasks for DAG Cycles (Directive 8)
    @Query("SELECT * FROM resolution_tasks WHERE status = 'PENDING' ORDER BY createdTimestamp DESC")
    fun getPendingResolutionTasks(): Flow<List<ResolutionTaskEntity>>

    @Query("SELECT * FROM resolution_tasks ORDER BY createdTimestamp DESC")
    fun getAllResolutionTasks(): Flow<List<ResolutionTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResolutionTask(task: ResolutionTaskEntity)

    @Query("UPDATE resolution_tasks SET status = 'RESOLVED', resolutionNote = :note, resolvedTimestamp = :timestamp WHERE id = :taskId")
    suspend fun resolveTask(taskId: String, note: String, timestamp: Long = System.currentTimeMillis())
}
