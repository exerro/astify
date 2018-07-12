package GDL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ASTifyGrammarPatternBuilderBase extends astify.PatternBuilder {
	
	ASTifyGrammarPatternBuilderBase() {
		super();
		
		initASTifyGrammar();
	}
	
	private void initASTifyGrammar() {
		sequence("type", this::__createType,
			token(Word),
			optional(sequence(
				symbol("?")
			)),
			optional(sequence(
				symbol("["),
				symbol("]")
			))
		);
		
		sequence("typed-name", this::__createTypedName,
			ref("type"),
			token(Word)
		);
		
		sequence("matcher-target", this::__createMatcherTarget,
			token(Word)
		);
		
		sequence("pattern-list", this::__createPatternList,
			symbol(":"),
			ref("root-pattern"),
			list(ref("root-pattern"))
		);
		
		sequence("named-property-list", this::__createNamedPropertyList,
			token(Word),
			symbol("("),
			optional(sequence(
				delim(ref("typed-name"), sequence(symbol(",")))
			)),
			symbol(")")
		);
		
		sequence("call", this::__createCall,
			token(Word),
			symbol("("),
			optional(sequence(
				delim(ref("parameter"), sequence(symbol(",")))
			)),
			symbol(")")
		);
		
		sequence("reference", this::__createReference,
			token(Word)
		);
		
		define("parameter", one_of(
			ref("reference"),
			ref("call")
		));
		
		sequence("type-reference", this::__createTypeReference,
			symbol("@"),
			token(Word)
		);
		
		sequence("terminal", this::__createTerminal,
			token(String)
		);
		
		sequence("function#1", this::__createFunction1,
			keyword("list"),
			symbol("("),
			ref("uncapturing-pattern"),
			symbol(")")
		);
		
		sequence("function#2", this::__createFunction2,
			keyword("delim"),
			symbol("("),
			ref("uncapturing-pattern"),
			symbol(","),
			ref("uncapturing-pattern"),
			symbol(")")
		);
		
		defineInline("function", one_of(
			ref("function#1"),
			ref("function#2")
		));
		
		sequence("matcher", this::__createMatcher,
			symbol("("),
			ref("uncapturing-pattern"),
			symbol("->"),
			ref("matcher-target"),
			symbol(")")
		);
		
		sequence("property-reference", this::__createPropertyReference,
			symbol("."),
			token(Word),
			optional(sequence(
				symbol("("),
				list(ref("uncapturing-pattern")),
				symbol(")")
			))
		);
		
		sequence("optional", this::__createOptional,
			symbol("["),
			list(ref("pattern")),
			symbol("]")
		);
		
		sequence("extend", this::__createExtend,
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
		
		sequence("abstract-type-definition", this::__createAbstractTypeDefinition,
			keyword("abstract"),
			ref("named-property-list")
		);
		
		sequence("type-definition", this::__createTypeDefinition,
			ref("named-property-list"),
			list(ref("pattern-list"))
		);
		
		sequence("union", this::__createUnion,
			keyword("union"),
			token(Word),
			symbol(":"),
			delim(token(Word), sequence(symbol(":")))
		);
		
		sequence("alias-definition", this::__createAliasDefinition,
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
		
		sequence("extern-definition", this::__createExternDefinition,
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
		
		sequence("apply-statement", this::__createApplyStatement,
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
		
		sequence("grammar", this::__createGrammar,
			keyword("grammar"),
			token(Word)
		);
		
		sequence("a-s-tify-grammar", this::__createASTifyGrammar,
			ref("grammar"),
			list(ref("statement")),
			token(EOF)
		); 
	}
	
	@Override
	public astify.Pattern getMain() {
		return lookup("a-s-tify-grammar");
	}
	
	private astify.Capture __createType(List<astify.Capture> captures) {
		astify.token.Token name = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		Boolean optional = (Boolean) !(captures.get(1) instanceof astify.Capture.EmptyCapture);
		Boolean lst = (Boolean) !(captures.get(2) instanceof astify.Capture.EmptyCapture);
		
		return new ASTifyGrammar.Type(captures.get(0).getPosition().to(captures.get(2).getPosition()), name, optional, lst);
	}
	
	private astify.Capture __createTypedName(List<astify.Capture> captures) {
		astify.token.Token name = ((astify.Capture.TokenCapture) captures.get(1)).getToken();
		ASTifyGrammar.Type type = (ASTifyGrammar.Type) captures.get(0);
		
		return new ASTifyGrammar.TypedName(captures.get(0).getPosition().to(captures.get(1).getPosition()), type, name);
	}
	
	private astify.Capture __createMatcherTarget(List<astify.Capture> captures) {
		astify.token.Token property = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ASTifyGrammar.MatcherTarget(captures.get(0).getPosition(), property);
	}
	
	private astify.Capture __createPatternList(List<astify.Capture> captures) {
		List<ASTifyGrammar.RootPattern> patterns = new ArrayList<>();
		
		patterns.add((ASTifyGrammar.RootPattern) captures.get(1));
		
		for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) captures.get(2)).iterator(); it.hasNext(); ) {
			patterns.add((ASTifyGrammar.RootPattern) it.next());
		}
		
		return new ASTifyGrammar.PatternList(captures.get(0).getPosition().to(captures.get(2).getPosition()), patterns);
	}
	
	private astify.Capture __createNamedPropertyList(List<astify.Capture> captures) {
		astify.token.Token name = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		List<ASTifyGrammar.TypedName> properties = new ArrayList<>();
		
		if (!(captures.get(2) instanceof astify.Capture.EmptyCapture)) {
			List<astify.Capture> subCaptures = ((astify.Capture.ListCapture) captures.get(2)).all();
			
			for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) subCaptures.get(0)).iterator(); it.hasNext(); ) {
				properties.add((ASTifyGrammar.TypedName) it.next());
			}
			
			}
		
		return new ASTifyGrammar.NamedPropertyList(captures.get(0).getPosition().to(captures.get(3).getPosition()), name, properties);
	}
	
	private astify.Capture __createCall(List<astify.Capture> captures) {
		astify.token.Token functionName = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		List<ASTifyGrammar.Parameter> parameters = new ArrayList<>();
		
		if (!(captures.get(2) instanceof astify.Capture.EmptyCapture)) {
			List<astify.Capture> subCaptures = ((astify.Capture.ListCapture) captures.get(2)).all();
			
			for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) subCaptures.get(0)).iterator(); it.hasNext(); ) {
				parameters.add((ASTifyGrammar.Parameter) it.next());
			}
			
			}
		
		return new ASTifyGrammar.Call(captures.get(0).getPosition().to(captures.get(3).getPosition()), functionName, parameters);
	}
	
	private astify.Capture __createReference(List<astify.Capture> captures) {
		astify.token.Token reference = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ASTifyGrammar.Reference(captures.get(0).getPosition(), reference);
	}
	
	private astify.Capture __createTypeReference(List<astify.Capture> captures) {
		astify.token.Token type = ((astify.Capture.TokenCapture) captures.get(1)).getToken();
		
		return new ASTifyGrammar.TypeReference(captures.get(0).getPosition().to(captures.get(1).getPosition()), type);
	}
	
	private astify.Capture __createTerminal(List<astify.Capture> captures) {
		astify.token.Token terminal = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ASTifyGrammar.Terminal(captures.get(0).getPosition(), terminal);
	}
	
	private astify.Capture __createFunction1(List<astify.Capture> captures) {
		List<ASTifyGrammar.UncapturingPattern> patterns = new ArrayList<>();
		astify.token.Token name = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		patterns.add((ASTifyGrammar.UncapturingPattern) captures.get(2));
		
		return new ASTifyGrammar.Function(captures.get(0).getPosition().to(captures.get(3).getPosition()), name, patterns);
	}
	
	private astify.Capture __createFunction2(List<astify.Capture> captures) {
		List<ASTifyGrammar.UncapturingPattern> patterns = new ArrayList<>();
		astify.token.Token name = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		patterns.add((ASTifyGrammar.UncapturingPattern) captures.get(2));
		patterns.add((ASTifyGrammar.UncapturingPattern) captures.get(4));
		
		return new ASTifyGrammar.Function(captures.get(0).getPosition().to(captures.get(5).getPosition()), name, patterns);
	}
	
	private astify.Capture __createMatcher(List<astify.Capture> captures) {
		ASTifyGrammar.UncapturingPattern source = (ASTifyGrammar.UncapturingPattern) captures.get(1);
		ASTifyGrammar.MatcherTarget targetProperty = (ASTifyGrammar.MatcherTarget) captures.get(3);
		
		return new ASTifyGrammar.Matcher(captures.get(0).getPosition().to(captures.get(4).getPosition()), source, targetProperty);
	}
	
	private astify.Capture __createPropertyReference(List<astify.Capture> captures) {
		List<ASTifyGrammar.UncapturingPattern> qualifier = new ArrayList<>();
		astify.token.Token property = ((astify.Capture.TokenCapture) captures.get(1)).getToken();
		
		if (!(captures.get(2) instanceof astify.Capture.EmptyCapture)) {
			List<astify.Capture> subCaptures = ((astify.Capture.ListCapture) captures.get(2)).all();
			
			for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) subCaptures.get(1)).iterator(); it.hasNext(); ) {
				qualifier.add((ASTifyGrammar.UncapturingPattern) it.next());
			}
			
			}
		
		return new ASTifyGrammar.PropertyReference(captures.get(0).getPosition().to(captures.get(2).getPosition()), property, qualifier);
	}
	
	private astify.Capture __createOptional(List<astify.Capture> captures) {
		List<ASTifyGrammar.Pattern> patterns = new ArrayList<>();
		
		for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) captures.get(1)).iterator(); it.hasNext(); ) {
			patterns.add((ASTifyGrammar.Pattern) it.next());
		}
		
		return new ASTifyGrammar.Optional(captures.get(0).getPosition().to(captures.get(2).getPosition()), patterns);
	}
	
	private astify.Capture __createExtend(List<astify.Capture> captures) {
		ASTifyGrammar.Call call = (ASTifyGrammar.Call) captures.get(5);
		List<ASTifyGrammar.PatternList> patterns = new ArrayList<>();
		ASTifyGrammar.NamedPropertyList properties = (ASTifyGrammar.NamedPropertyList) captures.get(1);
		
		patterns.add((ASTifyGrammar.PatternList) captures.get(3));
		
		return new ASTifyGrammar.Extend(captures.get(0).getPosition().to(captures.get(5).getPosition()), properties, patterns, call);
	}
	
	private astify.Capture __createAbstractTypeDefinition(List<astify.Capture> captures) {
		ASTifyGrammar.NamedPropertyList properties = (ASTifyGrammar.NamedPropertyList) captures.get(1);
		
		return new ASTifyGrammar.AbstractTypeDefinition(captures.get(0).getPosition().to(captures.get(1).getPosition()), properties);
	}
	
	private astify.Capture __createTypeDefinition(List<astify.Capture> captures) {
		List<ASTifyGrammar.PatternList> patternLists = new ArrayList<>();
		ASTifyGrammar.NamedPropertyList properties = (ASTifyGrammar.NamedPropertyList) captures.get(0);
		
		for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) captures.get(1)).iterator(); it.hasNext(); ) {
			patternLists.add((ASTifyGrammar.PatternList) it.next());
		}
		
		return new ASTifyGrammar.TypeDefinition(captures.get(0).getPosition().to(captures.get(1).getPosition()), properties, patternLists);
	}
	
	private astify.Capture __createUnion(List<astify.Capture> captures) {
		astify.token.Token typename = ((astify.Capture.TokenCapture) captures.get(1)).getToken();
		List<astify.token.Token> subtypes = new ArrayList<>();
		
		for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) captures.get(3)).iterator(); it.hasNext(); ) {
			subtypes.add(((astify.Capture.TokenCapture) it.next()).getToken());
		}
		
		return new ASTifyGrammar.Union(captures.get(0).getPosition().to(captures.get(3).getPosition()), typename, subtypes);
	}
	
	private astify.Capture __createAliasDefinition(List<astify.Capture> captures) {
		astify.token.Token name = ((astify.Capture.TokenCapture) captures.get(1)).getToken();
		ASTifyGrammar.TypedName property = null;
		List<ASTifyGrammar.PatternList> patternLists = new ArrayList<>();
		
		if (!(captures.get(2) instanceof astify.Capture.EmptyCapture)) {
			List<astify.Capture> subCaptures = ((astify.Capture.ListCapture) captures.get(2)).all();
			property = (ASTifyGrammar.TypedName) subCaptures.get(1);
		}
		
		patternLists.add((ASTifyGrammar.PatternList) captures.get(3));
		
		for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) captures.get(4)).iterator(); it.hasNext(); ) {
			patternLists.add((ASTifyGrammar.PatternList) it.next());
		}
		
		return new ASTifyGrammar.AliasDefinition(captures.get(0).getPosition().to(captures.get(4).getPosition()), name, property, patternLists);
	}
	
	private astify.Capture __createExternDefinition(List<astify.Capture> captures) {
		astify.token.Token name = ((astify.Capture.TokenCapture) captures.get(2)).getToken();
		List<ASTifyGrammar.PatternList> patternLists = new ArrayList<>();
		List<ASTifyGrammar.TypedName> parameters = new ArrayList<>();
		ASTifyGrammar.Type returnType = (ASTifyGrammar.Type) captures.get(1);
		
		if (!(captures.get(4) instanceof astify.Capture.EmptyCapture)) {
			List<astify.Capture> subCaptures = ((astify.Capture.ListCapture) captures.get(4)).all();
			
			for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) subCaptures.get(0)).iterator(); it.hasNext(); ) {
				parameters.add((ASTifyGrammar.TypedName) it.next());
			}
			
			}
		
		for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) captures.get(6)).iterator(); it.hasNext(); ) {
			patternLists.add((ASTifyGrammar.PatternList) it.next());
		}
		
		return new ASTifyGrammar.ExternDefinition(captures.get(0).getPosition().to(captures.get(6).getPosition()), returnType, name, parameters, patternLists);
	}
	
	private astify.Capture __createApplyStatement(List<astify.Capture> captures) {
		ASTifyGrammar.Call call = (ASTifyGrammar.Call) captures.get(1);
		List<ASTifyGrammar.PatternList> patternLists = new ArrayList<>();
		
		patternLists.add((ASTifyGrammar.PatternList) captures.get(2));
		
		for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) captures.get(3)).iterator(); it.hasNext(); ) {
			patternLists.add((ASTifyGrammar.PatternList) it.next());
		}
		
		return new ASTifyGrammar.ApplyStatement(captures.get(0).getPosition().to(captures.get(3).getPosition()), call, patternLists);
	}
	
	private astify.Capture __createGrammar(List<astify.Capture> captures) {
		astify.token.Token name = ((astify.Capture.TokenCapture) captures.get(1)).getToken();
		
		return new ASTifyGrammar.Grammar(captures.get(0).getPosition().to(captures.get(1).getPosition()), name);
	}
	
	private astify.Capture __createASTifyGrammar(List<astify.Capture> captures) {
		List<ASTifyGrammar.Statement> statements = new ArrayList<>();
		ASTifyGrammar.Grammar _grammar = (ASTifyGrammar.Grammar) captures.get(0);
		
		for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) captures.get(1)).iterator(); it.hasNext(); ) {
			statements.add((ASTifyGrammar.Statement) it.next());
		}
		
		return new ASTifyGrammar(captures.get(0).getPosition().to(captures.get(2).getPosition()), _grammar, statements);
	}
}