
package example;

import astify.Capture;
import astify.core.Position;
import astify.core.Positioned;
import astify.token.Token;

import java.util.List;

public class Declaration implements Positioned {
    private final String type, name;
    private final Position position;

    protected Declaration(String type, String name, Position position) {
        assert position != null;
        this.type = type;
        this.name = name;
        this.position = position;
    }

    public static Capture create(List<Capture> captures) {
        assert captures.size() == 3;
        assert captures.get(0) instanceof Capture.TokenCapture;
        assert captures.get(1) instanceof Capture.TokenCapture;

        Capture.TokenCapture type = (Capture.TokenCapture) captures.get(0);
        Capture.TokenCapture name = (Capture.TokenCapture) captures.get(1);
        Position position = type.spanningPosition.to(captures.get(captures.size() - 1).spanningPosition);

        return new Capture.ObjectCapture<>(new Declaration(type.getValue(), name.getValue(), position), position);
    }

    @Override public Position getPosition() {
        return position;
    }

    @Override public String toString() {
        return type + " " + name + ";";
    }
}
