package astify.GDL;

import astify.Parser;
import astify.ParserException;
import astify.core.Source;
import astify.token.TokenException;
import astify.token.TokenGenerator;

import java.io.File;
import java.io.FileNotFoundException;

public class GrammarDefinition {
    public static void parseAndBuild(String filename, BuildConfig config) throws TokenException, ParserException, FileNotFoundException {
        ASTifyGrammar grammar = parse(filename);
        buildOutput(grammar, config);
    }

    public static void parseAndBuild(String filename) throws TokenException, ParserException, FileNotFoundException {
        BuildConfig config = new BuildConfig(new File(filename).getParent().replace("/", "."));
        parseAndBuild(filename, config);
    }

    public static ASTifyGrammar parse(String filename) throws TokenException, ParserException, FileNotFoundException {
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

    public static void buildOutput(ASTifyGrammar grammar, BuildConfig config) {
        Builder outputBuilder = new Builder(grammar.getGrammar().getName().getValue(), config);

        for (ASTifyGrammar.Definition definition : grammar.getDefinitions()) {
            outputBuilder.registerDefinition(definition);
        }

        for (ASTifyGrammar.Definition definition : grammar.getDefinitions()) {
            outputBuilder.buildDefinition(definition);
        }

        outputBuilder.build();
        outputBuilder.createFiles();
    }
}
