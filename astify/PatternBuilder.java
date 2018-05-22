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

    public Pattern token(TokenType type) {
        assert type != null;
        return new Pattern.TokenPattern(type);
    }

    public Pattern token(TokenType type, String value) {
        assert type != null;
        assert value != null;
        return new Pattern.TokenPattern(type, value);
    }

    public Pattern keyword(String word) {
        assert word != null;
        keywords.add(word);
        return new Pattern.TokenPattern(TokenType.Keyword, word);
    }

    public Pattern symbol(String symbol) {
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

    public Pattern token(String name, TokenType type) {
        assert name != null;
        assert type != null;
        return define(name, token(type));
    }

    public Pattern token(String name, TokenType type, String value) {
        assert name != null;
        assert type != null;
        assert value != null;
        return define(name, token(type, value));
    }

    public Pattern optional(Pattern opt) {
        assert opt != null;
        return new Pattern.OptionalPattern(opt);
    }

    public Pattern optional(String name, Pattern opt) {
        assert name != null;
        assert opt != null;
        return define(name, optional(opt));
    }

    public Pattern list(Pattern pattern) {
        assert pattern != null;
        return new Pattern.ListPattern(pattern);
    }

    public Pattern list(String name, Pattern pattern) {
        assert name != null;
        assert pattern != null;
        return define(name, list(pattern));
    }

    public Pattern sequence(String name, CaptureGenerator generator, Pattern... parts) {
        assert name != null;
        assert generator != null;
        assert parts.length > 0;
        return define(name, new Pattern.SequencePattern(name, Arrays.asList(parts), generator));
    }

    public Pattern sequence(CaptureGenerator generator, Pattern... parts) {
        assert generator != null;
        assert parts.length > 0;
        return new Pattern.SequencePattern(null, Arrays.asList(parts), generator);
    }

    public Pattern one_of(Pattern... options) {
        assert options.length > 0;
        return new Pattern.BranchPattern(Arrays.asList(options));
    }

    public Pattern one_of(String name, Pattern... options) {
        assert name != null;
        assert options.length > 0;
        return define(name, one_of(options));
    }

    public Pattern ref(String name) {
        assert name != null;
        return new Pattern.GeneratorPattern(() -> lookup(name).getMatcher());
    }

    public Pattern eof() {
        return token(TokenType.EOF);
    }

    public Pattern predicate(MatchPredicate predicate) {
        return new Pattern.NothingPattern().addPredicate(predicate);
    }

    public Pattern defineInline(String name, Pattern pattern) {
        assert name != null;
        assert pattern != null;
        assert !environment.containsKey(name) : "Redefinition of " + name;

        environment.put(name, pattern);

        return pattern;
    }

    public Pattern define(String name, Pattern pattern) {
        assert name != null;
        assert pattern != null;

        if (!(pattern instanceof Pattern.SequencePattern)) {
            pattern = new Pattern.SequencePattern(name, Collections.singletonList(pattern), Capture.nth(0));
        }

        return defineInline(name, pattern);
    }

    public Pattern lookup(String name) {
        assert name != null;
        assert environment.containsKey(name) : "Lookup of " + name + " failed";
        return environment.get(name);
    }
}
