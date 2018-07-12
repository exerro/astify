package astify.GDL;

import astify.core.Position;

import java.util.ArrayList;
import java.util.List;

public abstract class Definition {
    private final String name;
    private final Position definitionPosition;

    Definition(String name, Position definitionPosition) {
        this.name = name;
        this.definitionPosition = definitionPosition;
    }

    String getName() {
        return name;
    }

    Position getPosition() {
        return definitionPosition;
    }

    static class TypeDefinition extends Definition {
        private final Type type;

        TypeDefinition(Type type, Position definitionPosition) {
            super(type.getName(), definitionPosition);
            this.type = type;
        }

        Type getType() {
            return type;
        }
    }

    static class AliasDefinition extends Definition {
        private Property result;
        private List<List<Pattern>> patterns = new ArrayList<>();

        AliasDefinition(String name, Position definitionPosition) {
            super(name, definitionPosition);
        }

        List<List<Pattern>> getPatternLists() {
            return patterns;
        }

        Property getResult() {
            return result;
        }

        boolean hasResult() {
            return result != null;
        }

        void addPatternList(List<Pattern> pattern) {
            patterns.add(pattern);
        }

        void setResult(Type type, String name) {
            result = new Property(type, name);
        }
    }

    static class ExternDefinition extends Definition {
        private Type returnType;
        private final PropertyList parameters;

        ExternDefinition(String name, Position definitionPosition) {
            super(name, definitionPosition);
            parameters = new PropertyList();
        }

        Type getReturnType() {
            assert returnType != null;
            return returnType;
        }

        PropertyList getParameters() {
            return parameters;
        }

        void setReturnType(Type returnType) {
            this.returnType = returnType;
        }

        void addParameter(Property property) {
            parameters.add(property);
        }
    }
}
