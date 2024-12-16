package utils

import java.util.*
import kotlin.collections.ArrayDeque

/**
 * Creates a deque (see [ArrayDeque]) of the given elements.
 *
 * *Note:* a deque can hold duplicate elements!
 */
fun <T> dequeOf(vararg elements: T) = dequeOf(elements.asIterable())

/**
 * Creates a deque (see [ArrayDeque]) of the given elements.
 *
 * *Note:* a deque can hold duplicate elements!
 */
fun <T> dequeOf(elements: Iterable<T>) = ArrayDeque(elements.toList())

/**
 * Creates a [Queue] with the given [elements] added in order.
 *
 * Queues reject duplicate entries of the same element.
 */
fun <T : Any> queueOf(vararg elements: T): Queue<T> = queueOf(elements.asIterable())

/**
 * Creates a [Queue] with the given [elements] added in order.
 *
 * Queues reject duplicate entries of the same element.
 */
fun <T : Any> queueOf(elements: Iterable<T>): Queue<T> = QueueImpl<T>().apply { addAll(elements) }

/**
 * Creates a [MinPriorityQueue] with all [elements] initially added with their given priority.
 */
fun <T> minPriorityQueueOf(vararg elements: Pair<T, Int>): MinPriorityQueue<T> =
    minPriorityQueueOf(elements.asIterable())

/**
 * Creates a [MinPriorityQueue] with all [elements] initially added with their given priority.
 */
fun <T> minPriorityQueueOf(elements: Iterable<Pair<T, Int>>): MinPriorityQueue<T> =
    MinPriorityQueueImpl<T>().apply { elements.forEach { this += it } }

interface QueueBasics<T> {
    val size: Int
    fun isEmpty(): Boolean

    fun removeFirst(): T
    fun removeFirstOrNull(): T?
}

/**
 * An ordered queue of elements of type [T].
 *
 * A queue combines the features of an [ArrayDeque] with those of a [Set].
 * It will only ever accept one occurrence of a given element.
 */
interface Queue<T : Any> : QueueBasics<T>, Set<T> {
    /**
     *  Adds the given [element] at the end of the queue, unless it is already present.
     */
    fun add(element: T): Boolean = addLast(element)

    fun addFirst(element: T): Boolean
    fun addLast(element: T): Boolean
    fun removeLast(): T
    fun removeLastOrNull(): T?

    operator fun plusAssign(element: T) {
        add(element)
    }

    operator fun plusAssign(elements: Iterable<T>) {
        addAll(elements)
    }

    fun addAll(elements: Iterable<T>): Boolean {
        var addedAny = false
        elements.forEach { addedAny = add(it) || addedAny }
        return addedAny
    }

    fun addAll(elements: Array<out T>): Boolean =
        addAll(elements.asIterable())
}

private class QueueImpl<T : Any> : Queue<T> {
    private val deque = ArrayDeque<T>()
    private val elementSet = mutableSetOf<T>()

    override val size: Int get() = deque.size

    override fun addFirst(element: T): Boolean =
        elementSet.add(element).also { wasAdded ->
            if (wasAdded) deque.addFirst(element)
        }

    override fun addLast(element: T): Boolean =
        elementSet.add(element).also { wasAdded ->
            if (wasAdded) deque.addLast(element)
        }

    override fun removeFirst(): T =
        deque.removeFirst().also { elementSet.remove(it) }

    override fun removeFirstOrNull(): T? =
        deque.removeFirstOrNull()?.also { elementSet.remove(it) }

    override fun removeLast(): T =
        deque.removeLast().also { elementSet.remove(it) }

    override fun removeLastOrNull(): T? =
        deque.removeLastOrNull()?.also { elementSet.remove(it) }

    override fun isEmpty(): Boolean = deque.isEmpty()

    override fun iterator(): Iterator<T> = deque.iterator()

    override fun containsAll(elements: Collection<T>): Boolean = elements.containsAll(elements)

