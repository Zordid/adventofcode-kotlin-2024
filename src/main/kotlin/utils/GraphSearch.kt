package utils

import java.util.*
import kotlin.collections.ArrayDeque

enum class SearchControl { STOP, CONTINUE }
typealias DebugHandler<N> = (level: Int, nodesOnLevel: Collection<N>, nodesVisited: Collection<N>) -> SearchControl

typealias SolutionPredicate<N> = (node: N) -> Boolean

data class SearchResult<N>(val solution: N?, val distance: Map<N, Int>, val prev: Map<N, N>) {
    val success: Boolean get() = solution != null
    val distanceToStart: Int? = solution?.let { distance[it] }
    val steps: Int? by lazy { (path.size - 1).takeIf { it >= 0 } }
    val path by lazy { buildPath() }

    private fun buildPath(): List<N> {
        val path = ArrayDeque<N>()
        if (solution in distance) {
            var nodeFoundThroughPrevious: N? = solution
            while (nodeFoundThroughPrevious != null) {
                path.addFirst(nodeFoundThroughPrevious)
                nodeFoundThroughPrevious = prev[nodeFoundThroughPrevious]
            }
        }
        return path
    }

}

data class MultiSolutionSearchResult<N>(val solutions: Set<N>, val distance: Map<N, Int>, val prev: Map<N, List<N>>) {
    val success: Boolean get() = solutions.isNotEmpty()
    val distanceToStart: Int? = solutions.firstOrNull()?.let { distance[it] }
    val paths by lazy { findAllShortestPaths() }

    private fun findAllShortestPaths(): List<List<N>> {
        fun backtrack(current: N, path: List<N> = listOf(current)): List<List<N>> {
            val prev = prev[current] ?: return listOf(listOf(current) + path.reversed())
            return buildList {
                val pathToCurrent = path + current
                for (predecessor in prev) {
                    addAll(backtrack(predecessor, pathToCurrent))
                }
            }
        }
        if (solutions.isEmpty()) return emptyList()
        return solutions.flatMap { backtrack(it) }
    }
}

interface SearchDefinition<N> {
    fun neighborNodes(node: N): Collection<N>
    fun cost(from: N, to: N): Int
    fun costEstimation(from: N, to: N): Int = throw NotImplementedError("please provide cost estimation")
}

interface SearchState<N> {
    val next: N?
    val dist: Map<N, Int>
    val prev: Map<N, N>
}

class AStarSearch<N>(
    startNodes: Collection<N>,
    val neighborNodes: (N) -> Collection<N>,
    val cost: (N, N) -> Int,
    val costEstimation: (N, N) -> Int,
    val onExpand: (SearchState<N>.(N) -> Unit)? = null,
) {
    constructor(
        startNode: N,
        neighborNodes: (N) -> Collection<N>,
        cost: (N, N) -> Int,
        costEstimation: (N, N) -> Int,
        onExpand: (SearchState<N>.(N) -> Unit)? = null,
    ) : this(listOf(startNode), neighborNodes, cost, costEstimation, onExpand)

    constructor(
        startNodes: Collection<N>,
        definition: SearchDefinition<N>,
        onExpand: (SearchState<N>.(N) -> Unit)? = null,
    ) : this(
        startNodes,
        definition::neighborNodes,
        definition::cost,
        definition::costEstimation,
        onExpand
    )

    constructor(
        startNode: N,
        definition: SearchDefinition<N>,
        onExpand: (SearchState<N>.(N) -> Unit)? = null,
    ) : this(listOf(startNode), definition, onExpand)

    private val dist = HashMap<N, Int>().apply { startNodes.forEach { put(it, 0) } }
    private val prev = HashMap<N, N>()
    private val openList = minPriorityQueueOf(startNodes.map { it to 0 })
    private val closedList = HashSet<N>()

    val state = object : SearchState<N> {
        override val next get() = openList.peekOrNull()
        override val dist get() = this@AStarSearch.dist
        override val prev get() = this@AStarSearch.prev
    }

    fun search(destinationNode: N, limitSteps: Int? = null): SearchResult<N> {

        fun expandNode(currentNode: N) {
            onExpand?.invoke(state, currentNode)
            for (successor in neighborNodes(currentNode)) {
                if (successor in closedList)
                    continue

                val tentativeDist = dist[currentNode]!! + cost(currentNode, successor)
                if (successor in openList && tentativeDist >= dist[successor]!!)
                    continue

                prev[successor] = currentNode
                dist[successor] = tentativeDist

                val f = tentativeDist + costEstimation(successor, destinationNode)
                openList.insertOrUpdate(successor, f)
            }
        }

        if (destinationNode in closedList)
            return SearchResult(destinationNode, dist, prev)

        var steps = 0
        while (steps++ != limitSteps && openList.isNotEmpty()) {
            val currentNode = openList.extractMin()
            if (currentNode == destinationNode)
                return SearchResult(destinationNode, dist, prev)

            closedList += currentNode
            expandNode(currentNode)
        }

        return SearchResult(null, dist, prev)
    }
}

