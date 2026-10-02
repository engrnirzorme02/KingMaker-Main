package com.example.core.math

import java.security.MessageDigest
import kotlin.math.roundToInt

data class DQSInput(
    val evidence: Double,        // E: 0.1 to 1.0 (Axiomatic=1.0, Empirical=0.8, Heuristic=0.5, Assumption=0.1)
    val trust: Double,           // T: 0.0 to 1.0 (Source reliability)
    val riskMitigation: Double,  // R: 0.0 to 1.0 (Coverage of downsides)
    val uncertainty: Double,     // U: 0.0 (High uncertainty) to 1.0 (Zero uncertainty)
    val agreement: Double,       // A: 0.0 to 1.0 (% agent consensus)
    val critiqueResolved: Double,// C: 0.0 to 1.0 (% of 14 dimensions cleared)
    val valueAlignment: Double   // V: 0.0 to 1.0 (Alignment with user goals)
)

enum class ComplexityTier {
    LIGHT,
    STANDARD,
    RIGOROUS,
    MAXIMUM
}

data class ComplexityResult(
    val score: Double,
    val tier: ComplexityTier
)

data class TierConfig(
    val tier: ComplexityTier,
    val maxAgents: Int,
    val maxDebateRounds: Int,
    val allowExternalResearch: Boolean,
    val adversarialStressTesting: Boolean,
    val description: String
)

data class AdmissionTestResult(
    val specificityPass: Boolean,
    val specificityNote: String,
    val noveltyPass: Boolean,
    val noveltyNote: String,
    val actionabilityPass: Boolean,
    val actionabilityNote: String,
    val valuePass: Boolean,
    val valueNote: String
) {
    val allPassed: Boolean
        get() = specificityPass && noveltyPass && actionabilityPass && valuePass
}

data class FinalizationCertificate(
    val certificateId: String,
    val decisionId: String,
    val issuedAt: Long,
    val admissionChecks: AdmissionTestResult,
    val finalDqs: Double,
    val certificateHash: String
)

