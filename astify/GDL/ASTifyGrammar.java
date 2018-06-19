package astify.GDL;

import astify.Capture;
import astify.token.Token;
import astify.core.Position;
import astify.core.Positioned;
import astify.GDL.support.Util;

import java.util.List;

import static java.util.Objects.hash;

public class ASTifyGrammar extends Capture.ObjectCapture {
    private final Grammar _grammar;
    private final List<Definition> definitions;

    ASTifyGrammar(Position spanningPosition, Grammar _grammar, List<Definition> definitions) {
        super(spanningPosition);
        assert _grammar != null : "_grammar is null";
        assert definitions != null : "definitions is null";
        this._grammar = _grammar;
        this.definitions = definitions;
    }

    public Grammar getGrammar() {
        return _grammar;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    @Override public String toString() {
        return "<ASTifyGrammar"
                + "\n\t_grammar: " + _grammar.toString().replace("\n", "\n\t")
                + "\n\tdefinitions: [" + Util.concatList(definitions).replace("\n", "\n\t") + "]"
                + "\n>";
    }

    @Override public boolean equals(Object object) {
        if (!(object instanceof ASTifyGrammar)) return false;
        ASTifyGrammar objectASTifyGrammar = (ASTifyGrammar) object;
        if (!(_grammar.equals(objectASTifyGrammar._grammar))) return false;
        return definitions.equals(objectASTifyGrammar.definitions);
    }

    @Override public int hashCode() {
        return hash(_grammar, definitions);
    }

    static class AbstractTypeDefinition extends Capture.ObjectCapture implements Definition {
        private final NamedPropertyList properties;

        AbstractTypeDefinition(Position spanningPosition, NamedPropertyList properties) {
            super(spanningPosition);
            assert properties != null : "properties is null";
            this.properties = properties;
        }

        public NamedPropertyList getProperties() {
            return properties;
        }

        @Override public String toString() {
            return "<AbstractTypeDefinition"
                    + "\n\tproperties: " + properties.toString().replace("\n", "\n\t")
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof AbstractTypeDefinition)) return false;
            AbstractTypeDefinition objectAbstractTypeDefinition = (AbstractTypeDefinition) object;
            return properties.equals(objectAbstractTypeDefinition.properties);
        }

