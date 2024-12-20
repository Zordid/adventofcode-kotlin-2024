package utils

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.TextColors.white
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles

fun <T : Any> Grid<T>.plot(
    area: Area? = this.maxArea,
    reverseX: Boolean = false,
    reverseY: Boolean = false,
    showHeaders: Boolean = true,
    elementWidth: Int = 1,
    colors: Map<T, TextStyle?> = emptyMap(),
    highlight: (Point) -> Boolean = { false },
    transform: (p: Point, value: T) -> Any = { _, value -> value },
): String = area.plot(
    reverseX,
    reverseY,
    showHeaders,
    elementWidth,
    colors = brokenGridStyle + colors,
    highlight = highlight
) { point ->
    val value = this.getOrElse(point) { return@plot HoleInGrid }
    transform(point, value)
}

fun <T : Any> MapGrid<T>.plot(
    area: Area? = keys.boundingArea(),
    reverseX: Boolean = false,
    reverseY: Boolean = false,
    showHeaders: Boolean = true,
    elementWidth: Int = 1,
    colors: Map<Any, TextStyle?> = emptyMap(),
    highlight: (Point) -> Boolean = { false },
    filler: (Point) -> String = { " " },
    transform: (Point, T) -> Any = { _, value -> value },
): String = area.plot(
    reverseX, reverseY, showHeaders, elementWidth, colors = colors, highlight = highlight
) { point ->
    transform(point, getOrElse(point) { return@plot filler(point) })
}

inline fun Iterable<Point>.plot(
    area: Area? = this.boundingArea(),
    reverseX: Boolean = false,
    reverseY: Boolean = false,
    showHeaders: Boolean = true,
    colors: Map<String, TextStyle?> = emptyMap(),
    crossinline highlight: (Point) -> Boolean = { false },
    filler: String = " ",
    crossinline paint: (Point) -> String = { "#" },
): String =
    area.plot(reverseX, reverseY, showHeaders, colors = colors, highlight = highlight) { point ->
        if (point in this) paint(point) else filler
    }

inline fun <T : Any> Area?.plot(
    reverseX: Boolean = false,
    reverseY: Boolean = false,
    showHeaders: Boolean = true,
    elementWidth: Int = 1,
    colors: Map<T, TextStyle?> = emptyMap(),
    crossinline highlight: (Point) -> Boolean = { false },
    crossinline transform: (Point) -> T,
): String {
    val headerStyle = TextColors.yellow

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
            headerStyle(colRange.joinToString("", prefix = " ".repeat(maxRowWidth)) { col ->
                val s = if (col % 5 == 0 || col == colRange.first || col == colRange.last)
                    "$col".padStart(maxColWidth)[r].toString()
                else " "
                s.padEnd(elementWidth)
            })
        }
        colHeader to { r: Int ->
            if (r % 5 == 0 || r == rowRange.first || r == rowRange.last)
                headerStyle("$r ".padStart(maxRowWidth))
            else emptyRowHeader
        }
    } else {
        "" to { _: Int -> "" }
    }
    return rowRange.joinToString(System.lineSeparator(), prefix = colPrefix, postfix = System.lineSeparator()) { row ->
        colRange.joinToString("", prefix = rowPrefix(row)) element@{ col ->
            val point = col to row
            val highlight = highlight(point)
            val value = transform(point)
            val color = colors[value]?.let {
                if (highlight) it on white else it
            }
            val s = value.toString().padEnd(elementWidth)
            val formatted = if (color != null) color(s) else s
            formatted.let {
                if (highlight(point)) red(it) else it
            }
        }
    }
}

private object HoleInGrid {
    override fun toString() = "?"
}

private val brokenGridStyle =
    mapOf<Any, TextStyle?>(HoleInGrid to (TextColors.black + TextStyles.italic on red))

fun main() {
    val maze = """
        #####
        #..E#   #####
        #...#   #...#
        #.#.#####.#.#####
        #S#.......*....Z# 
        #################
    """.trimIndent().toGrid()

    println(maze.plot(
        elementWidth = 3,
        colors = maze.autoColoring()
    ))

    println(maze.fixed(" ").plot(
        colors = maze.autoColoring()
    ))

}