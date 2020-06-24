import astify.TextPosition
import astify.Token

internal object GDL {
    internal data class GDL(
            val grammarName: IdentifierToken,
            val statements: List<GDLStatement>
    )

    internal sealed class GDLStatement {
        internal sealed class GDLTokenStatement {
            internal data class GDLSymbolSpecifier(
                    val symbolToken: IdentifierToken,
                    val position: TextPosition
            ): GDLTokenStatement()

            internal data class GDLKeywordTransform(
                    val sourceToken: IdentifierToken,
                    val keywordToken: IdentifierToken,
                    val position: TextPosition
            ): GDLTokenStatement()

            internal data class GDLTokenDefinition(
                    val inline: KeywordToken?,
                    val skip: KeywordToken?,
                    val name: IdentifierToken,
                    val regex: StringToken,
                    val position: TextPosition
            ): GDLTokenStatement()
        }

        ////////////////////////////////////////////////////////////////////////

        internal sealed class GDLRuleStatement {
            internal data class GDLRule(
                    val name: IdentifierToken,
//                    val parameters: List<IdentifierToken>?, TODO
                    val patterns: List<GDLPattern>,
                    val position: TextPosition
            ): GDLRuleStatement()

            internal sealed class GDLAlternationRule(
                    val name: IdentifierToken,
//                    val parameters: List<IdentifierToken>?, TODO
                    val statements: List<GDLAlternationRuleStatement>,
                    val position: TextPosition
            ): GDLRuleStatement()
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    internal sealed class GDLOperatorPrefix {
        internal data class InfixL(
                val precedence: IntegerToken,
                val position: TextPosition
        ): GDLOperatorPrefix()

        internal data class InfixR(
                val precedence: IntegerToken,
                val position: TextPosition
        ): GDLOperatorPrefix()

        internal data class UnaryL(
                val precedence: IntegerToken,
                val position: TextPosition
        ): GDLOperatorPrefix()

        internal data class UnaryR(
                val precedence: IntegerToken,
                val position: TextPosition
        ): GDLOperatorPrefix()
    }

    ////////////////////////////////////////////////////////////////////////////

    internal sealed class GDLAlternationRuleStatement {
        internal data class GDLRule(
                val operator: GDLOperatorPrefix?,
                val name: IdentifierToken,
                val patterns: List<GDLPattern>,
                val position: TextPosition
        ): GDLAlternationRuleStatement()

        internal data class GDLAlternationRule(
                val name: IdentifierToken,
                val statements: List<GDLAlternationRuleStatement>,
                val position: TextPosition
        ): GDLAlternationRuleStatement()
    }

    ////////////////////////////////////////////////////////////////////////////

    internal sealed class GDLPattern {
        internal data class GDLLiteral(
                val value: StringToken,
                val position: TextPosition
        ): GDLPattern()

        internal data class GDLRuleReference(
//                val inline: Boolean, TODO
                val reference: IdentifierToken,
//                val parameters: List<GDLPattern>?, TODO
                val position: TextPosition
        ): GDLPattern()

        internal data class GDLLabel(
                val patterns: List<GDLPattern>,
                val label: IdentifierToken,
                val position: TextPosition
        ): GDLPattern()

        internal data class GDLSepBy(
                val patterns: List<GDLPattern>,
                val atLeast1: Token?,
                val delimiter: List<GDLPattern>,
                val position: TextPosition
        ): GDLPattern()

        internal data class GDLMany(
                val atLeast1: Token?,
                val patterns: List<GDLPattern>,
                val label: IdentifierToken?,
                val position: TextPosition
        ): GDLPattern()

        internal data class GDLOptional(
                val patterns: List<GDLPattern>,
                val label: IdentifierToken?,
                val position: TextPosition
        ): GDLPattern()
    }
}
