package astify.grammar_definition;

import java.nio.file.Paths;
import java.util.*;

public class OutputBuilder {
    private final OutputHelper outputAST = new OutputHelper();
    private final OutputHelper outputPatterns = new OutputHelper();
    private final Scope scope = new Scope();
    private final ASTifyGrammar grammar;
    private final BuildConfig config;

    OutputBuilder(ASTifyGrammar grammar, BuildConfig config) {
        this.grammar = grammar;
        this.config = config;
        scope.defineNativeTypes();
    }

    private void validatePatternList(ASTifyGrammar.PatternList patternList, Definition.TypeDefinition definition) throws Exception {
        Set<String> requiredPropertyNames = new HashSet<>();
        Set<String> referredPropertyNames = new HashSet<>();
        List<List<ASTifyGrammar.Pattern>> queue = new ArrayList<>();
        boolean root = true;

        for (Definition.Property property : definition.getProperties()) {
            if (!property.getType().isOptional() && !property.getType().isList()) {
                requiredPropertyNames.add(property.getRawName());
            }
        }

        queue.add(patternList.getPatterns());

        while (!queue.isEmpty()) {
            List<ASTifyGrammar.Pattern> patterns = queue.remove(0);

            for (ASTifyGrammar.Pattern pattern : patterns) {
                if (pattern instanceof ASTifyGrammar.ParameterReference) {
                    String parameterName = ((ASTifyGrammar.ParameterReference) pattern).getParameter();

                    if (definition.getProperty(parameterName) == null) throw new Exception("undefined reference to parameter '" + parameterName + "' in " + definition.getName());
                    if (referredPropertyNames.contains(parameterName)) throw new Exception("multiple references to parameter '" + parameterName + "' in " + definition.getName());
                    if (root) requiredPropertyNames.remove(parameterName);

                    referredPropertyNames.add(parameterName);
                }
            }

            root = false;
        }

        if (!requiredPropertyNames.isEmpty()) {
            throw new Exception("undefined parameters " + String.join(", ", requiredPropertyNames) + " in " + definition.getName());
        }
    }

    private List<String> prepare() throws Exception {
        List<String> toBuild = new ArrayList<>();

        for (ASTifyGrammar.Definition definition : grammar.getDefinitions()) {
            Definition d = null;

            if (definition instanceof ASTifyGrammar.AbstractTypeDefinition) {
                ASTifyGrammar.AbstractTypeDefinition def = (ASTifyGrammar.AbstractTypeDefinition) definition;
                d = new Definition.TypeDefinition(def.getProperties().getName());
            }
            else if (definition instanceof ASTifyGrammar.TypeDefinition) {
                ASTifyGrammar.TypeDefinition def = (ASTifyGrammar.TypeDefinition) definition;
                d = new Definition.TypeDefinition(def.getProperties().getName());
            }
            else if (definition instanceof ASTifyGrammar.Union) {
                ASTifyGrammar.Union def = (ASTifyGrammar.Union) definition;
                d = new Definition.UnionDefinition(def.getTypename());
            }
            else {
                assert false : "oh no";
            }

            scope.define(d);
            toBuild.add(d.getName());
        }

        for (ASTifyGrammar.Definition d : grammar.getDefinitions()) {
            if (d instanceof ASTifyGrammar.AbstractTypeDefinition || d instanceof ASTifyGrammar.TypeDefinition) {
                ASTifyGrammar.NamedPropertyList plist;
                Definition.TypeDefinition definition;
                List<ASTifyGrammar.PatternList> patternLists = new ArrayList<>();

                if (d instanceof ASTifyGrammar.AbstractTypeDefinition) {
                    ASTifyGrammar.AbstractTypeDefinition def = (ASTifyGrammar.AbstractTypeDefinition) d;
                    plist = def.getProperties();
                }
                else {
                    ASTifyGrammar.TypeDefinition def = (ASTifyGrammar.TypeDefinition) d;
                    plist = def.getProperties();
                    patternLists = def.getPatterns();
                }

                definition = (Definition.TypeDefinition) scope.lookup(plist.getName());

                for (ASTifyGrammar.TypedName propertyDescriptor : plist.getProperties()) {
                    ASTifyGrammar.Type propertyTypeDescriptor = propertyDescriptor.getType();
                    String propertyName = propertyDescriptor.getName();

                    if (scope.exists(propertyTypeDescriptor.getName())) {
                        Type propertyType = new Type(scope.lookup(propertyTypeDescriptor.getName()), propertyTypeDescriptor.isOptional(), propertyTypeDescriptor.isList());
                        Definition.Property property = new Definition.Property(propertyType, propertyName);
                        definition.addProperty(property);
                    }
                    else {
                        throw new Exception("cannot resolve type '" + propertyTypeDescriptor.getName() + "'\n" + propertyTypeDescriptor.getPosition().getLineAndCaret());
                    }
                }

                for (ASTifyGrammar.PatternList patternList : patternLists) {
                    validatePatternList(patternList, definition);
                    definition.addPatternList(patternList);
                }
            }
            else if (d instanceof ASTifyGrammar.Union) {
                ASTifyGrammar.Union def = (ASTifyGrammar.Union) d;
                Definition.UnionDefinition definition = (Definition.UnionDefinition) scope.lookup(def.getTypename());

                for (String subtypeName : def.getSubtypes()) {
                    if (scope.exists(subtypeName)) {
                        Definition subtypeDefinition = scope.lookup(subtypeName);

                        if (subtypeDefinition instanceof Definition.TypeDefinition) {
                            definition.addSubType((Definition.TypeDefinition) subtypeDefinition);
                            ((Definition.TypeDefinition) subtypeDefinition).addSuperType(definition);
                        }
                        else {
                            throw new Exception("cannot have subtype '" + subtypeName + "': not a valid type");
                        }
                    }
                    else {
                        throw new Exception("no such subtype '" + subtypeName + "' for union " + def.getTypename());
                    }
                }
            }
            else {
                assert false : "oh no";
            }
        }

        toBuild.sort(String::compareTo);

        return toBuild;
    }

