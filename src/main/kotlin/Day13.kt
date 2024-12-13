import utils.Point
import utils.x
import utils.y
import kotlin.math.roundToLong

class Day13 : Day(13, 2024, "Claw Contraption") {

    private val games = input.sections.map {
        val (a, b, p) = it.map { it.extractAllIntegers() }
        Game(a[0] to a[1], b[0] to b[1], p[0].toLong() to p[1].toLong()).show()
    }

    override fun part1() = games.sumOf { it.minimumTokens() }

    override fun part2() =
        games.map { it.copy(price = it.price.first + 10000000000000 to it.price.second + 10000000000000) }
            .sumOf { it.minimumTokens() }

    data class Game(val a: Point, val b: Point, val price: Pair<Long, Long>)

    private fun Game.minimumTokens(): Long {
        // Using Cramer's rule to solve linear equation
        // https://en.wikipedia.org/wiki/Cramer%27s_rule
        val ax = a.x.toDouble()
        val ay = a.y.toDouble()
        val bx = b.x.toDouble()
        val by = b.y.toDouble()
        val px = price.first.toDouble()
        val py = price.second.toDouble()

        // Calculate the determinants
        val detA = ax * by - ay * bx
        val detAa = px * by - py * bx
        val detAb = ax * py - ay * px

        // Check if the system has a unique solution
        if (detA == 0.0) return 0

        // Calculate a and b using Cramer's rule and convert to long
        val pushA = (detAa / detA).roundToLong()
        val pushB = (detAb / detA).roundToLong()

        val isSolution = pushA * a.x + pushB * b.x == price.first && pushA * a.y + pushB * b.y == price.second
        return if (isSolution) pushA * 3 + pushB
        else 0
    }

//    fun Game.minimumTokensMultiK(): Long {
//        // Convert Long values to Double for numerical stability
//        val ax = a.x.toDouble()
//        val ay = a.y.toDouble()
//        val bx = b.x.toDouble()
//        val by = b.y.toDouble()
//        val px = price.first.toDouble()
//        val py = price.second.toDouble()
//
//        // Create the coefficient matrix A and the dependent variable vector bb
//        // A*x = b
//        val A = mk.ndarray(mk[mk[ax, bx], mk[ay, by]])
//        val bb = mk.ndarray(mk[px, py])
//
//        // Solve the system of linear equations
//        val solution = mk.linalg.solve(A, bb)
//
//        // Extract the values of a and b
//        val pushA = solution[0].roundToLong()
//        val pushB = solution[1].roundToLong()
//
//        val isSolution = pushA * a.x + pushB * b.x == price.first && pushA * a.y + pushB * b.y == price.second
//        return if (isSolution) pushA * 3 + pushB
//        else 0
//    }

}

fun main() {
    solve<Day13> {
        """
            Button A: X+94, Y+34
            Button B: X+22, Y+67
            Prize: X=8400, Y=5400

            Button A: X+26, Y+66
            Button B: X+67, Y+21
            Prize: X=12748, Y=12176

            Button A: X+17, Y+86
            Button B: X+84, Y+37
            Prize: X=7870, Y=6450

            Button A: X+69, Y+23
            Button B: X+27, Y+71
            Prize: X=18641, Y=10279
        """.trimIndent() part1 480
    }
}
