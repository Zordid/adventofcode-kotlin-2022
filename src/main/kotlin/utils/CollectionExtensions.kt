@file:Suppress("unused")

package utils

/**
 * Splits elements by a defined [delimiter] predicate into groups of elements.
 *
 * @param limit limits the number of generated groups.
 * @param keepEmpty if true, groups without elements are preserved, otherwise will be omitted in the result.
 * @return a List of the groups of elements.
 */
fun <T> Iterable<T>.splitBy(limit: Int = 0, keepEmpty: Boolean = true, delimiter: (T) -> Boolean): List<List<T>> {
    require(limit >= 0) { "Limit must not be negative, but was $limit" }
    val isLimited = limit > 0

    val result = ArrayList<List<T>>(if (isLimited) limit.coerceAtMost(10) else 10)
    var currentSubList = mutableListOf<T>()
    for (element in this) {
        if ((!isLimited || (result.size < limit - 1)) && delimiter(element)) {
            if (keepEmpty || currentSubList.isNotEmpty()) {
                result += currentSubList
                currentSubList = mutableListOf()
            }
        } else {
            currentSubList += element
        }
    }
    if (keepEmpty || currentSubList.isNotEmpty())
        result += currentSubList
    return result
}

/**
 * Splits nullable elements by `null` values. The resulting groups will not contain any nulls.
 *
 * @param keepEmpty if true, groups without elements are preserved, otherwise will be omitted in the result.
 * @return a List of the groups of elements.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> Iterable<T?>.splitByNulls(keepEmpty: Boolean = true): List<List<T>> =
    splitBy(keepEmpty = keepEmpty) { it == null } as List<List<T>>

/**
 * Returns the smallest and largest element or `null` if there are no elements.
 */
fun <T : Comparable<T>> Iterable<T>.minMaxOrNull(): Pair<T, T>? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var min = iterator.next()
    var max = min
    while (iterator.hasNext()) {
        val e = iterator.next()
        if (min > e) min = e
        if (e > max) max = e
    }
    return min to max
}

/**
 * Returns the smallest and largest element or throws [NoSuchElementException] if there are no elements.
 */
fun <T : Comparable<T>> Iterable<T>.minMax(): Pair<T, T> = minMaxOrNull() ?: throw NoSuchElementException()

/**
 * Returns the smallest and largest element or `null` if there are no elements.
 */
fun <T : Comparable<T>> Sequence<T>.minMaxOrNull(): Pair<T, T>? = asIterable().minMaxOrNull()

/**
 * Returns the smallest and largest element or throws [NoSuchElementException] if there are no elements.
 */
fun <T : Comparable<T>> Sequence<T>.minMax(): Pair<T, T> = minMaxOrNull() ?: throw NoSuchElementException()

/**
 * Returns the first element yielding the smallest and the first element yielding the largest value
 * of the given function or `null` if there are no elements.
 */
inline fun <T, R : Comparable<R>> Iterable<T>.minMaxByOrNull(selector: (T) -> R): Pair<T, T>? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var minElem = iterator.next()
    var maxElem = minElem
    if (!iterator.hasNext()) return minElem to maxElem
    var minValue = selector(minElem)
    var maxValue = minValue
    do {
        val e = iterator.next()
        val v = selector(e)
        if (minValue > v) {
            minElem = e
            minValue = v
        }
        if (v > maxValue) {
            maxElem = e
            maxValue = v
        }
    } while (iterator.hasNext())
    return minElem to maxElem
}

/**
 * Returns the first element yielding the smallest and the first element yielding the largest value
 * of the given function or throws [NoSuchElementException] if there are no elements.
 */
inline fun <T, R : Comparable<R>> Iterable<T>.minMaxBy(selector: (T) -> R): Pair<T, T> =
    minMaxByOrNull(selector) ?: throw NoSuchElementException()

/**
 * Returns the smallest and largest value as a range or `null` if there are no elements.
 */
fun Iterable<Int>.rangeOrNull(): IntRange? = minMaxOrNull()?.let { it.first..it.second }

/**
 * Returns the smallest and largest value as a range or throws [NoSuchElementException] if there are no elements.
 */
fun Iterable<Int>.range(): IntRange = rangeOrNull() ?: throw NoSuchElementException()

/**
 * Returns the smallest and largest value as a range or `null` if there are no elements.
 */
fun Iterable<Long>.rangeOrNull(): LongRange? = minMaxOrNull()?.let { it.first..it.second }

/**
 * Returns the smallest and largest value as a range or throws [NoSuchElementException] if there are no elements.
 */
fun Iterable<Long>.range(): LongRange = rangeOrNull() ?: throw NoSuchElementException()

/**
 * Efficiently generate the top [n] smallest elements without sorting all elements.
 */
@Suppress("DuplicatedCode")
fun <T : Comparable<T>> Iterable<T>.minN(n: Int): List<T> {
    require(n >= 0) { "Number of smallest elements must not be negative" }
    val iterator = iterator()
    when {
        n == 0 || !iterator.hasNext() -> return emptyList()
        n == 1 -> return minOrNull()?.let { listOf(it) } ?: emptyList()
        this is Collection<T> && n >= size -> return this.sorted()
    }

    val smallest = ArrayList<T>(n.coerceAtMost(10))
    var min = iterator.next()
        .also { smallest += it }
        .let { it to it }

    while (iterator.hasNext()) {
        val e = iterator.next()
        when {
            smallest.size < n -> {
                smallest += e
                min = when {
                    e < min.first -> e to min.second
                    e > min.second -> min.first to e
                    else -> min
                }
            }

            e < min.second -> {
                val removeAt = smallest.indexOfLast { it.compareTo(min.second) == 0 }
                smallest.removeAt(removeAt)
                smallest += e
                min = smallest.minMax()
            }
        }
    }
    return smallest.sorted()
}

/**
 * Efficiently generate the top [n] largest elements without sorting all elements.
 */
@Suppress("DuplicatedCode")
fun <T : Comparable<T>> Iterable<T>.maxN(n: Int): List<T> {
    require(n >= 0) { "Number of largest elements must not be negative" }
    val iterator = iterator()
    when {
        n == 0 || !iterator.hasNext() -> return emptyList()
        n == 1 -> return maxOrNull()?.let { listOf(it) } ?: emptyList()
        this is Collection<T> && n >= size -> return this.sortedDescending()
    }

    val largest = ArrayList<T>(n.coerceAtMost(10))
    var max = iterator.next()
        .also { largest += it }
        .let { it to it }

    while (iterator.hasNext()) {
        val e = iterator.next()
        when {
            largest.size < n -> {
                largest += e
                max = when {
                    e < max.first -> e to max.second
                    e > max.second -> max.first to e
                    else -> max
                }
            }

            e > max.first -> {
                val removeAt = largest.indexOfLast { it.compareTo(max.first) == 0 }
                largest.removeAt(removeAt)
                largest += e
                max = largest.minMax()
            }
        }
    }
    return largest.sortedDescending()
}
