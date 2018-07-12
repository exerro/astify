package example;

import static java.util.Objects.hash;
import java.util.List;

class ExampleGrammar extends astify.Capture.ObjectCapture {
	private final List<Statement> statements;
	
	ExampleGrammar(astify.core.Position spanningPosition, List<Statement> statements) {
		super(spanningPosition);
		
		assert statements != null : "'statements' is null";
		
		this.statements = statements;
	}
	
	List<Statement> getStatements() {
		return statements;
	}
	
	@Override
	public String toString() {
		return "(ExampleGrammar\n"
			 + "	statements = " + statements.toString().replace("\n", "\n\t") + "\n"
			 + ")";
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ExampleGrammar)) return false;
		ExampleGrammar otherCasted = (ExampleGrammar) other;
		return statements.equals(otherCasted.statements);
	}
	
	@Override
	public int hashCode() {
		return hash(statements);
	}
	
	static class NamedType extends astify.Capture.ObjectCapture implements Type {
		private final astify.token.Token typename;
		
		NamedType(astify.core.Position spanningPosition, astify.token.Token typename) {
			super(spanningPosition);
			
			assert typename != null : "'typename' is null";
			
			this.typename = typename;
		}
		
		astify.token.Token getTypename() {
			return typename;
		}
		
		@Override
		public String toString() {
			return "(NamedType\n"
				 + "	typename = " + typename.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof NamedType)) return false;
			NamedType otherCasted = (NamedType) other;
			return typename.equals(otherCasted.typename);
		}
		
		@Override
		public int hashCode() {
			return hash(typename);
		}
	}
	
	static class ListType extends astify.Capture.ObjectCapture implements Type {
		private final Type subtype;
		
		ListType(astify.core.Position spanningPosition, Type subtype) {
			super(spanningPosition);
			
			assert subtype != null : "'subtype' is null";
			
			this.subtype = subtype;
		}
		
		Type getSubtype() {
			return subtype;
		}
		
		@Override
		public String toString() {
			return "(ListType\n"
				 + "	subtype = " + subtype.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ListType)) return false;
			ListType otherCasted = (ListType) other;
			return subtype.equals(otherCasted.subtype);
		}
		
		@Override
		public int hashCode() {
			return hash(subtype);
		}
	}
	
	static class IntegerValue extends astify.Capture.ObjectCapture implements LiteralValue {
		private final astify.token.Token value;
		
		IntegerValue(astify.core.Position spanningPosition, astify.token.Token value) {
			super(spanningPosition);
			
			assert value != null : "'value' is null";
			
			this.value = value;
		}
		
		astify.token.Token getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "(IntegerValue\n"
				 + "	value = " + value.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof IntegerValue)) return false;
			IntegerValue otherCasted = (IntegerValue) other;
			return value.equals(otherCasted.value);
		}
		
		@Override
		public int hashCode() {
			return hash(value);
		}
	}
	
	static class StringValue extends astify.Capture.ObjectCapture implements LiteralValue {
		private final astify.token.Token value;
		
		StringValue(astify.core.Position spanningPosition, astify.token.Token value) {
			super(spanningPosition);
			
			assert value != null : "'value' is null";
			
			this.value = value;
		}
		
		astify.token.Token getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "(StringValue\n"
				 + "	value = " + value.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof StringValue)) return false;
			StringValue otherCasted = (StringValue) other;
			return value.equals(otherCasted.value);
		}
		
		@Override
		public int hashCode() {
			return hash(value);
		}
	}
	
	static class IdentifierValue extends astify.Capture.ObjectCapture implements PrimaryExpression {
		private final astify.token.Token identifier;
		
		IdentifierValue(astify.core.Position spanningPosition, astify.token.Token identifier) {
			super(spanningPosition);
			
			assert identifier != null : "'identifier' is null";
			
			this.identifier = identifier;
		}
		
		astify.token.Token getIdentifier() {
			return identifier;
		}
		
		@Override
		public String toString() {
			return "(IdentifierValue\n"
				 + "	identifier = " + identifier.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof IdentifierValue)) return false;
			IdentifierValue otherCasted = (IdentifierValue) other;
			return identifier.equals(otherCasted.identifier);
		}
		
		@Override
		public int hashCode() {
			return hash(identifier);
		}
	}
	
	static class Declaration extends astify.Capture.ObjectCapture implements Statement {
		private final Type type;
		private final astify.token.Token variable;
		private final Value value;
		
		Declaration(astify.core.Position spanningPosition, Type type, astify.token.Token variable, Value value) {
			super(spanningPosition);
			
			assert type != null : "'type' is null";
			assert variable != null : "'variable' is null";
			
			this.type = type;
			this.variable = variable;
			this.value = value;
		}
		
		Type getType() {
			return type;
		}
		
		astify.token.Token getVariable() {
			return variable;
		}
		
		Value getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "(Declaration\n"
				 + "	type = " + type.toString().replace("\n", "\n\t") + "\n"
				 + "	variable = " + variable.toString().replace("\n", "\n\t") + "\n"
				 + "	value = " + (value == null ? "null" : value.toString().replace("\n", "\n\t")) + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Declaration)) return false;
			Declaration otherCasted = (Declaration) other;
			return type.equals(otherCasted.type)
				&& variable.equals(otherCasted.variable)
				&& (value == null ? otherCasted.value == null : value.equals(otherCasted.value));
		}
		
		@Override
		public int hashCode() {
			return hash(type, variable, value);
		}
	}
	
	static class BinaryExpression extends astify.Capture.ObjectCapture implements Value {
		private final PrimaryExpression lvalue;
		private final Operator operator;
		private final Value rvalue;
		
		BinaryExpression(astify.core.Position spanningPosition, PrimaryExpression lvalue, Operator operator, Value rvalue) {
			super(spanningPosition);
			
			assert lvalue != null : "'lvalue' is null";
			assert operator != null : "'operator' is null";
			assert rvalue != null : "'rvalue' is null";
			
			this.lvalue = lvalue;
			this.operator = operator;
			this.rvalue = rvalue;
		}
		
		PrimaryExpression getLvalue() {
			return lvalue;
		}
		
		Operator getOperator() {
			return operator;
		}
		
		Value getRvalue() {
			return rvalue;
		}
		
		@Override
		public String toString() {
			return "(BinaryExpression\n"
				 + "	lvalue = " + lvalue.toString().replace("\n", "\n\t") + "\n"
				 + "	operator = " + operator.toString().replace("\n", "\n\t") + "\n"
				 + "	rvalue = " + rvalue.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof BinaryExpression)) return false;
			BinaryExpression otherCasted = (BinaryExpression) other;
			return lvalue.equals(otherCasted.lvalue)
				&& operator.equals(otherCasted.operator)
				&& rvalue.equals(otherCasted.rvalue);
		}
		
		@Override
		public int hashCode() {
			return hash(lvalue, operator, rvalue);
		}
	}
	
	static class Operator extends astify.Capture.ObjectCapture {
		private final astify.token.Token symbol;
		
		Operator(astify.core.Position spanningPosition, astify.token.Token symbol) {
			super(spanningPosition);
			
			assert symbol != null : "'symbol' is null";
			
			this.symbol = symbol;
		}
		
		astify.token.Token getSymbol() {
			return symbol;
		}
		
		@Override
		public String toString() {
			return "(Operator\n"
				 + "	symbol = " + symbol.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Operator)) return false;
			Operator otherCasted = (Operator) other;
			return symbol.equals(otherCasted.symbol);
		}
		
		@Override
		public int hashCode() {
			return hash(symbol);
		}
	}
	
	static class ExpressionStatement extends astify.Capture.ObjectCapture implements Statement {
		private final Value value;
		
		ExpressionStatement(astify.core.Position spanningPosition, Value value) {
			super(spanningPosition);
			
			assert value != null : "'value' is null";
			
			this.value = value;
		}
		
		Value getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "(ExpressionStatement\n"
				 + "	value = " + value.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ExpressionStatement)) return false;
			ExpressionStatement otherCasted = (ExpressionStatement) other;
			return value.equals(otherCasted.value);
		}
		
		@Override
		public int hashCode() {
			return hash(value);
		}
	}
	
	interface Type extends astify.core.Positioned {
		}
	
	interface LiteralValue extends astify.core.Positioned, PrimaryExpression {
		}
	
	interface PrimaryExpression extends Value, astify.core.Positioned {
		}
	
	interface Value extends astify.core.Positioned {
		}
	
	interface Statement extends astify.core.Positioned {
		}
}