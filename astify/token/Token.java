package astify.token;

import astify.core.Position;
import astify.core.Positioned;

public class Token implements Positioned {
    public final TokenType type;
    public final String value;
    public final Position position;

    public Token(TokenType type, String value, Position position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    public boolean matches(TokenType type) {
        return this.type == type;
    }

    public boolean matches(TokenType type, String value) {
        return matches(type) && this.value.equals(value);
    }

    @Override public Position getPosition() {
        return position;
    }

    @Override public String toString() {
        if (type == TokenType.EOF) return "<EOF>";
        return type.toString() + " \"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