    private void validate(List<String> toBuild) throws Exception {
        String grammarName = grammar.getGrammar().getName();

        if (toBuild.contains(grammarName)) {
            if (!(scope.lookup(grammarName) instanceof Definition.TypeDefinition)) {
                throw new Exception("expected '" + grammarName + "' to be a type");
            }
        }
        else {
            throw new Exception("expected type '" + grammarName + "' but none found");
        }
    }

    private void buildTypeDefinition(Definition.TypeDefinition definition) {
        List<String> implementedBy = new ArrayList<>();

        for (Definition.UnionDefinition _implements : definition.getSuperTypes()) {
            implementedBy.add(_implements.getStructName());
        }

        outputAST.writeWord("class");
        outputAST.writef(" %s extends Capture.ObjectCapture", definition.getStructName());

        if (implementedBy.size() > 0) {
            outputAST.write(" implements ");
            outputAST.write(String.join(", ", implementedBy));
        }

        outputAST.enterBlock(); outputAST.writeLine();
    }

    private void buildType(Definition.TypeDefinition definition) {
        NameHelper helper = new NameHelper();

        List<String> constructorParameterStrings = new ArrayList<>();

        constructorParameterStrings.add("Position spanningPosition");

        for (Definition.Property property : definition.getProperties()) {
            constructorParameterStrings.add(property.getParameterString());
            helper.define(property.getName());

            // properties
            outputAST.writeLine(property.getDefinitionString());
        }

        outputAST.writeLine();

        // constructor
        outputAST.write(definition.getStructName());
        outputAST.write("(");

        outputAST.write(String.join(", ", constructorParameterStrings));

        outputAST.write(")");
        outputAST.enterBlock();

        outputAST.writeLine();
        outputAST.write("super(spanningPosition);");

        for (Definition.Property property : definition.getProperties()) {
            if (!property.getType().isOptional()) {
                outputAST.writeLine();
                outputAST.writeWord("assert");
                outputAST.writeWord(property.getName());
                outputAST.writeOperator("!=");
                outputAST.write("null");
                outputAST.write(" : \"" + property.getName() + " is null\";");
            }
        }

        for (Definition.Property property : definition.getProperties()) {
            outputAST.writeLine();
            outputAST.write("this.");
            outputAST.write(property.getName());
            outputAST.writeOperator("=");
            outputAST.write(property.getName());
            outputAST.write(";");
        }

        outputAST.exitBlock();

        // getters
        for (Definition.Property property : definition.getProperties()) {
            outputAST.writeLine();
            outputAST.writeLine();

            outputAST.write(property.getGetterString());
            outputAST.enterBlock(); outputAST.writeLine();

            outputAST.writeWord("return");
            outputAST.writeWord(property.getName());
            outputAST.write(";");

            outputAST.exitBlock();
        }

        // toString()
        outputAST.writeLine();
        outputAST.writeLine();
        outputAST.write("@Override public String toString()");
        outputAST.enterBlock(); outputAST.writeLine();

            String builderName = helper.getName("result");
            outputAST.writeLine("return \"<" + definition.getStructName() + "\"");

            for (Definition.Property property : definition.getProperties()) {
                String propertyToString = property.getName() + ".toString()";

                if (property.getType().isNative(Definition.NativeDefinition.NativeType.String)) {
                    propertyToString = property.getName();
                }

                if (property.getType().isOptional()) {
                    propertyToString = property.getName() + " != null ? " + propertyToString + " : \"null\"";
                }

                if (property.getType().isList()) {
                    propertyToString = "\"[\" + Util.concatList(" + property.getName() + ") + \"]\"";
                }

                outputAST.writeLine("+ " + ("\"\\n\\t" + property.getName() + ": \" + " + propertyToString).replace("\" + \"", ""));
            }

            outputAST.write((definition.getProperties().size() > 0 ? "+ \"\\n>\";" : "+ \">\";"));

        outputAST.exitBlock();

        // equals()
        String paramName = helper.getName("object");
        String objectName = helper.getName(paramName + definition.getStructName());
        List<String> propertyEqualityChecks = new ArrayList<>();

        for (Definition.Property property : definition.getProperties()) {
            propertyEqualityChecks.add(property.getName() + ".equals(" + objectName + "." + property.getName() + ")");
        }

        outputAST.writeLine();
        outputAST.writeLine();
        outputAST.write("@Override public boolean equals(Object " + paramName + ")");
        outputAST.enterBlock(); outputAST.writeLine();

            outputAST.writeLine("if (!(" + paramName + " instanceof " + definition.getStructName() + ")) return false;");
            outputAST.writeLine(definition.getStructName() + " " + objectName + " = (" + definition.getStructName() + ") object;");

            for (int i = 0; i < propertyEqualityChecks.size() - 1; ++i) {
                outputAST.writeLine("if (!(" + propertyEqualityChecks.get(i) + ")) return false;");
            }

            outputAST.write(propertyEqualityChecks.size() == 0 ? "return true;" : "return " + propertyEqualityChecks.get(propertyEqualityChecks.size() - 1) + ";");

        outputAST.exitBlock();

        // hashCode()
        List<String> propertyNames = new ArrayList<>();

        outputAST.writeLine();
        outputAST.writeLine();
        outputAST.write("@Override public int hashCode()");
        outputAST.enterBlock(); outputAST.writeLine();

            for (Definition.Property property : definition.getProperties()) {
                propertyNames.add(property.getName());
            }

            outputAST.write(propertyNames.size() > 0 ? "return hash(" + String.join(", ", propertyNames) + ");" : "return 0;");

        outputAST.exitBlock();
    }

