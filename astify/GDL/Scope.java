package astify.GDL;

import astify.token.TokenType;

import java.util.HashMap;
import java.util.Map;

class Scope {
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
        define(value.getName(), value);
    }

    void defineNativeTypes() {
        define(new Type.BooleanType());

        for (TokenType type : TokenType.values()) {
            define(new Type.TokenType(type));
        }
    }
}