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
    private var _first: OwnedCircularListElement<E>? = null
    val first: CircularListElement<E> get() = _first ?: throw NoSuchElementException("")
    val firstOrNull: CircularListElement<E>? get() = _first

    private var _size: Int
    val size: Int get() = _size

    init {
        var c: OwnedCircularListElement<E>? = null
        var count = 0
        for (e in initial) {
            count++
            val n = OwnedCircularListElement(e, c, null, this)
            if (c == null) {
                _first = n
            } else
                c.unsafeNext = n
            c = n
        }
        c?.unsafeNext = _first
        _first?.unsafePrev = c
        _size = count
    }

    fun count(): Int {
        _first ?: return 0
        var count = 1
        var c = first.next
        while (c != _first) {
            count++
            c = c.next
        }
        return count
    }

    private class OwnedCircularListElement<E>(
        override val value: E,
        var unsafePrev: OwnedCircularListElement<E>?,
        var unsafeNext: OwnedCircularListElement<E>?,
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
        override fun hasNext() = size > 0 && lastProduced?.next != first

        override fun next(): CircularListElement<E> {
            hasNext() || throw NoSuchElementException()
            return (lastProduced?.next ?: first).also { lastProduced = it }
        }
    }

    val entries: Iterable<CircularListElement<E>>
        get() = object : Iterable<CircularListElement<E>> {
            override fun iterator(): Iterator<CircularListElement<E>> = CircularIterator()
        }

    override fun toString(): String {
        return buildString {
            append('[')
            if (_first != null) {
                var c = first
                append(c)
                c = c.next
                repeat(size - 1) {
                    append(", ")
                    append(c)
                    c = c.next
                }
                check(c == first)
            }
            append(']')
        }
    }

    fun remove(element: CircularListElement<E>) {
        checkOwnership(element)
        if (element.unsafePrev == null) return
        _size--
        if (size > 0) {
            val prev = element.unsafePrev!!
            val next = element.unsafeNext!!
            prev.unsafeNext = next
            next.unsafePrev = prev

            if (element == _first) {
                _first = element.unsafeNext!!
            }
        } else {
            _first = null
        }
        element.unsafePrev = null
        element.unsafeNext = null
    }

    private fun OwnedCircularListElement<E>.insertBetween(
        left: OwnedCircularListElement<E>,
        right: OwnedCircularListElement<E>,
    ) {
        left.unsafeNext = this
        this.unsafePrev = left
        right.unsafePrev = this
        this.unsafeNext = right
        _size++
    }

    private fun checkOwnership(element: CircularListElement<E>): OwnedCircularListElement<E> {
        contract {
            returns() implies (element is OwnedCircularListElement<E>)
        }
        return (element as OwnedCircularListElement<E>).also { require(element.owner == this) }
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
        insertAfter(target, OwnedCircularListElement(value, null, null, this))

    fun insertAfter(target: CircularListElement<E>, element: CircularListElement<E>) {
        checkOwnership(target)
        checkOwnership(element)
        if (target.next == element) return
        require(target != element) { "Cannot insert itself after itself" }
        if (element.unsafePrev != null) remove(element)

        element.insertBetween(target, target.unsafeNext!!)
    }

    fun insertBefore(target: CircularListElement<E>, value: E) =
        insertBefore(target, OwnedCircularListElement(value, null, null, this))

    fun insertBefore(target: CircularListElement<E>, element: CircularListElement<E>) {
        checkOwnership(target)
        checkOwnership(element)
        if (target.prev == element) return
        require(target != element) { "Cannot insert itself ($element) before itself ($target)" }
        if (element.unsafePrev != null) remove(element)

        element.insertBetween(target.unsafePrev!!, target)
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

    fun insert(element: E) {
        val n = OwnedCircularListElement(element, null, null, this)
        if (size == 0) {
            _size = 1
            _first = n
            n.unsafePrev = n
            n.unsafeNext = n
        } else {
            insertBefore(first, n)
        }
    }

}

fun <E> Iterable<E>.toCircularList(): CircularList<E> = CircularList(this)
