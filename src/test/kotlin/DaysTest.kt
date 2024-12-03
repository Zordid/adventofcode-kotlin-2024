import org.junit.jupiter.api.TestFactory

class DaysTest {

    @TestFactory
    fun `AoC 2024`() = aocTests {
        test<Day01>(765748, 27732508)
        test<Day02>(220, 296)
        test<Day03>(189600467, 107069718)
    }

}
