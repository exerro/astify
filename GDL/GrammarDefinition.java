package GDL;

import astify.ParserException;
import astify.token.Token;
import astify.token.TokenException;
import astify.util.MultiFileParser;
import astify.core.Source;
import astify.util.Util;
import astify.util.ParseUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class GrammarDefinition {
    private static class GrammarException extends Exception {
        GrammarException(String message) {
            super(message);
        }
    }

    public static List<Exception> parseAndBuild(String filename, BuildConfig config) throws IOException {
        MultiFileParser<ASTifyGrammar> parser = parse(filename);

        if (parser.hasError()) {
            return parser.getErrors();
        }

        ASTifyGrammar mainGrammar = parser.getResult(parser.listSources().get(0));
        List<ASTifyGrammar.Statement> combinedStatements = new ArrayList<>();

        if (mainGrammar.getGrammar() == null) {
            return Collections.singletonList(new GrammarException("Main grammar file '" + parser.listSources().get(0).getName() + "' missing grammar statement"));
        }

        for (Source source : parser.listSources()) {
            combinedStatements.addAll(parser.getResult(source).getStatements());
        }

        ASTifyGrammar combined = new ASTifyGrammar(mainGrammar.getPosition(), Collections.emptyList(), mainGrammar.getGrammar(), combinedStatements);

        return new ArrayList<>(buildOutput(combined, config));
    }

    @SuppressWarnings("unused")
    public static List<Exception> parseAndBuild(String filename) throws IOException {
        BuildConfig config = new BuildConfig(new File(filename).getParent().replace("/", "."));
        return parseAndBuild(filename, config);
    }

    public static MultiFileParser<ASTifyGrammar> parse(String filename) {
        MultiFileParser<ASTifyGrammar> parser = new MultiFileParser<ASTifyGrammar>() {
            public Stack<String> fileImportStack = new Stack<>();

            @Override
            public ASTifyGrammar parseSource(Source source) throws TokenException, ParserException {
                astify.PatternBuilder builder = new ASTifyGrammarPatternBuilderBase();
                return (ASTifyGrammar) ParseUtil.parseSingle(builder, new ASTifyGrammarTokenGenerator(source, builder.getKeywords()));
            }

            @Override
            public void onSourceParsed(Source source, ASTifyGrammar result) {
                if (source instanceof Source.FileSource) {
                    fileImportStack.add(((Source.FileSource) source).getPath());
                }

                for (ASTifyGrammar.ImportStatement importStatement : result.getImports()) {
                    String fileName = null;

                    if (importStatement instanceof ASTifyGrammar.AbsoluteImportStatement) {
                        fileName = Util.unformatString(((ASTifyGrammar.AbsoluteImportStatement) importStatement).getImportPath().getValue());
                    }
                    else if (importStatement instanceof ASTifyGrammar.RelativeImportStatement) {
                        fileName = Util.concatList(Util.map(Token::getValue, ((ASTifyGrammar.RelativeImportStatement) importStatement).getParts()), "/") + ".gdl";
                    }

                    parseFileDeferred(fileName);
                }

                if (source instanceof Source.FileSource) {
                    fileImportStack.pop();
                }
            }

            @Override
            public List<String> getBasePaths() {
                if (fileImportStack.empty()) {
                    return Collections.singletonList(null);
                }

                String lastFileImport = fileImportStack.peek().replace("\\", "/");

                if (!lastFileImport.contains("/")) {
                    return Collections.singletonList("");
                }

                return Collections.singletonList(lastFileImport.substring(0, lastFileImport.lastIndexOf("/")));
            }

        };

        parser.parseFileDeferred(filename);
        parser.parseSources();

        return parser;
    }

    public static List<GDLException> buildOutput(ASTifyGrammar grammarSource, BuildConfig config) throws IOException {
        Grammar grammar = new Grammar(grammarSource.getGrammar().getName().getValue());

        grammar.load(grammarSource);

        ASTDefinitionBuilder ASTDefinitionBuilder = new ASTDefinitionBuilder(grammar, config);
        PatternBuilder patternBuilder = new PatternBuilder(grammar, config);

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
