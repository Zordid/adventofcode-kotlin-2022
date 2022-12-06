import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class DaysTest {

    @TestFactory
    fun `AoC 2022`() = aocTests {
        test<Day01>(67622, 201491)
        test<Day02>(15523, 15702)
        test<Day03>(7824, 2798)
        test<Day04>(448, 794)
        test<Day05>("VJSFHWGFT", "LCTQFBVZV")
        test<Day06>(1142, 2803)
//        test<Day07>(337488, 89647695)
//        test<Day08>(239, 946346)
//        test<Day09>(512, 1600104)
//        test<Day10>(344193, 3241238967)
//        test<Day11>(1747, 505)
//        test<Day12>(5212, 134862)
//        test<Day13>(
//            592, """
//                    |  ##  ##   ##    ## #### #### #  # #  #
//                    |   # #  # #  #    # #    #    # #  #  #
//                    |   # #    #  #    # ###  ###  ##   #  #
//                    |   # # ## ####    # #    #    # #  #  #
//                    |#  # #  # #  # #  # #    #    # #  #  #
//                    | ##   ### #  #  ##  #### #    #  #  ##
//                """.trimMargin()
//        )
//        test<Day14>(3555, 4439442043739)
//        test<Day15>(755, 3016)
//        test<Day16>(938, 1495959086337)
//        test<Day17>(11175, 3540)
//        test<Day18>(4176, 4633)
//        test<Day19>(419, 13210)
//        test<Day20>(5291, 16665)
//        test<Day21>(713328, 92399285032143)
//        test<Day22>(546724, 1346544039176841)
    }

}

private fun aocTests(builder: AoCTestBuilder.() -> Unit): List<DynamicTest> =
    AoCTestBuilder().apply(builder).build().also { verbose = false }

private class AoCTestBuilder {

    private val tests = mutableListOf<DynamicTest>()

    inline fun <reified D : Day> test(expectedPart1: Any? = null, expectedPart2: Any? = null) {
        tests += listOfNotNull(
            expectedPart1?.let {
                DynamicTest.dynamicTest("${D::class.simpleName} - Part 1")
                { create(D::class).part1.toString() shouldBe expectedPart1.toString() }
            },
            expectedPart2?.let {
                DynamicTest.dynamicTest("${D::class.simpleName} - Part 2")
                { create(D::class).part2.toString() shouldBe expectedPart2.toString() }
            })
    }

    fun build(): List<DynamicTest> = tests

}

