package astify.grammar_definition;

public class Property {
    private final Type type;
    private final String propertyName;
    private final boolean isList;
    private final boolean isOptional;

    public Property(Type type, String propertyName, boolean isList, boolean isOptional) {
        this.type = type;
        this.propertyName = propertyName;
        this.isList = isList;
        this.isOptional = isOptional;
    }

    public Type getType() {
        return type;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isList() {
        return isList;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public String getTypeString(String prefix) {
        return isList ? "List<" + prefix + type.getReferenceName() + ">" : prefix + type.getReferenceName();
    }

    public String getTypeString() {
        return getTypeString("");
    }
}
