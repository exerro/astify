
fun main() {
    val lexer = gdlTokenStream("Hello /* comment */ grammar world, \"this\" is cool")

    generateSequence(lexer) { l ->
        val (t, ll) = l.next()
        println(t ?: "null")
        ll.takeIf { t != null }
    } .toList()
}
