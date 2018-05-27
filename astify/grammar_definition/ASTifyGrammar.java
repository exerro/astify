package astify.grammar_definition;

import astify.Capture;
import astify.core.Position;
import astify.core.Positioned;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ASTifyGrammar extends Capture.ObjectCapture {
    private final Grammar grammar;
    private final List<Definition> definitions;

    public ASTifyGrammar(Position spanningPosition, Grammar grammar, List<Definition> definitions) {
        super(spanningPosition);
        this.grammar = grammar;
        this.definitions = definitions;
    }

    public static Capture create(List<Capture> captures) {
        Grammar grammar = (Grammar) captures.get(0);
        ListCapture definitionsCaptures = (ListCapture) captures.get(1);
        List<Definition> definitions = new ArrayList<>();
        Position position = captures.get(0).spanningPosition.to(captures.get(1).spanningPosition);

        for (Iterator it = definitionsCaptures.iterator(); it.hasNext(); ) {
            definitions.add((Definition) it.next());
        }

        return new ASTifyGrammar(position, grammar, definitions);
    }

    @Override public String toString() {
        List<String> definitionStrings = new ArrayList<>();

        for (Definition definition : definitions) {
            definitionStrings.add(definition.toString());
        }

        return "<ASTify-grammar " + grammar.toString() + ", " + String.join(", ", definitionStrings) + ">";
    }

    public static class Union extends Capture.ObjectCapture implements Definition {
        private final String typename;
        private final List<String> subtypes;

        public Union(Position spanningPosition, String typename, List<String> subtypes) {
            super(spanningPosition);
            this.typename = typename;
            this.subtypes = subtypes;
        }

        public static Capture create(List<Capture> captures) {
            TokenCapture typenameCapture = (TokenCapture) captures.get(1);
            ListCapture subtypesCaptures = (ListCapture) captures.get(3);
            List<String> subtypes = new ArrayList<>();
            Position position = captures.get(0).spanningPosition.to(captures.get(3).spanningPosition);

            for (Iterator it = subtypesCaptures.iterator(); it.hasNext(); ) {
                subtypes.add(((TokenCapture) it.next()).getValue());
            }

            return new Union(position, typenameCapture.getValue(), subtypes);
        }

        @Override public String toString() {
            return "<Union " + typename + ", " + String.join(", ", subtypes) + ">";
        }
    }

    public static class Type extends Capture.ObjectCapture {
        private final String name;
        private final boolean optional, list;

        public Type(Position spanningPosition, String name, boolean optional, boolean list) {
            super(spanningPosition);
            this.name = name;
            this.optional = optional;
            this.list = list;
        }

        public static Capture create(List<Capture> captures) {
            TokenCapture nameCapture = (TokenCapture) captures.get(0);
            boolean optional = !(captures.get(1) instanceof EmptyCapture);
            boolean list = !(captures.get(2) instanceof EmptyCapture);
            Position position = captures.get(0).spanningPosition.to(captures.get(1).spanningPosition);

            return new Type(position, nameCapture.getValue(), optional, list);
        }

        @Override public String toString() {
            return "<Type " + name + (optional ? "?" : "") + (list ? "[]" : "") + ">";
        }
    }

    public static class TypedName extends Capture.ObjectCapture {
        private final Type type;
        private final String name;

        public TypedName(Position spanningPosition, Type type, String name) {
            super(spanningPosition);
            this.type = type;
            this.name = name;
        }

        public static Capture create(List<Capture> captures) {
            Type type = (Type) captures.get(0);
            TokenCapture nameCapture = (TokenCapture) captures.get(1);
            Position position = captures.get(0).spanningPosition.to(captures.get(1).spanningPosition);

            return new TypedName(position, type, nameCapture.getValue());
        }

        @Override public String toString() {
            return "<TypedName " + type.toString() + ", " + name + ">";
        }
    }

    public static class NamedPropertyList extends Capture.ObjectCapture {
        private final String name;
        private final List<TypedName> properties;

        public NamedPropertyList(Position spanningPosition, String name, List<TypedName> properties) {
            super(spanningPosition);
            this.name = name;
            this.properties = properties;
        }

        public static Capture create(List<Capture> captures) {
            TokenCapture nameCapture = (TokenCapture) captures.get(0);
            List<TypedName> properties = new ArrayList<>();
            Position position = captures.get(0).spanningPosition.to(captures.get(3).spanningPosition);

            if (!(captures.get(2) instanceof EmptyCapture)) {
                ListCapture propertiesCaptures = (ListCapture) captures.get(2);

                for (Iterator it = propertiesCaptures.iterator(); it.hasNext(); ) {
                    properties.add((TypedName) it.next());
                }
            }

            return new NamedPropertyList(position, nameCapture.getValue(), properties);
        }

        @Override public String toString() {
            List<String> propertyStrings = new ArrayList<>();

            for (TypedName property : properties) {
                propertyStrings.add(property.toString());
            }

            return "<NamedPropertyList " + name + ", " + String.join(", ", propertyStrings) + ">";
        }
    }

    public static class AbstractTypeDefinition extends Capture.ObjectCapture implements Definition {
        private final NamedPropertyList properties;

        public AbstractTypeDefinition(Position spanningPosition, NamedPropertyList properties) {
            super(spanningPosition);
            this.properties = properties;
        }

        public static Capture create(List<Capture> captures) {
            NamedPropertyList properties = (NamedPropertyList) captures.get(1);
            Position position = captures.get(0).spanningPosition.to(captures.get(1).spanningPosition);

            return new AbstractTypeDefinition(position, properties);
        }

        @Override public String toString() {
            return "<AbstractTypeDefinition " + properties.toString() + ">";
        }
    }

    public static class ParameterReference extends Capture.ObjectCapture implements Pattern {
        private final String parameter;
        private final List<Pattern> delimiter;

        public ParameterReference(Position spanningPosition, String parameter, List<Pattern> delimiter) {
            super(spanningPosition);
            this.parameter = parameter;
            this.delimiter = delimiter;
        }

        public static Capture create(List<Capture> captures) {
            TokenCapture parameterCapture = (TokenCapture) captures.get(1);
            List<Pattern> delimiter = new ArrayList<>();
            Position position = captures.get(0).spanningPosition.to(captures.get(2).spanningPosition);

            if (!(captures.get(2) instanceof EmptyCapture)) {
                ListCapture delimiterCaptures = (ListCapture) ((ListCapture) captures.get(2)).get(1);

                for (Iterator it = delimiterCaptures.iterator(); it.hasNext(); ) {
                    delimiter.add((Pattern) it.next());
                }
            }

            return new ParameterReference(position, parameterCapture.getValue(), delimiter);
        }

        @Override public String toString() {
            List<String> patternStrings = new ArrayList<>();

            for (Pattern part : delimiter) {
                patternStrings.add(part.toString());
            }

            return "<ParameterReference " + parameter + ", " + String.join(", ", patternStrings) + ">";
        }
    }

    public static class Terminal extends Capture.ObjectCapture implements Pattern {
        private final String terminal;

        public Terminal(Position spanningPosition, String terminal) {
            super(spanningPosition);
            this.terminal = terminal;
        }

        public static Capture create(List<Capture> captures) {
            TokenCapture terminalCapture = (TokenCapture) captures.get(0);
            Position position = terminalCapture.getPosition();

            return new Terminal(position, terminalCapture.getValue());
        }

        @Override public String toString() {
            return "<Terminal " + terminal + ">";
        }
    }

    public static class Optional extends Capture.ObjectCapture implements Pattern {
        private final List<Pattern> patterns;

        public Optional(Position spanningPosition, List<Pattern> patterns) {
            super(spanningPosition);
            this.patterns = patterns;
        }

        public static Capture create(List<Capture> captures) {
            ListCapture patternsCaptures = (ListCapture) captures.get(1);
            List<Pattern> patterns = new ArrayList<>();
            Position position = captures.get(0).spanningPosition.to(captures.get(2).spanningPosition);

            for (Iterator it = patternsCaptures.iterator(); it.hasNext(); ) {
                patterns.add((Pattern) it.next());
            }

            return new Optional(position, patterns);
        }

        @Override public String toString() {
            List<String> patternStrings = new ArrayList<>();

            for (Pattern part : patterns) {
                patternStrings.add(part.toString());
            }

            return "<Optional " + String.join(", ", patternStrings) + ">";
        }
    }

    public static class PatternList extends Capture.ObjectCapture {
        private final List<Pattern> patterns;

        public PatternList(Position spanningPosition, List<Pattern> patterns) {
            super(spanningPosition);
            this.patterns = patterns;
        }

        public static Capture create(List<Capture> captures) {
            ListCapture patternsCaptures = (ListCapture) captures.get(0);
            List<Pattern> patterns = new ArrayList<>();
            Position position = captures.get(0).spanningPosition;

            for (Iterator it = patternsCaptures.iterator(); it.hasNext(); ) {
                patterns.add((Pattern) it.next());
            }

            return new PatternList(position, patterns);
        }

        @Override public String toString() {
            List<String> patternStrings = new ArrayList<>();

            for (Pattern part : patterns) {
                patternStrings.add(part.toString());
            }

            return "<PatternList " + String.join(", ", patternStrings) + ">";
        }
    }

    public static class TypeDefinition extends Capture.ObjectCapture implements Definition {
        private final NamedPropertyList properties;
        private final List<PatternList> patterns;

        public TypeDefinition(Position spanningPosition, NamedPropertyList properties, List<PatternList> patterns) {
            super(spanningPosition);
            this.properties = properties;
            this.patterns = patterns;
        }

        public static Capture create(List<Capture> captures) {
            NamedPropertyList properties = (NamedPropertyList) captures.get(0);
            ListCapture patternsCaptures = (ListCapture) captures.get(2);
            List<PatternList> patterns = new ArrayList<>();
            Position position = captures.get(0).spanningPosition.to(captures.get(2).spanningPosition);

            for (Iterator it = patternsCaptures.iterator(); it.hasNext(); ) {
                patterns.add((PatternList) it.next());
            }

            return new TypeDefinition(position, properties, patterns);
        }

        @Override public String toString() {
            List<String> patternStrings = new ArrayList<>();

            for (PatternList part : patterns) {
                patternStrings.add(part.toString());
            }

            return "<TypeDefinition " + properties.toString() + ", " + String.join(", ", patternStrings) + ">";
        }
    }

    public static class Grammar extends Capture.ObjectCapture {
        private final String name;

        public Grammar(Position spanningPosition, String name) {
            super(spanningPosition);
            this.name = name;
        }

        public static Capture create(List<Capture> captures) {
            TokenCapture nameCapture = (TokenCapture) captures.get(1);
            Position position = captures.get(0).spanningPosition.to(captures.get(1).spanningPosition);

            return new Grammar(position, nameCapture.getValue());
        }

        @Override public String toString() {
            return "<Grammar " + name + ">";
        }
    }

    public interface Pattern extends Positioned {

    }

    public interface Definition extends Positioned {

    }
}
