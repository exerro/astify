package astify.GDL;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

class PatternBuilder {
    private final OutputHelper patternBuilder = new OutputHelper();
    private final Builder.ClassBuilder patternBuilderClass;
    private final List<String> builtPatterns = new ArrayList<>();
    private final List<HandlerBuilder> handlerBuilders = new ArrayList<>();
    private final BuildConfig buildConfig;
    private final Grammar grammar;

    PatternBuilder(Grammar grammar, BuildConfig buildConfig) {
        this.grammar = grammar;
        this.buildConfig = buildConfig;

        patternBuilderClass = new Builder.ClassBuilder(grammar.getClassName() + "PatternBuilderBase");
        patternBuilderClass.setAccess(buildConfig.getClassAccess());
        patternBuilderClass.setConstructorAccess(buildConfig.getPatternBuilderConstructorAccess());
        patternBuilderClass.setExtends("astify.PatternBuilder");
        patternBuilderClass.setMethodsEnabled(false);
        patternBuilderClass.addConstructorStatement("init" + grammar.getClassName() + "();");
    }

    void createFiles() throws IOException {
        String basePath = buildConfig.getFullPath() + "/";

        patternBuilder.writeToFile(new File(basePath + grammar.getClassName() + "PatternBuilderBase.java"));
    }

    void build() {
        patternBuilder.writeLine("package " + buildConfig.getPackage() + ";");
        patternBuilder.writeLine();
        patternBuilder.writeLine("import java.util.ArrayList;");
        patternBuilder.writeLine("import java.util.Iterator;");
        patternBuilder.writeLine("import java.util.List;");

        for (Definition definition : grammar.getScope().values()) {
            if (definition instanceof Definition.ExternDefinition) {
                patternBuilderClass.addMethod(buildExtern((Definition.ExternDefinition) definition));
            }
            else if (definition instanceof Definition.TypeDefinition) {
                if (((Definition.TypeDefinition) definition).getType() instanceof Type.ObjectType) {
                    buildPatterns((Type.ObjectType) ((Definition.TypeDefinition) definition).getType());
                }
                else if (((Definition.TypeDefinition) definition).getType() instanceof Type.Union) {
                    buildPatterns((Type.Union) ((Definition.TypeDefinition) definition).getType());
                }
            }
            else if (definition instanceof Definition.AliasDefinition) {
                buildPatterns((Definition.AliasDefinition) definition);
            }
        }

        patternBuilderClass.addMethod("private void init" + grammar.getClassName() + "() {\n\t" + String.join("\n\n", builtPatterns).replace("\n", "\n\t") + " \n}");

        patternBuilderClass.addMethod("@Override\n" +
                "public astify.Pattern getMain() {\n" +
                "\treturn lookup(\"" + NameHelper.toLowerLispCase(grammar.getName()) + "\");\n" +
                "}");

        for (HandlerBuilder handlerBuilder : handlerBuilders) {
            patternBuilderClass.addMethod(handlerBuilder.build());
        }

        patternBuilder.ensureLines(2);
        patternBuilderClass.buildTo(patternBuilder);

        /*patternBuilderClass.addMethod((helper) -> {
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

            helper.write("return lookup(\"" + NameHelper.toLowerLispCase(grammar.getName()) + "\");");

            helper.exitBlock();
        });*/

        // buildPatterns();
    }

    private String buildExtern(Definition.ExternDefinition definition) {
        List<String> params = new ArrayList<>();

        for (Iterator<Property> it = definition.getParameters().iterator(); it.hasNext(); ) {
            Property property = it.next();
            params.add(buildType(property.getType()) + " " + property.getName());
        }

        String parameters = Util.listToString(params);
        return "protected abstract " + buildType(definition.getReturnType()) + " " + definition.getName() + "(" + parameters + ");";
    }

