package parser

typealias RuleType = List<String>

data class ParseNFA(val begin: State, val end: State) {
    class State {
        val transitions: MutableList<Transition> = mutableListOf()

        fun add(transition: Transition): State {
            transitions.add(transition)
            return this
        }
    }

    sealed class Transition(open val to: State) {
        /** Transitions consuming no input and not updating h. */
        data class EpsilonTransition(override val to: State): Transition(to)
        /** Transitions consuming no input and dropping a value from h. */
        data class DropTransition(override val to: State): Transition(to)
        /** Transitions consuming no input, dropping `inputs` values from h, and pushing a value to `h`. */
        data class NewTransition(val inputs: Int, val type: RuleType, override val to: State): Transition(to)
        /** Transitions consuming a token and erring if the token type doesn't match.
         *  Pushes the token to `h`. */
        data class TypedTransition(val tokenType: String, override val to: State): Transition(to)
        /** Transitions consuming a token and erring if the token type or text doesn't match.
         *  Pushes the token to `h`. */
        data class TypedTransitionWithText(val tokenType: String, val text: String, override val to: State): Transition(to)
    }
}
