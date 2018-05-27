package astify.grammar_definition;

public class Type {
    private final Definition definition;
    private final boolean optional, list;

    public Type(Definition definition, boolean optional, boolean list) {
        this.definition = definition;
        this.optional = optional;
        this.list = list;
    }

    public Definition getDefinition() {
        return definition;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isList() {
        return list;
    }

    public boolean isNative() {
        return definition instanceof Definition.NativeDefinition;
    }

    public boolean isNative(Definition.NativeDefinition.NativeType t) {
        return isNative() && ((Definition.NativeDefinition) definition).getType() == t;
    }

    @Override public String toString() {
        String str = definition.getName();

        if (definition instanceof Definition.TokenTypeDefinition) {
            str = "Token";
        }

        if (isList()) {
            str = "List<" + str + ">";
        }

        return str;
    }
}