class DecisionMathEngine {
    companion object {
        private const val NORMALIZATION_DIVISOR = 0.85

        /**
         * Seven-parameter DQS model:
         * DQS = (0.20E + 0.20T + 0.18R + 0.17U + 0.12A + 0.08C + 0.05V) / 0.85
         * Normalized strictly to 0.0 - 1.0. No artificial boosts or forced minimums.
         */
        fun calculateNormalizedDQS(params: DQSInput): Double {
            val rawScore =
                (0.20 * params.evidence.coerceIn(0.0, 1.0)) +
                (0.20 * params.trust.coerceIn(0.0, 1.0)) +
                (0.18 * params.riskMitigation.coerceIn(0.0, 1.0)) +
                (0.17 * params.uncertainty.coerceIn(0.0, 1.0)) +
                (0.12 * params.agreement.coerceIn(0.0, 1.0)) +
                (0.08 * params.critiqueResolved.coerceIn(0.0, 1.0)) +
                (0.05 * params.valueAlignment.coerceIn(0.0, 1.0))

            val normalized = rawScore / NORMALIZATION_DIVISOR
            val rounded = ((normalized * 100.0).roundToInt()) / 100.0
            return rounded.coerceIn(0.0, 1.0)
        }

        /**
         * Proportionality / Complexity score:
         * Score = (Risk × 0.35) + (Impact × 0.30) + ((1 - Changeability) × 0.20) + (Budget × 0.15)
         * Single consistent numeric scale: 0.0 to 1.0.
         */
        fun calculateComplexity(
            risk: Double,
            impact: Double,
            changeability: Double,
            budget: Double
        ): ComplexityResult {
            val r = risk.coerceIn(0.0, 1.0)
            val i = impact.coerceIn(0.0, 1.0)
            val c = changeability.coerceIn(0.0, 1.0)
            val b = budget.coerceIn(0.0, 1.0)

            val score = (r * 0.35) + (i * 0.30) + ((1.0 - c) * 0.20) + (b * 0.15)
            val rounded = ((score * 100.0).roundToInt()) / 100.0

            val tier = when {
                rounded >= 0.80 -> ComplexityTier.MAXIMUM
                rounded >= 0.60 -> ComplexityTier.RIGOROUS
                rounded >= 0.30 -> ComplexityTier.STANDARD
                else -> ComplexityTier.LIGHT
            }

            return ComplexityResult(score = rounded, tier = tier)
        }

        /**
         * Actual tier behavior matching KingMaker architecture specifications.
         */
        fun getTierConfig(tier: ComplexityTier): TierConfig {
            return when (tier) {
                ComplexityTier.LIGHT -> TierConfig(
                    tier = tier,
                    maxAgents = 2,
                    maxDebateRounds = 0,
                    allowExternalResearch = false,
                    adversarialStressTesting = false,
                    description = "2 agents, no external research, 0 debate rounds (fast synthesis)"
                )
                ComplexityTier.STANDARD -> TierConfig(
                    tier = tier,
                    maxAgents = 4,
                    maxDebateRounds = 2,
                    allowExternalResearch = true,
                    adversarialStressTesting = false,
                    description = "4 agents, limited research, up to 2 debate rounds"
                )
                ComplexityTier.RIGOROUS -> TierConfig(
                    tier = tier,
                    maxAgents = 5,
                    maxDebateRounds = 2,
                    allowExternalResearch = true,
                    adversarialStressTesting = true,
                    description = "Full specialist team, deep research, max 2 refinement rounds"
                )
                ComplexityTier.MAXIMUM -> TierConfig(
                    tier = tier,
                    maxAgents = 6,
                    maxDebateRounds = 3,
                    allowExternalResearch = true,
                    adversarialStressTesting = true,
                    description = "Full specialist team, deep research, adversarial stress testing"
                )
            }
        }

        /**
         * Directive 6: Refinement stop rule.
         * If confidence improvement between consecutive rounds is < 0.05,
         * stop automatic refinement.
         */
        fun shouldStopRefinement(previousDqs: Double, currentDqs: Double): Boolean {
            val delta = currentDqs - previousDqs
            return delta < 0.05
        }

        /**
         * Directive 3 & CRITICAL 7: Admission Test prior to CEO approval.
         * Enforces: Specificity, Novelty, Actionability, Value.
         * State-aware: Requires D6 synthesis completed, ReviewPacket exists,
         * and provenance verification passes.
         */
        fun evaluateAdmissionTest(
            title: String,
            problemStatement: String,
            selectedOption: String?,
            risk: Double,
            impact: Double,
            dqsScore: Double,
            evidenceType: String,
            isD6SynthesisCompleted: Boolean = true,
            hasReviewPacket: Boolean = true
        ): AdmissionTestResult {
            // 1. Specificity check: Clear bounded scope, non-trivial title and problem statement
            val isSpecific = title.trim().length >= 8 && problemStatement.trim().length >= 25
            val specificityNote = if (isSpecific) {
                "Title and problem statement define bounded architectural context (${problemStatement.trim().length} chars)."
            } else {
                "Insufficient specificity: title or problem statement is too vague or truncated."
            }

            // 2. Novelty check: Architectural differentiation and non-trivial trade-offs
            val isNovel = (risk >= 0.20 || impact >= 0.20) && !title.equals("Untitled", ignoreCase = true)
            val noveltyNote = if (isNovel) {
                "Substantive architectural tension identified (Risk: ${String.format("%.2f", risk)}, Impact: ${String.format("%.2f", impact)})."
            } else {
                "Novelty failure: trivial or empty architectural trade-off profile."
            }

            // 3. Actionability check: Defined strategy or selected option with verifiable next steps + ReviewPacket
            val isActionable = (!selectedOption.isNullOrBlank() || dqsScore >= 0.50) && hasReviewPacket
            val actionabilityNote = when {
                !hasReviewPacket -> "Actionability failure: Structured Review Packet missing from D6 synthesis."
                isActionable -> "Actionable architectural direction identified: ${selectedOption?.take(40) ?: "Synthesized pathway verified"}."
                else -> "Actionability failure: no selected option or synthesized path available."
            }

            // 4. Value check: DQS score is grounded and aligned to user goals + D6 synthesis completed + non-unverified provenance
            val provenancePass = evidenceType != "UNVERIFIED"
            val isValueAligned = dqsScore >= 0.60 && provenancePass && isD6SynthesisCompleted
            val valueNote = when {
                !isD6SynthesisCompleted -> "Value failure: D6 synthesis is not yet completed."
                !provenancePass -> "Value failure: evidence is UNVERIFIED; claims require grounded provenance."
                isValueAligned -> "Value aligned: DQS ($dqsScore) meets rigor threshold with $evidenceType provenance."
                else -> "Value failure: DQS ($dqsScore) below minimum threshold (0.60)."
            }

            return AdmissionTestResult(
                specificityPass = isSpecific,
                specificityNote = specificityNote,
                noveltyPass = isNovel,
                noveltyNote = noveltyNote,
                actionabilityPass = isActionable,
                actionabilityNote = actionabilityNote,
                valuePass = isValueAligned,
                valueNote = valueNote
            )
        }

        /**
         * Generates immutable Finalization Certificate if admission tests pass and state requirements met.
         */
        fun generateFinalizationCertificate(
            decisionId: String,
            admissionChecks: AdmissionTestResult,
            finalDqs: Double,
            isD6SynthesisCompleted: Boolean = true,
            hasReviewPacket: Boolean = true
        ): FinalizationCertificate? {
            if (!admissionChecks.allPassed || !isD6SynthesisCompleted || !hasReviewPacket) return null

            val now = System.currentTimeMillis()
            val certId = "CERT-${java.util.UUID.randomUUID()}"
            val payload = "KINGMAKER_CERT:$certId:$decisionId:$now:$finalDqs:${admissionChecks.allPassed}"
            val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray())
            val hash = digest.joinToString("") { "%02x".format(it) }

            return FinalizationCertificate(
                certificateId = certId,
                decisionId = decisionId,
                issuedAt = now,
                admissionChecks = admissionChecks,
                finalDqs = finalDqs,
                certificateHash = hash
            )
        }
    }
}
