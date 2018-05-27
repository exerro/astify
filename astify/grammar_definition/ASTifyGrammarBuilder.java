package astify.grammar_definition;

import astify.Pattern;
import astify.PatternBuilder;

public class ASTifyGrammarBuilder extends PatternBuilder {
    public ASTifyGrammarBuilder() {
        super();
        define();
    }

    protected void define() {
        sequence("union", ASTifyGrammar.Union::create,
                keyword("union"),
                token(Word),
                symbol(":"),
                delim(token(Word), symbol(":"))
        );

        sequence("type", ASTifyGrammar.Type::create,
                token(Word),
                optional(symbol("?")),
                optional(sequence(symbol("["), symbol("]")))
        );

        sequence("typed-name", ASTifyGrammar.TypedName::create,
                ref("type"),
                token(Word)
        );

        sequence("named-property-list", ASTifyGrammar.NamedPropertyList::create,
                token(Word),
                symbol("("),
                optional(delim(ref("typed-name"), symbol(","))),
                symbol(")")
        );

        sequence("abstract-type-definition", ASTifyGrammar.AbstractTypeDefinition::create,
                keyword("abstract"),
                ref("named-property-list")
        );

        sequence("parameter-reference", ASTifyGrammar.ParameterReference::create,
                symbol("."),
                token(Word),
                optional(sequence(
                        symbol("("),
                        list(ref("pattern")),
                        symbol(")")
                ))
        );

        sequence("terminal", ASTifyGrammar.Terminal::create,
                token(String)
        );

        sequence("optional", ASTifyGrammar.Optional::create,
                symbol("["),
                list(ref("pattern")),
                symbol("]")
        );

        sequence("pattern-list", ASTifyGrammar.PatternList::create,
                list(ref("pattern"))
        );

        sequence("type-definition", ASTifyGrammar.TypeDefinition::create,
                ref("named-property-list"),
                symbol(":"),
                delim(ref("pattern-list"), symbol(":"))
        );

        sequence("grammar", ASTifyGrammar.Grammar::create,
                keyword("grammar"),
                token(Word)
        );

        sequence("ASTify-grammar", ASTifyGrammar::create,
                ref("grammar"),
                list(ref("definition")),
                eof()
        );

        define("pattern", one_of(
                ref("parameter-reference"),
                ref("terminal"),
                ref("optional")
        ));

        define("definition", one_of(
                ref("type-definition"),
                ref("abstract-type-definition"),
                ref("union")
        ));
    }

    @Override public Pattern getMain() {
        return lookup("ASTify-grammar");
    }
}