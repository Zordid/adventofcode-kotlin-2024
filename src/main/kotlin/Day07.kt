import com.github.ajalt.mordant.rendering.TextColors
import utils.permutations
import utils.pow
import kotlin.math.floor
import kotlin.math.log10
import kotlin.time.measureTime

class Day07 : Day(7, 2024, "Bridge Repair") {

    private val equations = input.map { it.extractAllLongs() }.also { it.checkForAnyOverflow() }

    override fun part1() =
        solveUsing(Add, Multiply)

    override fun part2() =
        solveUsing(Add, Multiply, Concat)

    sealed interface Operator : (Long, Long) -> Long
    data object Add : Operator {
        override fun invoke(a: Long, b: Long) = a + b
    }

    data object Multiply : Operator {
        override fun invoke(a: Long, b: Long) = a * b
    }

    data object Concat : Operator {
        override fun invoke(a: Long, b: Long) = "$a$b".toLong()
    }

    private fun solveUsing(vararg operators: Operator) =
        equations.filter {
            val (testValue, firstOperand) = it
            val remainingOperands = it.drop(2)
            testEquation(testValue, firstOperand, remainingOperands, operators.asList())
        }.sumOf { it.first() }

    private fun testEquation(
        testValue: Long,
        value: Long,
        operands: List<Long>,
        operators: List<Operator>,
    ): Boolean {
        if (operands.isEmpty()) return testValue == value
        if (value > testValue) return false

        val next = operands.first()
        val remainingOperands = operands.drop(1)

        return operators.any { operation ->
            testEquation(testValue, operation(value, next), remainingOperands, operators)
        }
    }

    private fun profileOrder() {
        listOf(Add, Multiply, Concat).permutations().map { it.toTypedArray() }.forEach { orderedOps ->
            print(TextColors.green("Testing ${orderedOps.asList()}: "))
            val repetitions = 100
            val time = measureTime {
                repeat(repetitions) {
                    solveUsing(*orderedOps)
                }
            } / repetitions
            println("took $time")
        }
    }

    private fun List<List<Long>>.checkForAnyOverflow() {
        forEach {
            it.drop(1).let { sample ->
                sample.reduce { acc, next ->
                    listOf(Add, Multiply, Concat).maxOf { it(acc, next) }.also { result ->
                        require(result >= acc)
                    }
                }
            }
        }
    }

    // endregion

}

fun main() {
    solve<Day07> {
        """
            190: 10 19
            3267: 81 40 27
            83: 17 5
            156: 15 6
            7290: 6 8 6 15
            161011: 16 10 13
            192: 17 8 14
            21037: 9 7 18 13
            292: 11 6 16 20
        """.trimIndent() part1 3749 part2 11387
    }
}

