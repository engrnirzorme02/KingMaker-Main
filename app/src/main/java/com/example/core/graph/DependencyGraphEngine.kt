package com.example.core.graph

import com.example.data.local.ConflictClass
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.min

data class GraphEdge(
    val from: String,
    val to: String,
    val relationship: String = "DEPENDS_ON"
)

data class CycleDetectionResult(
    val hasCycle: Boolean,
    val cyclicNodeIds: Set<String>,
    val cycles: List<List<String>>,
    val deduplicationKey: String = ""
)

data class TopologicalNodeCoordinate(
    val nodeId: String,
    val rank: Int,
    val slotIndex: Int,
    val totalInRank: Int,
    val normalizedX: Float, // 0.0 to 1.0
    val normalizedY: Float  // 0.0 to 1.0
)

/**
 * Section 12 & 51.3: What-if Simulation Result.
 * Computes projected graph changes without mutating authoritative state.
 */
data class WhatIfSimulationResult(
    val candidateEdge: GraphEdge,
    val action: String, // ADD or REMOVE
    val introducesCycle: Boolean,
    val resolvesCycle: Boolean,
    val affectedNodes: Set<String>,
    val projectedGraphHash: String,
    val summary: String
)

/**
 * Section 52: Semantic Graph Diff for Reconciliation Sandbox.
 */
data class SemanticGraphDiff(
    val nonOverlappingEdges: List<GraphEdge>,
    val conflictingEdges: List<Pair<GraphEdge, String>>, // (edge, conflictReason)
    val conflictClass: ConflictClass,
    val affectedNodeIds: Set<String>,
    val requiresManualReconciliation: Boolean,
    val reconciliationExplanation: String
)

