import com.github.ajalt.mordant.rendering.TextColors
import utils.*

const val ANIMATION = false

class Day15 : Day(15, 2024, "Warehouse Woes") {

    val maze = input.sections[0].grid
    val robotStart = maze.search('@').single()
    val instructions = input.sections[1].string

    override fun part1(): Any? {
        val mg = maze.toMutableGrid()
        var robot = robotStart
        log { "Initial state" }
        log { mg.plot() }

        for (command in instructions) {
            val cmd = Direction4.interpret(command)
            log { "Move $command" }
            if (mg.move(robot, cmd))
                robot = robot + cmd
            log { mg.plot() }
        }

        return mg.search(BOX).map { it.y * 100L + it.x }.sum()
    }

    fun MutableGrid<Char>.move(pos: Point, dir: Direction): Boolean {
        val targetPos = pos + dir
        val here = this[pos]
        val there = this[targetPos]

        log { "Pos: $pos, $here -> $there" }
        when (there) {
            WALL -> return false
            BOX -> if (!move(targetPos, dir)) return false
        }

        this[targetPos] = here
        this[pos] = EMPTY
        return true
    }

    override fun part2(): Any? {
        logEnabled = ANIMATION && !testInput
        val mg = maze.map {
            it.flatMap {
                when (it) {
                    '.' -> ".."
                    WALL -> "##"
                    BOX -> "[]"
                    '@' -> "@."
                    else -> error(it)
                }.toList()
            }
        }.toMutableGrid()

        var robot = mg.search('@').single()
        log { "Initial state\n" + mg.plot() }

        for ((idx, command) in instructions.withIndex()) {
            val cmd = Direction4.interpretOrNull(command) ?: continue
            if (mg.move2(robot, cmd))
                robot = robot + cmd

            log(clearEol = true) { "$idx / ${instructions.length}: Move $command" }
            log {
                mg.plot(
                    colors = mapOf(
                        EMPTY to TextColors.gray,
                        ROBOT to TextColors.red,
                        WALL to (TextColors.gray on TextColors.gray),
                        BOX to (TextColors.black on TextColors.white),
                        BOX_RIGHT to (TextColors.black on TextColors.white),
                        BOX_LEFT to (TextColors.black on TextColors.white),
                    )
                )
            }
            if (logEnabled) {
                //Thread.sleep(5)
                aocTerminal.cursor.hide()
                aocTerminal.cursor.move {
                    up(mg.height + (mg.width.toString().length) + 1)
                }
            }
        }
        return mg.search(BOX_LEFT).map { it.y * 100L + it.x }.sum()
    }

    fun MutableGrid<Char>.move2(pos: Point, dir: Direction): Boolean {
        val here = this[pos]
        if (here == WALL) return false
        require(here in "@[]") { "on wrong element $pos = $here" }
        val target = pos + dir
        val there = this[target]
        if (here == ROBOT) {
            if (there != EMPTY)
                if (!move2(target, dir)) return false
            this[target] = here
            this[pos] = EMPTY
            return true
        } else {
            if (dir in listOf(Direction4.LEFT, Direction4.RIGHT)) {
                if (there != EMPTY)
                    if (!move2(target, dir)) return false
                this[target] = here
                this[pos] = EMPTY
                return true
            }

            val boxStart = if (here == BOX_LEFT) pos else pos.left
            val copy = this.toMutableGrid()
            val free1 = (this[boxStart + dir] == EMPTY) || move2(boxStart + dir, dir)
            val free2 = (this[boxStart.right + dir] == EMPTY) || move2(boxStart.right + dir, dir)

            if (free1 && free2) {
                this[boxStart + dir] = BOX_LEFT
                this[boxStart.right + dir] = BOX_RIGHT
                this[boxStart] = EMPTY
                this[boxStart.right] = EMPTY
                return true
            } else {
                this.clear()
                this.addAll(copy)
                return false
            }
        }
    }

    companion object {
        private const val EMPTY = '.'
        private const val ROBOT = '@'
        private const val WALL = '#'
        private const val BOX = 'O'
        private const val BOX_LEFT = '['
        private const val BOX_RIGHT = ']'
    }

}

fun main() {
    solve<Day15> {

        """
            ########
            #..O.O.#
            ##@.O..#
            #...O..#
            #.#.O..#
            #...O..#
            #......#
            ########

            <^^>>>vv<v>>v<<
        """.trimIndent() part1 2028
        """
            ##########
            #..O..O.O#
            #......O.#
            #.OO..O.O#
            #..O@..O.#
            #O#..O...#
            #O..O..O.#
            #.OO.O.OO#
            #....O...#
            ##########

            <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
            vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
            ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
            <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
            ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
            ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
            >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
            <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
            ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
            v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
        """.trimIndent() part1 10092 part2 9021
    }
}