import kotlin.math.absoluteValue
import kotlin.random.Random

class Day01 : Day(1, 2024, "Historian Hysteria") {
    private val data = input.map { it.extractAllIntegers() }

    private val left = data.map { it[0] }
    private val right = data.map { it[1] }

    override fun part1(): Int =
        left.sorted().zip(right.sorted()).sumOf(::distance)

    override fun part2(): Int =
        left.sumOf { id -> right.count { it == id } * id }

    private fun distance(n: Pair<Int, Int>) = (n.first - n.second).absoluteValue
}


fun main() {
    solve<Day01> {
        """
            3   4
            4   3
            2   5
            1   3
            3   9
            3   3
        """.trimIndent() part1 11 part2 31
    }
}