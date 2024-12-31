import utils.*

class Day25 : Day(25, 2024, "Code Chronicle") {

    val p = input.sections.map {
        val m = it.grid
        if (m[0 to 0] == '#') {
            Lock(m.colIndices.map { m.column(it).count { it == '#' } -1 })
        } else
            Key(m.colIndices.map { m.column(it).count { it == '#' } -1 })
    }

    sealed interface KeyLock { val s: List<Int> }
    data class Key(override val s: List<Int>): KeyLock
    data class Lock(override val s: List<Int>): KeyLock

    override fun part1(): Any? {
        val (keys, locks) = p.partition { it is Key }
        return keys.sumOf { key ->
            locks.count { lock -> lock.s.zip(key.s) { a, b -> a + b}.all { it <= 5 } }
        }
    }

}

fun main() {
    solve<Day25> {
        """
    #####
    .####
    .####
    .####
    .#.#.
    .#...
    .....

    #####
    ##.##
    .#.##
    ...##
    ...#.
    ...#.
    .....

    .....
    #....
    #....
    #...#
    #.#.#
    #.###
    #####

    .....
    .....
    #.#..
    ###..
    ###.#
    ###.#
    #####

    .....
    .....
    .....
    #....
    #.#..
    #.#.#
    #####
""".trimIndent() part1 3
    }
}