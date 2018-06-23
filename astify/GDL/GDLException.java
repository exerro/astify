package astify.GDL;

import astify.core.Position;

public class GDLException extends Exception {
    private final Position position;

    public GDLException(String message, Position position) {
        super(message);
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    @Override public String toString() {
        return position.source.getName() + ": " + getMessage() + "\n" + position.getLineAndCaret();
    }
}
