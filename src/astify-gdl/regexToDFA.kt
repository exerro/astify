import astify.LexerTools
import astify.SparseDFA

internal fun regexToDFA(regex: RegexAST): SparseDFA {
    val nfa = regex.toNFA(NFA.State(SparseDFA.State.Type.Final))
    return NFAtoDFA(nfa.followEpsilonTransitions())
}

////////////////////////////////////////////////////////////////////////////////

private fun NFAtoDFA(
        state: PFETStates
): SparseDFA {
    val transitions = mutableMapOf<Int, Pair<Boolean, List<NFA.IntegralTransition>>>()
    NFAtoDFAInternal(state, transitions)
    return (0 until transitions.keys.size)
            .map { transitions[it]!! }
            .map { (final, transitions) ->
                val t = if (final) SparseDFA.State.Type.Final else SparseDFA.State.Type.Standard
                val tr = transitions.map { (l, st) ->
                    val (min, max) = when (l) {
                        is NFA.TransitionLabel.Consuming.Range -> l.min.toInt() to l.max.toInt()
                        NFA.TransitionLabel.Consuming.Any -> LexerTools.ANY0 to LexerTools.ANY1
                    }
                    SparseDFA.Transition(st, min, max)
                }
                SparseDFA.State(t, tr.toTypedArray())
            }
            .toTypedArray().let(::SparseDFA)
}

////////////////////////////////////////////////////////////////////////////////

private fun NFAtoDFAInternal(
        state: PFETStates,
        transitions: MutableMap<Int, Pair<Boolean, List<NFA.IntegralTransition>>>,
        stateLookup: MutableMap<PFETStates, Int> = mutableMapOf()
): Int {
    val idx = stateLookup[state]
    if (idx != null) return idx
    val index = stateLookup.values.size
    val thisTransitions = state.collectConsumingTransitions()
    val isFinal = state.states.any { it.type == SparseDFA.State.Type.Final }
    stateLookup[state] = index

    transitions[index] = isFinal to thisTransitions.map {
        NFA.IntegralTransition(it.label, NFAtoDFAInternal(it.states, transitions, stateLookup))
    }

    return index
}

//////////////////////////////////////////////////////////////////////////////////////////

private fun RegexAST.toNFA(end: NFA.State): NFA.State = when (this) {
    is RegexSeq -> rs.foldRight(end) { r, s -> r.toNFA(s) }
    is RegexCharRange -> {
        NFA.State().add(NFA.TransitionLabel.Consuming.Range(min, max), end)
    }
    is RegexAlt -> {
        val rs = rs.map { it.toNFA(end) }
        val s = NFA.State()
        rs.forEach { s.add(NFA.TransitionLabel.Epsilon, it) }
        s
    }
    is RegexRep0 -> {
        val endish = NFA.State().add(NFA.TransitionLabel.Epsilon, end)
        val s = r.toNFA(endish)
        endish.add(NFA.TransitionLabel.Epsilon, s)
        s.add(NFA.TransitionLabel.Epsilon, end)
        s
    }
    is RegexOpt -> {
        val s = r.toNFA(end)
        s.add(NFA.TransitionLabel.Epsilon, end)
    }
    RegexAny -> {
        NFA.State().add(NFA.TransitionLabel.Consuming.Any, end)
    }
}

////////////////////////////////////////////////////////////////////////////////

private fun NFA.State.followEpsilonTransitions(
        ignore: List<NFA.State> = listOf(this)
): PFETStates {
    val ss = transitions
            .filter { it.label == NFA.TransitionLabel.Epsilon }
            .map { it.state } - ignore
    val s = (listOf(this) + ss + ss.flatMap { it.followEpsilonTransitions(ss).states }).toSet()

    return PFETStates(s)
}

private fun Iterable<NFA.State>.followEpsilonTransitions()
        = PFETStates(flatMap { it.followEpsilonTransitions().states } .toSet())

////////////////////////////////////////////////////////////////////////////////

private fun PFETStates.collectConsumingTransitions(): List<NFA.MultiTransitionPFET> {
    val transitions = states.flatMap { it.transitions }
            .mapNotNull { t ->
                val label = t.label as? NFA.TransitionLabel.Consuming
                label ?.let { NFA.MultiTransition(it, setOf(t.state)) }
            }

    val transitionLookup = transitions.map { it.label to it.states } .toMap()

    val anyTransitionStates = transitions.filter { it.label is NFA.TransitionLabel.Consuming.Any }
            .flatMap { it.states } .toSet()

    val uniqueTransitions = uniqueRanges(
            transitions.mapNotNull { it.label as? NFA.TransitionLabel.Consuming.Range },
            NFA.TransitionLabel.Consuming.Range::min,
            NFA.TransitionLabel.Consuming.Range::max,
            { it + 1 }, { it - 1 },
            NFA.TransitionLabel.Consuming::Range
    )

    val anyTransition = if (anyTransitionStates.isNotEmpty()) {
        val s = anyTransitionStates.followEpsilonTransitions()
        listOf(NFA.MultiTransitionPFET(NFA.TransitionLabel.Consuming.Any, s))
    }
    else listOf()

    return uniqueTransitions.map { (t, vs) ->
        val states = vs.flatMap { transitionLookup[it] ?: setOf() } .followEpsilonTransitions()
        NFA.MultiTransitionPFET(t, states)
    } + anyTransition
}

//////////////////////////////////////////////////////////////////////////////////////////

internal data class PFETStates(val states: Set<NFA.State>)

internal object NFA {
    internal class State(val type: SparseDFA.State.Type = SparseDFA.State.Type.Standard) {
        val transitions: MutableList<Transition> = mutableListOf()

        fun add(label: TransitionLabel, state: State): State =
                this.also { transitions.add(Transition(label, state)) }
    }

    data class Transition(val label: TransitionLabel, val state: State)
    data class MultiTransition(val label: TransitionLabel.Consuming, val states: Set<State>)
    data class MultiTransitionPFET(val label: TransitionLabel.Consuming, val states: PFETStates)
    data class IntegralTransition(val label: TransitionLabel.Consuming, val state: Int)

    sealed class TransitionLabel {
        sealed class Consuming: TransitionLabel() {
            data class Range(val min: Char, val max: Char) : Consuming() {
                override fun toString() = "[$min-$max]"
            }
            object Any : Consuming() {
                override fun toString() = "<.>"
            }
        }
        object Epsilon: TransitionLabel() {
            override fun toString() = "<epsilon>"
        }
    }
}

//////////////////////////////////////////////////////////////////////////////////////////

private fun printNFA(state: NFA.State, labels: MutableMap<NFA.State, Int> = mutableMapOf()): Int {
    val idx = labels[state]
    if (idx != null) return idx
    val index = labels.values.size

    labels[state] = index

    println("s$index : ${state.type}")

    state.transitions.forEach {
        println("s$index --${it.label}--> s${printNFA(it.state, labels)}")
    }

    return index
}
