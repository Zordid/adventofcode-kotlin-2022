package utils

/**
 * Generates all combinations of the elements of the given [Iterable] for the requested size.
 * Note: combinations do not include all their permutations!
 * @receiver the [Iterable] to take elements from
 * @param size the size of the combinations to create
 * @return a sequence of all combinations
 */
fun <T> Iterable<T>.combinations(size: Int): Sequence<List<T>> =
    toList().combinations(size)

/**
 * Generates all combinations of the elements of the given list for the requested size.
 * Note: combinations do not include all their permutations!
 * @receiver the list to take elements from
 * @param size the size of the combinations to create
 * @return a sequence of all combinations
 */
fun <T> List<T>.combinations(size: Int): Sequence<List<T>> =
    when (size) {
        0 -> emptySequence()
        1 -> asSequence().map { listOf(it) }
        else -> sequence {
            this@combinations.forEachIndexed { index, element ->
                val head = listOf(element)
                val tail = this@combinations.subList(index + 1, this@combinations.size)
                tail.combinations(size - 1).forEach { tailCombinations ->
                    yield(head + tailCombinations)
                }
            }
        }
    }

/**
 * Generates all combinations of the elements of the given [IntRange] for the requested size.
 * Note: combinations do not include all their permutations!
 * @receiver the [IntRange] to take elements from
 * @param size the size of the combinations to create
 * @return a sequence of all combinations
 */
fun IntRange.combinations(size: Int): Sequence<List<Int>> =
    when (size) {
        0 -> emptySequence()
        1 -> asSequence().map { listOf(it) }
        else -> sequence {
            for (element in this@combinations) {
                val head = listOf(element)
                val tail = element + 1..this@combinations.last
                tail.combinations(size - 1).forEach {
                    yield(head + it)
                }
            }
        }
    }

/**
 * Generates a sequence of all permutations of the given list of elements.
 * @receiver the list of elements for permutation of order
 * @return a sequence of all permutations of the given list
 */
fun <T> Collection<T>.permutations(): Sequence<List<T>> =
    when (size) {
        0 -> emptySequence()
        1 -> sequenceOf(toList())
        else -> {
            val head = first()
            val tail = drop(1)
            tail.permutations().flatMap { perm ->
                (0..perm.size).asSequence().map { perm.copyAndInsert(it, head) }
            }
        }
    }

/**
 * Generates a sequence of all permutations of the given numbers in the [IntRange]
 * @receiver the [IntRange] for permutation of order
 * @return a sequence of all permutations of the numbers in the range
 */
fun IntRange.permutations(): Sequence<List<Int>> =
    when {
        first > last -> emptySequence()
        first == last -> sequenceOf(listOf(first))
        else -> {
            val head = first
            val tail = first+1..last
            tail.permutations().flatMap { perm ->
                (0..perm.size).asSequence().map { perm.copyAndInsert(it, head) }
            }
        }
    }

fun String.permutations(): Sequence<String> =
    toList().permutations().map { it.joinToString("") }

fun String.combinations(size: Int): Sequence<String> =
    toList().combinations(size).map { it.joinToString("") }

private fun <T> List<T>.copyAndInsert(insertAt: Int, element: T): List<T> =
    List(size + 1) { idx ->
        when {
            idx < insertAt -> this[idx]
            idx == insertAt -> element
            else -> this[idx - 1]
        }
    }