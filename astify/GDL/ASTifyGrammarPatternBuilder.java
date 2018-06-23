package astify.GDL;

import astify.Capture;
import astify.Pattern;
import astify.token.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ASTifyGrammarPatternBuilder extends astify.PatternBuilder {
	
	public ASTifyGrammarPatternBuilder() {
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
		
		sequence("type", this::createType,
			token(Word),
			optional(sequence(
				operator("?")
			)),
			optional(sequence(
				operator("["),
				operator("]")
			))
		);
		
		sequence("typed-name", this::createTypedName,
			ref("type"),
			token(Word)
		);
		
		sequence("matcher-target", this::createMatcherTarget,
			operator("."),
			token(Word)
		);
		
		sequence("pattern-list", this::createPatternList,
			list(ref("root-pattern"))
		);
		
		sequence("named-property-list", this::createNamedPropertyList,
			token(Word),
			operator("("),
			optional(sequence(
				delim(ref("typed-name"), operator(","))
			)),
			operator(")")
		);
		
		sequence("type-reference", this::createTypeReference,
			operator("@"),
			token(Word)
		);
		
		sequence("terminal", this::createTerminal,
			token(String)
		);
		
		sequence("function(0)", this::createFunction0,
			keyword("list"),
			operator("("),
			ref("pattern"),
			operator(")")
		);
		
		sequence("function(1)", this::createFunction1,
			keyword("delim"),
			operator("("),
			ref("pattern"),
			operator(","),
			ref("pattern"),
			operator(")")
		);
		
		defineInline("function", one_of(
			ref("function(0)"),
			ref("function(1)")
		));
		
		sequence("matcher", this::createMatcher,
			operator("("),
			ref("uncapturing-pattern"),
			operator("->"),
			ref("matcher-target"),
			operator(")")
		);
		
		sequence("property-reference", this::createPropertyReference,
			operator("."),
			token(Word),
			optional(sequence(
				operator("("),
				list(ref("uncapturing-pattern")),
				operator(")")
			))
		);
		
		sequence("optional", this::createOptional,
			operator("["),
			list(ref("pattern")),
			operator("]")
		);
		
		sequence("abstract-type-definition", this::createAbstractTypeDefinition,
			keyword("abstract"),
			ref("named-property-list")
		);
		
		sequence("type-definition", this::createTypeDefinition,
			ref("named-property-list"),
			operator(":"),
			delim(ref("pattern-list"), operator(":"))
		);
		
		sequence("union", this::createUnion,
			keyword("union"),
			token(Word),
			operator(":"),
			delim(token(Word), operator(":"))
		);
		
		sequence("grammar", this::createGrammar,
			keyword("grammar"),
			token(Word)
		);
		
		sequence("a-s-tify-grammar", this::createASTifyGrammar,
			ref("grammar"),
			list(ref("definition")),
			token(EOF)
		);
	}
	
	@Override
	public Pattern getMain() {
		return lookup("a-s-tify-grammar");
	}
	
	// [(Word -> name), (['?'] -> optional), (['[', ']'] -> lst)]
	private Capture createType(List<Capture> captures) {
		Token name = null;
		Boolean optional = null;
		Boolean lst = null;
		name = ((Capture.TokenCapture) captures.get(0)).getToken();
		optional = !(captures.get(1) instanceof Capture.EmptyCapture);
		lst = !(captures.get(2) instanceof Capture.EmptyCapture);
		
		return new ASTifyGrammar.Type(captures.get(0).spanningPosition.to(captures.get(2).spanningPosition), name, optional, lst);
	}
	
	// [(Type -> type), (Word -> name)]
	private Capture createTypedName(List<Capture> captures) {
		ASTifyGrammar.Type type = null;
		Token name = null;
		type = (ASTifyGrammar.Type) captures.get(0);
		name = ((Capture.TokenCapture) captures.get(1)).getToken();
		
		return new ASTifyGrammar.TypedName(captures.get(0).spanningPosition.to(captures.get(1).spanningPosition), type, name);
	}
	
	// ['.', (Word -> property)]
	private Capture createMatcherTarget(List<Capture> captures) {
		Token property = null;
		property = ((Capture.TokenCapture) captures.get(1)).getToken();
		
		return new ASTifyGrammar.MatcherTarget(captures.get(0).spanningPosition.to(captures.get(1).spanningPosition), property);
	}
	
	// [(list(RootPattern) -> patterns)]
	private Capture createPatternList(List<Capture> captures) {
		List<ASTifyGrammar.RootPattern> patterns = new ArrayList<>();
		for (Iterator it = ((Capture.ListCapture) captures.get(0)).iterator(); it.hasNext(); ) {
			patterns.add((ASTifyGrammar.RootPattern) it.next());
		}
		
		return new ASTifyGrammar.PatternList(captures.get(0).spanningPosition, patterns);
	}
	
	// [(Word -> name), '(', [(delim(TypedName, ',') -> properties)], ')']
	private Capture createNamedPropertyList(List<Capture> captures) {
		Token name = null;
		List<ASTifyGrammar.TypedName> properties = new ArrayList<>();
		name = ((Capture.TokenCapture) captures.get(0)).getToken();
		
		if (!(captures.get(2) instanceof Capture.EmptyCapture)) {
			List<Capture> subCaptures = (List<Capture>) ((Capture.ListCapture) captures.get(2)).all();for (Iterator it = ((Capture.ListCapture) subCaptures.get(0)).iterator(); it.hasNext(); ) {
				properties.add((ASTifyGrammar.TypedName) it.next());
			}
		}
		
		return new ASTifyGrammar.NamedPropertyList(captures.get(0).spanningPosition.to(captures.get(3).spanningPosition), name, properties);
	}
	
	// ['@', (Word -> type)]
	private Capture createTypeReference(List<Capture> captures) {
		Token type = null;
		type = ((Capture.TokenCapture) captures.get(1)).getToken();
		
		return new ASTifyGrammar.TypeReference(captures.get(0).spanningPosition.to(captures.get(1).spanningPosition), type);
	}
	
	// [(String -> terminal)]
	private Capture createTerminal(List<Capture> captures) {
		Token terminal = null;
		terminal = ((Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ASTifyGrammar.Terminal(captures.get(0).spanningPosition, terminal);
	}
	
	// ['list', '(', (Pattern -> patterns), ')']
	private Capture createFunction0(List<Capture> captures) {
		Token name = null;
		List<ASTifyGrammar.UncapturingPattern> patterns = new ArrayList<>();
		patterns.add((ASTifyGrammar.UncapturingPattern) captures.get(2));
		
		return new ASTifyGrammar.Function(captures.get(0).spanningPosition.to(captures.get(3).spanningPosition), name, patterns);
	}
	
	// ['delim', '(', (Pattern -> patterns), ',', (Pattern -> patterns), ')']
	private Capture createFunction1(List<Capture> captures) {
		Token name = null;
		List<ASTifyGrammar.UncapturingPattern> patterns = new ArrayList<>();
		patterns.add((ASTifyGrammar.UncapturingPattern) captures.get(2));
		patterns.add((ASTifyGrammar.UncapturingPattern) captures.get(4));
		
		return new ASTifyGrammar.Function(captures.get(0).spanningPosition.to(captures.get(5).spanningPosition), name, patterns);
	}
	
	// ['(', (UncapturingPattern -> source), '->', (MatcherTarget -> target), ')']
	private Capture createMatcher(List<Capture> captures) {
		ASTifyGrammar.UncapturingPattern source = null;
		ASTifyGrammar.MatcherTarget target = null;
		source = (ASTifyGrammar.UncapturingPattern) captures.get(1);
		target = (ASTifyGrammar.MatcherTarget) captures.get(3);
		
		return new ASTifyGrammar.Matcher(captures.get(0).spanningPosition.to(captures.get(4).spanningPosition), source, target);
	}
	
	// ['.', (Word -> property), ['(', (list(UncapturingPattern) -> qualifier), ')']]
	private Capture createPropertyReference(List<Capture> captures) {
		Token property = null;
		List<ASTifyGrammar.UncapturingPattern> qualifier = new ArrayList<>();
		property = ((Capture.TokenCapture) captures.get(1)).getToken();
		
		if (!(captures.get(2) instanceof Capture.EmptyCapture)) {
			List<Capture> subCaptures = (List<Capture>) ((Capture.ListCapture) captures.get(2)).all();for (Iterator it = ((Capture.ListCapture) subCaptures.get(1)).iterator(); it.hasNext(); ) {
				qualifier.add((ASTifyGrammar.UncapturingPattern) it.next());
			}
		}
		
		return new ASTifyGrammar.PropertyReference(captures.get(0).spanningPosition.to(captures.get(2).spanningPosition), property, qualifier);
	}
	
	// ['[', (list(Pattern) -> patterns), ']']
	private Capture createOptional(List<Capture> captures) {
		List<ASTifyGrammar.Pattern> patterns = new ArrayList<>();
		for (Iterator it = ((Capture.ListCapture) captures.get(1)).iterator(); it.hasNext(); ) {
			patterns.add((ASTifyGrammar.Pattern) it.next());
		}
		
		return new ASTifyGrammar.Optional(captures.get(0).spanningPosition.to(captures.get(2).spanningPosition), patterns);
	}
	
	// ['abstract', (NamedPropertyList -> properties)]
	private Capture createAbstractTypeDefinition(List<Capture> captures) {
		ASTifyGrammar.NamedPropertyList properties = null;
		properties = (ASTifyGrammar.NamedPropertyList) captures.get(1);
		
		return new ASTifyGrammar.AbstractTypeDefinition(captures.get(0).spanningPosition.to(captures.get(1).spanningPosition), properties);
	}
	
	// [(NamedPropertyList -> properties), ':', (delim(PatternList, ':') -> patterns)]
	private Capture createTypeDefinition(List<Capture> captures) {
		ASTifyGrammar.NamedPropertyList properties = null;
		List<ASTifyGrammar.PatternList> patterns = new ArrayList<>();
		properties = (ASTifyGrammar.NamedPropertyList) captures.get(0);
		for (Iterator it = ((Capture.ListCapture) captures.get(2)).iterator(); it.hasNext(); ) {
			patterns.add((ASTifyGrammar.PatternList) it.next());
		}
		
		return new ASTifyGrammar.TypeDefinition(captures.get(0).spanningPosition.to(captures.get(2).spanningPosition), properties, patterns);
	}
	
	// ['union', (Word -> typename), ':', (delim(Word, ':') -> subtypes)]
	private Capture createUnion(List<Capture> captures) {
		Token typename = null;
		List<Token> subtypes = new ArrayList<>();
		typename = ((Capture.TokenCapture) captures.get(1)).getToken();
		for (Iterator it = ((Capture.ListCapture) captures.get(3)).iterator(); it.hasNext(); ) {
			subtypes.add(((Capture.TokenCapture) it.next()).getToken());
		}
		
		return new ASTifyGrammar.Union(captures.get(0).spanningPosition.to(captures.get(3).spanningPosition), typename, subtypes);
	}
	
	// ['grammar', (Word -> name)]
	private Capture createGrammar(List<Capture> captures) {
		Token name = null;
		name = ((Capture.TokenCapture) captures.get(1)).getToken();
		
		return new ASTifyGrammar.Grammar(captures.get(0).spanningPosition.to(captures.get(1).spanningPosition), name);
	}
	
	// [(Grammar -> _grammar), (list(Definition) -> definitions), EOF]
	private Capture createASTifyGrammar(List<Capture> captures) {
		ASTifyGrammar.Grammar _grammar = null;
		List<ASTifyGrammar.Definition> definitions = new ArrayList<>();
		_grammar = (ASTifyGrammar.Grammar) captures.get(0);
		for (Iterator it = ((Capture.ListCapture) captures.get(1)).iterator(); it.hasNext(); ) {
			definitions.add((ASTifyGrammar.Definition) it.next());
		}
		
		return new ASTifyGrammar(captures.get(0).spanningPosition.to(captures.get(2).spanningPosition), _grammar, definitions);
	}
}