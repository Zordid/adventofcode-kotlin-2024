@file:Suppress("unused")

package utils

import kotlin.math.*

typealias Point = Pair<Int, Int>
typealias Area = Pair<Point, Point>

val Point.x: Int get() = first
val Point.y: Int get() = second

val Point.manhattanDistance: Int
    get() = x.absoluteValue + y.absoluteValue

infix fun Point.manhattanDistanceTo(other: Point) = (this - other).manhattanDistance

val Point.right: Point get() = x + 1 to y
fun Point.right(steps: Int = 1) = x + steps to y
val Point.left: Point get() = x - 1 to y
fun Point.left(steps: Int = 1) = x - steps to y
val Point.up: Point get() = x to y - 1
fun Point.up(steps: Int = 1) = x to y - steps
val Point.down: Point get() = x to y + 1
fun Point.down(steps: Int = 1) = x to y + steps

fun Point.walkWhile(direction: Direction, steps: Int = 1, predicate: (Point) -> Boolean): Point {
    var current = this
    var next = neighbor(direction, steps)
    while (predicate(next)) {
        current = next
        next = current.neighbor(direction, steps)
    }
    return current
}

fun Point.rightWhile(steps: Int = 1, predicate: (Point) -> Boolean) = walkWhile(Direction4.RIGHT, steps, predicate)
fun Point.leftWhile(steps: Int = 1, predicate: (Point) -> Boolean) = walkWhile(Direction4.LEFT, steps, predicate)

fun Point.neighbor(direction: Direction, steps: Int = 1) = this + (direction.vector * steps)

infix fun Point.isDirectNeighborOf(other: Point): Boolean =
    (this - other).manhattanDistance == 1

/**
 * calculates the list of the four direct neighbors of the point.
 */
fun Point.directNeighbors(): List<Point> = Direction4.allVectors.map { this + it }

/**
 * calculates the list of the four direct neighbors of the point, but removes the ones outside the given [area].
 */
fun Point.directNeighbors(area: Area): List<Point> = Direction4.allVectors.map { this + it }.filter { it in area }

/**
 * calculates the list of the eight direct neighbors of the point.
 */
fun Point.surroundingNeighbors(): List<Point> = Direction8.allVectors.map { this + it }

/**
 * calculates the list of the eight direct neighbors of the point, but removes the ones outside the given [area].
 */
fun Point.surroundingNeighbors(area: Area): List<Point> = Direction8.allVectors.map { this + it }.filter { it in area }

val origin: Point = 0 to 0
val emptyArea: Area = origin to (-1 to -1)

infix operator fun Point.plus(other: Point): Point = x + other.x to y + other.y
infix operator fun Point.minus(other: Point): Point = x - other.x to y - other.y
infix operator fun Point.times(factor: Int): Point = when (factor) {
    0 -> origin
    1 -> this
    else -> x * factor to y * factor
}

infix operator fun Point.div(factor: Int) = when (factor) {
    1 -> this
    else -> x / factor to y / factor
}

infix operator fun Point.div(factor: Point) = when (factor) {
    1 to 1 -> this
    else -> x / factor.x to y / factor.y
}

infix operator fun Point.rem(factor: Int): Point = x % factor to y % factor
infix operator fun Point.rem(factor: Point): Point = x % factor.x to y % factor.y
infix fun Point.mod(factor: Point): Point = x.mod(factor.x) to y.mod(factor.y)
infix fun Point.mod(factor: Int): Point = x.mod(factor) to y.mod(factor)

operator fun Point.unaryMinus(): Point = -x to -y

val Point.length: Double get() = sqrt(x.toDouble() * x + y.toDouble() * y)
val Point.absoluteValue: Point get() = x.absoluteValue to y.absoluteValue
val Point.sign: Point get() = x.sign to y.sign

fun Point.rotateLeft90(times: Int = 1): Point = when (times.mod(4)) {
    1 -> y to -x
    2 -> -x to -y
    3 -> -y to x
    else -> this
}

fun Point.rotateRight90(times: Int = 1): Point = when (times.mod(4)) {
    1 -> -y to x
    2 -> -x to -y
    3 -> y to -x
    else -> this
}

//operator fun Point.compareTo(other: Point): Int =
//    if (y == other.y) x.compareTo(other.x) else y.compareTo(other.y)

fun Point.toArea(): Area = this to this

operator fun Point.rangeTo(other: Point): Sequence<Point> = when (other) {
    this -> sequenceOf(this)
    else -> sequence {
        val d = Direction8.ofVector(this@rangeTo, other) ?: error("not a usable direction vector")
        var p = this@rangeTo
        while (p != other) {
            yield(p)
            p += d
        }
        yield(other)
    }
}

