package utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class RangeExtensionsTest : StringSpec({

    "overlaps for non overlapping or touching ranges is false" {
        listOf(
            (1..10) to (12..20),
            (1..10) to (11..20),
        ).forEach{ (a,b)->
            (a overlaps b).shouldBeFalse()
            (b overlaps a).shouldBeFalse()
        }
    }

    "overlaps for overlapping ranges is true" {
        listOf(
            (1..10) to (10..20),
        ).forEach{ (a,b)->
            (a overlaps b).shouldBeTrue()
            (b overlaps a).shouldBeTrue()
        }
    }

    "overlaps for ranges within each other is true" {
        listOf(
            (1..10) to (5..6),
        ).forEach{ (a,b)->
            (a overlaps b).shouldBeTrue()
            (b overlaps a).shouldBeTrue()
        }
    }

    "merging of IntRanges" {
        val ranges = listOf(1..3, 2..4, 6..8, 9..10)

        ranges.merge() shouldBe listOf(1..4, 6..10)
    }

    "subtract ranges from one range (aka 'poking holes')" {
        (1..10).minus(5..8) shouldBe listOf(1..4, 9..10)
        (1..10).subtract(7..100) shouldBe listOf(1..6)
        (1..10).subtract(-7..2) shouldBe listOf(3..10)

        (1..10).subtract(-10..0) shouldBe listOf(1..10)
        (1..10).subtract(15..100) shouldBe listOf(1..10)

        (1..10).subtract(-10..100) shouldBe emptyList()

        (1..10).subtract(2..3, 7..8) shouldBe listOf(1..1, 4..6, 9..10)
    }

})