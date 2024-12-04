import utils.*

class Day04 : Day(4, 2024) {

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
            (wordSearch[x] == 'A') &&
                    ((wordSearch[x + Direction8.NORTHWEST] == 'M' && wordSearch[x + Direction8.SOUTHEAST] == 'S') ||
                            (wordSearch[x + Direction8.NORTHWEST] == 'S' && wordSearch[x + Direction8.SOUTHEAST] == 'M')) && (
                    (wordSearch[x + Direction8.NORTHEAST] == 'M' && wordSearch[x + Direction8.SOUTHWEST] == 'S') ||
                            (wordSearch[x + Direction8.NORTHEAST] == 'S' && wordSearch[x + Direction8.SOUTHWEST] == 'M'))
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