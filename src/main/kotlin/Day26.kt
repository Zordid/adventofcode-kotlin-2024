import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import utils.*
import utils.dim3d.*
import kotlin.time.measureTime

typealias Cloud = Set<Point3D>

class Piece(val original: Cloud) {
    val rotations: Map<Cloud, Point3D> = buildMap {
        for (rx in 0..3) {
            for (ry in 0..3) {
                for (rz in 0..3) {
                    val rotated = original.rotateX(rx).rotateY(ry).rotateZ(rz)
                    val (lower, upper) = rotated.boundingCube()
                    val rotatedAndShifted = rotated.map { it - lower }.toSet()
                    if (rotatedAndShifted !in this)
                        put(rotatedAndShifted, rotatedAndShifted.boundingCube().second)
                }
            }
        }
    }
}

fun Set<Point3D>.normalized(): Set<Point3D> {
    if (isEmpty()) return this
    val (lower, _) = boundingCube()
    return mapTo(mutableSetOf()) { it - lower }
}

fun Cloud.rotateX(times: Int): Cloud = map { it.rotateX(times) }.toSet()
fun Cloud.rotateY(times: Int): Cloud = map { it.rotateY(times) }.toSet()
fun Cloud.rotateZ(times: Int): Cloud = map { it.rotateZ(times) }.toSet()

class Day26 : Day(25, 2024) {

    val dimensions = input.sections[0].integers.single()

    val piecesRaw = input.sections.drop(1).map {
        val count = it.lines.first().extractFirstInt()
        count to it.lines.drop(1).toGrid().toMapGrid('.')
            .keys.flatMap { p -> listOf(Point3D(p.x, p.y, 0), Point3D(p.x, p.y, 1)) }.toSet()
    }

    override fun part1(): Any? {
        val pieces = piecesRaw.map { it.first to Piece(it.second) }
        val bag = pieces.flatMap { (count, piece) -> List(count) { piece } }
            .withIndex().associate { it.index to it.value }

        val room = mapOf<Point3D, Int>()

        val time = measureTime { place(bag, room) }
        println("Took $time")
        return 0
    }

    fun Set<Point3D>.allOrientations() = sequence {
        var copy = this@allOrientations
        repeat(2) {
            repeat(3) {
                repeat(4) {
                    yield(copy.normalized())
                    copy = copy.rotateY(1)
                }
                copy = copy.rotateX(1)
            }
            copy = copy.rotateZ(2)
        }
    }.take(24)

    fun Set<Point3D>.canonical() = allOrientations().minBy {
        it.sumOf { it.x + it.y + it.z }
    }

    val cache = mutableMapOf<Pair<Set<Point3D>, Int>, Boolean>()

    fun place(pieces: Map<Int, Piece>, room: Map<Point3D, Int>): Boolean =
        cache.getOrPut(room.keys.canonical() to pieces.size) {
            if (pieces.isEmpty()) {
                log { "Found a solution!" }
                log { room.show() }
                return@getOrPut true
            }
            // we have more pieces!
            val (n, piece) = pieces.entries.first()
            val remaining = pieces - n

            for ((rot, max) in piece.rotations) {
                for (z in 0..dimensions - max.z) {
                    for (y in 0..dimensions - max.y) {
                        for (x in 0..dimensions - max.x) {
                            room.place(n, rot, Point3D(x, y, z))?.let { place(remaining, it) }
                        }
                    }
                }
            }

            return@getOrPut false
        }

    val lowerColors = listOf(
        TextColors.yellow,
        TextColors.green,
        TextColors.blue,
        TextColors.red,
        TextColors.cyan,
        TextColors.magenta
    )
    val upperColors = listOf(
        TextColors.brightYellow,
        TextColors.brightGreen,
        TextColors.brightBlue,
        TextColors.brightRed,
        TextColors.brightCyan,
        TextColors.brightMagenta
    ).reversed()

    fun Map<Point3D, Int>.show() {
        val planes = (0..dimensions).map { z ->
            val plane = this.entries.filter { it.key.z == z }.associate { (it.key.x to it.key.y) to it.value }
            plane.plot(origin areaTo (dimensions to dimensions)) { p, x ->
                val lower = x / 6 == 0
                val color = if (lower) lowerColors[x % 6] + TextStyles.bold else upperColors[x % 6]
                color(((x % 6) + 1).toString())
            }.lines().let {
                val width = 7
                listOf(TextColors.yellow("z = $z".padEnd(width))) + it.dropLast(1)
            }
        }
        planes.transposed().forEach { println(it.joinToString("  |  ", prefix = "| ", postfix = " |")) }
    }

    fun Map<Point3D, Int>.place(n: Int, piece: Cloud, pos: Point3D): Map<Point3D, Int>? =
        if (piece.all { it + pos !in this }) this + piece.associate { it + pos to n }
        else null

}

fun main() {
    solve<Day26> {
        """
            5
            
            6
            #..#
            ####
            
            6
            ####
            """.trimIndent() part1 -1
    }
}