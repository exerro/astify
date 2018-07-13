package GDL;

import static java.util.Objects.hash;
import java.util.List;

class ASTifyGrammar extends astify.Capture.ObjectCapture {
	private final Grammar _grammar;
	private final List<Statement> statements;
	
	ASTifyGrammar(astify.core.Position spanningPosition, Grammar _grammar, List<Statement> statements) {
		super(spanningPosition);
		
		assert _grammar != null : "'_grammar' is null";
		assert statements != null : "'statements' is null";
		
		this._grammar = _grammar;
		this.statements = statements;
	}
	
	Grammar getGrammar() {
		return _grammar;
	}
	
	List<Statement> getStatements() {
		return statements;
	}
	
	@Override
	public String toString() {
		return "(GDL.ASTifyGrammar\n"
			 + "	_grammar = " + _grammar.toString().replace("\n", "\n\t") + "\n"
			 + "	statements = " + statements.toString().replace("\n", "\n\t") + "\n"
			 + ")";
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ASTifyGrammar)) return false;
		ASTifyGrammar otherCasted = (ASTifyGrammar) other;
		return _grammar.equals(otherCasted._grammar)
			&& statements.equals(otherCasted.statements);
	}
	
	@Override
	public int hashCode() {
		return hash(_grammar, statements);
	}
	
	static class Type extends astify.Capture.ObjectCapture {
		private final astify.token.Token name;
		private final Boolean optional;
		private final Boolean lst;
		
		Type(astify.core.Position spanningPosition, astify.token.Token name, Boolean optional, Boolean lst) {
			super(spanningPosition);
			
			assert name != null : "'name' is null";
			assert optional != null : "'optional' is null";
			assert lst != null : "'lst' is null";
			
			this.name = name;
			this.optional = optional;
			this.lst = lst;
		}
		
		astify.token.Token getName() {
			return name;
		}
		
		Boolean isOptional() {
			return optional;
		}
		
		Boolean isLst() {
			return lst;
		}
		
		@Override
		public String toString() {
			return "(GDL.Type\n"
				 + "	name = " + name.toString().replace("\n", "\n\t") + "\n"
				 + "	optional = " + optional.toString().replace("\n", "\n\t") + "\n"
				 + "	lst = " + lst.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Type)) return false;
			Type otherCasted = (Type) other;
			return name.equals(otherCasted.name)
				&& optional.equals(otherCasted.optional)
				&& lst.equals(otherCasted.lst);
		}
		
		@Override
		public int hashCode() {
			return hash(name, optional, lst);
		}
	}
	
	static class TypedName extends astify.Capture.ObjectCapture {
		private final Type type;
		private final astify.token.Token name;
		
		TypedName(astify.core.Position spanningPosition, Type type, astify.token.Token name) {
			super(spanningPosition);
			
			assert type != null : "'type' is null";
			assert name != null : "'name' is null";
			
			this.type = type;
			this.name = name;
		}
		
		Type getType() {
			return type;
		}
		
		astify.token.Token getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return "(TypedName\n"
				 + "	type = " + type.toString().replace("\n", "\n\t") + "\n"
				 + "	name = " + name.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof TypedName)) return false;
			TypedName otherCasted = (TypedName) other;
			return type.equals(otherCasted.type)
				&& name.equals(otherCasted.name);
		}
		
		@Override
		public int hashCode() {
			return hash(type, name);
		}
	}
	
	static class MatcherTarget extends astify.Capture.ObjectCapture {
		private final astify.token.Token property;
		
		MatcherTarget(astify.core.Position spanningPosition, astify.token.Token property) {
			super(spanningPosition);
			
			assert property != null : "'property' is null";
			
			this.property = property;
		}
		
		astify.token.Token getProperty() {
			return property;
		}
		
		@Override
		public String toString() {
			return "(MatcherTarget\n"
				 + "	property = " + property.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof MatcherTarget)) return false;
			MatcherTarget otherCasted = (MatcherTarget) other;
			return property.equals(otherCasted.property);
		}
		
		@Override
		public int hashCode() {
			return hash(property);
		}
	}
	
	static class PatternList extends astify.Capture.ObjectCapture {
		private final List<RootPattern> patterns;
		
		PatternList(astify.core.Position spanningPosition, List<RootPattern> patterns) {
			super(spanningPosition);
			
			assert patterns != null : "'patterns' is null";
			
			this.patterns = patterns;
		}
		
		List<RootPattern> getPatterns() {
			return patterns;
		}
		
		@Override
		public String toString() {
			return "(PatternList\n"
				 + "	patterns = " + patterns.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof PatternList)) return false;
			PatternList otherCasted = (PatternList) other;
			return patterns.equals(otherCasted.patterns);
		}
		
		@Override
		public int hashCode() {
			return hash(patterns);
		}
	}
	
	static class NamedPropertyList extends astify.Capture.ObjectCapture {
		private final astify.token.Token name;
		private final List<TypedName> properties;
		
		NamedPropertyList(astify.core.Position spanningPosition, astify.token.Token name, List<TypedName> properties) {
			super(spanningPosition);
			
			assert name != null : "'name' is null";
			assert properties != null : "'properties' is null";
			
			this.name = name;
			this.properties = properties;
		}
		
		astify.token.Token getName() {
			return name;
		}
		
		List<TypedName> getProperties() {
			return properties;
		}
		
		@Override
		public String toString() {
			return "(NamedPropertyList\n"
				 + "	name = " + name.toString().replace("\n", "\n\t") + "\n"
				 + "	properties = " + properties.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof NamedPropertyList)) return false;
			NamedPropertyList otherCasted = (NamedPropertyList) other;
			return name.equals(otherCasted.name)
				&& properties.equals(otherCasted.properties);
		}
		
		@Override
		public int hashCode() {
			return hash(name, properties);
		}
	}
	
	static class Call extends astify.Capture.ObjectCapture implements Parameter {
		private final astify.token.Token functionName;
		private final List<Parameter> parameters;
		
		Call(astify.core.Position spanningPosition, astify.token.Token functionName, List<Parameter> parameters) {
			super(spanningPosition);
			
			assert functionName != null : "'functionName' is null";
			assert parameters != null : "'parameters' is null";
			
			this.functionName = functionName;
			this.parameters = parameters;
		}
		
		astify.token.Token getFunctionName() {
			return functionName;
		}
		
		List<Parameter> getParameters() {
			return parameters;
		}
		
		@Override
		public String toString() {
			return "(Call\n"
				 + "	functionName = " + functionName.toString().replace("\n", "\n\t") + "\n"
				 + "	parameters = " + parameters.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Call)) return false;
			Call otherCasted = (Call) other;
			return functionName.equals(otherCasted.functionName)
				&& parameters.equals(otherCasted.parameters);
		}
		
		@Override
		public int hashCode() {
			return hash(functionName, parameters);
		}
	}
	
	static class Reference extends astify.Capture.ObjectCapture implements Parameter {
		private final astify.token.Token reference;
		
		Reference(astify.core.Position spanningPosition, astify.token.Token reference) {
			super(spanningPosition);
			
			assert reference != null : "'reference' is null";
			
			this.reference = reference;
		}
		
		astify.token.Token getReference() {
			return reference;
		}
		
		@Override
		public String toString() {
			return "(Reference\n"
				 + "	reference = " + reference.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Reference)) return false;
			Reference otherCasted = (Reference) other;
			return reference.equals(otherCasted.reference);
		}
		
		@Override
		public int hashCode() {
			return hash(reference);
		}
	}
	
	interface Parameter extends astify.core.Positioned {
		}
	
	static class TypeReference extends astify.Capture.ObjectCapture implements UncapturingPattern {
		private final astify.token.Token type;
		
		TypeReference(astify.core.Position spanningPosition, astify.token.Token type) {
			super(spanningPosition);
			
			assert type != null : "'type' is null";
			
			this.type = type;
		}
		
		astify.token.Token getType() {
			return type;
		}
		
		@Override
		public String toString() {
			return "(TypeReference\n"
				 + "	type = " + type.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof TypeReference)) return false;
			TypeReference otherCasted = (TypeReference) other;
			return type.equals(otherCasted.type);
		}
		
		@Override
		public int hashCode() {
			return hash(type);
		}
	}
	
	static class Terminal extends astify.Capture.ObjectCapture implements UncapturingPattern {
		private final astify.token.Token terminal;
		
		Terminal(astify.core.Position spanningPosition, astify.token.Token terminal) {
			super(spanningPosition);
			
			assert terminal != null : "'terminal' is null";
			
			this.terminal = terminal;
		}
		
		astify.token.Token getTerminal() {
			return terminal;
		}
		
		@Override
		public String toString() {
			return "(Terminal\n"
				 + "	terminal = " + terminal.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Terminal)) return false;
			Terminal otherCasted = (Terminal) other;
			return terminal.equals(otherCasted.terminal);
		}
		
		@Override
		public int hashCode() {
			return hash(terminal);
		}
	}
	
	static class Function extends astify.Capture.ObjectCapture implements UncapturingPattern {
		private final astify.token.Token name;
		private final List<UncapturingPattern> patterns;
		
		Function(astify.core.Position spanningPosition, astify.token.Token name, List<UncapturingPattern> patterns) {
			super(spanningPosition);
			
			assert name != null : "'name' is null";
			assert patterns != null : "'patterns' is null";
			
			this.name = name;
			this.patterns = patterns;
		}
		
		astify.token.Token getName() {
			return name;
		}
		
		List<UncapturingPattern> getPatterns() {
			return patterns;
		}
		
		@Override
		public String toString() {
			return "(Function\n"
				 + "	name = " + name.toString().replace("\n", "\n\t") + "\n"
				 + "	patterns = " + patterns.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Function)) return false;
			Function otherCasted = (Function) other;
			return name.equals(otherCasted.name)
				&& patterns.equals(otherCasted.patterns);
		}
		
		@Override
		public int hashCode() {
			return hash(name, patterns);
		}
	}
	
	static class Matcher extends astify.Capture.ObjectCapture implements Pattern {
		private final UncapturingPattern source;
		private final MatcherTarget targetProperty;
		
		Matcher(astify.core.Position spanningPosition, UncapturingPattern source, MatcherTarget targetProperty) {
			super(spanningPosition);
			
			assert source != null : "'source' is null";
			assert targetProperty != null : "'targetProperty' is null";
			
			this.source = source;
			this.targetProperty = targetProperty;
		}
		
		UncapturingPattern getSource() {
			return source;
		}
		
		MatcherTarget getTargetProperty() {
			return targetProperty;
		}
		
		@Override
		public String toString() {
			return "(Matcher\n"
				 + "	source = " + source.toString().replace("\n", "\n\t") + "\n"
				 + "	targetProperty = " + targetProperty.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Matcher)) return false;
			Matcher otherCasted = (Matcher) other;
			return source.equals(otherCasted.source)
				&& targetProperty.equals(otherCasted.targetProperty);
		}
		
		@Override
		public int hashCode() {
			return hash(source, targetProperty);
		}
	}
	
	static class PropertyReference extends astify.Capture.ObjectCapture implements Pattern {
		private final astify.token.Token property;
		private final List<UncapturingPattern> qualifier;
		
		PropertyReference(astify.core.Position spanningPosition, astify.token.Token property, List<UncapturingPattern> qualifier) {
			super(spanningPosition);
			
			assert property != null : "'property' is null";
			assert qualifier != null : "'qualifier' is null";
			
			this.property = property;
			this.qualifier = qualifier;
		}
		
		astify.token.Token getProperty() {
			return property;
		}
		
		List<UncapturingPattern> getQualifier() {
			return qualifier;
		}
		
		@Override
		public String toString() {
			return "(PropertyReference\n"
				 + "	property = " + property.toString().replace("\n", "\n\t") + "\n"
				 + "	qualifier = " + qualifier.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof PropertyReference)) return false;
			PropertyReference otherCasted = (PropertyReference) other;
			return property.equals(otherCasted.property)
				&& qualifier.equals(otherCasted.qualifier);
		}
		
		@Override
		public int hashCode() {
			return hash(property, qualifier);
		}
	}
	
	static class Optional extends astify.Capture.ObjectCapture implements RootPattern {
		private final List<Pattern> patterns;
		
		Optional(astify.core.Position spanningPosition, List<Pattern> patterns) {
			super(spanningPosition);
			
			assert patterns != null : "'patterns' is null";
			
			this.patterns = patterns;
		}
		
		List<Pattern> getPatterns() {
			return patterns;
		}
		
		@Override
		public String toString() {
			return "(Optional\n"
				 + "	patterns = " + patterns.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Optional)) return false;
			Optional otherCasted = (Optional) other;
			return patterns.equals(otherCasted.patterns);
		}
		
		@Override
		public int hashCode() {
			return hash(patterns);
		}
	}
	
	static class Extend extends astify.Capture.ObjectCapture {
		private final NamedPropertyList properties;
		private final List<PatternList> patterns;
		private final Call call;
		
		Extend(astify.core.Position spanningPosition, NamedPropertyList properties, List<PatternList> patterns, Call call) {
			super(spanningPosition);
			
			assert properties != null : "'properties' is null";
			assert patterns != null : "'patterns' is null";
			assert call != null : "'call' is null";
			
			this.properties = properties;
			this.patterns = patterns;
			this.call = call;
		}
		
		NamedPropertyList getProperties() {
			return properties;
		}
		
		List<PatternList> getPatterns() {
			return patterns;
		}
		
		Call getCall() {
			return call;
		}
		
		@Override
		public String toString() {
			return "(Extend\n"
				 + "	properties = " + properties.toString().replace("\n", "\n\t") + "\n"
				 + "	patterns = " + patterns.toString().replace("\n", "\n\t") + "\n"
				 + "	call = " + call.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Extend)) return false;
			Extend otherCasted = (Extend) other;
			return properties.equals(otherCasted.properties)
				&& patterns.equals(otherCasted.patterns)
				&& call.equals(otherCasted.call);
		}
		
		@Override
		public int hashCode() {
			return hash(properties, patterns, call);
		}
	}
	
	interface UncapturingPattern extends Pattern, astify.core.Positioned {
		}
	
	interface Pattern extends RootPattern, astify.core.Positioned {
		}
	
	interface RootPattern extends astify.core.Positioned {
		}
	
	static class AbstractTypeDefinition extends astify.Capture.ObjectCapture implements Definition {
		private final NamedPropertyList properties;
		
		AbstractTypeDefinition(astify.core.Position spanningPosition, NamedPropertyList properties) {
			super(spanningPosition);
			
			assert properties != null : "'properties' is null";
			
			this.properties = properties;
		}
		
		NamedPropertyList getProperties() {
			return properties;
		}
		
		@Override
		public String toString() {
			return "(AbstractTypeDefinition\n"
				 + "	properties = " + properties.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof AbstractTypeDefinition)) return false;
			AbstractTypeDefinition otherCasted = (AbstractTypeDefinition) other;
			return properties.equals(otherCasted.properties);
		}
		
		@Override
		public int hashCode() {
			return hash(properties);
		}
	}
	
	static class TypeDefinition extends astify.Capture.ObjectCapture implements Definition {
		private final NamedPropertyList properties;
		private final List<PatternList> patternLists;
		
		TypeDefinition(astify.core.Position spanningPosition, NamedPropertyList properties, List<PatternList> patternLists) {
			super(spanningPosition);
			
			assert properties != null : "'properties' is null";
			assert patternLists != null : "'patternLists' is null";
			
			this.properties = properties;
			this.patternLists = patternLists;
		}
		
		NamedPropertyList getProperties() {
			return properties;
		}
		
		List<PatternList> getPatternLists() {
			return patternLists;
		}
		
		@Override
		public String toString() {
			return "(TypeDefinition\n"
				 + "	properties = " + properties.toString().replace("\n", "\n\t") + "\n"
				 + "	patternLists = " + patternLists.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof TypeDefinition)) return false;
			TypeDefinition otherCasted = (TypeDefinition) other;
			return properties.equals(otherCasted.properties)
				&& patternLists.equals(otherCasted.patternLists);
		}
		
		@Override
		public int hashCode() {
			return hash(properties, patternLists);
		}
	}
	
	static class Union extends astify.Capture.ObjectCapture implements Definition {
		private final astify.token.Token typename;
		private final List<astify.token.Token> subtypes;
		
		Union(astify.core.Position spanningPosition, astify.token.Token typename, List<astify.token.Token> subtypes) {
			super(spanningPosition);
			
			assert typename != null : "'typename' is null";
			assert subtypes != null : "'subtypes' is null";
			
			this.typename = typename;
			this.subtypes = subtypes;
		}
		
		astify.token.Token getTypename() {
			return typename;
		}
		
		List<astify.token.Token> getSubtypes() {
			return subtypes;
		}
		
		@Override
		public String toString() {
			return "(Union\n"
				 + "	typename = " + typename.toString().replace("\n", "\n\t") + "\n"
				 + "	subtypes = " + subtypes.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Union)) return false;
			Union otherCasted = (Union) other;
			return typename.equals(otherCasted.typename)
				&& subtypes.equals(otherCasted.subtypes);
		}
		
		@Override
		public int hashCode() {
			return hash(typename, subtypes);
		}
	}
	
	static class AliasDefinition extends astify.Capture.ObjectCapture implements Definition {
		private final astify.token.Token name;
		private final TypedName property;
		private final List<PatternList> patternLists;
		
		AliasDefinition(astify.core.Position spanningPosition, astify.token.Token name, TypedName property, List<PatternList> patternLists) {
			super(spanningPosition);
			
			assert name != null : "'name' is null";
			assert patternLists != null : "'patternLists' is null";
			
			this.name = name;
			this.property = property;
			this.patternLists = patternLists;
		}
		
		astify.token.Token getName() {
			return name;
		}
		
		TypedName getProperty() {
			return property;
		}
		
		List<PatternList> getPatternLists() {
			return patternLists;
		}
		
		@Override
		public String toString() {
			return "(AliasDefinition\n"
				 + "	name = " + name.toString().replace("\n", "\n\t") + "\n"
				 + "	property = " + (property == null ? "null" : property.toString().replace("\n", "\n\t")) + "\n"
				 + "	patternLists = " + patternLists.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof AliasDefinition)) return false;
			AliasDefinition otherCasted = (AliasDefinition) other;
			return name.equals(otherCasted.name)
				&& (property == null ? otherCasted.property == null : property.equals(otherCasted.property))
				&& patternLists.equals(otherCasted.patternLists);
		}
		
		@Override
		public int hashCode() {
			return hash(name, property, patternLists);
		}
	}
	
	static class ExternDefinition extends astify.Capture.ObjectCapture implements Definition {
		private final Type returnType;
		private final astify.token.Token name;
		private final List<TypedName> parameters;
		private final List<PatternList> patternLists;
		
		ExternDefinition(astify.core.Position spanningPosition, Type returnType, astify.token.Token name, List<TypedName> parameters, List<PatternList> patternLists) {
			super(spanningPosition);
			
			assert returnType != null : "'returnType' is null";
			assert name != null : "'name' is null";
			assert parameters != null : "'parameters' is null";
			assert patternLists != null : "'patternLists' is null";
			
			this.returnType = returnType;
			this.name = name;
			this.parameters = parameters;
			this.patternLists = patternLists;
		}
		
		Type getReturnType() {
			return returnType;
		}
		
		astify.token.Token getName() {
			return name;
		}
		
		List<TypedName> getParameters() {
			return parameters;
		}
		
		List<PatternList> getPatternLists() {
			return patternLists;
		}
		
		@Override
		public String toString() {
			return "(ExternDefinition\n"
				 + "	returnType = " + returnType.toString().replace("\n", "\n\t") + "\n"
				 + "	name = " + name.toString().replace("\n", "\n\t") + "\n"
				 + "	parameters = " + parameters.toString().replace("\n", "\n\t") + "\n"
				 + "	patternLists = " + patternLists.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ExternDefinition)) return false;
			ExternDefinition otherCasted = (ExternDefinition) other;
			return returnType.equals(otherCasted.returnType)
				&& name.equals(otherCasted.name)
				&& parameters.equals(otherCasted.parameters)
				&& patternLists.equals(otherCasted.patternLists);
		}
		
		@Override
		public int hashCode() {
			return hash(returnType, name, parameters, patternLists);
		}
	}
	
	static class ApplyStatement extends astify.Capture.ObjectCapture implements Statement {
		private final Call call;
		private final List<PatternList> patternLists;
		
		ApplyStatement(astify.core.Position spanningPosition, Call call, List<PatternList> patternLists) {
			super(spanningPosition);
			
			assert call != null : "'call' is null";
			assert patternLists != null : "'patternLists' is null";
			
			this.call = call;
			this.patternLists = patternLists;
		}
		
		Call getCall() {
			return call;
		}
		
		List<PatternList> getPatternLists() {
			return patternLists;
		}
		
		@Override
		public String toString() {
			return "(ApplyStatement\n"
				 + "	call = " + call.toString().replace("\n", "\n\t") + "\n"
				 + "	patternLists = " + patternLists.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ApplyStatement)) return false;
			ApplyStatement otherCasted = (ApplyStatement) other;
			return call.equals(otherCasted.call)
				&& patternLists.equals(otherCasted.patternLists);
		}
		
		@Override
		public int hashCode() {
			return hash(call, patternLists);
		}
	}
	
	interface Definition extends Statement, astify.core.Positioned {
		}
	
	interface Statement extends astify.core.Positioned {
		}
	
	static class Grammar extends astify.Capture.ObjectCapture {
		private final astify.token.Token name;
		
		Grammar(astify.core.Position spanningPosition, astify.token.Token name) {
			super(spanningPosition);
			
			assert name != null : "'name' is null";
			
			this.name = name;
		}
		
		astify.token.Token getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return "(GDL.Grammar\n"
				 + "	name = " + name.toString().replace("\n", "\n\t") + "\n"
				 + ")";
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Grammar)) return false;
			Grammar otherCasted = (Grammar) other;
			return name.equals(otherCasted.name);
		}
		
		@Override
		public int hashCode() {
			return hash(name);
		}
	}
}