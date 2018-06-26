package astify.GDL;

import astify.core.Position;

import java.util.*;

public abstract class Definition {
    private final String name;
    private final Position definitionPosition;

    public Definition(String name, Position definitionPosition) {
        this.name = name;
        this.definitionPosition = definitionPosition;
    }

    String getName() {
        return name;
    }

    public Position getPosition() {
        return definitionPosition;
    }

    static class TypeDefinition extends Definition {
        private final Type type;

        public TypeDefinition(Type type, Position definitionPosition) {
            super(type.getName(), definitionPosition);
            this.type = type;
        }

        Type getType() {
            return type;
        }
    }

    static class AliasDefinition extends Definition {
        private Pattern pattern = null;

        AliasDefinition(String name, Position definitionPosition) {
            super(name, definitionPosition);
        }

        public Pattern getPattern() {
            return pattern;
        }

        public void setPattern(Pattern pattern) {
            assert this.pattern == null;
            this.pattern = pattern;
        }
    }

    static class ExternDefinition extends Definition {
        private final List<Type> parameterTypes = new ArrayList<>();
        private final List<String> parameterNames = new ArrayList<>();

        ExternDefinition(String name, Position definitionPosition) {
            super(name, definitionPosition);
        }

        void addParameter(Type type, String name) {
            parameterTypes.add(type);
            parameterNames.add(name);
        }

        Type getParameterType(int i) {
            return parameterTypes.get(i);
        }

        String getParameterName(int i) {
            return parameterNames.get(i);
        }

        Iterator<Integer> parameterIterator() {
            return new Iterator<Integer>() {
                int i = 0;

                Type getType() {
                    return getParameterType(i);
                }

                String getName() {
                    return getParameterName(i);
                }

                @Override public boolean hasNext() {
                    return i < parameterTypes.size();
                }

                @Override public Integer next() {
                    return i++;
                }
            };
        }
    }
}
