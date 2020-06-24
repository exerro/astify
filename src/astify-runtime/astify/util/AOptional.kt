package astify.util

import astify.TextPosition
import astify.TextPositioned

sealed class AOptional<out T: TextPositioned>: TextPositioned {
    data class Some<T: TextPositioned>(val value: T): AOptional<T>() {
        override val position = value.position
    }
    data class None(override val position: TextPosition): AOptional<Nothing>()

    ////////////////////////////////////////////////////////////////////////////

    fun <R: TextPositioned> map(fn: (T) -> R): AOptional<R> = when (this) {
        is Some -> Some(fn(value))
        is None -> this
    }

    fun <R: TextPositioned> flatMap(fn: (T) -> AOptional<R>): AOptional<R> = when (this) {
        is Some -> fn(value)
        is None -> this
    }

    ////////////////////////////////////////////////////////////////////////////

    companion object {
        fun <T: TextPositioned> pure(value: T) = Some(value)
    }
}
