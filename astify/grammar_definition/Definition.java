package astify.grammar_definition;

import astify.grammar_definition.support.ClassBuilder;
import astify.grammar_definition.support.NameHelper;
import astify.grammar_definition.support.OutputHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class Definition {
    private final String name;
    protected final ClassBuilder classBuilder;

    public Definition(String name) {
        this.name = name;
        this.classBuilder = new ClassBuilder(getStructName());
    }

    public String getName() {
        return name;
    }

    public ClassBuilder getClassBuilder() {
        return classBuilder;
    }

    public String getStructName() {
        return NameHelper.toUpperCamelCase(name);
    }

    public String getPatternName() {
        return NameHelper.toLowerLispCase(name);
    }

    abstract void buildTo(OutputHelper helper);

    public static class TypeDefinition extends Definition {
        private final List<List<Pattern>> patternLists = new ArrayList<>();
        private final List<Property> properties = new ArrayList<>();

        public TypeDefinition(String name) {
            super(name);
            classBuilder.addExtends("Capture.ObjectCapture");
            classBuilder.addConstructorField("Position", "spanningPosition");
        }

        public List<Property> getProperties() {
            return properties;
        }

        public Property getProperty(String name) {
            for (Property property : properties) {
                if (property.getPropertyName().equals(name)) {
                    return property;
                }
            }

            return null;
        }

        public void addProperty(Property property) {
            classBuilder.addField(property.getTypeString(), property.getPropertyName(), property.isOptional());
            properties.add(property);
        }

        public void addPattern(ASTifyGrammar.PatternList patternList, Scope scope) {
            patternLists.add(Pattern.createFromList(patternList.getPatterns(), this, scope));
        }

        public List<List<Pattern>> getPatternLists() {
            return patternLists;
        }

        public String getCallbackName() {
            return "create" + getStructName();
        }

        @Override void buildTo(OutputHelper helper) {
            classBuilder.buildTo(helper);
        }
    }

    public static class UnionDefinition extends Definition {
        public UnionDefinition(String name) {
            super(name);
            classBuilder.setClassType(ClassBuilder.ClassType.Interface);
            classBuilder.setFlag(ClassBuilder.ENABLE_CONSTRUCTOR, false);
            classBuilder.setFlag(ClassBuilder.ENABLE_METHODS, false);
            classBuilder.addExtends("astify.core.Positioned");
        }

        public void addMember(Definition definition) {
            if (definition instanceof UnionDefinition) {
                definition.getClassBuilder().addExtends(getStructName());
            }
            else if (definition instanceof TypeDefinition) {
                definition.getClassBuilder().addImplements(getStructName());
            }
        }

        @Override void buildTo(OutputHelper helper) {
            classBuilder.buildTo(helper);
        }
    }
}
