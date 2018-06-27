package astify.GDL;

import astify.core.Position;
import astify.token.Token;

import java.util.*;

class Grammar {
    static class PatternExtension {
        private final List<Pattern> patternList;
        private final Definition.ExternDefinition definition;

        PatternExtension(List<Pattern> patternList, Definition.ExternDefinition definition) {
            this.patternList = patternList;
            this.definition = definition;
        }

        List<Pattern> getPatternList() {
            return patternList;
        }

        Definition.ExternDefinition getDefinition() {
            return definition;
        }
    }

    private final Scope scope = new Scope();
    private final String name, className;

    private final List<PatternExtension> patternExtensions = new ArrayList<>();
    private final List<GDLException> exceptions = new ArrayList<>();

    Grammar(String name) {
        this.name = name;
        this.className = NameHelper.toUpperCamelCase(name);

        scope.defineNativeTypes();
    }

    Scope getScope() {
        return scope;
    }

    String getName() {
        return name;
    }

    String getClassName() {
        return className;
    }

    List<GDLException> getExceptions() {
        return exceptions;
    }

    boolean hasException() {
        return !exceptions.isEmpty();
    }

    private void error(String message, Position position) {
        exceptions.add(new GDLException(message, position));
    }

    private void error(String message, Position position, String tag, Position tagPosition) {
        exceptions.add(new GDLException.TaggedGDLException(message, position, tag, tagPosition));
    }

    void load(ASTifyGrammar grammar) {
        registerDefinitions(grammar.getStatements(), grammar.getGrammar());

        if (!hasException()) {
            registerAllProperties(grammar.getStatements());
            registerAllUnionMembers(grammar.getStatements());
        }

        if (!hasException()) {
            bindAllPatternLists(grammar.getStatements());
            registerPatternExtensions(grammar.getStatements());
        }
    }

    private void registerDefinitions(List<ASTifyGrammar.Statement> statements, ASTifyGrammar.Grammar grammar) {
        for (ASTifyGrammar.Statement statement : statements) {
            if (statement instanceof ASTifyGrammar.Definition) {
                registerDefinition((ASTifyGrammar.Definition) statement);
            }
        }

        if (!scope.isType(name) || !(scope.lookupType(name) instanceof Type.ObjectType)) {
            error("Type '" + name + "' not defined to match grammar name", grammar.getPosition());
        }
    }

    private void registerDefinition(ASTifyGrammar.Definition definition) {
        Definition resolvedDefinition = null;
        Token name = null;

        if (definition instanceof ASTifyGrammar.AbstractTypeDefinition) {
            ASTifyGrammar.AbstractTypeDefinition def = (ASTifyGrammar.AbstractTypeDefinition) definition;

            name = def.getProperties().getName();
            resolvedDefinition = new Definition.TypeDefinition(new Type.ObjectType(name.getValue(), true), name.getPosition());
        }
        else if (definition instanceof ASTifyGrammar.TypeDefinition) {
            ASTifyGrammar.TypeDefinition def = (ASTifyGrammar.TypeDefinition) definition;

            name = def.getProperties().getName();
            resolvedDefinition = new Definition.TypeDefinition(new Type.ObjectType(name.getValue(), true), name.getPosition());
        }
        else if (definition instanceof ASTifyGrammar.Union) {
            ASTifyGrammar.Union def = (ASTifyGrammar.Union) definition;

            name = def.getTypename();
            resolvedDefinition = new Definition.TypeDefinition(new Type.Union(name.getValue()), name.getPosition());
        }
        else if (definition instanceof ASTifyGrammar.AliasDefinition) {
            ASTifyGrammar.AliasDefinition def = (ASTifyGrammar.AliasDefinition) definition;

            name = def.getName();
            resolvedDefinition = new Definition.AliasDefinition(name.getValue(), name.getPosition());
        }
        else if (definition instanceof ASTifyGrammar.ExternDefinition) {
            ASTifyGrammar.ExternDefinition def = (ASTifyGrammar.ExternDefinition) definition;

            name = def.getName();
            resolvedDefinition = new Definition.ExternDefinition(name.getValue(), name.getPosition());
        }
        else {
            assert false : definition.getClass().getName();
        }

        if (scope.exists(resolvedDefinition.getName())) {
            String message = "'" + resolvedDefinition.getName() + "' is already defined";
            Position definitionPosition = scope.lookup(resolvedDefinition.getName()).getPosition();
            error(message, name.getPosition(), "previously defined at", definitionPosition);
        }
        else {
            scope.define(resolvedDefinition);
        }
    }

