class Day03 : Day(3, 2024, "Mull It Over") {

    private val memory = input.string

    override fun part1() =
        Regex("mul\\((\\d+),(\\d+)\\)").findAll(memory).sumOf { mr ->
            val (a, b) = mr.destructured
            a.toInt() * b.toInt()
        }

    override fun part2() =
        Regex("mul\\((\\d+),(\\d+)\\)|do\\(\\)|don't\\(\\)").findAll(memory)
            .fold(true to 0) { (enabled, sum), mr ->
                when (mr.value) {
                    "do()" -> true to sum
                    "don't()" -> false to sum
                    else if enabled -> {
                        val (a, b) = mr.destructured
                        enabled to (sum + a.toInt() * b.toInt())
                    }

                    else -> enabled to sum
                }
            }.second

}

fun main() {
    solve<Day03> {
        "xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))" part1 161
        "xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))" part2 48
    }
}