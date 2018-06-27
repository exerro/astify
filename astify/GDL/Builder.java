package astify.GDL;

import java.util.ArrayList;
import java.util.List;

public abstract class Builder {
    enum AccessModifier {
        Default,
        Protected,
        Public
    }

    abstract void buildTo(OutputHelper helper);

    protected static String concat(List<String> terms, String sep) {
        if (terms.size() == 0) return "";

        StringBuilder s = new StringBuilder(terms.get(0));

        for (int i = 1; i < terms.size(); ++i) {
            s.append(sep);
            s.append(terms.get(i));
        }

        return s.toString();
    }

    protected static String concat(List<String> terms) {
        return concat(terms, ", ");
    }

    protected static String toString(AccessModifier modifier) {
        switch (modifier) {
            case Protected: return "protected ";
            case Default: return "";
            case Public: return "public ";
        }
        return "";
    }

    static class InterfaceBuilder extends Builder {
        private AccessModifier interfaceAccess;
        private final String interfaceName;
        private final List<String> extendsList = new ArrayList<>();
        private final List<String> abstractGetters = new ArrayList<>();
        private AccessModifier getterAccess = AccessModifier.Public;

        InterfaceBuilder(String interfaceName) {
            this.interfaceName = interfaceName;
        }

        void setAccess(AccessModifier access) {
            interfaceAccess = access;
        }

        void addExtends(String _extends) {
            extendsList.add(_extends);
        }

        void addAbstractGetter(String type, String name) {
            abstractGetters.add(toString(getterAccess) + type + " get" + NameHelper.toUpperCamelCase(name) + "();");
        }

        void setGetterAccess(AccessModifier access) {
            getterAccess = access;
        }

        @Override void buildTo(OutputHelper helper) {
            helper.write(toString(interfaceAccess));
            helper.write("interface ");
            helper.write(interfaceName);

            helper.writeIf(" extends " + concat(extendsList), !extendsList.isEmpty());
            helper.enterBlock();

            helper.write(concat(abstractGetters, "\n"));

            helper.exitBlock();
        }
    }

    static class ClassBuilder extends Builder {
        private AccessModifier classAccess = AccessModifier.Public;
        private boolean isStatic = false;
        private final String className;

        private String superClass = null;
        private final List<String> implementsList = new ArrayList<>();

        private final List<String> fields = new ArrayList<>();

        private final List<String> constructorParameters = new ArrayList<>();
        private final List<String> superCallParameters = new ArrayList<>();
        private final List<String> constructorBodyAssertions = new ArrayList<>();
        private final List<String> constructorBody = new ArrayList<>();

        private final List<String> getters = new ArrayList<>();
        private final List<String> methods = new ArrayList<>();

        private final List<String> toStringBodyTerms = new ArrayList<>();
        private final List<String> equalsBodyTerms = new ArrayList<>();
        private final List<String> hashCodeBodyTerms = new ArrayList<>();

        private final List<Builder> subtypes = new ArrayList<>();

        private AccessModifier constructorAccess = AccessModifier.Public, getterAccess = AccessModifier.Public;

        ClassBuilder(String className) {
            this.className = className;
        }

        String getClassName() {
            return className;
        }

        void setGetterAccess(AccessModifier access) {
            getterAccess = access;
        }

        void setConstructorAccess(AccessModifier access) {
            constructorAccess = access;
        }

        void setAccess(AccessModifier access) {
            classAccess = access;
        }

        void setStatic(boolean isStatic) {
            this.isStatic = isStatic;
        }

        void setExtends(String name) {
            superClass = name;
        }

        void addImplements(String name) {
            implementsList.add(name);
        }

        void addSuperField(String type, String name) {
            constructorParameters.add(type + " " + name);
            superCallParameters.add(name);
        }

