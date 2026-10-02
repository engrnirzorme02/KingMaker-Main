package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ReviewPacket
import com.example.ai.WarRoomEngine
import com.example.ai.WarRoomRoundResult
import com.example.core.graph.CycleDetectionResult
import com.example.core.math.AdmissionTestResult
import com.example.core.math.ComplexityResult
import com.example.core.math.ComplexityTier
import com.example.core.math.DecisionMathEngine
import com.example.core.math.FinalizationCertificate
import com.example.data.local.DecisionBranchEntity
import com.example.data.local.DecisionDao
import com.example.data.local.DecisionEdgeEntity
import com.example.data.local.DecisionEntity
import com.example.data.local.DecisionEventEntity
import com.example.data.local.DecisionStatus
import com.example.data.local.ResolutionTaskEntity
import com.example.data.repository.DecisionRepository
import com.example.data.repository.ForkResult
import com.example.services.ApiConfigManager
import com.example.services.ApiSettings
import com.example.services.FirebaseSyncService
import com.example.services.SyncResult
import com.nirzor.kingmaker.data.AppDatabase
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalizationManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CockpitScreen {
    VAULT_DAG,             // Screen 1: Project Vault & Living Dependency DAG
    INTAKE_FRAMING,        // Screen 2: D1-D3 Intake, Framing & Confirmation Studio
    WAR_ROOM,              // Screen 3: D4-D5 Multi-Agent War Room & Critique
    SMART_QUERY,           // Screen 4: Smart Query Auxiliary Recovery Modal
    BLUEPRINT_5_LENSES     // Screen 5: D6 Synthesis, D7 Review & CEO Gate Attestation
}

enum class BlueprintLens {
    EXECUTIVE,
    ARCHITECTURE,
    UX,
    DEVELOPER,
    GOVERNANCE
}

data class TaxonomyTag(
    val id: String,
    val text: String,
    val type: TagType
)

enum class TagType {
    FACT,
    BUSINESS_CONSTRAINT,
    TECHNICAL_CONSTRAINT,
    WISH
}

