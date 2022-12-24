import utils.*

class Day24 : Day(24, 2022, "Blizzard Basin") {

    val map = inputAsGrid
    val area = map.area
    val repeats = lcm(area.width - 2, area.height - 2)
    val start = map.indexOfOrNull('.')!!
    val destination = map.area.allPointsReversed().first { map[it] == '.' }

    val horizontal =
        (map.searchIndices('<').map { it to -1 } + map.searchIndices('>').map { it to +1 })
            .groupBy({ (p, _) -> p.y }) { (p, d) -> p.x to d }

    val vertical =
        (map.searchIndices('^').map { it to -1 } + map.searchIndices('v').map { it to +1 })
            .groupBy({ (p, _) -> p.x }) { (p, d) -> p.y to d }

    override fun part1(): Any? {
        val q = ArrayDeque<Pair<Point, Int>>()
        q.add(start to 0)
        val seen = mutableSetOf<Pair<Point, Int>>()
        while (q.isNotEmpty()) {
            val (p, t) = q.removeFirst()
            if (p == destination) return t
            if (!seen.add(p to t % repeats)) continue
            q += neighbors(p, t)
        }
        return null
    }

    override fun part2(): Any? {
        val q = ArrayDeque<Pair<Point, Int>>()
        val seen = mutableSetOf<Pair<Point, Int>>()
        var trip = 0
        var goal = destination

        fun setGoal(pos: Point, currentPos: Point, currentTime: Int) {
            trip++
            seen.clear()
            q.clear()
            q.add(currentPos to currentTime)
            goal = pos
        }

        setGoal(destination, start, 0)
        while (q.isNotEmpty()) {
            val (p, t) = q.removeFirst()
            if (p == goal)
                when (trip) {
                    1 -> setGoal(start, p, t)
                    2 -> setGoal(destination, p, t)
                    3 -> return t
                }
            if (!seen.add(p to t % repeats)) continue

            q += neighbors(p, t)
        }
        return null
    }

    val w = area.width - 2
    val h = area.height - 2

    fun noBlizzard(pos: Point, time: Int): Boolean =
        horizontal[pos.y]?.none { pos.x == (((it.first - 1) + it.second * time).mod(w)) + 1 } ?: true &&
                vertical[pos.x]?.none { pos.y == (((it.first - 1) + it.second * time).mod(h)) + 1 } ?: true

    fun neighbors(pos: Point, time: Int) = (pos.directNeighbors(area) + pos).filter {
        map[it] != '#' && noBlizzard(it, time + 1)
    }.map { it to time + 1 }

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