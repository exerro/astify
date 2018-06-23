package example;

import astify.Capture;
import astify.core.Position;
import astify.token.Token;

import java.util.List;

class ExampleGrammar extends Capture.ObjectCapture {
	private final List<Statement> statements;
	
	protected ExampleGrammar(Position spanningPosition, List<Statement> statements) {
		super(spanningPosition);
		assert statements != null;
		this.statements = statements;
	}
	
	public List<Statement> getStatements() {
		return statements;
	}
	
	@Override
	public String toString() {
		return "(ExampleGrammar"
		     + "\n\tstatements = " + statements.toString().replace("\n", "\n\t")
		     + "\n)";
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ExampleGrammar)) return false;
		ExampleGrammar otherCasted = (ExampleGrammar) other;
		return statements.equals(otherCasted.statements);
	}
	
	@Override
	public int hashCode() {
		return statements.hashCode();
	}
	
	static class NamedType extends Capture.ObjectCapture implements Type {
		private final Token typename;
		
		protected NamedType(Position spanningPosition, Token typename) {
			super(spanningPosition);
			assert typename != null;
			this.typename = typename;
		}
		
		public Token getTypename() {
			return typename;
		}
		
		@Override
		public String toString() {
			return "(NamedType"
			     + "\n\ttypename = " + typename.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof NamedType)) return false;
			NamedType otherCasted = (NamedType) other;
			return typename.equals(otherCasted.typename);
		}
		
		@Override
		public int hashCode() {
			return typename.hashCode();
		}
	}
	
	static class ListType extends Capture.ObjectCapture implements Type {
		private final Type subtype;
		
		protected ListType(Position spanningPosition, Type subtype) {
			super(spanningPosition);
			assert subtype != null;
			this.subtype = subtype;
		}
		
		public Type getSubtype() {
			return subtype;
		}
		
		@Override
		public String toString() {
			return "(ListType"
			     + "\n\tsubtype = " + subtype.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ListType)) return false;
			ListType otherCasted = (ListType) other;
			return subtype.equals(otherCasted.subtype);
		}
		
		@Override
		public int hashCode() {
			return subtype.hashCode();
		}
	}
	
	static class IntegerValue extends Capture.ObjectCapture implements LiteralValue {
		private final Token value;
		
		protected IntegerValue(Position spanningPosition, Token value) {
			super(spanningPosition);
			assert value != null;
			this.value = value;
		}
		
		public Token getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "(IntegerValue"
			     + "\n\tvalue = " + value.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof IntegerValue)) return false;
			IntegerValue otherCasted = (IntegerValue) other;
			return value.equals(otherCasted.value);
		}
		
		@Override
		public int hashCode() {
			return value.hashCode();
		}
	}
	
	static class StringValue extends Capture.ObjectCapture implements LiteralValue {
		private final Token value;
		
		protected StringValue(Position spanningPosition, Token value) {
			super(spanningPosition);
			assert value != null;
			this.value = value;
		}
		
		public Token getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "(StringValue"
			     + "\n\tvalue = " + value.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof StringValue)) return false;
			StringValue otherCasted = (StringValue) other;
			return value.equals(otherCasted.value);
		}
		
		@Override
		public int hashCode() {
			return value.hashCode();
		}
	}
	
	static class IdentifierValue extends Capture.ObjectCapture implements PrimaryExpression {
		private final Token identifier;
		
		protected IdentifierValue(Position spanningPosition, Token identifier) {
			super(spanningPosition);
			assert identifier != null;
			this.identifier = identifier;
		}
		
		public Token getIdentifier() {
			return identifier;
		}
		
		@Override
		public String toString() {
			return "(IdentifierValue"
			     + "\n\tidentifier = " + identifier.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof IdentifierValue)) return false;
			IdentifierValue otherCasted = (IdentifierValue) other;
			return identifier.equals(otherCasted.identifier);
		}
		
		@Override
		public int hashCode() {
			return identifier.hashCode();
		}
	}
	
	static class Declaration extends Capture.ObjectCapture implements Statement {
		private final Type type;
		private final Token variable;
		private final Value value;
		
		protected Declaration(Position spanningPosition, Type type, Token variable, Value value) {
			super(spanningPosition);
			assert type != null;
			assert variable != null;
			this.type = type;
			this.variable = variable;
			this.value = value;
		}
		
		public Type getType() {
			return type;
		}
		
		public Token getVariable() {
			return variable;
		}
		
		public Value getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "(Declaration"
			     + "\n\ttype = " + type.toString().replace("\n", "\n\t")
			     + "\n\tvariable = " + variable.toString().replace("\n", "\n\t")
			     + "\n\tvalue = " + value.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Declaration)) return false;
			Declaration otherCasted = (Declaration) other;
			return type.equals(otherCasted.type) && variable.equals(otherCasted.variable) && value.equals(otherCasted.value);
		}
		
		@Override
		public int hashCode() {
			return type.hashCode() + 31 * variable.hashCode() + 961 * value.hashCode();
		}
	}
	
	static class BinaryExpression extends Capture.ObjectCapture implements Value {
		private final PrimaryExpression lvalue;
		private final Operator operator;
		private final Value rvalue;
		
		protected BinaryExpression(Position spanningPosition, PrimaryExpression lvalue, Operator operator, Value rvalue) {
			super(spanningPosition);
			assert lvalue != null;
			assert operator != null;
			assert rvalue != null;
			this.lvalue = lvalue;
			this.operator = operator;
			this.rvalue = rvalue;
		}
		
		public PrimaryExpression getLvalue() {
			return lvalue;
		}
		
		public Operator getOperator() {
			return operator;
		}
		
		public Value getRvalue() {
			return rvalue;
		}
		
		@Override
		public String toString() {
			return "(BinaryExpression"
			     + "\n\tlvalue = " + lvalue.toString().replace("\n", "\n\t")
			     + "\n\toperator = " + operator.toString().replace("\n", "\n\t")
			     + "\n\trvalue = " + rvalue.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof BinaryExpression)) return false;
			BinaryExpression otherCasted = (BinaryExpression) other;
			return lvalue.equals(otherCasted.lvalue) && operator.equals(otherCasted.operator) && rvalue.equals(otherCasted.rvalue);
		}
		
		@Override
		public int hashCode() {
			return lvalue.hashCode() + 31 * operator.hashCode() + 961 * rvalue.hashCode();
		}
	}
	
	static class Operator extends Capture.ObjectCapture {
		private final Token symbol;
		
		protected Operator(Position spanningPosition, Token symbol) {
			super(spanningPosition);
			assert symbol != null;
			this.symbol = symbol;
		}
		
		public Token getSymbol() {
			return symbol;
		}
		
		@Override
		public String toString() {
			return "(Operator"
			     + "\n\tsymbol = " + symbol.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Operator)) return false;
			Operator otherCasted = (Operator) other;
			return symbol.equals(otherCasted.symbol);
		}
		
		@Override
		public int hashCode() {
			return symbol.hashCode();
		}
	}
	
	static class ExpressionStatement extends Capture.ObjectCapture implements Statement {
		private final Value value;
		
		protected ExpressionStatement(Position spanningPosition, Value value) {
			super(spanningPosition);
			assert value != null;
			this.value = value;
		}
		
		public Value getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "(ExpressionStatement"
			     + "\n\tvalue = " + value.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ExpressionStatement)) return false;
			ExpressionStatement otherCasted = (ExpressionStatement) other;
			return value.equals(otherCasted.value);
		}
		
		@Override
		public int hashCode() {
			return value.hashCode();
		}
	}
	
	interface Type extends astify.core.Positioned {
		
	}
	
	interface LiteralValue extends astify.core.Positioned, PrimaryExpression {
		
	}
	
	interface PrimaryExpression extends astify.core.Positioned, Value {
		
	}
	
	interface Value extends astify.core.Positioned {
		
	}
	
	interface Statement extends astify.core.Positioned {
		
	}
}