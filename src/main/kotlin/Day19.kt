class Day19 : Day(19, 2024, "Linen Layout") {

    val patterns = input.sections[0].string.split(", ")
    val desired = input.sections[1].lines.sortedBy { it.length }

    override fun part1() = desired.count { possible(it) }
    override fun part2() = desired.sumOf { ways(it).toLong() }

    val possibleCache = mutableMapOf<String, Boolean>()

    fun possible(desired: String): Boolean = possibleCache.getOrPut(desired) {
        if (desired.isEmpty()) return@getOrPut true
        for (t in patterns) {
            if (desired.startsWith(t) && possible(desired.drop(t.length))) return@getOrPut true
        }
        false
    }

    val waysCache = mutableMapOf<String, Long>()

    fun ways(desired: String): Long = waysCache.getOrPut(desired) {
        if (desired.isEmpty()) return@getOrPut 1L
        patterns.sumOf { t ->
            if (desired.startsWith(t)) ways(desired.drop(t.length)) else 0L
        }
    }

}

fun main() {
    solve<Day19> {
        """
            r, wr, b, g, bwu, rb, gb, br

            brwrr
            bggr
            gbbr
            rrbgbr
            ubwu
            bwurrg
            brgr
            bbrgwb
        """.trimIndent() part1 6 part2 16
    }
}
