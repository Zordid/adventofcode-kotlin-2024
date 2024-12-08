import arrow.core.fold
import utils.*

class Day08 : Day(8, 2024, "Resonant Collinearity") {

    val p = input.grid
    val antennas = p.allPointsAndValues().mapNotNull { (pos, antenna) ->
        if (antenna != '.') antenna to pos
        else null
    }.groupBy({ it.first }, { it.second })

    override fun part1() =
        antennas.fold(emptySet<Point>()) { antis, (type, pos) ->
            antis + pos.combinations(2).flatMap { (n, m) ->
                val diff = n - m
                listOf(m - diff, n + diff).filter { it in p }
            }
        }.size


    override fun part2(): Any? {
        val antis = mutableSetOf<Point>()
        antennas.forEach { (antenna, positions) ->
            positions.combinations(2).forEach { (n, m) ->
                val diff = n - m
                var anti = n
                while (anti in p) {
                    antis += anti
                    anti += diff
                }
                anti = n
                while (anti in p) {
                    antis += anti
                    anti -= diff
                }
            }
        }
        return antis.size
    }

}

fun main() {
    solve<Day08> {
        """
            ............
            ........0...
            .....0......
            .......0....
            ....0.......
            ......A.....
            ............
            ............
            ........A...
            .........A..
            ............
            ............
        """.trimIndent() part1 14 part2 34
    }
}