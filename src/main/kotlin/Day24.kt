import utils.*

class Day24 : Day(24, 2022) {

    val map = inputAsGrid


    override fun part1(): Any? {
        val wb = map.searchIndices('<').toList()
        val eb = map.searchIndices('>').toList()
        val nb = map.searchIndices('^').toList()
        val sb = map.searchIndices('v').toList()

        val pos = map.indexOfOrNull('.')!!
        println(pos)
        val goal = map.area.allPoints().toList().reversed().first { map[it] == '.' }
        check(map[goal] == '.')
        val area = map.area
        val w = area.width - 2
        val h = area.height - 2
        log { goal }
        log { area }
        val repeats = lcm(w, h)
        log { repeats }

        fun noBlizzard(pos: Point, time: Int): Boolean =
            wb.filter { it.y == pos.y }.none { pos.x == (((it.x - 1) - time).mod(w)) + 1 } &&
                    eb.filter { it.y == pos.y }.none { pos.x == (((it.x - 1) + time).mod(w)) + 1 } &&
                    nb.filter { it.x == pos.x }.none { pos.y == (((it.y - 1) - time).mod(h)) + 1 } &&
                    sb.filter { it.x == pos.x }.none { pos.y == (((it.y - 1) + time).mod(h)) + 1 }

        fun neighbors(pos: Pair<Point, Int>) = (pos.first.directNeighbors(area) + pos.first).filter {
            map[it] != '#' && noBlizzard(it, pos.second + 1)
        }.map { it to pos.second + 1 }


        val q = ArrayDeque<Pair<Point, Int>>()
        q.add(pos to 0)
        val seen = mutableSetOf<Pair<Point, Int>>()
        while (q.isNotEmpty()) {
            val (p, t) = q.removeFirst()
            if (p == goal) return t

            if (!seen.add(p to t % repeats)) continue
            log { seen.size }

            val n = neighbors(p to t)
            log { "from $p at $t we have $n" }
            log {
                MutableGrid(area.width, area.height) { pos ->
                    when (pos) {
                        p -> 'E'
                        else -> {
                            val c = wb.filter { it.y == pos.y }.count { pos.x == (((it.x - 1) - t).mod(w)) + 1 } +
                                    eb.filter { it.y == pos.y }.count { pos.x == (((it.x - 1) + t).mod(w)) + 1 } +
                                    nb.filter { it.x == pos.x }.count { pos.y == (((it.y - 1) - t).mod(h)) + 1 } +
                                    sb.filter { it.x == pos.x }.count { pos.y == (((it.y - 1) + t).mod(h)) + 1 }
                            if (c > 1) '0' + c else {
                                if (wb.filter { it.y == pos.y }.any { pos.x == (((it.x - 1) - t).mod(w)) + 1 }) '<'
                                else if (eb.filter { it.y == pos.y }.any { pos.x == (((it.x - 1) + t).mod(w)) + 1 }) '>'
                                else if (nb.filter { it.x == pos.x }.any { pos.y == (((it.y - 1) - t).mod(w)) + 1 }) '^'
                                else if (nb.filter { it.x == pos.x }.any { pos.y == (((it.y - 1) + t).mod(w)) + 1 }) 'v'
                                else '.'
                            }
                        }
                    }
                }.formatted()
            }
            q += n
        }
        return null
    }
}

fun main() {
    solve<Day24>(true) {

        """
            #.######
            #>>.<^<#
            #.<..<<#
            #>v.><>#
            #<^v^^>#
            ######.#
        """.trimIndent() part1 18 part2 54

    }
}