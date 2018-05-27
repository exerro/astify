package astify.grammar_definition;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    Map<String, Definition> scope = new HashMap<>();

    boolean exists(String name) {
        return scope.containsKey(name);
    }

    Definition lookup(String name) {
        assert exists(name);
        return scope.get(name);
    }

    void define(String name, Definition value) {
        assert !exists(name);
        scope.put(name, value);
    }

    void define(Definition value) {
        assert !exists(value.getName());
        scope.put(value.getName(), value);
    }

    void defineNativeTypes() {
        define("string", new Definition.NativeDefinition(Definition.NativeDefinition.NativeType.String));
        define("bool", new Definition.NativeDefinition(Definition.NativeDefinition.NativeType.Boolean));
    }
}
