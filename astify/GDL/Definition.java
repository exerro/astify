package astify.GDL;

import java.util.*;

public abstract class Definition {
    private final String name;

    public Definition(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    String getStructName() {
        return NameHelper.toUpperCamelCase(name);
    }

    String getPatternName() {
        return NameHelper.toLowerLispCase(name);
    }

    abstract boolean castsTo(Definition other);

    abstract boolean isAbstract();

    static class TypeDefinition extends Definition {
        private final List<List<Pattern>> patternLists = new ArrayList<>();
        private final PropertyList properties = new PropertyList();
        private final boolean isAbstract;

        TypeDefinition(String name, boolean isAbstract) {
            super(name);
            this.isAbstract = isAbstract;
        }

        PropertyList getProperties() {
            return properties;
        }

        List<List<Pattern>> getPatternLists() {
            return patternLists;
        }

        void addProperty(Property property) {
            properties.add(property);
        }

        void addPattern(List<Pattern> patternList) {
            patternLists.add(patternList);
        }

        @Override boolean isAbstract() {
            return isAbstract;
        }

        @Override
        boolean castsTo(Definition other) {
            if (other instanceof UnionDefinition) {
                return ((UnionDefinition) other).getMembers().contains(this);
            }

            return other == this;
        }
    }

    static class UnionDefinition extends Definition {
        private final List<Definition> members = new ArrayList<>();

        UnionDefinition(String name) {
            super(name);
        }

        List<TypeDefinition> getMembers() {
            List<TypeDefinition> result = new ArrayList<>();

            for (Definition d : members) {
                if (d instanceof TypeDefinition) {
                    result.add((TypeDefinition) d);
                }
                else if (d instanceof UnionDefinition) {
                    result.addAll(((UnionDefinition) d).getMembers());
                }
            }

            return result;
        }

        List<Definition> getRawMembers() {
            return members;
        }

        void addMember(Definition definition) {
            members.add(definition);
        }

        // returns properties shared across all types that the union encompasses
        Set<Property> getSharedProperties() {
            Set<Property> properties = new HashSet<>();
            List<TypeDefinition> members = getMembers();

            if (members.isEmpty()) return properties;

            for (Iterator<Property> it = members.get(0).getProperties().iterator(); it.hasNext();)
                properties.add(it.next());

            for (TypeDefinition member : members) {
                PropertyList memberProperties = member.getProperties();

                for (Iterator<Property> it = properties.iterator(); it.hasNext(); ) {
                    Property property = it.next();

                    if (!memberProperties.exists(property.getPropertyName())) {
                        it.remove();
                        continue;
                    }

                    if (!property.equals(memberProperties.lookup(property.getPropertyName()))) {
                        it.remove();
                    }
                }

                if (properties.size() == 0) break;
            }

            return properties;
        }

        List<Definition> getParseMembers() {
            List<Definition> result = new ArrayList<>();

            for (TypeDefinition member : getMembers()) {
                if (!member.isAbstract()) {
                    result.add(member);
                }
            }

            return result;
        }

        @Override
        boolean castsTo(Definition other) {
            if (!(other instanceof UnionDefinition)) return false;

            List<UnionDefinition> queue = new ArrayList<>();

            queue.add((UnionDefinition) other);

            for (int i = 0; i < queue.size(); ++i) {
                UnionDefinition def = queue.get(i);

                for (Definition member : def.getRawMembers()) {
                    if (member == this) {
                        return true;
                    }

                    if (member instanceof UnionDefinition && !queue.contains(member)) {
                        queue.add((UnionDefinition) member);
                    }
                }
            }

            return false;
        }

        @Override boolean isAbstract() {
            return getParseMembers().isEmpty();
        }
    }
}
