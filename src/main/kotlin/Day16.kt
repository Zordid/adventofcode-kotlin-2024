import utils.*

class Day16 : Day(16, 2024, "Reindeer Maze") {

    val maze = input.grid
    val s = maze.search('S').single()
    val e = maze.search('E').single()

    data class Pose(val pos: Point, val dir: Direction4)

    fun neighbors(p: Pose): List<Pose> =
        listOfNotNull(
            p.copy(pos = p.pos + p.dir).takeIf { maze[p.pos + p.dir] != '#' },
            p.copy(dir = p.dir.left).takeIf { maze[p.pos + p.dir.left] != '#' },
            p.copy(dir = p.dir.right).takeIf { maze[p.pos + p.dir.right] != '#' }
        )

    fun costs(from: Pose, to: Pose): Int =
        if (from.pos != to.pos) 1
        else 1000 // if (from.pos != s) 1000 else 0

    override fun part1(): Any? {
        val d = Dijkstra(Pose(s, Direction4.EAST), ::neighbors, ::costs)
        val r = d.search { it.pos == e }
        return r.distanceToStart
    }

    override fun part2(): Any? {
        val d = DijkstraMultiSolution(Pose(s, Direction4.EAST), ::neighbors, ::costs)

        val r = d.search { it.pos == e }

        val points = mutableSetOf<Point>()
        log {
            maze.area.plot { pos ->
                Direction4.all.joinToString { r.distance[Pose(pos, it)]?.toString() ?: "-" }.padStart(22) + " |"
            }
        }
        r.paths.forEach {
            points += it.map { it.pos }
        }

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
//        """
//            #####
//            #..E#
//            #...#
//            #S..#
//            #####
//        """.trimIndent() part1 1004 part2 8

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