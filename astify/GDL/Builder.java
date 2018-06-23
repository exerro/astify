package astify.GDL;

import astify.token.Token;
import astify.token.TokenType;

import java.io.File;
import java.io.IOException;
import java.util.*;

class Builder {
    private final Scope scope = new Scope();
    private final OutputHelper ASTDefinition = new OutputHelper();
    private final OutputHelper patternBuilder = new OutputHelper();
    private final ClassBuilder patternBuilderClass;
    private final List<String> definedTypes = new ArrayList<>();
    private final List<String> builtPatterns = new ArrayList<>();
    private final String grammarName, grammarClassName;
    private final BuildConfig buildConfig;

    Builder(String grammarName, BuildConfig buildConfig) {
        this.grammarName = grammarName;
        this.grammarClassName = NameHelper.toUpperCamelCase(grammarName);
        this.buildConfig = buildConfig;
        this.patternBuilderClass = new ClassBuilder(grammarClassName + "PatternBuilder");

        scope.defineNativeTypes();
    }

    // Setup phase /////////////////////////////////////////////////////////////////////////////////////////////////////

    void setup(ASTifyGrammar grammar) throws GDLException {
        registerDefinitions(grammar.getDefinitions(), grammar.getGrammar());
        registerAllProperties(grammar.getDefinitions());
        bindAllPatternLists(grammar.getDefinitions());
        registerAllUnionMembers(grammar.getDefinitions());
    }

    private void registerDefinitions(List<ASTifyGrammar.Definition> definitions, ASTifyGrammar.Grammar grammar) throws GDLException {
        for (ASTifyGrammar.Definition definition : definitions) {
            registerDefinition(definition);
        }

        if (!(scope.lookupDefinition(grammarName) instanceof Definition.TypeDefinition)) {
            throw new GDLException("Type '" + grammarName + "' not defined", grammar.getPosition());
        }
    }

