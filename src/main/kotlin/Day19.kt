import utils.product
import kotlin.math.min

class Day19 : Day(19, 2022, "Not Enough Minerals") {

    val bps = input.map { it.extractAllIntegers() }.show()

    val id = 0
    val ORE_ROBOT_COST_ORE = 1
    val CLAY_ROBOT_COST_ORE = 2
    val OBSIDIAN_ROBOT_COST_ORE = 3
    val OBSIDIAN_ROBOT_COST_CLAY = 4
    val GEODE_ROBOT_COST_ORE = 5
    val GEODE_ROBOT_COST_OBSIDIAN = 6

    data class State(
        val remainingTime: Int,
        val ore: Int = 0,
        val clay: Int = 0,
        val obsidian: Int = 0,
        val geode: Int = 0,
        val oreRobots: Int = 1,
        val clayRobots: Int = 0,
        val obsidianRobots: Int = 0,
        val geodeRobots: Int = 0,
    )

    override fun part1() = bps.sumOf { bp ->
        log { "Checking $bp" }
        val max = maxGeodeOpened(bp, 24).also { log { "this yields $it geodes" } }
        max * bp[id]
    }

    override fun part2() = bps.take(3).map { bp ->
        log { "Checking $bp" }
        val max = maxGeodeOpened(bp, 32).also { log { "this yields $it geodes" } }
        max
    }.product()

    fun maxGeodeOpened(bp: List<Int>, time: Int): Int {
        val maxSpendOre = listOf(
            ORE_ROBOT_COST_ORE,
            CLAY_ROBOT_COST_ORE,
            OBSIDIAN_ROBOT_COST_ORE,
            GEODE_ROBOT_COST_ORE
        ).maxOf { bp[it] }
        val maxSpendClay = bp[OBSIDIAN_ROBOT_COST_CLAY]
        val maxSpendObsidian = bp[GEODE_ROBOT_COST_OBSIDIAN]

        var best = 0
        val queue = ArrayDeque<State>()
        queue.add(State(time))

        val seen = mutableSetOf<Any>()
        while (queue.isNotEmpty()) {
            val state = queue.removeFirst()
            best = best.coerceAtLeast(state.geode)
            if (state.remainingTime == 0) continue

            val stateId = with(state) {
                val o = ore.coerceAtMost(remainingTime * maxSpendOre - oreRobots * (remainingTime - 1))
                val c = clay.coerceAtMost(remainingTime * maxSpendClay - clayRobots * (remainingTime - 1))
                val ob = obsidian.coerceAtMost(remainingTime * maxSpendObsidian - obsidianRobots * (remainingTime - 1))

                listOf(o, c, ob, geode, oreRobots, clayRobots, obsidianRobots, geodeRobots)
            }

            seen.add(stateId) || continue

            if (seen.size % 1000000 == 0)
                log { "${state.remainingTime} $best ${seen.size}" }

            with(state) {
                queue += copy(
                    remainingTime = remainingTime - 1,
                    ore = ore + oreRobots,
                    clay = clay + clayRobots,
                    obsidian = obsidian + obsidianRobots,
                    geode = geode + geodeRobots,
                )
                if (oreRobots < maxSpendOre && ore < maxSpendOre * remainingTime &&
                    ore >= bp[ORE_ROBOT_COST_ORE])
                    queue += copy(
                        remainingTime = remainingTime - 1,
                        ore = ore + oreRobots - bp[ORE_ROBOT_COST_ORE],
                        clay = clay + clayRobots,
                        obsidian = obsidian + obsidianRobots,
                        geode = geode + geodeRobots,
                        oreRobots = oreRobots + 1,
                    )
                if (clayRobots < maxSpendClay && clay < maxSpendClay * remainingTime &&
                    ore >= bp[CLAY_ROBOT_COST_ORE])
                    queue += copy(
                        remainingTime = remainingTime - 1,
                        ore = ore + oreRobots - bp[CLAY_ROBOT_COST_ORE],
                        clay = clay + clayRobots,
                        obsidian = obsidian + obsidianRobots,
                        geode = geode + geodeRobots,
                        clayRobots = clayRobots + 1,
                    )
                if (obsidianRobots < maxSpendObsidian && obsidian < maxSpendObsidian &&
                    ore >= bp[OBSIDIAN_ROBOT_COST_ORE] && clay >= bp[OBSIDIAN_ROBOT_COST_CLAY])
                    queue += copy(
                        remainingTime = remainingTime - 1,
                        ore = ore + oreRobots - bp[OBSIDIAN_ROBOT_COST_ORE],
                        clay = clay + clayRobots - bp[OBSIDIAN_ROBOT_COST_CLAY],
                        obsidian = obsidian + obsidianRobots,
                        geode = geode + geodeRobots,
                        obsidianRobots = obsidianRobots + 1,
                    )
                if (ore >= bp[GEODE_ROBOT_COST_ORE] && obsidian >= bp[GEODE_ROBOT_COST_OBSIDIAN])
                    queue += copy(
                        remainingTime = remainingTime - 1,
                        ore = ore + oreRobots - bp[GEODE_ROBOT_COST_ORE],
                        clay = clay + clayRobots,
                        obsidian = obsidian + obsidianRobots - bp[GEODE_ROBOT_COST_OBSIDIAN],
                        geode = geode + geodeRobots,
                        geodeRobots = geodeRobots + 1,
                    )

            }
        }
        return best
    }