    private void buildAST(List<String> toBuild) {
        String grammarName = grammar.getGrammar().getName();
        Definition.TypeDefinition mainDefinition = (Definition.TypeDefinition) scope.lookup(grammarName);

        outputAST.writeLine("package " + config.getPackage() + ";");

        outputAST.writeLine();
        outputAST.writeLine("import astify.Capture;");
        outputAST.writeLine("import astify.core.Position;");
        outputAST.writeLine("import astify.core.Positioned;");
        outputAST.writeLine("import astify.grammar_definition.support.Util;");

        outputAST.writeLine();
        outputAST.writeLine("import java.util.ArrayList;");
        outputAST.writeLine("import java.util.Iterator;");
        outputAST.writeLine("import java.util.List;");

        outputAST.writeLine();
        outputAST.writeLine("import static java.util.Objects.hash;");

        outputAST.writeLine();
        buildTypeDefinition(mainDefinition);
        buildType(mainDefinition);

        for (String typename : toBuild) {
            if (typename.equals(grammarName)) continue;

            Definition def = scope.lookup(typename);

            outputAST.writeLine();
            outputAST.writeLine();

            if (def instanceof Definition.TypeDefinition) {
                Definition.TypeDefinition definition = (Definition.TypeDefinition) def;

                outputAST.write("static");
                buildTypeDefinition(definition);
                buildType(definition);
                outputAST.exitBlock();
            }
            else if (def instanceof Definition.UnionDefinition) {
                Definition.UnionDefinition definition = (Definition.UnionDefinition) def;

                outputAST.writef("interface %s extends Positioned", typename);
                outputAST.enterBlock();
                outputAST.exitBlock();
            }
        }

        outputAST.exitBlock();
        outputAST.writeLine();
    }

