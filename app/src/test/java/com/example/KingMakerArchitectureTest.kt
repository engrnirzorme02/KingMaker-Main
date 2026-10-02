package com.example

import com.example.ai.WarRoomEngine
import com.example.core.graph.CycleDetectionResult
import com.example.core.graph.DependencyGraphEngine
import com.example.core.graph.GraphEdge
import com.example.core.math.AdmissionTestResult
import com.example.core.math.ComplexityTier
import com.example.core.math.DQSInput
import com.example.core.math.DecisionMathEngine
import com.example.data.local.DecisionBranchEntity
import com.example.data.local.DecisionDao
import com.example.data.local.DecisionEdgeEntity
import com.example.data.local.DecisionEntity
import com.example.data.local.DecisionEventEntity
import com.example.data.local.DecisionStatus
import com.example.data.local.ResolutionTaskEntity
import com.example.data.repository.DecisionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KingMakerArchitectureTest {

    // 1. Complexity Calculation Test (Directive 5)
    @Test
    fun testComplexityCalculation() {
        // Score = (Risk × 0.35) + (Impact × 0.30) + ((1 - Changeability) × 0.20) + (Budget × 0.15)
        // ADR-001 values: risk=0.80, impact=0.85, changeability=0.30, budget=0.60
        // (0.80 * 0.35) + (0.85 * 0.30) + ((1 - 0.30) * 0.20) + (0.60 * 0.15)
        // = 0.28 + 0.255 + 0.14 + 0.09 = 0.765 -> rounded to 0.77 (or 0.78) -> RIGOROUS tier
        val res = DecisionMathEngine.calculateComplexity(0.80, 0.85, 0.30, 0.60)
        assertEquals(0.77, res.score, 0.02)
        assertEquals(ComplexityTier.RIGOROUS, res.tier)

        // Light tier test
        val light = DecisionMathEngine.calculateComplexity(0.1, 0.1, 0.9, 0.1)
        assertTrue(light.score < 0.30)
        assertEquals(ComplexityTier.LIGHT, light.tier)

        // Maximum tier test
        val max = DecisionMathEngine.calculateComplexity(0.9, 0.9, 0.1, 0.9)
        assertTrue(max.score >= 0.80)
        assertEquals(ComplexityTier.MAXIMUM, max.tier)
    }

    // 2. DQS Calculation Test (Directive 4: 7-parameter formula, no artificial boosts)
    @Test
    fun testDQSCalculation() {
        // Formula: (0.20E + 0.20T + 0.18R + 0.17U + 0.12A + 0.08C + 0.05V) / 0.85
        val input = DQSInput(
            evidence = 0.85,
            trust = 0.80,
            riskMitigation = 0.75,
            uncertainty = 0.70,
            agreement = 0.80,
            critiqueResolved = 0.80,
            valueAlignment = 0.85
        )
        val dqs = DecisionMathEngine.calculateNormalizedDQS(input)
        assertTrue(dqs in 0.0..1.0)
        // Verify calculation precision
        val raw = (0.20 * 0.85) + (0.20 * 0.80) + (0.18 * 0.75) + (0.17 * 0.70) + (0.12 * 0.80) + (0.08 * 0.80) + (0.05 * 0.85)
        val expected = (raw / 0.85)
        assertEquals(expected, dqs, 0.02)

        // Missing evidence / Assumption must NOT artificially inflate
        val assumptionInput = input.copy(evidence = 0.30, trust = 0.50)
        val lowDqs = DecisionMathEngine.calculateNormalizedDQS(assumptionInput)
        assertTrue(lowDqs < dqs)
    }

    // 3. Stop Rule Test (Directive 6)
    @Test
    fun testStopRule() {
        // If improvement between rounds < 0.05, stop refinement
        assertTrue(DecisionMathEngine.shouldStopRefinement(0.82, 0.84)) // delta = 0.02 < 0.05 -> stop
        assertTrue(DecisionMathEngine.shouldStopRefinement(0.80, 0.81)) // delta = 0.01 < 0.05 -> stop
        assertFalse(DecisionMathEngine.shouldStopRefinement(0.70, 0.78)) // delta = 0.08 >= 0.05 -> continue
    }

    // 4. Admission Test & Finalization Certificate Guard (Directive 3)
    @Test
    fun testAdmissionTestAndCertificate() {
        val passedChecks = DecisionMathEngine.evaluateAdmissionTest(
            title = "Distributed Raft Consensus Quorum",
            problemStatement = "Mitigate P99 latency SLA violations during cross-datacenter partition recovery.",
            selectedOption = "3-Node Multi-AZ Quorum with adaptive heartbeats",
            risk = 0.70,
            impact = 0.80,
            dqsScore = 0.85,
            evidenceType = "EMPIRICAL"
        )
        assertTrue(passedChecks.allPassed)

        val cert = DecisionMathEngine.generateFinalizationCertificate(
            decisionId = "DEC-001",
            admissionChecks = passedChecks,
            finalDqs = 0.85
        )
        assertNotNull(cert)
        assertTrue(cert!!.certificateHash.isNotBlank())
        assertEquals("DEC-001", cert.decisionId)

        // Failure case: vague title and problem statement
        val failedChecks = DecisionMathEngine.evaluateAdmissionTest(
            title = "Short",
            problemStatement = "Too short",
            selectedOption = null,
            risk = 0.05,
            impact = 0.05,
            dqsScore = 0.40,
            evidenceType = "UNVERIFIED"
        )
        assertFalse(failedChecks.allPassed)
        val nullCert = DecisionMathEngine.generateFinalizationCertificate(
            decisionId = "DEC-002",
            admissionChecks = failedChecks,
            finalDqs = 0.40
        )
        assertNull(nullCert)
    }

    // 5. Tarjan SCC Cycle Detection Test (Directive 8)
    @Test
    fun testCycleDetection() {
        // Acyclic graph: 1 -> 2 -> 3
        val acyclicEdges = listOf(
            GraphEdge("1", "2"),
            GraphEdge("2", "3")
        )
        val acyclicResult = DependencyGraphEngine.detectCycles(setOf("1", "2", "3"), acyclicEdges)
        assertFalse(acyclicResult.hasCycle)
        assertTrue(acyclicResult.cyclicNodeIds.isEmpty())

        // Cyclic graph: 1 -> 2 -> 3 -> 1
        val cyclicEdges = listOf(
            GraphEdge("1", "2"),
            GraphEdge("2", "3"),
            GraphEdge("3", "1")
        )
        val cyclicResult = DependencyGraphEngine.detectCycles(setOf("1", "2", "3"), cyclicEdges)
        assertTrue(cyclicResult.hasCycle)
        assertEquals(3, cyclicResult.cyclicNodeIds.size)
    }

    // 6. Weakest Node Identification in Cycle (Directive 8)
    @Test
    fun testWeakestNodeIdentification() {
        val cyclicNodes = setOf("ADR-001", "ADR-002", "ADR-003")
        val evidenceMap = mapOf(
            "ADR-001" to Pair("AXIOMATIC", 0.94),
            "ADR-002" to Pair("EMPIRICAL", 0.89),
            "ADR-003" to Pair("ASSUMPTION", 0.65) // Weakest
        )
        val weakest = DependencyGraphEngine.identifyWeakestEvidenceNode(cyclicNodes, evidenceMap)
        assertEquals("ADR-003", weakest)
    }

    // 7. Ripple Effect Dependency Semantics Test (Directive 9)
    @Test
    fun testRippleDependencySemantics() {
        // Dependency semantics:
        // ADR-002 DEPENDS_ON ADR-001 (ADR-001 is prerequisite for ADR-002)
        // ADR-003 DEPENDS_ON ADR-002 (ADR-002 is prerequisite for ADR-003)
        val edges = listOf(
            GraphEdge(from = "ADR-002", to = "ADR-001", relationship = "DEPENDS_ON"),
            GraphEdge(from = "ADR-003", to = "ADR-002", relationship = "DEPENDS_ON")
        )

        // When prerequisite ADR-001 changes: both dependents ADR-002 and ADR-003 must be affected!
        val affectedByAdr001 = DependencyGraphEngine.calculateRippleEffect("ADR-001", edges)
        assertTrue(affectedByAdr001.contains("ADR-002"))
        assertTrue(affectedByAdr001.contains("ADR-003"))

        // When ADR-002 changes: ADR-003 is affected, but prerequisite ADR-001 is NOT affected
        val affectedByAdr002 = DependencyGraphEngine.calculateRippleEffect("ADR-002", edges)
        assertTrue(affectedByAdr002.contains("ADR-003"))
        assertFalse(affectedByAdr002.contains("ADR-001"))

        // When leaf ADR-003 changes: nobody depends on it, affected list is empty
        val affectedByAdr003 = DependencyGraphEngine.calculateRippleEffect("ADR-003", edges)
        assertTrue(affectedByAdr003.isEmpty())
    }

    // 8. Repository State Machine & Invariant Tests (Directives 1, 2, 3, 10, 11, 12, 13)
    @Test
    fun testRepositoryStateMachineAndInvariants() = runBlocking {
        val fakeDao = FakeDecisionDao()
        val repo = DecisionRepository(fakeDao)

        // Step 1: Create Decision (D1_INTAKE)
        val decId = repo.createDecisionStream(
            title = "Event Sourcing Pipeline",
            problemStatement = "Architectural decoupling between billing and ingestion service.",
            risk = 0.6,
            impact = 0.7,
            changeability = 0.4,
            budget = 0.5
        )
        // Directive 11: Collision-safe UUID
        assertTrue(decId.startsWith("DEC-"))
        val dec1 = repo.getDecisionByIdSync(decId)
        assertNotNull(dec1)
        assertEquals(DecisionStatus.D1_INTAKE, dec1!!.status)

        // Directive 10: Immutable event created
        assertEquals(1, fakeDao.events.size)
        assertEquals("INTAKE_INITIALIZED", fakeDao.events.first().eventType)

        // Step 2: Mandatory D2 Framing Guard (CRITICAL 3)
        // Jumping directly from D1 to D3 without D2 Framing MUST be rejected!
        try {
            repo.confirmD3Gate(
                id = decId,
                scopeConfirmed = true,
                focusAreaConfirmed = true,
                constraintsConfirmed = true,
                goalsConfirmed = true
            )
            assertFalse("Expected IllegalStateException due to jumping D2 Framing", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("D2 Framing must be completed before D3 Confirmation"))
        }

        // Complete D2 Framing
        repo.updateFramingAndTaxonomy(
            id = decId,
            tagsJson = "{\"tags\":[\"P99 Latency < 20ms\"]}",
            risk = 0.6,
            impact = 0.7,
            changeability = 0.4,
            budget = 0.5
        )
        val decFramed = repo.getDecisionByIdSync(decId)
        assertEquals(DecisionStatus.D2_FRAMING, decFramed!!.status)

        // Advancing to War Room without D3 confirmation MUST still be rejected!
        try {
            repo.advanceWarRoomRound(decId, 1, 0.70, "{}")
            assertFalse("Expected IllegalStateException due to missing D3 confirmation", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("D3 Human Confirmation"))
        }

        // Complete D3 confirmation
        val d3Result = repo.confirmD3Gate(
            id = decId,
            scopeConfirmed = true,
            focusAreaConfirmed = true,
            constraintsConfirmed = true,
            goalsConfirmed = true
        )
        assertTrue(d3Result)
        val decConfirmed = repo.getDecisionByIdSync(decId)
        assertEquals(DecisionStatus.D3_CONFIRMATION, decConfirmed!!.status)

        // Step 3: Advance War Room (D4_DEBATE -> D5_CRITIQUE)
        repo.advanceWarRoomRound(decId, 1, 0.75, "{\"round\": 1}")
        assertEquals(DecisionStatus.D4_DEBATE, repo.getDecisionByIdSync(decId)!!.status)

        repo.advanceWarRoomRound(decId, 2, 0.82, "{\"round\": 2}")
        assertEquals(DecisionStatus.D5_CRITIQUE, repo.getDecisionByIdSync(decId)!!.status)

        // Test Stop Rule Triggered (CRITICAL 8)
        repo.recordStopRuleTriggered(decId, 0.80, 0.82) // delta = 0.02 < 0.05
        assertTrue(fakeDao.events.any { it.eventType == "STOP_RULE_TRIGGERED" })

        // Step 4: D6 Synthesis & Review Packet (CRITICAL 4 structured JSON)
        val reviewPacket = WarRoomEngine.generateReviewPacket(
            decisionId = decId,
            title = "Event Sourcing Pipeline",
            problemStatement = "Architectural decoupling between billing and ingestion service.",
            selectedOption = "Append-only SQLite WAL",
            evidenceType = "HEURISTIC",
            dqsScore = 0.85,
            constraints = listOf("P99 Latency < 20ms")
        )
        repo.synthesizeReviewPacket(decId, reviewPacket.toJson(), 0.85)
        val decInReview = repo.getDecisionByIdSync(decId)
        assertEquals(DecisionStatus.D7_REVIEW, decInReview!!.status)

        // Step 5: CEO Approval Guard (Directive 13)
        // CEO approval without Finalization Certificate MUST be rejected!
        try {
            repo.executeCeoAttestationSignature(decId, "CEO Test")
            assertFalse("Expected IllegalStateException due to missing certificate", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Finalization Certificate is missing"))
        }

        // Issue Finalization Certificate
        val cert = repo.evaluateAdmissionAndIssueCertificate(decId)
        assertNotNull(cert)

        // Execute CEO Approval from D7_REVIEW with Certificate
        val signatureHash = repo.executeCeoAttestationSignature(decId, "Authorized CEO")
        assertTrue(signatureHash.isNotBlank())

        val approvedDec = repo.getDecisionByIdSync(decId)
        assertEquals(DecisionStatus.APPROVED, approvedDec!!.status)
        // CEO approval must NOT artificially inflate DQS to 0.90!
        assertEquals(0.85, approvedDec.dqsScore, 0.001)

        // Step 6: Branch Forking Test (Directive 12)
        val forkResult = repo.forkDecisionTimeline(decId, "exp/dynamo-eval", "Dynamo Multi-Master")
        assertTrue(forkResult.branchId.startsWith("exp-dynamo-eval"))
        assertTrue(forkResult.forkedDecisionId.startsWith("$decId-FORK-"))

        val forkedDec = repo.getDecisionByIdSync(forkResult.forkedDecisionId)
        assertNotNull(forkedDec)
        assertEquals(forkResult.branchId, forkedDec!!.branchId)
        assertEquals(DecisionStatus.D4_DEBATE, forkedDec.status)
    }
}

// In-Memory Test Fake for DecisionDao to execute JVM unit tests without native SQLite bindings
class FakeDecisionDao : DecisionDao {
    val decisions = mutableMapOf<String, DecisionEntity>()
    val events = mutableListOf<DecisionEventEntity>()
    val edges = mutableListOf<DecisionEdgeEntity>()
    val branches = mutableMapOf<String, DecisionBranchEntity>()
    val tasks = mutableMapOf<String, ResolutionTaskEntity>()

    override fun getDecisionsByProject(projectId: String): Flow<List<DecisionEntity>> =
        flowOf(decisions.values.filter { it.projectId == projectId })

    override fun getDecisionsByProjectAndBranch(projectId: String, branchId: String): Flow<List<DecisionEntity>> =
        flowOf(decisions.values.filter { it.projectId == projectId && it.branchId == branchId })

    override fun getAllDecisions(): Flow<List<DecisionEntity>> =
        flowOf(decisions.values.toList())

    override fun getDecisionsByBranch(branchId: String): Flow<List<DecisionEntity>> =
        flowOf(decisions.values.filter { it.branchId == branchId })

    override fun getDecisionById(id: String): Flow<DecisionEntity?> =
        flowOf(decisions[id])

    override suspend fun getDecisionByIdSync(id: String): DecisionEntity? =
        decisions[id]

    override suspend fun upsertDecision(decision: DecisionEntity) {
        decisions[decision.id] = decision
    }

    override suspend fun insertEvent(event: DecisionEventEntity) {
        events.add(event)
    }

    override fun getEventsForDecision(decisionId: String): Flow<List<DecisionEventEntity>> =
        flowOf(events.filter { it.decisionId == decisionId })

    override fun getEventsForDecisionAndBranch(decisionId: String, branchId: String): Flow<List<DecisionEventEntity>> =
        flowOf(events.filter { it.decisionId == decisionId && it.branch_id == branchId })

    override fun getEventsForBranch(branchId: String): Flow<List<DecisionEventEntity>> =
        flowOf(events.filter { it.branch_id == branchId })

    override fun getRecentEvents(): Flow<List<DecisionEventEntity>> =
        flowOf(events.takeLast(50))

    override suspend fun getAllEventsSync(): List<DecisionEventEntity> =
        events.toList()

    override suspend fun insertEventSafe(event: DecisionEventEntity) {
        events.add(event)
    }

    override suspend fun deleteDemoDecisions() {
        decisions.keys.removeAll(setOf("ADR-001", "ADR-002", "ADR-003", "ADR-EXP-01"))
    }

    override suspend fun deleteDemoEdges() {
        edges.removeAll { it.fromDecisionId in setOf("ADR-001", "ADR-002", "ADR-003", "ADR-EXP-01") || it.toDecisionId in setOf("ADR-001", "ADR-002", "ADR-003", "ADR-EXP-01") }
    }

    override suspend fun deleteDemoEvents() {
        events.removeAll { it.decisionId in setOf("ADR-001", "ADR-002", "ADR-003", "ADR-EXP-01") }
    }

    override suspend fun deleteDemoBranches() {
        branches.remove("exp-dynamo-paxos-eval")
    }

    override suspend fun deleteDemoTasks() {
        tasks.values.removeAll { it.weakestNodeId in setOf("ADR-001", "ADR-002", "ADR-003", "ADR-EXP-01") }
    }

    override suspend fun purgeDemoData() {
        deleteDemoDecisions()
        deleteDemoEdges()
        deleteDemoEvents()
        deleteDemoBranches()
        deleteDemoTasks()
    }

    override suspend fun clearAllDecisions() {
        decisions.clear()
    }

    override suspend fun clearAllEdges() {
        edges.clear()
    }

    override suspend fun clearAllEvents() {
        events.clear()
    }

    override suspend fun clearAllTasks() {
        tasks.clear()
    }

    override fun getAllEdges(): Flow<List<DecisionEdgeEntity>> =
        flowOf(edges.toList())

    override suspend fun getAllEdgesSync(): List<DecisionEdgeEntity> =
        edges.toList()

    override suspend fun insertEdge(edge: DecisionEdgeEntity) {
        edges.add(edge)
    }

    override suspend fun deleteEdge(fromId: String, toId: String) {
        edges.removeAll { it.fromDecisionId == fromId && it.toDecisionId == toId }
    }

    override suspend fun deleteBidirectionalEdge(nodeA: String, nodeB: String) {
        edges.removeAll { (it.fromDecisionId == nodeA && it.toDecisionId == nodeB) || (it.fromDecisionId == nodeB && it.toDecisionId == nodeA) }
    }

    override fun getAllBranches(): Flow<List<DecisionBranchEntity>> =
        flowOf(branches.values.toList())

    override suspend fun getBranchById(branchId: String): DecisionBranchEntity? =
        branches[branchId]

    override suspend fun insertBranch(branch: DecisionBranchEntity): Long {
        branches[branch.branchId] = branch
        return 1L
    }

    override fun getPendingResolutionTasks(): Flow<List<ResolutionTaskEntity>> =
        flowOf(tasks.values.filter { it.status == "PENDING" })

    override fun getAllResolutionTasks(): Flow<List<ResolutionTaskEntity>> =
        flowOf(tasks.values.toList())

    override suspend fun insertResolutionTask(task: ResolutionTaskEntity) {
        tasks[task.id] = task
    }

    override suspend fun resolveTask(taskId: String, note: String, timestamp: Long) {
        tasks[taskId]?.let {
            tasks[taskId] = it.copy(status = "RESOLVED", resolutionNote = note, resolvedTimestamp = timestamp)
        }
    }
}
