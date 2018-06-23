package astify.GDL;

import java.util.ArrayList;
import java.util.List;

public abstract class Definition {
    private final String name;
    final ClassBuilder classBuilder;

    public Definition(String name) {
        this.name = name;
        this.classBuilder = new ClassBuilder(getStructName());
    }

    String getName() {
        return name;
    }

    ClassBuilder getClassBuilder() {
        return classBuilder;
    }

    String getStructName() {
        return NameHelper.toUpperCamelCase(name);
    }

    String getPatternName() {
        return NameHelper.toLowerLispCase(name);
    }

    static class TypeDefinition extends Definition {
        private final List<List<Pattern>> patternLists = new ArrayList<>();
        private final List<Property> properties = new ArrayList<>();

        TypeDefinition(String name) {
            super(name);
            classBuilder.addExtends("Capture.ObjectCapture");
            classBuilder.addConstructorField("Position", "spanningPosition");
        }

        List<Property> getProperties() {
            return properties;
        }

        Property getProperty(String name) {
            for (Property property : properties) {
                if (property.getPropertyName().equals(name)) {
                    return property;
                }
            }

            return null;
        }

        void addProperty(Property property) {
            classBuilder.addField(property.getTypeString(), property.getPropertyName(), property.isOptional());
            properties.add(property);
        }

        void addPattern(ASTifyGrammar.PatternList patternList, Scope scope) throws GDLException {
            patternLists.add(Pattern.createFromList(patternList.getPatterns(), this, scope));
        }

        List<List<Pattern>> getPatternLists() {
            return patternLists;
        }

        String getCallbackName() {
            return "create" + getStructName();
        }
    }

    static class UnionDefinition extends Definition {
        UnionDefinition(String name) {
            super(name);
            classBuilder.setClassType(ClassBuilder.ClassType.Interface);
            classBuilder.setFlag(ClassBuilder.ENABLE_CONSTRUCTOR, false);
            classBuilder.setFlag(ClassBuilder.ENABLE_METHODS, false);
            classBuilder.addExtends("astify.core.Positioned");
        }

        void addMember(Definition definition) {
            if (definition instanceof UnionDefinition) {
                definition.getClassBuilder().addExtends(getStructName());
            }
            else if (definition instanceof TypeDefinition) {
                definition.getClassBuilder().addImplements(getStructName());
            }
        }
    }
}
