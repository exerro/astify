package astify.token;

import astify.core.Position;

public class TokenException extends Exception {
    private final String message;
    private final Position position;

    public TokenException(String message, Position position) {
        this.message = message;
        this.position = position;
    }

    @Override public String toString() {
        return this.getClass().getName() + ":\n" + message + " (in " + position.source.toString() + ")\n" + position.getLineAndCaret();
    }
}
