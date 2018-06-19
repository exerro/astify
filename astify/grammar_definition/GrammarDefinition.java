package astify.grammar_definition;

import astify.Parser;
import astify.ParserException;
import astify.core.Source;
import astify.token.TokenException;
import astify.token.TokenGenerator;

import java.io.IOException;

public class GrammarDefinition {
    public static void main(String[] args) throws Exception {
        ASTifyGrammar grammar = parse("astify/grammar_definition/ASTify-grammar.txt");
        IOException err = buildOutput(grammar, new BuildConfig("out"));

        if (err != null) throw err;
    }

    public static ASTifyGrammar parse(String filename) throws TokenException, ParserException {
        Source source = new Source.FileSource(filename);
        ASTifyGrammarPatternBuilder builder = new ASTifyGrammarPatternBuilder();
        TokenGenerator generator = new ASTifyGrammarTokenGenerator(source, builder.getKeywords());
        Parser parser = new Parser();

        parser.setup(builder.getMain(), generator.getStartingPosition());
        parser.parse(generator);

        if (parser.hasError()) {
            throw ParserException.combine(parser.getExceptions());
        }
        else {
            assert parser.getResults().size() == 1;
            return (ASTifyGrammar) parser.getResults().get(0);
        }
    }

    public static IOException buildOutput(ASTifyGrammar grammar, BuildConfig config) {
        Builder outputBuilder = new Builder(grammar.getGrammar().getName().getValue(), config);

        for (ASTifyGrammar.Definition definition : grammar.getDefinitions()) {
            outputBuilder.registerDefinition(definition);
        }

        for (ASTifyGrammar.Definition definition : grammar.getDefinitions()) {
            outputBuilder.buildDefinition(definition);
        }

        outputBuilder.build();

        try {
            outputBuilder.createFiles();
        }
        catch (IOException e) {
            return e;
        }

        return null;
    }
}
