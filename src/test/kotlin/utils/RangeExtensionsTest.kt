package utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RangeExtensionsTest : StringSpec({

    "merging of IntRanges" {

        val ranges = listOf(1..3, 2..4, 6..8, 9..10)

        ranges.merge() shouldBe listOf(1..4, 6..10)

    }


})