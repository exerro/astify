package astify.grammar_definition;

import astify.token.TokenType;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Map<String, Type> scope = new HashMap<>();

    boolean exists(String name) {
        assert name != null;
        return scope.containsKey(name);
    }

    Type lookup(String name) {
        assert name != null;
        assert exists(name);
        return scope.get(name);
    }

    Definition lookupDefinition(String name) {
        assert name != null;
        assert exists(name);
        Type t = lookup(name);
        return t instanceof Type.DefinedType ? ((Type.DefinedType) t).getDefinition() : null;
    }

    void define(String name, Type value) {
        assert !exists(name);
        scope.put(name, value);
    }

    void define(Type value) {
        assert !exists(value.getName()) : value.getName();
        scope.put(value.getName(), value);
    }

    void defineNativeTypes() {
        define(new Type.BuiltinType(Type.BuiltinType.Types.Boolean));
        define(new Type.BuiltinType(Type.BuiltinType.Types.String));

        for (TokenType type : TokenType.values()) {
            define(new Type.TokenType(type));
        }
    }
}
