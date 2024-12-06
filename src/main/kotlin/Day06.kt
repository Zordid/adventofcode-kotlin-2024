import com.github.ajalt.mordant.rendering.TextColors
import utils.*

class Day06 : Day(6, 2024) {

    val p = input.grid
    val obstacles = p.search('#').toSet()
    val start = p.search('^').single()
    val area = p.area

    override fun part1() = calculatePath().also {
        println(p.formatted { p, value ->
            if (value == '.' && p in it) TextColors.brightRed("*") else value.toString()
        })
    }.size

    override fun part2() = calculatePath().count { pathLoops(it) }

    private fun calculatePath(): Collection<Point> {
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
        var d = Direction4.NORTH
        while (pos in area) {
            if (!visited.add(pos to d)) return true
            var next = pos + d
            while (next !in newObstacles && next in area)
                next += d
            if (next !in area) return false
            next -= d
            pos = next
            d = d.right
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