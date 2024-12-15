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
        val what = this[pos]
        require(what in "@O") { "on wrong element $pos = $what" }

        val target = pos + dir
        val onTarget = this[target]
        log { "Pos: $pos, $what -> $onTarget" }

        if (onTarget == BOX)
            if (!move(target, dir)) return false

        if (this[target] == '.') {
            this[target] = what
            this[pos] = '.'
            return true
        } else {
            return false
        }
    }

    override fun part2(): Any? {
        val logEnabled = ANIMATION
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
            if (logEnabled) {
                Thread.sleep(10)
                aocTerminal.cursor.hide()
                aocTerminal.cursor.move {
                    up(mg.height + (mg.width.toString().length) + 1)
                    clearLine()
                }
            }
            if (mg.move2(robot, cmd))
                robot = robot + cmd

            log {
                "$idx / ${instructions.length}: Move $command\n" + mg.plot(
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
        }
        return mg.search(BOX_LEFT).map { it.y * 100L + it.x }.sum()
    }

    fun MutableGrid<Char>.move2(pos: Point, dir: Direction): Boolean {
        val what = this[pos]
        if (what == WALL) return false
        require(what in "@[]") { "on wrong element $pos = $what" }
        val target = pos + dir
        val onTarget = this[target]
        if (what == ROBOT) {
            if (onTarget != EMPTY)
                if (!move2(target, dir)) return false
            this[target] = what
            this[pos] = EMPTY
            return true
        } else {
            if (dir in listOf(Direction4.LEFT, Direction4.RIGHT)) {
                if (onTarget != EMPTY)
                    if (!move2(target, dir)) return false
                this[target] = what
                this[pos] = EMPTY
                return true
            }

            val boxStart = if (what == BOX_LEFT) pos else pos.left
            val copy = this.map { it.toMutableList() }
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