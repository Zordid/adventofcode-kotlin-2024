import utils.combinations
import utils.permutations

class Day07 : Day(7, 2024) {

    val equ = input.map { it.extractAllLongs() }

    fun List<Long>.testEquation(): Boolean {
        val test = first().toBigInteger()
        val first = drop(1).first()
        val rest = drop(2)
        require(rest.isNotEmpty())

        log { "$test: $first $rest" }

        for (o in 0..<1.shl(rest.size)) {
            var v = first.toBigInteger()
            var tooBig = false

            for (idx in rest.indices) {
                if (v > test) {
                    tooBig = true
                    break
                }
                val tryAddition = o.and(1.shl(idx)) > 0
                if (tryAddition)
                    v += rest[idx].toBigInteger()
                else
                    v *= rest[idx].toBigInteger()
            }

            if (!tooBig && v == test) {
                log { o }
                return true
            }
        }
        return false
    }

    fun <T> List<T>.countWith(size: Int): Sequence<List<T>> = sequence {
        val alphabet = this@countWith
        val initialValue = List(size) { alphabet[0] }
        val nextValue = initialValue.toMutableList()
        do {
            yield(nextValue.toList())
            for (p in size - 1 downTo 0) {
                val idx = alphabet.indexOf(nextValue[p])
                if (idx < alphabet.lastIndex) {
                    nextValue[p] = alphabet[idx + 1]
                    break
                }
                nextValue[p] = alphabet[0]
            }
        } while (nextValue != initialValue)
    }

    fun List<Long>.testEquation2(): Boolean {
        val test = first().toBigInteger()
        val first = drop(1).first()
        val rest = drop(2)
        require(rest.isNotEmpty())

        log { "$test: $first $rest" }


        listOf(0,1,2).countWith(rest.size).forEach { ops ->
            var result = first.toBigInteger()
            var tooBig = false

            for ((op, value) in ops.zip(rest)) {
                if (result > test) {
                    tooBig = true
                    break
                }
                when (op) {
                    0 -> result *= value.toBigInteger()
                    1 -> result += value.toBigInteger()
                    2 -> result = "$result$value".toBigInteger()
                    else -> error("op $op")
                }
            }

            if (!tooBig && result == test) {
                log { "Match with $ops" }
                return true
            }
        }
        return false
    }

    override fun part1(): Any? {
        return equ.filter { it.testEquation() }.sumOf { it.first().toBigInteger() }
    }

    override fun part2(): Any? {
        return equ.filter { it.testEquation2() }.sumOf { it.first().toBigInteger() }
    }

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