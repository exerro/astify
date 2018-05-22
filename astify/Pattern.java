package astify;

import astify.token.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Pattern {
    abstract Matcher getMatcher();

    static class TokenPattern extends Pattern {
        private final TokenType type;
        private final String value;

        TokenPattern(TokenType type, String value) {
            assert type != null;
            assert value != null;
            this.type = type;
            this.value = value;
        }

        TokenPattern(TokenType type) {
            assert type != null;
            this.type = type;
            this.value = null;
        }

        @Override Matcher getMatcher() {
            return value == null ? new Matcher.TokenMatcher(type) : new Matcher.TokenMatcher(type, value);
        }
    }

    static class NothingPattern extends Pattern {
        NothingPattern() {
            // empty
        }

        @Override Matcher getMatcher() {
            return new Matcher.NothingMatcher();
        }
    }

    static class SequencePattern extends Pattern {
        final String name;
        private final List<Pattern> patterns;
        private final CaptureGenerator generator;

        SequencePattern(String name, List<Pattern> patterns, CaptureGenerator generator) {
            assert patterns != null;
            assert generator != null;
            this.name = name;
            this.patterns = patterns;
            this.generator = generator;
        }

        @Override Matcher getMatcher() {
            List<Matcher> matchers = new ArrayList<>();

            for (Pattern pattern : patterns) {
                matchers.add(pattern.getMatcher());
            }

            return new Matcher.SequenceMatcher(name, matchers, generator);
        }
    }

    static class BranchPattern extends Pattern {
        private final List<Pattern> branches;

        BranchPattern(List<Pattern> branches) {
            assert branches != null;
            this.branches = branches;
        }

        @Override Matcher getMatcher() {
            List<Matcher> matchers = new ArrayList<>();

            for (Pattern pattern : branches) {
                matchers.add(pattern.getMatcher());
            }

            return new Matcher.BranchMatcher(matchers);
        }
    }

    static class GeneratorPattern extends Pattern {
        private final Matcher.GeneratorMatcher.MatcherGenerator generator;

        GeneratorPattern(Matcher.GeneratorMatcher.MatcherGenerator generator) {
            assert generator != null;
            this.generator = generator;
        }

        @Override  Matcher getMatcher() {
            return new Matcher.GeneratorMatcher(generator);
        }
    }

    static class OptionalPattern extends Pattern {
        private final Pattern pattern;
        private final CaptureGenerator generator;

        OptionalPattern(Pattern pattern) {
            assert pattern != null;
            this.pattern = pattern;
            this.generator = null;
        }

        OptionalPattern(Pattern pattern, CaptureGenerator generator) {
            this.pattern = pattern;
            this.generator = generator;
        }

        @Override Matcher getMatcher() {
            return new Matcher.BranchMatcher(Arrays.asList(
                pattern.getMatcher(),
                generator == null ? new Matcher.NothingMatcher() : new Matcher.SequenceMatcher(null, Collections.singletonList(new Matcher.NothingMatcher()), generator)
            ));
        }
    }

    static class ListPattern extends Pattern {
        private final Pattern pattern;

        ListPattern(Pattern pattern) {
            assert pattern != null;
            this.pattern = pattern;
        }

        static Capture generateFromList(Capture capture, Capture captures) {
            assert captures instanceof Capture.ListCapture;

            Capture.ListCapture listCapture = (Capture.ListCapture) captures;
            List<Capture> allCaptures = new ArrayList<>();

            allCaptures.add(capture);
            allCaptures.addAll((List<Capture>) listCapture.all());

            return Capture.ListCapture.createFrom(allCaptures);
        }

        Matcher generateMatcher() {
            return new OptionalPattern(
                    new Pattern.SequencePattern(null,
                            Arrays.asList(pattern, new GeneratorPattern(this::generateMatcher)),
                            captures -> ListPattern.generateFromList(captures.get(0), captures.get(1))
                    ),
                    (captures) -> Capture.ListCapture.createEmpty(captures.get(0).spanningPosition)
            ).getMatcher();
        }

        @Override public Matcher getMatcher() {
            Matcher matcher = pattern.getMatcher();
            return generateMatcher();
        }
    }
}
