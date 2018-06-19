package astify.grammar_definition;

public abstract class Type {
    abstract String getName();
    abstract String getReferenceName();

    public static class DefinedType extends Type {
        private final Definition definition;

        public DefinedType(Definition definition) {
            this.definition = definition;
        }

        public Definition getDefinition() {
            return definition;
        }

        @Override String getName() {
            return definition.getName();
        }

        @Override String getReferenceName() {
            return definition.getStructName();
        }
    }

    public static class TokenType extends Type {
        private final astify.token.TokenType tokenType;

        public TokenType(astify.token.TokenType tokenType) {
            this.tokenType = tokenType;
        }

        public astify.token.TokenType getTokenType() {
            return tokenType;
        }

        @Override String getName() {
            return tokenType.name();
        }

        @Override String getReferenceName() {
            return "Token";
        }
    }

    public static class BuiltinType extends Type {
        public enum Types {
            String,
            Boolean
        }

        private final Types type;

        public BuiltinType(Types type) {
            this.type = type;
        }

        public Types getType() {
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
