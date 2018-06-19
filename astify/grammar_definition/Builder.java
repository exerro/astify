package astify.grammar_definition;

import astify.grammar_definition.support.ClassBuilder;
import astify.grammar_definition.support.NameHelper;
import astify.grammar_definition.support.OutputHelper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Builder {
    private final Scope scope = new Scope();
    private final OutputHelper ASTDefinition = new OutputHelper();
    private final OutputHelper patternBuilder = new OutputHelper();
    private final ClassBuilder patternBuilderClass;
    private final List<String> definedTypes = new ArrayList<>();
    private final List<String> builtPatterns = new ArrayList<>();
    private final String grammarName;
    private final BuildConfig buildConfig;

    public Builder(String grammarName, BuildConfig buildConfig) {
        this.grammarName = grammarName;
        this.buildConfig = buildConfig;
        scope.defineNativeTypes();

        patternBuilderClass = new ClassBuilder(NameHelper.toUpperCamelCase(grammarName) + "PatternBuilder");

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
        patternBuilder.writeLine("import astify.PatternBuilder;");
        patternBuilder.writeLine("import astify.core.Position;");
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

            helper.write("public " + patternBuilderClass.getClassName() + "()");
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

            helper.write("return lookup(\"" + grammarName + "\");");

            helper.exitBlock();
        });

    }

    public void registerDefinition(ASTifyGrammar.Definition definition) {
        Definition d;

        if (definition instanceof ASTifyGrammar.AbstractTypeDefinition) {
            d = new Definition.TypeDefinition(((ASTifyGrammar.AbstractTypeDefinition) definition).getProperties().getName().getValue());
        }
        else if (definition instanceof ASTifyGrammar.TypeDefinition) {
            d = new Definition.TypeDefinition(((ASTifyGrammar.TypeDefinition) definition).getProperties().getName().getValue());
        }
        else if (definition instanceof ASTifyGrammar.Union) {
            d = new Definition.UnionDefinition(((ASTifyGrammar.Union) definition).getTypename().getValue());
        }
        else {
            throw new Error("what" + definition.getClass().getName());
        }

        scope.define(new Type.DefinedType(d));
        definedTypes.add(d.getName());
    }

    public void buildDefinition(ASTifyGrammar.Definition sourceDefinition) {
        String name = null;

        if (sourceDefinition instanceof ASTifyGrammar.AbstractTypeDefinition) name = ((ASTifyGrammar.AbstractTypeDefinition) sourceDefinition).getProperties().getName().getValue();
        else if (sourceDefinition instanceof ASTifyGrammar.TypeDefinition) name = ((ASTifyGrammar.TypeDefinition) sourceDefinition).getProperties().getName().getValue();
        else if (sourceDefinition instanceof ASTifyGrammar.Union) name = ((ASTifyGrammar.Union) sourceDefinition).getTypename().getValue();

        if (name == null) throw new Error("what " + sourceDefinition.getClass().getName());

        assert definedTypes.contains(name);

        Definition d = scope.lookupDefinition(name);

        if (d == null) throw new Error("TODO");

        if (sourceDefinition instanceof ASTifyGrammar.AbstractTypeDefinition) {
            ASTifyGrammar.AbstractTypeDefinition def = (ASTifyGrammar.AbstractTypeDefinition) sourceDefinition;
            Definition.TypeDefinition definition = (Definition.TypeDefinition) d;

            addTypeProperties(definition, def.getProperties());
        }
        else if (sourceDefinition instanceof ASTifyGrammar.TypeDefinition) {
            ASTifyGrammar.TypeDefinition def = (ASTifyGrammar.TypeDefinition) sourceDefinition;
            Definition.TypeDefinition definition = (Definition.TypeDefinition) d;

            addTypeProperties(definition, def.getProperties());

            for (ASTifyGrammar.PatternList patternList : def.getPatterns()) {
                definition.addPattern(patternList, scope);
            }
        }
        else if (sourceDefinition instanceof ASTifyGrammar.Union) {
            ASTifyGrammar.Union def = (ASTifyGrammar.Union) sourceDefinition;
            Definition.UnionDefinition definition = (Definition.UnionDefinition) d;
            StringBuilder patternString = new StringBuilder("define(\"" + definition.getPatternName() + "\", one_of(");

            for (astify.token.Token t : def.getSubtypes()) {
                Definition subtypeDefinition = scope.lookupDefinition(t.getValue());
                assert subtypeDefinition != null;
                definition.addMember(subtypeDefinition);
                patternString.append("\n\tref(\"").append(subtypeDefinition.getPatternName()).append("\"),");
            }

            builtPatterns.add(patternString.substring(0, patternString.length() - 1) + "\n));");
        }
    }

    public void build() {
        assert definedTypes.contains(NameHelper.toUpperCamelCase(grammarName));
        assert scope.lookupDefinition(NameHelper.toUpperCamelCase(grammarName)) instanceof Definition.TypeDefinition;

        Definition.TypeDefinition grammarDefinition = (Definition.TypeDefinition) scope.lookupDefinition(NameHelper.toUpperCamelCase(grammarName));

        for (String name : definedTypes) {
            if (!name.equals(NameHelper.toUpperCamelCase(grammarName))) {
                grammarDefinition.getClassBuilder().addClass(scope.lookupDefinition(name).getClassBuilder());
            }
        }

        scope.lookupDefinition(NameHelper.toUpperCamelCase(grammarName)).buildTo(ASTDefinition);

        buildPatternDefinitions();
        buildPatternHandlers();

        patternBuilderClass.buildTo(patternBuilder);
    }

    public void createFiles() throws IOException {
        FileWriter ASTDefinitionWriter = new FileWriter(buildConfig.getFullPath() + "/" + NameHelper.toUpperCamelCase(grammarName) + ".java");
        ASTDefinitionWriter.write(ASTDefinition.getResult());
        ASTDefinitionWriter.flush();
        ASTDefinitionWriter.close();

        FileWriter patternBuilderWriter = new FileWriter(buildConfig.getFullPath() + "/" + patternBuilderClass.getClassName() + ".java");
        patternBuilderWriter.write(patternBuilder.getResult());
        patternBuilderWriter.flush();
        patternBuilderWriter.close();
    }

    private void addTypeProperties(Definition.TypeDefinition definition, ASTifyGrammar.NamedPropertyList properties) {
        for (ASTifyGrammar.TypedName property : properties.getProperties()) {
            Type t = scope.lookup(property.getType().getName().getValue());
            definition.addProperty(new Property(t, property.getName().getValue(), property.getType().isLst(), property.getType().isOptional()));
        }
    }

    private void buildPatternDefinitions() {
        for (String typeName : definedTypes) {
            if (scope.lookupDefinition(typeName) instanceof Definition.TypeDefinition) {
                Definition.TypeDefinition definition = (Definition.TypeDefinition) scope.lookupDefinition(typeName);
                List<List<Pattern>> patternLists = definition.getPatternLists();

                if (patternLists.size() == 1) {
                    builtPatterns.add(buildPatternList(definition.getPatternName(), definition.getCallbackName(), patternLists.get(0)));
                }
                else {
                    List<String> refs = new ArrayList<>();

                    for (int i = 0; i < patternLists.size(); ++i) {
                        String ref = definition.getPatternName() + "(" + i + ")";
                        builtPatterns.add(buildPatternList(ref, definition.getCallbackName() + i, patternLists.get(i)));
                        refs.add("ref(\"" + ref + "\")");
                    }

                    builtPatterns.add("defineInline(\"" + definition.getPatternName() + "\", one_of(\n\t" + String.join(",\n\t", refs) + "\n));");
                }
            }
        }
    }

    private String buildPatternList(String name, String callbackName, List<Pattern> patternList) {
        return "sequence(\"" + name + "\", this::" + callbackName + ",\n\t"
                + String.join(",\n\t", Pattern.map(patternList, (pat) -> pat.getPatternBuilderTerm().replace("\n", "\n\t")))
                + "\n);";
    }

    private void buildPatternHandlers() {
        for (String typeName : definedTypes) {
            if (scope.lookupDefinition(typeName) instanceof Definition.TypeDefinition) {
                Definition.TypeDefinition definition = (Definition.TypeDefinition) scope.lookupDefinition(typeName);
                List<List<Pattern>> patternLists = definition.getPatternLists();

                if (patternLists.size() == 1) {
                    patternBuilderClass.addMethod(generateCreatorCallback(definition.getCallbackName(), definition, patternLists.get(0)));
                } else {
                    for (int i = 0; i < patternLists.size(); ++i) {
                        patternBuilderClass.addMethod(generateCreatorCallback(definition.getCallbackName() + i, definition, patternLists.get(i)));
                    }
                }
            }
        }
    }

    private ClassBuilder.Builder generateCreatorCallback(String name, Definition.TypeDefinition definition, List<Pattern> patternList) {
        OutputHelper content = new OutputHelper();
        Definition.TypeDefinition grammarDefinition = (Definition.TypeDefinition) scope.lookupDefinition(NameHelper.toUpperCamelCase(grammarName));
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

                    for (Pattern subPattern : ((Pattern.Optional) pattern).getPatterns()) {
                        if (subPattern instanceof Pattern.Matcher) {
                            content.writeLine("// sub-matcher " + ((Pattern.Matcher) subPattern).getSource().toString() + " -> " + ((Pattern.Matcher) subPattern).getTarget());
                            content.write(getMatcherSetString("subCaptures.get(0)", (Pattern.Matcher) subPattern, definition));
                        }
                    }

                    content.exitBlock();
                    content.writeLine();
                    content.writeLine();
                }
                else if (pattern instanceof Pattern.Matcher) {
                    content.ensureLines(1);
                    content.writeLine("// matcher " + ((Pattern.Matcher) pattern).getSource().toString() + " -> " + ((Pattern.Matcher) pattern).getTarget());
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
            helper.write("private Capture " + name + "(List<Capture> captures)");
            helper.enterBlock();

                helper.write(content.getResult());

            helper.exitBlock();
        };
    }

    private String getMatcherSetString(String sourceValue, Pattern.Matcher source, Definition.TypeDefinition definition) {
        String propertyName = source.getTarget();
        Property property = definition.getProperty(propertyName);
        Function<String, String> getValue;

        if (property == null) throw new Error("TODO");

        if (property.getType() instanceof Type.DefinedType) {
            getValue = (value) -> "(" + getPrefixedClassName(property.getType().getReferenceName()) + ") " + value;
        }
        else if (property.getType() instanceof Type.TokenType) {
            getValue = (value) -> "((Capture.TokenCapture) " + value + ").getToken()";
        }
        else if (property.getType() instanceof Type.BuiltinType) {
            getValue = (value) -> "!(" + value + " instanceof Capture.EmptyCapture)";
        }
        else throw new Error("what");

        if (property.isList()) {
            if (source.getSource() instanceof Pattern.ListType) {
                return "for (Iterator it = ((Capture.ListCapture) " + sourceValue + ").iterator(); it.hasNext(); ) {\n\t"
                        + propertyName + ".add(" + getValue.apply("it.next()") + ");\n"
                        + "}";
            }
            else {
                return propertyName + ".add(" + getValue.apply(sourceValue) + ");";
            }
        }
        else {
            if (source.getSource() instanceof Pattern.ListType) {
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
    }
}