class Dijkstra<N>(
    val startNode: N,
    private val neighborNodes: (N) -> Collection<N>,
    private val cost: ((N, N) -> Int)? = null,
) {
    constructor(startNode: N, definition: SearchDefinition<N>) : this(
        startNode,
        definition::neighborNodes,
        definition::cost
    )

    fun search(endNode: N) = search { it == endNode }

    fun search(predicate: SolutionPredicate<N>): SearchResult<N> {
        val dist = mutableMapOf<N, Int>(startNode to 0)
        val prev = mutableMapOf<N, N>()
        val queue = minPriorityQueueOf(startNode to 0)

        while (queue.isNotEmpty()) {
            val u = queue.extractMin()
            if (predicate(u)) {
                return SearchResult(u, dist, prev)
            }
            for (v in neighborNodes(u)) {
                val alt = dist[u]!! + (cost?.invoke(u, v) ?: 1)
                if (alt < dist.getOrDefault(v, Int.MAX_VALUE)) {
                    dist[v] = alt
                    prev[v] = u
                    queue.insertOrUpdate(v, alt)
                }
            }
        }

        // no matching solution found
        return SearchResult(null, dist, prev)
    }

    fun searchAll(predicate: SolutionPredicate<N>): MultiSolutionSearchResult<N> {
        val dist = mutableMapOf(startNode to 0)
        val prev = mutableMapOf<N, MutableList<N>>()
        val queue = minPriorityQueueOf(startNode to 0)

        while (queue.isNotEmpty()) {
            val (u, priority) = queue.extractMinWithPriority()
            if (predicate(u)) {
                // do not forget to ask for more solutions with the same priority
                val moreSolutions = if (queue.minPriority == priority)
                    queue.extractAllMin().filter { predicate(it) } else emptyList()
                return MultiSolutionSearchResult(setOf(u) + moreSolutions, dist, prev)
            }
            for (v in neighborNodes(u)) {
                val alt = dist[u]!! + (cost?.invoke(u, v) ?: 1)
                val known = dist.getOrDefault(v, Int.MAX_VALUE)
                when {
                    // relax, if new distance is less than known
                    alt < known -> {
                        dist[v] = alt
                        prev[v] = mutableListOf(u)
                        queue.insertOrUpdate(v, alt)
                    }

                    // add previous if distance is equal to known
                    alt == known -> {
                        prev[v]?.let { it += u }
                    }
                }
            }
        }

        // no matching solutions found
        return MultiSolutionSearchResult(emptySet(), dist, prev)
    }

}

//class DepthSearch<N, E>(
//    val startNode: N,
//    private val edgesOfNode: (N) -> Iterable<E>,
//    private val walkEdge: (N, E) -> N,
//) {
//    private val nodesVisited = mutableSetOf<N>(startNode)
//    private val nodesDiscoveredThrough = mutableMapOf<N, N>()
//
//    fun search(predicate: SolutionPredicate<N>): SearchResult<N> {
//        if (predicate(startNode))
//            return SearchResult(startNode, emptyMap(), nodesDiscoveredThrough)
//
//        val edges = edgesOfNode(startNode)
//        for (edge in edges) {
//            val nextNode = walkEdge(node, edge)
//            if (!nodesVisited.contains(nextNode)) {
//                nodesDiscoveredThrough[nextNode] = node
//                val found = searchFrom(nextNode, isSolution)
//                if (found != null)
//                    return found
//            }
//        }
//        return null
//    }
//}

interface EdgeGraph<N, E> {
    fun edgesOfNode(node: N): Iterable<E>
    fun walkEdge(node: N, edge: E): N
}

abstract class UninformedSearch<N, E>(val graph: EdgeGraph<N, E>) : EdgeGraph<N, E> by graph {

