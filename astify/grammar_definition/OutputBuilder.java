package astify.grammar_definition;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OutputBuilder {
    private final OutputHelper output = new OutputHelper();
    private final Scope scope = new Scope();
    private final ASTifyGrammar grammar;
    private final BuildConfig config;

    OutputBuilder(ASTifyGrammar grammar, BuildConfig config) {
        this.grammar = grammar;
        this.config = config;
        scope.defineNativeTypes();
    }

    Set<String> prepare() throws Exception {
        Set<String> toBuild = new HashSet<>();

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

            if (d != null) {
                scope.define(d);
                toBuild.add(d.getName());
            }
        }

        for (ASTifyGrammar.Definition d : grammar.getDefinitions()) {
            if (d instanceof ASTifyGrammar.AbstractTypeDefinition || d instanceof ASTifyGrammar.TypeDefinition) {
                ASTifyGrammar.NamedPropertyList plist;
                Definition.TypeDefinition definition;

                if (d instanceof ASTifyGrammar.AbstractTypeDefinition) {
                    ASTifyGrammar.AbstractTypeDefinition def = (ASTifyGrammar.AbstractTypeDefinition) d;
                    plist = def.getProperties();
                }
                else {
                    ASTifyGrammar.TypeDefinition def = (ASTifyGrammar.TypeDefinition) d;
                    plist = def.getProperties();
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

        return toBuild;
    }

    void buildTypeDefinition(Definition.TypeDefinition definition) {
        List<String> implementedBy = new ArrayList<>();

        for (Definition.UnionDefinition _implements : definition.getSuperTypes()) {
            implementedBy.add(_implements.getStructName());
        }

        output.writeWord("class");
        output.writef(" %s extends Capture.ObjectCapture", definition.getStructName());

        if (implementedBy.size() > 0) {
            output.write(" implements ");
            output.write(String.join(", ", implementedBy));
        }

        output.enterBlock(); output.writeLine();
    }

    void buildType(Definition.TypeDefinition definition) {
        NameHelper helper = new NameHelper();

        List<String> constructorParameterStrings = new ArrayList<>();

        constructorParameterStrings.add("Position spanningPosition");

        for (Definition.Property property : definition.getProperties()) {
            constructorParameterStrings.add(property.getParameterString());
            helper.define(property.getName());

            // properties
            output.writeLine(property.getDefinitionString());
        }

        output.writeLine();

        // constructor
        output.write(definition.getStructName());
        output.write("(");

        output.write(String.join(", ", constructorParameterStrings));

        output.write(")");
        output.enterBlock();

        output.writeLine();
        output.write("super(spanningPosition);");

        for (Definition.Property property : definition.getProperties()) {
            output.writeLine();
            output.write("this.");
            output.write(property.getName());
            output.writeOperator("=");
            output.write(property.getName());
            output.write(";");
        }

        output.exitBlock();

        // getters
        for (Definition.Property property : definition.getProperties()) {
            output.writeLine();
            output.writeLine();

            output.write(property.getGetterString());
            output.enterBlock(); output.writeLine();

            output.writeWord("return");
            output.writeWord("this.");
            output.write(property.getName());
            output.write(";");

            output.exitBlock();
        }

        // toString()
        output.writeLine();
        output.writeLine();
        output.write("@Override public String toString()");
        output.enterBlock(); output.writeLine();

            String builderName = helper.getName("result");
            output.writeLine("StringBuilder " + builderName + " = new StringBuilder();");
            output.writeLine(builderName + ".append(\"<" + definition.getStructName() + "\");");

            for (Definition.Property property : definition.getProperties()) {
                String propertyToString = property.getName() + ".toString()";

                if (property.getType().isOptional()) {
                    propertyToString = property.getName() + " != null ? " + propertyToString + " : \"null\"";
                }

                if (property.getType().isList()) {
                    propertyToString = "\"[\" + Util.concatList(" + property.getName() + ") + \"]\"";
                }

                output.writeLine(builderName + ".append(\"\\n\" + " + propertyToString + ");");
            }

            output.writeLine(builderName + (definition.getProperties().size() > 0 ? ".append(\"\\n>\");" : ".append(\">\");"));
            output.write("return " + builderName + ".toString();");

        output.exitBlock();
    }

    void build() throws Exception {
        Set<String> toBuild = prepare();

        String grammarName = grammar.getGrammar().getName();
        Definition.TypeDefinition mainDefinition;

        if (toBuild.contains(grammarName)) {
            toBuild.remove(grammarName);

            Definition definition = scope.lookup(grammarName);

            if (definition instanceof Definition.TypeDefinition) {
                mainDefinition = (Definition.TypeDefinition) definition;
            }
            else {
                throw new Exception("expected '" + grammarName + "' to be a type");
            }
        }
        else {
            throw new Exception("expected type '" + grammarName + "' but none found");
        }

        output.writeLine("package " + config.getPackage() + ";");

        output.writeLine();
        output.writeLine("import astify.Capture;");
        output.writeLine("import astify.core.Position;");
        output.writeLine("import astify.core.Positioned;");
        output.writeLine("import astify.grammar_definition.support.Util;");

        output.writeLine();
        output.writeLine("import java.util.ArrayList;");
        output.writeLine("import java.util.Iterator;");
        output.writeLine("import java.util.List;");

        output.writeLine();
        buildTypeDefinition(mainDefinition);
        buildType(mainDefinition);

        for (String typename : toBuild) {
            Definition def = scope.lookup(typename);

            output.writeLine();
            output.writeLine();

            if (def instanceof Definition.TypeDefinition) {
                Definition.TypeDefinition definition = (Definition.TypeDefinition) def;

                output.write("static");
                buildTypeDefinition(definition);
                buildType(definition);
                output.exitBlock();
            }
            else if (def instanceof Definition.UnionDefinition) {
                Definition.UnionDefinition definition = (Definition.UnionDefinition) def;

                output.writef("interface %s extends Positioned", typename);
                output.enterBlock();
                output.exitBlock();
            }
        }

        output.exitBlock();
        output.writeLine();
    }

    String getResult() {
        return output.getResult();
    }

    boolean writeToDirectory() {
        String directoryPath = Paths.get(config.getPath(), config.getPackage().split("\\.")).toString();
        String filename = NameHelper.toUpperCamelCase(grammar.getGrammar().getName()) + ".java";
        return output.writeToFile(Paths.get(directoryPath, filename).toFile());
    }
}