    private void buildPattern(ASTifyGrammar.Pattern pattern, Definition.TypeDefinition definition) throws Exception {
        if (pattern instanceof ASTifyGrammar.ParameterReference) {
            String parameterName = ((ASTifyGrammar.ParameterReference) pattern).getParameter();
            List<ASTifyGrammar.Pattern> listDelimiter = new ArrayList<>(), delimiter = ((ASTifyGrammar.ParameterReference) pattern).getDelimiter();
            Definition.Property property = definition.getProperty(parameterName);

            if (property.getType().isList()) {
                outputPatterns.write(delimiter.isEmpty() ? "list(" : "delim(");
                listDelimiter = delimiter;
                delimiter = new ArrayList<>();
            }

            if (property.getType().isNative(Definition.NativeDefinition.NativeType.String)) {
                if (delimiter.size() == 0) {
                    outputPatterns.write("token(Word)");
                }
                else if (delimiter.size() == 1) {
                    buildPattern(delimiter.get(0), definition);
                }
                else {
                    throw new Exception("unexpected list of patterns in string parameter qualifier");
                }
            }
            else if (property.getType().isNative(Definition.NativeDefinition.NativeType.Boolean)) {
                outputPatterns.write("optional(sequence(");
                outputPatterns.indent();
                buildPatternSequence(delimiter, definition);
                outputPatterns.unindent();
                outputPatterns.writeLine();
                outputPatterns.write("))");
            }
            else if (property.getType().isNative()) {
                assert false;
            }
            else {
                outputPatterns.writef("ref(\"%s\")", property.getType().getDefinition().getPatternName());
            }

            if (property.getType().isList()) {
                if (listDelimiter.isEmpty()) {
                    outputPatterns.writef(")", property.getType().getDefinition().getPatternName());
                }
                else {
                    outputPatterns.writef(", ", property.getType().getDefinition().getPatternName());

                    if (listDelimiter.size() == 1) {
                        buildPattern(listDelimiter.get(0), definition);
                        outputPatterns.write(")");
                    }
                    else {
                        outputPatterns.write("sequence(");
                        outputPatterns.indent();
                        buildPattern(listDelimiter.get(0), definition);
                        outputPatterns.unindent();
                        outputPatterns.writeLine();
                        outputPatterns.write(")");
                    }
                }
            }
        }
        else if (pattern instanceof ASTifyGrammar.Terminal) {
            boolean isWord = true;
            String text = ((ASTifyGrammar.Terminal) pattern).getTerminal();

            for (int i = 1; i < text.length() - 1; ++i) {
                if (!Character.isLetterOrDigit(text.charAt(i))) {
                    isWord = false;
                    break;
                }
            }

            outputPatterns.write(isWord ? "keyword" : "operator");
            outputPatterns.write("(" + text.replace("'", "\"") + ")");
        }
        else if (pattern instanceof ASTifyGrammar.Optional) {
            outputPatterns.write("optional(sequence(");
            outputPatterns.indent();
            buildPatternSequence(((ASTifyGrammar.Optional) pattern).getPatterns(), definition);
            outputPatterns.unindent();
            outputPatterns.writeLine();
            outputPatterns.write("))");
        }
        else {
            assert false;
        }
    }

    private void buildPatternSequence(List<ASTifyGrammar.Pattern> list, Definition.TypeDefinition definition) throws Exception {
        boolean first = true;

        for (ASTifyGrammar.Pattern pattern : list) {
            if (first) {
                first = false;
            }
            else {
                outputPatterns.write(",");
            }

            outputPatterns.writeLine();

            buildPattern(pattern, definition);
        }
    }

