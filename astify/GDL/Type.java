package astify.GDL;

abstract class Type {
    abstract String getName();
    abstract String getReferenceName();

    static class DefinedType extends Type {
        private final Definition definition;

        DefinedType(Definition definition) {
            this.definition = definition;
        }

        Definition getDefinition() {
            return definition;
        }

        @Override String getName() {
            return definition.getName();
        }

        @Override String getReferenceName() {
            return definition.getStructName();
        }

        @Override public boolean equals(Object other) {
            return other instanceof DefinedType && definition == ((DefinedType) other).definition;
        }
    }

    static class TokenType extends Type {
        private final astify.token.TokenType tokenType;

        TokenType(astify.token.TokenType tokenType) {
            this.tokenType = tokenType;
        }

        astify.token.TokenType getTokenType() {
            return tokenType;
        }

        @Override String getName() {
            return tokenType.name();
        }

        @Override String getReferenceName() {
            return "Token";
        }

        @Override public boolean equals(Object other) {
            return other instanceof TokenType && tokenType == ((TokenType) other).tokenType;
        }
    }

    static class BooleanType extends Type {
        BooleanType() {
        }

        @Override String getName() {
            return "bool";
        }

        @Override String getReferenceName() {
            return "Boolean";
        }

        @Override public boolean equals(Object other) {
            return other instanceof BooleanType;
        }
    }
}
