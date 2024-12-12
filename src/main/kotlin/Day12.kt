import utils.*

class Day12 : Day(12, 2024, "Garden Groups") {

    val g = input.grid

    override fun part1() = findRegions().sumOf { it.price(g) }

    override fun part2() = findRegions().sumOf { it.priceBulk(g) }

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

        fun price(g: Grid<Char>): Int {
            val perimeter = border.sumOf { p -> p.directNeighbors().count { g.getOrNull(it) != type } }
            log { "A region of $type with area $area and peri $perimeter: ${area * perimeter}" }
            return area * perimeter
        }

        fun priceBulk(g: Grid<Char>): Int {
            val fences = Direction4.all.sumOf { fenceFacing ->
                val requireFence = border.filter { g.getOrNull(it + fenceFacing) != type }.toMutableSet()

                var fencesNeeded = 0
                while (requireFence.isNotEmpty()) {
                    val p = requireFence.removeOne()
                    val sameFence = mutableSetOf(p)

                    do {
                        val growFence = requireFence.filter { it.directNeighbors().any { it in sameFence } }
                        sameFence += growFence
                        requireFence -= growFence
                    } while (growFence.isNotEmpty())

                    fencesNeeded += 1
                }
                fencesNeeded
            }

            log { "A region of $type with area $area and fence $fences: ${area * fences}" }
            return area * fences
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