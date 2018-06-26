package astify.GDL;

import astify.core.Position;
import astify.core.Source;
import astify.token.TokenType;

import java.util.*;

class Scope {
    private final Map<String, Definition> scope = new HashMap<>();

    boolean exists(String name) {
        assert name != null;
        return scope.containsKey(name);
    }

    boolean isType(String name) {
        assert name != null;
        return exists(name) && lookup(name) instanceof Definition.TypeDefinition;
    }

    Definition lookup(String name) {
        assert name != null;
        assert exists(name);
        return scope.get(name);
    }

    Type lookupType(String name) {
        assert name != null;
        assert isType(name);
        return ((Definition.TypeDefinition) lookup(name)).getType();
    }

    void define(Definition value) {
        assert value != null;
        assert !exists(value.getName()) : value.getName();
        scope.put(value.getName(), value);
    }

    void define(Type type, Position position) {
        define(new Definition.TypeDefinition(type, position));
    }

    void defineNativeTypes() {
        StringBuilder sourceContent = new StringBuilder("bool()");
        List<TokenType> tokenTypes = new ArrayList<>();

        for (TokenType type : TokenType.values()) {
            sourceContent.append("\n");
            sourceContent.append(type.name());
            sourceContent.append("()");

            tokenTypes.add(type);
        }

        Source source = new Source.VirtualSource("native", sourceContent.toString());

        define(new Type.BooleanType(), new Position(source, 1, 1, 4));

        for (int i = 0; i < tokenTypes.size(); ++i) {
            define(new Type.TokenType(tokenTypes.get(i)), new Position(source, i + 2, 1, tokenTypes.get(i).name().length()));
        }
    }

    Collection<Definition> values() {
        return scope.values();
    }
}
