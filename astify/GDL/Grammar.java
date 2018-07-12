package astify.GDL;

import astify.core.Position;
import astify.token.Token;

import java.util.*;

class Grammar {
    // TODO: tidy code and add validation to ObjectType patternLists (count)
    private final Scope scope = new Scope();
    private final String name, className;

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

    void error(String message, Position position) {
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
            bindAllApplications(grammar.getStatements());
            bindAllPatternLists(grammar.getStatements());
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    // registers all the definitions, who knew
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
            resolvedDefinition = new Definition.TypeDefinition(new Type.ObjectType(name.getValue(), false), name.getPosition());
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    // registers properties for types, aliases, and externs
    private void registerAllProperties(List<ASTifyGrammar.Statement> statements) {
        for (ASTifyGrammar.Statement statement : statements) {
            if (statement instanceof ASTifyGrammar.TypeDefinition) {
                registerProperties(((ASTifyGrammar.TypeDefinition) statement).getProperties());
            }
            else if (statement instanceof ASTifyGrammar.AbstractTypeDefinition) {
                registerProperties(((ASTifyGrammar.AbstractTypeDefinition) statement).getProperties());
            }
            else if (statement instanceof ASTifyGrammar.AliasDefinition) {
                if (((ASTifyGrammar.AliasDefinition) statement).getProperty() != null) {
                    registerProperty((ASTifyGrammar.AliasDefinition) statement);
                }
            }
            else if (statement instanceof ASTifyGrammar.ExternDefinition) {
                registerParameters((ASTifyGrammar.ExternDefinition) statement);
            }
        }
    }

    private void registerProperties(ASTifyGrammar.NamedPropertyList properties) {
        Type.ObjectType object = (Type.ObjectType) scope.lookupType(properties.getName().getValue());

        for (Iterator<Property> it = resolvePropertyList(properties.getProperties(), "property").iterator(); it.hasNext(); ) {
            object.addProperty(it.next());
        }
    }

    private void registerProperty(ASTifyGrammar.AliasDefinition statement) {
        ASTifyGrammar.TypedName typedName = statement.getProperty();
        Definition.AliasDefinition definition = (Definition.AliasDefinition) scope.lookup(statement.getName().getValue());
        Type propertyType;

        if ((propertyType = resolveType(typedName.getType())) != null) {
            if (propertyType instanceof Type.ObjectType || propertyType instanceof Type.Union || propertyType instanceof Type.TokenType) {
                definition.setResult(propertyType, typedName.getName().getValue());
            }
            else {
                error("Invalid type for alias property", typedName.getType().getPosition());
            }
        }
    }

    private void registerParameters(ASTifyGrammar.ExternDefinition definition) {
        Definition.ExternDefinition def = (Definition.ExternDefinition) scope.lookup(definition.getName().getValue());
        Type resolvedType;

        for (Iterator<Property> it = resolvePropertyList(definition.getParameters(), "parameter").iterator(); it.hasNext(); ) {
            def.addParameter(it.next());
        }

        if ((resolvedType = resolveType(definition.getReturnType())) != null) {
            def.setReturnType(resolvedType);
        }
    }

    private PropertyList resolvePropertyList(List<ASTifyGrammar.TypedName> typedNames, String s) {
        PropertyList result = new PropertyList();
        Set<Util.Pair<String, Position>> cache = new HashSet<>();
        Type resolvedType;

        for (ASTifyGrammar.TypedName parameter : typedNames) {
            boolean wasFound = false;

            for (Util.Pair<String, Position> param : cache) {
                if (param.a.equals(parameter.getName().getValue())) {
                    error(
                            "Redefinition of " + s + " '" + param.a + "'",
                            parameter.getPosition(),
                            "previously defined at",
                            param.b
                    );
                    wasFound = true;
                    break;
                }
            }

            if (!wasFound) {
                cache.add(new Util.Pair<>(parameter.getName().getValue(), parameter.getPosition()));
            }

            if ((resolvedType = resolveType(parameter.getType())) != null) {
                result.add(new Property(resolvedType, parameter.getName().getValue()));
            }
        }

        return result;
    }

    private Type resolveType(ASTifyGrammar.Type type) {
        if (scope.isType(type.getName().getValue())) {
            Type resolvedType = scope.lookupType(type.getName().getValue());

            if (type.isOptional()) {
                if (resolvedType instanceof Type.BooleanType) {
                    error("Invalid type declaration '" + resolvedType.getName() + "?'", type.getPosition());
                }
                else {
                    resolvedType = new Type.OptionalType(resolvedType);
                }
            }

            if (type.isLst()) {
                if (resolvedType instanceof Type.BooleanType) {
                    error("Invalid type declaration '" + resolvedType.getName() + "[]'", type.getPosition());
                }
                else {
                    resolvedType = new Type.ListType(resolvedType);
                }
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void bindAllApplications(List<ASTifyGrammar.Statement> statements) {
        for (ASTifyGrammar.Statement statement : statements) {
            if (statement instanceof ASTifyGrammar.ApplyStatement) {
                bindApplications(((ASTifyGrammar.ApplyStatement) statement).getPatternLists(), ((ASTifyGrammar.ApplyStatement) statement).getCall());
            }
            else if (statement instanceof ASTifyGrammar.ExternDefinition) {
                if (!((ASTifyGrammar.ExternDefinition) statement).getPatternLists().isEmpty()) {
                    List<ASTifyGrammar.Parameter> parameters = new ArrayList<>();

                    for (ASTifyGrammar.TypedName parameter : ((ASTifyGrammar.ExternDefinition) statement).getParameters()) {
                        parameters.add(new ASTifyGrammar.Reference(parameter.getName().getPosition(), parameter.getName()));
                    }

                    ASTifyGrammar.Call call = new ASTifyGrammar.Call(
                            ((ASTifyGrammar.ExternDefinition) statement).getName().getPosition(),
                            ((ASTifyGrammar.ExternDefinition) statement).getName(),
                            parameters
                    );

                    bindApplications(((ASTifyGrammar.ExternDefinition) statement).getPatternLists(), call);
                }
            }
        }
    }

    private void bindApplications(List<ASTifyGrammar.PatternList> patternLists, ASTifyGrammar.Call call) {
        if (validateCall(call)) return;
        ExternApplication.Call resultingCall = resolveCall(call);

        if (resultingCall == null) return;

        PropertyList propertyList = resolvePropertyList(resultingCall);

        for (ASTifyGrammar.PatternList patternList : patternLists) {
            bindApplication(patternList, resultingCall, propertyList);
        }
    }

    private void bindApplication(ASTifyGrammar.PatternList patternList, ExternApplication.Call call, PropertyList propertyList) {
        if (validatePatternList(patternList, propertyList, "property")) return;
        List<Pattern> resolvedPatternList = Pattern.createFromList(patternList.getPatterns(), propertyList, this);

        Type type = call.getExtern().getReturnType();

        if (type instanceof Type.IExtendable) {
            ((Type.IExtendable) type).addApplication(new ExternApplication(resolvedPatternList, call));
        }
        else {
            error("Cannot apply syntax to type '" + type.toString() + "'", patternList.getPosition());
        }
    }

    private PropertyList resolvePropertyList(ExternApplication.Call call) {
        return resolvePropertyListRecursive(call, new PropertyList());
    }

    private PropertyList resolvePropertyListRecursive(ExternApplication.Call call, PropertyList propertyList) {
        int i = 0;

        for (ExternApplication.Parameter parameter : call.getParameters()) {
            Type expectedType = call.getExtern().getParameters().get(i++).getType();

            if (parameter instanceof ExternApplication.Reference) {
                propertyList.add(new Property(expectedType, ((ExternApplication.Reference) parameter).getName()));
            }
            else {
                resolvePropertyListRecursive((ExternApplication.Call) parameter, propertyList);
            }
        }

        return propertyList;
    }

    private ExternApplication.Call resolveCall(ASTifyGrammar.Call call) {
        if (scope.exists(call.getFunctionName().getValue())) {
            Definition definition = scope.lookup(call.getFunctionName().getValue());

            if (definition instanceof Definition.ExternDefinition) {
                Definition.ExternDefinition definitionCasted = (Definition.ExternDefinition) definition;
                List<ExternApplication.Parameter> parameters = new ArrayList<>();
                int i = 0;

                for (ASTifyGrammar.Parameter parameter : call.getParameters()) {
                    if (parameter instanceof ASTifyGrammar.Call) {
                        ExternApplication.Call subCall = resolveCall((ASTifyGrammar.Call) parameter);
                        Type expectedType = definitionCasted.getParameters().get(i++).getType();

                        if (subCall != null) {
                            parameters.add(subCall);
                        }
                        else {
                            return null;
                        }

                        if (!subCall.getExtern().getReturnType().castsTo(expectedType)) {
                            error(
                                    "Incompatible types: cannot pass type " + subCall.getExtern().getReturnType().toString() + " to " + expectedType.toString() + " parameter",
                                    parameter.getPosition()
                            );
                        }
                    }
                    else {
                        parameters.add(new ExternApplication.Reference(((ASTifyGrammar.Reference) parameter).getReference().getValue()));
                    }
                }

                if (parameters.size() != definitionCasted.getParameters().size()) {
                    error(
                            "Incorrect number of parameters to function '" + definitionCasted.getName() + "'",
                            call.getPosition(),
                            "Function defined at",
                            definition.getPosition()
                    );
                }

                return new ExternApplication.Call((Definition.ExternDefinition) definition, parameters);
            }
            else {
                error("'" + call.getFunctionName().getValue() + "' is not a function", call.getFunctionName().getPosition());
                return null;
            }
        }
        else {
            error("Cannot find function '" + call.getFunctionName().getValue() + "'", call.getFunctionName().getPosition());
            return null;
        }
    }

    // returns true on error
    private boolean validateCall(ASTifyGrammar.Call call) {
        return validateCallRecursive(call, new HashSet<>());
    }

    private boolean validateCallRecursive(ASTifyGrammar.Call call, Set<Util.Pair<String, Position>> cache) {
        boolean errored = false;

        for (ASTifyGrammar.Parameter parameter : call.getParameters()) {
            if (parameter instanceof ASTifyGrammar.Reference) {

                Token parameterName = ((ASTifyGrammar.Reference) parameter).getReference();
                boolean found = false;

                for (Util.Pair<String, Position> pair : cache) {
                    if (pair.a.equals(parameterName.getValue())) {
                        error(
                                "Reuse of parameter '" + parameterName.getValue() + "'",
                                parameterName.getPosition(),
                                "previously used at",
                                pair.b
                        );
                        found = true;
                        break;
                    }
                }

                if (found) {
                    errored = true;
                }
                else {
                    cache.add(new Util.Pair<>(parameterName.getValue(), parameterName.getPosition()));
                }
            }
            else if (parameter instanceof ASTifyGrammar.Call) {
                if (validateCallRecursive((ASTifyGrammar.Call) parameter, cache)) return true;
            }
        }

        return errored;
    }

    private void bindAllPatternLists(List<ASTifyGrammar.Statement> statements) {
        for (ASTifyGrammar.Statement statement: statements) {
            if (statement instanceof ASTifyGrammar.TypeDefinition) {
                ASTifyGrammar.TypeDefinition castedDefinition = (ASTifyGrammar.TypeDefinition) statement;
                Type.ObjectType type = (Type.ObjectType) scope.lookupType(castedDefinition.getProperties().getName().getValue());

                bindPatternLists(castedDefinition.getPatternLists(), type);
            }
            else if (statement instanceof ASTifyGrammar.AliasDefinition) {
                ASTifyGrammar.AliasDefinition castedDefinition = (ASTifyGrammar.AliasDefinition) statement;
                Definition.AliasDefinition definition = (Definition.AliasDefinition) scope.lookup(castedDefinition.getName().getValue());

                bindPatternLists(castedDefinition.getPatternLists(), definition);
            }
        }
    }

    private void bindPatternLists(List<ASTifyGrammar.PatternList> patterns, Type.ObjectType type) {
        for (ASTifyGrammar.PatternList patternList : patterns) {
            bindPatternList(patternList, type);
        }
    }

    private void bindPatternLists(List<ASTifyGrammar.PatternList> patterns, Definition.AliasDefinition definition) {
        for (ASTifyGrammar.PatternList patternList : patterns) {
            bindPatternList(patternList, definition);
        }
    }

    private void bindPatternList(ASTifyGrammar.PatternList patternList, Type.ObjectType type) {
        if (validatePatternList(patternList, type.getProperties(), "property")) return;
        List<Pattern> resolvedPatternList = Pattern.createFromList(patternList.getPatterns(), type.getProperties(), this);
        type.addPatternList(resolvedPatternList);
    }

    private void bindPatternList(ASTifyGrammar.PatternList patternList, Definition.AliasDefinition definition) {
        PropertyList properties = new PropertyList();

        if (definition.hasResult()) {
            properties.add(definition.getResult());
        }

        if (validatePatternList(patternList, properties, "property")) return;
        List<Pattern> resolvedPatternList = Pattern.createFromList(patternList.getPatterns(), properties, this);
        definition.addPatternList(resolvedPatternList);
    }

    // returns true if there was an error
    private boolean validatePatternList(ASTifyGrammar.PatternList patternList, PropertyList properties, String s) {
        Set<String> requiredProperties = new HashSet<>();
        Set<Util.Pair<String, Position>> setProperties = new HashSet<>();
        boolean errored = false;

        for (Iterator<Property> it = properties.iterator(); it.hasNext(); ) {
            Property property = it.next();

            if (property.getType() instanceof Type.OptionalType) continue;
            if (property.getType() instanceof Type.ListType) continue;
            if (property.getType() instanceof Type.BooleanType) continue;

            requiredProperties.add(property.getName());
        }

        for (ASTifyGrammar.RootPattern p : patternList.getPatterns()) {
            if (p instanceof ASTifyGrammar.Matcher) {
                requiredProperties.remove(((ASTifyGrammar.Matcher) p).getTargetProperty().getProperty().getValue());
            }
            else if (p instanceof ASTifyGrammar.PropertyReference) {
                requiredProperties.remove(((ASTifyGrammar.PropertyReference) p).getProperty().getValue());
            }
        }

        for (String propertyName : requiredProperties) {
            error("Unassigned property '" + propertyName + "'", patternList.getPosition());
            errored = true;
        }

        for (ASTifyGrammar.RootPattern p : patternList.getPatterns()) {
            String propertyName = null;

            if (p instanceof ASTifyGrammar.Matcher) {
                propertyName = ((ASTifyGrammar.Matcher) p).getTargetProperty().getProperty().getValue();
            }
            else if (p instanceof ASTifyGrammar.PropertyReference) {
                propertyName = ((ASTifyGrammar.PropertyReference) p).getProperty().getValue();
            }

            if (propertyName != null) {
                if (!properties.exists(propertyName)) {
                    error(
                            "No such " + s + " '" + propertyName + "'",
                            p.getPosition()
                    );
                    errored = true;
                    continue;
                }

                if (!(properties.lookup(propertyName).getType() instanceof Type.ListType)) {
                    boolean found = false;

                    for (Util.Pair<String, Position> set : setProperties) {
                        if (set.a.equals(propertyName)) {
                            error(
                                    "Multiple assignments of " + s + " '" + propertyName + "'",
                                    p.getPosition(),
                                    "previously assigned at",
                                    set.b
                            );
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        setProperties.add(new Util.Pair<>(propertyName, p.getPosition()));
                    }
                }
            }
        }

        return errored;
    }

    /*
//
    private void bindPatternList(List<ASTifyGrammar.RootPattern> patternList, Definition.TypeDefinition definition, astify.core.Position position) throws GDLException {
        List<Pattern> resolvedPatternList = Pattern.createFromList(patternList, definition.getProperties(), scope);
        validatePatternList(resolvedPatternList, definition.getProperties(), position);
        definition.addPatternList(resolvedPatternList);
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
