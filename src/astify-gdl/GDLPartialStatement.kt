sealed class GDLPartialStatement {
    data class InlineTokenDefinition(
            val regex: String
    ): GDLPartialStatement()

    data class TokenDefinition(
            val skip: Boolean,
            val regex: String
    ): GDLPartialStatement()

    data class RuleReference(
            val ruleGroupIndex: Int,
            val ruleVariantIndex: Int
    )

    data class Rule(
            val type: GDLType,
            val variants: List<Variant>
    ) {
        data class Variant(
                val inStack: List<GDLType>,
                val outStack: List<GDLType>,
                val sequences: List<Sequence>
        )

        data class Sequence(
                val terms: List<Term>
        )

        sealed class Term
    }
}
