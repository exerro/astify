import parser.ParseNFA

internal data class GDLIR(
        val symbolName: String,
        val keywordTransform: Pair<String, String>,
        val tokens: List<GDL.GDLStatement.GDLTokenStatement.GDLTokenDefinition>,
        val rules: List<GDLIRRule>
)

internal data class GDLIRRule(
        val name: String,
        internal val toResolve: GDL.GDLStatement.GDLRuleStatement
) {
    val parseRule: ParseNFA = ParseNFA(ParseNFA.State(), ParseNFA.State())
}
