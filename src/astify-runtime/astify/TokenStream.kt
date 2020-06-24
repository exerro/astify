package astify

import kotlin.reflect.KProperty0

interface TokenStream<T: Token> {
    val next: TokenStreamNext<T>

    companion object {
        fun <T: Token> pure(
                p0: TextPosition = TextPosition.BEGIN,
                tokens: List<T>
        ): TokenStream<T> = PureTokenStream(p0, tokens)

        fun <T: Token> pure(
                p0: TextPosition = TextPosition.BEGIN,
                vararg tokens: T
        ): TokenStream<T> = pure(p0, tokens.toList())
    }
}

data class TokenStreamNext<T: Token>(
        /** The next token, or null if the end of input has been reached. */
        val token: T?,
        /** The position of the previous token. */
        val lastTokenPosition: TextPosition,
        /** The next token stream object. */
        val stream: TokenStream<T>
)

//////////////////////////////////////////////////////////////////////////////////////////

fun <T: Token, R: Token> TokenStream<T>.map(
        fn: (T) -> R
): TokenStream<R> = MapTokenStream(this, fn)

fun <T: Token, R: Token> TokenStream<T>.flatMap(
        fn: (T) -> TokenStream<R>
): TokenStream<R> = FlatMapTokenStream(this, fn)

operator fun <T: Token> TokenStream<T>.plus(
        other: TokenStream<T>
): TokenStream<T> = JoinTokenStream(this, other)

//////////////////////////////////////////////////////////////////////////////////////////

private class MapTokenStream<T: Token, R: Token>(
        private val stream: TokenStream<T>,
        private val fn: (T) -> R
): TokenStream<R> {
    override val next by LazyTS {
        val (t, p, s) = stream.next
        TokenStreamNext(t?.let(fn), p, MapTokenStream(s, fn))
    }
    //    override val next: TokenStreamNext<R> by lazy {
//        val (t, p, s) = stream.next()
//        return TokenStreamNext(t?.let(fn), p, MapTokenStream(s, fn))
//    }
}

private class JoinTokenStream<T: Token>(
        private val a: TokenStream<T>,
        private val b: TokenStream<T>
): TokenStream<T> {
    override val next: TokenStreamNext<T> by LazyTS {
        val (t, p, s) = a.next
        if (t == null) b.next
        else TokenStreamNext(t, p, JoinTokenStream(s, b))
    }
}

private class FlatMapTokenStream<T: Token, R: Token>(
        private val stream: TokenStream<T>,
        private val fn: (T) -> TokenStream<R>
): TokenStream<R> {
    override val next by LazyTS {
        val n = stream.next
        if (n.token == null) TokenStreamNext(null, n.lastTokenPosition, this)
        else (fn(n.token) + FlatMapTokenStream(n.stream, fn)).next
    }
}

private class PureTokenStream<T: Token>(
        private val p0: TextPosition,
        private val tokens: List<T>
): TokenStream<T> {
    override val next by LazyTS {
        val tk = tokens.getOrNull(0)
        TokenStreamNext(
                tk, p0,
                PureTokenStream(tk?.position ?: p0, tokens.drop(1))
        )
    }
}

private class LazyTS<T: Token>(
        private val get: () -> TokenStreamNext<T>
) {
    val getValue: (TokenStream<T>, KProperty0<*>) -> TokenStreamNext<T> = { _, _ -> get() }
}
