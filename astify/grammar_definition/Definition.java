package astify.grammar_definition;

import java.util.*;

public class Definition {
    private final String name;

    public Definition(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    String getStructName() {
        return NameHelper.toUpperCamelCase(getName());
    }

   String getPatternName() {
        return NameHelper.toLowerLispCase(getName());
    }

    static class Property {
        private final Type type;
        private final String name;
        private final String rawName;

        Property(Type type, String name) {
            this.type = type;
            this.name = NameHelper.toLowerCamelCase(name);
            this.rawName = name;
        }

        String getRawName() {
            return rawName;
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
        private final List<Property> properties = new ArrayList<>();
        private final Set<UnionDefinition> superTypes = new HashSet<>();
        private final List<ASTifyGrammar.PatternList> patternLists = new ArrayList<>();
        private final Map<String, Property> propertyLookup = new HashMap<>();

        TypeDefinition(String name) {
            super(name);
        }

        List<Property> getProperties() {
            return properties;
        }

        Set<UnionDefinition> getSuperTypes() {
            return superTypes;
        }

        List<ASTifyGrammar.PatternList> getPatternLists() {
            return patternLists;
        }

        void addProperty(Property property) {
            properties.add(property);
            propertyLookup.put(property.getRawName(), property);
        }

        Property getProperty(String name) {
            return propertyLookup.get(name);
        }

        void addSuperType(UnionDefinition union) {
            superTypes.add(union);
        }

        void addPatternList(ASTifyGrammar.PatternList list) {
            patternLists.add(list);
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
