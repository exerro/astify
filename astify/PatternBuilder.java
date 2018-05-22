package astify;

import astify.token.Token;
import astify.token.TokenType;

import java.util.*;

public class PatternBuilder {
    private final Map<String, Pattern> environment;
    private final Set<String> keywords;

    protected TokenType Word = TokenType.Word;
    protected TokenType String = TokenType.String;
    protected TokenType Integer = TokenType.Integer;
    protected TokenType Float = TokenType.Float;
    protected TokenType Symbol = TokenType.Symbol;
    protected TokenType Keyword = TokenType.Keyword;

    public PatternBuilder() {
        environment = new HashMap<>();
        keywords = new HashSet<>();
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public Pattern.TokenPattern token(TokenType type) {
        assert type != null;
        return new Pattern.TokenPattern(type);
    }

    public Pattern.TokenPattern token(TokenType type, String value) {
        assert type != null;
        assert value != null;
        return new Pattern.TokenPattern(type, value);
    }

    public astify.Pattern.TokenPattern keyword(String word) {
        assert word != null;
        keywords.add(word);
        return new Pattern.TokenPattern(TokenType.Keyword, word);
    }

    public Pattern.TokenPattern symbol(String symbol) {
        assert symbol != null;
        return new Pattern.TokenPattern(TokenType.Symbol, symbol);
    }

    public Pattern operator(String symbol) {
        if (symbol.length() == 1) return symbol(symbol);
        List<Pattern> patterns = new ArrayList<>();

        patterns.add(symbol(symbol.substring(0, 1)));

        for (int i = 1; i < symbol.length(); ++i) {
            patterns.add(symbol(symbol.substring(i, i + 1)).addPredicate(MatchPredicate.noSpace()));
        }

        return new Pattern.SequencePattern(null, patterns, (captures) -> new Capture.TokenCapture(new Token(
                Symbol,
                symbol,
                captures.get(0).spanningPosition.to(captures.get(captures.size() - 1).spanningPosition
                ))));
    }

    public Pattern.SequencePattern token(String name, TokenType type) {
        assert name != null;
        assert type != null;
        return define(name, token(type));
    }

    public Pattern.SequencePattern token(String name, TokenType type, String value) {
        assert name != null;
        assert type != null;
        assert value != null;
        return define(name, token(type, value));
    }

    public Pattern.OptionalPattern optional(Pattern opt) {
        assert opt != null;
        return new Pattern.OptionalPattern(opt);
    }

    public Pattern.SequencePattern optional(String name, Pattern opt) {
        assert name != null;
        assert opt != null;
        return define(name, optional(opt));
    }

    public Pattern.ListPattern list(Pattern pattern) {
        assert pattern != null;
        return new Pattern.ListPattern(pattern);
    }

    public Pattern.SequencePattern list(String name, Pattern pattern) {
        assert name != null;
        assert pattern != null;
        return define(name, list(pattern));
    }

    public Pattern.DelimitedPattern delim(Pattern pattern, Pattern delim) {
        assert pattern != null;
        return new Pattern.DelimitedPattern(pattern, delim);
    }

    public Pattern.SequencePattern delim(String name, Pattern pattern, Pattern delim) {
        assert name != null;
        assert pattern != null;
        return define(name, delim(pattern, delim));
    }

    public Pattern.SequencePattern sequence(String name, CaptureGenerator generator, Pattern... parts) {
        assert name != null;
        assert generator != null;
        assert parts.length > 0;
        return define(name, new Pattern.SequencePattern(name, Arrays.asList(parts), generator));
    }

    public Pattern.SequencePattern sequence(CaptureGenerator generator, Pattern... parts) {
        assert generator != null;
        assert parts.length > 0;
        return new Pattern.SequencePattern(null, Arrays.asList(parts), generator);
    }

    public Pattern.SequencePattern sequence(String name, Pattern... parts) {
        assert name != null;
        assert parts.length > 0;
        return sequence(name, Capture.ListCapture::createFrom, parts);
    }

    public Pattern.SequencePattern sequence(Pattern... parts) {
        assert parts.length > 0;
        return sequence(Capture.ListCapture::createFrom, parts);
    }

    public Pattern.BranchPattern one_of(Pattern... options) {
        assert options.length > 0;
        return new Pattern.BranchPattern(Arrays.asList(options));
    }

    public Pattern.SequencePattern one_of(String name, Pattern... options) {
        assert name != null;
        assert options.length > 0;
        return define(name, one_of(options));
    }

    public Pattern.GeneratorPattern ref(String name) {
        assert name != null;
        return new Pattern.GeneratorPattern(() -> lookup(name).getMatcher());
    }

    public Pattern.TokenPattern eof() {
        return token(TokenType.EOF);
    }

    public Pattern predicate(MatchPredicate predicate) {
        return new Pattern.NothingPattern().addPredicate(predicate);
    }

    public <T extends Pattern> T defineInline(String name, T pattern) {
        assert name != null;
        assert pattern != null;
        assert !environment.containsKey(name) : "Redefinition of " + name;

        environment.put(name, pattern);

        return pattern;
    }

    public <T extends Pattern> Pattern.SequencePattern define(String name, T pattern) {
        assert name != null;
        assert pattern != null;

        if (!(pattern instanceof Pattern.SequencePattern)) {
            return defineInline(name, new Pattern.SequencePattern(name, Collections.singletonList(pattern), Capture.nth(0)));
        }
        else {
            return defineInline(name, (Pattern.SequencePattern) pattern);
        }
    }

    public Pattern lookup(String name) {
        assert name != null;
        assert environment.containsKey(name) : "Lookup of " + name + " failed";
        return environment.get(name);
    }
}
