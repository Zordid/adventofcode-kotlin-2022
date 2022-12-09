@file:Suppress("unused")

package utils

typealias Grid<T> = List<List<T>>
typealias MutableGrid<T> = List<MutableList<T>>

fun <T> Grid<T>.toMutableGrid(): MutableGrid<T> = List(size) { this[it].toMutableList() }

fun <T> Grid<T>.fixed(default: T): Grid<T> {
    val (min, max) = asSequence().map { it.size }.minMaxOrNull() ?: return this
    if (min == max) return this
    return map { row ->
        row.takeIf { row.size == max } ?: List(max) { idx -> if (idx < row.size) row[idx] else default }
    }
}

val Grid<*>.area: Area get() = origin to (first().size - 1 to size - 1)

inline fun <T> Grid<T>.searchIndices(crossinline predicate: (T) -> Boolean): Sequence<Point> =
    area.allPoints().filter { getOrNull(it)?.let(predicate) ?: false }

fun <T> Grid<T>.searchIndices(element: T): Sequence<Point> =
    searchIndices { it == element }

@JvmName("areaString")
fun List<String>.area(): Area = origin to (first().length - 1 to size - 1)

fun Grid<*>.indices(): Sequence<Point> = sequence {
    for (y in this@indices.indices) {
        for (x in this@indices[y].indices)
            yield(x to y)
    }
}

inline fun <T> Grid<T>.forArea(f: (p: Point, v: T) -> Unit) {
    for (y in this.indices)
        for (x in this[y].indices)
            f(x to y, this[y][x])
}

inline fun <T> Grid<T>.forArea(f: (p: Point) -> Unit) {
    for (y in this.indices)
        for (x in this[y].indices)
            f(x to y)
}

fun <T> Grid<T>.toMapGrid(sparseElement: T? = null): Map<Point, T> =
    buildMap { forArea { p, v -> if (v != sparseElement) this[p] = v } }

fun <T, R> Grid<T>.mapValues(transform: (T) -> R): Grid<R> =
    map { it.map(transform) }

fun <T, R> Grid<T>.mapValuesIndexed(transform: (Point, T) -> R): Grid<R> =
    mapIndexed { y, r -> r.mapIndexed { x, v -> transform(x to y, v) } }

fun <T> Grid<T>.println(transform: (Point, T) -> String = { _, value -> value.toString() }) {
    println(withIndex().joinToString(System.lineSeparator()) { (y, row) ->
        row.withIndex().joinToString("") { (x, value) -> transform(x to y, value) }
    })
}

operator fun <T> Grid<T>.get(p: Point): T =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x] else error("Point $p not in grid of area $area")

fun <T> Grid<T>.getOrNull(p: Point): T? =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x] else null

inline fun <T> Grid<T>.getOrElse(p: Point, default: (Point) -> T): T =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x] else default(p)

operator fun <T> MutableGrid<T>.set(p: Point, v: T) {
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x] = v
    else error("Point $p not in grid of area $area")
}

operator fun List<String>.get(p: Point): Char =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x] else error("Point $p not in grid of area ${area()}")

fun List<String>.getOrNull(p: Point): Char? =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x] else null

fun List<String>.getOrElse(p: Point, default: (Point) -> Char): Char =
    if (p.y in indices && p.x in this[p.y].indices) this[p.y][p.x] else default(p)

fun <T> Grid<T>.matchingIndices(predicate: (T) -> Boolean): List<Point> =
    flatMapIndexed { y, l -> l.mapIndexedNotNull { x, item -> if (predicate(item)) x to y else null } }

class InfiniteList<T>(private val backingList: List<T>) : List<T> by backingList {
    init {
        require(backingList.isNotEmpty()) { "Cannot build an ${this::class.simpleName} from an empty list" }
    }

    override val size: Int
        get() = Int.MAX_VALUE

    override fun get(index: Int): T = backingList[index % backingList.size]

