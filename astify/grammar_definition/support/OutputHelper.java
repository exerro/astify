package astify.grammar_definition.support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputHelper {
    private int indentation = 0;
    private String indentationString = "";
    private char lastChar = 0;
    private int newlines = 0;
    private final StringBuilder result = new StringBuilder();

    interface Callback {
        void call();
    }

    public OutputHelper() {

    }

    public void indent() {
        ++indentation;
        indentationString = indentationString + "\t";
    }

    public void unindent() {
        if (indentation > 0) {
            --indentation;
            indentationString = indentationString.substring(1);
        }
    }

    public void ensureLines(int lines) {
        while (newlines < lines && lastChar != 0) {
            write("\n");
        }
    }

    public void indented(Callback c) {
        indent();
        c.call();
        unindent();
    }

    public void write(String text) {
        int oldNewlines = this.newlines;
        int newlines = 0;

        for (int i = text.length() - 1; i >= 0; --i) {
            if (text.charAt(i) == '\n') ++newlines;
            else if (text.charAt(i) != ' ' && text.charAt(i) != '\t') { oldNewlines = 0; break; }
        }

        this.newlines = oldNewlines + newlines;

        String toWrite = text.replace("\n", "\n" + indentationString);
        lastChar = toWrite.length() > 0 ? toWrite.charAt(toWrite.length() - 1) : lastChar;
        result.append(toWrite);
    }

    public void writef(String fmt, Object... params) {
        write(String.format(fmt, params));
    }

    public void writeLine() {
        write("\n");
    }

    public void writeLine(String text) {
        write(text + "\n");
    }

    public void writeLine(Callback c) {
        c.call();
        writeLine();
    }

    public void enterBlock() {
        indent();
        writeLine(" {");
    }

    public void exitBlock() {
        unindent();
        ensureLines(1);
        write("}");
    }

    public void block(Callback c) {
        enterBlock();
        c.call();
        exitBlock();
    }

    public void writeWord(String word) {
        if (word.equals("")) return;

        if (lastChar >= 'a' && lastChar <= 'z' || lastChar >= 'A' && lastChar <= 'Z' || lastChar >= '0' && lastChar <= '9' || lastChar == '_') {
            write(" " + word);
        }
        else {
            write(word);
        }
    }

    public void writeNumber(String number) {
        writeWord(number);
    }

    public void writeOperator(String operator) {
        write(" " + operator + " ");
    }

    public String getResult() {
        return result.toString();
    }

    public boolean writeToFile(File file) {
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
