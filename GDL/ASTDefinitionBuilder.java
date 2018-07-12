package GDL;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class ASTDefinitionBuilder {
    private final OutputHelper ASTDefinition = new OutputHelper();
    private final BuildConfig buildConfig;
    private final Grammar grammar;

    ASTDefinitionBuilder(Grammar grammar, BuildConfig buildConfig) {
        this.grammar = grammar;
        this.buildConfig = buildConfig;
    }

    void createFiles() throws IOException {
        String basePath = buildConfig.getFullPath() + "/";

        ASTDefinition.writeToFile(new File(basePath + grammar.getClassName() + ".java"));
    }

    void build() {
        ASTDefinition.writeLine("package " + buildConfig.getPackage() + ";");
        ASTDefinition.writeLine();
        ASTDefinition.writeLine("import static java.util.Objects.hash;");
        ASTDefinition.writeLine("import java.util.List;");

        buildASTDefinitions();
    }

    private void buildASTDefinitions() {
        Type.ObjectType grammarType = (Type.ObjectType) grammar.getScope().lookupType(grammar.getName());
        Builder.ClassBuilder builder = new Builder.ClassBuilder(grammar.getClassName());
        Map<Type, Builder> builders = new HashMap<>();

        builders.put(grammarType, builder);

        for (Definition definition : grammar.getScope().values()) {
            if (definition instanceof Definition.TypeDefinition) {
                Type type = ((Definition.TypeDefinition) definition).getType();

                if (type != grammarType) {
                    Builder subBuilder = null;

                    if (type instanceof Type.ObjectType) {
                        builders.put(type, subBuilder = new Builder.ClassBuilder(type.getReferenceName()));
                        ((Builder.ClassBuilder) subBuilder).setStatic(true);
                    }
                    else if (type instanceof Type.Union) {
                        builders.put(type, subBuilder = new Builder.InterfaceBuilder(type.getReferenceName()));
                    }

                    if (subBuilder != null) {
                        builder.addSubtype(subBuilder);
                    }
                }
            }
        }

        for (Type t : builders.keySet()) {
            if (t instanceof Type.ObjectType) {
                loadClassBuilder((Builder.ClassBuilder) builders.get(t), (Type.ObjectType) t);
            }
            else if (t instanceof Type.Union) {
                loadInterfaceBuilder((Builder.InterfaceBuilder) builders.get(t), (Type.Union) t, builders);
            }
        }

        ASTDefinition.ensureLines(2);
        builder.buildTo(ASTDefinition);
    }

    private void loadClassBuilder(Builder.ClassBuilder builder, Type.ObjectType type) {
        builder.setAccess(buildConfig.getClassAccess());
        builder.setGetterAccess(buildConfig.getGetterAccess());
        builder.setConstructorAccess(buildConfig.getConstructorAccess());
        builder.setExtends("astify.Capture.ObjectCapture");
        builder.addSuperField("astify.core.Position", "spanningPosition");

        for (Iterator<Property> it = type.getProperties().iterator(); it.hasNext(); ) {
            Property property = it.next();

            builder.addField(toString(property.getType()), property.getName(), isOptionalType(property.getType()));
        }
    }

    private void loadInterfaceBuilder(Builder.InterfaceBuilder builder, Type.Union type, Map<Type, Builder> builders) {
        builder.setAccess(buildConfig.getClassAccess());
        builder.setGetterAccess(buildConfig.getGetterAccess());
        builder.addExtends("astify.core.Positioned");

        for (Type subtype : type.getRawMembers()) {
            Builder subBuilder = builders.get(subtype);

            if (subBuilder instanceof Builder.ClassBuilder) {
                ((Builder.ClassBuilder) subBuilder).addImplements(type.getReferenceName());
            }
            else if (subBuilder instanceof Builder.InterfaceBuilder) {
                ((Builder.InterfaceBuilder) subBuilder).addExtends(type.getReferenceName());
            }
        }

        Set<Property> properties = type.getSharedProperties();

        for (Property property : properties) {
            builder.addAbstractGetter(toString(property.getType()), property.getName());
        }
    }

    private String toString(Type type) {
        if (type instanceof Type.ListType) {
            return "List<" + toString(((Type.ListType) type).getType()) + ">";
        }
        if (type instanceof Type.OptionalType) {
            return toString(((Type.OptionalType) type).getType());
        }
        if (type instanceof Type.TokenType) {
            return "astify.token.Token";
        }

        return type.getReferenceName();
    }

    private boolean isOptionalType(Type type) {
        return type instanceof Type.OptionalType;
    }
}
