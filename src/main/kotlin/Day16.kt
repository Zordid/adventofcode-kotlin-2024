import utils.*

class Day16 : Day(16, 2024, "Reindeer Maze") {

    val maze = input.grid
    val s = maze.search('S').single()
    val e = maze.search('E').single()

    data class Pose(val pos: Point, val dir: Direction4)

    val startPose = Pose(s, Direction4.EAST)

    val graph = graph<Pose>(
        { p ->
            listOfNotNull(
                p.copy(pos = p.pos + p.dir).takeIf { maze[p.pos + p.dir] != '#' },
                p.copy(dir = p.dir.left).takeIf { maze[p.pos + p.dir.left] != '#' },
                p.copy(dir = p.dir.right).takeIf { maze[p.pos + p.dir.right] != '#' }
            )
        }, { from, to ->
            if (from.pos != to.pos) 1
            else 1000
        })

    override fun part1() =
        graph.dijkstraSearch(startPose) { it.pos == e }.distanceToStart

    override fun part2(): Any? {
        val r = graph.dijkstraSearchAll(startPose) { it.pos == e }
        val points = r.paths.flatMap { it.map { it.pos } }.toSet()

        log {
            maze.plot { p, c ->
                if (p in points) 'O' else c
            }
        }

        return points.size
    }

}

fun main() {
    solve<Day16> {
        """
            ###############
            #.......#....E#
            #.#.###.#.###.#
            #.....#.#...#.#
            #.###.#####.#.#
            #.#.#.......#.#
            #.#.#####.###.#
            #...........#.#
            ###.#.#####.#.#
            #...#.....#.#.#
            #.#.#.###.#.#.#
            #.....#...#.#.#
            #.###.#.#.#.#.#
            #S..#.....#...#
            ###############
        """.trimIndent() part1 7036 part2 45

        """
            #################
            #...#...#...#..E#
            #.#.#.#.#.#.#.#.#
            #.#.#.#...#...#.#
            #.#.#.#.###.#.#.#
            #...#.#.#.....#.#
            #.#.#.#.#.#####.#
            #.#...#.#.#.....#
            #.#.#####.#.###.#
            #.#.#.......#...#
            #.#.###.#####.###
            #.#.#...#.....#.#
            #.#.#.#####.###.#
            #.#.#.........#.#
            #.#.#.#########.#
            #S#.............#
            #################
        """.trimIndent() part1 11048 part2 64
    }
}