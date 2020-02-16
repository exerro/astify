@file:Suppress("SelfReferenceConstructorParameter")

import astify.TokenStream

private fun parseRegex(
        tokens: TokenStream<RegexToken>
): RegexAST = parseTermsWhile(tokens) { true } .first

private fun parseTermsWhile(
        tokens: TokenStream<RegexToken>,
        fn: (RegexToken) -> Boolean
): Pair<RegexAST, TokenStream<RegexToken>> {
    var ts = tokens
    val result = mutableListOf<RegexAST>()

    while (true) {
        val (token, s) = ts.next()

        if (token != null && fn(token)) {
            val (a, ss) = parseRegexTerm(token, s)
            ts = ss
            result.add(a)
        }
        else {
            ts = s
            break
        }
    }

    return RegexSeq(result) to ts
}

private fun parseRegexTerm(
        token: RegexToken,
        tokens: TokenStream<RegexToken>
): Pair<RegexAST, TokenStream<RegexToken>> {
    val (t, s) = when (token) {
        is RegexCharToken -> {
            val c = token.text[0]
            RegexCharRange(c, c) to tokens
        }
        is RegexAnyToken -> RegexAny to tokens
        is RegexParenOpenToken -> {
            parseTermsWhile(tokens) { it !is RegexParenCloseToken }
        }
        is RegexBracketOpenToken -> TODO()
        is RegexParenCloseToken -> RegexCharRange(')', ')') to tokens
        is RegexBracketCloseToken -> RegexCharRange(']', ']') to tokens
        is RegexRep0Token -> RegexCharRange('*', '*') to tokens
        is RegexRep1Token -> RegexCharRange('+', '+') to tokens
        is RegexOptToken -> RegexCharRange('?', '?') to tokens
    }

    val (mod, ss) = tokens.next()

    return when (mod) {
        is RegexRep0Token -> RegexRep0(t) to ss
        is RegexRep1Token -> RegexSeq(listOf(t, RegexRep0(t))) to ss
        is RegexOptToken -> RegexOpt(t) to ss
        else -> t to s
    }
}

//////////////////////////////////////////////////////////////////////////////////////////
