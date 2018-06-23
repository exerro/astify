package astify.GDL;

import java.io.File;
import java.io.IOException;
import java.util.*;

class ASTDefinitionBuilder {
    private final OutputHelper ASTDefinition = new OutputHelper();
    private final BuildConfig buildConfig;
    private final Grammar grammar;

    ASTDefinitionBuilder(Grammar grammar, BuildConfig buildConfig) {
        this.grammar = grammar;
        this.buildConfig = buildConfig;
    }

    void build() {
        ASTDefinition.writeLine("package " + buildConfig.getPackage() + ";");
        ASTDefinition.writeLine();
        ASTDefinition.writeLine("import astify.Capture;");
        ASTDefinition.writeLine("import astify.core.Position;");
        ASTDefinition.writeLine("import astify.token.Token;");
        ASTDefinition.writeLine();
        ASTDefinition.writeLine("import java.util.List;");

        buildASTDefinitions();
    }

    private void buildASTDefinitions() {
        Definition.TypeDefinition grammarDefinition = (Definition.TypeDefinition) grammar.getScope().lookupDefinition(grammar.getName());
        ClassBuilder grammarClassBuilder = new ClassBuilder(grammar.getClassName());
        List<Definition> queued = new ArrayList<>();
        Map<Definition, ClassBuilder> classBuilders = new HashMap<>();

        classBuilders.put(grammarDefinition, grammarClassBuilder);
        setupClass(grammarClassBuilder);
        addProperties(grammarDefinition, grammarClassBuilder);

        for (String s : grammar.getDefinedTypes()) {
            if (!s.equals(grammar.getName())) {
                assert grammar.getScope().lookupDefinition(s) != null;
                queued.add(grammar.getScope().lookupDefinition(s));
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

    void createFiles() throws IOException {
        String basePath = buildConfig.getFullPath() + "/";

        ASTDefinition.writeToFile(new File(basePath + grammar.getClassName() + ".java"));
    }
}
