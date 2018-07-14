package GDL;

import astify.MatchPredicate;
import astify.core.Position;
import astify.token.Token;
import astify.token.TokenType;

import java.util.*;

abstract class Pattern {
    // used for UncapturingPatterns
    abstract Type getType();

    static class Optional extends Pattern {
        private final List<Pattern> optionalParts;

        Optional(List<Pattern> optionalParts) {
            this.optionalParts = optionalParts;
        }

        List<Pattern> getPatterns() {
            return optionalParts;
        }

        @Override Type getType() {
            return null;
        }

        @Override public String toString() {
            return optionalParts.toString();
        }
    }

    static class OptionalCapture extends Optional {
        private final String propertyName;

        OptionalCapture(List<Pattern> optionalParts, String propertyName) {
            super(optionalParts);
            this.propertyName = propertyName;
        }

        String getPropertyName() {
            return propertyName;
        }

        @Override public String toString() {
            return "." + propertyName + "(" + Util.listToString(getPatterns()) + ")";
        }
    }

    static class ListPattern extends Pattern {
        private final Pattern item;
        private final List<Pattern> separator;
        private final boolean hasSeparator;

        ListPattern(Pattern item, List<Pattern> separator) {
            if (separator == null) separator = new ArrayList<>();

            this.item = item;
            this.separator = separator;

            hasSeparator = separator.size() > 0;
        }

        ListPattern(Pattern item, Pattern pattern) {
            this.item = item;
            this.separator = Collections.singletonList(pattern);

            hasSeparator = true;
        }

        ListPattern(Pattern pattern) {
            this.item = pattern;
            this.separator = new ArrayList<>();

            hasSeparator = false;
        }

        Pattern getItem() {
            return item;
        }

        List<Pattern> getSeparator() {
            return separator;
        }

        boolean hasSeparator() {
            return hasSeparator;
        }

        @Override Type getType() {
            return new Type.ListType(item.getType());
        }

        @Override public String toString() {
            return (hasSeparator ? "delim" : "list") + "(" + item.toString() + (hasSeparator ? ", " + Util.listToString(separator) : "") + ")";
        }
    }

    static class Terminal extends Pattern {
        private final String value;
        private final boolean isKeyword;

        Terminal(String value, boolean isKeyword) {
            this.value = value;
            this.isKeyword = isKeyword;
        }

        String getValue() {
            return value;
        }

        boolean isKeyword() {
            return isKeyword;
        }

        @Override Type getType() {
            return new Type.TokenType(isKeyword() ? TokenType.Keyword : TokenType.Symbol);
        }

        @Override public String toString() {
            return value;
        }
    }

    static class BuiltinPredicate extends Pattern {
        private final String predicateName;

        BuiltinPredicate(String predicateName) {
            this.predicateName = predicateName;
        }

        String getPredicateName() {
            return predicateName;
        }

        @Override Type getType() {
            return new Type.BooleanType(); // TODO: this should really have an EmptyType or something
        }

        @Override public String toString() {
            return "{" + predicateName + "}";
        }
    }

    static class TypeReference extends Pattern {
        private final Type reference;

        TypeReference(Type reference) {
            this.reference = reference;
        }

        Type getReference() {
            return reference;
        }

        @Override Type getType() {
            return reference;
        }

        @Override public String toString() {
            return "@" + reference.getName();
        }
    }

    static class Matcher extends Pattern {
        private final Pattern source;
        private final String targetProperty;

        Matcher(Pattern source, String targetProperty) {
            this.source = source;
            this.targetProperty = targetProperty;
        }

        Pattern getSource() {
            return source;
        }

        String getTargetProperty() {
            return targetProperty;
        }

        @Override Type getType() {
            return null;
        }

        @Override public String toString() {
            return "(" + source.toString() + " -> " + targetProperty + ")";
        }
    }

