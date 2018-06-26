package astify.GDL;

import astify.Parser;
import astify.ParserException;
import astify.core.Source;
import astify.token.TokenException;
import astify.token.TokenGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GrammarDefinition {
    public static List<GDLException> parseAndBuild(String filename, BuildConfig config) throws TokenException, ParserException, IOException {
        ASTifyGrammar grammar = parse(filename);
        return buildOutput(grammar, config);
    }

    public static List<GDLException> parseAndBuild(String filename) throws TokenException, ParserException, IOException {
        BuildConfig config = new BuildConfig(new File(filename).getParent().replace("/", "."));
        return parseAndBuild(filename, config);
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

    public static List<GDLException> buildOutput(ASTifyGrammar grammarSource, BuildConfig config) throws IOException {
        Grammar grammar = new Grammar(grammarSource.getGrammar().getName().getValue());
        ASTDefinitionBuilder ASTDefinitionBuilder = new ASTDefinitionBuilder(grammar, config);
        PatternBuilder patternBuilder = new PatternBuilder(grammar, config);

        grammar.load(grammarSource);

        if (grammar.hasException()) {
            return grammar.getExceptions();
        }

        ASTDefinitionBuilder.build();
        ASTDefinitionBuilder.createFiles();
        patternBuilder.build();
        patternBuilder.createFiles();

        return Collections.emptyList();
    }
}
