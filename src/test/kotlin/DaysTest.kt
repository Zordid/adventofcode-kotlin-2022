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
        test<Day07>(1118405, 12545514)
        test<Day08>(1703, 496650)
        test<Day09>(5619, 2376)
        test<Day10>(
            14520, """
            |###  #### ###   ##  #### ####   ## ###  
            |#  #    # #  # #  #    # #       # #  # 
            |#  #   #  ###  #      #  ###     # ###  
            |###   #   #  # # ##  #   #       # #  # 
            |#    #    #  # #  # #    #    #  # #  # 
            |#    #### ###   ### #### ####  ##  ###  
        """.trimMargin()
        )
        test<Day11>(56350, 13954061248)
        test<Day12>(447, 446)
        test<Day13>(6420, 22000)
        test<Day14>(913, 30762)
        test<Day15>(4985193, 11583882601918)
        test<Day16>(1638, 2400)
        test<Day17>(3227, 1597714285698)
        test<Day18>(4310, 2466)
        test<Day19>(1349, 21840)
        test<Day20>(2275, 4090409331120)
        test<Day21>(121868120894282, 3582317956029)
        test<Day22>(109094, 53324)
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
                { create(D::class).part1.toString().trim() shouldBe "$expectedPart1".trim() }
            },
            expectedPart2?.let {
                DynamicTest.dynamicTest("${D::class.simpleName} - Part 2")
                { create(D::class).part2.toString().trim() shouldBe "$expectedPart2".trim() }
            })
    }

    fun build(): List<DynamicTest> = tests

}

