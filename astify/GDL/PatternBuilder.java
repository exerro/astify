package astify.GDL;

import java.io.File;
import java.io.IOException;
import java.util.*;

class PatternBuilder {
    private final OutputHelper patternBuilder = new OutputHelper();
    private final ClassBuilder patternBuilderClass;
    private final List<String> builtPatterns = new ArrayList<>();
    private final BuildConfig buildConfig;
    private final Grammar grammar;

    PatternBuilder(Grammar grammar, BuildConfig buildConfig) {
        this.grammar = grammar;
        this.buildConfig = buildConfig;
        this.patternBuilderClass = new ClassBuilder(grammar.getClassName() + "PatternBuilder");
    }

    void createFiles() throws IOException {
        String basePath = buildConfig.getFullPath() + "/";

        patternBuilder.writeToFile(new File(basePath + grammar.getClassName() + "PatternBuilder.java"));
    }

    void build() {
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

            helper.write("return lookup(\"" + NameHelper.toLowerLispCase(grammar.getName()) + "\");");

            helper.exitBlock();
        });

        // buildPatterns();
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
}
