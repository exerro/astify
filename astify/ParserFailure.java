package astify;

import astify.core.Position;
import astify.token.TokenType;

import java.util.List;
import java.util.Set;

import static java.util.Objects.hash;

abstract class ParserFailure {
    final List<String> sources;

    ParserFailure(List<String> sources) {
        this.sources = sources;
    }

    abstract String getExpected();

    String getSources() {
        if (sources.size() == 0) return "";

        StringBuilder builder = new StringBuilder(sources.get(sources.size() - 1));

        for (int i = sources.size() - 2; i >= 0; --i) {
            builder.append(".");
            builder.append(sources.get(i));
        }

        return builder.toString();
    }

    static Set<ParserFailure> simplifySources(Set<ParserFailure> failures) {
        if (failures.isEmpty()) return failures;

        ParserFailure someFailure = failures.iterator().next();
        List<String> compareTo = someFailure.sources;
        boolean removeNext = false;

        while (compareTo.size() >= 0) {
            String comparingTo = compareTo.get(compareTo.size() - 1);
            boolean allMatch = compareTo.size() > 0;

            for (ParserFailure failure : failures) {
                if (removeNext) failure.sources.remove(failure.sources.size() - 1);
                allMatch = allMatch && failure.sources.size() > 1 && failure.sources.get(failure.sources.size() - 1).equals(comparingTo);
            }

            removeNext = allMatch;

            if (!allMatch) break;
        }

        return failures;
    }

    static class TokenTypeMatchFailure extends ParserFailure {
        private final TokenType expectedType;
        private final String expectedValue;

        TokenTypeMatchFailure(List<String> sources, TokenType expectedType, String expectedValue) {
            super(sources);
            assert expectedType != null;
            this.expectedType = expectedType;
            this.expectedValue = expectedValue;
        }

        String getExpected() {
            return expectedValue == null ? expectedType.toString() : expectedType.toString() + " \"" + expectedValue + "\"";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TokenTypeMatchFailure that = (TokenTypeMatchFailure) o;

            if (expectedType != that.expectedType) return false;
            return expectedValue != null ? expectedValue.equals(that.expectedValue) : that.expectedValue == null;
        }

        @Override public int hashCode() {
            int result = expectedType.hashCode();
            result = 31 * result + (expectedValue != null ? expectedValue.hashCode() : 0);
            return result;
        }
    }

    static class TokenValueMatchFailure extends ParserFailure {
        private final String expectedValue;

        TokenValueMatchFailure(List<String> sources, String expectedValue) {
            super(sources);
            assert expectedValue != null;
            this.expectedValue = expectedValue;
        }

        @Override public String getExpected() {
            return "'" + expectedValue + "'";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TokenValueMatchFailure that = (TokenValueMatchFailure) o;

            return expectedValue.equals(that.expectedValue);
        }

        @Override public int hashCode() {
            return expectedValue.hashCode();
        }
    }
}
