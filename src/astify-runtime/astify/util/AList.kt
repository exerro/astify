package astify.util

import astify.TextPosition
import astify.TextPositioned
import astify.end
import astify.to

class AList<T: TextPositioned>(
        private val items: List<T>,
        p0: TextPosition
): List<T>, TextPositioned {
    fun <R: TextPositioned> map(fn: (T) -> R): AList<R>
            = AList(items.map(fn), position)

    fun <R: TextPositioned> flatMap(fn: (T) -> AList<R>): AList<R>
            = AList(items.flatMap(fn), position)

    ////////////////////////////////////////////////////////////////////////////

    companion object {
        fun <T: TextPositioned> pure(value: T)
                = AList(listOf(value), value.position)
    }

    ////////////////////////////////////////////////////////////////////////////

    override val position: TextPosition
    override val size = items.size
    override fun contains(element: T) = items.contains(element)
    override fun containsAll(elements: Collection<T>) = items.containsAll(elements)
    override fun get(index: Int) = items[index]
    override fun indexOf(element: T) = items.indexOf(element)
    override fun isEmpty() = items.isEmpty()
    override fun iterator() = items.iterator()
    override fun lastIndexOf(element: T) = items.lastIndexOf(element)
    override fun listIterator() = items.listIterator()
    override fun listIterator(index: Int) = items.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int)
            = AList(items.subList(fromIndex, toIndex), items.getOrNull(fromIndex)?.position
                    ?: items.getOrNull(fromIndex - 1)?.position?.end()
                    ?: position.end())

    ////////////////////////////////////////////////////////////////////////////

    init {
        val basePosition = items.firstOrNull()?.position ?: p0
        position = items.fold(basePosition) { p, v -> p.to(v.position) }
    }
}
