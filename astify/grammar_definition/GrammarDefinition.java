package astify.grammar_definition;

import astify.Capture;
import astify.Parser;
import astify.ParserException;
import astify.core.Source;
import astify.token.DefaultTokenGenerator;
import astify.token.TokenException;
import astify.token.TokenGenerator;

public class GrammarDefinition {
    public static void main(String[] args) throws TokenException, ParserException {
        Source source = new Source.FileSource("astify/grammar_definition/ASTify-grammar.txt");
        ASTifyGrammarBuilder builder = new ASTifyGrammarBuilder();
        TokenGenerator generator = new ASTifyGrammarTokenGenerator(source, builder.getKeywords());
        Parser parser = new Parser();

        parser.setup(builder.getMain(), generator.getStartingPosition());
        parser.parse(generator);

        if (parser.hasError()) {
            throw ParserException.combine(parser.getExceptions());
        }
        else {
            for (Capture result : parser.getResults()) {
                System.out.println(result);
            }
        }
    }
}
