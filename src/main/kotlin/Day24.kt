import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.Factory.mutNode
import utils.combinations
import utils.viz
import java.io.File

class Day24 : Day(24, 2024, "Crossed Wires") {

    val inputs = input.sections[0].map { it.substringBefore(':') to it.substringAfter(": ") }.toMap()
    val network = input.sections[1].map {
        it.split("""\s+""".toRegex()).let {
            it[4] to when (it[1]) {
                "AND" -> And(it[0], it[2], it[4])
                "OR" -> Or(it[0], it[2], it[4])
                "XOR" -> Xor(it[0], it[2], it[4])
                else -> error(it[1])
            }
        }
    }.toMap()

    override fun part1(): Any? {
        val nodes = network.values.flatMap { listOf(it.in1, it.in2, it.out) }.toSet()
        val state = mutableMapOf<String, Boolean>()
        inputs.forEach { (wire, s) ->
            if (s == "0") state[wire] = false
            else if (s == "1") state[wire] = true
            else error("$wire $state")
        }
        val waiting = network.toMutableMap()
        while (waiting.isNotEmpty()) {
            val process = waiting.firstNotNullOf { (_, gate) ->
                gate.takeIf { gate.in1 in state && gate.in2 in state }
            }
            waiting -= process.out
            state[process.out] = when (process) {
                is And -> state[process.in1]!! && state[process.in2]!!
                is Or -> state[process.in1]!! || state[process.in2]!!
                is Xor -> state[process.in1]!! xor state[process.in2]!!
                else -> error(process)
            }
        }
        return nodes.filter { it.startsWith('z') }.sortedDescending()
            .joinToString("") { if (state[it] == true) "1" else "0" }.toLong(2)
    }

    sealed interface Element
    data class Terminal(val name: String) : Element

    sealed interface Gate : Element {
        val in1: String
        val in2: String
        val in3: String
        val out: String
        val out2: String

        val ins get() = listOf(in1, in2, in3).filter { it.isNotEmpty() }.toSet()
        val outs get() = listOf(out, out2).filter { it.isNotEmpty() }.toSet()
    }

    abstract class BaseGate(override val in3: String = "", override val out2: String = "") : Gate

    data class And(override val in1: String, override val in2: String, override val out: String) : BaseGate()
    data class Or(override val in1: String, override val in2: String, override val out: String) : BaseGate()
    data class Xor(override val in1: String, override val in2: String, override val out: String) : BaseGate()

    data class HA(
        override val in1: String, override val in2: String,
        override val out: String, override val out2: String,
        val e: List<Gate>,
    ) : Gate {
        override val in3: String get() = ""
    }

    data class FA(
        override val in1: String, override val in2: String, override val in3: String,
        override val out: String, override val out2: String
    ) : Gate

