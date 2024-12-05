class Day05 : Day(5, 2024, "Print Queue") {

    private val rules = input[0].map { it.extractAllIntegers() }
    private val updates = input[1].map { it.extractAllIntegers() }

    private fun List<Int>.firstViolatedRuleOrNull(): List<Int>? =
        rules.firstOrNull { rule ->
            rule.all { it in this } && this.indexOf(rule[0]) > this.indexOf(rule[1])
        }

    override fun part1() =
        updates.filter { it.firstViolatedRuleOrNull() == null }
            .sumOf { it[it.size / 2] }

    override fun part2() =
        updates.filter { it.firstViolatedRuleOrNull() != null }.map {
            var violation = it.firstViolatedRuleOrNull()
            val update = it.toMutableList()
            while (violation != null) {
                val idx1 = update.indexOf(violation[0])
                val idx2 = update.indexOf(violation[1])
                update[idx1] = update[idx2].also { update[idx2] = update[idx1] }
                violation = update.firstViolatedRuleOrNull()
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