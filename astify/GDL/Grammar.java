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

    void error(GDLException exception) {
        exceptions.add(exception);
    }

    void error(String message, Position position) {
        exceptions.add(new GDLException(message, position));
    }

    void load(ASTifyGrammar grammar) {
        if (!hasException()) registerDefinitions(grammar.getStatements(), grammar.getGrammar());
        /*registerAllProperties(grammar.getStatements());
        registerAllUnionMembers(grammar.getStatements());
        bindAllPatternLists(grammar.getStatements());*/

        if (!hasException()) registerPatternExtensions(grammar.getStatements());
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
            error(new GDLException.TaggedGDLException(message, name.getPosition(), "previously defined at", definitionPosition));
        }
        else {
            scope.define(resolvedDefinition);
        }
    }

    private void registerPatternExtensions(List<ASTifyGrammar.Statement> statements) {
        for (ASTifyGrammar.Statement statement : statements) {
            if (statement instanceof ASTifyGrammar.Extend) {
                registerPatternExtension((ASTifyGrammar.Extend) statement);
            }
        }
    }

    private void registerPatternExtension(ASTifyGrammar.Extend extension) {

    }

    /*
    private void registerAllProperties(List<ASTifyGrammar.Statement> statements) throws GDLException {
        for (ASTifyGrammar.Statement statement : statements) {
            if (statement instanceof ASTifyGrammar.TypeDefinition) {
                registerProperties(((ASTifyGrammar.TypeDefinition) statement).getProperties());
            }
            else if (statement instanceof ASTifyGrammar.AbstractTypeDefinition) {
                registerProperties(((ASTifyGrammar.AbstractTypeDefinition) statement).getProperties());
            }
        }
    }

    private void registerProperties(ASTifyGrammar.NamedPropertyList properties) throws GDLException {
        Definition.TypeDefinition definition = (Definition.TypeDefinition) scope.lookupDefinition(properties.getName().getValue());
        Set<String> definedProperties = new HashSet<>();

        for (ASTifyGrammar.TypedName typedName : properties.getProperties()) {
            String propertyName = typedName.getName().getValue();

            if (definedProperties.contains(typedName.getName().getValue())) {
                throw new GDLException("Redefinition of property '" + propertyName + "'", typedName.getName().getPosition());
            }
            else {
                definedProperties.add(propertyName);
            }

            if (scope.exists(typedName.getType().getName().getValue())) {
                Type t = scope.lookup(typedName.getType().getName().getValue());
                Property property = new Property(t, propertyName, typedName.getType().isLst(), typedName.getType().isOptional());
                definition.addProperty(property);
            }
            else {
                throw new GDLException("Cannot find type '" + typedName.getType().getName().getValue() + "'", typedName.getType().getName().getPosition());
            }
        }
    }

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
                requiredProperties.add(property.getPropertyName());
            }
        }

        for (Pattern p : patternList) {
            String propertyName = null;

            if (p instanceof Pattern.Matcher) {
                propertyName = ((Pattern.Matcher) p).getTargetProperty();
            }
            else if (p instanceof Pattern.OptionalCapture) {
                propertyName = ((Pattern.OptionalCapture) p).getPropertyName();
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

    private void registerAllUnionMembers(List<ASTifyGrammar.Statement> statements) throws GDLException {
        for (ASTifyGrammar.Statement statement : statements) {
            if (statement instanceof ASTifyGrammar.Union) {
                registerUnionMembers((ASTifyGrammar.Union) statement);
            }
        }
    }

    private void registerUnionMembers(ASTifyGrammar.Union source) throws GDLException {
        Definition.UnionDefinition union = (Definition.UnionDefinition) scope.lookupDefinition(source.getTypename().getValue());

        for (Token subtypeToken : source.getSubtypes()) {
            if (scope.exists(subtypeToken.getValue())) {
                Definition subtypeDefinition = scope.lookupDefinition(subtypeToken.getValue());

                if (subtypeDefinition == null) {
                    throw new GDLException("Referenced type '" + subtypeToken.getValue() + "' is not a type definition or union", subtypeToken.getPosition());
                }
                else {
                    union.addMember(subtypeDefinition);
                }
            }
            else {
                throw new GDLException("Undefined sub-type '" + subtypeToken.getValue() + "'", subtypeToken.getPosition());
            }
        }
    }*/
}
