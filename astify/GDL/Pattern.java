package astify.GDL;

import astify.token.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class Pattern {
    abstract String getPatternBuilderTerm();

    static class Optional extends Pattern {
        private final List<Pattern> optionalParts;

        Optional(List<Pattern> optionalParts) {
            this.optionalParts = optionalParts;
        }

        List<Pattern> getPatterns() {
            return optionalParts;
        }

        @Override public String toString() {
            return optionalParts.toString();
        }

        @Override String getPatternBuilderTerm() {
            return "optional(sequence(\n\t" + String.join(",\n\t", map(optionalParts, Pattern::getPatternBuilderTerm)) + "\n))";
        }
    }

    static class ListType extends Pattern {
        private final Pattern item;
        private final List<Pattern> separator;

        ListType(Pattern item, List<Pattern> separator) {
            this.item = item;
            this.separator = separator;
        }

        ListType(Pattern item, Pattern pattern) {
            this(item, Collections.singletonList(pattern));
        }

        ListType(Pattern pattern) {
            this(pattern, new ArrayList<>());
        }

        @Override public String toString() {
            return (separator.isEmpty() ? "list" : "delim") + "(" + item.toString() + (separator.isEmpty() ? "" : ", " + String.join(", ", map(separator, Object::toString))) + ")";
        }

        @Override String getPatternBuilderTerm() {
            return (separator.isEmpty() ? "list" : "delim") + "(" + item.getPatternBuilderTerm() + (separator.isEmpty() ? "" : ", " + String.join(", ", map(separator, Pattern::getPatternBuilderTerm))) + ")";
        }
    }

    static class Terminal extends Pattern {
        private final String value;

        Terminal(String value) {
            this.value = value;
        }

        @Override public String toString() {
            return value;
        }

        @Override String getPatternBuilderTerm() {
            for (int i = 1; i < value.length() - 1; ++i) {
                if (!Character.isLetterOrDigit(value.charAt(i)) && value.charAt(i) != '_') {
                    return "symbol(" + str(value) + ")";
                }
            }

            return "keyword(" + str(value) + ")";
        }
    }

    static class TypeReference extends Pattern {
        private final Type reference;

        TypeReference(Type reference) {
            this.reference = reference;
        }

        @Override public String toString() {
            return reference.getName();
        }

        @Override String getPatternBuilderTerm() {
            if (reference instanceof Type.DefinedType) {
                return "ref(\"" + ((Type.DefinedType) reference).getDefinition().getPatternName() + "\")";
            }
            else if (reference instanceof Type.TokenType) {
                return "token(" + ((Type.TokenType) reference).getTokenType().name() + ")";
            }
            else throw new Error("what");
        }
    }

    static class Matcher extends Pattern {
        private final Pattern source;
        private final String target;

        Matcher(Pattern source, String target) {
            this.source = source;
            this.target = target;
        }

        Pattern getSource() {
            return source;
        }

        String getTarget() {
            return target;
        }

        @Override public String toString() {
            return "(" + source.toString() + " -> " + target + ")";
        }

        @Override String getPatternBuilderTerm() {
            return source.getPatternBuilderTerm();
        }
    }

    static<T extends ASTifyGrammar.RootPattern> List<Pattern> createFromList(List<T> sourcePatterns, Definition.TypeDefinition definition, Scope scope) {
        List<Pattern> result = new ArrayList<>();

        for (ASTifyGrammar.RootPattern pattern : sourcePatterns) {
            result.add(createFrom(pattern, definition, scope));
        }

        return result;
    }

    static<T, R> List<R> map(List<T> list, java.util.function.Function<T, R> f) {
        List<R> result = new ArrayList<>();

        for (T elem : list) {
            result.add(f.apply(elem));
        }

        return result;
    }

    private static Pattern createFrom(ASTifyGrammar.RootPattern sourcePattern, Definition.TypeDefinition definition, Scope scope) {
        if (sourcePattern instanceof ASTifyGrammar.TypeReference) {
            Token sourceType = ((ASTifyGrammar.TypeReference) sourcePattern).getType();

            if (scope.exists(sourceType.getValue())) {
                return new TypeReference(scope.lookup(sourceType.getValue()));
            }
            else {
                throw new Error("TODO");
            }
        }
        else if (sourcePattern instanceof ASTifyGrammar.Terminal) {
            return new Terminal(((ASTifyGrammar.Terminal) sourcePattern).getTerminal().getValue());
        }
        else if (sourcePattern instanceof ASTifyGrammar.Function) {
            String name =(((ASTifyGrammar.Function) sourcePattern).getName()).getValue();
            List<Pattern> parameters = new ArrayList<>();

            for (ASTifyGrammar.UncapturingPattern pat : ((ASTifyGrammar.Function) sourcePattern).getPatterns()) {
                parameters.add(createFrom(pat, definition, scope));
            }

            switch (name) {
                case "list":
                    if (parameters.size() != 1) throw new Error("TODO");
                    return new ListType(parameters.get(0));
                case "delim":
                    if (parameters.size() != 2) throw new Error("TODO");
                    return new ListType(parameters.get(0), parameters.get(1));
                default:
                    throw new Error("TODO");
            }
        }
        else if (sourcePattern instanceof ASTifyGrammar.Matcher) {
            return new Matcher(createFrom(((ASTifyGrammar.Matcher) sourcePattern).getSource(), definition, scope), ((ASTifyGrammar.Matcher) sourcePattern).getTarget().getProperty().getValue());
        }
        else if (sourcePattern instanceof ASTifyGrammar.PropertyReference) {
            Property property = definition.getProperty(((ASTifyGrammar.PropertyReference) sourcePattern).getProperty().getValue());
            List<ASTifyGrammar.UncapturingPattern> qualifier = ((ASTifyGrammar.PropertyReference) sourcePattern).getQualifier();

            if (property != null) {
                Type expectedType = property.getType();
                Pattern resultingPattern;

                if (property.isList()) {
                    Pattern parameter = new TypeReference(expectedType);

                    if (qualifier.size() == 0) {
                        resultingPattern = new ListType(parameter);
                    }
                    else {
                        resultingPattern = new ListType(parameter, createFromList(qualifier, definition, scope));
                    }
                }
                else if (expectedType instanceof Type.BuiltinType) {
                    resultingPattern = new Optional(createFromList(qualifier, definition, scope));
                }
                else {
                    resultingPattern = new TypeReference(expectedType);
                }

                return new Matcher(resultingPattern, property.getPropertyName());
            }
            else {
                throw new Error("TODO");
            }
        }
        else if (sourcePattern instanceof ASTifyGrammar.Optional) {
            return new Optional(createFromList(((ASTifyGrammar.Optional) sourcePattern).getPatterns(), definition, scope));
        }
        return null;
    }

    private static String str(String s) {
        if (s.charAt(0) == '\'') {
            return "\"" + s.substring(1, s.length() - 1).replace("\"", "\\\"").replace("\\'", "'") + "\"";
        }
        return s;
    }
}
