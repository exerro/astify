package astify.grammar_definition;

import java.util.HashSet;
import java.util.Set;

public class Definition {
    private final String name;

    public Definition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getStructName() {
        return NameHelper.toUpperCamelCase(getName());
    }

    static class Property {
        private final Type type;
        private final String name;

        Property(Type type, String name) {
            this.type = type;
            this.name = NameHelper.toLowerCamelCase(name);
        }

        String getName() {
            return name;
        }

        Type getType() {
            return type;
        }

        String getParameterString() {
            return type.toString() + " " + name;
        }

        String getDefinitionString() {
            return "private final " + getParameterString()+ ";";
        }

        String getGetterString() {
            Definition typeDefinition = type.getDefinition();
            boolean isBoolean = type.isNative(NativeDefinition.NativeType.Boolean);

            return "public " + type.toString() + " " + (isBoolean ? NameHelper.getBooleanGetterName(name) : NameHelper.getGetterName(name)) + "()";
        }
    }

    static class TypeDefinition extends Definition {
        private final Set<Property> properties = new HashSet<>();
        private final Set<UnionDefinition> superTypes = new HashSet<>();

        TypeDefinition(String name) {
            super(name);
        }

        Set<Property> getProperties() {
            return properties;
        }

        Set<UnionDefinition> getSuperTypes() {
            return superTypes;
        }

        void addProperty(Property property) {
            properties.add(property);
        }

        void addSuperType(UnionDefinition union) {
            superTypes.add(union);
        }
    }

    static class NativeDefinition extends Definition {
        private final Definition.NativeDefinition.NativeType type;

        enum NativeType {
            String,
            Boolean
        }

        NativeDefinition(NativeType type) {
            super(type.toString());
            this.type = type;
        }

        NativeType getType() {
            return type;
        }

        String getTypeString() {
            switch (type) {
                case String: return "String";
                case Boolean: return "Boolean";
            }
            return "";
        }
    }

    static class UnionDefinition extends Definition {
        private final Set<TypeDefinition> subtypes = new HashSet<>();

        UnionDefinition(String name) {
            super(name);
        }

        Set<TypeDefinition> getSubtypes() {
            return subtypes;
        }

        void addSubType(TypeDefinition definition) {
            subtypes.add(definition);
        }
    }
}
