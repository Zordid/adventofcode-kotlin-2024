import arrow.core.padZip

class Day09 : Day(9, 2024, "Disk Fragmenter") {

    val rawLayout = input.string.toList()
    val diskSize = rawLayout.sumOf { it.digitToInt() }.show("Disk size")

    override fun part1(): Any? {
        val (files, free) = rawLayout.withIndex().partition { (idx, c) ->
            idx % 2 == 0
        }.let { it.first.map { it.value.digitToInt() } to it.second.map { it.value.digitToInt() } }

        val layout = IntArray(diskSize) { -1 }

        var pos = 0
        var fileId = 0

        files.padZip(free).forEach { (disk, free) ->
            if (disk != null) {
                repeat(disk) { layout[pos++] = fileId }
                fileId++
            }
            if (free != null) {
                pos += free
            }
        }

        var seek = layout.lastIndex
        var firstFree = 0
        while (firstFree <= layout.lastIndex && layout[firstFree] >= 0) firstFree++

        log { "first to move is at $seek: ${layout[seek]}" }
        log { "first free at $firstFree" }

        while (seek > 0 && seek != firstFree) {
            log { layout.joinToString("") { if (it >= 0) it.toString() else "." } + "   seek: $seek, first free at: $firstFree" }

            while (seek > 0 && layout[seek] == -1) seek--
            layout[firstFree] = layout[seek]
            layout[seek] = -1

            while (firstFree <= layout.lastIndex && layout[firstFree] >= 0) firstFree++
        }

        return layout.withIndex().sumOf { (idx, fileId) ->
            if (fileId >= 0) (idx * fileId).toLong() else 0
        }
    }

    data class F(val pos: Int, val size: Int)

    override fun part2(): Any? {
        val (files, free) = rawLayout.withIndex().partition { (idx, c) ->
            idx % 2 == 0
        }.let { it.first.map { it.value.digitToInt() } to it.second.map { it.value.digitToInt() } }

        val layout = IntArray(diskSize) { -1 }

        var pos = 0
        var fileId = 0

        val fileMap = mutableMapOf<Int, F>()
        val freeMap = mutableSetOf<F>()

        files.padZip(free).forEach { (disk, free) ->
            if (disk != null) {
                fileMap[fileId] = F(pos, disk)
                repeat(disk) { layout[pos++] = fileId }
                fileId++
            }
            if (free != null) {
                freeMap += F(pos, free)
                pos += free
            }
        }

        fileMap.keys.sortedDescending().forEach { fileId ->
            val file = fileMap[fileId]!!
            log { "Looking at $fileId $file..." }
            log { freeMap.sortedBy { it.pos } }
            log { layout.joinToString("") { if (it >= 0) it.toString() else "." } }

            val free = freeMap.sortedBy { it.pos }.firstOrNull { it.size >= file.size }

            if (free != null && free.pos < file.pos) {
                log { "moving $fileId $file to $free" }
                fileMap[fileId] = F(free.pos, file.size)

                for (p in free.pos..<(free.pos + file.size)) layout[p] = fileId
                for (p in file.pos..<(file.pos + file.size)) layout[p] = -1

                freeMap -= free
                if (free.size > file.size) {
                    freeMap += F(free.pos + file.size, free.size - file.size)
                }
                freeMap += F(file.pos, file.size)

                var combine = freeMap.sortedBy { it.pos }.zipWithNext().firstOrNull { (f1, f2) ->
                    f1.pos + f1.size == f2.pos
                }
                while (combine != null) {
                    val (f1, f2) = combine
                    freeMap -= f1
                    freeMap -= f2
                    freeMap += F(f1.pos, f1.size + f2.size)
                    combine = freeMap.sortedBy { it.pos }.zipWithNext().firstOrNull { (f1, f2) ->
                        f1.pos + f1.size == f2.pos
                    }
                }


            }
        }

        return layout.withIndex().sumOf { (idx, fileId) ->
            if (fileId >= 0) (idx * fileId).toLong() else 0
        }
    }

}

fun main() {
    solve<Day09> {
        "2333133121414131402" part1 1928 part2 2858
    }
}