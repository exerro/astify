package astify

interface TextPositioned {
    val position: TextPosition
}

data class TextPosition(
        val char0: Int,
        val char1: Int = char0
) {
    companion object {
        val BEGIN = TextPosition(0)
    }
}

fun TextPosition.to(t: TextPosition) = TextPosition(char0, t.char1)
fun TextPosition.end() = TextPosition(char1)
