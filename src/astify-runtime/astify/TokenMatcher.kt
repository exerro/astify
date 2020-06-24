package astify

typealias TokenMatcher<T> = (T) -> Boolean

inline fun <T: Token, reified R: Any> tokenIsType(): TokenMatcher<T>
        = { token -> token is R }

inline fun <T: Token, reified R: Any> tokenIsTypeWithText(text: String): TokenMatcher<T>
        = { token -> token is R && token.text == text }
