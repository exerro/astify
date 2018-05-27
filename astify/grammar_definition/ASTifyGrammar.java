package astify.grammar_definition;

import astify.Capture;
import astify.core.Position;
import astify.core.Positioned;
import astify.token.Token;

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

    public Grammar getGrammar() {
        return grammar;
    }

    public List<Definition> getDefinitions() {
        return definitions;
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

    public static class Union extends Capture.ObjectCapture implements Definition {
        private final Token typename;
        private final List<Token> subtypes;

        public Union(Position spanningPosition, Token typename, List<Token> subtypes) {
            super(spanningPosition);
            this.typename = typename;
            this.subtypes = subtypes;
        }

        public Token getTypename() {
            return typename;
        }

        public List<Token> getSubtypes() {
            return subtypes;
        }

        public static Capture create(List<Capture> captures) {
            Token typename = ((TokenCapture) captures.get(1)).token;
            ListCapture subtypesCaptures = (ListCapture) captures.get(3);
            List<Token> subtypes = new ArrayList<>();
            Position position = captures.get(0).spanningPosition.to(captures.get(3).spanningPosition);

            for (Iterator it = subtypesCaptures.iterator(); it.hasNext(); ) {
                subtypes.add(((TokenCapture) it.next()).token);
            }

            return new Union(position, typename, subtypes);
        }
    }

    public static class Type extends Capture.ObjectCapture {
        private final Token name;
        private final boolean optional, list;

        public Type(Position spanningPosition, Token name, boolean optional, boolean list) {
            super(spanningPosition);
            this.name = name;
            this.optional = optional;
            this.list = list;
        }

        public Token getName() {
            return name;
        }

        public boolean isOptional() {
            return optional;
        }

        public boolean isList() {
            return list;
        }

        public static Capture create(List<Capture> captures) {
            Token name = ((TokenCapture) captures.get(0)).token;
            boolean optional = !(captures.get(1) instanceof EmptyCapture);
            boolean list = !(captures.get(2) instanceof EmptyCapture);
            Position position = captures.get(0).spanningPosition.to(captures.get(1).spanningPosition);

            return new Type(position, name, optional, list);
        }
    }

    public static class TypedName extends Capture.ObjectCapture {
        private final Type type;
        private final Token name;

        public TypedName(Position spanningPosition, Type type, Token name) {
            super(spanningPosition);
            this.type = type;
            this.name = name;
        }

        public Type getType() {
            return type;
        }

        public Token getName() {
            return name;
        }

        public static Capture create(List<Capture> captures) {
            Type type = (Type) captures.get(0);
            Token name = ((TokenCapture) captures.get(1)).token;
            Position position = captures.get(0).spanningPosition.to(captures.get(1).spanningPosition);

            return new TypedName(position, type, name);
        }
    }

    public static class NamedPropertyList extends Capture.ObjectCapture {
        private final Token name;
        private final List<TypedName> properties;

        public NamedPropertyList(Position spanningPosition, Token name, List<TypedName> properties) {
            super(spanningPosition);
            this.name = name;
            this.properties = properties;
        }

        public Token getName() {
            return name;
        }

        public List<TypedName> getProperties() {
            return properties;
        }

        public static Capture create(List<Capture> captures) {
            Token name = ((TokenCapture) captures.get(0)).token;
            List<TypedName> properties = new ArrayList<>();
            Position position = captures.get(0).spanningPosition.to(captures.get(3).spanningPosition);

            if (!(captures.get(2) instanceof EmptyCapture)) {
                ListCapture propertiesCaptures = (ListCapture) captures.get(2);

                for (Iterator it = propertiesCaptures.iterator(); it.hasNext(); ) {
                    properties.add((TypedName) it.next());
                }
            }

            return new NamedPropertyList(position, name, properties);
        }
    }

    public static class AbstractTypeDefinition extends Capture.ObjectCapture implements Definition {
        private final NamedPropertyList properties;

        public AbstractTypeDefinition(Position spanningPosition, NamedPropertyList properties) {
            super(spanningPosition);
            this.properties = properties;
        }

        public NamedPropertyList getProperties() {
            return properties;
        }

        public static Capture create(List<Capture> captures) {
            NamedPropertyList properties = (NamedPropertyList) captures.get(1);
            Position position = captures.get(0).spanningPosition.to(captures.get(1).spanningPosition);

            return new AbstractTypeDefinition(position, properties);
        }
    }

    public static class ParameterReference extends Capture.ObjectCapture implements Pattern {
        private final Token parameter;
        private final List<Pattern> delimiter;

        public ParameterReference(Position spanningPosition, Token parameter, List<Pattern> delimiter) {
            super(spanningPosition);
            this.parameter = parameter;
            this.delimiter = delimiter;
        }

        public Token getParameter() {
            return parameter;
        }

        public List<Pattern> getDelimiter() {
            return delimiter;
        }

        public static Capture create(List<Capture> captures) {
            Token parameter = ((TokenCapture) captures.get(1)).token;
            List<Pattern> delimiter = new ArrayList<>();
            Position position = captures.get(0).spanningPosition.to(captures.get(2).spanningPosition);

            if (!(captures.get(2) instanceof EmptyCapture)) {
                ListCapture delimiterCaptures = (ListCapture) ((ListCapture) captures.get(2)).get(1);

                for (Iterator it = delimiterCaptures.iterator(); it.hasNext(); ) {
                    delimiter.add((Pattern) it.next());
                }
            }

            return new ParameterReference(position, parameter, delimiter);
        }
    }

    public static class TypeReference extends Capture.ObjectCapture implements Pattern {
        private final Token type;

        public TypeReference(Position spanningPosition, Token type) {
            super(spanningPosition);
            this.type = type;
        }

        public Token getType() {
            return type;
        }

        public static Capture create(List<Capture> captures) {
            Token type = ((TokenCapture) captures.get(1)).token;
            Position position = captures.get(0).spanningPosition;

            return new TypeReference(position, type);
        }
    }

    public static class Terminal extends Capture.ObjectCapture implements Pattern {
        private final Token terminal;

        public Terminal(Position spanningPosition, Token terminal) {
            super(spanningPosition);
            this.terminal = terminal;
        }

        public Token getTerminal() {
            return terminal;
        }

        public static Capture create(List<Capture> captures) {
            Token terminal = ((TokenCapture) captures.get(0)).token;
            Position position = terminal.getPosition();

            return new Terminal(position, terminal);
        }
    }

    public static class Optional extends Capture.ObjectCapture implements Pattern {
        private final List<Pattern> patterns;

        public Optional(Position spanningPosition, List<Pattern> patterns) {
            super(spanningPosition);
            this.patterns = patterns;
        }

        public List<Pattern> getPatterns() {
            return patterns;
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
    }

    public static class PatternList extends Capture.ObjectCapture {
        private final List<Pattern> patterns;

        public PatternList(Position spanningPosition, List<Pattern> patterns) {
            super(spanningPosition);
            this.patterns = patterns;
        }

        public List<Pattern> getPatterns() {
            return patterns;
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
    }

    public static class TypeDefinition extends Capture.ObjectCapture implements Definition {
        private final NamedPropertyList properties;
        private final List<PatternList> patterns;

        public TypeDefinition(Position spanningPosition, NamedPropertyList properties, List<PatternList> patterns) {
            super(spanningPosition);
            this.properties = properties;
            this.patterns = patterns;
        }

        public NamedPropertyList getProperties() {
            return properties;
        }

        public List<PatternList> getPatterns() {
            return patterns;
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
    }

    public static class Grammar extends Capture.ObjectCapture {
        private final Token name;

        public Grammar(Position spanningPosition, Token name) {
            super(spanningPosition);
            this.name = name;
        }

        public Token getName() {
            return name;
        }

        public static Capture create(List<Capture> captures) {
            Token name = ((TokenCapture) captures.get(1)).token;
            Position position = captures.get(0).spanningPosition.to(captures.get(1).spanningPosition);

            return new Grammar(position, name);
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
