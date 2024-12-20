package utils

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PointsAndAreasKtTest {

    @Test
    fun `direction4 works`() {
        Direction4.NORTH.left shouldBe Direction4.WEST
        Direction4.NORTH.right shouldBe Direction4.EAST
        Direction4.EAST.left shouldBe Direction4.NORTH
        Direction4.EAST.right shouldBe Direction4.SOUTH
        Direction4.SOUTH.left shouldBe Direction4.EAST
        Direction4.SOUTH.right shouldBe Direction4.WEST
        Direction4.WEST.left shouldBe Direction4.SOUTH
        Direction4.WEST.right shouldBe Direction4.NORTH

        Direction4.ofVector(Direction4.NORTH.vector.rotateLeft90(-2)) shouldBe Direction4.SOUTH
        Direction4.ofVector(Direction4.EAST.vector.rotateLeft90(-2)) shouldBe Direction4.WEST
    }

    @Test
    fun `direction8 works`() {
        Direction8.NORTH.left shouldBe Direction8.NORTHWEST
        Direction8.NORTH.right shouldBe Direction8.NORTHEAST
        Direction8.EAST.left shouldBe Direction8.NORTHEAST
        Direction8.EAST.right shouldBe Direction8.SOUTHEAST
        Direction8.SOUTH.left shouldBe Direction8.SOUTHEAST
        Direction8.SOUTH.right shouldBe Direction8.SOUTHWEST
        Direction8.WEST.left shouldBe Direction8.SOUTHWEST
        Direction8.WEST.right shouldBe Direction8.NORTHWEST

        Direction8.ofVector(Direction8.NORTH.vector.rotateLeft90(-2)) shouldBe Direction8.SOUTH
        Direction8.ofVector(Direction8.NORTHEAST.vector.rotateLeft90(-2)) shouldBe Direction8.SOUTHWEST
    }

    private fun Area.assertArea(emptiness: Boolean, width: Int, height: Int, border: Int) {
        println(this)
        assertEquals(emptiness, this.isEmpty())
        assertEquals(width, this.width)
        assertEquals(height, this.height)
        assertEquals(width * height, this.size)
        assertEquals(width * height, this.allPoints().count())
        assertEquals(border, this.border().count())
        this.allPoints().forEach { assertTrue(it in this) }
    }

    @Test
    fun someRandomAreaTests() {
        var a = Point(10, 10).toArea()

        a.assertArea(false, 1, 1, 1)

        a = a.grow()
        a.assertArea(false, 3, 3, 8)

        a = a.shrink()
        a.assertArea(false, 1, 1, 1)

        a = a.shrink()
        a.assertArea(true, 0, 0, 0)

        a = a.fixed()
        a.assertArea(false, 3, 3, 8)

        a = a.grow()
        a.assertArea(false, 5, 5, 4 * 4)
    }

    @Test
    fun testInvalidAreas() {
        val a: Area = (Point(10, 10) areaTo Point(5, 20)).fixed()
        a.assertArea(false, 6, 11, 30)
    }

    @Test
    fun randomTests() = runTest {
        checkAll(
            Arb.int(-50..50),
            Arb.int(-50..50),
            Arb.int(-50..50),
            Arb.int(-50..50),
        ) { x1, y1, x2, y2 ->
            val a = x1 to y1
            val b = x2 to y2

            val uncheckedArea = a areaTo b
            val area = if (uncheckedArea.isValid()) {
                uncheckedArea.fixed() shouldBe uncheckedArea
                uncheckedArea.fixed().isValid().shouldBeTrue()
                uncheckedArea
            } else {
                uncheckedArea.fixed() shouldNotBe uncheckedArea
                uncheckedArea.fixed().isValid().shouldBeTrue()
                uncheckedArea.fixed()
            }

            area.width shouldBeGreaterThan 0
            area.height shouldBeGreaterThan 0

            area.size shouldBe area.width * area.height
            if (area.height > 1 && area.width > 1) {
                area.border().distinct().count() shouldBeExactly area.width * 2 + (area.height - 2) * 2

                val content = area.allPoints().toSet()
                content shouldHaveSize area.size

                if (area.height > 2 && area.width > 2) {
                    val shrunk = area.shrink(1)

                    val borderByArea = content - shrunk.allPoints().toSet()
                    val border = area.border().toSet()
                    border shouldBe borderByArea
                }
            }
            if (area.width == 1)
                area.border().distinct().count() shouldBeExactly area.height
            if (area.height == 1)
                area.border().distinct().count() shouldBeExactly area.width
        }
    }
}