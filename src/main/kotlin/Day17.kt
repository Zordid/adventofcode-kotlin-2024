import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold

class Day17 : Day(17, 2024, "Chronospatial Computer") {

    val registers = input.sections[0].longs
    val program = input.sections[1].integers

    data class State(
        var a: Long,
        var b: Long,
        var c: Long,
        val program: List<Int>,
        var ip: Int = 0,
    ) {
        val output = mutableListOf<Int>()

        fun reset(a: Long, b: Long, c: Long) {
            this.a = a
            this.b = b
            this.c = c
            ip = 0
            output.clear()
        }

        fun combo(value: Int?): Long = when (value) {
            null -> error("no operand")
            in 0..3 -> value.toLong()
            4 -> a
            5 -> b
            6 -> c
            else -> error("combo $value")
        }

        fun run() {
            while (ip in program.indices) step()
        }

        fun step() {
            val instruction = program[ip]
            val operand = program.getOrNull(ip + 1)
            ip += 2
            when (instruction) {
                // adv
                0 -> a = a / (1 shl combo(operand).toInt())
                // bxl
                1 -> b = b.xor(operand!!.toLong())
                // bst
                2 -> b = combo(operand) % 8
                // jnz
                3 -> if (a != 0L) ip = operand!!
                // bxc
                4 -> b = b.xor(c)
                // out
                5 -> output += (combo(operand) % 8).toInt()
                // bdv
                6 -> b = a / (1 shl combo(operand).toInt())
                // cdv
                7 -> c = a / (1 shl combo(operand).toInt())
            }
        }

        fun debug() {
            log { this@State }
        }
    }

    override fun part1(): String {
        var (a, b, c) = registers
        val state = State(a, b, c, program)

        state.debug()
        while (state.ip in program.indices) {
            state.step()
            state.debug()
        }

        return state.output.joinToString(",")
    }

    /**
     * All programs work like this with variations only in the xor codes or order of instructions
     * bst 4    b = a % 8
     * bxl 5    b = b xor 5
     * cdv 5    c = a / 2^b
     * bxl 6    b = b xor 6
     * adv 3    a = a / 8
     * bxc 0    b = b xor c
     * out 5    out b
     * jnz 0    if a!=0 jmp 0
     */
    override fun part2(): Long {
        val (a, b, c) = registers
        val state = State(a, b, c, program)

        alog {
            "New try, working from front to back\n" +
                    "Idea: the program will output one 3-bit digit per 3-bits in register a\n" +
                    "so, hack the lock from beginning to end, trying out all\n" +
                    "<0..7>0000000000, then holding on the one that makes the output fit at the\n" +
                    "last position. Repeat this for every 3-bit digit from left to right\n"
        }

        fun hack(fixed: String): String? {
            if (fixed.length == program.size) return fixed.also {
                alog { "Cracked with ${green(fixed)}!" }
            }
            val hackIndex = fixed.length
            alog { "Hacking at position ${hackIndex + 1}" }
            for (offer in 0..7) {
                if (offer == 0 && fixed.isEmpty()) continue

                val aAsString = "$fixed$offer".padEnd(program.size, '0')
                val a = aAsString.toLong(8)
                state.reset(a, b, c)
                state.run()
                val output = state.output.joinToString("")
                require(output.length == program.size) { "output is too short" }
                val itsAMatch = state.output[program.lastIndex - hackIndex] == program[program.lastIndex - hackIndex]

                alog {
                    val n =
                        green(fixed) + (if (itsAMatch) (green + bold)("$offer") else (red + bold)("$offer")) + gray(
                            "0".repeat(program.size - 1 - fixed.length)
                        )
                    val ignored = program.size - 1 - hackIndex
                    val o = gray(output.take(ignored)) +
                            output.withIndex().drop(ignored)
                                .joinToString("") { (idx, c) ->
                                    if (c.digitToInt() == program[idx]) green("$c")
                                    else red("$c")
                                }
                    "$n -> $o"
                }
                if (itsAMatch) {
                    val solution = hack("$fixed$offer")
                    if (solution != null) return solution
                }
            }
            alog { "No suitable digit for position ${hackIndex + 1} found, backing up..." }
            return null
        }

        return hack("")?.toLong(8) ?: 0
    }

}

fun main() {
    solve<Day17> {
        """
            Register A: 729
            Register B: 0
            Register C: 0

            Program: 0,1,5,4,3,0
        """.trimIndent() part1 "4,6,3,5,6,3,5,2,1,0"

//        """
//            Register A: 2024
//            Register B: 0
//            Register C: 0
//
//            Program: 0,3,5,4,3,0
//        """.trimIndent() part2 117440
    }
}