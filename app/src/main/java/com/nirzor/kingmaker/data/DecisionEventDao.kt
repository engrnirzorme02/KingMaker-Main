package com.nirzor.kingmaker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.local.DecisionEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * HARD INVARIANT: DecisionEventDao must ONLY have @Insert queries.
 * Do NOT provide @Update or @Delete methods (Append-only Event Sourcing
 * to prevent resulting fallacy and preserve immutable audit trails).
 */
@Dao
interface DecisionEventDao {

    @Insert
    suspend fun insertEvent(event: DecisionEventEntity)

    @Insert
    suspend fun insertEvents(events: List<DecisionEventEntity>)

    @Query("SELECT * FROM decision_events WHERE decisionId = :decisionId ORDER BY timestamp ASC")
    fun getEventsForDecision(decisionId: String): Flow<List<DecisionEventEntity>>

    @Query("SELECT * FROM decision_events WHERE decisionId = :decisionId AND branch_id = :branchId ORDER BY timestamp ASC")
    fun getEventsForDecisionAndBranch(decisionId: String, branchId: String): Flow<List<DecisionEventEntity>>

    @Query("SELECT * FROM decision_events WHERE branch_id = :branchId ORDER BY timestamp ASC")
    fun getEventsForBranch(branchId: String): Flow<List<DecisionEventEntity>>

    @Query("SELECT * FROM decision_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<DecisionEventEntity>>

    @Query("SELECT * FROM decision_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEvents(): Flow<List<DecisionEventEntity>>
}
