package astify

interface TokenStream<T: Token> {
    fun next(): TokenStreamPair<T>

    companion object {
        fun <T: Token> pure(tokens: List<T>): TokenStream<T> = PureTokenStream(tokens)
        fun <T: Token> pure(vararg tokens: T) = pure(tokens.toList())
    }
}

data class TokenStreamPair<T: Token>(val token: T?, val stream: TokenStream<T>)

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
    override fun next(): TokenStreamPair<R> {
        val (t, s) = stream.next()
        return TokenStreamPair(t?.let(fn), MapTokenStream(s, fn))
    }
}

private class JoinTokenStream<T: Token>(
        private val a: TokenStream<T>,
        private val b: TokenStream<T>
): TokenStream<T> {
    override fun next(): TokenStreamPair<T> {
        val (t, s) = a.next()
        return if (t == null) b.next()
        else TokenStreamPair(t, JoinTokenStream(s, b))
    }
}

private class FlatMapTokenStream<T: Token, R: Token>(
        private val stream: TokenStream<T>,
        private val fn: (T) -> TokenStream<R>
): TokenStream<R> {
    override fun next(): TokenStreamPair<R> {
        val pair = stream.next()
        return if (pair.token == null) TokenStreamPair(null, this)
        else (fn(pair.token) + FlatMapTokenStream(pair.stream, fn)).next()
    }
}

private class PureTokenStream<T: Token>(
        private val tokens: List<T>
): TokenStream<T> {
    override fun next() = TokenStreamPair(
            tokens.getOrNull(0),
            PureTokenStream(tokens.drop(1))
    )
}
