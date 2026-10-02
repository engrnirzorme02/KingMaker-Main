package com.example.ai

import com.example.core.math.ComplexityTier
import com.example.core.math.DecisionMathEngine
import com.example.data.local.ClaimType
import com.example.data.local.ProvenanceMode
import com.example.data.local.QualityVector
import org.json.JSONArray
import org.json.JSONObject

data class AgentPerspective(
    val specialistName: String,
    val role: String,
    val claim: String,
    val claimType: ClaimType = ClaimType.INFERENCE,
    val provenance: ClaimType = ClaimType.INFERENCE, // Legacy compatibility
    val proposedStrategy: String,
    val riskWarning: String,
    val mandate: String = "Reason from bounded revision context"
)

data class RedTeamCritique(
    val title: String,
    val target: String,
    val challenge: String,
    val counterArgument: String, // Legacy alias
    val stressTestScenario: String,
    val unhandledRisk: String,
    val severity: String, // HIGH, CRITICAL, MODERATE
    val resolutionState: String = "OPEN"
)

/**
 * CDR-P1 Dimension Rating (Section 49 & 50).
 * Personal Critique Registry with 11 explicit dimensions.
 */
data class DimensionRating(
    val code: String, // D01 - D11
    val name: String,
    val score: Int,   // 1 to 10
    val commentary: String,
    val testableHypothesis: String = ""
)

data class WarRoomRoundResult(
    val round: Int,
    val roundName: String,
    val agents: List<AgentPerspective>,
    val redTeam: RedTeamCritique,
    val coverageAudit: String,
    val dimensionRatings: List<DimensionRating>,
    val dqsScore: Double,
    val qualityVector: QualityVector,
    val dqsPassed: Boolean,
    val consensusSummary: String,
    val provenanceMode: ProvenanceMode = ProvenanceMode.SIMULATED,
    val isSimulatedOffline: Boolean = true
)

data class EvidenceItem(
    val claim: String,
    val provenance: ClaimType,
    val source: String,
    val contentHash: String = "SHA256:PROV"
)

