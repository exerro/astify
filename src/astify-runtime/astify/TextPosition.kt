package astify

data class TextPosition(
        val char0: Int,
        val char1: Int = char0
) {
    companion object {
        val BEGIN = TextPosition(0)
    }
}