    static<T extends ASTifyGrammar.RootPattern> List<Pattern> createFromList(List<T> sourcePatterns, PropertyList properties, Grammar grammar) {
        List<Pattern> result = new ArrayList<>();

        for (ASTifyGrammar.RootPattern pattern : sourcePatterns) {
            result.add(createFrom(pattern, properties, grammar));
        }

        return result;
    }

    private static Pattern createFrom(ASTifyGrammar.RootPattern sourcePattern, PropertyList properties, Grammar grammar) {
        if (sourcePattern instanceof ASTifyGrammar.TypeReference) {
            Token sourceTypeName = ((ASTifyGrammar.TypeReference) sourcePattern).getType();

            if (grammar.getScope().isType(sourceTypeName.getValue())) {
                Type type = grammar.getScope().lookupType(sourceTypeName.getValue());

                if (type.isAbstract()) {
                    grammar.error("Cannot refer to abstract type '" + sourceTypeName.getValue() + "'", sourceTypeName.getPosition());
                }

                return new TypeReference(type);
            }
            else if (grammar.getScope().exists(sourceTypeName.getValue())) {
                if (grammar.getScope().lookup(sourceTypeName.getValue()) instanceof Definition.AliasDefinition) {
                    return new TypeReference(new Type.AliasType((Definition.AliasDefinition) grammar.getScope().lookup(sourceTypeName.getValue())));
                }
                else {
                    grammar.error("'" + sourceTypeName.getValue() + "' is not a type", sourceTypeName.getPosition());
                }
            }
            else {
                grammar.error("Cannot find type '" + sourceTypeName.getValue() + "'", sourceTypeName.getPosition());
            }
        }
        else if (sourcePattern instanceof ASTifyGrammar.Terminal) {
            String tokenValue = ((ASTifyGrammar.Terminal) sourcePattern).getTerminal().getValue();
            return new Terminal(tokenValue, !containsSymbol(tokenValue));
        }
        else if (sourcePattern instanceof ASTifyGrammar.Function) {
            Token functionNameToken = (((ASTifyGrammar.Function) sourcePattern).getName());
            List<Pattern> parameters = new ArrayList<>();
            Pattern parameter;
            boolean isNull = false;

            for (ASTifyGrammar.UncapturingPattern pat : ((ASTifyGrammar.Function) sourcePattern).getPatterns()) {
                parameters.add(parameter = createFrom(pat, properties, grammar));
                if (parameter == null) isNull = true;
            }

            switch (functionNameToken.getValue()) {
                case "list":
                    if (parameters.size() != 1) {
                        grammar.error("Incorrect number of parameters for `list(pat)` (" + parameters.size() + ")", sourcePattern.getPosition());
                    }

                    return isNull ? null : new ListPattern(parameters.get(0));

                case "delim":
                    if (parameters.size() != 2) {
                        grammar.error("Incorrect number of parameters for `delim(pat, sep)` (" + parameters.size() + ")", sourcePattern.getPosition());
                    }

                    return isNull ? null : new ListPattern(parameters.get(0), parameters.get(1));

                default:
                    grammar.error("Unknown function '" + functionNameToken.getValue() + "'", functionNameToken.getPosition());
            }
        }
        else if (sourcePattern instanceof ASTifyGrammar.BuiltinPredicate) {
            Token predicateName = ((ASTifyGrammar.BuiltinPredicate) sourcePattern).getPredicateName();

            Set<String> validNames = new HashSet<>();

            validNames.add("noSpace");
            validNames.add("sameLine");
            validNames.add("nextLine");

            if (validNames.contains(predicateName.getValue())) {
                return new BuiltinPredicate(predicateName.getValue());
            }
            else {
                grammar.error("No such predicate '" + predicateName.getValue() + "'", predicateName.getPosition());
            }
        }
        else if (sourcePattern instanceof ASTifyGrammar.Matcher) {
            Pattern resolvedSourcePattern = createFrom(((ASTifyGrammar.Matcher) sourcePattern).getSource(), properties, grammar);
            Token targetPropertyToken = ((ASTifyGrammar.Matcher) sourcePattern).getTargetProperty().getProperty();

            if (resolvedSourcePattern == null) return null;

            if (properties.exists(targetPropertyToken.getValue())) {
                Property targetProperty = properties.lookup(targetPropertyToken.getValue());

                if (!validateTypes(resolvedSourcePattern.getType(), targetProperty.getType(), grammar, sourcePattern.getPosition())) return null;

                return new Matcher(resolvedSourcePattern, targetPropertyToken.getValue());
            }
            else {
                grammar.error("No such property '" + targetPropertyToken.getValue() + "'", targetPropertyToken.getPosition());
            }
        }
        else if (sourcePattern instanceof ASTifyGrammar.PropertyReference) {
            Token propertyToken = ((ASTifyGrammar.PropertyReference) sourcePattern).getProperty();

            if (!properties.exists(propertyToken.getValue())) {
                grammar.error("Reference to undefined property '" + propertyToken.getValue() + "'", propertyToken.getPosition());
                return null;
            }

            List<Pattern> qualifier = createFromList(((ASTifyGrammar.PropertyReference) sourcePattern).getQualifier(), properties, grammar);
            Property property = properties.lookup(propertyToken.getValue());
            Type expectedType = property.getType();
            Pattern resultingPattern;

            if (expectedType instanceof Type.OptionalType) {
                expectedType = ((Type.OptionalType) expectedType).getType();
            }

            if (expectedType.isAbstract()) {
                grammar.error("Cannot refer to property with abstract type '" + expectedType.getName() + "'", sourcePattern.getPosition());
            }

            // if the targetProperty property is a list, we need a ListPattern(pat [, delim])
            if (expectedType instanceof Type.ListType) {
                Pattern listTypeRef = new TypeReference(((Type.ListType) expectedType).getType());
                Pattern list = new ListPattern(listTypeRef, qualifier.size() > 0 ? qualifier : null);
                resultingPattern = new Matcher(list, property.getName());
            }
            // if the targetProperty property is a boolean, its value is true if a match is found
            else if (expectedType instanceof Type.BooleanType) {
                // property will not be
                if (qualifier.size() == 0) {
                    grammar.error("Qualifier required for property with boolean type", sourcePattern.getPosition());
                }

                resultingPattern = new OptionalCapture(qualifier, property.getName());
            }
            else {
                if (qualifier.size() != 0) {
                    grammar.error("Qualifier invalid for property with type '" + expectedType.getName() + "'", sourcePattern.getPosition());
                }

                resultingPattern = new Matcher(new TypeReference(expectedType), property.getName());
            }

            return resultingPattern;
        }
        else if (sourcePattern instanceof ASTifyGrammar.Optional) {
            List<Pattern> patterns = createFromList(((ASTifyGrammar.Optional) sourcePattern).getPatterns(), properties, grammar);
            return new Optional(patterns);
        }
        return null;
    }

    private static boolean containsSymbol(String tokenValue) {
        for (int i = 1; i < tokenValue.length() - 1; ++i) {
            char c = tokenValue.charAt(i);
            if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && c != '_') {
                return true;
            }
        }

        return false;
    }

    private static boolean validateTypes(Type sourceType, Type targetType, Grammar grammar, Position position) {
        if (targetType instanceof Type.OptionalType) {
            targetType = ((Type.OptionalType) targetType).getType();
        }

        if (targetType instanceof Type.ListType && sourceType.castsTo(((Type.ListType) targetType).getType())) {
            return true;
        }

        if (!sourceType.castsTo(targetType)) {
            String aliasText = "";

            if (sourceType instanceof Type.AliasType) {
                if (((Type.AliasType) sourceType).getAlias().hasResult()) {
                    aliasText = " (aliased " + ((Type.AliasType) sourceType).getAlias().getResult().getType().getName() + ")";
                }
                else {
                    aliasText = " (no aliased type)";
                }
            }

            grammar.error(
                    "Incompatible types: cannot assign type " + sourceType.getName() + aliasText + " to type " + targetType.getName(),
                    position
            );
            return false;
        }

        return true;
    }
}
