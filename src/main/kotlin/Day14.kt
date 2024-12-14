import utils.*
import utils.Direction4.Companion.LEFT
import utils.Direction4.Companion.RIGHT

class Day14 : Day(14, 2024, "Restroom Redoubt") {

    private val robots = input.map {
        val (px, py, vx, vy) = it.extractAllIntegers()
        Robot(px to py, vx to vy)
    }

    private val dim = if (testInput) 11 to 7 else 101 to 103

    data class Robot(val p: Point, val v: Point)

    private fun Collection<Robot>.moveAll() = map {
        it.copy(p = (it.p + it.v) mod dim)
    }

    override fun part1(): Int {
        var nr = robots
        repeat(100) { nr = nr.moveAll() }
        val mid = dim / 2
        val ul = nr.count { it.p.x < mid.x && it.p.y < mid.y }
        val ur = nr.count { it.p.x > mid.x && it.p.y < mid.y }
        val ll = nr.count { it.p.x < mid.x && it.p.y > mid.y }
        val lr = nr.count { it.p.x > mid.x && it.p.y > mid.y }
        return ul * ur * ll * lr
    }

    override fun part2(): Int {
        var nr = robots
        repeat(Int.MAX_VALUE) { seconds ->
            val pic = nr.map { it.p }.toSet()
            val diagonalsLeft = pic.mapNotNull { pic.diagonalLine(it, LEFT)}
            val diagonalsRight = pic.mapNotNull { pic.diagonalLine(it, RIGHT)}

            if (diagonalsLeft.size >= 4 && diagonalsRight.size >= 4) {
                val mark = (diagonalsLeft + diagonalsRight).flatten()
                alog {
                    "$seconds\n" + pic.plot(area = mark.boundingArea()?.grow(8), highlight = mark)
                }
                return seconds
            }

            nr = nr.moveAll()
        }
        return 0
    }

    fun Set<Point>.diagonalLine(start: Point, dir: Direction4, minLength: Int = 5): Set<Point>? =
        buildSet {
            var p = start
            while (p in this@diagonalLine && p + dir !in this@diagonalLine) {
                add(p)
                p = (p + dir).down
            }
        }.takeIf { it.size >= minLength }

}

fun main() {
    solve<Day14> {
        """
            p=0,4 v=3,-3
            p=6,3 v=-1,-3
            p=10,3 v=-1,2
            p=2,0 v=2,-1
            p=0,0 v=1,3
            p=3,0 v=-2,-2
            p=7,6 v=-1,-3
            p=3,0 v=-1,-2
            p=9,3 v=2,3
            p=7,3 v=-1,2
            p=2,4 v=2,-3
            p=9,5 v=-3,-3
        """.trimIndent() part1 12
    }
}
