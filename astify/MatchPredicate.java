
package astify;

import astify.core.Position;
import astify.token.Token;
import astify.token.TokenType;

public interface MatchPredicate {
    class State {
        public final Token nextToken;
        public final Position lastTokenPosition;

        public State(Token nextToken, Position lastTokenPosition) {
            this.nextToken = nextToken;
            this.lastTokenPosition = lastTokenPosition;
        }
    }

    boolean test(State state);

    default String getError(State state) {
        return "failed predicate";
    }

    static MatchPredicate noSpace() {
        return new MatchPredicate() {
            @Override public boolean test(State state) {
                return state.nextToken.position.isAfter(state.lastTokenPosition);
            }

            @Override public String getError(State state) {
                System.out.println(state.lastTokenPosition);
                System.out.println(state.nextToken.position);
                return "Unexpected space before " + state.nextToken.toString();
            }
        };
    }

    static MatchPredicate sameLine() {
        return new MatchPredicate() {
            @Override public boolean test(State state) {
                return state.nextToken.position.line1 == state.lastTokenPosition.line2;
            }

            @Override public String getError(State state) {
                return "Unexpected newline before " + state.nextToken.toString();
            }
        };
    }

    static MatchPredicate nextLine() {
        return new MatchPredicate() {
            @Override public boolean test(State state) {
                return state.nextToken.type == TokenType.EOF || state.nextToken.position.line1 > state.lastTokenPosition.line2;
            }

            @Override public String getError(State state) {
                return "Expected newline before " + state.nextToken.toString();
            }
        };
    }
}
