
package astify;

import astify.core.Positioned;
import astify.core.Position;
import astify.token.Token;
import astify.token.TokenType;

import java.util.*;

public abstract class Capture implements Positioned {
    public final Position spanningPosition;

    protected Capture(Position spanningPosition) {
        assert spanningPosition != null;
        this.spanningPosition = spanningPosition;
    }

    public static CaptureGenerator nth(int index) {
        return captures -> captures.get(index);
    }

    @Override public Position getPosition() {
        return spanningPosition;
    }

    public static final class TokenCapture extends Capture {
        public final Token token;

        public TokenCapture(Token token) {
            super(token.position);
            this.token = token;
        }

        public TokenType getType() {
            return token.type;
        }

        public String getValue() {
            return token.value;
        }

        @Override public String toString() {
            return "<token-capture " + token.toString() + ">";
        }
    }

    public static final class EmptyCapture extends Capture {
        public EmptyCapture(Position spanningPosition) {
            super(spanningPosition);
        }

        @Override public String toString() {
            return "<empty-capture>";
        }
    }

    public static class ListCapture<T> extends Capture {
        private final List<T> elements;

        public ListCapture(Position spanningPosition, List<T> elements) {
            super(spanningPosition);
            this.elements = elements;
        }

        public T get(int i) {
            return elements.get(i);
        }

        public int size() {
            return elements.size();
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        public List<T> all() {
            return new ArrayList<>(elements);
        }

        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int i = 0;

                @Override public boolean hasNext() {
                    return i < size();
                }

                @Override public T next() {
                    return get(i++);
                }
            };
        }

        public static<T> ListCapture<T> createEmpty(Position spanningPosition) {
            return new ListCapture<>(spanningPosition, new ArrayList<>());
        }

        public static<T extends Positioned> ListCapture<T> createFrom(List<T> elements) {
            assert elements != null;
            assert !elements.isEmpty();

            return new ListCapture<>(elements.get(0).getPosition().to(elements.get(elements.size() - 1).getPosition()), elements);
        }

        public static<T extends Positioned> ListCapture createFrom(List<T> elements, Position spanningPosition) {
            return elements.isEmpty() ? createEmpty(spanningPosition) : createFrom(elements);
        }

        @Override public String toString() {
            if (elements.isEmpty()) return "<list-capture>";
            StringBuilder builder = new StringBuilder("<list-capture " + elements.get(0).toString());

            for (int i = 1; i < elements.size(); ++i) {
                builder.append(", ")
                       .append(elements.get(i).toString());
            }

            return builder.append(">").toString();
        }
    }

    public static abstract class ObjectCapture extends Capture {
        protected ObjectCapture(Position spanningPosition) {
            super(spanningPosition);
        }

        @Override public String toString() {
            return "<object-capture>";
        }
    }
}
