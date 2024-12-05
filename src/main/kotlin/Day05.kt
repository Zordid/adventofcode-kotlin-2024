class Day05 : Day(5, 2024, "Print Queue") {

    private val rules = input[0].map { it.extractAllIntegers() }
    private val updates = input[1].map { it.extractAllIntegers() }

    private fun List<Int>.firstViolatedIndicesOrNull(): Pair<Int, Int>? =
        rules.asSequence()
            .map { (a, b) -> indexOf(a) to indexOf(b) }
            .firstOrNull { (idx1, idx2) -> idx2 in 0..<idx1 }

    override fun part1() =
        updates.filter { it.firstViolatedIndicesOrNull() == null }
            .sumOf { it[it.size / 2] }

    override fun part2() =
        updates.filterNot { it.firstViolatedIndicesOrNull() == null }.map {
            var violation = it.firstViolatedIndicesOrNull()
            val update = it.toMutableList()
            while (violation != null) {
                with(violation) {
                    update[first] = update[second].also { update[second] = update[first] }
                }
                violation = update.firstViolatedIndicesOrNull()
            }
            update
        }.sumOf { it[it.size / 2] }

}

fun main() {
    solve<Day05> {
        """
            47|53
            97|13
            97|61
            97|47
            75|29
            61|13
            75|53
            29|13
            97|29
            53|29
            61|53
            97|53
            61|29
            47|13
            75|47
            97|75
            47|61
            75|61
            47|29
            75|13
            53|13

            75,47,61,53,29
            97,61,53,29,13
            75,29,13
            75,97,47,61,53
            61,13,29
            97,13,75,29,47
        """.trimIndent() part1 143 part2 123
    }
}