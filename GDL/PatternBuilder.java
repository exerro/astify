package GDL;

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

        boolean externFound = false;

        for (Definition definition : grammar.getScope().values()) {
            if (definition instanceof Definition.ExternDefinition) {
                externFound = true;
                break;
            }
        }

        patternBuilderClass = new Builder.ClassBuilder(grammar.getClassName() + "PatternBuilderBase");
        patternBuilderClass.setAccess(buildConfig.getClassAccess());
        patternBuilderClass.setAbstract(externFound);
        patternBuilderClass.setConstructorAccess(buildConfig.getPatternBuilderConstructorAccess());
        patternBuilderClass.setExtends("astify.PatternBuilder");
        patternBuilderClass.disableMethods();
        patternBuilderClass.addConstructorStatement("init" + grammar.getClassName() + "();");
    }

    void createFiles() throws IOException {
        String basePath = buildConfig.getFullPath() + "/";

        patternBuilder.writeToFile(new File(basePath + grammar.getClassName() + "PatternBuilderBase.java"));
    }

    void build() {
        if (buildConfig.hasPackage()) {
            patternBuilder.writeLine("package " + buildConfig.getPackage() + ";");
            patternBuilder.writeLine();
        }

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
            helper.write("@Override\npublic GDL.Pattern getMain()");
            helper.enterBlock();

            helper.write("return lookup(\"" + GDL.NameHelper.toLowerLispCase(grammar.getName()) + "\");");

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
        String handlerName = "__create" + NameHelper.toUpperCamelCase(type.getName());
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
        String handlerName = "__create" + NameHelper.toUpperCamelCase(definition.getName());

        for (List<Pattern> patternList : definition.getPatternLists()) {
            String thisName = single ? name : name + "#" + (++i);
            String callName = single ? handlerName : handlerName + i;
            String s = buildPatternList(patternList);

            names.add("ref(\"" + thisName + "\")");
            builtPatterns.add("sequence(\"" + thisName + "\"" + (needsHandler ? ", this::" + callName : "") + ",\n\t" + s.replace("\n", "\n\t") + "\n);");

            if (needsHandler) {
                handlerBuilders.add(new AliasHandlerBuilder(callName, definition.getResult(), patternList));
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
        else if (pattern instanceof Pattern.BuiltinPredicate) {
            return "predicate(astify.MatchPredicate." + ((Pattern.BuiltinPredicate) pattern).getPredicateName() + "())";
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
            GDL.Definition d = grammar.getScope().lookupDefinition(s);

            if (d instanceof GDL.Definition.TypeDefinition) {
                GDL.Definition.TypeDefinition definition = (GDL.Definition.TypeDefinition) d;
                buildPatternDefinitions(definition);
                buildPatternHandlers(definition);
            }
            else if (d instanceof GDL.Definition.UnionDefinition) {
                GDL.Definition.UnionDefinition definition = (GDL.Definition.UnionDefinition) d;
                buildUnionPatternDefinition(definition);
            }
        }

        patternBuilderClass.buildToModified(patternBuilder, buildConfig.getClassAccess());
    }

    private void buildPatternDefinitions(GDL.Definition.TypeDefinition definition) {
        String baseName = definition.getPatternName();
        boolean appendIndex = definition.getPatternLists().size() > 1;
        int i = 0;
        List<String> patternNames = new ArrayList<>();

        for (List<GDL.Pattern> patternList : definition.getPatternLists()) {
            buildPatternDefinition(definition, patternList, i);
            patternNames.add(getPatternName(definition, i++));
        }

        if (appendIndex) {
            builtPatterns.add("defineInline(\"" + baseName + "\", one_of(" + GDL.Util.listToString(GDL.Util.map(patternNames, (s) -> "\n\tref(\"" + s + "\")")) + "\n));");
        }
    }

    private void buildPatternDefinition(GDL.Definition.TypeDefinition definition, List<GDL.Pattern> patternList, int index) {
        builtPatterns.add("sequence(\"" + getPatternName(definition, index) + "\", this::" + getPatternHandlerName(definition, index) + ", " + buildPatternList(patternList, true) + "\n);");
    }

    private String buildPattern(GDL.Pattern pat) {
        if (pat instanceof GDL.Pattern.Optional) {
            return "optional(sequence(" + buildPatternList(((GDL.Pattern.Optional) pat).getPatterns(), true) + "\n))";
        }
        else if (pat instanceof GDL.Pattern.ListPattern) {
            GDL.Pattern.ListPattern pattern = (GDL.Pattern.ListPattern) pat;

            if (pattern.hasSeparator()) {
                return "delim(" + buildPattern(pattern.getItem()) + ", sequence(" + buildPatternList(pattern.getSeparator(), false) + "))";
            }
            else {
                return "list(" + buildPattern(pattern.getItem()) + ")";
            }
        }
        else if (pat instanceof GDL.Pattern.Terminal) {
            return (((GDL.Pattern.Terminal) pat).isKeyword() ? "keyword" : "symbol") + "(" + GDL.Util.convertStringQuotes(((GDL.Pattern.Terminal) pat).getValue()) + ")";
        }
        else if (pat instanceof GDL.Pattern.TypeReference) {
            GDL.Type type = ((GDL.Pattern.TypeReference) pat).getReference();

            if (type instanceof GDL.Type.DefinedType) {
                return "ref(\"" + ((GDL.Type.DefinedType) type).getDefinition().getPatternName() + "\")";
            }
            else if (type instanceof GDL.Type.TokenType) {
                return "token(" + ((GDL.Type.TokenType) type).getTokenType().name() + ")";
            }
        }
        else if (pat instanceof GDL.Pattern.Matcher) {
            return buildPattern(((GDL.Pattern.Matcher) pat).getSource());
        }
        throw new Error("what");
    }

    private String buildPatternList(List<GDL.Pattern> pat, boolean newlines) {
        if (newlines)
            return GDL.Util.listToString(GDL.Util.map(pat, (pattern) -> "\n\t" + buildPattern(pattern).replace("\n", "\n\t")));
        else
            return GDL.Util.listToString(GDL.Util.map(pat, (pattern) -> "" + buildPattern(pattern)));
    }

    private void buildUnionPatternDefinition(GDL.Definition.UnionDefinition definition) {
        builtPatterns.add("define(\"" + definition.getPatternName() + "\", one_of(" + GDL.Util.listToString(GDL.Util.map(definition.getParseMembers(), (def) -> "\n\tref(\"" + def.getPatternName() + "\")")) + "\n));");
    }

    private void buildPatternHandlers(GDL.Definition.TypeDefinition definition) {
        int i = 0;

        for (List<GDL.Pattern> patternList : definition.getPatternLists()) {
            buildPatternHandler(definition, patternList, i++);
        }
    }

    private void buildPatternHandler(GDL.Definition.TypeDefinition definition, List<GDL.Pattern> patternList, int index) {
        patternBuilderClass.addMethod((output) -> {
            output.ensureLines(2);
            output.writeLine("// " + GDL.Util.listToString(patternList));
            output.write("private Capture " + getPatternHandlerName(definition, index) + "(List<Capture> captures)");
            output.enterBlock();
            output.write(buildCaptureHandler(definition, patternList));
            output.exitBlock();
        });
    }

    private String buildCaptureHandler(GDL.Definition.TypeDefinition definition, List<GDL.Pattern> patternList) {
        List<GDL.Property> propertyList = new ArrayList<>();
        List<Integer> queue = new ArrayList<>();
        Map<GDL.Property, String> definedValues = new HashMap<>();
        GDL.OutputHelper output = new GDL.OutputHelper();

        for (Iterator<GDL.Property> it = definition.getProperties().iterator(); it.hasNext(); ) {
            GDL.Property property = it.next();
            propertyList.add(property);
            definedValues.put(property, property.isList() ? "new ArrayList<>()" : "null");
        }

        for (int i = 0; i < patternList.size(); ++i) {
            GDL.Pattern pattern = patternList.get(i);

            if (pattern instanceof GDL.Pattern.Matcher) {
                GDL.Pattern.Matcher matcher = (GDL.Pattern.Matcher) pattern;
                GDL.Property property = definition.getProperties().lookup(matcher.getTargetProperty());

                if (property.isList()) {
                    queue.add(i);
                }
                else {
                    definedValues.put(property, getCaptureValue(matcher.getSource(), "captures.get(" + i + ")"));
                }
            }
            else if (pattern instanceof GDL.Pattern.OptionalCapture) {
                GDL.Pattern.OptionalCapture optional = (GDL.Pattern.OptionalCapture) pattern;
                GDL.Property property = definition.getProperties().lookup(optional.getName());

                definedValues.put(property, "!(captures.get(" + i + ") instanceof Capture.EmptyCapture)");
            }
            else if (pattern instanceof GDL.Pattern.Optional) {
                queue.add(i);
            }
        }

        for (GDL.Property property : propertyList) {
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
        output.write("return new " + getClassName(new GDL.Type.DefinedType(definition)) + "(spanningPosition");

        for (Iterator<GDL.Property> it = definition.getProperties().iterator(); it.hasNext(); ) {
            output.write(", ");
            output.write(it.next().getName());
        }

        output.write(");");

        return output.getResult();
    }

    private void writeCaptureHandler(GDL.Definition.TypeDefinition definition, GDL.Pattern pattern, String sourceList, int captureIndex, GDL.OutputHelper output) {
        String source = sourceList + ".get(" + captureIndex + ")";

        if (pattern instanceof GDL.Pattern.Matcher) {
            GDL.Property property = definition.getProperties().lookup(((GDL.Pattern.Matcher) pattern).getTargetProperty());
            GDL.Pattern sourcePattern = ((GDL.Pattern.Matcher) pattern).getSource();

            if (property.isList()) {
                output.ensureLines(1);

                if (sourcePattern instanceof GDL.Pattern.ListPattern) {
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
                output.write(((GDL.Pattern.Matcher) pattern).getTargetProperty() + " = " + value + ";");
            }
        }
        else if (pattern instanceof GDL.Pattern.OptionalCapture) {
            GDL.Property property = definition.getProperties().lookup(((GDL.Pattern.OptionalCapture) pattern).getName());

            output.ensureLines(1);
            output.write(property.getName() + " = !(" + source + " instanceof Capture.EmptyCapture);");
        }
        else if (pattern instanceof GDL.Pattern.Optional) {
            if (!optionalHasCapture((GDL.Pattern.Optional) pattern)) return;

            output.ensureLines(2);
            output.write("if (!(" + source + " instanceof Capture.EmptyCapture))");
            output.enterBlock();
                String subSourceList = "sub" + GDL.NameHelper.toUpperCamelCase(sourceList);
                List<GDL.Pattern> subPatterns = ((GDL.Pattern.Optional) pattern).getPatterns();

                output.write("Capture.ListCapture " + subSourceList + " = (Capture.ListCapture) " + source + ";");
                output.writeLine();
                output.writeLine();

                for (int i = 0; i < subPatterns.size(); ++i) {
                    writeCaptureHandler(definition, subPatterns.get(i), subSourceList, i, output);
                }
            output.exitBlock();
        }
    }

    private String getCaptureValue(GDL.Pattern pattern, String source) {
        if (pattern instanceof GDL.Pattern.OptionalCapture) {
            return "!(" + source + " instanceof Capture.EmptyCapture)";
        }
        else if (pattern instanceof GDL.Pattern.TypeReference) {
            return castValue(source, ((GDL.Pattern.TypeReference) pattern).getReference());
        }
        else if (pattern instanceof GDL.Pattern.Terminal) {
            return castValue(source, new GDL.Type.TokenType(TokenType.EOF));
        }
        throw new Error("what " + pattern.getClass().getName());
    }

    private String castValue(String value, GDL.Type type) {
        if (type instanceof GDL.Type.DefinedType) {
            return "(" + getClassName(type) + ") "  + value;
        }
        else if (type instanceof GDL.Type.TokenType) {
            return "((Capture.TokenCapture) " + value + ").getToken()";
        }
        else {
            throw new Error("what");
        }
    }

    private String getClassName(GDL.Property property) {
        String className = getClassName(property.getType());

        if (property.isList()) {
            return "List<" + className + ">";
        }

        return className;
    }

    private String getClassName(GDL.Type type) {
        if (type instanceof GDL.Type.BooleanType) {
            return "boolean";
        }
        else if (type instanceof GDL.Type.TokenType) {
            return "Token";
        }
        else if (type instanceof GDL.Type.DefinedType) {
            if (type.getReferenceName().equals(grammar.getClassName())) {
                return grammar.getClassName();
            }
            else {
                return grammar.getClassName() + "." + type.getReferenceName();
            }
        }
        else throw new Error("what");
    }

    private boolean optionalHasCapture(GDL.Pattern.Optional optional) {
        for (GDL.Pattern pattern : optional.getPatterns()) {
            if (pattern instanceof GDL.Pattern.Matcher) return true;
            if (pattern instanceof GDL.Pattern.Optional && optionalHasCapture((GDL.Pattern.Optional) pattern)) return true;
        }

        return false;
    }

    private String getPatternHandlerName(GDL.Definition.TypeDefinition definition, int index) {
        return "create" + definition.getStructName() + (definition.getPatternLists().size() > 1 ? "_" + (index + 1) : "");
    }

    private String getPatternName(GDL.Definition.TypeDefinition definition, int index) {
        return definition.getPatternName() + (definition.getPatternLists().size() > 1 ? "-" + (index + 1) : "");
    }*/

    private abstract class HandlerBuilder {
        protected final String handlerName;
        protected final OutputHelper body = new OutputHelper();
        protected final PropertyList properties;
        private final Map<String, Type> variableTypes = new HashMap<>();
        private final Map<String, String> variableValues = new HashMap<>();
        private final List<String> activeCaptureList = new ArrayList<>(); { activeCaptureList.add("captures"); }

        private int blockLevel = 0;

        private HandlerBuilder(String handlerName, PropertyList properties) {
            this.handlerName = handlerName;
            this.properties = properties;
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

        void assign(String propertyName, Pattern sourcePattern, int index) {
            Type propertyType = properties.lookup(propertyName).getType();

            if (propertyType instanceof Type.ListType) {
                Type objectType = ((Type.ListType) propertyType).getType();
                if (sourcePattern instanceof Pattern.ListPattern) {
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
            else if (propertyType instanceof Type.BooleanType) {
                assign(propertyName, "!(" + getActiveCaptureList() + ".get(" + index + ") instanceof astify.Capture.EmptyCapture)");
            }
            else {
                assign(propertyName, getActiveCaptureList() + ".get(" + index + ")");
            }
        }

        void assignOptional(String propertyName, int index) {
            assign(propertyName, "!(" + getActiveCaptureList() + ".get(" + index + ") instanceof astify.Capture.EmptyCapture)");
        }

        void assignProperties(List<Pattern> patternList) {
            int index = 0;

            body.ensureLines(2);

            for (Pattern pattern : patternList) {
                if (pattern instanceof Pattern.Matcher) {
                    assign(((Pattern.Matcher) pattern).getTargetProperty(), ((Pattern.Matcher) pattern).getSource(), index);
                }
                else if (pattern instanceof Pattern.OptionalCapture) {
                    assignOptional(((Pattern.OptionalCapture) pattern).getPropertyName(), index);
                }
                else if (pattern instanceof Pattern.Optional) {
                    if (test((Pattern.Optional) pattern, (pat) -> pat instanceof Pattern.Matcher)) {
                        int subIndex = 0;

                        enterOptionalConditional(index);

                        for (Pattern subPattern : ((Pattern.Optional) pattern).getPatterns()) {
                            if (subPattern instanceof Pattern.Matcher) {
                                assign(((Pattern.Matcher) subPattern).getTargetProperty(), ((Pattern.Matcher) subPattern).getSource(), subIndex);
                            }
                            else if (subPattern instanceof Pattern.OptionalCapture) {
                                assignOptional(((Pattern.OptionalCapture) subPattern).getPropertyName(), subIndex);
                            }

                            ++subIndex;
                        }

                        exitOptionalConditional();
                    }
                }

                ++index;
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
            super(handlerName, type.getProperties());
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

        @Override void buildBody() {
            String positionString = "captures.get(0).getPosition()";

            if (patternList.size() > 1) {
                positionString += ".to(captures.get(" + (patternList.size() - 1) + ").getPosition())";
            }

            assignProperties(patternList);

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
            super(handlerName, new PropertyList());
            this.application = application;

            List<ExternApplication.Call> queue = new ArrayList<>();

            queue.add(application.getCall());

            while (!queue.isEmpty()) {
                ExternApplication.Call call = queue.remove(0);
                int i = 0;

                for (ExternApplication.Parameter parameter : call.getParameters()) {
                    if (parameter instanceof ExternApplication.Call) {
                        queue.add((ExternApplication.Call) parameter);
                    }
                    else if (parameter instanceof ExternApplication.Reference) {
                        String name = ((ExternApplication.Reference) parameter).getName();
                        Type type = call.getExtern().getParameters().get(i).getType();
                        addProperty(name, type);
                        properties.add(new Property(type, name));
                    }

                    ++i;
                }
            }
        }

        private String buildCall(ExternApplication.Call call) {
            List<String> parameters = new ArrayList<>();

            for (ExternApplication.Parameter parameter : call.getParameters()) {
                if (parameter instanceof ExternApplication.Call) {
                    parameters.add(buildCall((ExternApplication.Call) parameter));
                }
                else if (parameter instanceof ExternApplication.Reference) {
                    parameters.add(((ExternApplication.Reference) parameter).getName());
                }
            }

            return call.getExtern().getName() + "(" + String.join(", ", parameters) + ")";
        }

        @Override void buildBody() {
            assignProperties(application.getPatternList());

            body.write("return " + buildCall(application.getCall()) + ";");
        }
    }

    private class AliasHandlerBuilder extends HandlerBuilder {
        private final Property desiredProperty;
        private final List<Pattern> patternList;

        private AliasHandlerBuilder(String handlerName, Property desiredProperty, List<Pattern> patternList) {
            super(handlerName, new PropertyList());
            this.desiredProperty = desiredProperty;
            this.patternList = patternList;

            addProperty(desiredProperty.getName(), desiredProperty.getType());
            properties.add(desiredProperty);
        }

        @Override void buildBody() {
            assignProperties(patternList);

            body.write("return " + desiredProperty.getName() + ";");
        }
    }
}