        void addField(String type, String name, boolean isOptional) {
            constructorParameters.add(type + " " + name);
            constructorBody.add("this." + name + " = " + name + ";");
            fields.add("private final " + type + " " + name + ";");
            getters.add(toString(getterAccess) + type + " get" + NameHelper.toUpperCamelCase(name) + "() {\n" +
                    "\treturn " + name + ";\n" +
                    "}");

            if (isOptional) {
                toStringBodyTerms.add("\"\t" + name + " = \" + (" + name + " == null ? \"null\" : " + name + ".toString()) + \"\\n\"");
                equalsBodyTerms.add("(" + name + " == null ? otherCasted." + name + " == null : " + name + ".equals(otherCasted." + name + "))");
            }
            else {
                constructorBodyAssertions.add("assert " + name + " != null : \"'" + name + "' is null\";");
                toStringBodyTerms.add("\"\t" + name + " = \" + " + name + ".toString() + \"\\n\"");
                equalsBodyTerms.add(name + ".equals(otherCasted." + name + ")");
            }

            hashCodeBodyTerms.add(name);
        }

        void addMethod(String method) {
            methods.add(method);
        }

        void addSubtype(Builder builder) {
            subtypes.add(builder);
        }

        @Override void buildTo(OutputHelper helper) {
            final boolean shouldWriteConstructor = superClass != null || !constructorBody.isEmpty();

            helper.write(toString(classAccess));
            helper.writeIf("static ", isStatic);
            helper.write("class ");
            helper.write(className);

            if (superClass != null) helper.write(" extends " + superClass);

            helper.writeIf(" implements " + concat(implementsList), !implementsList.isEmpty());
            helper.enterBlock();

                helper.write(concat(fields, "\n"));

                if (shouldWriteConstructor) {
                    OutputHelper constructorBodyBuilder = new OutputHelper();

                    constructorBodyBuilder.writeIf("super(" + concat(superCallParameters) + ");", superClass != null);
                    constructorBodyBuilder.ensureLinesIf(2, !constructorBodyAssertions.isEmpty());
                    constructorBodyBuilder.write(concat(constructorBodyAssertions, "\n"));
                    constructorBodyBuilder.ensureLinesIf(2, !constructorBody.isEmpty());
                    constructorBodyBuilder.write(concat(constructorBody, "\n"));

                    helper.ensureLines(2);
                    helper.write(toString(constructorAccess));
                    helper.write(className + "(" + concat(constructorParameters) + ")");
                    helper.enterBlock();
                        helper.write(constructorBodyBuilder.getResult());
                    helper.exitBlock();
                }

                helper.ensureLinesIf(2, !getters.isEmpty());
                helper.write(concat(getters, "\n\n"));

                helper.ensureLinesIf(2, !methods.isEmpty());
                helper.write(concat(methods, "\n\n"));

                helper.ensureLines(2);
                helper.write("@Override\npublic String toString()");
                helper.enterBlock();

                    helper.write("return \"(" + className);

                    if (fields.isEmpty()) {
                        helper.write(")\";");
                    }
                    else {
                        helper.write("\\n\"\n\t + " + concat(toStringBodyTerms, "\n\t + "));
                        helper.write("\n\t + \")\";");
                    }

                helper.exitBlock();

                helper.ensureLines(2);
                helper.write("@Override\npublic boolean equals(Object other)");
                helper.enterBlock();

                    if (fields.isEmpty()) {
                        helper.write("return other instanceof " + className + ";");
                    }
                    else {
                        helper.writeLine("if (!(other instanceof " + className + ")) return false;");
                        helper.writeLine(className + " otherCasted = (" + className + ") other;");
                        helper.write("return " + concat(equalsBodyTerms, "\n\t&& "));
                        helper.write(";");
                    }

                helper.exitBlock();

                helper.ensureLines(2);
                helper.write("@Override\npublic int hashCode()");
                helper.enterBlock();

                if (fields.isEmpty()) {
                    helper.write("return 0;");
                }
                else {
                    helper.write("return hash(" + concat(hashCodeBodyTerms) + ");");
                }

            helper.exitBlock();

                if (!subtypes.isEmpty()) {
                    for (Builder subtype : subtypes) {
                        helper.ensureLines(2);
                        subtype.buildTo(helper);
                    }
                }

            helper.exitBlock();
        }
    }

}
