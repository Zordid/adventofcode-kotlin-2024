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
        val outer = mutableSetOf<Point>()
        while (q.isNotEmpty()) {
            val here = q.removeFirst()

            val (neighborsIn, neighborsOut) = here.directNeighbors().partition { g.getOrNull(it) == type }
            region += here
            if (neighborsOut.isNotEmpty()) outer += here

            q += neighborsIn.filter { it !in region && it !in q }
        }
        return Region(type, region, outer)
    }

    data class Region(val type: Char, val contains: Set<Point>, val border: Set<Point>) {

        fun price(g: Grid<Char>): Int {
            val area = contains.size
            val perimeter = border.sumOf { p -> p.directNeighbors().count { g.getOrNull(it) != type } }
            log { "A region of $type with area $area and peri $perimeter: ${area * perimeter}" }
            return area * perimeter
        }
        fun priceBulk(g: Grid<Char>): Int {
            val area = contains.size
            val fences = Direction4.all.sumOf { dir ->
                var need = 0
                val allThatNeedFencing = border.filter { g.getOrNull(it + dir) != type }.toMutableSet()
                while (allThatNeedFencing.isNotEmpty()) {
                    val p = allThatNeedFencing.first().also { allThatNeedFencing -= it }
                    val inLine = mutableSetOf(p)

                    var include = allThatNeedFencing.firstOrNull { it.directNeighbors().any { it in inLine } }
                    while (include != null) {
                        inLine += include
                        allThatNeedFencing -= include
                        include = allThatNeedFencing.firstOrNull { it.directNeighbors().any { it in inLine } }
                    }

                    need += 1
                }
                need
            }

            log { "A region of $type with area $area and fence $fences: ${area * fences}" }
            return area * fences
        }
    }

}

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