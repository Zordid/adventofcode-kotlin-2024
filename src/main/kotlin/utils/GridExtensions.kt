package utils

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle

/**
 * An alias for looking at `List<List<T>>` as a [Grid].
 *
 * Important: Grids are always treated as densely filled. If a Grid has rows with fewer elements, use
 * [fixed] helper function to fix this issue.
 */
typealias Grid<T> = List<List<T>>
typealias MutableGrid<T> = MutableList<MutableList<T>>

typealias MapGrid<T> = Map<Point, T>
typealias MutableMapGrid<T> = MutableMap<Point, T>

fun String.toGrid(): Grid<Char> = split("\n").toGrid()
fun List<String>.toGrid(): Grid<Char> = map { it.toList() }.asGrid()
fun <T> List<List<T>>.asGrid(): Grid<T> = this

val Grid<*>.width: Int get() = firstOrNull()?.size ?: 0
val Grid<*>.height: Int get() = size
val Grid<*>.area: Area get() = origin to lastPoint
val Grid<*>.colIndices: IntRange get() = firstOrNull()?.indices ?: IntRange.EMPTY
val Grid<*>.rowIndices: IntRange get() = indices

val Iterable<Point>.area: Area get() = areaOrNull ?: error("No points given")
val Iterable<Point>.areaOrNull: Area? get() = boundingArea()
val MapGrid<*>.area: Area get() = areaOrNull ?: error("No points given in Map")
val MapGrid<*>.areaOrNull: Area? get() = keys.boundingArea()

/**
 * The last (bottom right) point in this [Grid] or `-1 to -1` for an empty Grid.
 */
val Grid<*>.lastPoint get() = width - 1 to height - 1

/**
 * Checks whether the given coordinate [p] is within the bounds of this [Grid]
 */
operator fun Grid<*>.contains(p: Point) = p.y in indices && p.x in 0 until width

operator fun <T> Grid<T>.iterator(): Iterator<Pair<Point, T>> = iterator {
    for (row in indices) {
        val r = get(row)
        for (col in r.indices) {
            yield((col to row) to r[col])
        }
    }
}

/**
 * Creates a new [Grid] with the specified [area], where each element is calculated by calling
 * the specified [init] function.
 */
inline fun <T> Grid(area: Area, init: (Point) -> T): Grid<T> = MutableGrid(area, init)

/**
 * Creates a new [Grid] with the specified [width] and [height], where each element is calculated by calling
 * the specified [init] function.
 */
inline fun <T> Grid(width: Int, height: Int, init: (Point) -> T): Grid<T> = MutableGrid(width, height, init)

/**
 * Creates a new [MutableGrid] with the specified [area], where each element is calculated by calling
 * the specified [init] function.
 */
inline fun <T> MutableGrid(area: Area, init: (Point) -> T): MutableGrid<T> {
    area.requireOrigin()
    return MutableGrid(area.width, area.height, init)
}

/**
 * Creates a new [MutableGrid] with the specified [width] and [height], where each element is calculated by calling
 * the specified [init] function.
 */
inline fun <T> MutableGrid(width: Int, height: Int, init: (Point) -> T): MutableGrid<T> {
    require(width >= 0 && height >= 0) { "Given area $width x $height must not be negative" }
    return MutableList(height) { y ->
        MutableList(width) { x -> init(x to y) }
    }
}

fun <T> Grid(map: Map<Point, T>, default: T): Grid<T> = MutableGrid(map, default)
fun <T> MutableGrid(map: Map<Point, T>, default: T): MutableGrid<T> = MutableGrid(map) { default }

inline fun <T> Grid(map: Map<Point, T>, crossinline default: (Point) -> T): Grid<T> =
    MutableGrid(map, default)

inline fun <T> MutableGrid(map: Map<Point, T>, crossinline default: (Point) -> T): MutableGrid<T> {
    val (first, last) = map.keys.boundingArea() ?: return mutableListOf()
    require(first.x >= 0 && first.y >= 0) {
        "Given Map contains negative points. Maybe construct using Grid(width, height) { custom translation }"
    }
    val area = origin to last
    return MutableGrid(area, map, default)
}

inline fun <T> Grid(area: Area, map: Map<Point, T>, crossinline default: (Point) -> T): Grid<T> =
    MutableGrid(area, map, default)

inline fun <T> MutableGrid(area: Area, map: Map<Point, T>, crossinline default: (Point) -> T): MutableGrid<T> {
    area.requireOrigin()
    return MutableGrid(area.width, area.height, map, default)
}

inline fun <T> Grid(width: Int, height: Int, map: Map<Point, T>, crossinline default: (Point) -> T): Grid<T> =
    MutableGrid(width, height, map, default)

