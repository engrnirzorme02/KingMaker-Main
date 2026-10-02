package com.example.services.gateway

import com.example.data.local.ProvenanceMode

/**
 * KingMaker v7.0: Provider-Agnostic Model Gateway Abstraction (Section 13, 14 & 39).
 * Decouples core decision intelligence from vendor SDKs.
 * Enforces execution budgets, explicit provenance, and failure states.
 * Strictly prevents silent fallback from provider failure to fake/simulated output.
 */

data class ExecutionBudget(
    val maxRounds: Int = 3,
    val timeoutMillis: Long = 15000L,
    val maxCostEstimateUsd: Double = 0.50,
    val monthlyCapUsd: Double = 10.0,
    val stopCondition: (currentDqsDelta: Double) -> Boolean = { delta -> delta < 0.05 }
)

data class ProviderMetadata(
    val providerId: String,          // e.g., "gemini", "openai", "anthropic", "replay", "mock"
    val modelName: String,           // e.g., "gemini-2.5-flash", "claude-3-5-sonnet"
    val endpointUrl: String? = null,
    val isLocalSimulation: Boolean = false,
    val isAuthenticatedBackend: Boolean = false
)

data class ProvenanceMetadata(
    val mode: ProvenanceMode,         // SIMULATED, REPLAY, PROVIDER_BACKED
    val providerId: String,
    val modelVersion: String,
    val executionTimestamp: Long = System.currentTimeMillis(),
    val payloadHash: String,          // SHA-256 deterministic envelope hash
    val costEstimateUsd: Double = 0.0
)

data class ExecutionRequest(
    val taskId: String,
    val decisionId: String,
    val systemPrompt: String,
    val userPayload: String,
    val contextJson: String? = null,
    val budget: ExecutionBudget = ExecutionBudget()
)

sealed class ExecutionResult {
    data class Success(
        val envelope: ArtifactEnvelope,
        val provenance: ProvenanceMetadata
    ) : ExecutionResult()

    data class CheckpointFailed(
        val reason: String,
        val statusCode: Int? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : ExecutionResult()

    data class NotConfigured(
        val message: String = "No authenticated AI backend or gateway endpoint configured."
    ) : ExecutionResult()

    data class BudgetExceeded(
        val reason: String,
        val estimatedCost: Double
    ) : ExecutionResult()
}

data class ArtifactEnvelope(
    val content: String,
    val rawResponse: String? = null,
    val format: String = "application/json"
)

/**
 * Provider Adapter interface for pluggable AI backends.
 */
interface ProviderAdapter {
    val metadata: ProviderMetadata
    suspend fun execute(request: ExecutionRequest): ExecutionResult
}

/**
 * Model Gateway facade providing execution orchestration and budget enforcement.
 */
interface ModelGateway {
    fun getActiveProvider(): ProviderMetadata
    suspend fun executeInquiry(request: ExecutionRequest): ExecutionResult
    suspend fun validateConnection(): Pair<Boolean, String>
}
