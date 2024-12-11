import utils.powerOf10

class Day11 : Day(11, 2024, "Plutonian Pebbles") {

    val stones = input.longs

    override fun part1() = stones.sumOf { blinky(it, 25) }

    override fun part2() = stones.sumOf { blinky(it, 75) }

    private fun blinky(stone: Long, blinks: Int): Long =
        cache.getOrPut(stone to blinks) {
            if (blinks == 0) return@getOrPut 1L

            val s = stone.toString()
            when {
                stone == 0L -> blinky(1, blinks - 1)
                s.length % 2 == 0 -> {
                    val factor = powerOf10(s.length / 2)
                    blinky(stone / factor, blinks - 1) +
                            blinky(stone % factor, blinks - 1)
                }

                else -> blinky(stone * 2024, blinks - 1)
            }
        }

    private val cache = mutableMapOf<Pair<Long, Int>, Long>()

}

fun main() {
    solve<Day11> {
        "125 17" part1 55312
    }
}