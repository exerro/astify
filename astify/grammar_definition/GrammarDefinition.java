package astify.grammar_definition;

import astify.Parser;
import astify.ParserException;
import astify.PatternBuilder;
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

            ClassLoader classLoader = GrammarDefinition.class.getClassLoader();

            try {
                Class c = classLoader.loadClass("out.ASTifyGrammarPatternBuilder");
                PatternBuilder testBuilder = (PatternBuilder) c.newInstance();

                TokenGenerator testGenerator = new ASTifyGrammarTokenGenerator(source, testBuilder.getKeywords());
                Parser testParser = new Parser();

                testParser.setup(testBuilder.getMain(), testGenerator.getStartingPosition());
                testParser.parse(testGenerator);

                if (testParser.hasError()) {
                    throw ParserException.combine(testParser.getExceptions());
                }
                else {
                    if (testParser.getResults().size() > 1) {
                        for (int i = 0; i < testParser.getResults().size(); ++i) {
                            System.out.println("Result " + i + " :: ");
                            System.out.println(testParser.getResults().get(i));
                        }
                    }
                    else {
                        System.out.println("Successful parse!");
                    }
                }
            }
            /*catch (ClassNotFoundException e) {
                e.printStackTrace();
            }*/
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
