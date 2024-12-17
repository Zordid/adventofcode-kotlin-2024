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

        fun step() {
            val instruction = program[ip]
            val operand = program.getOrNull(ip + 1)
            ip += 2
            when (instruction) {
                0 -> { // adv
                    a = a / (1 shl combo(operand).toInt())
                }

                1 -> { // bxl
                    b = b.xor(operand!!.toLong())
                }

                2 -> { // bst
                    b = combo(operand) % 8
                }

                3 -> if (a != 0L) { // jnz
                    ip = operand!!
                }

                4 -> { // bxc
                    b = b.xor(c)
                }

                5 -> { // out
                    val v = (combo(operand) % 8).toInt()
                    output += v
                    log { "Output $v" }
                }

                6 -> { // bdv
                    b = a / (1 shl combo(operand).toInt())
                }

                7 -> { // cdv
                    c = a / (1 shl combo(operand).toInt())
                }
            }
        }

        fun debug() {
            log { this@State }
        }
    }

    override fun part1(): Any? {
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
     *
     * bst 4    b = a % 8
     * bxl 5    b = b xor 101
     * cdv 5    c = a / 2^b
     * bxl 6    b = b xor 110
     * bxc 0    b = b xor c
     * out 5    out b
     * adv 3    a = a / 8
     * jnz 0    if a!=0 jmp 0
     *
     */
    fun decrypt(a: Int, originalB: Int) {
        var b = originalB // 3 bit
        val c = a / 1 shl b
    }

    override fun part2(): Any? {
        val (a, b, c) = registers
        val state = State(a, b, c, program)

        var maxMatching = 0
        val s: String? = "65110264632" // "5110264632"
        var fixedDigits = s?.length ?: 0
        var fixedValue = s?.toLong(8) ?: 0
        var tryOut = 0L

        alog{ "Target: ${program.joinToString("")}"}
        while (true) {
            val a = (tryOut shl (fixedDigits * 3)) + fixedValue
            state.reset(a, b, c)
            while (state.ip in program.indices) {
                state.step()
                val outputSize = state.output.size
                if (outputSize > 0) {
                    if (state.output[outputSize - 1] != program[outputSize - 1]) break
                    if (outputSize > maxMatching) {
                        maxMatching = outputSize
                        alog { "Max matching $maxMatching / ${program.size} with ${a.toString(8)}" }
                    }
                    if (outputSize == program.size) return a
                }
            }
            tryOut++
        }

        return 0
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