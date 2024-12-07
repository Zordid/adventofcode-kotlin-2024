import utils.KPixelGameEngine
import utils.height
import utils.width
import java.awt.Color

fun main() {
    Day06Vis().start()
}

class Day06Vis : KPixelGameEngine("AoC 2024 Day 6") {

    val day06 = Day06()

    val path = day06.calculatePath().toList()

    override fun onCreate() {
        construct(day06.area.width, day06.area.height, 6, 6)

        day06.obstacles.forEach {
            draw(it)
        }

        limitFps = 10
    }

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        if (frame < path.size)
            draw(path[frame.toInt()], Color.CYAN)
        if (frame == 100L) limitFps = 100
    }

}