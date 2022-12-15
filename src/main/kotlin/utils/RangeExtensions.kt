@file:Suppress("DuplicatedCode", "unused")

package utils

val IntRange.size get() = (last - first + 1).coerceAtLeast(0)

infix fun IntRange.overlaps(other: IntRange): Boolean {
    if (isEmpty() || other.isEmpty()) return false
    return !(this.last < other.first || this.first > other.last)
}

fun IntRange.combine(other: IntRange): IntRange =
    listOf(first, last, other.first, other.last).minMax().let { (f, l) -> f..l }

/**
 * Merges non-empty [IntRange]s to a potentially shorter list of not-overlapping IntRanges.
 */
fun Iterable<IntRange>.merge(): List<IntRange> {
    val sorted = this.filter { !it.isEmpty() }.sortedBy { it.first }
    sorted.isNotEmpty() || return emptyList()

    val stack = ArrayDeque<IntRange>()
    stack.add(sorted.first())
    sorted.drop(1).forEach { current ->
        if (current.first > stack.last().last + 1) {
            stack.add(current)
        } else {
            stack.add(stack.removeLast().let { it.first..current.last.coerceAtLeast(it.last) })
        }
    }
    return stack
}

/**
 * Merges non-empty [LongRange]s to a potentially shorter list of not-overlapping LongRanges.
 */
@JvmName("mergeLongRanges")
fun Iterable<LongRange>.merge(): List<LongRange> {
    val sorted = this.filter { !it.isEmpty() }.sortedBy { it.first }
    sorted.isNotEmpty() || return emptyList()

    val stack = ArrayDeque<LongRange>()
    stack.add(sorted.first())
    sorted.drop(1).forEach { current ->
        if (current.first > stack.last().last + 1) {
            stack.add(current)
        } else {
            stack.add(stack.removeLast().let { it.first..current.last.coerceAtLeast(it.last) })
        }
    }
    return stack
}