    private void buildPatternBuilder(List<String> toBuild) throws Exception {
        String className = NameHelper.toUpperCamelCase(grammar.getGrammar().getName()) + "PatternBuilder";

        outputPatterns.writeLine("package " + config.getPackage() + ";");

        outputPatterns.writeLine();
        outputPatterns.writeLine("import astify.Capture;");
        outputPatterns.writeLine("import astify.Pattern;");
        outputPatterns.writeLine("import astify.PatternBuilder;");

        outputPatterns.writeLine();
        outputPatterns.writef("public class %s extends PatternBuilder", className);
        outputPatterns.enterBlock(); outputPatterns.writeLine();

            outputPatterns.write("public " + className + "()");
            outputPatterns.enterBlock(); outputPatterns.writeLine();

                outputPatterns.write("super();");

                for (String definitionName : toBuild) {
                    Definition def = scope.lookup(definitionName);

                    if (def instanceof Definition.UnionDefinition) {
                        Definition.UnionDefinition definition = (Definition.UnionDefinition) def;
                        List<String> refs = new ArrayList<>();

                        for (Definition.TypeDefinition subtype : definition.getSubtypes()) {
                            refs.add("ref(\"" + subtype.getPatternName() + "\")");
                        }

                        outputPatterns.writeLine();
                        outputPatterns.writeLine();
                        outputPatterns.writef("defineInline(\"%s\", one_of(\n\t%s\n));", definition.getPatternName(), String.join(",\n\t", refs));
                    }
                    else if (def instanceof Definition.TypeDefinition) {
                        Definition.TypeDefinition definition = (Definition.TypeDefinition) def;
                        List<ASTifyGrammar.PatternList> patternLists = definition.getPatternLists();
                        String patternName = NameHelper.toLowerLispCase(definition.getPatternName());

                        if (patternLists.size() == 0) continue;

                        outputPatterns.writeLine();
                        outputPatterns.writeLine();

                        if (patternLists.size() == 1) {
                            outputPatterns.writef("sequence(\"%s\", /*callback, */", patternName);
                            outputPatterns.indent();
                            buildPatternSequence(patternLists.get(0).getPatterns(), definition);
                            outputPatterns.unindent();
                            outputPatterns.writeLine();
                            outputPatterns.write(");");

                        }
                        else {
                            List<String> patternRefs = new ArrayList<>();

                            for (int i = 0; i < patternLists.size(); ++i) {
                                String subPatternName = "_" + patternName + (i + 1);

                                patternRefs.add("ref(\"" + subPatternName + "\")");

                                outputPatterns.writef("sequence(\"%s\", /*callback, */", subPatternName);
                                outputPatterns.indent();
                                buildPatternSequence(patternLists.get(i).getPatterns(), definition);
                                outputPatterns.unindent();
                                outputPatterns.writeLine();
                                outputPatterns.write(");");
                                outputPatterns.writeLine();
                                outputPatterns.writeLine();
                            }

                            outputPatterns.writef("define(\"%s\", one_of(\n\t%s\n));", patternName, String.join(",\n\t", patternRefs));
                        }
                    }
                }

            outputPatterns.exitBlock();

            outputPatterns.writeLine();
            outputPatterns.writeLine();
            outputPatterns.write("@Override public Pattern getMain()");
            outputPatterns.enterBlock(); outputPatterns.writeLine();

                outputPatterns.writef("return lookup(\"%s\");", NameHelper.toLowerLispCase(grammar.getGrammar().getName()));

            outputPatterns.exitBlock();

        outputPatterns.exitBlock();
    }

    void build() throws Exception {
        List<String> toBuild = prepare();
        validate(toBuild);
        buildPatternBuilder(toBuild);
        buildAST(toBuild);
    }

    String getResult() {
        return outputAST.getResult();
    }

    boolean writeToDirectory() {
        String directoryPath = Paths.get(config.getPath(), config.getPackage().split("\\.")).toString();
        String ASTFilename = NameHelper.toUpperCamelCase(grammar.getGrammar().getName()) + ".java";
        String PatternBuilderFilename = NameHelper.toUpperCamelCase(grammar.getGrammar().getName()) + "PatternBuilder.java";

        return outputAST.writeToFile(Paths.get(directoryPath, ASTFilename).toFile())
             & outputPatterns.writeToFile(Paths.get(directoryPath, PatternBuilderFilename).toFile());
    }
}