@OptIn(ExperimentalCoroutinesApi::class)
class KingMakerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = DecisionRepository(db.decisionDao())
    private val firebaseService = FirebaseSyncService(application, db.decisionDao())
    private val apiConfigManager = ApiConfigManager(application)
    private val localizationManager = LocalizationManager.getInstance(application)

    // Language State & Persistence (Defaults to BN / Bengali)
    val currentLanguage: StateFlow<AppLanguage> = localizationManager.currentLanguage

    fun setLanguage(language: AppLanguage) {
        localizationManager.setLanguage(language)
    }

    // Section 51.3: What-if Simulation State
    private val _whatIfSimulation = MutableStateFlow<com.example.core.graph.WhatIfSimulationResult?>(null)
    val whatIfSimulation: StateFlow<com.example.core.graph.WhatIfSimulationResult?> = _whatIfSimulation.asStateFlow()

    // Section 52: Reconciliation Diff State
    private val _reconciliationDiff = MutableStateFlow<com.example.core.graph.SemanticGraphDiff?>(null)
    val reconciliationDiff: StateFlow<com.example.core.graph.SemanticGraphDiff?> = _reconciliationDiff.asStateFlow()

    // Active Git Branch state (defaults to 'main' or saved session)
    private val _activeBranchId = MutableStateFlow(firebaseService.getSavedBranchId())
    val activeBranchId: StateFlow<String> = _activeBranchId.asStateFlow()

    // Dynamic AI Engine & External API Settings State
    private val _apiSettings = MutableStateFlow(apiConfigManager.getSettings())
    val apiSettings: StateFlow<ApiSettings> = _apiSettings.asStateFlow()

    // Firebase Cloud Sync & User Account State
    private val _userEmail = MutableStateFlow(firebaseService.getSavedUserEmail())
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _cloudSyncStatus = MutableStateFlow("FIREBASE READY")
    val cloudSyncStatus: StateFlow<String> = _cloudSyncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Branches stream
    val allBranches: StateFlow<List<DecisionBranchEntity>> = repository.getAllBranches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Decisions filtered reactively by active branch
    val branchDecisions: StateFlow<List<DecisionEntity>> = _activeBranchId
        .flatMapLatest { branchId ->
            repository.getDecisionsByBranch(branchId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDecisions: StateFlow<List<DecisionEntity>> = repository.getAllDecisions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEdges: StateFlow<List<DecisionEdgeEntity>> = repository.getAllEdges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingResolutionTasks: StateFlow<List<ResolutionTaskEntity>> = repository.getPendingResolutionTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(CockpitScreen.VAULT_DAG)
    val currentScreen: StateFlow<CockpitScreen> = _currentScreen.asStateFlow()

    private val _screenBackStack = mutableListOf(CockpitScreen.VAULT_DAG)

    private val _selectedDecision = MutableStateFlow<DecisionEntity?>(null)
    val selectedDecision: StateFlow<DecisionEntity?> = _selectedDecision.asStateFlow()

    private val _decisionEvents = MutableStateFlow<List<DecisionEventEntity>>(emptyList())
    val decisionEvents: StateFlow<List<DecisionEventEntity>> = _decisionEvents.asStateFlow()

    private val _cycleDetection = MutableStateFlow<CycleDetectionResult?>(null)
    val cycleDetection: StateFlow<CycleDetectionResult?> = _cycleDetection.asStateFlow()

    private val _rippleHighlightedNodes = MutableStateFlow<Set<String>>(emptySet())
    val rippleHighlightedNodes: StateFlow<Set<String>> = _rippleHighlightedNodes.asStateFlow()

    // Screen 2: Intake & Framing State
    val intakeTitle = MutableStateFlow("")
    val intakeProblemStatement = MutableStateFlow("")
    val intakeRisk = MutableStateFlow(0.65)
    val intakeImpact = MutableStateFlow(0.70)
    val intakeChangeability = MutableStateFlow(0.35)
    val intakeBudget = MutableStateFlow(0.50)
    val intakeTags = MutableStateFlow<List<TaxonomyTag>>(emptyList())

    // D3 Confirmation State Guards
    val scopeConfirmed = MutableStateFlow(false)
    val focusAreaConfirmed = MutableStateFlow(false)
    val constraintsConfirmed = MutableStateFlow(false)
    val goalsConfirmed = MutableStateFlow(false)
    val d2FramingCompleted = MutableStateFlow(false)

    // Dynamic Complexity calculation (0.0 to 1.0)
    private val _realtimeComplexity = MutableStateFlow(
        DecisionMathEngine.calculateComplexity(0.65, 0.70, 0.35, 0.50)
    )
    val realtimeComplexity: StateFlow<ComplexityResult> = _realtimeComplexity.asStateFlow()

    // Screen 3: War Room State
    private val _warRoomRound = MutableStateFlow(1)
    val warRoomRound: StateFlow<Int> = _warRoomRound.asStateFlow()

    private val _warRoomResult = MutableStateFlow<WarRoomRoundResult?>(null)
    val warRoomResult: StateFlow<WarRoomRoundResult?> = _warRoomResult.asStateFlow()

    private val _isDebateRunning = MutableStateFlow(false)
    val isDebateRunning: StateFlow<Boolean> = _isDebateRunning.asStateFlow()

    private val _stopRuleTriggered = MutableStateFlow(false)
    val stopRuleTriggered: StateFlow<Boolean> = _stopRuleTriggered.asStateFlow()

    // D6 Review Packet
    private val _reviewPacket = MutableStateFlow<ReviewPacket?>(null)
    val reviewPacket: StateFlow<ReviewPacket?> = _reviewPacket.asStateFlow()

    // D7 Admission Test & Finalization Certificate
    private val _admissionTestResult = MutableStateFlow<AdmissionTestResult?>(null)
    val admissionTestResult: StateFlow<AdmissionTestResult?> = _admissionTestResult.asStateFlow()

    private val _finalizationCertificate = MutableStateFlow<FinalizationCertificate?>(null)
    val finalizationCertificate: StateFlow<FinalizationCertificate?> = _finalizationCertificate.asStateFlow()

    private val _gateErrorMessage = MutableStateFlow<String?>(null)
    val gateErrorMessage: StateFlow<String?> = _gateErrorMessage.asStateFlow()

    // Screen 5: Blueprint Lens
    private val _activeLens = MutableStateFlow(BlueprintLens.EXECUTIVE)
    val activeLens: StateFlow<BlueprintLens> = _activeLens.asStateFlow()

    private val _signatureHash = MutableStateFlow<String?>(null)
    val signatureHash: StateFlow<String?> = _signatureHash.asStateFlow()

    init {
        viewModelScope.launch {
            db.ensureInitialDataSeeded()
            refreshCycles()
        }
        initializeDefaultTags()
    }

    private fun initializeDefaultTags() {
        intakeTags.value = listOf(
            TaxonomyTag("t1", "Latency < 20ms P99", TagType.TECHNICAL_CONSTRAINT),
            TaxonomyTag("t2", "Audit Trail must be Append-Only", TagType.FACT),
            TaxonomyTag("t3", "Cloud Budget cap $2000/mo", TagType.BUSINESS_CONSTRAINT),
            TaxonomyTag("t4", "Zero Downtime Deployments", TagType.FACT),
            TaxonomyTag("t5", "100% automated AI decisions", TagType.WISH) // Strikethrough (Invariant 2 prohibits)
        )
    }

    fun setActiveBranch(branchId: String) {
        _activeBranchId.value = branchId
        viewModelScope.launch {
            val decisions = db.decisionDao().getDecisionsByBranch(branchId)
            val current = _selectedDecision.value
            if (current?.branchId != branchId) {
                val branchDecisionsList = db.decisionDao().getDecisionsByBranch(branchId)
            }
        }
    }

    fun navigateTo(screen: CockpitScreen) {
        if (_currentScreen.value != screen) {
            _screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun handleBack(): Boolean {
        if (_screenBackStack.isNotEmpty()) {
            val previous = _screenBackStack.removeLast()
            _currentScreen.value = previous
            return true
        }
        return false
    }

    fun selectDecision(decision: DecisionEntity) {
        _selectedDecision.value = decision
        _signatureHash.value = decision.digitalSignatureHash
        _admissionTestResult.value = null
        _finalizationCertificate.value = null
        _gateErrorMessage.value = null

        // Sync D3 confirmation states
        scopeConfirmed.value = decision.scopeConfirmed
        focusAreaConfirmed.value = decision.focusAreaConfirmed
        constraintsConfirmed.value = decision.constraintsConfirmed
        goalsConfirmed.value = decision.goalsConfirmed
        d2FramingCompleted.value = decision.status != DecisionStatus.D1_INTAKE

        // Check if admission certificate exists in JSON
        if (!decision.admissionCertificateJson.isNullOrBlank()) {
            val isD6Done = decision.status == DecisionStatus.D6_SYNTHESIS || decision.status == DecisionStatus.D7_REVIEW || decision.status == DecisionStatus.APPROVED
            val hasPacket = !decision.reviewPacketJson.isNullOrBlank()
            val checks = DecisionMathEngine.evaluateAdmissionTest(
                title = decision.title,
                problemStatement = decision.problemStatement,
                selectedOption = decision.selectedOption,
                risk = decision.risk,
                impact = decision.impact,
                dqsScore = decision.dqsScore,
                evidenceType = decision.evidenceType,
                isD6SynthesisCompleted = isD6Done,
                hasReviewPacket = hasPacket
            )
            _admissionTestResult.value = checks
            _finalizationCertificate.value = DecisionMathEngine.generateFinalizationCertificate(
                decisionId = decision.id,
                admissionChecks = checks,
                finalDqs = decision.dqsScore,
                isD6SynthesisCompleted = isD6Done,
                hasReviewPacket = hasPacket
            )
        }

        viewModelScope.launch {
            repository.getEventsForDecision(decision.id).collect {
                _decisionEvents.value = it
            }
        }
    }

    fun openNewDecisionStream() {
        intakeTitle.value = "Autonomous Event Sourcing Gateway"
        intakeProblemStatement.value = "Evaluate Raft-based WAL commit vs partitioned message broker for sub-second ordering guarantees."
        intakeRisk.value = 0.70
        intakeImpact.value = 0.80
        intakeChangeability.value = 0.30
        intakeBudget.value = 0.60
        scopeConfirmed.value = false
        focusAreaConfirmed.value = false
        constraintsConfirmed.value = false
        goalsConfirmed.value = false
        d2FramingCompleted.value = false
        _selectedDecision.value = null
        recalculateRealtimeComplexity()
        navigateTo(CockpitScreen.INTAKE_FRAMING)
    }

    fun updateIntakeSliders(risk: Double, impact: Double, changeability: Double, budget: Double) {
        intakeRisk.value = risk
        intakeImpact.value = impact
        intakeChangeability.value = changeability
        intakeBudget.value = budget
        recalculateRealtimeComplexity()
    }

    private fun recalculateRealtimeComplexity() {
        _realtimeComplexity.value = DecisionMathEngine.calculateComplexity(
            intakeRisk.value,
            intakeImpact.value,
            intakeChangeability.value,
            intakeBudget.value
        )
    }

    fun addTag(text: String, type: TagType) {
        val newTag = TaxonomyTag(System.currentTimeMillis().toString(), text, type)
        intakeTags.value = intakeTags.value + newTag
    }

    fun removeTag(id: String) {
        intakeTags.value = intakeTags.value.filter { it.id != id }
    }

    /**
     * D2 Framing & Taxonomy:
     * Persists D2 Framing as an explicit state transition and event before D3 can be unlocked.
     */
    fun completeD2Framing() {
        viewModelScope.launch {
            val titleText = intakeTitle.value.ifBlank { "High-Concurrency Storage Layer" }
            val problemText = intakeProblemStatement.value.ifBlank { "Architectural trade-off evaluation" }
            val tagsJson = "{\"tags\": [${intakeTags.value.joinToString(",") { "\"${it.text}\"" }}]}"

            var current = _selectedDecision.value
            val decId = if (current == null) {
                repository.createDecisionStream(
                    title = titleText,
                    problemStatement = problemText,
                    risk = intakeRisk.value,
                    impact = intakeImpact.value,
                    changeability = intakeChangeability.value,
                    budget = intakeBudget.value,
                    branchId = _activeBranchId.value
                )
            } else {
                current.id
            }

            repository.updateFramingAndTaxonomy(
                id = decId,
                tagsJson = tagsJson,
                risk = intakeRisk.value,
                impact = intakeImpact.value,
                changeability = intakeChangeability.value,
                budget = intakeBudget.value
            )

            val updated = db.decisionDao().getDecisionByIdSync(decId)
            _selectedDecision.value = updated
            d2FramingCompleted.value = true
            _gateErrorMessage.value = null
        }
    }

    /**
     * D1 Intake -> D2 Framing -> D3 Confirmation -> D4 Debate pipeline
     * CRITICAL 3: D2 Framing must be completed before D3 confirmation and D4 War Room.
     */
    fun launchMultiAgentProtocolFromIntake() {
        if (!d2FramingCompleted.value) {
            _gateErrorMessage.value = "Pipeline Violation: D2 Framing must be completed before D3 confirmation."
            return
        }

        val isConfirmed = scopeConfirmed.value && focusAreaConfirmed.value && constraintsConfirmed.value && goalsConfirmed.value
        if (!isConfirmed) {
            _gateErrorMessage.value = "D3 Guard: Scope, focus area, constraints, and goals must all be explicitly confirmed."
            return
        }

        viewModelScope.launch {
            var decision = _selectedDecision.value
            if (decision == null) {
                completeD2Framing()
                decision = _selectedDecision.value ?: return@launch
            }

            // Save D3 confirmation in repository
            repository.confirmD3Gate(
                id = decision.id,
                scopeConfirmed = scopeConfirmed.value,
                focusAreaConfirmed = focusAreaConfirmed.value,
                constraintsConfirmed = constraintsConfirmed.value,
                goalsConfirmed = goalsConfirmed.value
            )

            val updatedDecision = db.decisionDao().getDecisionByIdSync(decision.id)
            _selectedDecision.value = updatedDecision

            // Setup round 1 War Room
            _warRoomRound.value = 1
            _stopRuleTriggered.value = false
            evaluateCurrentWarRoomRound(1, updatedDecision ?: return@launch)
            navigateTo(CockpitScreen.WAR_ROOM)
        }
    }

    fun startWarRoomForSelected() {
        val decision = _selectedDecision.value ?: return
        if (decision.status == DecisionStatus.D1_INTAKE || decision.status == DecisionStatus.D2_FRAMING ||
            !decision.scopeConfirmed || !decision.focusAreaConfirmed || !decision.constraintsConfirmed || !decision.goalsConfirmed) {
            _gateErrorMessage.value = "Cannot launch War Room: D2 Framing and D3 Confirmation checks must be completed."
            navigateTo(CockpitScreen.INTAKE_FRAMING)
            return
        }

        _warRoomRound.value = 1
        _stopRuleTriggered.value = false
        evaluateCurrentWarRoomRound(1, decision)
        navigateTo(CockpitScreen.WAR_ROOM)
    }

    /**
     * Advances War Room rounds with dynamic tier enforcement and Directive 6 Stop Rule.
     */
    fun advanceWarRoom() {
        val decision = _selectedDecision.value ?: return
        if (_stopRuleTriggered.value) {
            synthesizeAndAdvanceToReview(decision)
            return
        }

        val tier = try { ComplexityTier.valueOf(decision.complexityTier) } catch (e: Exception) { ComplexityTier.STANDARD }
        val tierConfig = DecisionMathEngine.getTierConfig(tier)

        // For LIGHT tier: 0 debate rounds, advance immediately to synthesis!
        if (tierConfig.maxDebateRounds == 0) {
            synthesizeAndAdvanceToReview(decision)
            return
        }

        val currentRound = _warRoomRound.value
        val previousDqs = _warRoomResult.value?.dqsScore ?: decision.dqsScore

        if (currentRound < tierConfig.maxDebateRounds) {
            val nextRound = currentRound + 1
            _warRoomRound.value = nextRound
            evaluateCurrentWarRoomRound(nextRound, decision, previousDqs)
        } else {
            // Debate rounds completed according to tier: proceed to D6 Synthesis!
            synthesizeAndAdvanceToReview(decision)
        }
    }

    private fun evaluateCurrentWarRoomRound(round: Int, decision: DecisionEntity, previousDqs: Double = decision.dqsScore) {
        _isDebateRunning.value = true
        val tier = try { ComplexityTier.valueOf(decision.complexityTier) } catch (e: Exception) { ComplexityTier.STANDARD }
        val constraintList = intakeTags.value.map { it.text }

        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            val effectiveKey = apiConfigManager.getEffectiveApiKey().takeIf { it.isNotBlank() }
            val result = WarRoomEngine.evaluateRound(
                decisionTitle = decision.title,
                problemStatement = decision.problemStatement,
                constraints = constraintList,
                tier = tier,
                evidenceType = decision.evidenceType,
                round = round,
                previousDqs = previousDqs,
                apiKey = effectiveKey
            )
            _warRoomResult.value = result
            _isDebateRunning.value = false

            // Directive 6 & CRITICAL 8: Check stop rule if beyond round 1
            if (round > 1 && DecisionMathEngine.shouldStopRefinement(previousDqs, result.dqsScore)) {
                _stopRuleTriggered.value = true
                repository.recordStopRuleTriggered(decision.id, previousDqs, result.dqsScore)
                _selectedDecision.value = db.decisionDao().getDecisionByIdSync(decision.id)
                // Stop further automatic refinement; advance to synthesis
                synthesizeAndAdvanceToReview(decision)
                return@launch
            }

            // Persist round as immutable event in Room
            repository.advanceWarRoomRound(
                id = decision.id,
                round = round,
                newDqs = result.dqsScore,
                summaryPayload = "{\"round\": $round, \"dqs\": ${result.dqsScore}, \"summary\": \"${result.consensusSummary}\", \"mode\": \"SIMULATED_OFFLINE\"}"
            )
            _selectedDecision.value = db.decisionDao().getDecisionByIdSync(decision.id)
        }
    }

    /**
     * D6 Synthesis: Generates real Review Packet and advances to D7 Review.
     */
    fun synthesizeAndAdvanceToReview(decision: DecisionEntity) {
        viewModelScope.launch {
            val currentDqs = _warRoomResult.value?.dqsScore ?: decision.dqsScore
            val constraints = intakeTags.value.map { it.text }
            val effectiveKey = apiConfigManager.getEffectiveApiKey().takeIf { it.isNotBlank() }
            val packet = WarRoomEngine.generateReviewPacket(
                decisionId = decision.id,
                title = decision.title,
                problemStatement = decision.problemStatement,
                selectedOption = decision.selectedOption,
                evidenceType = decision.evidenceType,
                dqsScore = currentDqs,
                constraints = constraints,
                apiKey = effectiveKey
            )
            _reviewPacket.value = packet

            // CRITICAL 4: Persist structured JSON Review Packet
            val packetJson = packet.toJson()
            repository.synthesizeReviewPacket(decision.id, packetJson, currentDqs)

            val updated = db.decisionDao().getDecisionByIdSync(decision.id)
            _selectedDecision.value = updated

            // Auto-run Admission Test for D7 review readiness
            runAdmissionTestForSelected()
            navigateTo(CockpitScreen.BLUEPRINT_5_LENSES)
        }
    }

    /**
     * Directive 3 & CRITICAL 7: Admission Test (Specificity, Novelty, Actionability, Value).
     */
    fun runAdmissionTestForSelected() {
        val decision = _selectedDecision.value ?: return
        viewModelScope.launch {
            val cert = repository.evaluateAdmissionAndIssueCertificate(decision.id)
            val updated = db.decisionDao().getDecisionByIdSync(decision.id)
            _selectedDecision.value = updated
            _finalizationCertificate.value = cert
            val isD6Done = updated?.status == DecisionStatus.D6_SYNTHESIS || updated?.status == DecisionStatus.D7_REVIEW || updated?.status == DecisionStatus.APPROVED
            val hasPacket = !updated?.reviewPacketJson.isNullOrBlank()

            _admissionTestResult.value = DecisionMathEngine.evaluateAdmissionTest(
                title = updated?.title ?: decision.title,
                problemStatement = updated?.problemStatement ?: decision.problemStatement,
                selectedOption = updated?.selectedOption ?: decision.selectedOption,
                risk = updated?.risk ?: decision.risk,
                impact = updated?.impact ?: decision.impact,
                dqsScore = updated?.dqsScore ?: decision.dqsScore,
                evidenceType = updated?.evidenceType ?: decision.evidenceType,
                isD6SynthesisCompleted = isD6Done,
                hasReviewPacket = hasPacket
            )
        }
    }

    fun resolveSmartQueryTradeoff(optionSelected: String, note: String) {
        val decision = _selectedDecision.value ?: return
        viewModelScope.launch {
            repository.resolveSmartQueryTradeoff(decision.id, optionSelected, note)
            val updated = db.decisionDao().getDecisionByIdSync(decision.id)
            _selectedDecision.value = updated
            synthesizeAndAdvanceToReview(updated ?: decision)
        }
    }

    fun setBlueprintLens(lens: BlueprintLens) {
        _activeLens.value = lens
    }

    /**
     * Section 53.4 & Invariant I-01, I-02: Human CEO Gate Attestation.
     */
    fun executeCeoApprovalSignature(
        signerName: String = "Authorized CEO / Chief Architect",
        rationale: String = "Approved with validated empirical evidence and policy compliance.",
        preMortem: String? = null,
        selectedOption: String? = null
    ) {
        val decision = _selectedDecision.value ?: return
        _gateErrorMessage.value = null

        viewModelScope.launch {
            try {
                val hash = repository.executeCeoAttestationSignature(
                    id = decision.id,
                    signerName = signerName,
                    rationale = rationale,
                    preMortem = preMortem,
                    selectedOption = selectedOption
                )
                _signatureHash.value = hash
                val updated = db.decisionDao().getDecisionByIdSync(decision.id)
                _selectedDecision.value = updated
            } catch (e: Exception) {
                _gateErrorMessage.value = e.message ?: "CEO Gate Authorization Failed"
            }
        }
    }

    fun rejectSelectedDecision(rationale: String) {
        val decision = _selectedDecision.value ?: return
        viewModelScope.launch {
            repository.rejectDecision(decision.id, rationale)
            _selectedDecision.value = db.decisionDao().getDecisionByIdSync(decision.id)
        }
    }

    fun deferSelectedDecision(rationale: String) {
        val decision = _selectedDecision.value ?: return
        viewModelScope.launch {
            repository.deferDecision(decision.id, rationale)
            _selectedDecision.value = db.decisionDao().getDecisionByIdSync(decision.id)
        }
    }

    fun recordOutcomeForSelected(
        expected: String,
        observed: String,
        divergence: com.example.data.local.OutcomeDivergence,
        followUp: String
    ) {
        val decision = _selectedDecision.value ?: return
        viewModelScope.launch {
            repository.recordOutcomeObservation(decision.id, expected, observed, divergence, followUp)
            _selectedDecision.value = db.decisionDao().getDecisionByIdSync(decision.id)
        }
    }

    fun simulateGraphEdge(fromId: String, toId: String, relationship: String = "DEPENDS_ON", action: String = "ADD") {
        viewModelScope.launch {
            _whatIfSimulation.value = repository.simulateEdgeOperation(fromId, toId, relationship, action)
        }
    }

    fun clearWhatIfSimulation() {
        _whatIfSimulation.value = null
    }

    fun applyWhatIfMutation(candidateEdge: com.example.core.graph.GraphEdge, action: String) {
        viewModelScope.launch {
            if (action == "ADD") {
                repository.addDependencyEdge(candidateEdge.from, candidateEdge.to, candidateEdge.relationship)
            } else {
                repository.removeDependencyEdge(candidateEdge.from, candidateEdge.to)
            }
            _whatIfSimulation.value = null
            refreshCycles()
        }
    }

    fun computeGraphReconciliationDiff(localEdges: List<com.example.core.graph.GraphEdge>) {
        viewModelScope.launch {
            _reconciliationDiff.value = repository.computeGraphReconciliationDiff(localEdges)
        }
    }

    fun clearReconciliationDiff() {
        _reconciliationDiff.value = null
    }

    /**
     * Directive 12: Fork Decision Timeline.
     * Returns actual forked decision ID and selects that exact decision.
     */
    fun forkDecisionTimeline(
        sourceDecisionId: String,
        newBranchName: String,
        exploratoryTitle: String
    ) {
        viewModelScope.launch {
            try {
                val forkResult = repository.forkDecisionTimeline(sourceDecisionId, newBranchName, exploratoryTitle)
                _activeBranchId.value = forkResult.branchId

                val forkedDec = db.decisionDao().getDecisionByIdSync(forkResult.forkedDecisionId)
                if (forkedDec != null) {
                    selectDecision(forkedDec)
                }
            } catch (e: Exception) {
                _gateErrorMessage.value = e.message ?: "Branch Forking Failed"
            }
        }
    }

    fun testRippleEffect(decisionId: String) {
        viewModelScope.launch {
            val affected = repository.calculateRippleEffect(decisionId)
            _rippleHighlightedNodes.value = affected.toSet()
        }
    }

    fun clearRippleHighlight() {
        _rippleHighlightedNodes.value = emptySet()
    }

    fun refreshCycles() {
        viewModelScope.launch {
            val result = repository.detectCyclesAndRecordTasks()
            _cycleDetection.value = result
        }
    }

    fun createCyclicDeadlockForDemo() {
        viewModelScope.launch {
            val decisions = allDecisions.value
            if (decisions.size >= 2) {
                val nodeA = decisions[0].id
                val nodeB = decisions[1].id
                repository.addDependencyEdge(nodeA, nodeB)
                repository.addDependencyEdge(nodeB, nodeA)
                refreshCycles()
            }
        }
    }

    fun purgeDemoData() {
        viewModelScope.launch {
            db.decisionDao().purgeDemoData()
            _selectedDecision.value = null
            refreshCycles()
        }
    }

    fun clearAllWorkspaceData() {
        viewModelScope.launch {
            db.decisionDao().clearAllDecisions()
            db.decisionDao().clearAllEdges()
            db.decisionDao().clearAllEvents()
            db.decisionDao().clearAllTasks()
            _selectedDecision.value = null
            refreshCycles()
        }
    }

    fun resolveCycleTask(taskId: String, note: String, breakFromId: String? = null, breakToId: String? = null) {
        viewModelScope.launch {
            repository.resolveCycleTask(taskId, note, breakFromId, breakToId)
            refreshCycles()
        }
    }

    /**
     * Upload local decisions, branches, edges, and immutable WAL event history to Firebase Cloud Firestore.
     */
    fun syncAllToFirebase() {
        viewModelScope.launch {
            _isSyncing.value = true
            _cloudSyncStatus.value = "SYNCING TO FIREBASE..."
            val result = firebaseService.syncLocalToCloud(
                activeBranchId = _activeBranchId.value,
                selectedDecisionId = _selectedDecision.value?.id
            )
            when (result) {
                is SyncResult.Success -> {
                    _cloudSyncStatus.value = result.message
                }
                is SyncResult.Error -> {
                    _cloudSyncStatus.value = result.errorMessage
                }
            }
            _isSyncing.value = false
        }
    }

    /**
     * Firebase Authentication & Account State Restore:
     * When any account logs in, restores that account's last active branch, decisions, and complete WAL history.
     */
    fun loginAndRestoreAccount(email: String, pass: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            _userEmail.value = email.trim()
            _cloudSyncStatus.value = "RESTORING ACCOUNT STATE..."
            val result = firebaseService.restoreStateFromCloud(email.trim(), pass)
            when (result) {
                is SyncResult.Success -> {
                    _cloudSyncStatus.value = result.message
                    val restoredBranch = firebaseService.getSavedBranchId()
                    _activeBranchId.value = restoredBranch
                    val restoredDecisionId = firebaseService.getSavedDecisionId()
                    if (restoredDecisionId != null) {
                        val dec = db.decisionDao().getDecisionByIdSync(restoredDecisionId)
                        if (dec != null) {
                            selectDecision(dec)
                        }
                    }
                    refreshCycles()
                }
                is SyncResult.Error -> {
                    _cloudSyncStatus.value = result.errorMessage
                }
            }
            _isSyncing.value = false
        }
    }

    /**
     * API Settings Management: Save external API Key, Endpoint and Model preferences.
     */
    fun saveApiSettings(apiKey: String, endpoint: String, model: String, enabled: Boolean) {
        apiConfigManager.saveSettings(apiKey, endpoint, model, enabled)
        _apiSettings.value = apiConfigManager.getSettings()
    }

    /**
     * Test connection to configured Gemini API Key.
     */
    suspend fun testApiConnection(testKey: String): Pair<Boolean, String> {
        return apiConfigManager.testConnection(testKey)
    }
}
