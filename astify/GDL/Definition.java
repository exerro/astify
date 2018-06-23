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

    static class TypeDefinition extends Definition {
        private final List<List<Pattern>> patternLists = new ArrayList<>();
        private final PropertyList properties = new PropertyList();

        TypeDefinition(String name) {
            super(name);
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

        void addPattern(ASTifyGrammar.PatternList patternList, Scope scope) throws GDLException {
            patternLists.add(Pattern.createFromList(patternList.getPatterns(), properties, scope));
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
    }
}
