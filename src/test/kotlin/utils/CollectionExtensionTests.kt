package utils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll

class CollectionExtensionTests : ShouldSpec({

    context("Iterable.minN and Iterable.maxN") {
        should("match less efficient sorted().take(n) result") {
            val scenario = arbitrary {
                val list = Arb.list(Arb.int(-100..100).map { Data(it) }).bind()
                val n = Arb.int(0..list.size).bind()
                list to n
            }

            checkAll(scenario) { (list, n) ->
                val sortedAscending = list.sortedBy { it.v }
                list.minN(n) shouldBe sortedAscending.take(n)

                val sortedDescending = list.sortedByDescending { it.v }
                list.maxN(n) shouldBe sortedDescending.take(n)
            }
        }
    }

    context("Iterable.splitBy") {
        should("not split if no delimiter is found") {
            (1..10).toList().splitBy { it == 0 } shouldBe listOf((1..10).toList())
        }
        should("split at delimiter") {
            listOf(null, 1, 2, 3, null, 4, 5, 6, null, null, 7, null, 8, null, 9).splitByNulls() shouldBe
                    listOf(emptyList(), listOf(1, 2, 3), listOf(4, 5, 6), emptyList(), listOf(7), listOf(8), listOf(9))
        }
        should("allow to remove empty groups") {
            listOf(null, 1, 2, 3, null, 4, 5, 6, null, null, 7, null, 8, null, 9)
                .splitByNulls(keepEmpty = false) shouldBe
                    listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7), listOf(8), listOf(9))
        }
        should("have correct size and remove all delimiting elements") {
            checkAll(
                Arb.list(Arb.int(-10..10), 0..20),
                Arb.int(-10..11)
            ) { list, d ->
                val delimiters = list.count { it == d }

                val splitBy = list.splitBy { it == d }
                splitBy should {
                    it shouldHaveSize delimiters + 1
                    it.flatten() shouldNotContain d
                }

                (1..delimiters).forEach { limit ->
                    list.splitBy(limit = limit) { it == d } should {
                        it shouldHaveSize limit
                        it.dropLast(1).forAll { g -> g shouldNotContain d }
                        it.last() shouldContain d
                    }
                }

                list.splitBy(keepEmpty = false) { it == d } should {
                    it.size shouldBeLessThanOrEqual splitBy.size
                    it.forAll { g -> g.shouldNotBeEmpty() }
                    it.flatten() shouldHaveSize list.size - delimiters

                    (1 until it.size).forEach { limit ->
                        list.splitBy(limit = limit, keepEmpty = false) { it == d } should {
                            it shouldHaveSize limit
                            it.forAll { g -> g.shouldNotBeEmpty() }
                            it.dropLast(1).forAll { g -> g shouldNotContain d }
                            it.last() shouldContain d
                        }
                    }
                }
            }
        }
        should("behave exactly like String.split") {
            val scenario = arbitrary {
                val s = Arb.string(0..20, Codepoint.alphanumeric()).bind()
                s to if (s.isNotEmpty()) s[Arb.int(s.indices).bind()] else Arb.char('a'..'z').bind()
            }
            checkAll(scenario) { (s, d) ->
                val l = s.toList()
                l.splitBy { it == d } shouldBe s.split(d).map { it.toList() }
            }
        }
    }

})

private class Data(val v: Int) : Comparable<Data> {
    override fun compareTo(other: Data): Int = v.compareTo(other.v)
    override fun toString() = v.toString()
}
