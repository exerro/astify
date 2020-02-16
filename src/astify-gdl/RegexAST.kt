
internal fun regexToString(r: RegexAST): String = when (r) {
    is RegexCharRange -> r.run { "[$min-$max]" }
    is RegexAlt -> "(${r.rs.joinToString("|", transform = ::regexToString)})"
    is RegexRep0 -> "(${regexToString(r.r)})*"
    RegexAny -> "."
    is RegexSeq -> "(" + r.rs.joinToString("", transform = ::regexToString) + ")"
    is RegexOpt -> "(" + regexToString(r.r) + ")?"
}

//////////////////////////////////////////////////////////////////////////////////////////

internal sealed class RegexAST
internal object RegexAny: RegexAST()
internal class  RegexSeq(val rs: List<RegexAST>): RegexAST()
internal class  RegexCharRange(val min: Char, val max: Char = min): RegexAST()
internal class  RegexAlt(val rs: List<RegexAST>): RegexAST()
internal class  RegexRep0(val r: RegexAST): RegexAST()
internal class  RegexOpt(val r: RegexAST): RegexAST()
