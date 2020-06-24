package astify.util

import astify.Token
import glh.support.Result
import glh.support.flatMap

typealias Parser<T, E, H, HH> = (InputStreamStack<T, H>) -> Result<InputStreamStack<T, HH>, E>
typealias MaybeParser<T, E, H, HH> = (InputStreamStack<T, H>) -> Result<InputStreamStack<T, HH>, E>?

inline fun <E, T: Token, H: HList, HH: HList> branchWithDefault(
        crossinline default: Parser<T, E, H, HH>,
        vararg branches: MaybeParser<T, E, H, HH>
): Parser<T, E, H, HH> = { iss ->
    branches.asSequence().map { it(iss) }.firstNotNull() ?: default(iss)
}

inline fun <T: Token, E, H: HList, HH: HList> branch(
        crossinline err: (InputStreamStack<T, H>) -> E,
        vararg branches: MaybeParser<T, E, H, HH>
) = branchWithDefault({ Result.err(err(it)) }, *branches)

fun <T: Token, E, H: HList, HH: HList> tokenPredicate(
        p: Parser<T, E, HCons<T, H>, HH>,
        predicate: (T) -> Boolean
): MaybeParser<T, E, H, HH> = { iss ->
    val (t, _, n) = iss.input.next
    if (t != null && predicate(t)) {
        p(InputStreamStack(n, HCons(t, iss.stack)))
    }
    else null
}

fun <T: Token, E, H0: HList, H1: HList, H2: HList> succ(
        a: Parser<T, E, H0, H1>,
        b: Parser<T, E, H1, H2>
): Parser<T, E, H0, H2> = { iss0 -> a(iss0).flatMap { iss1 -> b(iss1) } }

fun <T: Token, H: HList, E> EOF(): MaybeParser<T, E, H, H> = { iss ->
    val (t) = iss.input.next
    if (t == null) {
        Result.pure(iss)
    }
    else null
}