    override fun part2(): Any? {
        val wireAlias = mutableMapOf<String, String>()

        fun reduceNetwork(network: Collection<Gate>): Set<Gate> {
            val newNetwork: MutableSet<Gate> = network.toMutableSet()

            val byInputs = network.groupBy { it.ins }
            val halfAdderElements = byInputs.filter { (_, gates) ->
                gates.size == 2 && gates.filterIsInstance<And>().size == 1 && gates.filterIsInstance<Xor>().size == 1
            }

            val halfAdders = halfAdderElements.map { (_, elements) ->
                val and = elements.single { it is And }
                val xor = elements.single { it is Xor }
                val ha = HA(elements[0].in1, elements[0].in2, xor.out, and.out, listOf(and, xor))
                newNetwork -= elements
                wireAlias[xor.out] = "Sum ${and.in1} ${and.in2}"
                wireAlias[and.out] = "Carry ${and.in1} ${and.in2}"
                newNetwork += ha
                ha
            }
            alog { "Detected ${halfAdders.size} half adders" }

            val fullAdders = halfAdders.combinations(2).flatMap { listOf(it, it.reversed()) }.mapNotNull { (ha1, ha2) ->
                val or = network.singleOrNull { it is Or && it.ins == setOf(ha1.out2, ha2.out2) }
                if (or != null && ha1.out in ha2.ins) {
                    val fa = FA(ha2.ins.single { it != ha1.out }, ha1.in1, ha1.in2, ha2.out, or.out)
                    newNetwork -= listOf(ha1, ha2, or)
                    newNetwork += fa
                    wireAlias[fa.out] = "Sum ${fa.in1} ${fa.in2} ${fa.in3}"
                    wireAlias[fa.out2] = "Carry ${fa.in1} ${fa.in2} ${fa.in3}"
                } else null
            }.count()
            alog { "detected $fullAdders full adders!" }
            return newNetwork
        }


//       val newNetwork = network.values.toSet()
        val newNetwork = reduceNetwork(network.values.toSet())

        val nodes = newNetwork.flatMap { it.ins + it.outs }

        val graph = graph("wires").setDirected(true)
        val wires = nodes.associateWith { wireName ->
            mutNode(wireAlias[wireName].orEmpty() + " (" + wireName + ")").also { graph.add(it) }
        }

        val terminals = nodes.filter { it[0] in "xyz" }.map { Terminal(it) }

        val g = utils.graph<Element>({ e ->
            when (e) {
                is Terminal -> newNetwork.filter { e.name in it.ins }
                is Gate -> e.outs.map { out ->
                    terminals.firstOrNull { it.name  == out } ?: newNetwork.first { out in it.ins }
                }
            }
        })

        println(g.viz(terminals.filter { it.name[0] in "xy" }))
        return ""


        var c = 0
        newNetwork.forEach { gate ->
            val gateNode = mutNode(gate::class.simpleName!! + " $c")
            c++
            gate.ins.forEach { wires[it]!!.addLink(gateNode) }
            gate.outs.forEach { gateNode.addLink(wires[it]!!) }
            if (gate is HA) {
                gateNode.setName(Label.lines(gate.e[0].toString(), gate.e[1].toString()))
            }
            graph.add(gateNode)
        }
        Graphviz.fromGraph(graph).render(Format.SVG).toFile(File("day24.svg"))
        return "z05 dkr htp z15 ggk rhv hhh z20".split(" ").sorted().joinToString(",")
    }

}

fun main() {
    solve<Day24> {
        """
            x00: 1
            x01: 1
            x02: 1
            y00: 0
            y01: 1
            y02: 0

            x00 AND y00 -> z00
            x01 XOR y01 -> z01
            x02 OR y02 -> z02
        """.trimIndent() part1 4

        """
            x00: 1
            x01: 0
            x02: 1
            x03: 1
            x04: 0
            y00: 1
            y01: 1
            y02: 1
            y03: 1
            y04: 1

            ntg XOR fgs -> mjb
            y02 OR x01 -> tnw
            kwq OR kpj -> z05
            x00 OR x03 -> fst
            tgd XOR rvg -> z01
            vdt OR tnw -> bfw
            bfw AND frj -> z10
            ffh OR nrd -> bqk
            y00 AND y03 -> djm
            y03 OR y00 -> psh
            bqk OR frj -> z08
            tnw OR fst -> frj
            gnj AND tgd -> z11
            bfw XOR mjb -> z00
            x03 OR x00 -> vdt
            gnj AND wpb -> z02
            x04 AND y00 -> kjc
            djm OR pbm -> qhw
            nrd AND vdt -> hwm
            kjc AND fst -> rvg
            y04 OR y02 -> fgs
            y01 AND x02 -> pbm
            ntg OR kjc -> kwq
            psh XOR fgs -> tgd
            qhw XOR tgd -> z09
            pbm OR djm -> kpj
            x03 XOR y03 -> ffh
            x00 XOR y04 -> ntg
            bfw OR bqk -> z06
            nrd XOR fgs -> wpb
            frj XOR qhw -> z04
            bqk OR frj -> z07
            y03 OR x01 -> nrd
            hwm AND bqk -> z03
            tgd XOR rvg -> z12
            tnw OR pbm -> gnj
        """.trimIndent() part1 2024
    }
}