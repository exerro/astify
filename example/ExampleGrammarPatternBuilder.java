package example;

import astify.Capture;
import astify.Pattern;
import astify.token.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ExampleGrammarPatternBuilder extends astify.PatternBuilder {
	
	public ExampleGrammarPatternBuilder() {
		sequence("named-type", this::createNamedType, 
			token(Word)
		);
		
		sequence("list-type", this::createListType, 
			operator("["), 
			ref("type"), 
			operator("]")
		);
		
		sequence("integer-value", this::createIntegerValue, 
			token(Integer)
		);
		
		sequence("string-value", this::createStringValue, 
			token(String)
		);
		
		sequence("identifier-value", this::createIdentifierValue, 
			token(Word)
		);
		
		sequence("declaration", this::createDeclaration, 
			ref("type"), 
			token(Word), 
			optional(sequence(
				operator("="), 
				optional(sequence(
					ref("value")
				))
			))
		);
		
		sequence("binary-expression", this::createBinaryExpression, 
			ref("primary-expression"), 
			ref("operator"), 
			ref("value")
		);
		
		sequence("operator-1", this::createOperator_1, 
			operator("+")
		);
		
		sequence("operator-2", this::createOperator_2, 
			operator("-")
		);
		
		sequence("operator-3", this::createOperator_3, 
			operator("*")
		);
		
		sequence("operator-4", this::createOperator_4, 
			operator("/")
		);
		
		sequence("operator-5", this::createOperator_5, 
			operator("==")
		);
		
		sequence("operator-6", this::createOperator_6, 
			operator("!=")
		);
		
		defineInline("operator", one_of(
			ref("operator-1"), 
			ref("operator-2"), 
			ref("operator-3"), 
			ref("operator-4"), 
			ref("operator-5"), 
			ref("operator-6")
		));
		
		sequence("expression-statement", this::createExpressionStatement, 
			keyword("eval"), 
			ref("value")
		);
		
		sequence("example-grammar", this::createExampleGrammar, 
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
	public Pattern getMain() {
		return lookup("example-grammar");
	}
	
	// (@Word -> typename)
	private Capture createNamedType(List<Capture> captures) {
		Token typename = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ExampleGrammar.NamedType(spanningPosition, typename);
	}
	
	// '[', (@Type -> subtype), ']'
	private Capture createListType(List<Capture> captures) {
		ExampleGrammar.Type subtype = (ExampleGrammar.Type) captures.get(1);
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(2).getPosition());
		
		return new ExampleGrammar.ListType(spanningPosition, subtype);
	}
	
	// (@Integer -> value)
	private Capture createIntegerValue(List<Capture> captures) {
		Token value = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ExampleGrammar.IntegerValue(spanningPosition, value);
	}
	
	// (@String -> value)
	private Capture createStringValue(List<Capture> captures) {
		Token value = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ExampleGrammar.StringValue(spanningPosition, value);
	}
	
	// (@Word -> identifier)
	private Capture createIdentifierValue(List<Capture> captures) {
		Token identifier = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ExampleGrammar.IdentifierValue(spanningPosition, identifier);
	}
	
	// (@Type -> type), (@Word -> variable), ['=', [(@Value -> value)]]
	private Capture createDeclaration(List<Capture> captures) {
		ExampleGrammar.Type type = (ExampleGrammar.Type) captures.get(0);
		Token variable = ((Capture.TokenCapture) captures.get(1)).getToken();
		ExampleGrammar.Value value = null;
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(2).getPosition());
		
		if (!(captures.get(2) instanceof Capture.EmptyCapture)) {
			List<Capture> subCaptures = ((Capture.ListCapture) captures.get(2)).all();
			
			if (!(subCaptures.get(1) instanceof Capture.EmptyCapture)) {
				List<Capture> subSubCaptures = ((Capture.ListCapture) subCaptures.get(1)).all();
				
				value = (ExampleGrammar.Value) subSubCaptures.get(0);
			}
		}
		
		return new ExampleGrammar.Declaration(spanningPosition, type, variable, value);
	}
	
	// (@PrimaryExpression -> lvalue), (@Operator -> operator), (@Value -> rvalue)
	private Capture createBinaryExpression(List<Capture> captures) {
		ExampleGrammar.PrimaryExpression lvalue = (ExampleGrammar.PrimaryExpression) captures.get(0);
		ExampleGrammar.Operator operator = (ExampleGrammar.Operator) captures.get(1);
		ExampleGrammar.Value rvalue = (ExampleGrammar.Value) captures.get(2);
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(2).getPosition());
		
		return new ExampleGrammar.BinaryExpression(spanningPosition, lvalue, operator, rvalue);
	}
	
	// ('+' -> symbol)
	private Capture createOperator_1(List<Capture> captures) {
		Token symbol = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ExampleGrammar.Operator(spanningPosition, symbol);
	}
	
	// ('-' -> symbol)
	private Capture createOperator_2(List<Capture> captures) {
		Token symbol = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ExampleGrammar.Operator(spanningPosition, symbol);
	}
	
	// ('*' -> symbol)
	private Capture createOperator_3(List<Capture> captures) {
		Token symbol = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ExampleGrammar.Operator(spanningPosition, symbol);
	}
	
	// ('/' -> symbol)
	private Capture createOperator_4(List<Capture> captures) {
		Token symbol = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ExampleGrammar.Operator(spanningPosition, symbol);
	}
	
	// ('==' -> symbol)
	private Capture createOperator_5(List<Capture> captures) {
		Token symbol = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ExampleGrammar.Operator(spanningPosition, symbol);
	}
	
	// ('!=' -> symbol)
	private Capture createOperator_6(List<Capture> captures) {
		Token symbol = ((Capture.TokenCapture) captures.get(0)).getToken();
		astify.core.Position spanningPosition = captures.get(0).getPosition();
		
		return new ExampleGrammar.Operator(spanningPosition, symbol);
	}
	
	// 'eval', (@Value -> value)
	private Capture createExpressionStatement(List<Capture> captures) {
		ExampleGrammar.Value value = (ExampleGrammar.Value) captures.get(1);
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(1).getPosition());
		
		return new ExampleGrammar.ExpressionStatement(spanningPosition, value);
	}
	
	// (list(@Statement) -> statements), @EOF
	private Capture createExampleGrammar(List<Capture> captures) {
		List<ExampleGrammar.Statement> statements = new ArrayList<>();
		astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(1).getPosition());
		
		for (Iterator<Capture> it = ((Capture.ListCapture) captures.get(0)).iterator(); it.hasNext(); ) {
			statements.add((ExampleGrammar.Statement) it.next());
		}
		
		return new ExampleGrammar(spanningPosition, statements);
	}
}