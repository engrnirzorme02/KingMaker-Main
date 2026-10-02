package com.nirzor.kingmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CockpitScreen
import com.example.ui.KingMakerViewModel
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.getAppStrings
import com.example.ui.screens.BlueprintStudioScreen
import com.example.ui.screens.IntakeFramingScreen
import com.example.ui.screens.SmartQueryModal
import com.example.ui.screens.VaultDagScreen
import com.example.ui.screens.WarRoomScreen
import com.example.ui.theme.MyApplicationTheme

/**
 * Single Activity Entry Point for Nirzor KingMaker Mobile OS.
 * Package: com.nirzor.kingmaker
 * Uses Jetpack Compose with Material3 Obsidian/Dark Palette.
 */
class MainActivity : ComponentActivity() {
    private val viewModel: KingMakerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    KingMakerCockpitApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun KingMakerCockpitApp(
    viewModel: KingMakerViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allDecisions by viewModel.allDecisions.collectAsStateWithLifecycle()
    val branchDecisions by viewModel.branchDecisions.collectAsStateWithLifecycle()
    val branches by viewModel.allBranches.collectAsStateWithLifecycle()
    val activeBranchId by viewModel.activeBranchId.collectAsStateWithLifecycle()
    val edges by viewModel.allEdges.collectAsStateWithLifecycle()
    val selectedDecision by viewModel.selectedDecision.collectAsStateWithLifecycle()
    val events by viewModel.decisionEvents.collectAsStateWithLifecycle()
    val cycleDetection by viewModel.cycleDetection.collectAsStateWithLifecycle()
    val rippleHighlighted by viewModel.rippleHighlightedNodes.collectAsStateWithLifecycle()
    val pendingTasks by viewModel.pendingResolutionTasks.collectAsStateWithLifecycle()

    // Intake & D3 Confirmation state
    val intakeTitle by viewModel.intakeTitle.collectAsStateWithLifecycle()
    val intakeProblem by viewModel.intakeProblemStatement.collectAsStateWithLifecycle()
    val intakeRisk by viewModel.intakeRisk.collectAsStateWithLifecycle()
    val intakeImpact by viewModel.intakeImpact.collectAsStateWithLifecycle()
    val intakeChangeability by viewModel.intakeChangeability.collectAsStateWithLifecycle()
    val intakeBudget by viewModel.intakeBudget.collectAsStateWithLifecycle()
    val complexity by viewModel.realtimeComplexity.collectAsStateWithLifecycle()
    val intakeTags by viewModel.intakeTags.collectAsStateWithLifecycle()
    val scopeConfirmed by viewModel.scopeConfirmed.collectAsStateWithLifecycle()
    val focusAreaConfirmed by viewModel.focusAreaConfirmed.collectAsStateWithLifecycle()
    val constraintsConfirmed by viewModel.constraintsConfirmed.collectAsStateWithLifecycle()
    val goalsConfirmed by viewModel.goalsConfirmed.collectAsStateWithLifecycle()
    val d2FramingCompleted by viewModel.d2FramingCompleted.collectAsStateWithLifecycle()

    // War room state
    val warRoomRound by viewModel.warRoomRound.collectAsStateWithLifecycle()
    val warRoomResult by viewModel.warRoomResult.collectAsStateWithLifecycle()
    val isDebating by viewModel.isDebateRunning.collectAsStateWithLifecycle()
    val stopRuleTriggered by viewModel.stopRuleTriggered.collectAsStateWithLifecycle()

    // D6 Review Packet & D7 Admission state
    val reviewPacket by viewModel.reviewPacket.collectAsStateWithLifecycle()
    val admissionResult by viewModel.admissionTestResult.collectAsStateWithLifecycle()
    val certificate by viewModel.finalizationCertificate.collectAsStateWithLifecycle()
    val gateErrorMessage by viewModel.gateErrorMessage.collectAsStateWithLifecycle()

    // Blueprint state
    val activeLens by viewModel.activeLens.collectAsStateWithLifecycle()
    val signatureHash by viewModel.signatureHash.collectAsStateWithLifecycle()

    // Firebase state
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val apiSettings by viewModel.apiSettings.collectAsStateWithLifecycle()

    // Decisions to show: prioritize branch decisions, fallback to allDecisions if branch has none
    val displayedDecisions = if (branchDecisions.isNotEmpty()) branchDecisions else allDecisions

    // Language state
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val strings = getAppStrings(currentLanguage)

    // Back handling
    BackHandler(enabled = currentScreen != CockpitScreen.VAULT_DAG) {
        if (!viewModel.handleBack()) {
            viewModel.navigateTo(CockpitScreen.VAULT_DAG)
        }
    }

    CompositionLocalProvider(
        LocalAppLanguage provides currentLanguage,
        LocalAppStrings provides strings
    ) {
        when (currentScreen) {
            CockpitScreen.VAULT_DAG -> {
                VaultDagScreen(
                    decisions = displayedDecisions,
                    branches = branches,
                    activeBranchId = activeBranchId,
                    edges = edges,
                    selectedDecision = selectedDecision,
                    rippleHighlightedNodes = rippleHighlighted,
                    cycleDetection = cycleDetection,
                    pendingTasks = pendingTasks,
                    currentLanguage = currentLanguage,
                    onSelectLanguage = { viewModel.setLanguage(it) },
                    onSelectBranch = { branchId ->
                        viewModel.setActiveBranch(branchId)
                    },
                onForkTimeline = { sourceDecisionId, branchName, title ->
                    viewModel.forkDecisionTimeline(sourceDecisionId, branchName, title)
                },
                onSelectDecision = { decision ->
                    viewModel.selectDecision(decision)
                },
                onTestRipple = { id ->
                    viewModel.testRippleEffect(id)
                },
                onOpenWarRoom = { decision ->
                    viewModel.selectDecision(decision)
                    viewModel.startWarRoomForSelected()
                },
                onOpenBlueprint = { decision ->
                    viewModel.selectDecision(decision)
                    viewModel.navigateTo(CockpitScreen.BLUEPRINT_5_LENSES)
                },
                onNewDecisionStream = {
                    viewModel.openNewDecisionStream()
                },
                onTriggerDeadlockDemo = {
                    viewModel.createCyclicDeadlockForDemo()
                },
                onResolveCycleTask = { taskId, note, breakFromId, breakToId ->
                    viewModel.resolveCycleTask(taskId, note, breakFromId, breakToId)
                },
                userEmail = userEmail,
                cloudSyncStatus = cloudSyncStatus,
                isSyncing = isSyncing,
                onSyncToCloud = {
                    viewModel.syncAllToFirebase()
                },
                onLoginAccount = { email, pass ->
                    viewModel.loginAndRestoreAccount(email, pass)
                },
                onPurgeWorkspace = {
                    viewModel.purgeDemoData()
                },
                apiSettings = apiSettings,
                onSaveApiSettings = { key, endpoint, model, enabled ->
                    viewModel.saveApiSettings(key, endpoint, model, enabled)
                },
                onTestApiConnection = { key ->
                    viewModel.testApiConnection(key)
                },
                modifier = modifier
            )
        }

        CockpitScreen.INTAKE_FRAMING -> {
            IntakeFramingScreen(
                title = intakeTitle,
                problemStatement = intakeProblem,
                risk = intakeRisk,
                impact = intakeImpact,
                changeability = intakeChangeability,
                budget = intakeBudget,
                complexity = complexity,
                tags = intakeTags,
                scopeConfirmed = scopeConfirmed,
                focusAreaConfirmed = focusAreaConfirmed,
                constraintsConfirmed = constraintsConfirmed,
                goalsConfirmed = goalsConfirmed,
                isFramingCompleted = d2FramingCompleted,
                onTitleChange = { viewModel.intakeTitle.value = it },
                onProblemChange = { viewModel.intakeProblemStatement.value = it },
                onSliderChange = { r, i, c, b ->
                    viewModel.updateIntakeSliders(r, i, c, b)
                },
                onAddTag = { text, type ->
                    viewModel.addTag(text, type)
                },
                onRemoveTag = { id ->
                    viewModel.removeTag(id)
                },
                onCompleteFraming = {
                    viewModel.completeD2Framing()
                },
                onScopeChange = { viewModel.scopeConfirmed.value = it },
                onFocusAreaChange = { viewModel.focusAreaConfirmed.value = it },
                onConstraintsChange = { viewModel.constraintsConfirmed.value = it },
                onGoalsChange = { viewModel.goalsConfirmed.value = it },
                onLaunchProtocol = {
                    viewModel.launchMultiAgentProtocolFromIntake()
                },
                onBack = {
                    viewModel.navigateTo(CockpitScreen.VAULT_DAG)
                },
                modifier = modifier
            )
        }

        CockpitScreen.WAR_ROOM -> {
            val decision = selectedDecision ?: displayedDecisions.firstOrNull()
            if (decision != null) {
                WarRoomScreen(
                    decision = decision,
                    currentRound = warRoomRound,
                    roundResult = warRoomResult,
                    isDebating = isDebating,
                    stopRuleTriggered = stopRuleTriggered,
                    onAdvanceRound = {
                        viewModel.advanceWarRoom()
                    },
                    onBack = {
                        viewModel.navigateTo(CockpitScreen.VAULT_DAG)
                    },
                    modifier = modifier
                )
            } else {
                viewModel.navigateTo(CockpitScreen.VAULT_DAG)
            }
        }

        CockpitScreen.SMART_QUERY -> {
            val decision = selectedDecision ?: displayedDecisions.firstOrNull()
            if (decision != null) {
                SmartQueryModal(
                    decision = decision,
                    onResolve = { choice, note ->
                        viewModel.resolveSmartQueryTradeoff(choice, note)
                    },
                    onBack = {
                        viewModel.navigateTo(CockpitScreen.WAR_ROOM)
                    },
                    modifier = modifier
                )
            } else {
                viewModel.navigateTo(CockpitScreen.VAULT_DAG)
            }
        }

        CockpitScreen.BLUEPRINT_5_LENSES -> {
            val decision = selectedDecision ?: displayedDecisions.firstOrNull()
            if (decision != null) {
                BlueprintStudioScreen(
                    decision = decision,
                    events = events,
                    activeLens = activeLens,
                    activeBranchId = activeBranchId,
                    signatureHash = signatureHash,
                    reviewPacket = reviewPacket,
                    admissionResult = admissionResult,
                    certificate = certificate,
                    gateErrorMessage = gateErrorMessage,
                    onSelectLens = { lens ->
                        viewModel.setBlueprintLens(lens)
                    },
                    onSignApproved = {
                        viewModel.executeCeoApprovalSignature()
                    },
                    onBack = {
                        viewModel.navigateTo(CockpitScreen.VAULT_DAG)
                    },
                    modifier = modifier
                )
            } else {
                viewModel.navigateTo(CockpitScreen.VAULT_DAG)
            }
        }
    }
}
}
