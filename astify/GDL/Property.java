package astify.GDL;

import static java.util.Objects.hash;

class Property {
    private final Type type;
    private final String propertyName;

    Property(Type type, String propertyName) {
        this.type = type;
        this.propertyName = propertyName;
    }

    Type getType() {
        return type;
    }

    String getName() {
        return propertyName;
    }

    Property rename(String name) {
        return new Property(type, name);
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof Property)) return false;
        Property otherCasted = (Property) other;
        return type == otherCasted.type && propertyName.equals(otherCasted.propertyName);
    }

    @Override public int hashCode() {
        return hash(type, propertyName);
    }
}
