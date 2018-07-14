
package example;

import astify.*;
import astify.core.Source;
import astify.token.TokenException;
import astify.util.Util;

import java.io.FileNotFoundException;
import java.util.List;

public class ExampleParser {
    public static void parse(String filePath) throws TokenException, ParserException, FileNotFoundException {
        Source source = new Source.FileSource(filePath);
        PatternBuilder builder = new ExampleGrammarPatternBuilderBase();

        List<Capture> results = astify.util.ParseUtil.parse(source, builder);
        int i = 0;

        for (Capture result : results) {
            System.out.println("Match " + (++i) + ":");
            System.out.println(result.toString());
        }
    }

    public static void main(String[] args) throws TokenException, ParserException, FileNotFoundException {
        parse("example/example.txt");
    }
}