    override fun iterator(): Iterator<T> = backingList.asInfiniteSequence().iterator()

    override fun listIterator(): ListIterator<T> = listIterator(0)

    override fun listIterator(index: Int): ListIterator<T> = object : ListIterator<T> {
        val backingList = this@InfiniteList.backingList
        var nextIndex = index
        override fun hasNext(): Boolean = true

        override fun hasPrevious(): Boolean = true

        override fun next(): T = backingList[nextIndex].also {
            nextIndex++
            if (nextIndex == backingList.size) nextIndex = 0
        }

        override fun nextIndex(): Int = nextIndex

        override fun previous(): T {
            nextIndex--
            if (nextIndex < 0) nextIndex = backingList.lastIndex
            return backingList[nextIndex]
        }

        override fun previousIndex(): Int = if (nextIndex == 0) backingList.lastIndex else nextIndex - 1

    }

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        error("subList has not been implemented by EndlessList")
    }

    override fun toString(): String = "inf$backingList"
}

fun <K, V> Map<K, V>.flip(): Map<V, K> = asIterable().associate { (k, v) -> v to k }

fun Iterable<Long>.product(): Long = reduce(Long::safeTimes)
fun Sequence<Long>.product(): Long = reduce(Long::safeTimes)

@JvmName("intProduct")
fun Iterable<Int>.product(): Long = fold(1L, Long::safeTimes)

@JvmName("intProduct")
fun Sequence<Int>.product(): Long = fold(1L, Long::safeTimes)

infix fun Int.safeTimes(other: Int) = (this * other).also {
    check(it / other == this) { "Integer Overflow at $this * $other" }
}

infix fun Long.safeTimes(other: Long) = (this * other).also {
    check(it / other == this) { "Long Overflow at $this * $other" }
}

infix fun Long.safeTimes(other: Int) = (this * other).also {
    check(it / other == this) { "Long Overflow at $this * $other" }
}

infix fun Int.safeTimes(other: Long) = (this.toLong() * other).also {
    check(it / other == this.toLong()) { "Long Overflow at $this * $other" }
}

fun Long.checkedToInt(): Int = let {
    check(it in Int.MIN_VALUE..Int.MAX_VALUE) { "Value does not fit in Int: $it" }
    it.toInt()
}

/**
 * Returns a list containing the runs of equal elements and their respective count as Pairs.
 */
fun <T> Iterable<T>.runs(): List<Pair<T, Int>> {
    val iterator = iterator()
    if (!iterator.hasNext())
        return emptyList()
    val result = mutableListOf<Pair<T, Int>>()
    var current = iterator.next()
    var count = 1
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (next != current) {
            result.add(current to count)
            current = next
            count = 0
        }
        count++
    }
    result.add(current to count)
    return result
}

fun <T> Iterable<T>.runsOf(e: T): List<Int> {
    val iterator = iterator()
    if (!iterator.hasNext())
        return emptyList()
    val result = mutableListOf<Int>()
    var count = 0
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (next == e) {
            count++
        } else if (count > 0) {
            result.add(count)
            count = 0
        }
    }
    if (count > 0)
        result.add(count)
    return result
}

/**
 * Returns a sequence containing the runs of equal elements and their respective count as Pairs.
 */
fun <T> Sequence<T>.runs(): Sequence<Pair<T, Int>> = sequence {
    val iterator = iterator()
    if (iterator.hasNext()) {
        var current = iterator.next()
        var count = 1
        while (iterator.hasNext()) {
            val next: T = iterator.next()
            if (next != current) {
                yield(current to count)
                current = next
                count = 0
            }
            count++
        }
        yield(current to count)
    }
}

fun <T> Sequence<T>.runsOf(e: T): Sequence<Int> = runs().filter { it.first == e }.map { it.second }

fun <T> T.applyTimes(n: Int, f: (T) -> T): T = when (n) {
    0 -> this
    else -> f(this).applyTimes(n - 1, f)
}
