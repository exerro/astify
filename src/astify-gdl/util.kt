import astify.SparseDFA

fun <R, T, K: Comparable<K>> uniqueRanges(
        items: List<T>,
        first: (T) -> K,
        second: (T) -> K,
        succ: (K) -> K,
        prev: (K) -> K,
        new: (K, K) -> R
): Map<R, List<T>> {
    if (items.isEmpty())
        return mapOf()

    val ss = items.flatMap { listOf(
            StartStop(first(it), listOf(it to true)),
            StartStop(succ(second(it)), listOf(it to false))
    ) }
            .groupBy { it.key } .toList()
            .map { (k, vs) -> StartStop(k, vs.flatMap { it.items }) }
            .sortedBy { it.key }

    val k0 = ss[0].key
    val active = ss[0].items.filterTrue()
    val upd = StartStopUpdater(k0, active, mapOf<R, List<T>>())

    return ss.drop(1).fold(upd) { (lastK, active, map), (k, ts) ->
        val kn = new(lastK, prev(k))
        val f = ts.filterFalse()
        val a = active + ts.filterTrue().filter { it in f }

        StartStopUpdater(
                k,
                active + ts.filterTrue() - ts.filterFalse(),
                map + mapOf(kn to active + a)
        )
    }
            .map
            .filterValues { it.isNotEmpty() }
}

////////////////////////////////////////////////////////////////////////////////

fun SparseDFA.encode(pretty: Boolean = true): String =
        "D(${if (pretty) "\n\t" else ""}${states.joinToString(if (pretty) ",\n\t" else ",") { state ->
            val prefix = when (state.type) {
                SparseDFA.State.Type.Error -> "E"
                SparseDFA.State.Type.Final -> "F"
                SparseDFA.State.Type.Standard -> "S"
            }
            val ts = state.transitions.joinToString(if (pretty) ", " else ",") {
                "T(${it.to}, ${it.min}, ${it.max})"
            }
            "$prefix($ts)"
        }}${if (pretty) "\n" else ""})"

//////////////////////////////////////////////////////////////////////////////////////////

private data class StartStopUpdater<R, T, K>(
        val lastK: K,
        val active: List<T>,
        val map: Map<R, List<T>>
)
private data class StartStop<K, T>(val key: K, val items: List<Pair<T, Boolean>>)

private fun <T> List<Pair<T, Boolean>>.filterTrue() = filter { it.second } .map { it.first }
private fun <T> List<Pair<T, Boolean>>.filterFalse() = filter { !it.second } .map { it.first }
