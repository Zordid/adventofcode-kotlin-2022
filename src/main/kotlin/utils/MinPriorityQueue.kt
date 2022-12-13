package utils

/**
 * Creates a [MinPriorityQueue] with all [elements] initially added with their given priority.
 */
fun <T> minPriorityQueueOf(vararg elements: Pair<T, Int>): MinPriorityQueue<T> =
    MinPriorityQueueImpl<T>().apply { elements.forEach { this += it } }

/**
 * Creates a [MinPriorityQueue] with all [elements] initially added with an [initialPriority].
 */
fun <T> minPriorityQueueOf(initialPriority: Int = 0, vararg elements: T): MinPriorityQueue<T> =
    MinPriorityQueueImpl<T>().apply { elements.forEach { insertOrUpdate(it, initialPriority) } }

interface MinPriorityQueue<T> : Set<T> {
    fun insertOrUpdate(element: T, priority: Int)
    fun decreasePriority(element: T, priority: Int): Boolean
    fun remove(element: T): Boolean
    fun extractMin(): T
    fun extractMinOrNull(): T?
    fun peekOrNull(): T?
    fun getPriorityOf(element: T): Int

    override operator fun contains(element: T): Boolean
    override fun isEmpty(): Boolean
    override operator fun iterator(): Iterator<T>

    operator fun plusAssign(elementWithPriority: Pair<T, Int>) {
        insertOrUpdate(elementWithPriority.first, elementWithPriority.second)
    }

    operator fun minusAssign(element: T) {
        remove(element)
    }

    operator fun plus(other: MinPriorityQueue<T>) = minPriorityQueueOf<T>().apply {
        for (e in this) this += e to getPriorityOf(e)
        for (e in other) this += e to other.getPriorityOf(e)
    }
}

private class MinPriorityQueueImpl<T> : MinPriorityQueue<T> {
    private val elementToPrio = HashMap<T, Int>()
    private val prioToElement = HashMap<Int, MutableSet<T>>()
    private val priorities = sortedSetOf<Int>()

    override val size get() = elementToPrio.size

    override fun iterator() = createSequence().iterator()
    override fun isEmpty() = elementToPrio.isEmpty()
    override operator fun contains(element: T) = elementToPrio.containsKey(element)
    override fun containsAll(elements: Collection<T>) = elementToPrio.keys.containsAll(elements)

    override fun insertOrUpdate(element: T, priority: Int) {
        elementToPrio[element]?.let {
            if (it == priority) return
            remove(element, it)
        }
        elementToPrio[element] = priority
        prioToElement.getOrPut(priority) {
            priorities.add(priority)
            mutableSetOf()
        }.add(element)
    }

    override fun remove(element: T): Boolean =
        elementToPrio[element]?.let { remove(element, it) } != null

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

    override fun decreasePriority(element: T, priority: Int): Boolean {
        if (getPriorityOf(element) > priority) {
            remove(element)
            insertOrUpdate(element, priority)
            return true
        }
        return false
    }

    private fun createSequence(): Sequence<T> = sequence {
        for (priority in priorities)
            for (element in prioToElement[priority]!!)
                yield(element)
    }

}
