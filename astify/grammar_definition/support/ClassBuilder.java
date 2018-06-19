package astify.grammar_definition.support;

import java.util.ArrayList;
import java.util.List;

public class ClassBuilder {

    public interface Builder {
        void build(OutputHelper helper);
    }

    public enum ClassType {
        Class,
        Interface
    }

    private final String className;
    private final OutputHelper fields = new OutputHelper();
    private final OutputHelper constructorParameters = new OutputHelper();
    private final OutputHelper constructorBodyHeader = new OutputHelper();
    private final OutputHelper constructorBody = new OutputHelper();
    private final OutputHelper getters = new OutputHelper();
    private final OutputHelper toStringBody = new OutputHelper();
    private final OutputHelper equalsBody = new OutputHelper();
    private final OutputHelper hashCodeBody = new OutputHelper();
    private final OutputHelper extendsList = new OutputHelper();
    private final OutputHelper implementsList = new OutputHelper();
    private final OutputHelper superList = new OutputHelper();

    private final List<Builder> fieldBuilders = new ArrayList<>();
    private final List<Builder> methodBuilders = new ArrayList<>();
    private final List<Builder> classBuilders = new ArrayList<>();

    private int flags = ENABLE_CONSTRUCTOR | ENABLE_METHODS;
    private ClassType type = ClassType.Class;
    private int fieldCount = 0, constructorFieldCount = 0;
    private int extendsCount = 0, implementsCount = 0;

    public static final int
            ENABLE_CONSTRUCTOR = 1,
            ENABLE_METHODS = 2;

    public ClassBuilder(String className) {
        this.className = className;

        toStringBody.write("return \"(" + className + "\"");
        equalsBody.write("return ");
        hashCodeBody.write("return ");
    }

    public String getClassName() {
        return className;
    }

    public void addExtends(String name) {
        if (extendsCount == 0) extendsList.write("extends");
        else extendsList.write(", ");
        extendsList.writeWord(name);
        ++extendsCount;
    }

    public void addImplements(String name) {
        if (implementsCount == 0) implementsList.write("implements");
        else implementsList.write(", ");
        implementsList.writeWord(name);
        ++implementsCount;
    }

    public void addField(String type, String name, boolean isOptional, boolean isMutable) {
        boolean isPrimitiveType = type.charAt(0) >= 'a' && type.charAt(0) <= 'z'; // assuming lower case first character -> primitive

        fields.ensureLines(1);
        fields.write("private " + (isMutable ? " " : "final ") + type + " " + name + ";");

        constructorParameters.write(((fieldCount + constructorFieldCount) == 0 ? "" : ", ") + type + " " + name);

        constructorBody.ensureLines(1);
        constructorBody.write("this." + name + " = " + name + ";");

        if (!isOptional && !isPrimitiveType) {
            constructorBodyHeader.writeLine("assert " + name + " != null;");
        }

        getters.write("public " + type + " " + NameHelper.getGetterName(name, type.equals("boolean")) + "()");
        getters.enterBlock();
            getters.write("return " + name + ";");
        getters.exitBlock();

        getters.writeLine();
        getters.writeLine();

        toStringBody.writeLine();
        toStringBody.write("     + \"\\n\\t" + name + " = \" + " + (isPrimitiveType || type.equals("String") ? name : name + ".toString().replace(\"\\n\", \"\\n\\t\")"));

        if (fieldCount != 0) equalsBody.write(" && ");
        if (isPrimitiveType) {
            equalsBody.write(name + " == otherCasted." + name);
        }
        else {
            equalsBody.write(name + ".equals(otherCasted." + name + ")");
        }

        if (fieldCount != 0) hashCodeBody.write(" + ");
        hashCodeBody.write((fieldCount > 0 ? String.valueOf((int) Math.pow(31, fieldCount)) + " * " : ""));
        hashCodeBody.write(isPrimitiveType ? (type.equals("boolean") ? "(" + name + " ? 1 : 0)" : name) : name + ".hashCode()");

        ++fieldCount;
    }

    public void addAbstractGetter(String type, String name) {
        getters.write("public " + type + " " + NameHelper.getGetterName(name, type.equals("boolean")) + "();");
        getters.writeLine();
        getters.writeLine();
    }