    val dp = mutableMapOf<Any, Int>()

    fun opt(
        bp: List<Int>,
        time: Int,
        ore: Int = 0,
        clay: Int = 0,
        obsidian: Int = 0,
        geode: Int = 0,
        oreRobots: Int = 1,
        clayRobots: Int = 0,
        obsidianRobots: Int = 0,
        geodeRobots: Int = 0,
    ): Int {
        if (time == 0) return geode
        val maxSpendOre = listOf(
            ORE_ROBOT_COST_ORE,
            CLAY_ROBOT_COST_ORE,
            OBSIDIAN_ROBOT_COST_ORE,
            GEODE_ROBOT_COST_ORE
        ).maxOf { bp[it] }
        val t = time
        val key = listOf(
            time,
            ore.coerceAtMost(t + maxSpendOre - oreRobots * (t - 1)),
            clay.coerceAtMost(t * bp[OBSIDIAN_ROBOT_COST_CLAY] - clayRobots * (t - 1)),
            obsidian.coerceAtMost(t * bp[GEODE_ROBOT_COST_OBSIDIAN] - obsidianRobots * (t - 1)),
            geode,
            oreRobots,
            clayRobots,
            obsidianRobots,
            geodeRobots
        )
        dp[key]?.let { return it }

        val nOre = ore + oreRobots
        val nClay = clay + clayRobots
        val nObsidian = obsidian + obsidianRobots
        val nGeode = geode + geodeRobots

        val maxOre = ore / bp[ORE_ROBOT_COST_ORE]
        val maxClay = ore / bp[CLAY_ROBOT_COST_ORE]
        val maxObsidian = min(ore / bp[OBSIDIAN_ROBOT_COST_ORE], clay / bp[OBSIDIAN_ROBOT_COST_CLAY])
        val maxGeode = min(ore / bp[GEODE_ROBOT_COST_ORE], obsidian / bp[GEODE_ROBOT_COST_OBSIDIAN])

        val max = listOfNotNull(
            opt(bp, time - 1, nOre, nClay, nObsidian, nGeode, oreRobots, clayRobots, obsidianRobots, geodeRobots),
            if (maxOre > 0 && oreRobots < maxSpendOre) opt(
                bp,
                time - 1,
                nOre - bp[ORE_ROBOT_COST_ORE],
                nClay,
                nObsidian,
                nGeode,
                oreRobots + 1,
                clayRobots,
                obsidianRobots,
                geodeRobots
            ) else null,
            if (maxClay > 0 && clayRobots < bp[OBSIDIAN_ROBOT_COST_CLAY]) opt(
                bp,
                time - 1,
                nOre - bp[CLAY_ROBOT_COST_ORE],
                nClay,
                nObsidian,
                nGeode,
                oreRobots,
                clayRobots + 1,
                obsidianRobots,
                geodeRobots
            ) else null,
            if (maxObsidian > 0 && obsidianRobots < bp[GEODE_ROBOT_COST_OBSIDIAN]) opt(
                bp,
                time - 1,
                nOre - bp[OBSIDIAN_ROBOT_COST_ORE],
                nClay - bp[OBSIDIAN_ROBOT_COST_CLAY],
                nObsidian,
                nGeode,
                oreRobots,
                clayRobots,
                obsidianRobots + 1,
                geodeRobots
            ) else null,
            if (maxGeode > 0) opt(
                bp,
                time - 1,
                nOre - bp[GEODE_ROBOT_COST_ORE],
                nClay,
                nObsidian - bp[GEODE_ROBOT_COST_OBSIDIAN],
                nGeode,
                oreRobots,
                clayRobots,
                obsidianRobots,
                geodeRobots + 1
            ) else null,
        ).max()

        dp[key] = max

        return max
    }

    fun part1A(): Any? {
        return bps.sumOf { bp ->
            println("Checking $bp")
            dp.clear()
            val max = opt(bp, 24)

            (max.also { println("Yields $it") } * bp[id]).also { println("QL is $it") }
        }
    }

}

fun main() {
    solve<Day19> {
        """
            Blueprint 1: Each ore robot costs 4 ore. Each clay robot costs 2 ore. Each obsidian robot costs 3 ore and 14 clay. Each geode robot costs 2 ore and 7 obsidian.
            Blueprint 2: Each ore robot costs 2 ore. Each clay robot costs 3 ore. Each obsidian robot costs 3 ore and 8 clay. Each geode robot costs 3 ore and 12 obsidian.
        """.trimIndent() part1 33
    }
}