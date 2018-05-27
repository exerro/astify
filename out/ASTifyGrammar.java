package out;

import astify.Capture;
import astify.core.Position;
import astify.core.Positioned;
import astify.grammar_definition.support.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ASTifyGrammar extends Capture.ObjectCapture {
	private final Grammar _grammar;
	private final List<Definition> definitions;
	
	ASTifyGrammar(Position spanningPosition, Grammar _grammar, List<Definition> definitions) {
		super(spanningPosition);
		this._grammar = _grammar;
		this.definitions = definitions;
	}
	
	public Grammar getGrammar() {
		return this._grammar;
	}
	
	public List<Definition> getDefinitions() {
		return this.definitions;
	}
	
	@Override public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("<ASTifyGrammar");
		result.append("\n" + _grammar.toString());
		result.append("\n" + "[" + Util.concatList(definitions) + "]");
		result.append("\n>");
		return result.toString();
	}
	
	interface Pattern extends Positioned {
	}
	
	static class Optional extends Capture.ObjectCapture implements Pattern {
		private final List<Pattern> patterns;
		
		Optional(Position spanningPosition, List<Pattern> patterns) {
			super(spanningPosition);
			this.patterns = patterns;
		}
		
		public List<Pattern> getPatterns() {
			return this.patterns;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<Optional");
			result.append("\n" + "[" + Util.concatList(patterns) + "]");
			result.append("\n>");
			return result.toString();
		}
	}
	
	static class ParameterReference extends Capture.ObjectCapture implements Pattern {
		private final List<Pattern> delimiter;
		private final String parameter;
		
		ParameterReference(Position spanningPosition, List<Pattern> delimiter, String parameter) {
			super(spanningPosition);
			this.delimiter = delimiter;
			this.parameter = parameter;
		}
		
		public List<Pattern> getDelimiter() {
			return this.delimiter;
		}
		
		public String getParameter() {
			return this.parameter;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<ParameterReference");
			result.append("\n" + "[" + Util.concatList(delimiter) + "]");
			result.append("\n" + parameter.toString());
			result.append("\n>");
			return result.toString();
		}
	}
	
	static class PatternList extends Capture.ObjectCapture {
		private final List<Pattern> patterns;
		
		PatternList(Position spanningPosition, List<Pattern> patterns) {
			super(spanningPosition);
			this.patterns = patterns;
		}
		
		public List<Pattern> getPatterns() {
			return this.patterns;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<PatternList");
			result.append("\n" + "[" + Util.concatList(patterns) + "]");
			result.append("\n>");
			return result.toString();
		}
	}
	
	interface Definition extends Positioned {
	}
	
	static class TypedName extends Capture.ObjectCapture {
		private final String name;
		private final Type type;
		
		TypedName(Position spanningPosition, String name, Type type) {
			super(spanningPosition);
			this.name = name;
			this.type = type;
		}
		
		public String getName() {
			return this.name;
		}
		
		public Type getType() {
			return this.type;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<TypedName");
			result.append("\n" + name.toString());
			result.append("\n" + type.toString());
			result.append("\n>");
			return result.toString();
		}
	}
	
	static class Grammar extends Capture.ObjectCapture {
		private final String name;
		
		Grammar(Position spanningPosition, String name) {
			super(spanningPosition);
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<Grammar");
			result.append("\n" + name.toString());
			result.append("\n>");
			return result.toString();
		}
	}
	
	static class Terminal extends Capture.ObjectCapture implements Pattern {
		private final String terminal;
		
		Terminal(Position spanningPosition, String terminal) {
			super(spanningPosition);
			this.terminal = terminal;
		}
		
		public String getTerminal() {
			return this.terminal;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<Terminal");
			result.append("\n" + terminal.toString());
			result.append("\n>");
			return result.toString();
		}
	}
	
	static class Union extends Capture.ObjectCapture implements Definition {
		private final List<String> subtypes;
		private final String typename;
		
		Union(Position spanningPosition, List<String> subtypes, String typename) {
			super(spanningPosition);
			this.subtypes = subtypes;
			this.typename = typename;
		}
		
		public List<String> getSubtypes() {
			return this.subtypes;
		}
		
		public String getTypename() {
			return this.typename;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<Union");
			result.append("\n" + "[" + Util.concatList(subtypes) + "]");
			result.append("\n" + typename.toString());
			result.append("\n>");
			return result.toString();
		}
	}
	
	static class NamedPropertyList extends Capture.ObjectCapture {
		private final String name;
		private final List<TypedName> properties;
		
		NamedPropertyList(Position spanningPosition, String name, List<TypedName> properties) {
			super(spanningPosition);
			this.name = name;
			this.properties = properties;
		}
		
		public String getName() {
			return this.name;
		}
		
		public List<TypedName> getProperties() {
			return this.properties;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<NamedPropertyList");
			result.append("\n" + name.toString());
			result.append("\n" + "[" + Util.concatList(properties) + "]");
			result.append("\n>");
			return result.toString();
		}
	}
	
	static class TypeDefinition extends Capture.ObjectCapture implements Definition {
		private final NamedPropertyList properties;
		private final List<PatternList> patterns;
		
		TypeDefinition(Position spanningPosition, NamedPropertyList properties, List<PatternList> patterns) {
			super(spanningPosition);
			this.properties = properties;
			this.patterns = patterns;
		}
		
		public NamedPropertyList getProperties() {
			return this.properties;
		}
		
		public List<PatternList> getPatterns() {
			return this.patterns;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<TypeDefinition");
			result.append("\n" + properties.toString());
			result.append("\n" + "[" + Util.concatList(patterns) + "]");
			result.append("\n>");
			return result.toString();
		}
	}
	
	static class Type extends Capture.ObjectCapture {
		private final Boolean optional;
		private final Boolean list;
		private final String name;
		
		Type(Position spanningPosition, Boolean optional, Boolean list, String name) {
			super(spanningPosition);
			this.optional = optional;
			this.list = list;
			this.name = name;
		}
		
		public Boolean isOptional() {
			return this.optional;
		}
		
		public Boolean isList() {
			return this.list;
		}
		
		public String getName() {
			return this.name;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<Type");
			result.append("\n" + optional.toString());
			result.append("\n" + list.toString());
			result.append("\n" + name.toString());
			result.append("\n>");
			return result.toString();
		}
	}
	
	static class AbstractTypeDefinition extends Capture.ObjectCapture implements Definition {
		private final NamedPropertyList propertiesDoop;
		
		AbstractTypeDefinition(Position spanningPosition, NamedPropertyList propertiesDoop) {
			super(spanningPosition);
			this.propertiesDoop = propertiesDoop;
		}
		
		public NamedPropertyList getPropertiesDoop() {
			return this.propertiesDoop;
		}
		
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("<AbstractTypeDefinition");
			result.append("\n" + propertiesDoop.toString());
			result.append("\n>");
			return result.toString();
		}
	}
}
