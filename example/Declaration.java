
package example;

import astify.Capture;
import astify.core.Position;

import java.util.List;

public class Declaration extends Capture.ObjectCapture {
    private final String type, name;

    protected Declaration(String type, String name, Position position) {
        super(position);
        assert type != null;
        assert name != null;
        this.type = type;
        this.name = name;
    }

    public static Capture create(List<Capture> captures) {
        assert captures.size() == 3;
        assert captures.get(0) instanceof Capture.TokenCapture;
        assert captures.get(1) instanceof Capture.TokenCapture;

        Capture.TokenCapture type = (Capture.TokenCapture) captures.get(0);
        Capture.TokenCapture name = (Capture.TokenCapture) captures.get(1);
        Position position = type.spanningPosition.to(captures.get(captures.size() - 1).spanningPosition);

        return new Declaration(type.getValue(), name.getValue(), position);
    }

    @Override public String toString() {
        return type + " " + name + ";";
    }
}
