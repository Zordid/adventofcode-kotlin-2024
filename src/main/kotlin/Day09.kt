class Day09 : Day(9, 2024, "Disk Fragmenter") {

    companion object {
        const val FREE = -1
    }

    val rawLayout = input.string.asIterable().map { it.digitToInt() }

    override fun part1(): Long {
        val diskSize = rawLayout.sum()
        val layout = IntArray(diskSize)
        var pos = 0
        var fileId = 0
        rawLayout.alternate(
            { repeat(it) { layout[pos++] = fileId }; fileId++ },
            { repeat(it) { layout[pos++] = FREE } }
        )

        var blockToMove = layout.lastIndex
        var firstFreeBlock = 0

        while (true) {
            // advance first free to free
            while (firstFreeBlock <= layout.lastIndex && layout[firstFreeBlock] >= 0) firstFreeBlock++
            // decrease blockToMove to last occupied
            while (blockToMove > 0 && layout[blockToMove] == -1) blockToMove--

            log { layout.joinToString("") { if (it >= 0) it.toString() else "." } + "   seek: $blockToMove, first free at: $firstFreeBlock" }
            if (blockToMove > firstFreeBlock && blockToMove in layout.indices && firstFreeBlock in layout.indices) {
                layout[firstFreeBlock] = layout[blockToMove]
                layout[blockToMove] = -1
            } else break
        }

        return layout.withIndex().sumOf { (idx, fileId) ->
            if (fileId >= 0) (idx * fileId).toLong() else 0
        }
    }

    override fun part2(): Long {
        var pos = 0
        var fileId = 0

        val fileMap = mutableMapOf<Int, DiskSpan>()
        val freeMap = setOf<DiskSpan>().toSortedSet(compareBy { it.pos })
        rawLayout.alternate(
            { fileMap[fileId++] = DiskSpan(pos, it); pos += it },
            { if (it > 0) freeMap += DiskSpan(pos, it); pos += it }
        )

        (fileId - 1 downTo 0).forEach { fileId ->
            val file = fileMap[fileId]!!
            val free = freeMap.firstOrNull { it.size >= file.size }

            if (free != null && free.pos < file.pos) {
                fileMap[fileId] = DiskSpan(free.pos, file.size)
                freeMap -= free
                if (free.size > file.size) {
                    freeMap += DiskSpan(free.pos + file.size, free.size - file.size)
                }

                val prevFree = freeMap.find { it.pos + it.size == file.pos }
                val nextFree = freeMap.find { it.pos == file.pos + file.size }
                freeMap += when {
                    prevFree != null && nextFree != null -> DiskSpan(
                        prevFree.pos,
                        prevFree.size + file.size + nextFree.size
                    )

                    prevFree != null -> DiskSpan(prevFree.pos, prevFree.size + file.size)
                    nextFree != null -> DiskSpan(file.pos, file.size + nextFree.size)
                    else -> DiskSpan(file.pos, file.size)
                }
            }
        }

        return fileMap.entries.sumOf { (fileId, span) ->
            fileId.toLong() * (span.pos..<span.pos + span.size).fastSum()
        }
    }

    private inline fun <T> Iterable<T>.alternate(a: (T) -> Unit, b: (T) -> Unit) =
        forEachIndexed { index, t ->
            if (index % 2 == 0) a(t) else b(t)
        }

    data class DiskSpan(val pos: Int, val size: Int)

    private fun IntRange.fastSum() = if (isEmpty()) 0 else (first + last).toLong() * (last - first + 1) / 2

}

fun main() {
    solve<Day09> {
        "2333133121414131402" part1 1928 part2 2858
    }
}