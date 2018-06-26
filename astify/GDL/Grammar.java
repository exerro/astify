package astify.GDL;

import astify.core.Position;
import astify.core.Source;
import astify.token.Token;

import java.util.*;

class Grammar {
    private final Scope scope = new Scope();
    private final List<String> definedTypes = new ArrayList<>();
    private final String name, className;

    Grammar(String name) {
        this.name = name;
        this.className = NameHelper.toUpperCamelCase(name);

        scope.defineNativeTypes();
    }

    Scope getScope() {
        return scope;
    }

    List<String> getDefinedTypes() {
        return definedTypes;
    }

    String getName() {
        return name;
    }

    String getClassName() {
        return className;
    }

    void load(ASTifyGrammar grammar) throws GDLException {
        registerDefinitions(grammar.getDefinitions(), grammar.getGrammar());
        registerAllProperties(grammar.getDefinitions());
        registerAllUnionMembers(grammar.getDefinitions());
        bindAllPatternLists(grammar.getDefinitions());
    }

    private void registerDefinitions(List<ASTifyGrammar.Definition> definitions, ASTifyGrammar.Grammar grammar) throws GDLException {
        for (ASTifyGrammar.Definition definition : definitions) {
            registerDefinition(definition);
        }

        if (!scope.exists(name) || !(scope.lookupDefinition(name) instanceof Definition.TypeDefinition)) {
            throw new GDLException("Type '" + name + "' not defined", grammar.getPosition());
        }
    }

    private void registerDefinition(ASTifyGrammar.Definition definition) {
        Definition d;

        if (definition instanceof ASTifyGrammar.AbstractTypeDefinition) {
            String name = ((ASTifyGrammar.AbstractTypeDefinition) definition).getProperties().getName().getValue();
            d = new Definition.TypeDefinition(name, true);
        }
        else if (definition instanceof ASTifyGrammar.TypeDefinition) {
            String name = ((ASTifyGrammar.TypeDefinition) definition).getProperties().getName().getValue();
            d = new Definition.TypeDefinition(name, false);
        }
        else if (definition instanceof ASTifyGrammar.Union) {
            String name = ((ASTifyGrammar.Union) definition).getTypename().getValue();
            d = new Definition.UnionDefinition(name);
        }
        else {
            throw new Error("unknown class " + definition.getClass().getName());
        }

        scope.define(new Type.DefinedType(d));
        definedTypes.add(d.getName());
    }

    private void registerAllProperties(List<ASTifyGrammar.Definition> definitions) throws GDLException {
        for (ASTifyGrammar.Definition definition : definitions) {
            if (definition instanceof ASTifyGrammar.TypeDefinition) {
                registerProperties(((ASTifyGrammar.TypeDefinition) definition).getProperties());
            }
            else if (definition instanceof ASTifyGrammar.AbstractTypeDefinition) {
                registerProperties(((ASTifyGrammar.AbstractTypeDefinition) definition).getProperties());
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

    private void bindAllPatternLists(List<ASTifyGrammar.Definition> definitions) throws GDLException {
        for (ASTifyGrammar.Definition definition : definitions) {
            if (definition instanceof ASTifyGrammar.TypeDefinition) {
                ASTifyGrammar.TypeDefinition castedDefinition = (ASTifyGrammar.TypeDefinition) definition;
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

    private void registerAllUnionMembers(List<ASTifyGrammar.Definition> definitions) throws GDLException {
        for (ASTifyGrammar.Definition definition : definitions) {
            if (definition instanceof ASTifyGrammar.Union) {
                registerUnionMembers((ASTifyGrammar.Union) definition);
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
    }
}
