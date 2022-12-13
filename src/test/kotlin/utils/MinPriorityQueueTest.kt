package utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll


class MinPriorityQueueTest : StringSpec({

    "an empty queue behaves correctly" {
        minPriorityQueueOf<String>().shouldBeEmpty()
        minPriorityQueueOf<String>() shouldHaveSize 0
    }

    "insertion works and will offer in right order" {
        val q = minPriorityQueueOf("a" to 2, "b" to 1)
        q.extractMin() shouldBe "b"
        q.extractMin() shouldBe "a"
        q.shouldBeEmpty()
    }

    "priority can be changed" {
        val q = minPriorityQueueOf("a" to 2, "b" to 1)
        q.insertOrUpdate("c", 99)
        q.getPriorityOf("c") shouldBe 99
        q.decreasePriority("c", 20)
        q.getPriorityOf("c") shouldBe 20
    }

    "insert and remove in right order works" {

        checkAll<List<String>> { arbList ->
            val q = minPriorityQueueOf<String>()
            arbList.forEach { q.insertOrUpdate(it, it.hashCode()) }

            val sorted = arbList.distinct().sortedBy { it.hashCode() }
            q.toList() shouldBe sorted
        }

        val arbStringWithPrio: Arb<Pair<String, Int>> = Arb.bind(
            Arb.string(), Arb.int(-10..10)
        ) { s, p -> s to p }

        val a = Arb.list(Arb.string()).flatMap { s ->
            Arb.list(Arb.int(-10..10), range = s.size..s.size).map { s to it }
        }

    }

})