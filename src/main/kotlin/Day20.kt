import kotlin.math.absoluteValue

class Day20 : Day(20, 2022) {

    val p = inputAsLongs

    data class V(val v: Long, var prev: V?, var originalNext: V?, var next: V?)

    override fun part1(): Any? {
        println(p.size)
        println(p.distinct().size)

        val s = p.size

        val begin = V(p.first(), null, null, null)
        var c = begin
        p.drop(1).forEach { n ->
            val nItem = V(n, c, null, null)
            c.next = nItem
            c.originalNext = nItem

            c = nItem
        }
        c.next = begin
        c.originalNext = begin

        begin.prev = c
        c = begin
        repeat(s) {
            log { "${c.prev!!.v}  -  ${c.v}  - ${c.next!!.v}" }
            c.originalNext = c.next
            c = c.next!!
        }
        repeat(s) {
            check(c.next!!.prev!! == c) { "WTF at ${c.v}" }
            c = c.next!!
        }
        repeat(s) {
//            log {
//                var x = c
//                (1..s).joinToString { "${x.v}".also { x = x.next!! } }
//            }

            val v = c.v
            val r = fuckingR(v, s)
            repeat(s) {
                check(c.next!!.prev!! == c)
                c = c.next!!
            }
            if (r != 0L) {
                log {
                    var x = c
                    (1..s).joinToString { "${x.v}".also { x = x.next!! } }
                }
                log {
                    repeat(s) {
                        check(c.next!!.prev!! == c)
                        c = c.next!!
                    }
                    "Moving $v ($r) forward..."
                }
                var t = c
                repeat(r.toInt()) {
                    t = if (v > 0) t.next!! else t.prev!!
                }
                // detach from oldPos
                val oldP = c.prev!!
                val oldN = c.next!!
                oldP.next = oldN
                oldN.prev = oldP

                val tN = t.next!!
                t.next = c
                c.prev = t

                tN.prev = c
                c.next = tN
            }

            c = c.originalNext!!
        }
        log { "Done" }
        log {
            var x = c
            (1..s).joinToString { "${x.v}".also { x = x.next!! } }
        }

        while (c.v != 0L) {
            c = c.next!!
        }
        val j = 1000 % s
        repeat(j) { c = c.next!! }
        val a1 = c.v
        repeat(j) { c = c.next!! }
        val a2 = c.v
        repeat(j) { c = c.next!! }
        val a3 = c.v

        return a1 + a2 + a3
    }



    override fun part2(): Any? {

        val key = 811589153L
        val s = p.size

        val begin = V(p.first()*key, null, null, null)
        var c = begin
        p.drop(1).forEach { n ->
            val nItem = V(n*key, c, null, null)
            c.next = nItem
            c.originalNext = nItem

            c = nItem
        }
        c.next = begin
        c.originalNext = begin

        begin.prev = c

        repeat(s) {
            log { "${c.prev!!.v}  -  ${c.v}  - ${c.next!!.v}" }
            c.originalNext = c.next
            c = c.next!!
        }
        repeat(10) { round->
            c = begin
            repeat(s) {
                val v = c.v
                val r = fuckingR(v, s)
                repeat(s) {
                    check(c.next!!.prev!! == c)
                    c = c.next!!
                }
                if (r != 0L) {

                    log {
                        repeat(s) {
                            check(c.next!!.prev!! == c)
                            c = c.next!!
                        }
//                        "Moving $v ($r) forward..."
                    }
                    var t = c
                    repeat(r.toInt()) {
                        t = if (v > 0) t.next!! else t.prev!!
                    }
                    // detach from oldPos
                    val oldP = c.prev!!
                    val oldN = c.next!!
                    oldP.next = oldN
                    oldN.prev = oldP

                    val tN = t.next!!
                    t.next = c
                    c.prev = t

                    tN.prev = c
                    c.next = tN
                }

                c = c.originalNext!!
            }
            log { "Done $round" }
            log {
                var x = c
                (1..s).joinToString { "${x.v}".also { x = x.next!! } }
            }
        }
        while (c.v != 0L) {
            c = c.next!!
        }
        val j = 1000 % s
        repeat(j) { c = c.next!! }
        val a1 = c.v
        repeat(j) { c = c.next!! }
        val a2 = c.v
        repeat(j) { c = c.next!! }
        val a3 = c.v

        return a1 + a2 + a3
    }

}

private fun fuckingR(v: Long, s: Int) = if (v >= 0)
    v % (s - 1)
else
        -v % (s-1) +1
//    (v.absoluteValue % s + 1) % s

fun main() {

//    (-6..6).forEach {v->
//        val s = 4
//        val r = fuckingR(v.toLong(), s)
//
//        println("$v -> $r")
//    }
//    return
//


    solve<Day20>(true) {

        """
            1
            2
            -3
            3
            -2
            0
            4
        """.trimIndent() part1 3 part2 1623178306

    }
}