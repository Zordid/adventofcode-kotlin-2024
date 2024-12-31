import arrow.fx.coroutines.parMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class Day22 : Day(22, 2024, "Monkey Market") {

    val initialSecrets = input.map { it.extractFirstLong() }

    override fun part1(): Long =
        initialSecrets.sumOf {
            it.pseudoRandSequence().drop(2000).first()
        }

    override fun part2(): Int = runBlocking(Dispatchers.Default) {
        val sequences = initialSecrets.map {
            it.pseudoRandSequence()
                .map { (it % 10).toInt() }
                .zipWithNext()
                .map { (a, b) -> b to b - a }
                .take(2000)
        }

        val signatures = sequences.parMap {
            val stats = buildMap<List<Int>, Int> {
                it.windowed(4) { window ->
                    window.map { it.second } to window.last().first
                }.forEach { (key, price) ->
                    if (key !in this) {
                        this[key] = price
                    }
                }
            }
            stats
        }
        val allSignatures = signatures.flatMapTo(mutableSetOf()) { it.keys }

        alog { "There are ${allSignatures.size} unique signatures in ${signatures.size} buyers" }
        allSignatures.parMap { key ->
            signatures.sumOf { it[key] ?: 0 }
        }.max()
    }

    fun Long.pseudoRandSequence(): Sequence<Long> =
        generateSequence(this) { secret -> secret.nextSecret() }

    fun Long.nextSecret(): Long {
        // step 1
        var secretNumber = this
        val mul64 = secretNumber * 64
        secretNumber = secretNumber.xor(mul64)
        secretNumber = secretNumber % 16777216

        // step 2
        val div32 = secretNumber / 32
        secretNumber = secretNumber.xor(div32)
        secretNumber = secretNumber % 16777216

        // step 3
        val mul2048 = secretNumber * 2048
        secretNumber = secretNumber.xor(mul2048)
        secretNumber = secretNumber % 16777216

        return secretNumber
    }

}

fun main() {
    solve<Day22> {
        """
            1
            10
            100
            2024
        """.trimIndent() part1 37327623

        """
            1
            2
            3
            2024
        """.trimIndent() part2 23
    }
}