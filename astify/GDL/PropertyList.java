package astify.GDL;

import java.util.*;

public class PropertyList {
    private final List<Property> properties;

    PropertyList() {
        properties = new ArrayList<>();
    }

    public void add(Property property) {
        properties.add(property);
    }

    boolean exists(String name) {
        for (Property property : properties) {
            if (property.getPropertyName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    Property lookup(String name) {
        assert exists(name);


        for (Property property : properties) {
            if (property.getPropertyName().equals(name)) {
                return property;
            }
        }

        return null;
    }

    Iterator<Property> iterator() {
        return properties.iterator();
    }
}
