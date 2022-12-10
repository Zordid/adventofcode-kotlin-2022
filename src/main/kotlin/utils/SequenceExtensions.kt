package utils

class InfiniteSequence<T>(base: Sequence<T>) : Sequence<T> by base

fun <T> Iterable<T>.asInfiniteSequence() =
    InfiniteSequence(sequence { while (true) yieldAll(this@asInfiniteSequence) })

fun <T> Sequence<T>.repeatLastForever() = InfiniteSequence(sequence {
    val it = this@repeatLastForever.iterator()
    if (it.hasNext()) {
        var elem: T
        do {
            elem = it.next()
            yield(elem)
        } while (it.hasNext())
        while (true) yield(elem)
    }
})
