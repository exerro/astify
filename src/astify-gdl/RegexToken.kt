import astify.Lexer
import astify.LexerTools
import astify.TextPosition
import astify.Token

internal fun regexTokenStream(input: String) = Lexer(regexLexerDescriptor, input)

//////////////////////////////////////////////////////////////////////////////////////////

private val regexLexerDescriptor = LexerTools.new {
    L(
            createToken(::RegexCharToken),
            exactly('(') to createKToken(::RegexParenOpenToken),
            exactly(')') to createKToken(::RegexParenCloseToken),
            exactly('[') to createKToken(::RegexBracketOpenToken),
            exactly(']') to createKToken(::RegexBracketCloseToken),
            exactly('*') to createKToken(::RegexRep0Token),
            exactly('+') to createKToken(::RegexRep1Token),
            exactly('?') to createKToken(::RegexOptToken),
            D(
                    S(T(1, '\\'.toInt(), '\\'.toInt()), T(2)),
                    S(T(2)),
                    F()
            ) to createToken(::RegexCharToken)
    )
}

//////////////////////////////////////////////////////////////////////////////////////////

internal sealed class RegexToken(position: TextPosition, text: String): Token(position, text)
internal class RegexCharToken(position: TextPosition, text: String): RegexToken(position, text)
internal class RegexAnyToken(position: TextPosition): RegexToken(position, ".")
internal class RegexParenOpenToken(position: TextPosition): RegexToken(position, "(")
internal class RegexParenCloseToken(position: TextPosition): RegexToken(position, ")")
internal class RegexBracketOpenToken(position: TextPosition): RegexToken(position, "[")
internal class RegexBracketCloseToken(position: TextPosition): RegexToken(position, "]")
internal class RegexRep0Token(position: TextPosition): RegexToken(position, "*")
internal class RegexRep1Token(position: TextPosition): RegexToken(position, "+")
internal class RegexOptToken(position: TextPosition): RegexToken(position, "?")

//////////////////////////////////////////////////////////////////////////////////////////

private fun LexerTools.exactly(c: Char) =  D(S(T(1, c.toInt())), F())