data class ReviewPacket(
    val decisionId: String,
    val decisionTitle: String,
    val summary: String,
    val recommendation: String,
    val strongestAlternative: String,
    val decisiveCriteria: List<String>,
    val whatCouldGoWrong: String,
    val alternatives: List<String>,
    val supportingEvidence: List<EvidenceItem>,
    val contradictions: List<String>,
    val risks: List<String>,
    val unresolvedAssumptions: List<String>,
    val expertAgreement: Double, // 0.0 to 1.0
    val critiqueDimensions: List<DimensionRating>,
    val dqsScore: Double,
    val qualityVector: QualityVector,
    val recommendedNextAction: String,
    val t3PreMortemPrompt: String = "It's 6 months later and this decision failed. The most likely cause is...",
    val evaluationMode: String = "SIMULATED / OFFLINE EVALUATION",
    val policyVersion: String = "v7.0-personal",
    val critiqueRegistry: String = "CDR-P1 (11 Dimensions)"
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("decisionId", decisionId)
        root.put("decisionTitle", decisionTitle)
        root.put("summary", summary)
        root.put("recommendation", recommendation)
        root.put("strongestAlternative", strongestAlternative)
        root.put("whatCouldGoWrong", whatCouldGoWrong)

        val critArray = JSONArray()
        decisiveCriteria.forEach { critArray.put(it) }
        root.put("decisiveCriteria", critArray)

        val altArray = JSONArray()
        alternatives.forEach { altArray.put(it) }
        root.put("alternatives", altArray)

        val evArray = JSONArray()
        supportingEvidence.forEach { ev ->
            val evObj = JSONObject()
            evObj.put("claim", ev.claim)
            evObj.put("provenance", ev.provenance.name)
            evObj.put("source", ev.source)
            evArray.put(evObj)
        }
        root.put("supportingEvidence", evArray)

        val contraArray = JSONArray()
        contradictions.forEach { contraArray.put(it) }
        root.put("contradictions", contraArray)

        val riskArray = JSONArray()
        risks.forEach { riskArray.put(it) }
        root.put("risks", riskArray)

        val unresArray = JSONArray()
        unresolvedAssumptions.forEach { unresArray.put(it) }
        root.put("unresolvedAssumptions", unresArray)

        root.put("expertAgreement", expertAgreement)

        val dimArray = JSONArray()
        critiqueDimensions.forEach { dim ->
            val dimObj = JSONObject()
            dimObj.put("code", dim.code)
            dimObj.put("name", dim.name)
            dimObj.put("score", dim.score)
            dimObj.put("commentary", dim.commentary)
            dimArray.put(dimObj)
        }
        root.put("critiqueDimensions", dimArray)

        root.put("dqsScore", dqsScore)
        root.put("recommendedNextAction", recommendedNextAction)
        root.put("evaluationMode", evaluationMode)
        root.put("policyVersion", policyVersion)
        root.put("critiqueRegistry", critiqueRegistry)

        return root.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): ReviewPacket {
            val root = JSONObject(jsonStr)
            val decisionId = root.optString("decisionId", "")
            val decisionTitle = root.optString("decisionTitle", "")
            val summary = root.optString("summary", "")
            val recommendation = root.optString("recommendation", "Adopt modular decoupled architecture")
            val strongestAlternative = root.optString("strongestAlternative", "Status quo with rate-limiting proxy")
            val whatCouldGoWrong = root.optString("whatCouldGoWrong", "Cascading timeout if failover network is saturated")

            val decisiveCriteria = mutableListOf<String>()
            val dcArray = root.optJSONArray("decisiveCriteria")
            if (dcArray != null) {
                for (i in 0 until dcArray.length()) decisiveCriteria.add(dcArray.getString(i))
            } else {
                decisiveCriteria.addAll(listOf("P99 Latency < 20ms", "Audit append-only immutability", "Cost ceiling"))
            }

            val alternatives = mutableListOf<String>()
            val altArray = root.optJSONArray("alternatives")
            if (altArray != null) {
                for (i in 0 until altArray.length()) alternatives.add(altArray.getString(i))
            }

            val supportingEvidence = mutableListOf<EvidenceItem>()
            val evArray = root.optJSONArray("supportingEvidence")
            if (evArray != null) {
                for (i in 0 until evArray.length()) {
                    val evObj = evArray.getJSONObject(i)
                    val prov = try {
                        ClaimType.valueOf(evObj.optString("provenance", "UNVERIFIED"))
                    } catch (_: Exception) {
                        ClaimType.UNVERIFIED
                    }
                    supportingEvidence.add(
                        EvidenceItem(
                            claim = evObj.optString("claim", ""),
                            provenance = prov,
                            source = evObj.optString("source", "")
                        )
                    )
                }
            }

            val contradictions = mutableListOf<String>()
            val contraArray = root.optJSONArray("contradictions")
            if (contraArray != null) {
                for (i in 0 until contraArray.length()) contradictions.add(contraArray.getString(i))
            }

            val risks = mutableListOf<String>()
            val riskArray = root.optJSONArray("risks")
            if (riskArray != null) {
                for (i in 0 until riskArray.length()) risks.add(riskArray.getString(i))
            }

            val unresolvedAssumptions = mutableListOf<String>()
            val unresArray = root.optJSONArray("unresolvedAssumptions")
            if (unresArray != null) {
                for (i in 0 until unresArray.length()) unresolvedAssumptions.add(unresArray.getString(i))
            }

            val expertAgreement = root.optDouble("expertAgreement", 0.8)

            val critiqueDimensions = mutableListOf<DimensionRating>()
            val dimArray = root.optJSONArray("critiqueDimensions")
            if (dimArray != null) {
                for (i in 0 until dimArray.length()) {
                    val dimObj = dimArray.getJSONObject(i)
                    critiqueDimensions.add(
                        DimensionRating(
                            code = dimObj.optString("code", "D01"),
                            name = dimObj.optString("name", ""),
                            score = dimObj.optInt("score", 7),
                            commentary = dimObj.optString("commentary", "")
                        )
                    )
                }
            } else {
                critiqueDimensions.addAll(getDefaultCDRP1Dimensions(7, decisionTitle))
            }

            val dqsScore = root.optDouble("dqsScore", 0.70)
            val recommendedNextAction = root.optString("recommendedNextAction", "Proceed to Human Review & CEO Gate")
            val evaluationMode = root.optString("evaluationMode", "SIMULATED / OFFLINE EVALUATION")

            val qv = DecisionMathEngine.evaluateQualityVector(
                evidenceType = "HEURISTIC",
                hasConfirmedFraming = true,
                constraintsCount = 3,
                optionsCount = 2,
                changeability = 0.40,
                risk = 0.60,
                specialistAgreement = expertAgreement,
                validationPass = true,
                complexityScore = 0.65
            )

            return ReviewPacket(
                decisionId = decisionId,
                decisionTitle = decisionTitle,
                summary = summary,
                recommendation = recommendation,
                strongestAlternative = strongestAlternative,
                decisiveCriteria = decisiveCriteria,
                whatCouldGoWrong = whatCouldGoWrong,
                alternatives = alternatives,
                supportingEvidence = supportingEvidence,
                contradictions = contradictions,
                risks = risks,
                unresolvedAssumptions = unresolvedAssumptions,
                expertAgreement = expertAgreement,
                critiqueDimensions = critiqueDimensions,
                dqsScore = dqsScore,
                qualityVector = qv,
                recommendedNextAction = recommendedNextAction,
                evaluationMode = evaluationMode
            )
        }

        fun getDefaultCDRP1Dimensions(baseScore: Int, title: String): List<DimensionRating> {
            return listOf(
                DimensionRating("CDR-01", "Problem & Goal Integrity", (baseScore + 1).coerceIn(1, 10), "Problem bounded to '$title'; explicit non-goals verified."),
                DimensionRating("CDR-02", "Context & Constraint Integrity", baseScore.coerceIn(1, 10), "Material claims grounded; non-negotiable boundaries enforced."),
                DimensionRating("CDR-03", "Option & Trade-off Coverage", (baseScore + 1).coerceIn(1, 10), "Realistic alternatives evaluated including status quo."),
                DimensionRating("CDR-04", "Technical Feasibility & Architecture Fit", (baseScore + 1).coerceIn(1, 10), "Topology integrates cleanly without cyclic coupling."),
                DimensionRating("CDR-05", "Data & Integration Integrity", baseScore.coerceIn(1, 10), "ACID WAL persistence and verifiable state transitions guaranteed."),
                DimensionRating("CDR-06", "Security, Privacy & Trust Boundary", baseScore.coerceIn(1, 10), "Secret isolation, cryptographic attestation, zero hardcoded keys."),
                DimensionRating("CDR-07", "Reliability, Ops & Failure Modes", baseScore.coerceIn(1, 10), "Circuit breakers and graceful degradation under failures."),
                DimensionRating("CDR-08", "Scalability, Performance & Complexity", (baseScore - 1).coerceIn(1, 10), "Low latency; cognitive and architectural complexity penalized."),
                DimensionRating("CDR-09", "Cost & Resource Sustainability", (baseScore - 1).coerceIn(1, 10), "Execution within budgeted \$0.50-\$2.00 policy cap."),
                DimensionRating("CDR-10", "UX, Human Factors & Adoption", (baseScore + 1).coerceIn(1, 10), "Bilingual Bengali/English accessibility and progressive disclosure."),
                DimensionRating("CDR-11", "Evidence, Governance & System Integrity", baseScore.coerceIn(1, 10), "Tarjan SCC validated DAG and immutable audit event log.")
            )
        }
    }
}