    private void buildPatterns(Type.ObjectType type) {
        boolean single = (type.getPatternLists().size() + type.getApplications().size()) == 1;
        int i = 0;
        String name = NameHelper.toLowerLispCase(type.getName());
        String handlerName = "create" + NameHelper.toUpperCamelCase(type.getName());
        List<String> names = new ArrayList<>();

        for (List<Pattern> patternList : type.getPatternLists()) {
            String thisName = single ? name : name + "#" + (++i);
            String callName = single ? handlerName : handlerName + i;
            String s = buildPatternList(patternList);

            names.add("ref(\"" + thisName + "\")");
            builtPatterns.add("sequence(\"" + thisName + "\", this::" + callName + ",\n\t" + s.replace("\n", "\n\t") + "\n);");
            handlerBuilders.add(new ObjectTypeHandlerBuilder(callName, type, patternList));
        }

        for (ExternApplication application : type.getApplications()) {
            String thisName = single ? name : name + "#" + (++i);
            String callName = single ? handlerName : handlerName + i;
            String s = buildPatternList(application.getPatternList());

            names.add("ref(\"" + thisName + "\")");
            builtPatterns.add("sequence(\"" + thisName + "\", this::" + callName + ",\n\t" + s.replace("\n", "\n\t") + "\n);");
            handlerBuilders.add(new ExternApplicationHandlerBuilder(callName, application));
        }

        if (!single) {
            builtPatterns.add("defineInline(\"" + name + "\", one_of(\n\t" + String.join(",\n", names).replace("\n", "\n\t") + "\n));");
        }
    }

    private void buildPatterns(Type.Union type) {
        List<String> members = new ArrayList<>();
        String name = NameHelper.toLowerLispCase(type.getName());

        for (Type.ObjectType subtype : type.getMembers()) {
            members.add("ref(\"" + NameHelper.toLowerLispCase(subtype.getName()) + "\")");
        }

        builtPatterns.add("define(\"" + name + "\", one_of(\n\t" + String.join(",\n\t", members) + "\n));");
    }

    private void buildPatterns(Definition.AliasDefinition definition) {
        boolean single = definition.getPatternLists().size() == 1;
        boolean needsHandler = definition.hasResult();
        List<String> names = new ArrayList<>();
        int i = 0;
        String name = NameHelper.toLowerLispCase(definition.getName());
        String handlerName = "create" + NameHelper.toUpperCamelCase(definition.getName());

        for (List<Pattern> patternList : definition.getPatternLists()) {
            String thisName = single ? name : name + "#" + (++i);
            String callName = single ? handlerName : handlerName + i;
            String s = buildPatternList(patternList);

            names.add("ref(\"" + thisName + "\")");
            builtPatterns.add("sequence(\"" + thisName + "\"" + (needsHandler ? ", this::" + callName : "") + ",\n\t" + s.replace("\n", "\n\t") + "\n);");

            if (needsHandler) {
                handlerBuilders.add(new AliasHandlerBuilder(callName, definition.getResult().getName(), patternList));
            }
        }

        if (!single) {
            builtPatterns.add("defineInline(\"" + name + "\", one_of(\n\t" + String.join(",\n", names).replace("\n", "\n\t") + "\n));");
        }
    }

    private String buildPatternList(List<Pattern> patternList) {
        List<String> terms = new ArrayList<>();

        for (Pattern pattern : patternList) {
            terms.add(buildPattern(pattern));
        }

        return String.join(",\n", terms);
    }