inline fun <T> MutableGrid(
    width: Int,
    height: Int,
    map: Map<Point, T>,
    crossinline default: (Point) -> T
): MutableGrid<T> =
    MutableGrid(width, height) { p -> map.getOrElse(p) { default(p) } }

@PublishedApi
internal fun Area.requireOrigin() =
    require(first == origin) { "Area for grid must start at origin, but $this was given." }

/**
 * Returns a new [MutableGrid] filled with all elements of this Grid.
 */
fun <T> Grid<T>.toMutableGrid(): MutableGrid<T> = MutableList(size) { this[it].toMutableList() }

/**
 * Fixes missing elements in a [Grid] by filling in `null`.
 * @return a completely uniform n x m Grid
 */
fun <T> Grid<T>.fixed(): Grid<T?> = fixed(null)

/**
 * Fixes missing elements in a [Grid] by filling in [default].
 * @return a completely uniform n x m Grid
 */
fun <T> Grid<T>.fixed(default: T): Grid<T> {
    val (min, max) = asSequence().map { it.size }.minMaxOrNull() ?: return this
    if (min == max) return this
    return map { row ->
        row.takeIf { row.size == max } ?: List(max) { idx -> if (idx <= row.lastIndex) row[idx] else default }
    }
}

/**
 * Returns a sequence of all position/element pairs.
 */
fun <T> Grid<T>.allPointsAndValues(): Sequence<Pair<Point, T>> = sequence {
    forArea {
        yield(it to get(it))
    }
}

/**
 * Returns the first occurrences index coordinates or null if no such element can be found.
 */
fun <T> Grid<T>.indexOfOrNull(e: T): Point? = search(e).firstOrNull()

/**
 * Searches the grid from top most left point left to right, top to bottom for matching predicate.
 */
inline fun <T> Grid<T>.search(crossinline predicate: (T) -> Boolean): Sequence<Point> =
    area.allPoints().filter { predicate(this[it]) }

/**
 * Searches the grid from top most left point left to right, top to bottom for matching elements.
 */
fun <T> Grid<T>.search(vararg elements: T): Sequence<Point> =
    search { it in elements }

fun Grid<*>.indices(): Sequence<Point> = sequence {
    for (y in this@indices.indices) {
        for (x in this@indices[y].indices)
            yield(x to y)
    }
}

inline fun <T> Grid<T>.forAreaIndexed(f: (p: Point, v: T) -> Unit) {
    for (y in this.indices)
        for (x in this[y].indices)
            f(x to y, this[y][x])
}

inline fun <T> Grid<T>.forArea(f: (p: Point) -> Unit) {
    for (y in this.indices)
        for (x in this[y].indices)
            f(x to y)
}

fun <T> Grid<T>.row(row: Int): List<T> = this[row]
fun <T> Grid<T>.column(col: Int): List<T> = List(height) { row -> this[row][col] }

fun <T> Grid<T>.transposed(): Grid<T> =
    Grid(width = height, height = width) { (x, y) -> this[x][y] }

fun <T> Grid<T>.rotate90(): Grid<T> =
    Grid(width = height, height = width) { (x, y) -> this[height - 1 - x][y] }

fun <T> Grid<T>.rotate180(): Grid<T> =
    Grid(width = width, height = height) { (x, y) -> this[height - 1 - y][width - 1 - x] }

fun <T> Grid<T>.rotate270(): Grid<T> =
    Grid(width = height, height = width) { (x, y) -> this[x][width - 1 - y] }

fun <T> Grid<T>.toMapGrid(vararg sparseElements: T): Map<Point, T> =
    toMapGrid { it in sparseElements }

inline fun <T> Grid<T>.toMapGrid(sparsePredicate: (T) -> Boolean): MapGrid<T> =
    buildMap { forAreaIndexed { p, v -> if (!sparsePredicate(v)) this[p] = v } }

fun <T, R> Grid<T>.mapValues(transform: (T) -> R): Grid<R> =
    map { it.map(transform) }

fun <T, R> Grid<T>.mapValuesIndexed(transform: (Point, T) -> R): Grid<R> =
    mapIndexed { y, r -> r.mapIndexed { x, v -> transform(x to y, v) } }

fun <T> Grid<T>.plot(
    area: Area? = this.area,
    reverseX: Boolean = false,
    reverseY: Boolean = false,
    showHeaders: Boolean = true,
    colors: Map<Char, TextStyle>? = null,
    highlight: (Point) -> Boolean = { false },
    broken: String = (TextColors.white on TextColors.red)("?"),
    filler: String = " ",
    transform: (p: Point, value: T) -> Any? = { _, value -> value },
): String =
    area.plot(reverseX, reverseY, showHeaders, colors, highlight) { point ->
        transform(point, this[point.y].getOrElse(point.x) { return@plot broken }) ?: filler
    }

