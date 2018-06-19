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
    }

    static class BuiltinType extends Type {
        enum Types {
            String,
            Boolean
        }

        private final Types type;

        BuiltinType(Types type) {
            this.type = type;
        }

        Types getType() {
            return type;
        }

        @Override String getName() {
            switch (type) {
                case String: return "string";
                case Boolean: return "bool";
            }
            return "what";
        }

        @Override String getReferenceName() {
            return type.name();
        }
    }
}
