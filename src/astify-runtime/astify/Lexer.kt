package astify

typealias LexerTokenGenerator<T> = (ByteArray, Int, Int) -> T

//////////////////////////////////////////////////////////////////////////////////////////

object LexerTools {
    const val ANY0 = -1
    const val ANY1 = -1

    inline fun <reified T: Token> createToken(
            crossinline constructor: (TextPosition, String) -> T
    ): LexerTokenGenerator<T> = { bytes, from, to ->
        val s = bytes.slice(from until to).toByteArray()
        constructor(TextPosition(from, to), String(s))
    }

    inline fun <reified T: Token> createKToken(
            crossinline constructor: (TextPosition) -> T
    ): LexerTokenGenerator<T> = { _, from, to ->
        constructor(TextPosition(from, to))
    }

    fun <T: Token> L(d: LexerTokenGenerator<T>, vararg dfas: Pair<SparseDFA, LexerTokenGenerator<T>>) = LexerDescriptor(d, dfas)
    fun D(vararg states: SparseDFA.State) = SparseDFA(states)
    fun F(vararg transitions: SparseDFA.Transition) = SparseDFA.State(SparseDFA.State.Type.Final, transitions)
    fun S(vararg transitions: SparseDFA.Transition) = SparseDFA.State(SparseDFA.State.Type.Standard, transitions)
    fun E(vararg transitions: SparseDFA.Transition) = SparseDFA.State(SparseDFA.State.Type.Error, transitions)
    fun T(to: Int, min: Int, max: Int = min) = SparseDFA.Transition(to, min, max)
    fun T(to: Int) = SparseDFA.Transition(to, ANY0, ANY1)

    fun <T: Token> new(fn: LexerTools.() -> LexerDescriptor<T>) = fn(LexerTools)
}

////////////////////////////////////////////////////////////////////////////////

class SparseDFA(val states: Array<out State>) {
    class State(val type: Type, val transitions: Array<out Transition>) {
        enum class Type {
            Error,
            Final,
            Standard
        }
    }

    data class Transition(val to: Int, val min: Int, val max: Int)
}

////////////////////////////////////////////////////////////////////////////////

class LexerDescriptor<T>(
        private val default: LexerTokenGenerator<T>,
        private val matchers: Array<out Pair<SparseDFA, LexerTokenGenerator<T>>>
) {
    fun createToken(bytes: ByteArray, tokenIndex: Int?, from: Int, to: Int): T {
        val creator = (tokenIndex?.let { matchers[it] } ?.second ?: default)
        return creator(bytes, from, to)
    }

    internal fun machines()
            = Array(matchers.size) { i -> StateMachine(matchers[i].first) }
}

//////////////////////////////////////////////////////////////////////////////////////////

class Lexer<T: Token>(
        private val descriptor: LexerDescriptor<T>,
        private val input: ByteArray,
        private val index: Int
): TokenStream<T> {
    constructor(descriptor: LexerDescriptor<T>, input: String):
            this(descriptor, input.toByteArray(), 0)

    override fun next(): TokenStreamNext<T> {
        if (index >= input.size)
            return TokenStreamNext(null, TextPosition(input.size), this)

        val (ti, i) = readToken(input, index, descriptor)
        val token = descriptor.createToken(input, ti, index, i)
        val nextLexer = Lexer(descriptor, input, i)
        return TokenStreamNext(token, token.position, nextLexer)
    }
}

//////////////////////////////////////////////////////////////////////////////////////////

private fun <T: Token> readToken(
        input: ByteArray, index: Int,
        descriptor: LexerDescriptor<T>
): ReadTokenMatch {
    val machines = descriptor.machines()
    var i = index
    var len = null as Int?
    var idx = null as Int?

    while (i < input.size) {
        val (ci, c) = groupUpdate(machines, input[i++])

        if (ci != null) {
            len = i
            idx = ci
        }

        if (!c) break
    }

    return ReadTokenMatch(idx, len ?: index + 1)
}

private fun groupUpdate(machines: Array<StateMachine>, b: Byte): Pair<Int?, Boolean> {
    /** The index of a state machine that is in a final state after consuming. */
    var indexOfFinal = null as Int?

    for (machine in machines) {
        if (!machine.hasErred()) machine.consume(b)
    }

    machines.forEachIndexed { i, machine ->
        if (machine.isFinal()) {
            indexOfFinal = i
        }
    }

    return indexOfFinal to machines.any { !it.hasErred() }
}

private data class ReadTokenMatch(
        val tokenIndex: Int?,
        val nextIndex: Int
)

internal class StateMachine(
        private val dfa: SparseDFA
) {
    var type = SparseDFA.State.Type.Standard

    fun isFinal() = type == SparseDFA.State.Type.Final
    fun hasErred() = type == SparseDFA.State.Type.Error

    fun consume(b: Byte) {
        val state = dfa.states[index]

        for (transition in state.transitions) {
            val b0 = transition.min
            val b1 = transition.max

            if (b0 == LexerTools.ANY0 && b1 == LexerTools.ANY1 || b.toInt() in b0..b1) {
                index = transition.to
                type = dfa.states[index].type
                return
            }
        }

        type = SparseDFA.State.Type.Error
    }

    ////////////////////////////////////////////////////////////////////////////

    private var index = 0
}