fun highlight(highlight: Collection<Point>, style: TextStyle = TextColors.brightRed): (Point, Any?) -> String =
    { p, v ->
        if (p in highlight) style("$v") else "$v"
    }

fun <T> MapGrid<T>.plot(
    area: Area? = keys.boundingArea(),
    reverseX: Boolean = false,
    reverseY: Boolean = false,
    showHeaders: Boolean = true,
    highlight: (Point) -> Boolean = { false },
    filler: (Point) -> String = { " " },
    transform: (Point, T) -> String = { _, value -> value.toString() },
): String =
    area.plot(reverseX, reverseY, showHeaders, highlight = highlight) { point ->
        transform(point, getOrElse(point) { return@plot filler(point) })
    }

fun Iterable<Point>.plot(
    area: Area? = this.boundingArea(),
    reverseX: Boolean = false,
    reverseY: Boolean = false,
    showHeaders: Boolean = true,
    highlight: Collection<Point> = null ?: emptyList(),
    filler: String = " ",
    paint: (Point) -> String = { "#" }
): String =
    area.plot(reverseX, reverseY, showHeaders, highlight = { it in highlight.toSet() }) { point ->
        if (point in this) paint(point) else filler
    }

inline fun Area?.plot(
    reverseX: Boolean = false,
    reverseY: Boolean = false,
    showHeaders: Boolean = true,
    colors: Map<Char, TextStyle>? = null,
    crossinline highlight: (Point) -> Boolean = { false },
    crossinline paint: (Point) -> Any,
): String {
    val area = this
    if (area == null || area.isEmpty()) return "empty area, no plot"
    val colRange = if (reverseX) area.right downTo area.left else area.left..area.right
    val rowRange = if (reverseY) area.bottom downTo area.top else area.top..area.bottom

    val (colPrefix, rowPrefix: (Int) -> String) = if (showHeaders) {
        val maxColWidth = listOf(left, right).maxOf { "$it".length }
        val maxRowWidth = listOf(top, bottom).maxOf { "$it ".length }
        val emptyRowHeader = " ".repeat(maxRowWidth)
        val colHeader = (0 until maxColWidth).joinToString(
            System.lineSeparator(),
            postfix = System.lineSeparator()
        ) { r ->
            colRange.joinToString("", prefix = " ".repeat(maxRowWidth)) { col ->
                if (col % 5 == 0 || col == colRange.first || col == colRange.last)
                    TextColors.gray("$col".padStart(maxColWidth)[r].toString())
                else " "
            }
        }
        colHeader to { r: Int ->
            if (r % 5 == 0 || r == rowRange.first || r == rowRange.last)
                TextColors.gray("$r ".padStart(maxRowWidth))
            else emptyRowHeader
        }
    } else {
        "" to { _: Int -> "" }
    }
    return rowRange.joinToString(System.lineSeparator(), prefix = colPrefix, postfix = System.lineSeparator()) { row ->
        colRange.joinToString("", prefix = rowPrefix(row)) element@{ col ->
            val point = col to row
            val value = paint(point)
            val formatted = if (colors != null && value is Char) {
                colors[value]?.let { it(value.toString()) } ?: value.toString()
            } else value.toString()
            formatted.let {
                if (highlight(point)) TextColors.red(it) else it
            }
        }
    }
}

operator fun <T> Grid<T>.get(p: Point): T =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else notInGridError(p)

fun <T> Grid<T>.getOrNull(p: Point): T? =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else null

inline fun <T> Grid<T>.getOrElse(p: Point, default: (Point) -> T): T =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else default(p)

fun <T> Grid<T>.getOrDefault(p: Point, default: T): T =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else default

operator fun <T> MutableGrid<T>.set(p: Point, v: T) {
    if (p.y in indices && p.x in first().indices) this[p.y][p.x] = v
    else notInGridError(p)
}

operator fun List<String>.get(p: Point): Char =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else notInListGridError(p)

fun List<String>.getOrNull(p: Point): Char? =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else null

fun List<String>.getOrElse(p: Point, default: (Point) -> Char): Char =
    if (p.y in indices && p.x in first().indices) this[p.y][p.x]
    else default(p)

private fun Grid<*>.notInGridError(p: Point): Nothing =
    error("Point $p not in grid of dimensions $width x $height")

private fun List<String>.notInListGridError(p: Point): Nothing =
    error("Point $p not in grid of dimensions ${firstOrNull()?.length ?: 0} x $size")
