import utils.*
import utils.Direction8.*

class Day04 : Day(4, 2024, "Ceres Search") {

    private val wordSearch = input.grid

    override fun part1(): Int {
        val candidates = wordSearch.area.allPoints()
            .flatMap { start -> Direction8.map { start to it } }

        return candidates.count { (start, direction) ->
            "XMAS".withIndex().all { (index, c) ->
                wordSearch.getOrDefault(start + (direction * index), '.') == c
            }
        }
    }

    override fun part2() =
        wordSearch.area.shrink(1).allPoints().count { x ->
            (wordSearch[x] == 'A') && run {
                val criss = setOf(wordSearch[x + NORTHWEST], wordSearch[x + SOUTHEAST])
                val cross = setOf(wordSearch[x + NORTHEAST], wordSearch[x + SOUTHWEST])
                criss == MS && cross == MS
            }
        }

    companion object {
        private val MS = setOf('M', 'S')
    }
}

fun main() {
    solve<Day04> {
        """
            MMMSXXMASM
            MSAMXMSMSA
            AMXSXMAAMM
            MSAMASMSMX
            XMASAMXAMM
            XXAMMXXAMA
            SMSMSASXSS
            SAXAMASAAA
            MAMMMXMMMM
            MXMXAXMASX
        """.trimIndent() part1 18 part2 9
    }
}