    data class Result<N, E>(val node: N, val prev: Map<N, Pair<N, E>>, val visited: Set<N>)

    fun search(start: N, destination: N) = search(start) { it == destination }
    open fun search(start: N, solutionPredicate: SolutionPredicate<N>): Result<N, E>? =
        traverse(start).firstOrNull { solutionPredicate(it.node) }

    abstract fun traverse(start: N): Sequence<Result<N, E>>

    class BFS<N, E>(graph: EdgeGraph<N, E>) : UninformedSearch<N, E>(graph) {
        override fun traverse(start: N): Sequence<Result<N, E>> = sequence {
            val nodesVisited = HashSet<N>()
            val nodesDiscoveredThrough = HashMap<N, Pair<N, E>>()
            val queue = ArrayDeque<N>()
            queue += start
            nodesVisited += start
            yield(Result(start, nodesDiscoveredThrough, nodesVisited))
            while (queue.isNotEmpty()) {
                val currentNode = queue.removeFirst()
                nodesVisited += currentNode
                edgesOfNode(currentNode).forEach { edge ->
                    val neighbor = walkEdge(currentNode, edge)
                    if (neighbor !in nodesVisited) {
                        nodesDiscoveredThrough[neighbor] = currentNode to edge
                        queue.addLast(neighbor)
                        yield(Result(neighbor, nodesDiscoveredThrough, nodesVisited))
                    }
                }
            }
        }
    }


}

fun <N, E> SearchEngineWithEdges<N, E>.bfsSequence(startNode: N): Sequence<N> = sequence {
    val nodesVisited = mutableSetOf<N>()
    val nodesDiscoveredThrough = mutableMapOf<N, N>()
    val queue = ArrayDeque<N>()
    queue += startNode
    yield(startNode)
    while (queue.isNotEmpty()) {
        val currentNode = queue.removeFirst()
        nodesVisited += currentNode
        edgesOfNode(currentNode).forEach { edge ->
            val neighbor = walkEdge(currentNode, edge)
            if (neighbor !in nodesVisited) {
                nodesDiscoveredThrough[neighbor] = currentNode
                queue.addLast(neighbor)
                yield(neighbor)
            }
        }
    }
}


