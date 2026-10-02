package com.nirzor.kingmaker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.DecisionBranchEntity
import com.example.data.local.DecisionDao
import com.example.data.local.DecisionEdgeEntity
import com.example.data.local.DecisionEntity
import com.example.data.local.DecisionEventEntity
import com.example.data.local.DecisionStatus
import com.example.data.local.ResolutionTaskEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(
    entities = [
        DecisionEntity::class,
        DecisionEventEntity::class,
        DecisionEdgeEntity::class,
        DecisionBranchEntity::class,
        ResolutionTaskEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun decisionDao(): DecisionDao
    abstract fun decisionEventDao(): DecisionEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val seedMutex = Mutex()

        /**
         * Directive 10: Explicit Room Migration from version 1 to 2.
         * Destructive migration is strictly forbidden.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE decisions ADD COLUMN scopeConfirmed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE decisions ADD COLUMN focusAreaConfirmed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE decisions ADD COLUMN constraintsConfirmed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE decisions ADD COLUMN goalsConfirmed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE decisions ADD COLUMN reviewPacketJson TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN admissionCertificateJson TEXT")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS resolution_tasks (
                        id TEXT NOT NULL PRIMARY KEY,
                        cycleNodes TEXT NOT NULL,
                        weakestNodeId TEXT NOT NULL,
                        reason TEXT NOT NULL,
                        status TEXT NOT NULL,
                        resolutionNote TEXT,
                        createdTimestamp INTEGER NOT NULL,
                        resolvedTimestamp INTEGER
                    )
                """.trimIndent())
            }
        }

        /**
         * KingMaker v7.0: Migration from version 2 to 3.
         * Adds canonical revision hashing, 9-D quality vector, pre-mortem, provenance mode, and outcome tracking.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE decisions ADD COLUMN revisionNumber INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE decisions ADD COLUMN revisionHash TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN qualityVectorJson TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN preMortemRationale TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN approvalRationale TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN provenanceMode TEXT NOT NULL DEFAULT 'SIMULATED'")
                db.execSQL("ALTER TABLE decisions ADD COLUMN policyVersion TEXT NOT NULL DEFAULT 'v7.0-personal'")
                db.execSQL("ALTER TABLE decisions ADD COLUMN expectedOutcome TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN observedOutcome TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN outcomeDivergence TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN outcomeReviewDate INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_decisions_revisionHash ON decisions(revisionHash)")

                db.execSQL("ALTER TABLE decision_events ADD COLUMN revisionHash TEXT")
                db.execSQL("ALTER TABLE decision_events ADD COLUMN policyVersion TEXT NOT NULL DEFAULT 'v7.0-personal'")

                db.execSQL("ALTER TABLE resolution_tasks ADD COLUMN proposedAction TEXT")
                db.execSQL("ALTER TABLE resolution_tasks ADD COLUMN cycleHash TEXT")
            }
        }

        /**
         * KingMaker v7.0 Consolidation: Migration from version 3 to 4.
         * Adds raw user statement, provisional interpretation, context questions, framing, and diagram URI.
         * Explicit non-destructive schema evolution.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE decisions ADD COLUMN rawUserStatement TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN provisionalInterpretation TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN contextQuestionsJson TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN framingJson TEXT")
                db.execSQL("ALTER TABLE decisions ADD COLUMN architecturalDiagramUri TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kingmaker.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
                INSTANCE = dbInstance
                dbInstance
            }
        }
    }

    /**
     * Clean Workspace Initialization:
     * - Purges any legacy demo data (ADR-001, ADR-002, ADR-003, ADR-EXP-01, etc.)
     * - Ensures only a clean baseline 'main' branch exists if the branch table is empty.
     * - No synthetic or dummy decisions/events are seeded into user's database.
     */
    suspend fun ensureInitialDataSeeded() {
        seedMutex.withLock {
            val dao = decisionDao()
            // Purge any legacy demo data to ensure a completely clean personal workspace
            dao.purgeDemoData()

            // Ensure baseline 'main' branch exists if none present
            val existingBranches = dao.getAllBranches().firstOrNull()
            if (existingBranches.isNullOrEmpty()) {
                val mainBranch = DecisionBranchEntity(
                    branchId = "main",
                    name = "main",
                    parentBranchId = null,
                    forkedAtDecisionId = null,
                    forkedAtEventId = null,
                    dqsScore = 0.0,
                    status = "MAIN",
                    createdTimestamp = System.currentTimeMillis()
                )
                dao.insertBranch(mainBranch)
            }
        }
    }
}
