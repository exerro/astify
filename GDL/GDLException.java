package GDL;

import astify.core.Position;

public class GDLException extends Exception {
    protected final Position position;

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

    public static class TaggedGDLException extends GDLException {
        private final String tag;
        private final Position tagPosition;

        public TaggedGDLException(String message, Position position, String tag, Position tagPosition) {
            super(message, position);
            this.tag = tag;
            this.tagPosition = tagPosition;
        }

        @Override public String toString() {
            return super.toString() + "\n" + tag + "\n" + tagPosition.source.getName() + ":\n" + tagPosition.getLineAndCaret();
        }
    }
}
