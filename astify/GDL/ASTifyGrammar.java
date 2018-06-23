package astify.GDL;

import astify.Capture;
import astify.core.Position;
import astify.token.Token;

import java.util.List;

class ASTifyGrammar extends Capture.ObjectCapture {
	private final Grammar _grammar;
	private final List<Definition> definitions;
	
	protected ASTifyGrammar(Position spanningPosition, Grammar _grammar, List<Definition> definitions) {
		super(spanningPosition);
		assert _grammar != null;
		assert definitions != null;
		this._grammar = _grammar;
		this.definitions = definitions;
	}
	
	public Grammar getGrammar() {
		return _grammar;
	}
	
	public List<Definition> getDefinitions() {
		return definitions;
	}
	
	@Override
	public String toString() {
		return "(ASTifyGrammar"
		     + "\n\t_grammar = " + _grammar.toString().replace("\n", "\n\t")
		     + "\n\tdefinitions = " + definitions.toString().replace("\n", "\n\t")
		     + "\n)";
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ASTifyGrammar)) return false;
		ASTifyGrammar otherCasted = (ASTifyGrammar) other;
		return _grammar.equals(otherCasted._grammar) && definitions.equals(otherCasted.definitions);
	}
	
	@Override
	public int hashCode() {
		return _grammar.hashCode() + 31 * definitions.hashCode();
	}
	
	static class Type extends Capture.ObjectCapture {
		private final Token name;
		private final Boolean optional;
		private final Boolean lst;
		
		protected Type(Position spanningPosition, Token name, Boolean optional, Boolean lst) {
			super(spanningPosition);
			assert name != null;
			assert optional != null;
			assert lst != null;
			this.name = name;
			this.optional = optional;
			this.lst = lst;
		}
		
		public Token getName() {
			return name;
		}
		
		public Boolean isOptional() {
			return optional;
		}
		
		public Boolean isLst() {
			return lst;
		}
		
		@Override
		public String toString() {
			return "(Type"
			     + "\n\tname = " + name.toString().replace("\n", "\n\t")
			     + "\n\toptional = " + optional.toString().replace("\n", "\n\t")
			     + "\n\tlst = " + lst.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Type)) return false;
			Type otherCasted = (Type) other;
			return name.equals(otherCasted.name) && optional.equals(otherCasted.optional) && lst.equals(otherCasted.lst);
		}
		
		@Override
		public int hashCode() {
			return name.hashCode() + 31 * optional.hashCode() + 961 * lst.hashCode();
		}
	}
	
	static class TypedName extends Capture.ObjectCapture {
		private final Type type;
		private final Token name;
		
		protected TypedName(Position spanningPosition, Type type, Token name) {
			super(spanningPosition);
			assert type != null;
			assert name != null;
			this.type = type;
			this.name = name;
		}
		
		public Type getType() {
			return type;
		}
		
		public Token getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return "(TypedName"
			     + "\n\ttype = " + type.toString().replace("\n", "\n\t")
			     + "\n\tname = " + name.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof TypedName)) return false;
			TypedName otherCasted = (TypedName) other;
			return type.equals(otherCasted.type) && name.equals(otherCasted.name);
		}
		
		@Override
		public int hashCode() {
			return type.hashCode() + 31 * name.hashCode();
		}
	}
	
	static class MatcherTarget extends Capture.ObjectCapture {
		private final Token property;
		
		protected MatcherTarget(Position spanningPosition, Token property) {
			super(spanningPosition);
			assert property != null;
			this.property = property;
		}
		
		public Token getProperty() {
			return property;
		}
		
		@Override
		public String toString() {
			return "(MatcherTarget"
			     + "\n\tproperty = " + property.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof MatcherTarget)) return false;
			MatcherTarget otherCasted = (MatcherTarget) other;
			return property.equals(otherCasted.property);
		}
		
		@Override
		public int hashCode() {
			return property.hashCode();
		}
	}
	
	static class PatternList extends Capture.ObjectCapture {
		private final List<RootPattern> patterns;
		
		protected PatternList(Position spanningPosition, List<RootPattern> patterns) {
			super(spanningPosition);
			assert patterns != null;
			this.patterns = patterns;
		}
		
		public List<RootPattern> getPatterns() {
			return patterns;
		}
		
		@Override
		public String toString() {
			return "(PatternList"
			     + "\n\tpatterns = " + patterns.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof PatternList)) return false;
			PatternList otherCasted = (PatternList) other;
			return patterns.equals(otherCasted.patterns);
		}
		
		@Override
		public int hashCode() {
			return patterns.hashCode();
		}
	}
	
	static class NamedPropertyList extends Capture.ObjectCapture {
		private final Token name;
		private final List<TypedName> properties;
		
		protected NamedPropertyList(Position spanningPosition, Token name, List<TypedName> properties) {
			super(spanningPosition);
			assert name != null;
			assert properties != null;
			this.name = name;
			this.properties = properties;
		}
		
		public Token getName() {
			return name;
		}
		
		public List<TypedName> getProperties() {
			return properties;
		}
		
		@Override
		public String toString() {
			return "(NamedPropertyList"
			     + "\n\tname = " + name.toString().replace("\n", "\n\t")
			     + "\n\tproperties = " + properties.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof NamedPropertyList)) return false;
			NamedPropertyList otherCasted = (NamedPropertyList) other;
			return name.equals(otherCasted.name) && properties.equals(otherCasted.properties);
		}
		
		@Override
		public int hashCode() {
			return name.hashCode() + 31 * properties.hashCode();
		}
	}
	
	static class TypeReference extends Capture.ObjectCapture implements UncapturingPattern {
		private final Token type;
		
		protected TypeReference(Position spanningPosition, Token type) {
			super(spanningPosition);
			assert type != null;
			this.type = type;
		}
		
		public Token getType() {
			return type;
		}
		
		@Override
		public String toString() {
			return "(TypeReference"
			     + "\n\ttype = " + type.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof TypeReference)) return false;
			TypeReference otherCasted = (TypeReference) other;
			return type.equals(otherCasted.type);
		}
		
		@Override
		public int hashCode() {
			return type.hashCode();
		}
	}
	
	static class Terminal extends Capture.ObjectCapture implements UncapturingPattern {
		private final Token terminal;
		
		protected Terminal(Position spanningPosition, Token terminal) {
			super(spanningPosition);
			assert terminal != null;
			this.terminal = terminal;
		}
		
		public Token getTerminal() {
			return terminal;
		}
		
		@Override
		public String toString() {
			return "(Terminal"
			     + "\n\tterminal = " + terminal.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Terminal)) return false;
			Terminal otherCasted = (Terminal) other;
			return terminal.equals(otherCasted.terminal);
		}
		
		@Override
		public int hashCode() {
			return terminal.hashCode();
		}
	}
	
	static class Function extends Capture.ObjectCapture implements UncapturingPattern {
		private final Token name;
		private final List<UncapturingPattern> patterns;
		
		protected Function(Position spanningPosition, Token name, List<UncapturingPattern> patterns) {
			super(spanningPosition);
			assert name != null;
			assert patterns != null;
			this.name = name;
			this.patterns = patterns;
		}
		
		public Token getName() {
			return name;
		}
		
		public List<UncapturingPattern> getPatterns() {
			return patterns;
		}
		
		@Override
		public String toString() {
			return "(Function"
			     + "\n\tname = " + name.toString().replace("\n", "\n\t")
			     + "\n\tpatterns = " + patterns.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Function)) return false;
			Function otherCasted = (Function) other;
			return name.equals(otherCasted.name) && patterns.equals(otherCasted.patterns);
		}
		
		@Override
		public int hashCode() {
			return name.hashCode() + 31 * patterns.hashCode();
		}
	}
	
	static class Matcher extends Capture.ObjectCapture implements Pattern {
		private final UncapturingPattern source;
		private final MatcherTarget targetProperty;
		
		protected Matcher(Position spanningPosition, UncapturingPattern source, MatcherTarget targetProperty) {
			super(spanningPosition);
			assert source != null;
			assert targetProperty != null;
			this.source = source;
			this.targetProperty = targetProperty;
		}
		
		public UncapturingPattern getSource() {
			return source;
		}
		
		public MatcherTarget getTargetProperty() {
			return targetProperty;
		}
		
		@Override
		public String toString() {
			return "(Matcher"
			     + "\n\tsource = " + source.toString().replace("\n", "\n\t")
			     + "\n\ttargetProperty = " + targetProperty.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Matcher)) return false;
			Matcher otherCasted = (Matcher) other;
			return source.equals(otherCasted.source) && targetProperty.equals(otherCasted.targetProperty);
		}
		
		@Override
		public int hashCode() {
			return source.hashCode() + 31 * targetProperty.hashCode();
		}
	}
	
	static class PropertyReference extends Capture.ObjectCapture implements Pattern {
		private final Token property;
		private final List<UncapturingPattern> qualifier;
		
		protected PropertyReference(Position spanningPosition, Token property, List<UncapturingPattern> qualifier) {
			super(spanningPosition);
			assert property != null;
			assert qualifier != null;
			this.property = property;
			this.qualifier = qualifier;
		}
		
		public Token getProperty() {
			return property;
		}
		
		public List<UncapturingPattern> getQualifier() {
			return qualifier;
		}
		
		@Override
		public String toString() {
			return "(PropertyReference"
			     + "\n\tproperty = " + property.toString().replace("\n", "\n\t")
			     + "\n\tqualifier = " + qualifier.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof PropertyReference)) return false;
			PropertyReference otherCasted = (PropertyReference) other;
			return property.equals(otherCasted.property) && qualifier.equals(otherCasted.qualifier);
		}
		
		@Override
		public int hashCode() {
			return property.hashCode() + 31 * qualifier.hashCode();
		}
	}
	
	static class Optional extends Capture.ObjectCapture implements RootPattern {
		private final List<Pattern> patterns;
		
		protected Optional(Position spanningPosition, List<Pattern> patterns) {
			super(spanningPosition);
			assert patterns != null;
			this.patterns = patterns;
		}
		
		public List<Pattern> getPatterns() {
			return patterns;
		}
		
		@Override
		public String toString() {
			return "(Optional"
			     + "\n\tpatterns = " + patterns.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Optional)) return false;
			Optional otherCasted = (Optional) other;
			return patterns.equals(otherCasted.patterns);
		}
		
		@Override
		public int hashCode() {
			return patterns.hashCode();
		}
	}
	
	interface UncapturingPattern extends astify.core.Positioned, Pattern {
		
	}
	
	interface Pattern extends astify.core.Positioned, RootPattern {
		
	}
	
	interface RootPattern extends astify.core.Positioned {
		
	}
	
	static class AbstractTypeDefinition extends Capture.ObjectCapture implements Definition {
		private final NamedPropertyList properties;
		
		protected AbstractTypeDefinition(Position spanningPosition, NamedPropertyList properties) {
			super(spanningPosition);
			assert properties != null;
			this.properties = properties;
		}
		
		public NamedPropertyList getProperties() {
			return properties;
		}
		
		@Override
		public String toString() {
			return "(AbstractTypeDefinition"
			     + "\n\tproperties = " + properties.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof AbstractTypeDefinition)) return false;
			AbstractTypeDefinition otherCasted = (AbstractTypeDefinition) other;
			return properties.equals(otherCasted.properties);
		}
		
		@Override
		public int hashCode() {
			return properties.hashCode();
		}
	}
	
	static class TypeDefinition extends Capture.ObjectCapture implements Definition {
		private final NamedPropertyList properties;
		private final List<PatternList> patterns;
		
		protected TypeDefinition(Position spanningPosition, NamedPropertyList properties, List<PatternList> patterns) {
			super(spanningPosition);
			assert properties != null;
			assert patterns != null;
			this.properties = properties;
			this.patterns = patterns;
		}
		
		public NamedPropertyList getProperties() {
			return properties;
		}
		
		public List<PatternList> getPatterns() {
			return patterns;
		}
		
		@Override
		public String toString() {
			return "(TypeDefinition"
			     + "\n\tproperties = " + properties.toString().replace("\n", "\n\t")
			     + "\n\tpatterns = " + patterns.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof TypeDefinition)) return false;
			TypeDefinition otherCasted = (TypeDefinition) other;
			return properties.equals(otherCasted.properties) && patterns.equals(otherCasted.patterns);
		}
		
		@Override
		public int hashCode() {
			return properties.hashCode() + 31 * patterns.hashCode();
		}
	}
	
	static class Union extends Capture.ObjectCapture implements Definition {
		private final Token typename;
		private final List<Token> subtypes;
		
		protected Union(Position spanningPosition, Token typename, List<Token> subtypes) {
			super(spanningPosition);
			assert typename != null;
			assert subtypes != null;
			this.typename = typename;
			this.subtypes = subtypes;
		}
		
		public Token getTypename() {
			return typename;
		}
		
		public List<Token> getSubtypes() {
			return subtypes;
		}
		
		@Override
		public String toString() {
			return "(Union"
			     + "\n\ttypename = " + typename.toString().replace("\n", "\n\t")
			     + "\n\tsubtypes = " + subtypes.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Union)) return false;
			Union otherCasted = (Union) other;
			return typename.equals(otherCasted.typename) && subtypes.equals(otherCasted.subtypes);
		}
		
		@Override
		public int hashCode() {
			return typename.hashCode() + 31 * subtypes.hashCode();
		}
	}
	
	interface Definition extends astify.core.Positioned {
		
	}
	
	static class Grammar extends Capture.ObjectCapture {
		private final Token name;
		
		protected Grammar(Position spanningPosition, Token name) {
			super(spanningPosition);
			assert name != null;
			this.name = name;
		}
		
		public Token getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return "(Grammar"
			     + "\n\tname = " + name.toString().replace("\n", "\n\t")
			     + "\n)";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Grammar)) return false;
			Grammar otherCasted = (Grammar) other;
			return name.equals(otherCasted.name);
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}
}