        @Override public int hashCode() {
            return hash(properties);
        }
    }

    interface Definition extends Positioned {
    }

    static class Grammar extends Capture.ObjectCapture {
        private final Token name;

        Grammar(Position spanningPosition, Token name) {
            super(spanningPosition);
            assert name != null : "name is null";
            this.name = name;
        }

        public Token getName() {
            return name;
        }

        @Override public String toString() {
            return "<Grammar"
                    + "\n\tname: " + name.getValue().replace("\n", "\n\t")
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof Grammar)) return false;
            Grammar objectGrammar = (Grammar) object;
            return name.equals(objectGrammar.name);
        }

        @Override public int hashCode() {
            return hash(name);
        }
    }

    static class Function extends Capture.ObjectCapture implements UncapturingPattern {
        private final Token name;
        private final List<UncapturingPattern> patterns;

        Function(Position spanningPosition, Token name, List<UncapturingPattern> patterns) {
            super(spanningPosition);
            this.name = name;
            this.patterns = patterns;
        }

        public Token getName() {
            return name;
        }

        public List<UncapturingPattern> getPatterns() {
            return patterns;
        }

        @Override public String toString() {
            return "<Function"
                    + "\n\tname: " + name.toString().replace("\n", "\n\t")
                    + "\n\tpatterns: [" + Util.concatList(patterns).replace("\n", "\n\t") + "]"
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof Function)) return false;
            Function objectFunction = (Function) object;
            return name.equals(objectFunction.name) && patterns.equals(objectFunction.patterns);
        }

        @Override public int hashCode() {
            return hash(name, patterns);
        }
    }

    static class Matcher extends Capture.ObjectCapture implements Pattern {
        private final UncapturingPattern source;
        private final MatcherTarget target;

        Matcher(Position spanningPosition, UncapturingPattern source, MatcherTarget target) {
            super(spanningPosition);
            this.source = source;
            this.target = target;
        }

        public UncapturingPattern getSource() {
            return source;
        }

        public MatcherTarget getTarget() {
            return target;
        }

        @Override public String toString() {
            return "<Matcher"
                    + "\n\tsource: " + source.toString().replace("\n", "\n\t")
                    + "\n\ttarget: " + target.toString().replace("\n", "\n\t")
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof Matcher)) return false;
            Matcher objectMatcher = (Matcher) object;
            return source.equals(objectMatcher.source) && target.equals(objectMatcher.target);
        }

        @Override public int hashCode() {
            return hash(source, target);
        }
    }

    static class MatcherTarget extends Capture.ObjectCapture {
        private final Token property;

        MatcherTarget(Position spanningPosition, Token property) {
            super(spanningPosition);
            this.property = property;
        }

        public Token getProperty() {
            return property;
        }

        @Override public String toString() {
            return "<MatcherTarget"
                    + "\n\tproperty: " + property.getValue().replace("\n", "\n\t")
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof MatcherTarget)) return false;
            MatcherTarget objectMatcherTarget = (MatcherTarget) object;
            return property.equals(objectMatcherTarget.property);
        }

        @Override public int hashCode() {
            return hash(property);
        }
    }

    static class NamedPropertyList extends Capture.ObjectCapture {
        private final Token name;
        private final List<TypedName> properties;

        NamedPropertyList(Position spanningPosition, Token name, List<TypedName> properties) {
            super(spanningPosition);
            assert name != null : "name is null";
            assert properties != null : "properties is null";
            this.name = name;
            this.properties = properties;
        }

        public Token getName() {
            return name;
        }

        public List<TypedName> getProperties() {
            return properties;
        }

        @Override public String toString() {
            return "<NamedPropertyList"
                    + "\n\tname: " + name.getValue().replace("\n", "\n\t")
                    + "\n\tproperties: [" + Util.concatList(properties).replace("\n", "\n\t") + "]"
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof NamedPropertyList)) return false;
            NamedPropertyList objectNamedPropertyList = (NamedPropertyList) object;
            if (!(name.equals(objectNamedPropertyList.name))) return false;
            return properties.equals(objectNamedPropertyList.properties);
        }

        @Override public int hashCode() {
            return hash(name, properties);
        }
    }

    static class Optional extends Capture.ObjectCapture implements RootPattern {
        private final List<Pattern> patterns;

        Optional(Position spanningPosition, List<Pattern> patterns) {
            super(spanningPosition);
            assert patterns != null : "patterns is null";
            this.patterns = patterns;
        }

        public List<Pattern> getPatterns() {
            return patterns;
        }

        @Override public String toString() {
            return "<Optional"
                    + "\n\tpatterns: [" + Util.concatList(patterns).replace("\n", "\n\t") + "]"
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof Optional)) return false;
            Optional objectOptional = (Optional) object;
            return patterns.equals(objectOptional.patterns);
        }

        @Override public int hashCode() {
            return hash(patterns);
        }
    }

    interface Pattern extends Positioned, RootPattern {
    }

    static class PatternList extends Capture.ObjectCapture {
        private final List<RootPattern> patterns;

        PatternList(Position spanningPosition, List<RootPattern> patterns) {
            super(spanningPosition);
            assert patterns != null : "patterns is null";
            this.patterns = patterns;
        }

        public List<RootPattern> getPatterns() {
            return patterns;
        }

        @Override public String toString() {
            return "<PatternList"
                    + "\n\tpatterns: [" + Util.concatList(patterns).replace("\n", "\n\t") + "]"
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof PatternList)) return false;
            PatternList objectPatternList = (PatternList) object;
            return patterns.equals(objectPatternList.patterns);
        }

        @Override public int hashCode() {
            return hash(patterns);
        }
    }

    static class PropertyReference extends Capture.ObjectCapture implements Pattern {
        private final Token property;
        private final List<UncapturingPattern> qualifier;

        PropertyReference(Position spanningPosition, Token property, List<UncapturingPattern> qualifier) {
            super(spanningPosition);
            assert property != null : "property is null";
            assert qualifier != null : "qualifier is null";
            this.property = property;
            this.qualifier = qualifier;
        }

        public Token getProperty() {
            return property;
        }

        public List<UncapturingPattern> getQualifier() {
            return qualifier;
        }

        @Override public String toString() {
            return "<PropertyReference"
                    + "\n\tproperty: " + property.getValue().replace("\n", "\n\t")
                    + "\n\tqualifier: [" + Util.concatList(qualifier).replace("\n", "\n\t") + "]"
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof PropertyReference)) return false;
            PropertyReference objectPropertyReference = (PropertyReference) object;
            if (!(property.equals(objectPropertyReference.property))) return false;
            return qualifier.equals(objectPropertyReference.qualifier);
        }

        @Override public int hashCode() {
            return hash(property, qualifier);
        }
    }

    interface RootPattern {

    }

    static class Terminal extends Capture.ObjectCapture implements UncapturingPattern {
        private final Token terminal;

        Terminal(Position spanningPosition, Token terminal) {
            super(spanningPosition);
            assert terminal != null : "terminal is null";
            this.terminal = terminal;
        }

        public Token getTerminal() {
            return terminal;
        }

        @Override public String toString() {
            return "<Terminal"
                    + "\n\tterminal: " + terminal.getValue().replace("\n", "\n\t")
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof Terminal)) return false;
            Terminal objectTerminal = (Terminal) object;
            return terminal.equals(objectTerminal.terminal);
        }

        @Override public int hashCode() {
            return hash(terminal);
        }
    }

    static class Type extends Capture.ObjectCapture {
        private final Token name;
        private final Boolean optional;
        private final Boolean lst;

        Type(Position spanningPosition, Token name, Boolean optional, Boolean lst) {
            super(spanningPosition);
            assert name != null : "name is null";
            assert optional != null : "optional is null";
            assert lst != null : "lst is null";
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

        @Override public String toString() {
            return "<Type"
                    + "\n\tname: " + name.getValue().replace("\n", "\n\t")
                    + "\n\toptional: " + optional.toString().replace("\n", "\n\t")
                    + "\n\tlst: " + lst.toString().replace("\n", "\n\t")
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof Type)) return false;
            Type objectType = (Type) object;
            if (!(name.equals(objectType.name))) return false;
            if (!(optional.equals(objectType.optional))) return false;
            return lst.equals(objectType.lst);
        }

        @Override public int hashCode() {
            return hash(name, optional, lst);
        }
    }

    static class TypeDefinition extends Capture.ObjectCapture implements Definition {
        private final NamedPropertyList properties;
        private final List<PatternList> patterns;

        TypeDefinition(Position spanningPosition, NamedPropertyList properties, List<PatternList> patterns) {
            super(spanningPosition);
            assert properties != null : "properties is null";
            assert patterns != null : "patterns is null";
            this.properties = properties;
            this.patterns = patterns;
        }

        public NamedPropertyList getProperties() {
            return properties;
        }

        public List<PatternList> getPatterns() {
            return patterns;
        }

        @Override public String toString() {
            return "<TypeDefinition"
                    + "\n\tproperties: " + properties.toString().replace("\n", "\n\t")
                    + "\n\tpatterns: [" + Util.concatList(patterns).replace("\n", "\n\t") + "]"
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof TypeDefinition)) return false;
            TypeDefinition objectTypeDefinition = (TypeDefinition) object;
            if (!(properties.equals(objectTypeDefinition.properties))) return false;
            return patterns.equals(objectTypeDefinition.patterns);
        }

        @Override public int hashCode() {
            return hash(properties, patterns);
        }
    }

    static class TypeReference extends Capture.ObjectCapture implements UncapturingPattern {
        private final Token type;

        TypeReference(Position spanningPosition, Token type) {
            super(spanningPosition);
            assert type != null : "type is null";
            this.type = type;
        }

        public Token getType() {
            return type;
        }

        @Override public String toString() {
            return "<TypeReference"
                    + "\n\ttype: " + type.getValue().replace("\n", "\n\t")
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof TypeReference)) return false;
            TypeReference objectTypeReference = (TypeReference) object;
            return type.equals(objectTypeReference.type);
        }

        @Override public int hashCode() {
            return hash(type);
        }
    }

    static class TypedName extends Capture.ObjectCapture {
        private final Type type;
        private final Token name;

        TypedName(Position spanningPosition, Type type, Token name) {
            super(spanningPosition);
            assert type != null : "type is null";
            assert name != null : "name is null";
            this.type = type;
            this.name = name;
        }

        public Type getType() {
            return type;
        }

        public Token getName() {
            return name;
        }

        @Override public String toString() {
            return "<TypedName"
                    + "\n\ttype: " + type.toString().replace("\n", "\n\t")
                    + "\n\tname: " + name.getValue().replace("\n", "\n\t")
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof TypedName)) return false;
            TypedName objectTypedName = (TypedName) object;
            if (!(type.equals(objectTypedName.type))) return false;
            return name.equals(objectTypedName.name);
        }

        @Override public int hashCode() {
            return hash(type, name);
        }
    }

    interface UncapturingPattern extends Pattern, Positioned {

    }

    static class Union extends Capture.ObjectCapture implements Definition {
        private final Token typename;
        private final List<Token> subtypes;

        Union(Position spanningPosition, Token typename, List<Token> subtypes) {
            super(spanningPosition);
            assert typename != null : "typename is null";
            assert subtypes != null : "subtypes is null";
            this.typename = typename;
            this.subtypes = subtypes;
        }

        public Token getTypename() {
            return typename;
        }

        public List<Token> getSubtypes() {
            return subtypes;
        }

        @Override public String toString() {
            return "<Union"
                    + "\n\ttypename: " + typename.getValue().replace("\n", "\n\t")
                    + "\n\tsubtypes: [" + Util.concatList(subtypes).replace("\n", "\n\t") + "]"
                    + "\n>";
        }

        @Override public boolean equals(Object object) {
            if (!(object instanceof Union)) return false;
            Union objectUnion = (Union) object;
            if (!(typename.equals(objectUnion.typename))) return false;
            return subtypes.equals(objectUnion.subtypes);
        }

        @Override public int hashCode() {
            return hash(typename, subtypes);
        }
    }
}
