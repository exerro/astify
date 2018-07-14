package astify;

import astify.core.Source;
import astify.token.DefaultTokenGenerator;
import astify.token.TokenException;
import astify.token.TokenGenerator;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static List<Capture> parse(PatternBuilder patternBuilder, TokenGenerator tokenGenerator, String patternName) throws TokenException, ParserException {
        Parser parser = new Parser();
        Pattern pattern;

        if (patternName == null) {
            pattern = patternBuilder.getMain();
        }
        else {
            pattern = patternBuilder.lookup(patternName);
        }

        if (pattern == null) {
            return null;
        }

        parser.setup(pattern, tokenGenerator.getStartingPosition());
        parser.parse(tokenGenerator);

        if (parser.hasError()) {
            throw ParserException.combine(parser.getExceptions());
        }
        else {
            return parser.getResults();
        }
    }

    public static List<Capture> parse(PatternBuilder patternBuilder, TokenGenerator tokenGenerator) throws TokenException, ParserException {
        return parse(patternBuilder, tokenGenerator, null);
    }

    public static List<Capture> parse(Source source, PatternBuilder patternBuilder, String patternName) throws TokenException, ParserException {
        return parse(patternBuilder, new DefaultTokenGenerator(source, patternBuilder.getKeywords()), patternName);
    }

    public static List<Capture> parse(Source source, PatternBuilder patternBuilder) throws TokenException, ParserException {
        return parse(source, patternBuilder, null);
    }


    public static Capture parseSingle(PatternBuilder patternBuilder, TokenGenerator tokenGenerator, String patternName) throws TokenException, ParserException {
        List<Capture> captures = parse(patternBuilder, tokenGenerator, patternName);

        if (captures == null) {
            return null;
        }

        if (captures.size() == 1) {
            return captures.get(0);
        }
        else if (captures.size() == 0) {
            // this should be impossible without exceptions being thrown but eh who knows
            System.out.println("wtf (astify/Util.java: parseSingle#1");
            return null;
        }
        else {
            // ambiguous syntax
            StringBuilder errorBuilder = new StringBuilder("Ambiguous syntax detected: multiple parse results returned\n\n");
            int i = 0;

            for (Capture result : captures) {
                errorBuilder.append("Result ");
                errorBuilder.append(++i);
                errorBuilder.append(":\n\t");
                errorBuilder.append(result.toString().replace("\n", "\n\t"));
            }

            throw new Error(errorBuilder.toString());
        }
    }

    public static Capture parseSingle(PatternBuilder patternBuilder, TokenGenerator tokenGenerator) throws TokenException, ParserException {
        return parseSingle(patternBuilder, tokenGenerator, null);
    }

    public static Capture parseSingle(Source source, PatternBuilder patternBuilder, String patternName) throws TokenException, ParserException {
        return parseSingle(patternBuilder, new DefaultTokenGenerator(source, patternBuilder.getKeywords()), patternName);
    }

    public static Capture parseSingle(Source source, PatternBuilder patternBuilder) throws TokenException, ParserException {
        return parseSingle(source, patternBuilder, null);
    }


    public static<T> String concatList(List<T> elements, String delimiter) {
        List<String> elementStrings = new ArrayList<>();

        for (T element : elements) {
            elementStrings.add(element.toString());
        }

        return String.join(delimiter, elementStrings);
    }

    public static<T> String concatList(List<T> elements) {
        return concatList(elements, ", ");
    }
}
