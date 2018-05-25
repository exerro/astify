
package example;

import astify.Capture;
import astify.core.Position;

public abstract class Expression extends Capture.ObjectCapture {
    protected Expression(Position position) {
        super(position);
    }

    @Override public String toString() {
        return "<expression>";
    }
}
