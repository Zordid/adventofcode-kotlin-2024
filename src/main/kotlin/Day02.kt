import kotlin.math.absoluteValue

class Day02 : Day(2, 2024, "Red-Nosed Reports") {

    private val reports = input.map { it.extractAllIntegers() }

    override fun part1() = reports.count { it.isSafe() }

    override fun part2() = reports.count { r ->
        r.isSafe() || r.indices.any { removeAt ->
            (r.take(removeAt) + r.drop(removeAt + 1)).isSafe()
        }
    }

    private fun List<Int>.isSafe(): Boolean = with(zipWithNext()) {
        val allIncreasing = all { (a, b) -> a < b }
        val allDecreasing = all { (a, b) -> a > b }
        val differenceOk = all { (a, b) -> (a - b).absoluteValue in 1..3 }
        (allIncreasing || allDecreasing) && differenceOk
    }

}

fun main() {
    solve<Day02> {
        """
            7 6 4 2 1
            1 2 7 8 9
            9 7 6 2 1
            1 3 2 4 5
            8 6 4 4 1
            1 3 6 7 9
        """.trimIndent() part1 2 part2 4
    }
}