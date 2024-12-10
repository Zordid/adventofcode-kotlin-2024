import utils.*

class Day10 : Day(10, 2024, "Hoof it") {

    private val topo = input.grid
    private val starts = topo.search('0')

    override fun part1() = starts.sumOf { it.scorating(mutableSetOf()) }

    override fun part2() = starts.sumOf { it.scorating(mutableListOf()) }

    private fun Point.scorating(summitStore: MutableCollection<Point>): Int {
        val x = ArrayDeque(listOf(this))

        while (x.isNotEmpty()) {
            val p = x.removeFirst()
            val h = topo[p].digitToInt()
            if (h == 9) summitStore += p else
                for (n in p.directNeighbors(topo.area)) {
                    val nh = topo[n].digitToInt()
                    if (nh == h + 1) x += n
                }
        }
        return summitStore.size
    }

}

fun main() {
    solve<Day10> {
        """
            89010123
            78121874
            87430965
            96549874
            45678903
            32019012
            01329801
            10456732
        """.trimIndent() part1 36 part2 81
    }
}