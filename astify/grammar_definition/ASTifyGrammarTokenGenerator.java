package astify.grammar_definition;

import astify.core.Position;
import astify.core.Source;
import astify.token.DefaultTokenGenerator;
import astify.token.Token;
import astify.token.TokenType;

import java.util.Set;

public class ASTifyGrammarTokenGenerator extends DefaultTokenGenerator {
    public ASTifyGrammarTokenGenerator(Source source, Set<String> keywords) {
        super(source, keywords);
    }

    @Override protected Token consumeWord() {
        StringBuilder word = new StringBuilder();
        Position position = currentPosition;

        for (; isAlpha() || getCharacter() == '-'; advance(1)) {
            word.append(getCharacter());
            position = position.to(currentPosition);
        }

        return new Token(keywords.contains(word.toString()) ? TokenType.Keyword : TokenType.Word, word.toString(), position);
    }
}
