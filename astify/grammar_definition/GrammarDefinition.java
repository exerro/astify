package astify.grammar_definition;

import astify.Parser;
import astify.ParserException;
import astify.core.Source;
import astify.token.TokenGenerator;

public class GrammarDefinition {
    public static void main(String[] args) throws Exception {
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
            assert parser.getResults().size() == 1;
            OutputBuilder outputBuilder = new OutputBuilder((ASTifyGrammar) parser.getResults().get(0), new BuildConfig("out"));

            outputBuilder.build();
            assert outputBuilder.writeToDirectory();
        }
    }
}
