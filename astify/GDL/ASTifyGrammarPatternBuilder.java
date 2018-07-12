package astify.GDL;

import astify.Capture;
import astify.Pattern;
import astify.token.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ASTifyGrammarPatternBuilder extends astify.PatternBuilder {
	
	public ASTifyGrammarPatternBuilder() {
		sequence("type", this::createType, 
			token(Word), 
			optional(sequence(
				symbol("?")
			)), 
			optional(sequence(
				symbol("["), 
				symbol("]")
			))
		);
		
		sequence("typed-name", this::createTypedName, 
			ref("type"), 
			token(Word)
		);
		
		sequence("matcher-target", this::createMatcherTarget, 
			token(Word)
		);
		
		sequence("pattern-list", this::createPatternList, 
			symbol(":"), 
			ref("root-pattern"), 
			list(ref("root-pattern"))
		);
		
		sequence("named-property-list", this::createNamedPropertyList, 
			token(Word), 
			symbol("("), 
			optional(sequence(
				delim(ref("typed-name"), sequence(symbol(",")))
			)), 
			symbol(")")
		);
		
		sequence("call", this::createCall, 
			token(Word), 
			symbol("("), 
			optional(sequence(
				delim(ref("parameter"), sequence(symbol(",")))
			)), 
			symbol(")")
		);
		
		sequence("reference", this::createReference, 
			token(Word)
		);
		
		define("parameter", one_of(
			ref("reference"), 
			ref("call")
		));
		
		sequence("type-reference", this::createTypeReference, 
			symbol("@"), 
			token(Word)
		);
		
		sequence("terminal", this::createTerminal, 
			token(String)
		);
		
		sequence("function-1", this::createFunction_1, 
			keyword("list"), 
			symbol("("), 
			ref("uncapturing-pattern"), 
			symbol(")")
		);
		
		sequence("function-2", this::createFunction_2, 
			keyword("delim"), 
			symbol("("), 
			ref("uncapturing-pattern"), 
			symbol(","), 
			ref("uncapturing-pattern"), 
			symbol(")")
		);
		
		defineInline("function", one_of(
			ref("function-1"), 
			ref("function-2")
		));
		
		sequence("matcher", this::createMatcher, 
			symbol("("), 
			ref("uncapturing-pattern"), 
			symbol("->"), 
			ref("matcher-target"), 
			symbol(")")
		);
		
		sequence("property-reference", this::createPropertyReference, 
			symbol("."), 
			token(Word), 
			optional(sequence(
				symbol("("), 
				list(ref("uncapturing-pattern")), 
				symbol(")")
			))
		);
		
		sequence("optional", this::createOptional, 
			symbol("["), 
			list(ref("pattern")), 
			symbol("]")
		);
		
		sequence("extend", this::createExtend, 
			keyword("extend"), 
			ref("named-property-list"), 
			symbol(":"), 
			ref("pattern-list"), 
			keyword("in"), 
			ref("call")
		);
		
		define("uncapturing-pattern", one_of(
			ref("type-reference"), 
			ref("terminal"), 
			ref("function")
		));
		
		define("pattern", one_of(
			ref("type-reference"), 
			ref("terminal"), 
			ref("function"), 
			ref("matcher"), 
			ref("property-reference")
		));
		
		define("root-pattern", one_of(
			ref("type-reference"), 
			ref("terminal"), 
			ref("function"), 
			ref("matcher"), 
			ref("property-reference"), 
			ref("optional")
		));
		
		sequence("abstract-type-definition", this::createAbstractTypeDefinition, 
			keyword("abstract"), 
			ref("named-property-list")
		);
		
		sequence("type-definition", this::createTypeDefinition, 
			ref("named-property-list"), 
			list(ref("pattern-list"))
		);
		
		sequence("union", this::createUnion, 
			keyword("union"), 
			token(Word), 
			symbol(":"), 
			delim(token(Word), sequence(symbol(":")))
		);
		
		sequence("alias-definition", this::createAliasDefinition, 
			keyword("alias"), 
			token(Word), 
			optional(sequence(
				symbol("("), 
				ref("typed-name"), 
				symbol(")")
			)), 
			ref("pattern-list"), 
			list(ref("pattern-list"))
		);
		
		sequence("extern-definition", this::createExternDefinition, 
			keyword("extern"), 
			ref("type"), 
			token(Word), 
			symbol("("), 
			optional(sequence(
				delim(ref("typed-name"), sequence(symbol(",")))
			)), 
			symbol(")"), 
			list(ref("pattern-list"))
		);
		
		sequence("apply-statement", this::createApplyStatement, 
			keyword("apply"), 
			ref("call"), 
			ref("pattern-list"), 
			list(ref("pattern-list"))
		);
		
		define("definition", one_of(
			ref("abstract-type-definition"), 
			ref("type-definition"), 
			ref("union"), 
			ref("alias-definition"), 
			ref("extern-definition")
		));
		
		define("statement", one_of(
			ref("apply-statement"), 
			ref("abstract-type-definition"), 
			ref("type-definition"), 
			ref("union"), 
			ref("alias-definition"), 
			ref("extern-definition")
		));
		
		sequence("grammar", this::createGrammar, 
			keyword("grammar"), 
			token(Word)
		);
		
		sequence("a-s-tify-grammar", this::createASTifyGrammar, 
			ref("grammar"), 
			list(ref("statement")), 
			token(EOF)
		);
	}
	
	@Override
	public Pattern getMain() {
		return lookup("a-s-tify-grammar");
	}
	
	// (@Word -> name), .optional('?'), .lst('[', ']')
	private Capture createType(List<Capture> captures) {
		Token name = ((Capture.TokenCapture) captures.get(0)).getToken();
		boolean optional = !(captures.get(1) instanceof Capture.EmptyCapture);
		boolean lst = !(captures.get(2) instanceof Capture.EmptyCapture);
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(2).getPosition());
		
		return new ASTifyGrammar.Type(spanningPosition, name, optional, lst);
	}
	
	// (@Type -> type), (@Word -> name)
	private Capture createTypedName(List<Capture> captures) {
		ASTifyGrammar.Type type = (ASTifyGrammar.Type) captures.get(0);
		Token name = ((Capture.TokenCapture) captures.get(1)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(1).getPosition());
		
		return new ASTifyGrammar.TypedName(spanningPosition, type, name);
	}
	
	// (@Word -> property)
	private Capture createMatcherTarget(List<Capture> captures) {
		Token property = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ASTifyGrammar.MatcherTarget(spanningPosition, property);
	}
	
	// ':', (@RootPattern -> patterns), (list(@RootPattern) -> patterns)
	private Capture createPatternList(List<Capture> captures) {
		List<ASTifyGrammar.RootPattern> patterns = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(2).getPosition());
		
		patterns.add((ASTifyGrammar.RootPattern) captures.get(1));
		
		for (Iterator<Capture> it = ((Capture.ListCapture) captures.get(2)).iterator(); it.hasNext(); ) {
			patterns.add((ASTifyGrammar.RootPattern) it.next());
		}
		
		return new ASTifyGrammar.PatternList(spanningPosition, patterns);
	}
	
	// (@Word -> name), '(', [(delim(@TypedName, ',') -> properties)], ')'
	private Capture createNamedPropertyList(List<Capture> captures) {
		Token name = ((Capture.TokenCapture) captures.get(0)).getToken();
		List<ASTifyGrammar.TypedName> properties = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(3).getPosition());
		
		if (!(captures.get(2) instanceof Capture.EmptyCapture)) {
			Capture.ListCapture subCaptures = (Capture.ListCapture) captures.get(2);
			
			for (Iterator<Capture> it = ((Capture.ListCapture) subCaptures.get(0)).iterator(); it.hasNext(); ) {
				properties.add((ASTifyGrammar.TypedName) it.next());
			}
		}
		
		return new ASTifyGrammar.NamedPropertyList(spanningPosition, name, properties);
	}
	
	// (@Word -> functionName), '(', [(delim(@Parameter, ',') -> parameters)], ')'
	private Capture createCall(List<Capture> captures) {
		Token functionName = ((Capture.TokenCapture) captures.get(0)).getToken();
		List<ASTifyGrammar.Parameter> parameters = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(3).getPosition());
		
		if (!(captures.get(2) instanceof Capture.EmptyCapture)) {
			Capture.ListCapture subCaptures = (Capture.ListCapture) captures.get(2);
			
			for (Iterator<Capture> it = ((Capture.ListCapture) subCaptures.get(0)).iterator(); it.hasNext(); ) {
				parameters.add((ASTifyGrammar.Parameter) it.next());
			}
		}
		
		return new ASTifyGrammar.Call(spanningPosition, functionName, parameters);
	}
	
	// (@Word -> reference)
	private Capture createReference(List<Capture> captures) {
		Token reference = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ASTifyGrammar.Reference(spanningPosition, reference);
	}
	
	// '@', (@Word -> type)
	private Capture createTypeReference(List<Capture> captures) {
		Token type = ((Capture.TokenCapture) captures.get(1)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(1).getPosition());
		
		return new ASTifyGrammar.TypeReference(spanningPosition, type);
	}
	
	// (@String -> terminal)
	private Capture createTerminal(List<Capture> captures) {
		Token terminal = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ASTifyGrammar.Terminal(spanningPosition, terminal);
	}
	
	// ('list' -> name), '(', (@UncapturingPattern -> patterns), ')'
	private Capture createFunction_1(List<Capture> captures) {
		Token name = ((Capture.TokenCapture) captures.get(0)).getToken();
		List<ASTifyGrammar.UncapturingPattern> patterns = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(3).getPosition());
		
		patterns.add((ASTifyGrammar.UncapturingPattern) captures.get(2));
		
		return new ASTifyGrammar.Function(spanningPosition, name, patterns);
	}
	
	// ('delim' -> name), '(', (@UncapturingPattern -> patterns), ',', (@UncapturingPattern -> patterns), ')'
	private Capture createFunction_2(List<Capture> captures) {
		Token name = ((Capture.TokenCapture) captures.get(0)).getToken();
		List<ASTifyGrammar.UncapturingPattern> patterns = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(5).getPosition());
		
		patterns.add((ASTifyGrammar.UncapturingPattern) captures.get(2));
		patterns.add((ASTifyGrammar.UncapturingPattern) captures.get(4));
		
		return new ASTifyGrammar.Function(spanningPosition, name, patterns);
	}
	
	// '(', (@UncapturingPattern -> source), '->', (@MatcherTarget -> targetProperty), ')'
	private Capture createMatcher(List<Capture> captures) {
		ASTifyGrammar.UncapturingPattern source = (ASTifyGrammar.UncapturingPattern) captures.get(1);
		ASTifyGrammar.MatcherTarget targetProperty = (ASTifyGrammar.MatcherTarget) captures.get(3);
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(4).getPosition());
		
		return new ASTifyGrammar.Matcher(spanningPosition, source, targetProperty);
	}
	
	// '.', (@Word -> property), ['(', (list(@UncapturingPattern) -> qualifier), ')']
	private Capture createPropertyReference(List<Capture> captures) {
		Token property = ((Capture.TokenCapture) captures.get(1)).getToken();
		List<ASTifyGrammar.UncapturingPattern> qualifier = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(2).getPosition());
		
		if (!(captures.get(2) instanceof Capture.EmptyCapture)) {
			Capture.ListCapture subCaptures = (Capture.ListCapture) captures.get(2);
			
			for (Iterator<Capture> it = ((Capture.ListCapture) subCaptures.get(1)).iterator(); it.hasNext(); ) {
				qualifier.add((ASTifyGrammar.UncapturingPattern) it.next());
			}
		}
		
		return new ASTifyGrammar.PropertyReference(spanningPosition, property, qualifier);
	}
	
	// '[', (list(@Pattern) -> patterns), ']'
	private Capture createOptional(List<Capture> captures) {
		List<ASTifyGrammar.Pattern> patterns = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(2).getPosition());
		
		for (Iterator<Capture> it = ((Capture.ListCapture) captures.get(1)).iterator(); it.hasNext(); ) {
			patterns.add((ASTifyGrammar.Pattern) it.next());
		}
		
		return new ASTifyGrammar.Optional(spanningPosition, patterns);
	}
	
	// 'extend', (@NamedPropertyList -> properties), ':', (@PatternList -> patterns), 'in', (@Call -> call)
	private Capture createExtend(List<Capture> captures) {
		ASTifyGrammar.NamedPropertyList properties = (ASTifyGrammar.NamedPropertyList) captures.get(1);
		List<ASTifyGrammar.PatternList> patterns = new ArrayList<>();
		ASTifyGrammar.Call call = (ASTifyGrammar.Call) captures.get(5);
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(5).getPosition());
		
		patterns.add((ASTifyGrammar.PatternList) captures.get(3));
		
		return new ASTifyGrammar.Extend(spanningPosition, properties, patterns, call);
	}
	
	// 'abstract', (@NamedPropertyList -> properties)
	private Capture createAbstractTypeDefinition(List<Capture> captures) {
		ASTifyGrammar.NamedPropertyList properties = (ASTifyGrammar.NamedPropertyList) captures.get(1);
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(1).getPosition());
		
		return new ASTifyGrammar.AbstractTypeDefinition(spanningPosition, properties);
	}
	
	// (@NamedPropertyList -> properties), (list(@PatternList) -> patternLists)
	private Capture createTypeDefinition(List<Capture> captures) {
		ASTifyGrammar.NamedPropertyList properties = (ASTifyGrammar.NamedPropertyList) captures.get(0);
		List<ASTifyGrammar.PatternList> patternLists = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(1).getPosition());
		
		for (Iterator<Capture> it = ((Capture.ListCapture) captures.get(1)).iterator(); it.hasNext(); ) {
			patternLists.add((ASTifyGrammar.PatternList) it.next());
		}
		
		return new ASTifyGrammar.TypeDefinition(spanningPosition, properties, patternLists);
	}
	
	// 'union', (@Word -> typename), ':', (delim(@Word, ':') -> subtypes)
	private Capture createUnion(List<Capture> captures) {
		Token typename = ((Capture.TokenCapture) captures.get(1)).getToken();
		List<Token> subtypes = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(3).getPosition());
		
		for (Iterator<Capture> it = ((Capture.ListCapture) captures.get(3)).iterator(); it.hasNext(); ) {
			subtypes.add(((Capture.TokenCapture) it.next()).getToken());
		}
		
		return new ASTifyGrammar.Union(spanningPosition, typename, subtypes);
	}
	
	// 'alias', (@Word -> name), ['(', (@TypedName -> property), ')'], (@PatternList -> patternLists), (list(@PatternList) -> patternLists)
	private Capture createAliasDefinition(List<Capture> captures) {
		Token name = ((Capture.TokenCapture) captures.get(1)).getToken();
		ASTifyGrammar.TypedName property = null;
		List<ASTifyGrammar.PatternList> patternLists = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(4).getPosition());
		
		if (!(captures.get(2) instanceof Capture.EmptyCapture)) {
			Capture.ListCapture subCaptures = (Capture.ListCapture) captures.get(2);
			
			property = (ASTifyGrammar.TypedName) subCaptures.get(1);
		}
		patternLists.add((ASTifyGrammar.PatternList) captures.get(3));
		
		for (Iterator<Capture> it = ((Capture.ListCapture) captures.get(4)).iterator(); it.hasNext(); ) {
			patternLists.add((ASTifyGrammar.PatternList) it.next());
		}
		
		return new ASTifyGrammar.AliasDefinition(spanningPosition, name, property, patternLists);
	}
	
	// 'extern', (@Type -> returnType), (@Word -> name), '(', [(delim(@TypedName, ',') -> parameters)], ')', (list(@PatternList) -> patternLists)
	private Capture createExternDefinition(List<Capture> captures) {
		ASTifyGrammar.Type returnType = (ASTifyGrammar.Type) captures.get(1);
		Token name = ((Capture.TokenCapture) captures.get(2)).getToken();
		List<ASTifyGrammar.TypedName> parameters = new ArrayList<>();
		List<ASTifyGrammar.PatternList> patternLists = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(6).getPosition());
		
		if (!(captures.get(4) instanceof Capture.EmptyCapture)) {
			Capture.ListCapture subCaptures = (Capture.ListCapture) captures.get(4);
			
			for (Iterator<Capture> it = ((Capture.ListCapture) subCaptures.get(0)).iterator(); it.hasNext(); ) {
				parameters.add((ASTifyGrammar.TypedName) it.next());
			}
		}
		
		for (Iterator<Capture> it = ((Capture.ListCapture) captures.get(6)).iterator(); it.hasNext(); ) {
			patternLists.add((ASTifyGrammar.PatternList) it.next());
		}
		
		return new ASTifyGrammar.ExternDefinition(spanningPosition, returnType, name, parameters, patternLists);
	}
	
	// 'apply', (@Call -> call), (@PatternList -> patternLists), (list(@PatternList) -> patternLists)
	private Capture createApplyStatement(List<Capture> captures) {
		ASTifyGrammar.Call call = (ASTifyGrammar.Call) captures.get(1);
		List<ASTifyGrammar.PatternList> patternLists = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(3).getPosition());
		
		patternLists.add((ASTifyGrammar.PatternList) captures.get(2));
		
		for (Iterator<Capture> it = ((Capture.ListCapture) captures.get(3)).iterator(); it.hasNext(); ) {
			patternLists.add((ASTifyGrammar.PatternList) it.next());
		}
		
		return new ASTifyGrammar.ApplyStatement(spanningPosition, call, patternLists);
	}
	
	// 'grammar', (@Word -> name)
	private Capture createGrammar(List<Capture> captures) {
		Token name = ((Capture.TokenCapture) captures.get(1)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(1).getPosition());
		
		return new ASTifyGrammar.Grammar(spanningPosition, name);
	}
	
	// (@Grammar -> _grammar), (list(@Statement) -> statements), @EOF
	private Capture createASTifyGrammar(List<Capture> captures) {
		ASTifyGrammar.Grammar _grammar = (ASTifyGrammar.Grammar) captures.get(0);
		List<ASTifyGrammar.Statement> statements = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(2).getPosition());
		
		for (Iterator<Capture> it = ((Capture.ListCapture) captures.get(1)).iterator(); it.hasNext(); ) {
			statements.add((ASTifyGrammar.Statement) it.next());
		}
		
		return new ASTifyGrammar(spanningPosition, _grammar, statements);
	}
}