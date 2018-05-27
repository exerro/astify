package astify.grammar_definition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class OutputHelper {
    private int indentation = 0;
    private List<String> indentationString = new ArrayList<>();
    private StringBuilder result = new StringBuilder();
    private char lastChar = 0;

    interface Callback {
        void call();
    }

    OutputHelper() {

    }

    void indent() {
        ++indentation;
        indentationString.add("\t");
    }

    void unindent() {
        if (indentation > 0) {
            --indentation;
            indentationString.remove(0);
        }
    }

    void indented(Callback c) {
        indent();
        c.call();
        unindent();
    }

    void write(String text) {
        String toWrite = text.replace("\n", "\n" + String.join("", indentationString));
        lastChar = toWrite.length() > 0 ? toWrite.charAt(toWrite.length() - 1) : lastChar;
        result.append(toWrite);
    }

    void writef(String fmt, Object... params) {
        write(String.format(fmt, params));
    }

    void writeLine() {
        write("\n");
    }

    void writeLine(String text) {
        write(text + "\n");
    }

    void writeLine(Callback c) {
        c.call();
        writeLine();
    }

    void enterBlock() {
        indent();
        write(" {");
    }

    void exitBlock() {
        unindent();
        writeLine();
        write("}");
    }

    void block(Callback c) {
        enterBlock();
        c.call();
        exitBlock();
    }

    void writeWord(String word) {
        if (lastChar >= 'a' && lastChar <= 'z' || lastChar >= 'A' && lastChar <= 'Z' || lastChar >= '0' && lastChar <= '9' || lastChar == '_') {
            write(" " + word);
        }
        else {
            write(word);
        }
    }

    void writeNumber(String number) {
        writeWord(number);
    }

    void writeOperator(String operator) {
        write(" " + operator + " ");
    }

    String getResult() {
        return result.toString();
    }

    boolean writeToFile(File file) {
        FileWriter writer;
        boolean ok = true;

        try { writer = new FileWriter(file); }
        catch (IOException e) { return false; }

        try { writer.write(getResult()); }
        catch (IOException e) { ok = false; }

        try { writer.close(); }
        catch (IOException e) { ok = false; }

        return ok;
    }
}
