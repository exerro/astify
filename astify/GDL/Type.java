package astify.GDL;

import java.util.*;

abstract class Type {
    protected final String name;

    protected Type(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    String getReferenceName() {
        return NameHelper.toUpperCamelCase(name);
    }

    abstract boolean isAbstract();

    abstract boolean castsTo(Type type);





    static class ObjectType extends Type {
        private final List<List<Pattern>> patternLists = new ArrayList<>();
        private final PropertyList properties = new PropertyList();
        private final boolean isAbstract;

        ObjectType(String name, boolean isAbstract) {
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
        boolean castsTo(Type other) {
            if (other instanceof Union) {
                return ((Union) other).getMembers().contains(this);
            }

            return other == this;
        }
    }


    static class Union extends Type {
        private final List<Type> members = new ArrayList<>();

        Union(String name) {
            super(name);
        }

        List<ObjectType> getMembers() {
            List<ObjectType> result = new ArrayList<>();

            for (Type d : members) {
                if (d instanceof ObjectType) {
                    result.add((ObjectType) d);
                }
                else if (d instanceof Union) {
                    result.addAll(((Union) d).getMembers());
                }
            }

            return result;
        }

        List<Type> getRawMembers() {
            return members;
        }

        void addMember(Type type) {
            members.add(type);
        }

        // returns properties shared across all types that the union encompasses
        Set<Property> getSharedProperties() {
            Set<Property> properties = new HashSet<>();
            List<ObjectType> members = getMembers();

            if (members.isEmpty()) return properties;

            for (Iterator<Property> it = members.get(0).getProperties().iterator(); it.hasNext();)
                properties.add(it.next());

            for (ObjectType member : members) {
                PropertyList memberProperties = member.getProperties();

                for (Iterator<Property> it = properties.iterator(); it.hasNext(); ) {
                    Property property = it.next();

                    if (!memberProperties.exists(property.getName())) {
                        it.remove();
                        continue;
                    }

                    if (!property.equals(memberProperties.lookup(property.getName()))) {
                        it.remove();
                    }
                }

                if (properties.size() == 0) break;
            }

            return properties;
        }

        List<Type> getNonAbstractMembers() {
            List<Type> result = new ArrayList<>();

            for (ObjectType member : getMembers()) {
                if (!member.isAbstract()) {
                    result.add(member);
                }
            }

            return result;
        }

        @Override boolean isAbstract() {
            return getNonAbstractMembers().isEmpty();
        }

        @Override boolean castsTo(Type other) {
            if (!(other instanceof Union)) return false;

            List<Union> queue = new ArrayList<>();

            queue.add((Union) other);

            for (int i = 0; i < queue.size(); ++i) {
                Union t = queue.get(i);

                if (t == this) {
                    return true;
                }

                for (Type member : t.getRawMembers()) {
                    if (member instanceof Union && !queue.contains(member)) {
                        queue.add((Union) member);
                    }
                }
            }

            return false;
        }
    }

    static class TokenType extends Type {
        private final astify.token.TokenType tokenType;

        TokenType(astify.token.TokenType tokenType) {
            super(tokenType.name());
            this.tokenType = tokenType;
        }

        astify.token.TokenType getTokenType() {
            return tokenType;
        }

        @Override boolean isAbstract() {
            return false;
        }

        @Override boolean castsTo(Type type) {
            return type instanceof TokenType && tokenType == ((TokenType) type).getTokenType();
        }
    }

    static class BooleanType extends Type {
        BooleanType() {
            super("bool");
        }

        @Override String getReferenceName() {
            return "Boolean";
        }

        @Override boolean isAbstract() {
            return false;
        }

        @Override boolean castsTo(Type type) {
            return type instanceof BooleanType;
        }
    }

    static class ListType extends Type {
        private final Type type;

        ListType(Type type) {
            super(type.getName() + "[]");
            this.type = type;
        }

        Type getType() {
            return type;
        }

        @Override boolean isAbstract() {
            return type.isAbstract();
        }

        @Override boolean castsTo(Type type) {
            return type instanceof ListType && this.type.castsTo(((ListType) type).type);
        }
    }

    static class OptionalType extends Type {
        private final Type type;

        OptionalType(Type type) {
            super(type.getName() + "?");
            this.type = type;
        }

        Type getType() {
            return type;
        }

        @Override boolean isAbstract() {
            return type.isAbstract();
        }

        @Override boolean castsTo(Type type) {
            return type instanceof OptionalType && this.type.castsTo(((OptionalType) type).type);
        }
    }
}
