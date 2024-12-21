import utils.*

class Day21 : Day(21, 2024, "Keypad Conundrum") {

    val codes = input.lines

    override fun part1(): Any? {
        return codes.sumOf { codeAndSolution ->
            val code = codeAndSolution.substringBefore(':')
            val sol = codeAndSolution.substringAfter(':').trim()

//            val t = robotPad.type(robotPad.type(keyPad.type(code)))
            //   val t = robotPad.typeM(robotPad.typeM(keyPad.typeM(listOf(code)))).minBy { it.length }

            //val t = optimize(code)
            val t = "A$code".zipWithNext().sumOf { (on, press) ->
                shortestWayToPress(3, on, press, 0)
            }
            //val t = shortestWayToPress("AA", '0', 1)

            val codeNum = code.extractFirstInt()
            log { "$code: $codeNum * ${t} | $t" }
            if (sol.isNotEmpty()) {
                if (sol.length != t.toInt())
                    log { "$code: $codeNum * ${sol.length} | $sol" }
            }
            t.toLong() * codeNum
        }
    }

    override fun part2(): Any? {
        cache.clear()
        return codes.sumOf { code ->
            val t = "A$code".zipWithNext().sumOf { (on, press) ->
                shortestWayToPress(3+23, on, press, 0)
            }

            val codeNum = code.extractFirstInt()
            alog { "$code: $codeNum * ${t} | $t" }
            t.toLong() * codeNum
        }
    }

    val cache = mutableMapOf<Triple<Char, Char, Int>, Long>()

    fun shortestWayToPress(maxLevels: Int, fingerOn: Char, digit: Char, level: Int): Long =
        cache.getOrPut(Triple(fingerOn, digit, level)) {
            log { "What is the shortest way to press $digit on level $level when $fingerOn" }
            if (level == maxLevels) return@getOrPut 1

            val controlling = if (level == 0) keyPad else robotPad

            val possible = controlling.shortestPaths["$fingerOn$digit"] ?: error("No way on $level: $fingerOn$digit")

            possible.minOf { combination ->
                "A${combination}A".windowed(2).sumOf {
                    shortestWayToPress(maxLevels, it[0], it[1], level + 1)
                }
            }
        }

    fun optimize(code: String, levels: Int = 1): String {
        val pad = List(levels) { if (it == 0) keyPad else robotPad }
        val start = State(0, 'A', List(levels) { 'A' })
        var c = start
        code.forEach { press ->
            c.next += State(0, press, List(levels) { if (it == 0) press else 'A' }).also { c = it }
        }

        fun increaseLevel(level: Int, node: State): List<State> {
            if (node.level != level) {
                return node.next
            }

            TODO()
        }

        println(start)
        TODO()
    }

    data class State(
        val level: Int,
        val code: Char,
        val fingers: List<Char>,
        var next: List<State> = mutableListOf(),
    )


    val keypadLayout = """
        789
        456
        123
        #0A
    """.trimIndent().lines().toGrid()

    val keyPad = KeyPad("KEYPAD", keypadLayout)
    val robotPadLayout = """
        #^A
        <v>
    """.trimIndent().lines().toGrid()

    val robotPad = KeyPad("REMOTE", robotPadLayout)

    class Edge(
        val type: Char,
    )

    class KeyPad(val name: String, val layout: Grid<Char>) {
        val A = layout['A']
        val shortestPaths: Map<String, List<String>>

        val isRemote = name == "REMOTE"

        override fun toString(): String {
            return name
        }

        fun typeM(combinations: List<String>): List<String> {
            return combinations.flatMap { combination ->
                var poss = listOf<StringBuilder>(StringBuilder())

                "A$combination".windowed(2).forEach { move ->
                    val x = shortestPaths[move]!!
                    if (x.size == 1) poss.forEach { it.append(x[0]).append('A') }
                    else if (x.size > 1) poss = poss.flatMap { s ->
                        x.map { StringBuilder("$s${it}A") }
                    } else error("BBOM")
                }
                poss.map { it.toString() }
            }
        }

        fun type(combination: String): String {
            return "A$combination".windowed(2).joinToString("") { move ->
                val possibleWays = shortestPaths[move]!!

                val optimum = if (isRemote)
                    possibleWays.minBy { it.zipWithNext().count { it.first != it.second } }
                else possibleWays.maxBy { it.zipWithNext().count { it.first == it.second } }
                optimum + "A"
            }
        }

        init {
            val contents = layout.frequencies().keys - setOf('#')
            shortestPaths = buildMap {
                for (start in contents) {
                    val g = graph<Point>({
                        it.directNeighbors(layout.area).filter { layout[it] != '#' }
                    })
                    for (dest in contents) {
                        val shortest = g.dijkstraSearchAll(layout[start], layout[dest]).paths.map { path ->
                            val directions = mutableListOf<Direction4>()
                            val remaining = path.toMutableList()
                            var where = remaining.removeFirst()
                            while (remaining.isNotEmpty()) {
                                val next = remaining.removeFirst()
                                directions += Direction4.ofVector(next - where)!!
                                where = next
                            }
                            directions.joinToString("") {
                                when (it) {
                                    Direction4.NORTH -> "^"
                                    Direction4.SOUTH -> "v"
                                    Direction4.WEST -> "<"
                                    Direction4.EAST -> ">"
                                }
                            }
                        }
                        put("$start$dest", shortest)
//                        put("$start$dest", shortest.maxBy {
//                            it.windowed(2).count { it[0]==it[1] }
//                        })
                    }
                }
            }
            println(shortestPaths)
        }
    }



}

fun main() {
    solve<Day21> {
        """
            029A: <vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
            980A: <v<A>>^AAAvA^A<vA<AA>>^AvAA<^A>A<v<A>A>^AAAvA<^A>A<vA>^A<A>A
            179A: <v<A>>^A<vA<A>>^AAvAA<^A>A<v<A>>^AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
            456A: <v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A
            379A: <v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
        """.trimIndent() part1 126384
    }
}