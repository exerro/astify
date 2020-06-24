package astify

import astify.util.HList
import astify.util.firstNotNull
import jdk.internal.jimage.decompressor.SignatureParser

sealed class ParseResult<out Tk: Token, out T> {
    data class Success<Tk: Token, out T>(val result: T, val str: TokenStream<Tk>): ParseResult<Tk, T>()
    data class Failure(val message: String, val position: TextPosition): ParseResult<Nothing, Nothing>()

    ////////////////////////////////////////////////////////////////////////////

    companion object {
        fun <Tk: Token, T> pure(value: T, ts: TokenStream<Tk>) = Success(value, ts)
    }
}

//////////////////////////////////////////////////////////////////////////////////////////

inline fun <Tk: Token, reified T: Tk> isOfType()
        = { t: Tk -> t is T }

inline fun <Tk: Token, reified T: Tk> isOfTypeWithText(text: String)
        = { t: Tk -> t is T && t.text == text }

//////////////////////////////////////////////////////////////////////////////////////////

typealias Parser<Tk, H0, H> = (H0, TokenStream<Tk>) -> ParseResult<Tk, H>

inline fun <Tk: Token, R> parse(
        p: Parser<Tk, HList.Empty, HList.Cons<R, HList.Empty>>,
        ts: TokenStream<Tk>
): ParseResult<Tk, R> = p(HList.Empty, ts).map { it.value }

//////////////////////////////////////////////////////////////////////////////////////////

inline fun <Tk: Token, T, R> ParseResult<Tk, T>.map(
        fn: (T) -> R
): ParseResult<Tk, R> = when (this) {
    is ParseResult.Success -> ParseResult.Success(fn(result), str)
    is ParseResult.Failure -> this
}

inline fun <Tk: Token, T, R> ParseResult<Tk, T>.flatMap(
        fn: (T, TokenStream<Tk>) -> ParseResult<Tk, R>
): ParseResult<Tk, R> = when (this) {
    is ParseResult.Success -> fn(result, str)
    is ParseResult.Failure -> this
}

//////////////////////////////////////////////////////////////////////////////////////////

inline fun <Tk: Token, H: HList> consume(
        err: String,
        crossinline fn: (Tk) -> Boolean
): Parser<Tk, H, HList.Cons<Tk, H>> = { h, ts ->
    val (tk, p, tsn) = ts.next
    if (tk != null && fn(tk)) {
        ParseResult.Success(HList.Cons(tk, h), tsn)
    }
    else {
        ParseResult.Failure(err, p)
    }
}

////////////////////////////////////////////////////////////////////////////////

fun <Tk: Token, H0: HList, H1: HList> pmap(
        fn: (H0) -> H1
): Parser<Tk, H0, H1> = { h, ts -> ParseResult.Success(fn(h), ts) }

////////////////////////////////////////////////////////////////////////////////

inline fun <Tk: Token, H: HList, HR: HList> alternation(
        crossinline epsilon: Parser<Tk, H, HR>,
        vararg fns: Pair<(Tk) -> Boolean, Parser<Tk, HList.Cons<Tk, H>, HR>>
): Parser<Tk, H, HR> = { h, ts ->
    val (tk, _, tsn) = ts.next

    fns.asSequence().map { (matcher, p) ->
        if (tk != null && matcher(tk)) p(HList.Cons(tk, h), tsn)
        else null
    } .firstNotNull() ?: epsilon.invoke(h, ts)
}

fun <Tk: Token, H: HList, HR: HList> alternation(
        err: String,
        vararg fns: Pair<(Tk) -> Boolean, Parser<Tk, HList.Cons<Tk, H>, HR>>
): Parser<Tk, H, HR> {
    return if (fns.isEmpty()) ({ h, ts ->
        ParseResult.Failure(err, ts.next.token?.position ?: ts.next.lastTokenPosition.end())
    })
    else ({ h, ts ->
        tryOr(h, ts, fns[0].first, fns[0].second, alternation(err, *fns.drop(1).toTypedArray()))
    })
}

////////////////////////////////////////////////////////////////////////////////

inline fun <Tk: Token, H0: HList, H1: HList, H2: HList> sequence(
        crossinline a: Parser<Tk, H0, H1>,
        crossinline b: Parser<Tk, H1, H2>
): Parser<Tk, H0, H2> = { h0, ts0 -> when (val r = a(h0, ts0)) {
    is ParseResult.Success -> b(r.result, r.str)
    is ParseResult.Failure -> r
} }

//////////////////////////////////////////////////////////////////////////////////////////

private inline fun <Tk: Token, H: HList, HR: HList> tryOr(
        h: H,
        ts: TokenStream<Tk>,
        match: (Tk) -> Boolean,
        p: Parser<Tk, HList.Cons<Tk, H>, HR>,
        pd: Parser<Tk, H, HR>
): ParseResult<Tk, HR> {
    val (tk, _, tsn) = ts.next

    return if (tk != null && match(tk)) p(HList.Cons(tk, h), tsn)
    else pd(h, ts)
}