    override fun contains(element: T): Boolean = elementSet.contains(element)

    override fun toString(): String {
        return deque.toString()
    }
}

interface MinPriorityQueue<T> : QueueBasics<T>, Set<T> {
    val minPriority: Int?
    val maxPriority: Int?

    fun copy(): MinPriorityQueue<T>
    fun insertOrUpdate(element: T, priority: Int)
    fun decreasePriority(element: T, priority: Int): Boolean
    fun extractMin(): T
    fun extractMinWithPriority(): Pair<T, Int>
    fun extractMinOrNull(): T?
    fun extractAllMin(): Set<T>
    fun remove(element: T): Boolean
    fun peekOrNull(): T?
    fun getPriorityOf(element: T): Int
    override fun removeFirst() = extractMin()
    override fun removeFirstOrNull(): T? = extractMinOrNull()

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

private class MinPriorityQueueImpl<T>(
    private val elementToPriority: HashMap<T, Int> = HashMap<T, Int>(),
    private val priorityToElements: MutableMap<Int, MutableSet<T>> = HashMap<Int, MutableSet<T>>(),
    private val priorities: SortedSet<Int> = sortedSetOf<Int>(),
) : MinPriorityQueue<T> {
    override val size get() = elementToPriority.size

    override fun iterator() = createSequence().iterator()
    override fun isEmpty() = elementToPriority.isEmpty()
    override operator fun contains(element: T) = elementToPriority.containsKey(element)
    override fun containsAll(elements: Collection<T>) = elementToPriority.keys.containsAll(elements)
    override val minPriority: Int?
        get() = priorities.firstOrNull()
    override val maxPriority: Int?
        get() = priorities.lastOrNull()

    override fun copy(): MinPriorityQueue<T> =
        MinPriorityQueueImpl(
            HashMap(elementToPriority),
            priorityToElements.mapValues { (_, v) -> v.toMutableSet() }.toMutableMap(),
            TreeSet(priorities)
        )

    override fun insertOrUpdate(element: T, priority: Int) {
        elementToPriority[element]?.let {
            if (it == priority) return
            remove(element, it)
        }
        elementToPriority[element] = priority
        priorityToElements.getOrPut(priority) {
            priorities.add(priority)
            mutableSetOf()
        }.add(element)
    }

    override fun remove(element: T): Boolean =
        elementToPriority[element]?.let { remove(element, it) } != null

    private fun remove(element: T, priority: Int) {
        elementToPriority.remove(element)
        val elementsForPriority = priorityToElements[priority]!!
        elementsForPriority.remove(element)
        // last element for this specific priority?
        if (elementsForPriority.isEmpty()) {
            priorityToElements.remove(priority)
            priorities.remove(priority)
        }
    }

    override fun getPriorityOf(element: T) =
        elementToPriority[element] ?: throw NoSuchElementException()

    override fun extractMin(): T {
        val lowestPriority = priorities.first()
        val result = priorityToElements[lowestPriority]!!.first()
        remove(result, lowestPriority)
        return result
    }

    override fun extractAllMin(): Set<T> {
        val lowestPriority = priorities.firstOrNull() ?: return emptySet()
        val results = priorityToElements[lowestPriority]!!.toSet()
        results.forEach {
            remove(it, lowestPriority)
        }
        return results
    }

    override fun extractMinWithPriority(): Pair<T, Int> {
        val lowestPriority = priorities.first()
        val result = priorityToElements[lowestPriority]!!.first()
        remove(result, lowestPriority)
        return result to lowestPriority
    }

    override fun extractMinOrNull(): T? =
        if (isEmpty()) null else extractMin()

    override fun peekOrNull(): T? =
        priorityToElements[priorities.firstOrNull()]?.first()

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
            for (element in priorityToElements[priority]!!)
                yield(element)
    }

    override fun toString(): String {
        return priorities.joinToString(prefix = "[", postfix = "]") { prio ->
            "$prio: ${priorityToElements[prio]}"
        }
    }

}
