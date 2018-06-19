package astify.GDL;

import astify.Capture;
import astify.Pattern;
import astify.PatternBuilder;
import astify.core.Position;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ASTifyGrammarPatternBuilder extends PatternBuilder {
    ASTifyGrammarPatternBuilder() {
        super();

        sequence("type", ASTifyGrammarPatternBuilder::createType,
                token(Word),
                optional(symbol("?")),
                optional(sequence(symbol("["), symbol("]")))
        );

        sequence("typed-name", ASTifyGrammarPatternBuilder::createTypedName,
                ref("type"),
                token(Word)
        );

        sequence("matcher-target", ASTifyGrammarPatternBuilder::createMatcherTarget,
                symbol("."),
                token(Word)
        );

        sequence("pattern-list", ASTifyGrammarPatternBuilder::createPatternList,
                list(ref("root-pattern"))
        );

        sequence("named-property-list", ASTifyGrammarPatternBuilder::createNamedPropertyList,
                token(Word),
                symbol("("),
                optional(delim(ref("typed-name"), symbol(","))),
                symbol(")")
        );

        sequence("type-reference", ASTifyGrammarPatternBuilder::createTypeReference,
                symbol("@"),
                token(Word)
        );

        sequence("terminal", ASTifyGrammarPatternBuilder::createTerminal,
                token(String)
        );

        sequence("function", ASTifyGrammarPatternBuilder::createFunction,
                token(Word),
                symbol("("),
                optional(
                        delim(
                                ref("uncapturing-pattern"),
                                symbol(",")
                        )
                ),
                symbol(")")
        );

        sequence("matcher", ASTifyGrammarPatternBuilder::createMatcher,
                symbol("("),
                ref("uncapturing-pattern"),
                operator("->"),
                ref("matcher-target"),
                symbol(")")
        );

        sequence("property-reference", ASTifyGrammarPatternBuilder::createPropertyReference,
                symbol("."),
                token(Word),
                optional(sequence(Capture.nth(1),
                        symbol("("),
                        list(ref("uncapturing-pattern")),
                        symbol(")")
                ))
        );

        sequence("optional", ASTifyGrammarPatternBuilder::createOptional,
                symbol("["),
                list(ref("pattern")),
                symbol("]")
        );

        sequence("abstract-type-definition", ASTifyGrammarPatternBuilder::createAbstractTypeDefinition,
                keyword("abstract"),
                ref("named-property-list")
        );

        sequence("type-definition", ASTifyGrammarPatternBuilder::createTypeDefinition,
                ref("named-property-list"),
                symbol(":"),
                delim(
                        ref("pattern-list"),
                        symbol(":")
                )
        );

        sequence("union", ASTifyGrammarPatternBuilder::createUnion,
                keyword("union"),
                token(Word),
                symbol(":"),
                delim(
                        token(Word),
                        symbol(":")
                )
        );

        sequence("grammar", ASTifyGrammarPatternBuilder::createGrammar,
                keyword("grammar"),
                token(Word)
        );

        sequence("ASTify-grammar", ASTifyGrammarPatternBuilder::createASTifyGrammar,
                ref("grammar"),
                list(ref("definition")),
                eof()
        );

        define("uncapturing-pattern", one_of(
                ref("type-reference"),
                ref("terminal"),
                ref("function")
        ));

        define("pattern", one_of(
                ref("uncapturing-pattern"),
                ref("matcher"),
                ref("property-reference")
        ));

        define("root-pattern", one_of(
                ref("pattern"),
                ref("optional")
        ));

        define("definition", one_of(
                ref("abstract-type-definition"),
                ref("type-definition"),
                ref("union")
        ));
    }

    private static Position pos(List<Capture> captures) {
        return captures.get(0).spanningPosition.to(captures.get(captures.size() - 1).spanningPosition);
    }

    private static astify.token.Token tok(Capture capture) {
        return ((Capture.TokenCapture) capture).getToken();
    }

    private static boolean notEmpty(Capture capture) {
        return !(capture instanceof Capture.EmptyCapture);
    }

    private static<T> List<T> list(Capture capture) {
        if (capture instanceof Capture.EmptyCapture) return new ArrayList<>();

        assert capture instanceof Capture.ListCapture;

        List<T> result = new ArrayList<>();

        for (Iterator it = ((Capture.ListCapture) capture).iterator(); it.hasNext(); ) {
            result.add((T) it.next());
        }

        return result;
    }

    private static Capture createType(List<Capture> captures) {
        return new ASTifyGrammar.Type(pos(captures),
                tok(captures.get(0)),
                notEmpty(captures.get(1)),
                notEmpty(captures.get(2))
        );
    }

    private static Capture createTypedName(List<Capture> captures) {
        return new ASTifyGrammar.TypedName(pos(captures),
                (ASTifyGrammar.Type) captures.get(0),
                tok(captures.get(1))
        );
    }

    private static Capture createMatcherTarget(List<Capture> captures) {
        return new ASTifyGrammar.MatcherTarget(pos(captures),
                tok(captures.get(1))
        );
    }

    private static Capture createPatternList(List<Capture> captures) {
        return new ASTifyGrammar.PatternList(pos(captures),
                list(captures.get(0))
        );
    }

    private static Capture createNamedPropertyList(List<Capture> captures) {
        return new ASTifyGrammar.NamedPropertyList(pos(captures),
                tok(captures.get(0)),
                list(captures.get(2))
        );
    }

    private static Capture createTypeReference(List<Capture> captures) {
        return new ASTifyGrammar.TypeReference(pos(captures),
                tok(captures.get(1))
        );
    }

    private static Capture createTerminal(List<Capture> captures) {
        return new ASTifyGrammar.Terminal(pos(captures),
                tok(captures.get(0))
        );
    }

    private static Capture createFunction(List<Capture> captures) {
        return new ASTifyGrammar.Function(pos(captures),
                tok(captures.get(0)),
                list(captures.get(2))
        );
    }

    private static Capture createMatcher(List<Capture> captures) {
        return new ASTifyGrammar.Matcher(pos(captures),
                (ASTifyGrammar.UncapturingPattern) captures.get(1),
                (ASTifyGrammar.MatcherTarget) captures.get(3)
        );
    }

    private static Capture createPropertyReference(List<Capture> captures) {

        /*
                optional(sequence(Capture.nth(1),
                        symbol("("),
                        list(ref("uncapturing-pattern")),
                        symbol(")")
                ))
         */
        return new ASTifyGrammar.PropertyReference(pos(captures),
                tok(captures.get(1)),
                list(captures.get(2))
        );
    }

    private static Capture createOptional(List<Capture> captures) {
        return new ASTifyGrammar.Optional(pos(captures),
                list(captures.get(1))
        );
    }

    private static Capture createAbstractTypeDefinition(List<Capture> captures) {
        return new ASTifyGrammar.AbstractTypeDefinition(pos(captures),
                (ASTifyGrammar.NamedPropertyList) captures.get(1)
        );
    }

    private static Capture createTypeDefinition(List<Capture> captures) {
        return new ASTifyGrammar.TypeDefinition(pos(captures),
                (ASTifyGrammar.NamedPropertyList) captures.get(0),
                list(captures.get(2))
        );
    }

    private static Capture createUnion(List<Capture> captures) {
        List<astify.token.Token>  subtypes = new ArrayList<>();

        for (Iterator it = ((Capture.ListCapture) captures.get(3)).iterator(); it.hasNext(); ) {
            Capture.TokenCapture c = (Capture.TokenCapture) it.next();
            subtypes.add(c.getToken());
        }

        return new ASTifyGrammar.Union(pos(captures),
                tok(captures.get(1)),
                subtypes
        );
    }

    private static Capture createGrammar(List<Capture> captures) {
        return new ASTifyGrammar.Grammar(pos(captures),
                tok(captures.get(1))
        );
    }

    private static Capture createASTifyGrammar(List<Capture> captures) {
        return new ASTifyGrammar(pos(captures),
                (ASTifyGrammar.Grammar) captures.get(0),
                list(captures.get(1))
        );
    }

    @Override public Pattern getMain() {
        return lookup("ASTify-grammar");
    }
}