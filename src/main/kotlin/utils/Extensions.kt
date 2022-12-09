@file:Suppress("unused")

package utils

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
