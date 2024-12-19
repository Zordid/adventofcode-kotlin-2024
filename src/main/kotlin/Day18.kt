import utils.*

class Day18 : Day(18, 2024, "RAM Run") {

    val positions: List<Point> = input.map { it.extractAllIntegers().let { (x, y) -> x to y } }

    val dim = if (testInput) 6 else 70
    val end = dim to dim
    val area = origin to (end)

    inner class RAM(val fallen: Set<Point>) : Graph<Point> {
        override fun neighborsOf(node: Point) =
            node.directNeighbors(area).filter { it !in fallen }

        override fun costEstimation(from: Point, to: Point) =
            from manhattanDistanceTo to
    }

    override fun part1(): Any? {
        val fallen = positions.take(if (testInput) 12 else 1024).toSet()
        log {
            area.plot { if (it in fallen) "#" else "." }
        }

        val r = RAM(fallen).aStarSearch(origin, end)

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

        var knownPath = emptySet<Point>()
        for (fall in positions) {
            fallen += fall
            if (knownPath.isEmpty() || fall in knownPath) {
                val r = RAM(fallen).aStarSearch(origin, end)
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