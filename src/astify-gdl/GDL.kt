
internal data class GDL(
        val grammarName: IdentifierToken,
        val statements: List<GDLStatement>
) {
    internal sealed class GDLStatement {
        internal sealed class GDLTokenStatement {
            internal data class GDLSymbolSpecifier(
                    val symbolToken: IdentifierToken
            ): GDLTokenStatement()

            internal data class GDLKeywordTransform(
                    val sourceToken: IdentifierToken,
                    val keywordToken: IdentifierToken
            ): GDLTokenStatement()

            internal data class GDLTokenDefinition(
                    val skip: Boolean,
                    val name: IdentifierToken,
                    val regex: StringToken
            ): GDLTokenStatement()

            internal data class GDLInlineTokenDefinition(
                    val name: IdentifierToken,
                    val regex: StringToken
            ): GDLTokenStatement()
        }

        ////////////////////////////////////////////////////////////////////////

        internal sealed class GDLRuleStatement {
            internal data class GDLRule(
                    val name: IdentifierToken,
                    val parameters: List<IdentifierToken>?,
                    val patterns: List<GDLPattern>
            ): GDLRuleStatement()

            internal sealed class GDLAlternationRule(
                    val name: IdentifierToken,
                    val parameters: List<IdentifierToken>?,
                    val statements: List<GDLAlternationRuleStatement>
            ): GDLRuleStatement()
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    internal sealed class GDLAlternationRuleStatement {
        internal data class GDLInfixLRule(
                val name: IdentifierToken,
                val precedence: IntegerToken,
                val patterns: List<GDLPattern>
        ): GDLAlternationRuleStatement()

        internal data class GDLInfixRRule(
                val name: IdentifierToken,
                val precedence: IntegerToken,
                val patterns: List<GDLPattern>
        ): GDLAlternationRuleStatement()

        internal data class GDLUnaryLRule(
                val name: IdentifierToken,
                val precedence: IntegerToken,
                val patterns: List<GDLPattern>
        ): GDLAlternationRuleStatement()

        internal data class GDLUnaryRRule(
                val name: IdentifierToken,
                val precedence: IntegerToken,
                val patterns: List<GDLPattern>
        ): GDLAlternationRuleStatement()

        internal data class GDLRule(
                val name: IdentifierToken,
                val patterns: List<GDLPattern>
        ): GDLAlternationRuleStatement()

        internal sealed class GDLAlternationRule(
                val name: IdentifierToken,
                val statements: List<GDLAlternationRuleStatement>
        ): GDLAlternationRuleStatement()
    }

    ////////////////////////////////////////////////////////////////////////////

    internal sealed class GDLPattern {
        internal data class GDLLiteral(
                val value: StringToken
        ): GDLPattern()

        internal data class GDLRuleReference(
                val inline: Boolean,
                val reference: IdentifierToken,
                val parameters: List<GDLPattern>?,
                val q: Boolean?,
                val label: IdentifierToken?
        ): GDLPattern()

        internal data class GDLParen(
                val patterns: List<GDLPattern>
        ): GDLPattern()

        internal data class GDLSepBy(
                val patterns: List<GDLPattern>,
                val delimiter: List<GDLPattern>,
                val zeroAccepted: Boolean
        ): GDLPattern()

        internal data class GDLMany0(
                val patterns: List<GDLPattern>
        ): GDLPattern()

        internal data class GDLMany1(
                val patterns: List<GDLPattern>
        ): GDLPattern()

        internal data class GDLOptional(
                val patterns: List<GDLPattern>
        ): GDLPattern()
    }
}
