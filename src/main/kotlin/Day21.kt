import utils.gcd
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4
import kotlin.collections.set

class Day21 : Day(21, 2022, "Monkey Math") {

    val p = input.map {
        val n = it.substringBefore(':')
        val num = it.extractAllLongs()
        if (num.isNotEmpty())
            n to num.single()
        else {
            val (_, a, op, b) = it.split(" ")
            n to Triple(a, op, b)
        }
    }.show()


    override fun part1(): Long? {

        val known = p.filter { it.second is Long }.associate { it.first to it.second as Long }.toMutableMap()
        val op = p.filter { it.second is Triple<*, *, *> }
            .associate { it.first to it.second as Triple<String, String, String> }.toMutableMap()

        while ("root" !in known && op.isNotEmpty()) {
            val keys = op.keys.toList()
            for (name in keys) {
                val (an, o, bn) = op[name]!!
                val v = known[name]
                val a = known[an] ?: an.toLongOrNull()
                val b = known[bn] ?: bn.toLongOrNull()
                if (listOf(v, a, b).count { it != null } == 2) {
                    if (a != null && b != null) {
                        known[name] = calc(a, o, b)
                    } else if (b != null && v!=null) {
                        // <value> = a +-*/ <value>
                        known[an] = when (o) {
                            "+" -> calc(v, "-", b)
                            "-" -> calc(v, "+", b)
                            "*" -> calc(v, "/", b)
                            "/" -> calc(v, "*", b)
                            else -> error("")
                        }
                    } else if (a!=null && v!=null){
                        // <value> = <value> +-*/ b
                        known[bn] = when (o) {
                            "+" -> calc (v, "-", a)
                            "-" -> -calc(v, "-", a)
                            "*" -> calc (v, "/", a)
                            "/" -> calc(a, "/", v)
                            else -> error("")
                        }
                    }
                    op.remove(name)
                }
            }
        }

        return known["root"]
    }

    override fun part2(): Long {

        val known = p.filter { it.second is Long }.associate { it.first to it.second as Long }.toMutableMap()
        val op = p.filter { it.second is Triple<*, *, *> }
            .associate { it.first to it.second as Triple<String, String, String> }.toMutableMap()

        val (r1, _, r2) = op["root"]!!
        op.remove("root")

        known.remove("humn")


        while ("root" !in known && op.isNotEmpty()) {
            var goon = false
            val keys = op.keys.toList()
            for (name in keys) {
                val (an, o, bn) = op[name]!!
                val a = known[an] ?: an.toLongOrNull()
                val b = known[bn] ?: bn.toLongOrNull()
                if (a != null && b != null) {
                    goon = true
                    known[name] = calc(a, o, b)
                    op.remove(name)
                } else if (a != null) {
                    op[name] = Triple(a.toString(), o, bn)
                } else if (b != null) {
                    op[name] = Triple(an, o, b.toString())
                }

            }
            if (!goon) {
                alog { "We are stuck!" }
                break

            }
        }

        fun op(n: String): String =
            known[n]?.toString() ?: n.toLongOrNull()?.let { "$it" } ?: op[n]?.let { (a, o, b) ->
                "(${op(a)} $o ${op(b)})"
            } ?: n

        alog { "${op(r1)} = ${op(r2)}" }

        val knownValue = known[r1] ?: known[r2]!!
        val unknown = op[r1] ?: op[r2]!!

        fun resolve(what: String, o: Triple<String, String, String>, v: Rational): Rational {
            alog { "$o = $v" }
            o.third.toLongOrNull()?.let { va ->
                val na = o.first
                val nv = calc(v, o.second.inv, va to 1)
                alog { "==> $na = $nv" }
                if (what == na) return nv

                val newOp = op[na]!!
                return resolve(what, newOp, nv)
            }
            o.first.toLongOrNull()?.let { va ->
                val na = o.third
                val nv = when (o.second) {
                    "+", "*" -> calc(v, o.second.inv, va to 1)
                    "-" -> calc(v, o.second, va to 1).let { -it.first to it.second }
                    "/" -> calc(v, o.second, va to 1).let { it.second to it.first }
                    else -> error("")
                }
                alog { "==> $na = $nv" }
                if (what == na) return nv

                val newOp = op[na] ?: error("missing operation for $na")
                return resolve(what, newOp, nv)
            }
            error("")
        }

        val r = resolve("humn", unknown, knownValue to 1L)

        return r.first / r.second
    }

    val String.inv
        get() = when (this) {
            "+" -> "-"
            "-" -> "+"
            "*" -> "/"
            "/" -> "*"
            else -> error(this)
        }

    private fun calc(a: Long, op: String, b: Long): Long {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> (a / b).also { check(a % b == 0L) }
            else -> error(op)
        }
    }
}

private typealias Rational = Pair<Long, Long>

fun Rational.shorten(): Rational =
    gcd(first, second).let { (first / it) to (second / it) }

fun calc(a: Rational, op: String, b: Rational): Rational =
    when (op) {
        "+" -> (a.first * b.second + b.first * a.second) to (a.second * b.second)
        "-" -> (a.first * b.second - b.first * a.second) to (a.second * b.second)
        "*" -> (a.first * b.first) to (a.second * b.second)
        "/" -> (a.first * b.second) to (a.second * b.first)
        else -> error(op)
    }.shorten()

fun main() {
    solve<Day21> {
        """
            root: pppw + sjmn
            dbpl: 5
            cczh: sllz + lgvd
            zczc: 2
            ptdq: humn - dvpt
            dvpt: 3
            lfqf: 4
            humn: 5
            ljgn: 2
            sjmn: drzm * dbpl
            sllz: 4
            pppw: cczh / lfqf
            lgvd: ljgn * ptdq
            drzm: hmdt - zczc
            hmdt: 32
        """.trimIndent() part1 152 part2 301

    }
}