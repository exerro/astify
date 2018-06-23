package astify.GDL;

import static java.util.Objects.hash;

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

    @Override public boolean equals(Object other) {
        if (!(other instanceof Property)) return false;
        Property otherCasted = (Property) other;
        return type == otherCasted.type && propertyName.equals(otherCasted.propertyName) && isList == otherCasted.isList && isOptional == otherCasted.isOptional;
    }

    @Override public int hashCode() {
        return hash(type, propertyName, isList, isOptional);
    }
}