    private String buildPattern(Pattern pattern) {
        if (pattern instanceof Pattern.Matcher) {
            return buildPattern(((Pattern.Matcher) pattern).getSource());
        }
        else if (pattern instanceof Pattern.TypeReference) {
            Type type = ((Pattern.TypeReference) pattern).getReference();

            if (type instanceof Type.TokenType) {
                return "token(" + type.getName() + ")";
            }
            else {
                return "ref(\"" + NameHelper.toLowerLispCase(type.getName()) + "\")";
            }
        }
        else if (pattern instanceof Pattern.ListPattern) {
            if (((Pattern.ListPattern) pattern).hasSeparator()) {
                List<String> patterns = new ArrayList<>();

                for (Pattern p : ((Pattern.ListPattern) pattern).getSeparator()) {
                    patterns.add(buildPattern(p));
                }

                return "delim(" + buildPattern(((Pattern.ListPattern) pattern).getItem()) + ", sequence(" + String.join(", ", patterns) + "))";
            }
            else {
                return "list(" + buildPattern(((Pattern.ListPattern) pattern).getItem()) + ")";
            }
        }
        else if (pattern instanceof Pattern.Optional) {
            return "optional(sequence(\n\t" + buildPatternList(((Pattern.Optional) pattern).getPatterns()).replace("\n", "\n\t") + "\n))";
        }
        else if (pattern instanceof Pattern.Terminal) {
            String value = ((Pattern.Terminal) pattern).getValue();
            value = "\"" + value.substring(1, value.length() - 1).replace("\"", "\\\"").replace("\\'", "'") + "\"";
            return (((Pattern.Terminal) pattern).isKeyword() ? "keyword" : "symbol") + "(" + value + ")";
        }

        throw new Error("what " + pattern.getClass().getName());
    }

    private String buildType(Type type) {
        if (type instanceof Type.ListType) {
            return "List<" + buildType(((Type.ListType) type).getType()) + ">";
        }
        else if (type instanceof Type.OptionalType) {
            return buildType(((Type.OptionalType) type).getType());
        }
        else if (type instanceof Type.BooleanType) {
            return "Boolean";
        }
        else if (type instanceof Type.TokenType) {
            return "astify.token.Token";
        }
        else {
            String typename = type.getReferenceName();

            if (!typename.equals(grammar.getClassName())) {
                typename = grammar.getClassName() + "." + typename;
            }

            return typename;
        }
    }

    /*private void buildPatterns() {
        for (String s : grammar.getDefinedTypes()) {
            Definition d = grammar.getScope().lookupDefinition(s);

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
            return (((Pattern.Terminal) pat).isKeyword() ? "keyword" : "symbol") + "(" + Util.convertStringQuotes(((Pattern.Terminal) pat).getValue()) + ")";
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
        builtPatterns.add("define(\"" + definition.getPatternName() + "\", one_of(" + Util.listToString(Util.map(definition.getParseMembers(), (def) -> "\n\tref(\"" + def.getPatternName() + "\")")) + "\n));");
    }

    private void buildPatternHandlers(Definition.TypeDefinition definition) {
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
                Property property = definition.getProperties().lookup(optional.getName());

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
            output.write(property.getName());
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
            output.write(it.next().getName());
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
                        output.write(property.getName() + ".add(" + castValue("it.next()", property.getType()) + ");");
                    output.exitBlock();
                }
                else {
                    output.write(property.getName() + ".add(" + castValue(source, property.getType()) + ");");
                }
            }
            else {
                String value = getCaptureValue(sourcePattern, source);
                output.ensureLines(1);
                output.write(((Pattern.Matcher) pattern).getTargetProperty() + " = " + value + ";");
            }
        }
        else if (pattern instanceof Pattern.OptionalCapture) {
            Property property = definition.getProperties().lookup(((Pattern.OptionalCapture) pattern).getName());

            output.ensureLines(1);
            output.write(property.getName() + " = !(" + source + " instanceof Capture.EmptyCapture);");
        }
        else if (pattern instanceof Pattern.Optional) {
            if (!optionalHasCapture((Pattern.Optional) pattern)) return;

            output.ensureLines(2);
            output.write("if (!(" + source + " instanceof Capture.EmptyCapture))");
            output.enterBlock();
                String subSourceList = "sub" + NameHelper.toUpperCamelCase(sourceList);
                List<Pattern> subPatterns = ((Pattern.Optional) pattern).getPatterns();

                output.write("Capture.ListCapture " + subSourceList + " = (Capture.ListCapture) " + source + ";");
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
            if (type.getReferenceName().equals(grammar.getClassName())) {
                return grammar.getClassName();
            }
            else {
                return grammar.getClassName() + "." + type.getReferenceName();
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
    }*/

