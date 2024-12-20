@file:Suppress("unused")

package utils

/**
 * A general interface to describe graphs with nodes described by neighborhood.
 */
interface Graph<N> {
    fun neighborsOf(node: N): Collection<N>

    fun cost(from: N, to: N): Int = 1
    fun costEstimation(from: N, to: N): Int = throw NotImplementedError("needs a costEstimation fun")
}

fun <N> graph(
    neighborsOf: (N) -> Collection<N>,
    cost: (N, N) -> Int = { _, _ -> 1 },
    costEstimation: (N, N) -> Int = { _, _ -> throw NotImplementedError("define costEstimation") },
): Graph<N> =
    object : Graph<N> {
        override fun neighborsOf(node: N) = neighborsOf(node)
        override fun cost(from: N, to: N) = cost(from, to)
        override fun costEstimation(from: N, to: N) = costEstimation(from, to)
    }

fun <N> Graph<N>.depthFirstSearch(start: N, destinationPredicate: (N) -> Boolean): ArrayDeque<N> =
    depthFirstSearch(start, ::neighborsOf, destinationPredicate)

fun <N> Graph<N>.depthFirstSearch(start: N, destination: N): ArrayDeque<N> =
    depthFirstSearch(start, ::neighborsOf) { it == destination }

fun <N> Graph<N>.completeAcyclicTraverse(start: N) =
    SearchEngineWithNodes(::neighborsOf).completeAcyclicTraverse(start)

fun <N> Graph<N>.breadthFirstSearch(start: N, predicate: SolutionPredicate<N>) =
    SearchEngineWithNodes(::neighborsOf).bfsSearch(start, predicate)

fun <N> Graph<N>.aStarSearch(start: N, destination: N) =
    AStarSearch(start, neighborsOf = ::neighborsOf, cost = ::cost, costEstimation = ::costEstimation).search(destination)

fun <N> Graph<N>.dijkstraSearch(start: N, destination: N?) =
    Dijkstra(start, ::neighborsOf, ::cost).search(destination)

fun <N> Graph<N>.dijkstraSearch(start: N, destinationPredicate: (N) -> Boolean) =
    Dijkstra(start, ::neighborsOf, ::cost).search(destinationPredicate)

fun <N> Graph<N>.dijkstraSearchAll(start: N, destination: N?) =
    Dijkstra(start, ::neighborsOf, ::cost).searchAll(destination)

fun <N> Graph<N>.dijkstraSearchAll(start: N, destinationPredicate: (N) -> Boolean) =
    Dijkstra(start, ::neighborsOf, ::cost).searchAll(destinationPredicate)
