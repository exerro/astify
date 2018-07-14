package example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ExampleGrammarPatternBuilderBase extends astify.PatternBuilder {
	
	public ExampleGrammarPatternBuilderBase() {
		super();
		
		initExampleGrammar();
	}
	
	private void initExampleGrammar() {
		sequence("named-type", this::__createNamedType,
			token(Word)
		);
		
		sequence("list-type", this::__createListType,
			symbol("["),
			ref("type"),
			symbol("]")
		);
		
		sequence("integer-value", this::__createIntegerValue,
			token(Integer)
		);
		
		sequence("string-value", this::__createStringValue,
			token(String)
		);
		
		sequence("identifier-value", this::__createIdentifierValue,
			token(Word)
		);
		
		sequence("declaration", this::__createDeclaration,
			ref("type"),
			token(Word),
			optional(sequence(
				symbol("="),
				ref("value")
			))
		);
		
		sequence("binary-expression", this::__createBinaryExpression,
			ref("primary-expression"),
			ref("operator"),
			ref("value")
		);
		
		sequence("operator#1", this::__createOperator1,
			symbol("+")
		);
		
		sequence("operator#2", this::__createOperator2,
			symbol("-")
		);
		
		sequence("operator#3", this::__createOperator3,
			symbol("*")
		);
		
		sequence("operator#4", this::__createOperator4,
			symbol("/")
		);
		
		sequence("operator#5", this::__createOperator5,
			symbol("==")
		);
		
		sequence("operator#6", this::__createOperator6,
			symbol("!=")
		);
		
		defineInline("operator", one_of(
			ref("operator#1"),
			ref("operator#2"),
			ref("operator#3"),
			ref("operator#4"),
			ref("operator#5"),
			ref("operator#6")
		));
		
		sequence("expression-statement", this::__createExpressionStatement,
			keyword("eval"),
			ref("value")
		);
		
		sequence("example-grammar", this::__createExampleGrammar,
			list(ref("statement")),
			token(EOF)
		);
		
		define("type", one_of(
			ref("named-type"),
			ref("list-type")
		));
		
		define("literal-value", one_of(
			ref("integer-value"),
			ref("string-value")
		));
		
		define("primary-expression", one_of(
			ref("integer-value"),
			ref("string-value"),
			ref("identifier-value")
		));
		
		define("value", one_of(
			ref("integer-value"),
			ref("string-value"),
			ref("identifier-value"),
			ref("binary-expression")
		));
		
		define("statement", one_of(
			ref("expression-statement"),
			ref("declaration")
		)); 
	}
	
	@Override
	public astify.Pattern getMain() {
		return lookup("example-grammar");
	}
	
	private astify.Capture __createNamedType(List<astify.Capture> captures) {
		astify.token.Token typename = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ExampleGrammar.NamedType(captures.get(0).getPosition(), typename);
	}
	
	private astify.Capture __createListType(List<astify.Capture> captures) {
		ExampleGrammar.Type subtype = (ExampleGrammar.Type) captures.get(1);
		
		return new ExampleGrammar.ListType(captures.get(0).getPosition().to(captures.get(2).getPosition()), subtype);
	}
	
	private astify.Capture __createIntegerValue(List<astify.Capture> captures) {
		astify.token.Token value = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ExampleGrammar.IntegerValue(captures.get(0).getPosition(), value);
	}
	
	private astify.Capture __createStringValue(List<astify.Capture> captures) {
		astify.token.Token value = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ExampleGrammar.StringValue(captures.get(0).getPosition(), value);
	}
	
	private astify.Capture __createIdentifierValue(List<astify.Capture> captures) {
		astify.token.Token identifier = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ExampleGrammar.IdentifierValue(captures.get(0).getPosition(), identifier);
	}
	
	private astify.Capture __createDeclaration(List<astify.Capture> captures) {
		astify.token.Token variable = ((astify.Capture.TokenCapture) captures.get(1)).getToken();
		ExampleGrammar.Type type = (ExampleGrammar.Type) captures.get(0);
		ExampleGrammar.Value value = null;
		
		if (!(captures.get(2) instanceof astify.Capture.EmptyCapture)) {
			List<astify.Capture> subCaptures = ((astify.Capture.ListCapture) captures.get(2)).all();
			value = (ExampleGrammar.Value) subCaptures.get(1);
		}
		
		return new ExampleGrammar.Declaration(captures.get(0).getPosition().to(captures.get(2).getPosition()), type, variable, value);
	}
	
	private astify.Capture __createBinaryExpression(List<astify.Capture> captures) {
		ExampleGrammar.PrimaryExpression lvalue = (ExampleGrammar.PrimaryExpression) captures.get(0);
		ExampleGrammar.Value rvalue = (ExampleGrammar.Value) captures.get(2);
		ExampleGrammar.Operator operator = (ExampleGrammar.Operator) captures.get(1);
		
		return new ExampleGrammar.BinaryExpression(captures.get(0).getPosition().to(captures.get(2).getPosition()), lvalue, operator, rvalue);
	}
	
	private astify.Capture __createOperator1(List<astify.Capture> captures) {
		astify.token.Token symbol = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ExampleGrammar.Operator(captures.get(0).getPosition(), symbol);
	}
	
	private astify.Capture __createOperator2(List<astify.Capture> captures) {
		astify.token.Token symbol = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ExampleGrammar.Operator(captures.get(0).getPosition(), symbol);
	}
	
	private astify.Capture __createOperator3(List<astify.Capture> captures) {
		astify.token.Token symbol = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ExampleGrammar.Operator(captures.get(0).getPosition(), symbol);
	}
	
	private astify.Capture __createOperator4(List<astify.Capture> captures) {
		astify.token.Token symbol = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ExampleGrammar.Operator(captures.get(0).getPosition(), symbol);
	}
	
	private astify.Capture __createOperator5(List<astify.Capture> captures) {
		astify.token.Token symbol = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ExampleGrammar.Operator(captures.get(0).getPosition(), symbol);
	}
	
	private astify.Capture __createOperator6(List<astify.Capture> captures) {
		astify.token.Token symbol = ((astify.Capture.TokenCapture) captures.get(0)).getToken();
		
		return new ExampleGrammar.Operator(captures.get(0).getPosition(), symbol);
	}
	
	private astify.Capture __createExpressionStatement(List<astify.Capture> captures) {
		ExampleGrammar.Value value = (ExampleGrammar.Value) captures.get(1);
		
		return new ExampleGrammar.ExpressionStatement(captures.get(0).getPosition().to(captures.get(1).getPosition()), value);
	}
	
	private astify.Capture __createExampleGrammar(List<astify.Capture> captures) {
		List<ExampleGrammar.Statement> statements = new ArrayList<>();
		
		for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) captures.get(0)).iterator(); it.hasNext(); ) {
			statements.add((ExampleGrammar.Statement) it.next());
		}
		
		return new ExampleGrammar(captures.get(0).getPosition().to(captures.get(1).getPosition()), statements);
	}
}