    private void registerDefinition(ASTifyGrammar.Definition definition) {
        Definition d;

        if (definition instanceof ASTifyGrammar.AbstractTypeDefinition) {
            String name = ((ASTifyGrammar.AbstractTypeDefinition) definition).getProperties().getName().getValue();
            d = new Definition.TypeDefinition(name);
        }
        else if (definition instanceof ASTifyGrammar.TypeDefinition) {
            String name = ((ASTifyGrammar.TypeDefinition) definition).getProperties().getName().getValue();
            d = new Definition.TypeDefinition(name);
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

    // Build phase /////////////////////////////////////////////////////////////////////////////////////////////////////

    void build() {
        ASTDefinition.writeLine("package " + buildConfig.getPackage() + ";");
        ASTDefinition.writeLine();
        ASTDefinition.writeLine("import astify.Capture;");
        ASTDefinition.writeLine("import astify.core.Position;");
        ASTDefinition.writeLine("import astify.token.Token;");
        ASTDefinition.writeLine();
        ASTDefinition.writeLine("import java.util.List;");

        patternBuilder.writeLine("package " + buildConfig.getPackage() + ";");
        patternBuilder.writeLine();
        patternBuilder.writeLine("import astify.Capture;");
        patternBuilder.writeLine("import astify.Pattern;");
        patternBuilder.writeLine("import astify.token.Token;");
        patternBuilder.writeLine();
        patternBuilder.writeLine("import java.util.ArrayList;");
        patternBuilder.writeLine("import java.util.Iterator;");
        patternBuilder.writeLine("import java.util.List;");

        patternBuilderClass.addExtends("astify.PatternBuilder");
        patternBuilderClass.setFlag(ClassBuilder.ENABLE_CONSTRUCTOR, false);
        patternBuilderClass.setFlag(ClassBuilder.ENABLE_METHODS, false);

        patternBuilderClass.addMethod((helper) -> {
            boolean first = true;

            helper.write(buildConfig.getPatternBuilderConstructorAccess().equals("default") ? "" : buildConfig.getPatternBuilderConstructorAccess() + " ");
            helper.write(patternBuilderClass.getClassName() + "()");
            helper.enterBlock();

            for (String pattern : builtPatterns) {
                helper.ensureLines(first ? 1 : 2);
                helper.write(pattern);

                first = false;
            }

            helper.exitBlock();
        });

        patternBuilderClass.addMethod((helper) -> {
            helper.write("@Override\npublic Pattern getMain()");
            helper.enterBlock();

            helper.write("return lookup(\"" + NameHelper.toLowerLispCase(grammarName) + "\");");

            helper.exitBlock();
        });

        buildASTDefinitions();
        buildPatterns();
    }

    private void buildASTDefinitions() {
        Definition.TypeDefinition grammarDefinition = (Definition.TypeDefinition) scope.lookupDefinition(grammarName);
        ClassBuilder grammarClassBuilder = new ClassBuilder(grammarClassName);
        List<Definition> queued = new ArrayList<>();
        Map<Definition, ClassBuilder> classBuilders = new HashMap<>();

        classBuilders.put(grammarDefinition, grammarClassBuilder);
        setupClass(grammarClassBuilder);
        addProperties(grammarDefinition, grammarClassBuilder);

        for (String s : definedTypes) {
            if (!s.equals(grammarName)) {
                assert scope.lookupDefinition(s) != null;
                queued.add(scope.lookupDefinition(s));
            }
        }

        for (Definition d : queued) {
            if (d instanceof Definition.TypeDefinition) {
                Definition.TypeDefinition definition = (Definition.TypeDefinition) d;
                ClassBuilder builder = new ClassBuilder(definition.getStructName());

                setupClass(builder);
                addProperties(definition, builder);

                classBuilders.put(definition, builder);
                grammarClassBuilder.addClass(builder, buildConfig.getClassAccess());
            }
            else if (d instanceof Definition.UnionDefinition) {
                Definition.UnionDefinition definition = (Definition.UnionDefinition) d;
                ClassBuilder builder = new ClassBuilder(definition.getStructName());

                builder.setClassType(ClassBuilder.ClassType.Interface);
                builder.setFlag(ClassBuilder.ENABLE_CONSTRUCTOR, false);
                builder.setFlag(ClassBuilder.ENABLE_METHODS, false);
                builder.addExtends("astify.core.Positioned");

                for (Property property : definition.getSharedProperties()) {
                    builder.addAbstractGetter(property.getType().getReferenceName(), property.getPropertyName());
                }

                classBuilders.put(definition, builder);
                grammarClassBuilder.addClass(builder, buildConfig.getClassAccess());
            }
            else {
                assert false;
            }
        }

        for (Definition d : queued) {
            if (d instanceof Definition.UnionDefinition) {
                Definition.UnionDefinition definition = (Definition.UnionDefinition) d;

                for (Definition member : definition.getRawMembers()) {
                    if (member instanceof Definition.UnionDefinition) {
                        classBuilders.get(member).addExtends(definition.getStructName());
                    }
                    else {
                        classBuilders.get(member).addImplements(definition.getStructName());
                    }
                }
            }
        }

        ASTDefinition.ensureLines(2);
        grammarClassBuilder.buildToModified(ASTDefinition, buildConfig.getClassAccess());
    }

    private void setupClass(ClassBuilder builder) {
        builder.setConstructorAccess(buildConfig.getConstructorAccess());
        builder.setGetterAccess(buildConfig.getGetterAccess());
        builder.addExtends("Capture.ObjectCapture");
        builder.addConstructorField("Position", "spanningPosition");
    }

    private void addProperties(Definition.TypeDefinition definition, ClassBuilder builder) {
        for (Iterator<Property> it = definition.getProperties().iterator(); it.hasNext(); ) {
            Property property = it.next();
            builder.addField(getTypeString(property), property.getPropertyName(), property.isOptional());
        }
    }

    private String getTypeString(Property property) {
        String typeString = property.getType().getReferenceName();
        return property.isList() ? "List<" + typeString + ">" : typeString;
    }

    private void buildPatterns() {
        for (String s : definedTypes) {
            Definition d = scope.lookupDefinition(s);

            if (d instanceof Definition.TypeDefinition) {
                Definition.TypeDefinition definition = (Definition.TypeDefinition) d;
                buildPatternDefinitions(definition);
                buildPatternHandlers(definition);
            }
            else if (d instanceof Definition.UnionDefinition) {
                Definition.UnionDefinition definition = (Definition.UnionDefinition) d;
                buildUnionPatternDefinition(definition);
            }
        }

        patternBuilderClass.buildToModified(patternBuilder, buildConfig.getClassAccess());
    }

    private void buildPatternDefinitions(Definition.TypeDefinition definition) {
        String baseName = definition.getPatternName();
        boolean appendIndex = definition.getPatternLists().size() > 1;
        int i = 0;
        List<String> patternNames = new ArrayList<>();

        for (List<Pattern> patternList : definition.getPatternLists()) {
            buildPatternDefinition(definition, patternList, i);
            patternNames.add(getPatternName(definition, i++));
        }

        if (appendIndex) {
            builtPatterns.add("defineInline(\"" + baseName + "\", one_of(" + Util.listToString(Util.map(patternNames, (s) -> "\n\tref(\"" + s + "\")")) + "\n));");
        }
    }

    private void buildPatternDefinition(Definition.TypeDefinition definition, List<Pattern> patternList, int index) {
        builtPatterns.add("sequence(\"" + getPatternName(definition, index) + "\", this::" + getPatternHandlerName(definition, index) + ", " + buildPatternList(patternList, true) + "\n);");
    }

    private String buildPattern(Pattern pat) {
        if (pat instanceof Pattern.Optional) {
            return "optional(sequence(" + buildPatternList(((Pattern.Optional) pat).getPatterns(), true) + "\n))";
        }
        else if (pat instanceof Pattern.ListPattern) {
            Pattern.ListPattern pattern = (Pattern.ListPattern) pat;

            if (pattern.hasSeparator()) {
                return "delim(" + buildPattern(pattern.getItem()) + ", sequence(" + buildPatternList(pattern.getSeparator(), false) + "))";
            }
            else {
                return "list(" + buildPattern(pattern.getItem()) + ")";
            }
        }
        else if (pat instanceof Pattern.Terminal) {
            return (((Pattern.Terminal) pat).isKeyword() ? "keyword" : "operator") + "(" + Util.convertStringQuotes(((Pattern.Terminal) pat).getValue()) + ")";
        }
        else if (pat instanceof Pattern.TypeReference) {
            Type type = ((Pattern.TypeReference) pat).getReference();

            if (type instanceof Type.DefinedType) {
                return "ref(\"" + ((Type.DefinedType) type).getDefinition().getPatternName() + "\")";
            }
            else if (type instanceof Type.TokenType) {
                return "token(" + ((Type.TokenType) type).getTokenType().name() + ")";
            }
        }
        else if (pat instanceof Pattern.Matcher) {
            return buildPattern(((Pattern.Matcher) pat).getSource());
        }
        throw new Error("what");
    }

    private String buildPatternList(List<Pattern> pat, boolean newlines) {
        if (newlines)
            return Util.listToString(Util.map(pat, (pattern) -> "\n\t" + buildPattern(pattern).replace("\n", "\n\t")));
        else
            return Util.listToString(Util.map(pat, (pattern) -> "" + buildPattern(pattern)));
    }

    private void buildUnionPatternDefinition(Definition.UnionDefinition definition) {
        builtPatterns.add("define(\"" + definition.getPatternName() + "\", one_of(" + Util.listToString(Util.map(definition.getMembers(), (def) -> "\n\tref(\"" + def.getPatternName() + "\")")) + "\n));");
    }

    private void buildPatternHandlers(Definition.TypeDefinition definition) {
        String baseName = "create" + definition.getStructName();
        boolean appendIndex = definition.getPatternLists().size() > 1;
        int i = 0;

        for (List<Pattern> patternList : definition.getPatternLists()) {
            buildPatternHandler(definition, patternList, i++);
        }
    }

    private void buildPatternHandler(Definition.TypeDefinition definition, List<Pattern> patternList, int index) {
        patternBuilderClass.addMethod((output) -> {
            output.ensureLines(2);
            output.writeLine("// " + Util.listToString(patternList));
            output.write("private Capture " + getPatternHandlerName(definition, index) + "(List<Capture> captures)");
            output.enterBlock();
            output.write(buildCaptureHandler(definition, patternList));
            output.exitBlock();
        });
    }

    private String buildCaptureHandler(Definition.TypeDefinition definition, List<Pattern> patternList) {
        List<Property> propertyList = new ArrayList<>();
        List<Integer> queue = new ArrayList<>();
        Map<Property, String> definedValues = new HashMap<>();
        OutputHelper output = new OutputHelper();

        for (Iterator<Property> it = definition.getProperties().iterator(); it.hasNext(); ) {
            Property property = it.next();
            propertyList.add(property);
            definedValues.put(property, property.isList() ? "new ArrayList<>()" : "null");
        }

        for (int i = 0; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);

            if (pattern instanceof Pattern.Matcher) {
                Pattern.Matcher matcher = (Pattern.Matcher) pattern;
                Property property = definition.getProperties().lookup(matcher.getTargetProperty());

                if (property.isList()) {
                    queue.add(i);
                }
                else {
                    definedValues.put(property, getCaptureValue(matcher.getSource(), "captures.get(" + i + ")"));
                }
            }
            else if (pattern instanceof Pattern.OptionalCapture) {
                Pattern.OptionalCapture optional = (Pattern.OptionalCapture) pattern;
                Property property = definition.getProperties().lookup(optional.getPropertyName());

                definedValues.put(property, "!(captures.get(" + i + ") instanceof Capture.EmptyCapture)");
            }
            else if (pattern instanceof Pattern.Optional) {
                queue.add(i);
            }
        }

        for (Property property : propertyList) {
            output.ensureLines(1);
            output.write(getClassName(property));
            output.write(" ");
            output.write(property.getPropertyName());
            output.write(" = ");
            output.write(definedValues.get(property));
            output.write(";");
        }

        output.ensureLines(1);

        if (patternList.size() > 1) {
            output.write("astify.core.Position spanningPosition = captures.get(0).getPosition().to(captures.get(" + (patternList.size() - 1) + ").getPosition());");
        }
        else {
            output.write("astify.core.Position spanningPosition = captures.get(0).getPosition();");
        }

        output.ensureLines(2);

        for (int i : queue) {
            writeCaptureHandler(definition, patternList.get(i), "captures", i, output);
        }

        output.ensureLines(2);
        output.write("return new " + getClassName(new Type.DefinedType(definition)) + "(spanningPosition");

        for (Iterator<Property> it = definition.getProperties().iterator(); it.hasNext(); ) {
            output.write(", ");
            output.write(it.next().getPropertyName());
        }

        output.write(");");

        return output.getResult();
    }

    private void writeCaptureHandler(Definition.TypeDefinition definition, Pattern pattern, String sourceList, int captureIndex, OutputHelper output) {
        String source = sourceList + ".get(" + captureIndex + ")";

        if (pattern instanceof Pattern.Matcher) {
            Property property = definition.getProperties().lookup(((Pattern.Matcher) pattern).getTargetProperty());
            Pattern sourcePattern = ((Pattern.Matcher) pattern).getSource();

            if (property.isList()) {
                output.ensureLines(1);

                if (sourcePattern instanceof Pattern.ListPattern) {
                    output.ensureLines(2);
                    output.write("for (Iterator<Capture> it = ((Capture.ListCapture) " + source + ").iterator(); it.hasNext(); )");
                    output.enterBlock();
                        output.write(property.getPropertyName() + ".add(" + castValue("it.next()", property.getType()) + ");");
                    output.exitBlock();
                }
                else {
                    output.write(property.getPropertyName() + ".add(" + castValue(source, property.getType()) + ");");
                }
            }
            else {
                String value = getCaptureValue(sourcePattern, source);
                output.ensureLines(1);
                output.write(((Pattern.Matcher) pattern).getTargetProperty() + " = " + value + ";");
            }
        }
        else if (pattern instanceof Pattern.OptionalCapture) {
            Property property = definition.getProperties().lookup(((Pattern.OptionalCapture) pattern).getPropertyName());

            output.ensureLines(1);
            output.write(property.getPropertyName() + " = !(" + source + " instanceof Capture.EmptyCapture);");
        }
        else if (pattern instanceof Pattern.Optional) {
            if (!optionalHasCapture((Pattern.Optional) pattern)) return;

            output.ensureLines(2);
            output.write("if (!(" + source + " instanceof Capture.EmptyCapture))");
            output.enterBlock();
                String subSourceList = "sub" + NameHelper.toUpperCamelCase(sourceList);
                List<Pattern> subPatterns = ((Pattern.Optional) pattern).getPatterns();

                output.write("List<Capture> " + subSourceList + " = ((Capture.ListCapture) " + source + ").all();");
                output.writeLine();
                output.writeLine();

                for (int i = 0; i < subPatterns.size(); ++i) {
                    writeCaptureHandler(definition, subPatterns.get(i), subSourceList, i, output);
                }
            output.exitBlock();
        }
    }

    private String getCaptureValue(Pattern pattern, String source) {
        if (pattern instanceof Pattern.OptionalCapture) {
            return "!(" + source + " instanceof Capture.EmptyCapture)";
        }
        else if (pattern instanceof Pattern.TypeReference) {
            return castValue(source, ((Pattern.TypeReference) pattern).getReference());
        }
        else if (pattern instanceof Pattern.Terminal) {
            return castValue(source, new Type.TokenType(TokenType.EOF));
        }
        throw new Error("what " + pattern.getClass().getName());
    }

    private String castValue(String value, Type type) {
        if (type instanceof Type.DefinedType) {
            return "(" + getClassName(type) + ") "  + value;
        }
        else if (type instanceof Type.TokenType) {
            return "((Capture.TokenCapture) " + value + ").getToken()";
        }
        else {
            throw new Error("what");
        }
    }

    private String getClassName(Property property) {
        String className = getClassName(property.getType());

        if (property.isList()) {
            return "List<" + className + ">";
        }

        return className;
    }

    private String getClassName(Type type) {
        if (type instanceof Type.BooleanType) {
            return "boolean";
        }
        else if (type instanceof Type.TokenType) {
            return "Token";
        }
        else if (type instanceof Type.DefinedType) {
            if (type.getReferenceName().equals(grammarClassName)) {
                return grammarClassName;
            }
            else {
                return grammarClassName + "." + type.getReferenceName();
            }
        }
        else throw new Error("what");
    }

    private boolean optionalHasCapture(Pattern.Optional optional) {
        for (Pattern pattern : optional.getPatterns()) {
            if (pattern instanceof Pattern.Matcher) return true;
            if (pattern instanceof Pattern.Optional && optionalHasCapture((Pattern.Optional) pattern)) return true;
        }

        return false;
    }

    private String getPatternHandlerName(Definition.TypeDefinition definition, int index) {
        return "create" + definition.getStructName() + (definition.getPatternLists().size() > 1 ? "_" + (index + 1) : "");
    }

    private String getPatternName(Definition.TypeDefinition definition, int index) {
        return definition.getPatternName() + (definition.getPatternLists().size() > 1 ? "-" + (index + 1) : "");
    }

    // Output phase ////////////////////////////////////////////////////////////////////////////////////////////////////

    void createFiles() throws IOException {
        String basePath = buildConfig.getFullPath() + "/";

        ASTDefinition.writeToFile(new File(basePath + grammarClassName + ".java"));
        patternBuilder.writeToFile(new File(basePath + grammarClassName + "PatternBuilder.java"));
    }

    /*
    private ClassBuilder.Builder generateCreatorCallback(String name, Definition.TypeDefinition definition, List<Pattern> patternList) {
        OutputHelper content = new OutputHelper();
        Definition.TypeDefinition grammarDefinition = (Definition.TypeDefinition) scope.lookupDefinition(grammarName);
        List<String> propertyNames = new ArrayList<>();

        for (Property property : definition.getProperties()) {

            String value = property.isList() ? "new ArrayList<>()" : "null";

            content.writeLine(getPrefixedPropertyType(property) + " " + property.getPropertyName() + " = " + value + ";");
            propertyNames.add(property.getPropertyName());
        }

        for (int i = 0; i < patternList.size(); ++i) {
            Pattern pattern = patternList.get(i);
            boolean useful = false;

            if (pattern instanceof Pattern.Optional) {
                for (Pattern subPattern : ((Pattern.Optional) pattern).getPatterns()) {
                    if (subPattern instanceof Pattern.Matcher) {
                        useful = true;
                        break;
                    }
                }
            }
            else if (pattern instanceof Pattern.Matcher) {
                useful = true;
            }

            if (useful) {
                if (pattern instanceof Pattern.Optional) {
                    content.ensureLines(2);
                    content.write("if (!(captures.get(" + i + ") instanceof Capture.EmptyCapture))");
                    content.enterBlock();
                    content.write("List<Capture> subCaptures = (List<Capture>) ((Capture.ListCapture) captures.get(" + i + ")).all();");

                    for (int n = 0; n < ((Pattern.Optional) pattern).getPatterns().size(); ++n) {
                        Pattern subPattern = ((Pattern.Optional) pattern).getPatterns().get(n);
                        if (subPattern instanceof Pattern.Matcher) {
                            content.write(getMatcherSetString("subCaptures.get(" + n + ")", (Pattern.Matcher) subPattern, definition));
                        }
                    }

                    content.exitBlock();
                    content.writeLine();
                    content.writeLine();
                }
                else if (pattern instanceof Pattern.Matcher) {
                    content.ensureLines(1);
                    content.write(getMatcherSetString("captures.get(" + i + ")", (Pattern.Matcher) pattern, definition));
                }
            }
        }

        content.ensureLines(2);
        content.write("return new ");
        content.write((definition == grammarDefinition ? "" : grammarDefinition.getStructName() + ".") + definition.getStructName());

        if (patternList.size() == 1) {
            content.write("(captures.get(0).spanningPosition, ");
        }
        else {
            content.write("(captures.get(0).spanningPosition.to(captures.get(" + (patternList.size() - 1) + ").spanningPosition), ");
        }
        content.write(String.join(", ", propertyNames) + ");");

        return (helper) -> {
            helper.writeLine("// " + patternList.toString());
            helper.write("private Capture " + name + "(List<Capture> captures)");
            helper.enterBlock();

                helper.write(content.getResult());

            helper.exitBlock();
        };
    }

    private String getMatcherSetString(String sourceValue, Pattern.Matcher source, Definition.TypeDefinition definition) {
        String propertyName = source.getTargetProperty();
        Property property = definition.getProperty(propertyName);
        Function<String, String> getValue;

        if (property == null) throw new Error("TODO");

        if (property.getType() instanceof Type.DefinedType) {
            getValue = (value) -> "(" + getPrefixedClassName(property.getType().getReferenceName()) + ") " + value;
        }
        else if (property.getType() instanceof Type.TokenType) {
            getValue = (value) -> "((Capture.TokenCapture) " + value + ").getToken()";
        }
        else if (property.getType() instanceof Type.BooleanType) {
            getValue = (value) -> "!(" + value + " instanceof Capture.EmptyCapture)";
        }
        else throw new Error("what");

        if (property.isList()) {
            if (source.getSource() instanceof Pattern.ListPattern) {
                return "for (Iterator it = ((Capture.ListCapture) " + sourceValue + ").iterator(); it.hasNext(); ) {\n\t"
                        + propertyName + ".add(" + getValue.apply("it.next()") + ");\n"
                        + "}";
            }
            else {
                return propertyName + ".add(" + getValue.apply(sourceValue) + ");";
            }
        }
        else {
            if (source.getSource() instanceof Pattern.ListPattern) {
                throw new Error("TODO");
            }
            else {
                return propertyName + " = " + getValue.apply(sourceValue) + ";";
            }
        }
    }

    private String getPrefixedPropertyType(Property property) {
        if (property.getType() instanceof Type.DefinedType) {
            if (!((Type.DefinedType) property.getType()).getDefinition().getStructName().equals(NameHelper.toUpperCamelCase(grammarName))) {
                return property.getTypeString(NameHelper.toUpperCamelCase(grammarName) + ".");
            }
        }
        return property.getTypeString();
    }

    private String getPrefixedClassName(String name) {
        if (name.equals("Token") || name.equals(NameHelper.toUpperCamelCase(grammarName))) {
            return name;
        }

        return NameHelper.toUpperCamelCase(grammarName) + "." + name;
    }*/
}