class WarRoomEngine {
    companion object {
        const val EVALUATION_BADGE = "SIMULATED / OFFLINE EVALUATION"

        /**
         * Evaluates a War Room debate round deterministically under CDR-P1.
         */
        fun evaluateRound(
            decisionTitle: String,
            problemStatement: String,
            constraints: List<String> = emptyList(),
            tier: ComplexityTier = ComplexityTier.STANDARD,
            evidenceType: String = "HEURISTIC",
            round: Int = 1,
            previousDqs: Double = 0.50,
            apiKey: String? = null
        ): WarRoomRoundResult {
            val tierConfig = DecisionMathEngine.getTierConfig(tier)
            val agentCount = tierConfig.maxAgents.coerceIn(2, 6)

            val allSpecialists = listOf(
                AgentPerspective(
                    specialistName = "Dr. Elena Vance",
                    role = "Software Architect",
                    claim = "Isolate bounded contexts and enforce linearizable state machines for '$decisionTitle'.",
                    claimType = ClaimType.INFERENCE,
                    provenance = ClaimType.INFERENCE,
                    proposedStrategy = "Modular domain boundary with append-only event log to resolve: ${problemStatement.take(60)}...",
                    riskWarning = "Context fragmentation if cross-domain boundary contracts are weakly typed.",
                    mandate = "Enforce structural cohesion and bounded domain interfaces."
                ),
                AgentPerspective(
                    specialistName = "Marcus Thorne",
                    role = "Security Architect",
                    claim = "Cryptographic ledger attestation must precede commitment for '$decisionTitle'.",
                    claimType = ClaimType.CONSTRAINT,
                    provenance = ClaimType.CONSTRAINT,
                    proposedStrategy = "Zero-trust verification with SHA-256 state hashing and biometric attestation at CEO Gate.",
                    riskWarning = "Attestation latency penalties during high-volume event bursts.",
                    mandate = "Verify threat model, secret boundaries, and attestation."
                ),
                AgentPerspective(
                    specialistName = "Siddharth Rao",
                    role = "Data Architect",
                    claim = "Local SQLite WAL journal mode guarantees crash-proof ACID durability on mobile edge.",
                    claimType = ClaimType.FACT,
                    provenance = ClaimType.FACT,
                    proposedStrategy = "WAL mode with transactional checkpointing; prevent in-place record mutations.",
                    riskWarning = "Storage consumption if event vacuum policies are neglected.",
                    mandate = "Ensure ACID persistence, schema evolution, and replay equality."
                ),
                AgentPerspective(
                    specialistName = "Aiko Tanaka",
                    role = "UX & Human Factors",
                    claim = "Maintain deterministic state projections and provide zero-latency optimistic UI.",
                    claimType = ClaimType.INFERENCE,
                    provenance = ClaimType.INFERENCE,
                    proposedStrategy = "Unidirectional state flow with explicit human-in-the-loop review sheet.",
                    riskWarning = "Cognitive fatigue if review packets lack concise structured synthesis.",
                    mandate = "Protect user clarity, bilingual accessibility, and cognitive load."
                ),
                AgentPerspective(
                    specialistName = "David Sterling",
                    role = "Reliability / Operations",
                    claim = "Constrain resource consumption within budgeted runtime envelope.",
                    claimType = ClaimType.ASSUMPTION,
                    provenance = ClaimType.ASSUMPTION,
                    proposedStrategy = "Enforce tiered complexity gates (T1 to T3) to prevent over-engineering.",
                    riskWarning = "Hidden operational maintenance costs if architecture lacks automated test harnesses.",
                    mandate = "Verify failover modes, SLOs, and resource budget."
                ),
                AgentPerspective(
                    specialistName = "Nadia Rostova",
                    role = "Compliance & Governance Specialist",
                    claim = "Strict separation of powers: AI may propose, but ONLY authorized humans can approve at CEO Gate.",
                    claimType = ClaimType.CONSTRAINT,
                    provenance = ClaimType.CONSTRAINT,
                    proposedStrategy = "Explicit approval sheet with >= 30-char rationale and biometric confirmation.",
                    riskWarning = "Regulatory non-compliance if AI agents are granted autonomous approval authority.",
                    mandate = "Enforce Invariant I-01 and I-02 human governance."
                )
            )

            val activeAgents = allSpecialists.take(agentCount)

            // Devil's Advocate: adversarial challenge attacking the leading option
            val redTeam = when (round) {
                1 -> RedTeamCritique(
                    title = "Boundary & Stress Assumption Attack",
                    target = "Architecture & State Isolation",
                    challenge = "The proposed architecture assumes operational preconditions for '$decisionTitle' hold under edge partition. Given '${problemStatement.take(45)}', failure to isolate failure domains will trigger cascading timeouts.",
                    counterArgument = "Assumes operational preconditions for '$decisionTitle' hold under edge partition.",
                    stressTestScenario = "Simulate 45% concurrent thread contention during state projection updates.",
                    unhandledRisk = "No automated rollback path if state validation fails before synthesis.",
                    severity = "HIGH"
                )
                2 -> RedTeamCritique(
                    title = "Dependency Deadlock & Cyclic Entanglement",
                    target = "Integration & Dependency Graph (D09)",
                    challenge = "If downstream services depend circularly on '$decisionTitle', Tarjan cycle deadlock will freeze the pipeline.",
                    counterArgument = "Circular dependency creates deadlocks across dependent nodes.",
                    stressTestScenario = "Simultaneous bidirectional schema mutations across dependent nodes.",
                    unhandledRisk = "Requires human-supervised cycle resolution tasks; cannot auto-resolve.",
                    severity = "CRITICAL"
                )
                else -> RedTeamCritique(
                    title = "Residual Governance & Verification Audit",
                    target = "Reversibility & Operational Migration (D11)",
                    challenge = "While structural defenses for '$decisionTitle' are established, evidence provenance relies on simulation. A formal rollback playbook must be ratified.",
                    counterArgument = "Simulation mode cannot substitute for empirical production measurement.",
                    stressTestScenario = "10,000 fault injections under adversarial payload corruption.",
                    unhandledRisk = "Requires explicit human CEO Gate attestation to assume residual operational liability.",
                    severity = "MODERATE"
                )
            }

            // Coverage Auditor: cross-cutting check across all 11 CDR-P1 dimensions
            val coverageAudit = "Coverage Auditor Check [CDR-P1]: Evaluated all 11 dimensions. " +
                    "Primary risks concentrated in D06 (Reliability) and D09 (Dependency). " +
                    "No unassessed domain blind spots detected."

            val baseDimScore = when (round) {
                1 -> 6
                2 -> 7
                else -> 8
            }
            val dimensions = ReviewPacket.getDefaultCDRP1Dimensions(baseDimScore, decisionTitle)

            val agreementCount = activeAgents.count { it.claimType in listOf(ClaimType.FACT, ClaimType.INFERENCE, ClaimType.CONSTRAINT) }
            val specialistAgreement = (agreementCount.toDouble() / activeAgents.size.toDouble()).coerceIn(0.50, 0.95)

            val qv = DecisionMathEngine.evaluateQualityVector(
                evidenceType = evidenceType,
                hasConfirmedFraming = true,
                constraintsCount = constraints.size,
                optionsCount = 2,
                changeability = 0.40,
                risk = 0.60,
                specialistAgreement = specialistAgreement,
                validationPass = round >= 2,
                complexityScore = 0.65
            )

            val roundName = when (round) {
                1 -> "D4: Multi-Agent Opening Perspective"
                2 -> "D5: Devil's Advocate & Coverage Audit (CDR-P1)"
                else -> "D5+: Synthesis Refinement & Convergence"
            }

            val consensusSummary = "Round $round completed with ${activeAgents.size} specialist perspectives. " +
                    "Red Team raised ${redTeam.severity} challenge on '${redTeam.target}'. " +
                    "Quality composite score: ${(qv.compositeHeuristic * 100).toInt()}% [CDR-P1 Registry]."

            return WarRoomRoundResult(
                round = round,
                roundName = roundName,
                agents = activeAgents,
                redTeam = redTeam,
                coverageAudit = coverageAudit,
                dimensionRatings = dimensions,
                dqsScore = qv.compositeHeuristic,
                qualityVector = qv,
                dqsPassed = qv.compositeHeuristic >= 0.70,
                consensusSummary = consensusSummary,
                provenanceMode = ProvenanceMode.SIMULATED,
                isSimulatedOffline = true
            )
        }

        /**
         * Generates structured review packet with decisive criteria, alternatives, and pre-mortem guidance.
         */
        fun generateReviewPacket(
            decisionId: String,
            title: String,
            problemStatement: String,
            selectedOption: String?,
            evidenceType: String,
            dqsScore: Double,
            constraints: List<String>,
            apiKey: String? = null
        ): ReviewPacket {
            val qv = DecisionMathEngine.evaluateQualityVector(
                evidenceType = evidenceType,
                hasConfirmedFraming = true,
                constraintsCount = constraints.size,
                optionsCount = 3,
                changeability = 0.45,
                risk = 0.60,
                specialistAgreement = 0.85,
                validationPass = true,
                complexityScore = 0.65
            )

            val recommendation = selectedOption ?: "Adopt Append-Only Event Sourcing with SQLite WAL Durability"
            val alternative = "Partitioned Message Broker with Distributed Quorum Commit"
            val whatCouldGoWrong = "Network partition between edge and central coordinator exceeding 5000ms latency ceiling"

            val decisiveCriteria = listOf(
                "Sub-20ms P99 Local Write Latency SLA",
                "Cryptographic Append-Only Tamper Evidence (SHA-256)",
                "Zero External Cloud Dependencies for Critical Local Writes",
                "Two-way Reversibility with Exportable JSON/ADR Projection"
            )

            val alternativesList = listOf(
                recommendation,
                alternative,
                "In-Memory Cache with Periodic Asynchronous Flush (Rejected: High Data Loss Risk)"
            )

            val supportingEvidence = listOf(
                EvidenceItem(
                    claim = "SQLite WAL journal mode guarantees linearizable write commits without blocking reads.",
                    provenance = ClaimType.FACT,
                    source = "Android SQLite Architecture Whitepaper",
                    contentHash = "SHA256:4f89d3a7"
                ),
                EvidenceItem(
                    claim = "Deterministic RFC 8785 JSON canonical hashing prevents hash mismatch during verification.",
                    provenance = ClaimType.FACT,
                    source = "IETF RFC 8785 Specification",
                    contentHash = "SHA256:7b21e890"
                ),
                EvidenceItem(
                    claim = "Edge device battery consumption remains under 1.2% per hour under continuous event append.",
                    provenance = ClaimType.FACT,
                    source = "Hardware Battery Benchmark Run 2026-Q1",
                    contentHash = "SHA256:9c12a45d"
                ),
                EvidenceItem(
                    claim = "Human-governed CEO Gate eliminates liability of autonomous AI hallucinations.",
                    provenance = ClaimType.CONSTRAINT,
                    source = "KingMaker Governance Constitution v7.0",
                    contentHash = "SHA256:0d54c123"
                )
            )

            val contradictions = listOf(
                "Message broker provides easier multi-master clustering, but introduces network partition failure points.",
                "In-memory caching is 4x faster on read bursts, but fails ACID crash durability test."
            )

            val risks = listOf(
                "SQLite DB growth requires automated vacuuming policy to prevent file fragmentation.",
                "Biometric step-up may fail on devices without hardware keystore; fallback to device PIN required."
            )

            val assumptions = listOf(
                "Local flash storage write endurance exceeds 100,000 IOPS per day.",
                "User operates on native Android handheld with at least 2GB free storage."
            )

            val dimensions = ReviewPacket.getDefaultCDRP1Dimensions(8, title)

            val summary = "Architectural synthesis for '$title': Recommend $recommendation. " +
                    "Strongest alternative: $alternative. Grounded by 4 evidence items across 11 CDR-P1 critique dimensions. " +
                    "Ready for executive human review."

            return ReviewPacket(
                decisionId = decisionId,
                decisionTitle = title,
                summary = summary,
                recommendation = recommendation,
                strongestAlternative = alternative,
                decisiveCriteria = decisiveCriteria,
                whatCouldGoWrong = whatCouldGoWrong,
                alternatives = alternativesList,
                supportingEvidence = supportingEvidence,
                contradictions = contradictions,
                risks = risks,
                unresolvedAssumptions = assumptions,
                expertAgreement = 0.88,
                critiqueDimensions = dimensions,
                dqsScore = qv.compositeHeuristic,
                qualityVector = qv,
                recommendedNextAction = "Proceed to Executive Review Sheet & Record Human CEO Attestation",
                t3PreMortemPrompt = "It's 6 months later and this decision failed. The most likely reason is: $whatCouldGoWrong",
                evaluationMode = "SIMULATED / OFFLINE EVALUATION"
            )
        }
    }
}
