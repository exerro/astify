package astify.GDL;

class Property {
    private final Type type;
    private final String propertyName;
    private final boolean isList;
    private final boolean isOptional;

    Property(Type type, String propertyName, boolean isList, boolean isOptional) {
        this.type = type;
        this.propertyName = propertyName;
        this.isList = isList;
        this.isOptional = isOptional;
    }

    Type getType() {
        return type;
    }

    String getPropertyName() {
        return propertyName;
    }

    boolean isList() {
        return isList;
    }

    boolean isOptional() {
        return isOptional;
    }

    String getTypeString(String prefix) {
        return isList ? "List<" + prefix + type.getReferenceName() + ">" : prefix + type.getReferenceName();
    }

    String getTypeString() {
        return getTypeString("");
    }
}