@JvmName("areaPlus")
operator fun Area.plus(other: Area) = listOf(first, second, other.first, other.second).boundingArea()!!

fun areaOf(a: Point, b: Point): Area = (min(a.x, b.x) to min(a.y, b.y)) to (max(a.x, b.x) to max(a.y, b.y))
fun Area.isValid(): Boolean = first.x <= second.x && first.y <= second.y
fun Area.fixed(): Area = if (isValid()) this else areaOf(first, second)

fun Area.grow(by: Int = 1): Area = upperLeft + Direction8.NORTHWEST * by to lowerRight + Direction8.SOUTHEAST * by
fun Area.growWidth(by: Int = 1): Area = upperLeft + Direction8.WEST * by to lowerRight + Direction8.EAST * by
fun Area.growHeight(by: Int = 1): Area = upperLeft + Direction8.NORTH * by to lowerRight + Direction8.SOUTH * by
fun Area.growTop(by: Int = 1): Area = upperLeft + Direction8.NORTH * by to lowerRight
fun Area.growLeft(by: Int = 1): Area = upperLeft + Direction8.WEST * by to lowerRight
fun Area.growRight(by: Int = 1): Area = upperLeft to lowerRight + Direction8.EAST * by
fun Area.growBottom(by: Int = 1): Area = upperLeft to lowerRight + Direction8.SOUTH * by
fun Area.shrink(by: Int = 1): Area = upperLeft + Direction8.SOUTHEAST * by to lowerRight + Direction8.NORTHWEST * by
fun Area.scale(by: Int): Area = upperLeft to upperLeft + (width * by - 1 to height * by - 1)

fun Area.isEmpty() = size == 0
fun Area.isNotEmpty() = !isEmpty()
val Area.size: Int
    get() = width * height

val Area.upperLeft: Point get() = first
val Area.lowerRight: Point get() = second
val Area.upperRight: Point get() = second.x to first.y
val Area.lowerLeft: Point get() = first.x to second.y
val Area.left: Int get() = first.x
val Area.right: Int get() = second.x
val Area.top: Int get() = first.y
val Area.bottom: Int get() = second.y

fun allPointsInArea(from: Point, to: Point): Sequence<Point> =
    areaOf(from, to).allPoints()

fun Iterable<Point>.withIn(area: Area) = filter { it in area }
fun Sequence<Point>.withIn(area: Area) = filter { it in area }

private val areaRegex = ".*?(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+(\\d+).*".toRegex()

fun areaFromString(s: String): Area? =
    areaRegex.matchEntire(s)?.groupValues
        ?.let { (it[1].toInt() to it[2].toInt()) to (it[3].toInt() to it[4].toInt()) }

fun Area.allPoints(): Sequence<Point> = sequence { forEach { yield(it) } }
fun Area.allPointsReversed(): Sequence<Point> = sequence { forEachReversed { yield(it) } }
fun Area.border(): Sequence<Point> = sequence { forBorder { yield(it) } }
fun Area.corners(): Sequence<Point> =
    if (isEmpty())
        emptySequence()
    else
        listOf(upperLeft, upperRight, lowerRight, lowerLeft).distinct().asSequence()

inline fun Area.forEach(f: (p: Point) -> Unit) {
    for (y in first.y..second.y) {
        for (x in first.x..second.x) {
            f(x to y)
        }
    }
}

inline fun Area.forEachReversed(f: (p: Point) -> Unit) {
    for (y in second.y downTo first.y) {
        for (x in second.x downTo first.x) {
            f(x to y)
        }
    }
}

inline fun Area.forBorder(f: (p: Point) -> Unit) {
    for (y in first.y..second.y) {
        when (y) {
            first.y, second.y -> for (x in first.x..second.x) {
                f(x to y)
            }

            else -> {
                f(first.x to y)
                f(second.x to y)
            }
        }
    }
}

operator fun Area.contains(p: Point) = p.x in first.x..second.x && p.y in first.y..second.y
operator fun Area.plus(amount: Int) = grow(by = amount)
operator fun Area.minus(amount: Int) = shrink(by = amount)

val Area.width: Int
    get() = (second.x - first.x + 1).coerceAtLeast(0)

val Area.height: Int
    get() = (second.y - first.y + 1).coerceAtLeast(0)

fun Area.overlaps(other: Area): Boolean =
    max(left, other.left) <= min(right, other.right) && max(top, other.top) <= min(bottom, other.bottom)

