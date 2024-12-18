import utils.*

class Day18 : Day(18, 2024, "RAM Run") {

    val p: List<Point> = input.map { it.extractAllIntegers().let { (x, y) -> x to y } }

    val dim = if (testInput) 6 else 70
    val end = dim to dim
    val area = origin to (end)

    override fun part1(): Any? {
        val fallen = mutableSetOf<Point>()
        p.take(if (testInput) 12 else 1024).forEach { fallen += it }

        alog {
            area.plot { if (it in fallen) "#" else "." }
        }

        val d = Dijkstra(0 to 0, { p ->
            p.directNeighbors(area).filter { it !in fallen }
        }, { p1, p2 -> 1 })
        val r = d.search { p -> p == end }

        alog {
            area.plot {
                when (it) {
                    in r.path -> "O"
                    in fallen -> "#"
                    else -> "."
                }
            }
        }

        return r.steps
    }

    override fun part2(): Any? {
        val fallen = mutableSetOf<Point>()
        val search = object : SearchDefinition<Point> {
            override fun neighborNodes(node: Point): Collection<Point> =
                node.directNeighbors(area).filter { it !in fallen }

            override fun cost(from: Point, to: Point) = 1
            override fun costEstimation(from: Point, to: Point) = from manhattanDistanceTo to
        }

        for (fall in p) {
            if (!fallen.add(fall)) continue
            val r = AStarSearch<Point>(origin, search).search(end)
            if (!r.success) return "${fall.x},${fall.y}"
        }

        return null
    }

}

fun main() {
    solve<Day18> {
        """
            5,4
            4,2
            4,5
            3,0
            2,1
            6,3
            2,4
            1,5
            0,6
            3,3
            2,6
            5,1
            1,2
            5,5
            2,5
            6,5
            1,4
            0,4
            6,4
            1,1
            6,1
            1,0
            0,5
            1,6
            2,0
        """.trimIndent() part1 22 part2 "6,1"
    }
}