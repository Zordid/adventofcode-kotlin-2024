import com.github.ajalt.mordant.rendering.TextColors
import utils.*

class Day06 : Day(6, 2024) {

    val p = input.grid
    val obstacles = p.searchIndices('#').toList()
    val start = p.searchIndices('^').single()
    val area = p.area

    override fun part1() = calcPath().also {
        println(p.formatted { p, value ->
            if (value == '.' && p in it) TextColors.brightRed("*") else value.toString()
        })
    }.size

    override fun part2() = calcPath().count { pathLoops(it) }

    fun calcPath(): Collection<Point> {
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

    fun pathLoops(nO: Point): Boolean {
        val newObstacles = obstacles + nO
        val visited = mutableSetOf<Pair<Point, Direction4>>()
        var pos = start
        var d = Direction4.NORTH
        while (pos in area) {
            if ((pos to d) in visited) return true
            visited += pos to d
            val next = pos + d
            when (next in newObstacles) {
                true -> d = d.right
                else -> pos = next
            }
        }
        return false
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