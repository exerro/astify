import glh.GLOBAL_SCOPE
import glh.PartialResolver
import glh.ScopeLabel
import glh.ScopeLabelOrdering

internal class GDLPartialResolver: PartialResolver<
        GDL.GDL,
        GDLSourceID,
        GDL.GDLStatement,
        IdentifierToken,
        GDLPartialStatement,
        GDLPartialResolver.Result,
        GDLPartialResolver.Context
>() {
    override fun apply(
            entitySource: GDL.GDLStatement,
            previousResult: Result,
            context: Context
    ): Pair<Result, Context> {
        TODO("not implemented")
    }

    override fun getContext(source: GDL.GDL): Context {
        return Context()
    }

    override fun getEmptyResult(source: GDL.GDL, orderings: List<ScopeLabelOrdering<GDLSourceID>>): Result {
        return Result(
                source.grammarName,
                null,
                null,
                listOf(), listOf(), listOf())
    }

    override fun getEntitySources(source: GDL.GDL): List<GDL.GDLStatement> {
        return source.statements
    }

    ////////////////////////////////////////////////////////////////////////////

    data class Result(
            val grammarName: IdentifierToken,
            val keywordTransform: GDL.GDLStatement.GDLTokenStatement.GDLKeywordTransform?,
            val symbolSpecifier: GDL.GDLStatement.GDLTokenStatement.GDLSymbolSpecifier?,
            override val scopeOrderings: List<ScopeLabelOrdering<GDLSourceID>>,
            override val allScopes: List<ScopeLabel>,
            override val definitions: List<ScopedEntityDefinition<IdentifierToken, GDLPartialStatement>>
    ): ScopedEntityList<GDLSourceID, IdentifierToken, GDLPartialStatement>(
            scopeOrderings,
            allScopes,
            definitions
    ) {

    }

    class Context(
            val nextScopeLabel: ScopeLabel = GLOBAL_SCOPE + 1
    )
}
