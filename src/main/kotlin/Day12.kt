import utils.*

class Day12 : Day(12, 2024, "Garden Groups") {

    val g = input.grid

    override fun part1() = findRegions().sumOf { it.area * it.perimeter }

    override fun part2() = findRegions().sumOf { it.area * it.fences }

    fun findRegions(): Collection<Region> {
        val regions = mutableListOf<Region>()
        g.forArea { p ->
            if (regions.none { p in it.contains })
                regions += regionOf(p)
        }
        return regions
    }

    fun regionOf(p: Point): Region {
        val type = g[p]
        val q = dequeOf(p)
        val region = mutableSetOf<Point>()
        val border = mutableSetOf<Point>()
        while (q.isNotEmpty()) {
            val here = q.removeFirst()
            val neighborsIn = here.directNeighbors().filter { g.getOrNull(it) == type }

            region += here
            if (neighborsIn.size < 4) border += here

            q += neighborsIn.filter { it !in region && it !in q }
        }
        return Region(type, region, border)
    }

    data class Region(val type: Char, val contains: Set<Point>, val border: Set<Point>) {
        val area get() = contains.size
        val perimeter
            get() = border.sumOf { p -> p.directNeighbors().count { it !in contains } }
        val fences
            get() = Direction4.all.sumOf { fenceFacing ->
                val requireFence = border.filter { (it + fenceFacing) !in contains }.toMutableSet()

                var fencesNeeded = 0
                while (requireFence.isNotEmpty()) {
                    val p = requireFence.removeOne()
                    val sameFence = mutableSetOf(p)

                    do {
                        val growFence = requireFence.filter { it.directNeighbors().any { it in sameFence } }
                        sameFence += growFence
                        requireFence -= growFence
                    } while (growFence.isNotEmpty())

                    fencesNeeded++
                }
                fencesNeeded
            }
    }

}

fun <T> MutableSet<T>.removeOne(): T =
    first().also { remove(it) }

fun main() {
    solve<Day12> {
        """
            AAAA
            BBCD
            BBCC
            EEEC
        """.trimIndent() part1 140 part2 80

        """
            OOOOO
            OXOXO
            OOOOO
            OXOXO
            OOOOO
        """.trimIndent() part1 772

        """
            RRRRIICCFF
            RRRRIICCCF
            VVRRRCCFFF
            VVRCCCJFFF
            VVVVCJJCFE
            VVIVCCJJEE
            VVIIICJJEE
            MIIIIIJJEE
            MIIISIJEEE
            MMMISSJEEE
        """.trimIndent() part1 1930

        """
            AAAAAA
            AAABBA
            AAABBA
            ABBAAA
            ABBAAA
            AAAAAA
        """.trimIndent() part2 368
    }
}