class DependencyGraphEngine {
    companion object {

        /**
         * Calculates SHA-256 hash of graph topology.
         */
        fun calculateGraphHash(edges: List<GraphEdge>): String {
            val serialized = edges.map { "${it.from}->${it.relationship}->${it.to}" }
                .sorted()
                .joinToString(";")
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(serialized.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }

        /**
         * Ripple Effect Semantics:
         * If A DEPENDS_ON B (where edge.from = A, edge.to = B),
         * then changing prerequisite B directly and transitively affects dependent A.
         */
        fun calculateRippleEffect(changedNodeId: String, edges: List<GraphEdge>): List<String> {
            val dependentsMap = mutableMapOf<String, MutableList<String>>()

            edges.forEach { edge ->
                when (edge.relationship) {
                    "DEPENDS_ON", "CONSTRAINS", "AFFECTS" -> {
                        dependentsMap.getOrPut(edge.to) { mutableListOf() }.add(edge.from)
                    }
                    "FORKED_TO", "DERIVED_FROM", "SUPERSEDES" -> {
                        dependentsMap.getOrPut(edge.from) { mutableListOf() }.add(edge.to)
                    }
                    else -> {
                        dependentsMap.getOrPut(edge.to) { mutableListOf() }.add(edge.from)
                    }
                }
            }

            val affected = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            queue.add(changedNodeId)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                val downstreamDependents = dependentsMap[current] ?: emptyList()
                for (next in downstreamDependents) {
                    if (affected.add(next)) {
                        queue.add(next)
                    }
                }
            }
            return affected.toList()
        }

        /**
         * Tarjan's Strongly Connected Components (SCC) Cycle Detection (Section 12 & 51).
         * Generates deduplicationKey: revisionHash + sorted(nodeIds) + sorted(cycleEdges).
         */
        fun detectCycles(allNodeIds: Set<String>, edges: List<GraphEdge>, revisionHash: String = "HEAD"): CycleDetectionResult {
            val adjacency = mutableMapOf<String, MutableList<String>>()
            allNodeIds.forEach { adjacency[it] = mutableListOf() }
            edges.forEach { edge ->
                adjacency.getOrPut(edge.from) { mutableListOf() }.add(edge.to)
            }

            var index = 0
            val indices = mutableMapOf<String, Int>()
            val lowlink = mutableMapOf<String, Int>()
            val onStack = mutableSetOf<String>()
            val stack = ArrayDeque<String>()
            val sccs = mutableListOf<List<String>>()

            fun strongConnect(v: String) {
                indices[v] = index
                lowlink[v] = index
                index++
                stack.addLast(v)
                onStack.add(v)

                val neighbors = adjacency[v] ?: emptyList()
                for (w in neighbors) {
                    if (!indices.containsKey(w)) {
                        strongConnect(w)
                        lowlink[v] = min(lowlink[v] ?: 0, lowlink[w] ?: 0)
                    } else if (onStack.contains(w)) {
                        lowlink[v] = min(lowlink[v] ?: 0, indices[w] ?: 0)
                    }
                }

                if (lowlink[v] == indices[v]) {
                    val scc = mutableListOf<String>()
                    while (true) {
                        val w = stack.removeLast()
                        onStack.remove(w)
                        scc.add(w)
                        if (w == v) break
                    }
                    if (scc.size > 1 || (adjacency[v]?.contains(v) == true)) {
                        sccs.add(scc)
                    }
                }
            }

            for (node in allNodeIds) {
                if (!indices.containsKey(node)) {
                    strongConnect(node)
                }
            }

            val cyclicNodes = sccs.flatten().toSet()
            val sortedNodeStr = cyclicNodes.sorted().joinToString(",")
            val dedupKey = "$revisionHash:$sortedNodeStr"

            return CycleDetectionResult(
                hasCycle = cyclicNodes.isNotEmpty(),
                cyclicNodeIds = cyclicNodes,
                cycles = sccs,
                deduplicationKey = dedupKey
            )
        }

        /**
         * Identifies weakest-evidence node in cyclic dependencies.
         */
        fun identifyWeakestEvidenceNode(
            cyclicNodeIds: Set<String>,
            nodeEvidenceMap: Map<String, Pair<String, Double>>
        ): String? {
            if (cyclicNodeIds.isEmpty()) return null

            fun evidenceWeight(type: String): Int = when (type.uppercase()) {
                "UNVERIFIED" -> 1
                "ASSUMPTION" -> 2
                "HEURISTIC" -> 3
                "EMPIRICAL" -> 4
                "AXIOMATIC" -> 5
                else -> 2
            }

            return cyclicNodeIds.minByOrNull { id ->
                val (evidenceType, dqs) = nodeEvidenceMap[id] ?: Pair("ASSUMPTION", 0.5)
                val weight = evidenceWeight(evidenceType)
                weight * 10.0 + dqs
            }
        }

        /**
         * Section 51.3: What-if Simulation.
         * Evaluates effect of candidate edge change without mutating authoritative graph.
         */
        fun simulateEdgeOperation(
            currentEdges: List<GraphEdge>,
            allNodeIds: Set<String>,
            candidateEdge: GraphEdge,
            action: String // "ADD" or "REMOVE"
        ): WhatIfSimulationResult {
            val simulatedEdges = if (action == "ADD") {
                if (currentEdges.any { it.from == candidateEdge.from && it.to == candidateEdge.to }) {
                    currentEdges
                } else {
                    currentEdges + candidateEdge
                }
            } else {
                currentEdges.filterNot { it.from == candidateEdge.from && it.to == candidateEdge.to }
            }

            val baselineCycle = detectCycles(allNodeIds, currentEdges)
            val simulatedCycle = detectCycles(allNodeIds, simulatedEdges)

            val introducesCycle = !baselineCycle.hasCycle && simulatedCycle.hasCycle
            val resolvesCycle = baselineCycle.hasCycle && !simulatedCycle.hasCycle

            val affectedNodes = calculateRippleEffect(candidateEdge.from, simulatedEdges).toSet() + candidateEdge.from + candidateEdge.to
            val projectedHash = calculateGraphHash(simulatedEdges)

            val summary = when {
                resolvesCycle -> "Simulated $action successfully resolves circular deadlock! Dependency graph restored to clean DAG."
                introducesCycle -> "WARNING: Simulated $action introduces a circular dependency cycle between ${simulatedCycle.cyclicNodeIds}!"
                action == "ADD" -> "Simulated ADD: Links '${candidateEdge.from}' -> '${candidateEdge.to}' (${candidateEdge.relationship}). Touches ${affectedNodes.size} nodes."
                else -> "Simulated REMOVE: Cuts link between '${candidateEdge.from}' and '${candidateEdge.to}'. Touches ${affectedNodes.size} nodes."
            }

            return WhatIfSimulationResult(
                candidateEdge = candidateEdge,
                action = action,
                introducesCycle = introducesCycle,
                resolvesCycle = resolvesCycle,
                affectedNodes = affectedNodes,
                projectedGraphHash = projectedHash,
                summary = summary
            )
        }

        /**
         * Section 52: Semantic Graph Diff for Offline-to-Online Reconciliation.
         */
        fun computeGraphReconciliationDiff(
            serverEdges: List<GraphEdge>,
            localEdges: List<GraphEdge>,
            allNodeIds: Set<String>
        ): SemanticGraphDiff {
            val serverEdgeSet = serverEdges.toSet()
            val localEdgeSet = localEdges.toSet()

            val nonOverlapping = localEdges.filter { it !in serverEdgeSet }
            val conflicting = mutableListOf<Pair<GraphEdge, String>>()

            nonOverlapping.forEach { edge ->
                // Check if reverse edge exists on server (creating mutual loop)
                val reverseOnServer = serverEdges.find { it.from == edge.to && it.to == edge.from }
                if (reverseOnServer != null) {
                    conflicting.add(Pair(edge, "Creates immediate circular conflict with Server edge (${reverseOnServer.from} -> ${reverseOnServer.to})"))
                }
            }

            val mergedCandidate = (serverEdges + nonOverlapping.filter { edge ->
                conflicting.none { it.first == edge }
            }).distinct()

            val cycleCheck = detectCycles(allNodeIds, mergedCandidate)

            val conflictClass = when {
                cycleCheck.hasCycle -> ConflictClass.CYCLE_CONFLICT
                conflicting.isNotEmpty() -> ConflictClass.SAME_EDGE_CONFLICT
                nonOverlapping.isNotEmpty() -> ConflictClass.NON_OVERLAPPING
                else -> ConflictClass.NON_OVERLAPPING
            }

            val requiresManual = conflictClass != ConflictClass.NON_OVERLAPPING

            val explanation = when (conflictClass) {
                ConflictClass.NON_OVERLAPPING -> "All local graph operations are non-overlapping with server state. Safe to merge automatically into draft."
                ConflictClass.CYCLE_CONFLICT -> "Merge candidate introduces an illegal cycle! Human Guided Resolution Task required."
                ConflictClass.SAME_EDGE_CONFLICT -> "Conflicting edge mutations detected between local outbox and server graph."
                else -> "Reconciliation required before sealing new revision."
            }

            val affected = nonOverlapping.flatMap { listOf(it.from, it.to) }.toSet()

            return SemanticGraphDiff(
                nonOverlappingEdges = nonOverlapping,
                conflictingEdges = conflicting,
                conflictClass = conflictClass,
                affectedNodeIds = affected,
                requiresManualReconciliation = requiresManual,
                reconciliationExplanation = explanation
            )
        }

        /**
         * Lightweight Topological Rank Calculation for rendering DAG.
         */
        fun calculateTopologicalCoordinates(
            nodeIds: List<String>,
            edges: List<GraphEdge>
        ): Map<String, TopologicalNodeCoordinate> {
            if (nodeIds.isEmpty()) return emptyMap()

            val nodeSet = nodeIds.toSet()
            val validEdges = edges.filter { it.from in nodeSet && it.to in nodeSet }

            val adjacency = mutableMapOf<String, MutableList<String>>()
            val inDegree = mutableMapOf<String, Int>()
            nodeIds.forEach {
                adjacency[it] = mutableListOf()
                inDegree[it] = 0
            }

            validEdges.forEach { edge ->
                adjacency[edge.from]?.add(edge.to)
                inDegree[edge.to] = (inDegree[edge.to] ?: 0) + 1
            }

            val ranks = mutableMapOf<String, Int>()
            nodeIds.forEach { ranks[it] = 0 }

            val queue = ArrayDeque<String>()
            nodeIds.filter { (inDegree[it] ?: 0) == 0 }.forEach { queue.add(it) }

            if (queue.isEmpty()) {
                nodeIds.forEach { queue.add(it) }
            }

            val visited = mutableSetOf<String>()
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                visited.add(current)
                val currentRank = ranks[current] ?: 0

                val neighbors = adjacency[current] ?: emptyList()
                for (next in neighbors) {
                    val nextRank = max(ranks[next] ?: 0, currentRank + 1)
                    ranks[next] = nextRank
                    if (visited.add(next)) {
                        queue.add(next)
                    }
                }
            }

            val maxRank = ranks.values.maxOrNull() ?: 0
            val rankGroups = mutableMapOf<Int, MutableList<String>>()
            nodeIds.forEach { id ->
                val r = ranks[id] ?: 0
                rankGroups.getOrPut(r) { mutableListOf() }.add(id)
            }

            val result = mutableMapOf<String, TopologicalNodeCoordinate>()
            val totalRanks = maxRank + 1

            rankGroups.forEach { (rank, members) ->
                val countInRank = members.size
                members.forEachIndexed { index, id ->
                    val normalizedX = (rank + 1f) / (totalRanks + 1f)
                    val normalizedY = (index + 1f) / (countInRank + 1f)

                    result[id] = TopologicalNodeCoordinate(
                        nodeId = id,
                        rank = rank,
                        slotIndex = index,
                        totalInRank = countInRank,
                        normalizedX = normalizedX,
                        normalizedY = normalizedY
                    )
                }
            }

            return result
        }
    }
}
