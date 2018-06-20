
package example;

import astify.Capture;
import astify.Parser;
import astify.ParserException;
import astify.PatternBuilder;
import astify.core.Source;
import astify.token.DefaultTokenGenerator;
import astify.token.TokenException;
import astify.token.TokenGenerator;

import java.io.FileNotFoundException;

public class ExampleParser {
    public static void parse(String filePath) throws TokenException, ParserException, FileNotFoundException {
        Source source = new Source.FileSource(filePath);
        PatternBuilder builder = new ExamplePatternBuilder();
        TokenGenerator generator = new DefaultTokenGenerator(source, builder.getKeywords());
        Parser parser = new Parser();

        parser.setup(builder.lookup("example"), generator.getStartingPosition());
        parser.parse(generator);

        if (parser.hasError()) {
            throw ParserException.combine(parser.getExceptions());
        }
        else {
            int i = 0;

            for (Capture result : parser.getResults()) {
                System.out.println("Match " + (++i) + ":");
                System.out.println(result.toString());
            }
        }
    }

    public static void main(String[] args) throws TokenException, ParserException, FileNotFoundException {
        parse("example/example.txt");
    }
}
