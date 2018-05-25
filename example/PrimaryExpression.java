
package example;

import astify.Capture;
import astify.core.Position;

import java.util.List;

public class PrimaryExpression extends Expression {
    private final String value;

    public PrimaryExpression(String value, Position position) {
        super(position);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    static Capture create(Capture.TokenCapture capture) {
        return new PrimaryExpression(capture.token.value, capture.getPosition());
    }

    static Capture callback(List<Capture> captures) {
        assert captures.get(0) instanceof Capture.TokenCapture;
        return create((Capture.TokenCapture) captures.get(0));
    }

    @Override public String toString() {
        return value;
    }
}
