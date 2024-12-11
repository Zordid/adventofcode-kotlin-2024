class Day11 : Day(11, 2024, "Plutonian Pebbles") {

    val stones = input.longs

    override fun part1() = stones.sumOf { blinky(25, it) }

    override fun part2() = stones.sumOf { blinky(75, it) }

    private fun blinky(blinks: Int, stone: Long): Long {
        if (blinks == 0) return 1L
        cache[stone to blinks]?.let { return it }

        val s = stone.toString()
        return when {
            stone == 0L -> blinky(blinks - 1, 1)
            s.length % 2 == 0 ->
                blinky(blinks - 1, s.take(s.length / 2).toLong()) +
                        blinky(blinks - 1, s.drop(s.length / 2).toLong())

            else -> blinky(blinks - 1, stone * 2024)
        }.also { cache[stone to blinks] = it }
    }

    private val cache = mutableMapOf<Pair<Long, Int>, Long>()

}

fun main() {
    solve<Day11> {
        "125 17" part1 55312
    }
}