open class SearchEngineWithEdges<N, E>(
    val edgesOfNode: (N) -> Iterable<E>,
    val walkEdge: (N, E) -> N,
) {

    var debugHandler: DebugHandler<N>? = null

    inner class BfsSearch(val startNode: N, val isSolution: SolutionPredicate<N>) {
        val solution: N?
        val nodesVisited = mutableSetOf<N>()
        val nodesDiscoveredThrough = mutableMapOf<N, N>()

        private tailrec fun searchLevel(nodesOnLevel: Set<N>, level: Int = 0): N? {
            if (debugHandler?.invoke(level, nodesOnLevel, nodesVisited) == SearchControl.STOP)
                return null
            val nodesOnNextLevel = mutableSetOf<N>()
            nodesOnLevel.forEach { currentNode ->
                nodesVisited.add(currentNode)
                edgesOfNode(currentNode).forEach { edge ->
                    val node = walkEdge(currentNode, edge)
                    if (node !in nodesVisited && node !in nodesOnLevel) {
                        nodesDiscoveredThrough[node] = currentNode
                        if (isSolution(node))
                            return node
                        else
                            nodesOnNextLevel.add(node)
                    }
                }
            }
            return if (nodesOnNextLevel.isEmpty())
                null
            else
                searchLevel(nodesOnNextLevel, level + 1)
        }

        private fun buildStack(node: N?): List<N> {
            //println("Building stack for solution node $node")
            val pathStack = ArrayDeque<N>()
            var nodeFoundThroughPrevious = node
            while (nodeFoundThroughPrevious != null) {
                pathStack.addFirst(nodeFoundThroughPrevious)
                nodeFoundThroughPrevious = nodesDiscoveredThrough[nodeFoundThroughPrevious]
            }
            return pathStack
        }

        init {
            solution = if (isSolution(startNode)) startNode else searchLevel(setOf(startNode))
        }

        fun path(): List<N> {
            return buildStack(solution)
        }

    }

    private inner class DepthSearch(val startNode: N, val isSolution: SolutionPredicate<N>) {

        private val nodesVisited = mutableSetOf<N>()
        private val nodesDiscoveredThrough = mutableMapOf<N, N>()

        private fun searchFrom(node: N, isSolution: SolutionPredicate<N>): N? {
            if (isSolution(node))
                return node
            nodesVisited.add(node)
            val edges = edgesOfNode(node)
            for (edge in edges) {
                val nextNode = walkEdge(node, edge)
                if (!nodesVisited.contains(nextNode)) {
                    nodesDiscoveredThrough[nextNode] = node
                    val found = searchFrom(nextNode, isSolution)
                    if (found != null)
                        return found
                }
            }
            return null
        }

        private fun buildStack(node: N?): Stack<N> {
            //println("Building stack for solution node $node")
            val pathStack = Stack<N>()
            var nodeFoundThroughPrevious = node
            while (nodeFoundThroughPrevious != null) {
                pathStack.add(0, nodeFoundThroughPrevious)
                nodeFoundThroughPrevious = nodesDiscoveredThrough[nodeFoundThroughPrevious]
            }
            return pathStack
        }

        fun search() = buildStack(searchFrom(startNode, isSolution))

        fun findBest(): Pair<Stack<N>, Set<N>> {
            return buildStack(searchFrom(startNode, isSolution)) to nodesVisited
        }

    }

    fun bfsSearch(startNode: N, isSolution: SolutionPredicate<N>) =
        BfsSearch(startNode, isSolution)

    fun depthFirstSearch(startNode: N, isSolution: SolutionPredicate<N>): Stack<N> {
        return DepthSearch(startNode, isSolution).search()
    }

    fun depthFirstSearchWithNodes(startNode: N, isSolution: SolutionPredicate<N>): Pair<Stack<N>, Set<N>> {
        return DepthSearch(startNode, isSolution).findBest()
    }

    fun completeAcyclicTraverse(startNode: N): Sequence<AcyclicTraverseLevel<N>> =
        sequence {
            var nodesOnPreviousLevel: Set<N>
            var nodesOnLevel = setOf<N>()
            var nodesOnNextLevel = setOf(startNode)
            var level = 0

            while (nodesOnNextLevel.isNotEmpty()) {
                nodesOnPreviousLevel = nodesOnLevel
                nodesOnLevel = nodesOnNextLevel
                yield(AcyclicTraverseLevel(level++, nodesOnLevel, nodesOnPreviousLevel))
                nodesOnNextLevel = mutableSetOf()
                nodesOnLevel.forEach { node ->
                    nodesOnNextLevel.addAll(
                        edgesOfNode(node).map { e -> walkEdge(node, e) }
                            .filter { neighbor ->
                                neighbor !in nodesOnLevel && neighbor !in nodesOnPreviousLevel
                            }
                    )
                }
            }
        }

}

data class AcyclicTraverseLevel<N>(val level: Int, val nodesOnLevel: Set<N>, val nodesOnPreviousLevel: Set<N>) :
    Collection<N> by nodesOnLevel

data class SearchLevel<N>(val level: Int, val nodesOnLevel: Collection<N>, val visited: Set<N>)

class SearchEngineWithNodes<N>(neighborNodes: (N) -> Collection<N>) :
    SearchEngineWithEdges<N, N>(neighborNodes, { _, edge -> edge })

fun <N, E> breadthFirstSearch(
    startNode: N,
    edgesOf: (N) -> Collection<E>,
    walkEdge: (N, E) -> N,
    isSolution: SolutionPredicate<N>,
) =
    SearchEngineWithEdges(edgesOf, walkEdge).bfsSearch(startNode, isSolution)

fun <N> breadthFirstSearch(
    startNode: N,
    neighborNodes: (N) -> Collection<N>,
    isSolution: SolutionPredicate<N>,
) =
    SearchEngineWithNodes(neighborNodes).bfsSearch(startNode, isSolution)

fun <N> depthFirstSearch(
    startNode: N,
    neighborNodes: (N) -> Collection<N>,
    isSolution: SolutionPredicate<N>,
): Stack<N> =
    SearchEngineWithNodes(neighborNodes).depthFirstSearch(startNode, isSolution)

fun <N> loggingDebugger(): DebugHandler<N> = { level: Int, nodesOnLevel: Collection<N>, nodesVisited: Collection<N> ->
    println("I am on level $level, searching through ${nodesOnLevel.size}. Visited so far: ${nodesVisited.size}")
    SearchControl.CONTINUE
}
