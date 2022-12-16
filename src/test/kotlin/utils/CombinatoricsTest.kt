package utils

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test

internal class CombinatoricsTest {

    @Test
    fun `simple combinations of 2 elements`() {
        "a".combinations(2).toList().shouldBeEmpty()
        "ab".combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    "ab"
                )
        "abc".combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    "ab",
                    "ac",
                    "bc",
                )
        "abcd".combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    "ab",
                    "ac",
                    "ad",
                    "bc",
                    "bd",
                    "cd",
                )
    }

    @Test
    fun `simple combinations of IntRanges`() {
        (1..1).combinations(2).toList().shouldBeEmpty()
        (1..2).combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    listOf(1, 2)
                )
        (1..3).combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    listOf(1, 2),
                    listOf(1, 3),
                    listOf(2, 3),
                )
        (1..4).combinations(2).toList() shouldContainExactlyInAnyOrder
                listOf(
                    listOf(1, 2),
                    listOf(1, 3),
                    listOf(1, 4),
                    listOf(2, 3),
                    listOf(2, 4),
                    listOf(3, 4),
                )
    }

    @Test
    fun `test permutations of elements`() {
        "".permutations().toList().shouldBeEmpty()

        "a".permutations().toList() shouldContainExactlyInAnyOrder
                listOf("a")

        "ab".permutations().toList() shouldContainExactlyInAnyOrder
                listOf("ab", "ba")

        "aa".permutations().toList() shouldContainExactlyInAnyOrder
                listOf("aa", "aa")

        "abc".permutations().toList() shouldContainExactlyInAnyOrder
                listOf(
                    "abc",
                    "acb",
                    "bac",
                    "cab",
                    "bca",
                    "cba"
                )
    }

    @Test
    fun `test permutations of IntRange`() {
        @Suppress("EmptyRange")
        (1..0).permutations().toList().shouldBeEmpty()

        (10..10).permutations().toList() shouldContainExactlyInAnyOrder
                listOf(listOf(10))

        (41..42).permutations().toList() shouldContainExactlyInAnyOrder
                listOf(listOf(41, 42), listOf(42, 41))

        (100..102).permutations().toList() shouldContainExactlyInAnyOrder
                listOf(
                    listOf(100, 101, 102),
                    listOf(100, 102, 101),
                    listOf(101, 100, 102),
                    listOf(102, 100, 101),
                    listOf(101, 102, 100),
                    listOf(102, 101, 100),
                )
    }

}