fun Iterable<Point>.boundingArea(): Area? {
    val (minX, maxX) = minMaxByOrNull { it.x } ?: return null
    val (minY, maxY) = minMaxByOrNull { it.y }!!
    return (minX.x to minY.y) to (maxX.x to maxY.y)
}

/**
 * Turns a list of exactly 2 Int values into a Point, useful for map(::asPoint)
 */
fun asPoint(l: List<Int>): Point {
    require(l.size == 2) { "List should exactly contain 2 values for x and y, but has ${l.size} values!" }
    return Point(l[0], l[1])
}

interface Direction {
    val name: String
    val right: Direction
    val left: Direction
    val opposite: Direction
    val vector: Point
    val symbol: Char
}

operator fun Direction.times(n: Int): Point = vector * n
operator fun Point.plus(direction: Direction): Point = this + direction.vector
operator fun Point.minus(direction: Direction): Point = this - direction.vector

const val LEFT_ARROW = '\u2190'
const val UP_ARROW = '\u2191'
const val RIGHT_ARROW = '\u2192'
const val DOWN_ARROW = '\u2193'
const val NW_ARROW = '\u2196'
const val NE_ARROW = '\u2197'
const val SE_ARROW = '\u2198'
const val SW_ARROW = '\u2199'

enum class Direction4(override val vector: Point, override val symbol: Char) : Direction {
    NORTH(0 to -1, UP_ARROW),
    EAST(1 to 0, RIGHT_ARROW),
    SOUTH(0 to 1, DOWN_ARROW),
    WEST(-1 to 0, LEFT_ARROW);

    override val right by lazy { entries[(ordinal + 1).mod(entries.size)] }
    override val left by lazy { entries[(ordinal - 1).mod(entries.size)] }
    override val opposite by lazy { entries[(ordinal + entries.size / 2).mod(entries.size)] }

    companion object {
        val all = entries
        val allVectors = all.map { it.vector }

        val UP = NORTH
        val RIGHT = EAST
        val DOWN = SOUTH
        val LEFT = WEST

        fun ofVector(p1: Point, p2: Point): Direction4? = ofVector(p2 - p1)

        fun ofVector(v: Point) =
            all.firstOrNull { it.vector.x == v.x.sign && it.vector.y == v.y.sign }

        fun ofSymbol(c: Char, nesw: String = "^>v<"): Direction4? =
            nesw.indexOf(c).takeUnless { it == -1 }?.let { entries[it] }

        fun interpret(s: Any): Direction4 = interpretOrNull(s) ?: error("What direction should '$s' indicate?")

        fun interpretOrNull(s: Any): Direction4? = when ("$s".uppercase()) {
            NORTH.name, "N", "UP", "U", "^" -> NORTH
            EAST.name, "E", "RIGHT", "R", ">" -> EAST
            SOUTH.name, "S", "DOWN", "D", "V" -> SOUTH
            WEST.name, "W", "LEFT", "L", "<" -> WEST
            else -> null
        }

        inline fun forEach(action: (Direction) -> Unit) {
            all.forEach(action)
        }

        inline fun <T> map(f: (p: Direction4) -> T) = all.map(f)
    }
}

enum class Direction8(override val vector: Point, override val symbol: Char) : Direction {
    NORTH(0 to -1, UP_ARROW),
    NORTHEAST(1 to -1, NE_ARROW),
    EAST(1 to 0, RIGHT_ARROW),
    SOUTHEAST(1 to 1, SE_ARROW),
    SOUTH(0 to 1, DOWN_ARROW),
    SOUTHWEST(-1 to 1, SW_ARROW),
    WEST(-1 to 0, LEFT_ARROW),
    NORTHWEST(-1 to -1, NW_ARROW);

    override val right by lazy { entries[(ordinal + 1).mod(entries.size)] }
    override val left by lazy { entries[(ordinal - 1).mod(entries.size)] }
    override val opposite by lazy { entries[(ordinal + entries.size / 2).mod(entries.size)] }

    companion object {
        val all = entries
        val allVectors = all.map { it.vector }

        val UP = NORTH
        val UP_LEFT = NORTHWEST
        val UP_RIGHT = NORTHEAST
        val RIGHT = EAST
        val DOWN = SOUTH
        val DOWN_LEFT = SOUTHWEST
        val DOWN_RIGHT = SOUTHEAST
        val LEFT = WEST

        fun ofVector(p1: Point, p2: Point) = ofVector(p2 - p1)

        fun ofVector(v: Point) =
            all.firstOrNull { it.vector.x == v.x.sign && it.vector.y == v.y.sign }

        inline fun forEach(action: (Direction) -> Unit) {
            all.forEach(action)
        }

        inline fun <T> map(f: (p: Direction8) -> T) = all.map(f)
    }
}