    public void addField(String type, String name, boolean isOptional) {
        addField(type, name, isOptional, false);
    }

    public void addConstructorField(String type, String name) {
        superList.write((constructorFieldCount == 0 ? "" : ", ") + name);
        constructorParameters.write(((fieldCount + constructorFieldCount) == 0 ? "" : ", ") + type + " " + name);
        ++constructorFieldCount;
    }

    public void addCustomField(Builder builder) {
        fieldBuilders.add(builder);
    }

    public void addMethod(Builder builder) {
        methodBuilders.add(builder);
    }

    public void addClass(ClassBuilder builder) {
        classBuilders.add(builder::buildToStatic);
    }

    public void setFlag(int flag, boolean state) {
        flags = state ? flags | flag : flags & ~flag;
    }

    public void setFlag(int flag) {
        setFlag(flag, true);
    }

    public void setClassType(ClassType type) {
        this.type = type;
    }

    public OutputHelper buildTo(OutputHelper result) {
        result.ensureLines(2);
        return buildToInternal(result);
    }

    public OutputHelper buildToStatic(OutputHelper result) {
        result.ensureLines(2);
        result.writeWord("static");
        return buildToInternal(result);
    }

    public String getResult() {
        return buildTo(new OutputHelper()).getResult();
    }

    private OutputHelper buildToInternal(OutputHelper result) {
        result.writeWord((type == ClassType.Class ? "class" : "interface") + " " + className);
        result.writeWord(extendsList.getResult());
        result.writeWord(implementsList.getResult());
        result.enterBlock();

        result.write(fields.getResult());

        if (fieldBuilders.size() > 0) {
            result.ensureLines(2);

            for (Builder fieldBuilder : fieldBuilders) {
                result.ensureLines(1);
                fieldBuilder.build(result);
            }
        }

        if ((flags & ENABLE_CONSTRUCTOR) != 0) {
            result.ensureLines(2);

            result.write("public " + className + "(");
            result.write(constructorParameters.getResult());
            result.write(")");
            result.enterBlock();

            if (extendsCount != 0)
                result.writeLine("super(" + superList.getResult() + ");");

            result.write(constructorBodyHeader.getResult());
            result.write(constructorBody.getResult());

            result.exitBlock();
        }

        if (methodBuilders.size() > 0) {
            for (Builder methodBuilder : methodBuilders) {
                result.ensureLines(2);
                methodBuilder.build(result);
            }
        }

        if (fieldCount > 0) {
            result.ensureLines(2);
            result.write(getters.getResult());
        }

        if ((flags & ENABLE_METHODS) != 0) {
            result.ensureLines(2);

            result.write("@Override\npublic String toString()");
            result.enterBlock();

            if (fieldCount == 0) {
                result.write("return \"(" + className + ")\";");
            }
            else {
                result.writeLine(toStringBody.getResult());
                result.write("     + \"\\n)\";");
            }

            result.exitBlock();

            result.ensureLines(2);

            result.write("@Override\npublic boolean equals(Object other)");
            result.enterBlock();

            if (fieldCount == 0) {
                result.write("return other instanceof " + className + ";");
            }
            else {
                result.writeLine("if (!(other instanceof " + className + ")) return false;");
                result.writeLine(className + " otherCasted = (" + className + ") other;");
                result.write(equalsBody.getResult());
                result.write(";");
            }

            result.exitBlock();

            result.ensureLines(2);

            result.write("@Override\npublic int hashCode()");
            result.enterBlock();

            result.write(fieldCount == 0 ? "return 0" : hashCodeBody.getResult());
            result.write(";");

            result.exitBlock();
        }

        if (!classBuilders.isEmpty()) {
            for (Builder classBuilder : classBuilders) {
                result.ensureLines(2);
                classBuilder.build(result);
            }
        }

        if (fieldBuilders.isEmpty() && (flags & ENABLE_CONSTRUCTOR) == 0 && methodBuilders.isEmpty() && fieldCount == 0 && (flags & ENABLE_METHODS) == 0 && classBuilders.isEmpty()) {
            result.unindent();
            result.writeLine();
            result.indent();
        }

        result.exitBlock();

        return result;
    }
}
