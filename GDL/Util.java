package GDL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Objects.hash;

class Util {
    static class Pair<A, B> {
        A a;
        B b;

        Pair(A a, B b) {
            assert a != null;
            assert b != null;
            this.a = a;
            this.b = b;
        }

        @Override public String toString() {
            return "(" + a.toString() + ", " + b.toString() + ")";
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Pair)) return false;
            return a.equals(((Pair) other).a) && b.equals(((Pair) other).b);
        }

        @Override public int hashCode() {
            return hash(a, b);
        }
    }

    static<T, R> List<R> map(List<T> list, java.util.function.Function<T, R> f) {
        List<R> result = new ArrayList<>();

        for (T elem : list) {
            result.add(f.apply(elem));
        }

        return result;
    }

    // converts 'a' into "a" and 'a"\'' into "a\"'"
    static String convertStringQuotes(String s) {
        if (s.charAt(0) == '\'') {
            return "\"" + s.substring(1, s.length() - 1).replace("\"", "\\\"").replace("\\'", "'") + "\"";
        }
        return s;
    }

    static<T> String listToString(List<T> elements) {
        String s = elements.toString();
        return s.substring(1, s.length() - 1);
    }

    static String setToStringQuoted(Set<String> set) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (String s : set) {
            if (first) first = false;
            else builder.append(", ");

            builder.append("'").append(s).append("'");
        }

        return builder.toString();
    }
}
