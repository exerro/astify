
fun main() {
    val lexer = gdlTokenStream("Hello /* comment */ grammar world, \"this\" is cool")

    generateSequence(lexer) { l ->
        val (t, ll) = l.next()
        println(t ?: "null")
        ll.takeIf { t != null }
    } .toList()

    /* sealed class RegexAST
    object RegexAny: RegexAST()
    class  RegexSeq(val rs: List<RegexAST>): RegexAST()
    class  RegexCharRange(val min: Char, val max: Char): RegexAST()
    class  RegexAlt(val rs: List<RegexAST>): RegexAST()
    class  RegexRep0(val r: RegexAST): RegexAST()
    class  RegexOpt(val r: RegexAST): RegexAST() */
}