    private void registerAllProperties(List<ASTifyGrammar.Statement> statements) {
        for (ASTifyGrammar.Statement statement : statements) {
            if (statement instanceof ASTifyGrammar.TypeDefinition) {
                registerProperties(((ASTifyGrammar.TypeDefinition) statement).getProperties());
            }
            else if (statement instanceof ASTifyGrammar.AbstractTypeDefinition) {
                registerProperties(((ASTifyGrammar.AbstractTypeDefinition) statement).getProperties());
            }
            else if (statement instanceof ASTifyGrammar.ExternDefinition) {
                registerParameters((ASTifyGrammar.ExternDefinition) statement);
            }
        }
    }

    private void registerProperties(ASTifyGrammar.NamedPropertyList properties) {
        Type.ObjectType object = (Type.ObjectType) scope.lookupType(properties.getName().getValue());
        Set<Util.Pair<String, Position>> definedProperties = new HashSet<>();
        Type propertyType;

        for (ASTifyGrammar.TypedName typedName : properties.getProperties()) {
            String propertyName = typedName.getName().getValue();
            boolean wasFound = false;

            for (Util.Pair<String, Position> pair : definedProperties) {
                if (pair.a.equals(propertyName)) {
                    error(
                            "Redefinition of property '" + propertyName + "'",
                            typedName.getPosition(),
                            "previously defined at",
                            pair.b
                    );
                    wasFound = true;
                    break;
                }
            }

            if (!wasFound) {
                definedProperties.add(new Util.Pair<>(typedName.getName().getValue(), typedName.getPosition()));
            }

            if ((propertyType = resolveType(typedName.getType())) != null) {
                object.addProperty(new Property(propertyType, typedName.getName().getValue()));
            }
        }
    }

    private void registerParameters(ASTifyGrammar.ExternDefinition definition) {
        Definition.ExternDefinition def = (Definition.ExternDefinition) scope.lookup(definition.getName().getValue());
        Set<Util.Pair<String, Position>> parameters = new HashSet<>();
        Type resolvedType;

        for (ASTifyGrammar.TypedName parameter : definition.getParameters()) {
            boolean wasFound = false;

            for (Util.Pair<String, Position> param : parameters) {
                if (param.a.equals(parameter.getName().getValue())) {
                    error(
                            "Redefinition of parameter '" + param.a + "'",
                            parameter.getPosition(),
                            "previously defined at",
                            param.b
                    );
                    wasFound = true;
                    break;
                }
            }

            if (!wasFound) {
                parameters.add(new Util.Pair<>(parameter.getName().getValue(), parameter.getPosition()));
            }

            if ((resolvedType = resolveType(parameter.getType())) != null) {
                def.addParameter(resolvedType, parameter.getName().getValue());
            }
        }

        if ((resolvedType = resolveType(definition.getReturnType())) != null) {
            def.setReturnType(resolvedType);
        }
    }

    private Type resolveType(ASTifyGrammar.Type type) {
        if (scope.isType(type.getName().getValue())) {
            Type resolvedType = scope.lookupType(type.getName().getValue());

            if (type.isOptional()) {
                resolvedType = new Type.OptionalType(resolvedType);
            }

            if (type.isLst()) {
                resolvedType = new Type.ListType(resolvedType);
            }

            return resolvedType;
        }
        else if (scope.exists(type.getName().getValue())) {
            error(
                    "'" + type.getName().getValue() + "' is not a type",
                    type.getName().getPosition(),
                    "defined at",
                    scope.lookup(type.getName().getValue()).getPosition()
            );
        }
        else {
            error("Cannot find type '" + type.getName().getValue() + "'", type.getName().getPosition());
        }

        return null;
    }

