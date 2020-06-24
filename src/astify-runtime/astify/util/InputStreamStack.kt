package astify.util

import astify.Token
import astify.TokenStream

data class InputStreamStack<T: Token, H: HList>(
        val input: TokenStream<T>,
        val stack: H
)

typealias ISS<T, H> = InputStreamStack<T, H>
