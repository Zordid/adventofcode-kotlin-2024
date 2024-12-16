import org.junit.jupiter.api.TestFactory

class DaysTest {

    @TestFactory
    fun `AoC 2024`() = aocTests {
        test<Day01>(765748, 27732508)
        test<Day02>(220, 296)
        test<Day03>(189600467, 107069718)
        test<Day04>(2500, 1933)
        test<Day05>(6267, 5184)
        test<Day06>(5212, 1767)
        test<Day07>(882304362421, 145149066755184)
        test<Day08>(252, 839)
        test<Day09>(6334655979668, 6349492251099)
        test<Day10>(510, 1058)
        test<Day11>(229043, 272673043446478)
        test<Day12>(1464678, 877492)
        test<Day13>(28753, 102718967795500)
        test<Day14>(230436441, 8270)
        test<Day15>(1495147, 1524905)
        test<Day16>(65436, 489)
    }

}