    private abstract class HandlerBuilder {
        protected final String handlerName;
        protected final OutputHelper body = new OutputHelper();
        private final Map<String, Type> variableTypes = new HashMap<>();
        private final Map<String, String> variableValues = new HashMap<>();
        private final List<String> activeCaptureList = new ArrayList<>(); { activeCaptureList.add("captures"); }

        private int blockLevel = 0;

        private HandlerBuilder(String handlerName) {
            this.handlerName = handlerName;
        }

        String getActiveCaptureList() {
            return activeCaptureList.get(activeCaptureList.size() - 1);
        }

        void addProperty(String name, Type type, String initialValue) {
            variableTypes.put(name, type);
            variableValues.put(name, initialValue);
        }

        void addProperty(String name, Type type) {
            addProperty(name, type, "null");
        }

        void enterConditional(String condition) {
            body.ensureLines(2);
            body.write("if (" + condition + ")");
            body.enterBlock();
            ++blockLevel;
        }

        void exitConditional() {
            body.exitBlock();
            --blockLevel;
            body.ensureLines(2);
        }

        void assign(String name, String value) {
            if (blockLevel == 0) {
                variableValues.put(name, cast(value, variableTypes.get(name)));
            }
            else {
                body.ensureLines(1);
                body.write(name + " = " + cast(value, variableTypes.get(name)) + ";");
            }
        }

        boolean test(Pattern.Optional pattern, Function<Pattern, Boolean> test) {
            for (Pattern subPattern : pattern.getPatterns()) {
                if (test.apply(subPattern)) {
                    return true;
                }
            }

            return false;
        }

        void enterOptionalConditional(int i) {
            String subCaptureList = "sub" + NameHelper.toUpperCamelCase(getActiveCaptureList());

            enterConditional("!(" + getActiveCaptureList() + ".get(" + i + ") instanceof astify.Capture.EmptyCapture)");
            body.write("List<astify.Capture> " + subCaptureList + " = ((astify.Capture.ListCapture) " + getActiveCaptureList() + ".get(" + i + ")).all()" + ";");
            activeCaptureList.add(subCaptureList);
        }

        void exitOptionalConditional() {
            exitConditional();
            activeCaptureList.remove(activeCaptureList.size() - 1);
        }

        String cast(String object, Type type) {
            if (type instanceof Type.TokenType) {
                return "((astify.Capture.TokenCapture) " + object + ").getToken()";
            }
            else {
                return "(" + buildType(type) + ") " + object;
            }
        }

        String build() {
            OutputHelper result = new OutputHelper();
            buildBody();

            result.write("private astify.Capture " + handlerName + "(List<astify.Capture> captures)");
            result.enterBlock();

                for (String variableName : variableTypes.keySet()) {
                    result.ensureLines(1);
                    result.write(buildType(variableTypes.get(variableName)) + " " + variableName + " = " + variableValues.get(variableName) + ";");
                }

                result.ensureLinesIf(2, !variableValues.isEmpty());
                result.write(body.getResult());

            result.exitBlock();

            return result.getResult();
        }

        abstract void buildBody();
    }

    private class ObjectTypeHandlerBuilder extends HandlerBuilder {
        private final Type.ObjectType type;
        private final List<Pattern> patternList;

        private ObjectTypeHandlerBuilder(String handlerName, Type.ObjectType type, List<Pattern> patternList) {
            super(handlerName);
            this.type = type;
            this.patternList = patternList;

            for (Iterator<Property> it = type.getProperties().iterator(); it.hasNext(); ) {
                Property property = it.next();
                String defaultValue = "null";

                if (property.getType() instanceof Type.ListType) {
                    defaultValue = "new ArrayList<>()";
                }
                if (property.getType() instanceof Type.BooleanType) {
                    defaultValue = "false";
                }

                addProperty(property.getName(), property.getType(), defaultValue);
            }
        }

