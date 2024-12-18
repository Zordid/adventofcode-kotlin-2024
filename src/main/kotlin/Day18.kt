import utils.*

class Day18 : Day(18, 2024, "RAM Run") {

    val positions: List<Point> = input.map { it.extractAllIntegers().let { (x, y) -> x to y } }

    val dim = if (testInput) 6 else 70
    val end = dim to dim
    val area = origin to (end)

    override fun part1(): Any? {
        val fallen = mutableSetOf<Point>()
        positions.take(if (testInput) 12 else 1024).forEach { fallen += it }
        log {
            area.plot { if (it in fallen) "#" else "." }
        }

        val search = object : SearchDefinition<Point> {
            override fun neighborNodes(node: Point): Collection<Point> =
                node.directNeighbors(area).filter { it !in fallen }

            override fun cost(from: Point, to: Point) = 1
            override fun costEstimation(from: Point, to: Point) = from manhattanDistanceTo to
        }

        val r = AStarSearch(origin, search).search(end)

        log {
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

        var knownPath = emptySet<Point>()
        for (fall in positions) {
            fallen += fall
            if (knownPath.isEmpty() || fall in knownPath) {
                val r = AStarSearch<Point>(origin, search).search(end)
                if (!r.success) return with(fall) { "$x,$y" }
                knownPath = r.path.toSet()
            }
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