    private void registerAllUnionMembers(List<ASTifyGrammar.Statement> statements) {
        for (ASTifyGrammar.Statement statement : statements) {
            if (statement instanceof ASTifyGrammar.Union) {
                registerUnionMembers((ASTifyGrammar.Union) statement);
            }
        }
    }

    private void registerUnionMembers(ASTifyGrammar.Union statement) {
        Type.Union union = (Type.Union) scope.lookupType(statement.getTypename().getValue());

        for (Token subtypeToken : statement.getSubtypes()) {
            if (scope.isType(subtypeToken.getValue())) {
                union.addMember(scope.lookupType(subtypeToken.getValue()));
            }
            else if (scope.exists(subtypeToken.getValue())) {
                error(
                        "'" + subtypeToken.getValue() + "' is not a type",
                        subtypeToken.getPosition(),
                        "defined at",
                        scope.lookup(subtypeToken.getValue()).getPosition()
                );
            }
            else {
                error("Cannot find type '" + subtypeToken.getValue() + "'", subtypeToken.getPosition());
            }
        }
    }

    private void bindAllPatternLists(List<ASTifyGrammar.Statement> statements) {

    }

    private void registerPatternExtensions(List<ASTifyGrammar.Statement> statements) {
        for (ASTifyGrammar.Statement statement : statements) {
            if (statement instanceof ASTifyGrammar.Extend) {
                registerPatternExtension((ASTifyGrammar.Extend) statement);
            }
        }
    }

    private void registerPatternExtension(ASTifyGrammar.Extend extension) {
        // do stuff with patternExtensions
    }

    /*
    private void bindAllPatternLists(List<ASTifyGrammar.Statement> statements) throws GDLException {
        for (ASTifyGrammar.Statement statement: statements) {
            if (statement instanceof ASTifyGrammar.TypeDefinition) {
                ASTifyGrammar.TypeDefinition castedDefinition = (ASTifyGrammar.TypeDefinition) statement;
                Definition.TypeDefinition resolvedDefinition = (Definition.TypeDefinition) scope.lookupDefinition(castedDefinition.getProperties().getName().getValue());

                bindPatternLists(castedDefinition, resolvedDefinition);
            }
        }
    }

    private void bindPatternLists(ASTifyGrammar.TypeDefinition source, Definition.TypeDefinition definition) throws GDLException {
        for (ASTifyGrammar.PatternList pattern : source.getPatterns()) {
            bindPatternList(pattern.getPatterns(), definition, pattern.getPosition());
        }
    }

    private void bindPatternList(List<ASTifyGrammar.RootPattern> patternList, Definition.TypeDefinition definition, astify.core.Position position) throws GDLException {
        List<Pattern> resolvedPatternList = Pattern.createFromList(patternList, definition.getProperties(), scope);
        validatePatternList(resolvedPatternList, definition.getProperties(), position);
        definition.addPattern(resolvedPatternList);
    }

    private void validatePatternList(List<Pattern> patternList, PropertyList properties, astify.core.Position position) throws GDLException {
        Set<String> requiredProperties = new HashSet<>();
        Set<String> setProperties = new HashSet<>();

        for (Iterator<Property> it = properties.iterator(); it.hasNext(); ) {
            Property property = it.next();

            if (!property.isOptional() && !property.isList()) {
                requiredProperties.add(property.getName());
            }
        }

        for (Pattern p : patternList) {
            String propertyName = null;

            if (p instanceof Pattern.Matcher) {
                propertyName = ((Pattern.Matcher) p).getTargetProperty();
            }
            else if (p instanceof Pattern.OptionalCapture) {
                propertyName = ((Pattern.OptionalCapture) p).getName();
            }

            if (propertyName != null) {
                Property property = properties.lookup(propertyName);

                if (setProperties.contains(propertyName)) {
                    throw new GDLException("Multiple assignments of property '" + propertyName + "'", position);
                }
                else if (!property.isList()) {
                    setProperties.add(propertyName);
                }

                requiredProperties.remove(propertyName);
            }
        }

        if (requiredProperties.size() > 0) {
            throw new GDLException("Unassigned " + (requiredProperties.size() == 1 ? "property" : "properties") + ": " + Util.setToStringQuoted(requiredProperties), position);
        }
    }

  */
}
