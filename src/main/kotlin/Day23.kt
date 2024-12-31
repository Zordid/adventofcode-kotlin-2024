import utils.combinations

class Day23 : Day(23, 2024, "LAN Party") {

    val cRaw = input.map { it.split('-') }
    val members = cRaw.flatten().toSet()

    val connections = buildAdjacencyList(cRaw)

    override fun part1(): Any? {
        alog { "${members.size} members with ${cRaw.size} connections" }

//        val ring = findCliquesOfSize(connections, 3).filter {
//            it.any { it.startsWith('t') }
//        }.toSet()

        val ring = mutableSetOf<Set<String>>()
        members.forEach { f ->
            val connectedTo = connections[f]!!
            for (s in connectedTo) {
                if (s == f) continue
                val ts = connectedTo.filter {
                    it != f && it != s && f in connections[it]!! && s in connections[it]!!
                }
                ts.forEach { t ->
                    if (f[0] == 't' || s[0] == 't' || t[0] == 't') {
                        ring += setOf(f, s, t)
                    }
                }
            }
        }

        log { ring.forEach { println(it) } }
        return ring.size
    }

    override fun part2(): Any? {
        var largestSet = findMaximumClique(connections)
//        var largestSet = emptyList<String>()
//        for (choose in members.size downTo 1) {
//            alog { "Choose $choose... ${comb(members.size, choose)} parties..." }
//            val party = members.combinations(choose).firstOrNull { p ->
//                p.all { guest -> p.all { it == guest || guest in connections[it]!! } }
//            }
//            if (party != null) {
//                largestSet = party
//                break
//            }
//        }

        return largestSet.sorted().joinToString(",")
    }

    fun <S> findCliquesOfSize(adjacencyList: Map<S, Set<S>>, size: Int): List<Set<S>> {
        var cliques = mutableListOf<Set<S>>()

        fun bronKerbosch(r: Set<S>, p: Set<S>, x: MutableSet<S>) {
            if (r.size == size) {
                cliques += r.toSet()
                return
            }

            val pWork = p.toMutableSet() // A mutable copy of p to allow reduction
            for (v in p) {
                val neighbors = adjacencyList[v] ?: emptySet()
                bronKerbosch(
                    r + v,
                    pWork.intersect(neighbors),
                    x.intersect(neighbors).toMutableSet()
                )
                pWork.remove(v)
                x.add(v)
            }
        }

        val allNodes = adjacencyList.keys
        bronKerbosch(mutableSetOf(), allNodes.toMutableSet(), mutableSetOf())
//        bronKerboschPivoting(mutableSetOf(), allNodes.toMutableSet(), mutableSetOf())
        return cliques
    }


    fun <S> findMaximumClique(adjacencyList: Map<S, Set<S>>): Set<S> {
        var maxClique = setOf<S>()

        fun bronKerbosch(r: Set<S>, p: Set<S>, x: MutableSet<S>) {
            if (p.isEmpty() && x.isEmpty()) {
                if (r.size > maxClique.size) {
                    maxClique = r.toSet()
                }
                return
            }

            val pWork = p.toMutableSet() // A mutable copy of p to allow reduction
            for (v in p) {
                val neighbors = adjacencyList[v] ?: emptySet()
                bronKerbosch(
                    r + v,
                    pWork.intersect(neighbors),
                    x.intersect(neighbors).toMutableSet()
                )
                pWork.remove(v)
                x.add(v)
            }
        }

        fun bronKerboschPivoting(r: MutableSet<S>, p: MutableSet<S>, x: MutableSet<S>) {
            if (p.isEmpty() && x.isEmpty()) {
                if (r.size > maxClique.size) {
                    maxClique = r.toSet()
                }
                return
            }

            // Pivoting: Choose a pivot node u from P ⋃ X
            val unionPX = p.union(x)
            val u = unionPX.maxByOrNull { adjacencyList[it]?.size ?: 0 } ?: return // Pivot is node with max degree

            // Iterate over candidates in P that are NOT neighbors of the pivot u
            val pWithoutNeighborsU = p.minus(adjacencyList[u] ?: emptySet())
            for (v in pWithoutNeighborsU) {
                val neighbors = adjacencyList[v] ?: emptySet()
                bronKerboschPivoting(
                    r.plus(v).toMutableSet(),
                    p.intersect(neighbors).toMutableSet(),
                    x.intersect(neighbors).toMutableSet()
                )
                p.remove(v)
                x.add(v)
            }
        }

        val allNodes = adjacencyList.keys
//        bronKerbosch(mutableSetOf(), allNodes.toMutableSet(), mutableSetOf())
        bronKerboschPivoting(mutableSetOf(), allNodes.toMutableSet(), mutableSetOf())
        return maxClique
    }

    fun <S> buildAdjacencyList(connections: Iterable<Iterable<S>>): Map<S, Set<S>> {
        val adjacencyList = mutableMapOf<S, MutableSet<S>>()
        for (connected in connections) {
            for ((a, b) in connected.combinations(2)) {
                adjacencyList.getOrPut(a) { mutableSetOf() }.add(b)
                adjacencyList.getOrPut(b) { mutableSetOf() }.add(a)
            }
        }
        return adjacencyList
    }

//    // Example Usage
//    fun main() {
//        val connections = listOf(
//            1 to 2, 1 to 3, 1 to 4,
//            2 to 3, 2 to 4,
//            3 to 4,
//            5 to 6 // Separate clique
//        )
//        val maximumClique = findMaximumClique(connections)
//        println("Maximum clique: $maximumClique") // Output: Maximum clique: [1, 2, 3, 4]
//    }

}

fun main() {
    solve<Day23> {
        """
            kh-tc
            qp-kh
            de-cg
            ka-co
            yn-aq
            qp-ub
            cg-tb
            vc-aq
            tb-ka
            wh-tc
            yn-cg
            kh-ub
            ta-co
            de-co
            tc-td
            tb-wq
            wh-td
            ta-ka
            td-qp
            aq-cg
            wq-ub
            ub-vc
            de-ta
            wq-aq
            wq-vc
            wh-yn
            ka-de
            kh-ta
            co-tc
            wh-qp
            tb-vc
            td-yn
        """.trimIndent() part1 7 part2 "co,de,ka,ta"
    }
}