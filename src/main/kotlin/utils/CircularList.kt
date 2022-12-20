package utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.absoluteValue
import kotlin.math.sign

interface CircularListElement<E> {
    val value: E
    val prev: CircularListElement<E>
    val next: CircularListElement<E>

    fun forward(n: Int): CircularListElement<E>
}

@OptIn(ExperimentalContracts::class)
class CircularList<E>(initial: Iterable<E>) {
    private var _first: DLLE<E>
    val first: CircularListElement<E> get() = _first

    private var _size: Int
    val size: Int get() = _size

    init {
        val iterator = initial.iterator()
        require(iterator.hasNext()) { "Must not be empty" }
        var c = DLLE(iterator.next(), null, null, this)
        _first = c
        var count = 1
        while (iterator.hasNext()) {
            count++
            val n = DLLE(iterator.next(), c, null, this)
            c.unsafeNext = n
            c = n
        }
        c.unsafeNext = _first
        _first.unsafePrev = c
        _size = count
    }

    fun count(): Int {
        var count = 1
        var c = _first.next
        while (c != _first) {
            count++
            c = c.next
        }
        return count
    }

    private class DLLE<E>(
        override val value: E,
        var unsafePrev: DLLE<E>?,
        var unsafeNext: DLLE<E>?,
        var owner: CircularList<E>?,
    ) : CircularListElement<E> {
        override val prev: CircularListElement<E>
            get() = unsafePrev!!
        override val next: CircularListElement<E>
            get() = unsafeNext!!

        override fun forward(n: Int): CircularListElement<E> =
            owner!!.advanceBy(this, n)

        override fun toString() = value.toString()
    }

    private inner class CircularIterator : Iterator<CircularListElement<E>> {
        var lastProduced: CircularListElement<E>? = null
        override fun hasNext() = lastProduced?.next != _first

        override fun next(): CircularListElement<E> {
            lastProduced?.next != _first || throw NoSuchElementException()
            return (lastProduced?.next ?: _first).also { lastProduced = it }
        }
    }

    val entries: Iterable<CircularListElement<E>>
        get() = object : Iterable<CircularListElement<E>> {
            override fun iterator(): Iterator<CircularListElement<E>> = CircularIterator()
        }

    override fun toString(): String {
        return buildString {
            var c = first
            append('[')
            append(c)
            c = c.next
            repeat(size - 1) {
                append(", ")
                append(c)
                c = c.next
            }
            check(c == first)
            append(']')
        }
    }

    fun remove(element: CircularListElement<E>) {
        checkOwnership(element)
        if (element.unsafePrev == null) return
        require(first.next != first) { "Cannot remove last element" }
        _size--
        val prev = element.unsafePrev!!
        val next = element.unsafeNext!!
        prev.unsafeNext = next
        next.unsafePrev = prev

        if (element == _first) {
            _first = element.unsafeNext!!
        }
        element.unsafePrev = null
        element.unsafeNext = null
    }



    private fun checkOwnership(element: CircularListElement<E>): DLLE<E> {
        contract {
            returns() implies (element is DLLE<E>)
        }
        return (element as DLLE<E>).also { require(element.owner == this ) }
    }

    fun swap(a: CircularListElement<E>, b: CircularListElement<E>) {
        checkOwnership(a)
        checkOwnership(b)
        a != b || return
        val aPrev = a.unsafePrev!!
        val aNext = a.unsafeNext!!
        val bPrev = b.unsafePrev!!
        val bNext = b.unsafeNext!!
        aPrev.unsafeNext = b
        aNext.unsafePrev = b
        bPrev.unsafeNext = a
        bNext.unsafePrev = a
        a.unsafePrev = b.unsafePrev.also { b.unsafePrev = a.unsafePrev }
        a.unsafeNext = b.unsafeNext.also { b.unsafeNext = a.unsafeNext }
        if (a == first) _first = b
        else if (b == first) _first = a
    }

    fun insertAfter(target: CircularListElement<E>, value: E) =
        insertAfter(target, DLLE(value, null, null, this))

    fun insertAfter(target: CircularListElement<E>, element: CircularListElement<E>) {
        checkOwnership(target)
        checkOwnership(element)
        if (target.next == element) return
        require(target != element) { "Cannot insert itself after itself" }
        if (element.unsafePrev !=null) remove(element)

        _size++
        element.unsafePrev = target
        element.unsafeNext = target.unsafeNext
        target.unsafeNext = element
        element.unsafeNext!!.unsafePrev = element
    }

    fun insertBefore(target: CircularListElement<E>, value: E) =
        insertBefore(target, DLLE(value, null, null, this))

    fun insertBefore(target: CircularListElement<E>, element: CircularListElement<E>) {
        checkOwnership(target)
        checkOwnership(element)
        if (target.prev == element) return
        require(target != element) { "Cannot insert itself ($element) before itself ($target)" }
        remove(element)

        _size++
        element.unsafePrev = target.unsafePrev
        element.unsafeNext = target
        target.unsafePrev = element
        element.unsafePrev!!.unsafeNext = element
    }

    fun first(predicate: (E) -> Boolean): CircularListElement<E> {
        var e = first
        repeat(size) {
            if (predicate(e.value)) return e
            e = e.next
        }
        throw NoSuchElementException("")
    }

    fun advanceBy(target: CircularListElement<E>, c: Int): CircularListElement<E> =
        advanceBy(target, c.toLong())

    fun advanceBy(target: CircularListElement<E>, c: Long): CircularListElement<E> {
        checkOwnership(target)
        var result = target
        val normalized = (c.absoluteValue % size).toInt()
        if (normalized == 0) return result

        val (steps, dir) = if (normalized <= size / 2)
            normalized to c.sign
        else
            size - normalized to -c.sign
        repeat(
            steps,
            if (dir > 0) { _ -> result = result.next } else { _ -> result = result.prev }
        )
        return result
    }

}

fun <E> Iterable<E>.toCircularList(): CircularList<E> = CircularList(this)
