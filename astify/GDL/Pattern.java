package astify.GDL;

import astify.token.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class Pattern {
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

        @Override public String toString() {
            return value;
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

        @Override public String toString() {
            return "(" + source.toString() + " -> " + targetProperty + ")";
        }
    }

    static<T extends ASTifyGrammar.RootPattern> List<Pattern> createFromList(List<T> sourcePatterns, PropertyList properties, Scope scope) throws GDLException {
        List<Pattern> result = new ArrayList<>();

        for (ASTifyGrammar.RootPattern pattern : sourcePatterns) {
            result.add(createFrom(pattern, properties, scope));
        }

        return result;
    }

    private static Pattern createFrom(ASTifyGrammar.RootPattern sourcePattern, PropertyList properties, Scope scope) throws GDLException {
        if (sourcePattern instanceof ASTifyGrammar.TypeReference) {
            Token sourceTypeName = ((ASTifyGrammar.TypeReference) sourcePattern).getType();

            if (scope.exists(sourceTypeName.getValue())) {
                return new TypeReference(scope.lookup(sourceTypeName.getValue()));
            }
            else {
                throw new GDLException("Cannot find type '" + sourceTypeName.getValue() + "'", sourceTypeName.getPosition());
            }
        }

        else if (sourcePattern instanceof ASTifyGrammar.Terminal) {
            boolean isKeyword = true;
            String tokenValue = ((ASTifyGrammar.Terminal) sourcePattern).getTerminal().getValue();

            for (int i = 1; i < tokenValue.length() - 1; ++i) {
                char c = tokenValue.charAt(i);
                if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && c != '_') {
                    isKeyword = false;
                    break;
                }
            }

            return new Terminal(tokenValue, isKeyword);
        }

        else if (sourcePattern instanceof ASTifyGrammar.Function) {
            Token functionNameToken = (((ASTifyGrammar.Function) sourcePattern).getName());
            List<Pattern> parameters = new ArrayList<>();

            for (ASTifyGrammar.UncapturingPattern pat : ((ASTifyGrammar.Function) sourcePattern).getPatterns()) {
                parameters.add(createFrom(pat, properties, scope));
            }

            switch (functionNameToken.getValue()) {
                case "list":
                    if (parameters.size() != 1) {
                        throw new GDLException("Incorrect number of parameters for `list(pat)` (" + parameters.size() + ")", sourcePattern.getPosition());
                    }

                    return new ListPattern(parameters.get(0));

                case "delim":
                    if (parameters.size() != 2) {
                        throw new GDLException("Incorrect number of parameters for `delim(pat, sep)` (" + parameters.size() + ")", sourcePattern.getPosition());
                    }

                    return new ListPattern(parameters.get(0), parameters.get(1));

                default:
                    throw new GDLException("Unknown function '" + functionNameToken.getValue() + "'", functionNameToken.getPosition());
            }
        }

        else if (sourcePattern instanceof ASTifyGrammar.Matcher) {
            Pattern resolvedSourcePattern = createFrom(((ASTifyGrammar.Matcher) sourcePattern).getSource(), properties, scope);
            Token targetPropertyToken = ((ASTifyGrammar.Matcher) sourcePattern).getTargetProperty().getProperty();

            if (properties.exists(targetPropertyToken.getValue())) {
                // TODO: validate types
                return new Matcher(resolvedSourcePattern, targetPropertyToken.getValue());
            }
            else {
                throw new GDLException("Undefined property '" + targetPropertyToken.getValue() + "'", targetPropertyToken.getPosition());
            }
        }

        else if (sourcePattern instanceof ASTifyGrammar.PropertyReference) {
            Token propertyToken = ((ASTifyGrammar.PropertyReference) sourcePattern).getProperty();

            if (!properties.exists(propertyToken.getValue())) {
                throw new GDLException("Reference to undefined property '" + propertyToken.getValue() + "'", propertyToken.getPosition());
            }

            List<Pattern> qualifier = createFromList(((ASTifyGrammar.PropertyReference) sourcePattern).getQualifier(), properties, scope);
            Property property = properties.lookup(propertyToken.getValue());
            Type expectedType = property.getType();
            Pattern resultingPattern;

            // if the targetProperty property is a list, we need a ListPattern(pat [, delim])
            if (property.isList()) {
                resultingPattern = new Matcher(new ListPattern(new TypeReference(expectedType), qualifier.size() > 0 ? qualifier : null), property.getPropertyName());
            }
            // if the targetProperty property is a boolean, its value is true if a match is found
            else if (expectedType instanceof Type.BooleanType) {
                // property will not be optional
                resultingPattern = new OptionalCapture(qualifier, property.getPropertyName());
            }
            // otherwise, simply refer a type ref
            else {
                resultingPattern = new Matcher(new TypeReference(expectedType), property.getPropertyName());
            }

            return resultingPattern;
        }

        else if (sourcePattern instanceof ASTifyGrammar.Optional) {
            List<Pattern> patterns = createFromList(((ASTifyGrammar.Optional) sourcePattern).getPatterns(), properties, scope);
            return new Optional(patterns);
        }

        return null;
    }

}
