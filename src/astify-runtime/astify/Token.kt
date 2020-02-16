package astify

import java.util.*

abstract class Token(
        val position: TextPosition,
        val text: String
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != this::class) return false
        if (other !is Token) return false
        return position == other.position && text == other.text
    }

    override fun hashCode() = Objects.hash(this::class, position, text)
    override fun toString() = "${this::class.simpleName}($text)"
}
