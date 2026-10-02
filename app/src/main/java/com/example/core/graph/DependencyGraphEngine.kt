package com.example.core.graph

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
    val cycles: List<List<String>>
)

data class TopologicalNodeCoordinate(
    val nodeId: String,
    val rank: Int,
    val slotIndex: Int,
    val totalInRank: Int,
    val normalizedX: Float, // 0.0 to 1.0
    val normalizedY: Float  // 0.0 to 1.0
)

class DependencyGraphEngine {
    companion object {
        /**
         * Directive 9: Ripple Effect Semantics.
         * If A DEPENDS_ON B (where edge.from = A, edge.to = B),
         * then changing prerequisite B directly and transitively affects dependent A.
         *
         * Traversal follows dependency semantics:
         * For DEPENDS_ON edges: when B changes, traverse to all A that depend on B.
         * For FORKED_TO edges: when root changes, traverse to fork descendants.
         */
        fun calculateRippleEffect(changedNodeId: String, edges: List<GraphEdge>): List<String> {
            // Build dependency mapping: prerequisite -> list of dependents
            val dependentsMap = mutableMapOf<String, MutableList<String>>()

            edges.forEach { edge ->
                when (edge.relationship) {
                    "DEPENDS_ON" -> {
                        // edge.from depends on edge.to -> when edge.to changes, edge.from is affected
                        dependentsMap.getOrPut(edge.to) { mutableListOf() }.add(edge.from)
                    }
                    "FORKED_TO" -> {
                        // edge.to was forked from edge.from -> when edge.from changes, edge.to is affected
                        dependentsMap.getOrPut(edge.from) { mutableListOf() }.add(edge.to)
                    }
                    else -> {
                        // Default fallback
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
         * Tarjan's Strongly Connected Components (SCC) Cycle Detection.
         * Identifies cycles / deadlocks in the dependency graph.
         */
        fun detectCycles(allNodeIds: Set<String>, edges: List<GraphEdge>): CycleDetectionResult {
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
            return CycleDetectionResult(
                hasCycle = cyclicNodes.isNotEmpty(),
                cyclicNodeIds = cyclicNodes,
                cycles = sccs
            )
        }

        /**
         * Directive 8: Identify weakest-evidence node in cyclic dependencies.
         * Hierarchy: UNVERIFIED (1) < ASSUMPTION (2) < HEURISTIC (3) < EMPIRICAL (4) < AXIOMATIC (5).
         * Lowest score breaks ties.
         */
        fun identifyWeakestEvidenceNode(
            cyclicNodeIds: Set<String>,
            nodeEvidenceMap: Map<String, Pair<String, Double>> // nodeId -> (evidenceType, dqsScore)
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
