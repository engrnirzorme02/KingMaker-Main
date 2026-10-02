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
    version = 2,
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kingmaker.db"
                )
                    .addMigrations(MIGRATION_1_2)
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
