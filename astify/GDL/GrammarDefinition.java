package astify.GDL;

import astify.Parser;
import astify.ParserException;
import astify.core.Source;
import astify.token.TokenException;
import astify.token.TokenGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GrammarDefinition {
    public static void parseAndBuild(String filename, BuildConfig config) throws TokenException, ParserException, IOException, GDLException {
        ASTifyGrammar grammar = parse(filename);
        buildOutput(grammar, config);
    }

    public static void parseAndBuild(String filename) throws TokenException, ParserException, IOException, GDLException {
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

    public static void buildOutput(ASTifyGrammar grammar, BuildConfig config) throws GDLException, IOException {
        Builder outputBuilder = new Builder(grammar.getGrammar().getName().getValue(), config);

        outputBuilder.setup(grammar);
        outputBuilder.build();
        outputBuilder.createFiles();
    }
}
