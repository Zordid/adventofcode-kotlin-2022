package utils

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class CircularListTest : StringSpec({

    "an empty list cannot be created" {
        shouldThrowAny { emptyList<Int>().toCircularList() }
    }

    "a list with one elements can be created" {
        listOf("anything").toCircularList() should {
            it.count() shouldBeExactly 1
            it.first.value shouldBe "anything"
            it.first.next shouldBe it.first
            it.first.prev shouldBe it.first

            it.check()
        }
    }

    "elements are linked correctly" {
        listOf(1, 2, 3).toCircularList() should {
            it.count() shouldBeExactly 3

            it.first.value shouldBeExactly 1
            it.first.next.value shouldBeExactly 2
            it.first.next.next.value shouldBeExactly 3

            it.first.value shouldBeExactly 1
            it.first.prev.value shouldBeExactly 3
            it.first.prev.prev.value shouldBeExactly 2
            it.check()
        }
    }

    "element in the middle can be removed" {
        val cl = listOf(1, 2, 3).toCircularList()
        cl.remove(cl.first.next)
        cl should {
            it.count() shouldBeExactly 2
            it.first.value shouldBeExactly 1
            it.first.next.value shouldBeExactly 3
            it.first.next.next.value shouldBeExactly 1

            it.first.value shouldBeExactly 1
            it.first.prev.value shouldBeExactly 3
            it.first.prev.prev.value shouldBeExactly 1
            it.check()
        }
    }

    "first element can be removed" {
        val cl = listOf(1, 2, 3).toCircularList()
        cl.remove(cl.first)
        cl should {
            it.count() shouldBeExactly 2
            it.first.value shouldBeExactly 2
            it.first.next.value shouldBeExactly 3
            it.first.next.next.value shouldBeExactly 2

            it.first.value shouldBeExactly 2
            it.first.prev.value shouldBeExactly 3
            it.first.prev.prev.value shouldBeExactly 2
            it.check()
        }
    }

    "cannot remove last element" {
        val cl = listOf(1, 2, 3).toCircularList()
        cl.remove(cl.first)
        cl.remove(cl.first)
        shouldThrowAny { cl.remove(cl.first) }
        cl.check()
    }

    "can insert new element after a previous" {
        val cl = listOf(1, 2, 3).toCircularList()
        cl.insertAfter(cl.first, 99)
        cl should {
            it.count() shouldBeExactly 4
            it.first.value shouldBeExactly 1
            it.first.next.value shouldBeExactly 99
            it.first.next.next.value shouldBeExactly 2

            it.first.value shouldBeExactly 1
            it.first.prev.value shouldBeExactly 3
            it.first.prev.prev.value shouldBeExactly 2
            it.first.prev.prev.prev.value shouldBeExactly 99
            it.check()
        }
    }

    "can insert new element after a previous in a list of one" {
        val cl = listOf(1).toCircularList()
        cl.insertAfter(cl.first, 99)
        cl should {
            it.count() shouldBeExactly 2
            it.first.value shouldBeExactly 1
            it.first.next.value shouldBeExactly 99
            it.first.next.next.value shouldBeExactly 1
            it.check()
        }
    }

    "can insert new element before a previous" {
        val cl = listOf(1, 2, 3).toCircularList()
        cl.insertBefore(cl.first, 99)
        cl should {
            it.count() shouldBeExactly 4
            it.first.value shouldBeExactly 1
            it.first.prev.value shouldBeExactly 99
            it.first.prev.prev.value shouldBeExactly 3

            it.first.value shouldBeExactly 1
            it.first.next.value shouldBeExactly 2
            it.first.next.next.value shouldBeExactly 3
            it.first.next.next.next.value shouldBeExactly 99
            it.check()
        }
    }

    "can swap two values" {
        val cl = listOf(1, 2, 3, 4).toCircularList()
        cl should {
            val one = cl.first
            val two = one.next
            val three = two.next
            val four = three.next
            three.value shouldBeExactly 3
            cl.swap(one, three)

            cl.entries.map { it.value } shouldBe listOf(3, 2, 1, 4)
            cl.toString() shouldBe "[3, 2, 1, 4]"

            cl.swap(two, four)
            cl.toString() shouldBe "[3, 4, 1, 2]"

            cl.swap(two, three)
            cl.toString() shouldBe "[2, 4, 1, 3]"
            it.check()
        }
    }

})

private fun CircularList<*>.check() {
    var c = first

    repeat(size) {
        check(c.next.prev == c) { "$c: next.prev != $c, but ${c.next.prev}" }
        check(c.prev.next == c) { "$c: prev.next != $c, but ${c.prev.next}" }
        c = c.next
    }
    check(c == first) { "size problem? started at $first, now at $c" }
}