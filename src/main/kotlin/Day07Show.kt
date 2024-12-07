import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.Factory.node
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.MutableNode
import guru.nidi.graphviz.model.Node
import utils.permutations
import utils.pow
import java.io.File
import java.text.Normalizer.Form
import kotlin.math.floor
import kotlin.math.log10
import kotlin.system.exitProcess
import kotlin.time.measureTime

class Day07Show : Day(7, 2024, "Bridge Repair") {

    val t = Terminal()

    private val equations = input.map { it.extractAllLongs() }.also { it.checkForAnyOverflow() }

    override fun part1() =
        solveUsing(Add, Multiply)

    override fun part2() =
        solveUsing(Add, Multiply, Concat)

    sealed interface Operator : (Long, Long) -> Long {
        val symbol: String
    }
    data object Add : Operator {
        override val symbol: String
            get() = "+"

        override fun invoke(a: Long, b: Long) = a + b
    }

    data object Multiply : Operator {
        override val symbol: String
            get() = "*"

        override fun invoke(a: Long, b: Long) = a * b
    }

    data object Concat : Operator {
        override val symbol: String
            get() = "||"

        override fun invoke(a: Long, b: Long) = "$a$b".toLong()
    }

    private fun solveUsing(vararg operators: Operator) =
        equations.filter {
            val (testValue, firstOperand) = it
            val operands = it.drop(1)
            testEquation(testValue, 0L, operands, operators.asList())
        }.sumOf { it.first() }

    fun testEquation(
        testValue: Long,
        value: Long,
        operands: List<Long>,
        operators: List<Operator> = listOf(Add, Multiply, Concat),
        parentGraph: MutableGraph? = null,
        parentNode : MutableNode? = null
    ): Boolean {
        val origin = parentGraph == null
        val graph = parentGraph ?: graph("Test").setDirected(true)

        if (operands.isEmpty()) return (testValue == value).also {
            parentNode?.addLink(mutNode(if (it) "That's a match!" else "Nope, wrong!"))
        }
        if (value > testValue) return false.also{
            parentNode?.addLink(mutNode("Too big already!"))
        }

        val next = operands.first()
        val remainingOperands = operands.drop(1)

        val pn = parentNode ?: run {
            mutNode("$testValue: ${operands.joinToString(" ? ")}").also {
                graph.add(it)
            }
        }

        val result = operators.any { operation ->
            val node = mutNode("$testValue: $value ${operation.symbol} $next ${remainingOperands.joinToString(" ? ")}")
            pn.addLink(node)
            testEquation(testValue, operation(value, next), remainingOperands, operators, graph, node)
        }

        if (origin) {
            Graphviz.fromGraph(graph).width(400).render(Format.PNG).toFile(File("graph.png"))
        }

        return result
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
//32423085115: 519 6 52 3 70 5 9 4 3 9
    Day07Show().testEquation(32423085115, 0L, "6 52 3 70 5 9 4 3 9".extractAllLongs())
exitProcess(1)
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