        void buildMatcher(Pattern.Matcher pattern, int index) {
            String propertyName = pattern.getTargetProperty();
            Property property = type.getProperties().lookup(propertyName);

            if (property.getType() instanceof Type.ListType) {
                Type objectType = ((Type.ListType) property.getType()).getType();

                if (pattern.getSource() instanceof Pattern.ListPattern) {
                    body.ensureLines(2);

                    body.write("for (Iterator<astify.Capture> it = ((astify.Capture.ListCapture) " + getActiveCaptureList() + ".get(" + index + ")).iterator(); it.hasNext(); )");
                    body.enterBlock();
                        body.write(propertyName + ".add(" + cast("it.next()", objectType) + ");");
                    body.exitBlock();

                    body.ensureLines(2);
                }
                else {
                    body.ensureLines(1);
                    body.write(propertyName + ".add(" + cast(getActiveCaptureList() + ".get(" + index + ")", objectType) + ");");
                }
            }
            else if (property.getType() instanceof Type.BooleanType) {
                assign(propertyName, "!(" + getActiveCaptureList() + ".get(" + index + ") instanceof astify.Capture.EmptyCapture)");
            }
            else {
                assign(propertyName, getActiveCaptureList() + ".get(" + index + ")");
            }
        }

        void buildMatcher(Pattern.OptionalCapture pattern, int index) {
            String propertyName = pattern.getPropertyName();
            assign(propertyName, "!(" + getActiveCaptureList() + ".get(" + index + ") instanceof astify.Capture.EmptyCapture)");
        }

        @Override void buildBody() {
            int index = 0;
            String positionString = "captures.get(0).getPosition()";

            if (patternList.size() > 1) {
                positionString += ".to(captures.get(" + (patternList.size() - 1) + ").getPosition())";
            }

            for (Pattern pattern : patternList) {
                if (pattern instanceof Pattern.Matcher) {
                    buildMatcher((Pattern.Matcher) pattern, index);
                }
                else if (pattern instanceof Pattern.OptionalCapture) {
                    buildMatcher((Pattern.OptionalCapture) pattern, index);
                }
                else if (pattern instanceof Pattern.Optional) {
                    if (test((Pattern.Optional) pattern, (pat) -> pat instanceof Pattern.Matcher)) {
                        int subIndex = 0;

                        enterOptionalConditional(index);

                            for (Pattern subPattern : ((Pattern.Optional) pattern).getPatterns()) {
                                if (subPattern instanceof Pattern.Matcher) {
                                    buildMatcher((Pattern.Matcher) subPattern, subIndex);
                                }
                                else if (subPattern instanceof Pattern.OptionalCapture) {
                                    buildMatcher((Pattern.OptionalCapture) subPattern, subIndex);
                                }

                                ++subIndex;
                            }

                        exitOptionalConditional();
                    }
                }

                ++index;
            }

            body.ensureLines(2);
            body.write("return new " + buildType(type) + "(" + positionString);

            for (Iterator<Property> it = type.getProperties().iterator(); it.hasNext(); ) {
                Property property = it.next();
                body.write(", " + property.getName());
            }

            body.write(");");
        }
    }

    private class ExternApplicationHandlerBuilder extends HandlerBuilder {
        private final ExternApplication application;

        private ExternApplicationHandlerBuilder(String handlerName, ExternApplication application) {
            super(handlerName);
            this.application = application;
        }

        @Override void buildBody() {
            body.write("return null;");
        }
    }

    private class AliasHandlerBuilder extends HandlerBuilder {
        private final String desiredProperty;
        private final List<Pattern> patternList;

        private AliasHandlerBuilder(String handlerName, String desiredProperty, List<Pattern> patternList) {
            super(handlerName);
            this.desiredProperty = desiredProperty;
            this.patternList = patternList;
        }

        @Override void buildBody() {
            body.write("return null;");
        }
    }
}
