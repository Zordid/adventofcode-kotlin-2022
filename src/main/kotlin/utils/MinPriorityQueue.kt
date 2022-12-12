package utils

fun <T> minPriorityQueueOf(vararg elements: Pair<T, Int>): MinPriorityQueue<T> =
    MinPriorityQueueImpl<T>().apply { elements.forEach { this += it } }

interface MinPriorityQueue<T> : Collection<T> {
    override fun isEmpty(): Boolean
    fun insert(element: T, priority: Int)
    fun remove(element: T)
    fun extractMin(): T
    fun extractMinOrNull(): T?
    fun peekOrNull(): T?
    fun decreasePriority(element: T, priority: Int)
    fun getPriorityOf(element: T): Int

    override operator fun contains(element: T): Boolean
    operator fun plusAssign(elementWithPriority: Pair<T, Int>) =
        insert(elementWithPriority.first, elementWithPriority.second)

    operator fun minusAssign(element: T) = remove(element)
    override operator fun iterator(): Iterator<T>
    operator fun plus(other: MinPriorityQueue<T>) = minPriorityQueueOf<T>().apply {
        for (e in this) this += e to getPriorityOf(e)
        for (e in other) this += e to other.getPriorityOf(e)
    }
}

private class MinPriorityQueueImpl<T> : MinPriorityQueue<T> {
    private val elementToPrio = mutableMapOf<T, Int>()
    private val prioToElement = mutableMapOf<Int, MutableSet<T>>()
    private val priorities = sortedSetOf<Int>()

    override val size get() = elementToPrio.size

    override fun iterator() = createSequence().iterator()
    override fun isEmpty() = elementToPrio.isEmpty()
    override operator fun contains(element: T) = elementToPrio.containsKey(element)
    override fun containsAll(elements: Collection<T>) = elementToPrio.keys.containsAll(elements)

    override fun insert(element: T, priority: Int) {
        elementToPrio[element]?.let { previousPriority ->
            remove(element, previousPriority)
        }
        elementToPrio[element] = priority
        prioToElement.getOrPut(priority) {
            priorities.add(priority)
            mutableSetOf()
        }.add(element)
    }

    override fun remove(element: T) {
        remove(element, getPriorityOf(element))
    }

    private fun remove(element: T, priority: Int) {
        elementToPrio.remove(element)
        val elementsForPriority = prioToElement[priority]!!
        elementsForPriority.remove(element)
        // last element for this specific priority?
        if (elementsForPriority.isEmpty()) {
            prioToElement.remove(priority)
            priorities.remove(priority)
        }
    }

    override fun getPriorityOf(element: T) =
        elementToPrio[element] ?: throw NoSuchElementException()

    override fun extractMin(): T {
        val lowestPriority = priorities.first()
        val result = prioToElement[lowestPriority]!!.first()
        remove(result, lowestPriority)
        return result
    }

    override fun extractMinOrNull(): T? =
        if (isEmpty()) null else extractMin()

    override fun peekOrNull(): T? =
        prioToElement[priorities.firstOrNull()]?.first()

    override fun decreasePriority(element: T, priority: Int) {
        remove(element)
        insert(element, priority)
    }

    private fun createSequence(): Sequence<T> = sequence {
        for (priority in priorities)
            for (element in prioToElement[priority]!!)
                yield(element)
    }

}
