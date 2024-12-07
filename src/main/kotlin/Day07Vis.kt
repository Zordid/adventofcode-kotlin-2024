import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableNode
import java.io.File

fun testEquation(
    testValue: Long,
    value: Long,
    operands: List<Long>,
    operators: List<Day07.Operator> = listOf(Day07.Add, Day07.Multiply, Day07.Concat),
    parentNode: MutableNode? = null
): Boolean {
    if (operands.isEmpty()) return (testValue == value).also {
        parentNode?.attrs()
        parentNode?.addLink(mutNode(if (it) "That's a match!" else "Nope, $value is too ${if (value > testValue) "big" else "small"}!"))
    }
    if (value > testValue) return false.also {
        parentNode?.addLink(mutNode("$value is too big already!"))
    }

    val next = operands.first()
    val remainingOperands = operands.drop(1)

    val result = operators.any { operation ->
        val node =
            mutNode("$testValue: $value ${operation.symbol} $next ? ${remainingOperands.joinToString(" ? ")}")
        parentNode?.addLink(node)
        testEquation(testValue, operation(value, next), remainingOperands, operators, node)
    }
    return result
}

fun main() {
    val examples = """
        190: 10 19
        3267: 81 40 27
        83: 17 5
        156: 15 6
        7290: 6 8 6 15
        161011: 16 10 13
        192: 17 8 14
        21037: 9 7 18 13
        292: 11 6 16 20
    """.trimIndent().split("\n").filter { it.isNotEmpty() }

    examples.forEachIndexed { index, demo ->
        val graph = graph("Test").setDirected(true)
        demo.extractAllLongs().let {
            val testValue = it.first()
            val operands = it.drop(1)
            val rootNode = mutNode("$testValue: ${operands.joinToString(" ? ")}")
            graph.add(rootNode)
            testEquation(testValue, operands.first(), operands.drop(1), parentNode = rootNode)
        }
        Graphviz.fromGraph(graph).render(Format.PNG).toFile(File("day7-example$index.png"))
    }
}

val Day07.Operator.symbol
    get() = when (this) {
        is Day07.Add -> "+"
        is Day07.Multiply -> "*"
        is Day07.Concat -> "||"
    }
