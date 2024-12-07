import com.github.ajalt.mordant.rendering.TextColors.brightRed
import utils.*

open class Day06 : Day(6, 2024) {

    val map = input.grid
    val area = map.area
    val obstacles = map.search('#').toSet()
    val start = map.search('^').single()

    override fun part1() = calculatePath().also {
        log {
            map.formatted { p, value -> if (value == '.' && p in it) brightRed("*") else "$value" }
        }
    }.size

    override fun part2() = calculatePath().count { pathLoops(it) }

    fun calculatePath(): Collection<Point> {
        val path = mutableSetOf<Point>()
        var pos = start
        var dir = Direction4.NORTH
        while (pos in area) {
            path += pos
            val next = pos + dir
            if (next in obstacles)
                dir = dir.right
            else
                pos = next
        }
        return path
    }

    private fun pathLoops(newObstacleAt: Point): Boolean {
        val newObstacles = (obstacles + newObstacleAt).toSet()
        val visited = mutableSetOf<Pair<Point, Direction4>>()
        var pos = start
        var dir = Direction4.NORTH
        while (true) {
            if (!visited.add(pos to dir)) return true
            var next = pos + dir
            while (next !in newObstacles && next in area)
                next += dir
            if (next !in area) return false
            next -= dir
            pos = next
            dir = dir.right
        }
    }

}

fun main() {
    solve<Day06> {
        """
            ....#.....
            .........#
            ..........
            ..#.......
            .......#..
            ..........
            .#..^.....
            ........#.
            #.........
            ......#...
        """.trimIndent() part1 41 part2 6
    }
}