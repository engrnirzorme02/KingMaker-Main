package com.example.core.math

import com.example.data.local.QualityVector
import org.json.JSONObject
import java.security.MessageDigest
import java.util.TreeMap
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
    LIGHT,       // T1 Lightweight (Reversible, low-risk, minimal reasoning)
    STANDARD,    // T2 Standard (Moderate impact, bounded debate)
    RIGOROUS,    // T3 Rigorous (High-risk, cross-cutting dependencies)
    MAXIMUM      // T3 Maximum (Mission-critical, irreversible, mandatory pre-mortem)
}

data class ComplexityResult(
    val score: Double,
    val tier: ComplexityTier,
    val policyProfileName: String = "PolicyProfile-P1"
)

data class TierConfig(
    val tier: ComplexityTier,
    val maxAgents: Int,
    val maxDebateRounds: Int,
    val allowExternalResearch: Boolean,
    val adversarialStressTesting: Boolean,
    val requiresPreMortem: Boolean,
    val maxCostBudgetUsd: Double,
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
         * KingMaker v7.0: Section 41 Revision Integrity
         * Canonical JSON (RFC 8785 deterministic key order) + SHA-256 Hash.
         */
        fun calculateRevisionHash(
            decisionId: String,
            title: String,
            problemStatement: String,
            options: List<String>,
            constraints: List<String>,
            criteria: List<String>,
            evidenceType: String,
            policyVersion: String = "v7.0-personal"
        ): String {
            val sortedMap = TreeMap<String, Any>()
            sortedMap["constraints"] = constraints.sorted()
            sortedMap["criteria"] = criteria.sorted()
            sortedMap["decisionId"] = decisionId
            sortedMap["evidenceType"] = evidenceType
            sortedMap["options"] = options.sorted()
            sortedMap["policyVersion"] = policyVersion
            sortedMap["problemStatement"] = problemStatement.trim()
            sortedMap["title"] = title.trim()

            val canonicalJson = JSONObject(sortedMap as Map<*, *>).toString()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(canonicalJson.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }

        /**
         * KingMaker v7.0: 9-Dimensional Quality Vector (Section 9 & 48)
         * Evaluates diagnostic vector. Does NOT equal automatic approval!
         */
        fun evaluateQualityVector(
            evidenceType: String,
            hasConfirmedFraming: Boolean,
            constraintsCount: Int,
            optionsCount: Int,
            changeability: Double,
            risk: Double,
            specialistAgreement: Double,
            validationPass: Boolean,
            complexityScore: Double
        ): QualityVector {
            val evStrength = when (evidenceType.uppercase()) {
                "AXIOMATIC" -> 0.95
                "EMPIRICAL" -> 0.85
                "HEURISTIC" -> 0.65
                "ASSUMPTION" -> 0.40
                else -> 0.20
            }

            val frameComp = if (hasConfirmedFraming) 0.90 else 0.45
            val constraintFit = (0.50 + (constraintsCount.coerceAtMost(5) * 0.08)).coerceIn(0.40, 0.95)
            val optCoverage = (0.40 + (optionsCount.coerceAtMost(4) * 0.15)).coerceIn(0.40, 0.95)
            val reversibility = changeability.coerceIn(0.0, 1.0)
            val riskExp = (1.0 - risk).coerceIn(0.10, 0.95) // Inverted: lower risk = higher safety score
            val disagreement = (1.0 - specialistAgreement).coerceIn(0.05, 0.90) // Divergence level
            val valReadiness = if (validationPass) 0.88 else 0.50
            val compPenalty = (complexityScore * 0.25).coerceIn(0.05, 0.35)

            // Diagnostic composite heuristic for scanability
            val composite = (
                (evStrength * 0.20) +
                (frameComp * 0.15) +
                (constraintFit * 0.15) +
                (optCoverage * 0.10) +
                (reversibility * 0.10) +
                (riskExp * 0.15) +
                (valReadiness * 0.15) -
                compPenalty
            ).coerceIn(0.0, 1.0)

            val roundedComposite = ((composite * 100.0).roundToInt()) / 100.0

            val explanation = "Quality Vector evaluated across 9 dimensions under Policy v7.0. " +
                    "Evidence: ${(evStrength * 100).toInt()}%, Framing: ${(frameComp * 100).toInt()}%, " +
                    "Reversibility: ${(reversibility * 100).toInt()}%. Note: Quality is a diagnostic instrument, NOT approval."

            return QualityVector(
                evidenceStrength = evStrength,
                frameCompleteness = frameComp,
                constraintFit = constraintFit,
                optionCoverage = optCoverage,
                reversibility = reversibility,
                riskExposure = riskExp,
                disagreement = disagreement,
                validationReadiness = valReadiness,
                complexityPenalty = compPenalty,
                compositeHeuristic = roundedComposite,
                explanation = explanation
            )
        }

        /**
         * Legacy 7-parameter DQS model retained for legacy tests & backwards compatibility.
         * DQS = (0.20E + 0.20T + 0.18R + 0.17U + 0.12A + 0.08C + 0.05V) / 0.85
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
         * Proportionality Engine (Section 8):
         * Evaluates context classification: Risk, Impact, Changeability, Budget.
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
            val roundedScore = ((score * 100.0).roundToInt()) / 100.0

            val tier = when {
                roundedScore < 0.30 -> ComplexityTier.LIGHT
                roundedScore < 0.65 -> ComplexityTier.STANDARD
                roundedScore < 0.80 -> ComplexityTier.RIGOROUS
                else -> ComplexityTier.MAXIMUM
            }

            val policyName = when (tier) {
                ComplexityTier.LIGHT -> "T1_LIGHT_POLICY"
                ComplexityTier.STANDARD -> "T2_STANDARD_POLICY"
                ComplexityTier.RIGOROUS -> "T3_RIGOROUS_POLICY"
                ComplexityTier.MAXIMUM -> "T3_MAXIMUM_PREMORTEM_POLICY"
            }

            return ComplexityResult(score = roundedScore, tier = tier, policyProfileName = policyName)
        }

        /**
         * Proportionality tier configuration.
         */
        fun getTierConfig(tier: ComplexityTier): TierConfig {
            return when (tier) {
                ComplexityTier.LIGHT -> TierConfig(
                    tier = tier,
                    maxAgents = 2,
                    maxDebateRounds = 0,
                    allowExternalResearch = false,
                    adversarialStressTesting = false,
                    requiresPreMortem = false,
                    maxCostBudgetUsd = 0.10,
                    description = "T1 Light: Low impact, high reversibility. Single round synthesis without adversarial round."
                )
                ComplexityTier.STANDARD -> TierConfig(
                    tier = tier,
                    maxAgents = 3,
                    maxDebateRounds = 1,
                    allowExternalResearch = false,
                    adversarialStressTesting = true,
                    requiresPreMortem = false,
                    maxCostBudgetUsd = 0.50,
                    description = "T2 Standard: Moderate impact. 1 round debate with Red Team critique."
                )
                ComplexityTier.RIGOROUS -> TierConfig(
                    tier = tier,
                    maxAgents = 5,
                    maxDebateRounds = 2,
                    allowExternalResearch = true,
                    adversarialStressTesting = true,
                    requiresPreMortem = true,
                    maxCostBudgetUsd = 1.20,
                    description = "T3 Rigorous: High risk/impact. 2 rounds multi-specialist debate, Devil's Advocate & Coverage Auditor."
                )
                ComplexityTier.MAXIMUM -> TierConfig(
                    tier = tier,
                    maxAgents = 6,
                    maxDebateRounds = 3,
                    allowExternalResearch = true,
                    adversarialStressTesting = true,
                    requiresPreMortem = true,
                    maxCostBudgetUsd = 2.00,
                    description = "T3 Maximum: Irreversible architectural baseline. Full CDR-P1 critique, exhaustive stress tests & mandatory pre-mortem."
                )
            }
        }

        /**
         * Directive 6 Stop Rule: Stop debate when refinement delta < 0.05.
         */
        fun shouldStopRefinement(previousDqs: Double, currentDqs: Double): Boolean {
            val delta = Math.abs(currentDqs - previousDqs)
            return delta < 0.05
        }

        /**
         * Admission Test: Specificity, Novelty, Actionability, Value.
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
            val specPass = title.length >= 8 && problemStatement.length >= 25
            val specNote = if (specPass) "Objective & Scope framed precisely" else "Title or Problem statement too ambiguous"

            val novPass = !selectedOption.isNullOrBlank() || isD6SynthesisCompleted
            val novNote = if (novPass) "Differentiated architectural option identified" else "Pending option selection"

            val actPass = isD6SynthesisCompleted && hasReviewPacket
            val actNote = if (actPass) "Review packet synthesized with executable recommendations" else "Incomplete review packet synthesis"

            val valPass = dqsScore >= 0.60 || evidenceType.uppercase() in listOf("AXIOMATIC", "EMPIRICAL", "HEURISTIC")
            val valNote = if (valPass) "Grounded evidence supports decision value" else "Unverified assumptions dominate"

            return AdmissionTestResult(
                specificityPass = specPass,
                specificityNote = specNote,
                noveltyPass = novPass,
                noveltyNote = novNote,
                actionabilityPass = actPass,
                actionabilityNote = actNote,
                valuePass = valPass,
                valueNote = valNote
            )
        }

        /**
         * Finalization Certificate with cryptographic SHA-256 fingerprint.
         */
        fun generateFinalizationCertificate(
            decisionId: String,
            admissionChecks: AdmissionTestResult,
            finalDqs: Double,
            isD6SynthesisCompleted: Boolean = true,
            hasReviewPacket: Boolean = true
        ): FinalizationCertificate? {
            if (!admissionChecks.allPassed || !isD6SynthesisCompleted || !hasReviewPacket) {
                return null
            }

            val timestamp = System.currentTimeMillis()
            val raw = "CERT:$decisionId:$timestamp:$finalDqs:${admissionChecks.allPassed}"
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(raw.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

            return FinalizationCertificate(
                certificateId = "CERT-${hash.take(12).uppercase()}",
                decisionId = decisionId,
                issuedAt = timestamp,
                admissionChecks = admissionChecks,
                finalDqs = finalDqs,
                certificateHash = hash
            )
        }
    }
}
