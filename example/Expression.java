
package example;

import astify.core.Position;
import astify.core.Positioned;

public abstract class Expression implements Positioned {
    private final Position position;

    protected Expression(Position position) {
        assert position != null;
        this.position = position;
    }

    @Override public Position getPosition() {
        return position;
    }

    @Override public String toString() {
        return "<expression>";
    }
}
