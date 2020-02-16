import astify.*

internal fun gdlTokenStream(input: String): TokenStream<GDLToken> = Lexer(gdlLexerDescriptor, input)
        .map { tk -> when (tk) { // map relevant identifiers to keywords
            is IdentifierToken -> if (tk.text in keywords) KeywordToken(tk.position, tk.text) else tk
            else -> tk
        } }
        .flatMap { tk -> when (tk) { // filter out whitespace and comments (non-GDLTokens)
            is GDLToken -> TokenStream.pure(tk)
            else -> TokenStream.pure()
        } }

//////////////////////////////////////////////////////////////////////////////////////////

private val keywords = setOf("grammar")

private val gdlLexerDescriptor = LexerTools.new {
    L(
            createToken(::SymbolToken),

            regexToDFA(RegexSeq(listOf(
                    RegexCharRange('"'),
                    RegexRep0(RegexAlt(listOf(
                            RegexSeq(listOf(RegexCharRange('\\'), RegexAny)),
                            RegexCharRange(0.toChar(), '"' - 1),
                            RegexCharRange('"' + 1, '\\' - 1),
                            RegexCharRange('\\' + 1, 128.toChar())
                    ))),
                    RegexCharRange('"')
            ))) to createToken(::StringToken),
            regexToDFA(RegexSeq(listOf(
                    RegexCharRange('\''),
                    RegexRep0(RegexAlt(listOf(
                            RegexSeq(listOf(RegexCharRange('\\'), RegexAny)),
                            RegexCharRange(0.toChar(), '\'' - 1),
                            RegexCharRange('\'' + 1, '\\' - 1),
                            RegexCharRange('\\' + 1, 127.toChar())
                    ))),
                    RegexCharRange('\'')
            ))) to createToken(::StringToken),

            regexToDFA(RegexSeq(listOf(
                    RegexCharRange('0', '9'),
                    RegexRep0(RegexCharRange('0', '9'))
            ))) to createToken(::IntegerToken),

            regexToDFA(RegexSeq(listOf(
                    RegexAlt(listOf(RegexCharRange('a', 'z'), RegexCharRange('A', 'Z'))),
                    RegexRep0(RegexAlt(listOf(RegexCharRange('a', 'z'), RegexCharRange('A', 'Z'), RegexCharRange('0', '9'), RegexCharRange('_'))))
            ))) to createToken(::IdentifierToken),

            regexToDFA(RegexSeq(listOf(
                    RegexCharRange('/'),
                    RegexCharRange('/'),
                    RegexRep0(RegexAlt(listOf(
                            RegexCharRange(0.toChar(), '\n' - 1),
                            RegexCharRange('\n' + 1, 127.toChar())
                    ))),
                    RegexCharRange('\n')
            ))) to createToken(::CommentToken),
            regexToDFA(RegexSeq(listOf(
                    RegexCharRange('/'),
                    RegexCharRange('*'),
                    RegexRep0(RegexAlt(listOf(
                            RegexCharRange(0.toChar(), '\\' - 1),
                            RegexCharRange('\\' + 1, 127.toChar())
                    ))),
                    RegexCharRange('*'),
                    RegexCharRange('/')
            ))) to createToken(::CommentToken),

            regexToDFA(RegexAlt(listOf(
                    RegexCharRange(' '),
                    RegexCharRange('\n'),
                    RegexCharRange('\t')
            ))) to createToken(::WhitespaceToken)
    )
}

//////////////////////////////////////////////////////////////////////////////////////////

internal sealed class GDLAnyToken(position: TextPosition, text: String): Token(position, text)
internal class WhitespaceToken(position: TextPosition, text: String): GDLAnyToken(position, text)
internal class CommentToken(position: TextPosition, text: String): GDLAnyToken(position, text)

internal sealed class GDLToken(position: TextPosition, text: String): GDLAnyToken(position, text)
internal class SymbolToken(position: TextPosition, text: String): GDLToken(position, text)
internal class IdentifierToken(position: TextPosition, text: String): GDLToken(position, text)
internal class KeywordToken(position: TextPosition, text: String): GDLToken(position, text)
internal class StringToken(position: TextPosition, text: String): GDLToken(position, text)
internal class IntegerToken(position: TextPosition, text: String): GDLToken(position, text)
