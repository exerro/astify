package astify.GDL;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class OutputHelper {
    private int indentation = 0;
    private String indentationString = "";
    private char lastChar = 0;
    private int newlines = 0;
    private final StringBuilder result = new StringBuilder();

    OutputHelper() {

    }

    void indent() {
        ++indentation;
        indentationString = indentationString + "\t";
    }

    void unindent() {
        if (indentation > 0) {
            --indentation;
            indentationString = indentationString.substring(1);
        }
    }

    void ensureLines(int lines) {
        while (newlines < lines && lastChar != 0) {
            write("\n");
        }
    }

    void ensureLinesIf(int lines, boolean cond) {
        while (cond && newlines < lines && lastChar != 0) {
            write("\n");
        }
    }

    void write(String text) {
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

    void writeIf(String str, boolean condition) {
        if (condition) write(str);
    }

    public void writef(String fmt, Object... params) {
        write(String.format(fmt, params));
    }

    void writeLine() {
        write("\n");
    }

    void writeLine(String text) {
        write(text + "\n");
    }

    void enterBlock() {
        indent();
        writeLine(" {");
    }

    void exitBlock() {
        unindent();
        ensureLines(1);
        write("}");
    }

    void writeWord(String word) {
        if (word.equals("")) return;

        if (lastChar >= 'a' && lastChar <= 'z' || lastChar >= 'A' && lastChar <= 'Z' || lastChar >= '0' && lastChar <= '9' || lastChar == '_') {
            write(" " + word);
        }
        else {
            write(word);
        }
    }

    String getResult() {
        return result.toString();
    }

    void writeToFile(File file) throws IOException {
        FileWriter writer = new FileWriter(file);

        writer.write(getResult());
        writer.flush();
        writer.close();
    }
}
