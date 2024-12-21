import arrow.fx.coroutines.parMap
import com.github.ajalt.mordant.rendering.TextColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import utils.*
import kotlin.math.absoluteValue

class Day20 : Day(20, 2024, "Race Condition") {

    val trackMap = input.grid
    val area = trackMap.area
    val start = trackMap.search('S').single()
    val end = trackMap.search('E').single()
    val track = graph<Point>({
        it.directNeighbors(area).filter { trackMap[it] != '#' }
    })

    override fun part1() = cheat(2)
    override fun part2() = cheat(20)

    fun cheat(allowedCheatLength: Int): Int {
        val honestResult = track.dijkstraSearch(start, null)
        val honestPath = honestResult.pathTo(end)
        val distance = honestResult.distance
        val saveThreshold = if (testInput) 50 else 100

        log { "Normal path takes ${honestPath.size} picoseconds..." }
        log {
            trackMap.plot(
                colors = trackMap.autoColoring('O' to TextColors.blue),
                highlight = { it == start || it == end }
            ) { point, c ->
                if (c == '.' && point in honestPath) 'O' else c
            }
        }
        log { "Searching for cheat starts that cut at least $saveThreshold picoseconds." }
        return runBlocking(Dispatchers.Default) {
            honestPath.parMap(concurrency = 10) { cheatHere ->
                val cheatStartCost = distance[cheatHere]!!
                cheatHere.vicinityByManhattan(allowedCheatLength).count { cheatEnd ->
                    val cheatEndCost = distance[cheatEnd] ?: return@count false
                    val cheatLength = cheatEnd manhattanDistanceTo cheatHere
                    val normalLength = cheatEndCost - cheatStartCost
                    (normalLength - cheatLength) >= saveThreshold
                }
            }.sum()
        }
    }

}

/**
 * Provides all [Point]s within a square area around the given [Point], max [size] steps away in all directions.
 */
fun Point.vicinity(size: Int): Sequence<Point> = sequence {
    for (y in this@vicinity.y - size..this@vicinity.y + size)
        for (x in this@vicinity.x - size..this@vicinity.x + size)
            yield(x to y)
}

fun Point.vicinityByManhattan(size: Int): Sequence<Point> = sequence {
    for (dy in -size..+size) {
        val width = (size - dy.absoluteValue)
        for (x in x - width..x + width) {
            yield(x to y + dy)
        }
    }
}

fun main() {
    solve<Day20> {
        val shouldSave = """
            There are 32 cheats that save 50 picoseconds.
            There are 31 cheats that save 52 picoseconds.
            There are 29 cheats that save 54 picoseconds.
            There are 39 cheats that save 56 picoseconds.
            There are 25 cheats that save 58 picoseconds.
            There are 23 cheats that save 60 picoseconds.
            There are 20 cheats that save 62 picoseconds.
            There are 19 cheats that save 64 picoseconds.
            There are 12 cheats that save 66 picoseconds.
            There are 14 cheats that save 68 picoseconds.
            There are 12 cheats that save 70 picoseconds.
            There are 22 cheats that save 72 picoseconds.
            There are 4 cheats that save 74 picoseconds.
            There are 3 cheats that save 76 picoseconds.
        """.trimIndent().lines().sumOf { it.extractFirstInt() }

        """
            ###############
            #...#...#.....#
            #.#.#.#.#.###.#
            #S#...#.#.#...#
            #######.#.#.###
            #######.#.#...#
            #######.#.###.#
            ###..E#...#...#
            ###.#######.###
            #...###...#...#
            #.#####.#.###.#
            #.#...#.#.#...#
            #.#.#.#.#.#.###
            #...#...#...###
            ###############
        """.trimIndent() part1 1 part2 shouldSave
    }
}