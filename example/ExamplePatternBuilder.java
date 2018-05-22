
package example;

import astify.Capture;
import astify.MatchPredicate;

public class ExamplePatternBuilder extends astify.PatternBuilder {
    public ExamplePatternBuilder() {
        sequence("primary-expression", PrimaryExpression::callback, one_of(
                token(Integer),
                token(Float),
                token(String),
                keyword("true"),
                keyword("false"),
                keyword("null")
        ));

        defineInline("binary-operator", one_of(
                operator("+"),
                operator("-"),
                operator("*"),
                operator("/"),
                operator("==")
        ));

        define("binary-expression", one_of(
                sequence(BinaryExpression::create, ref("primary-expression"), ref("binary-operator"), ref("binary-expression")),
                ref("primary-expression")
        ));

        sequence("declaration", Declaration::create, token(Word), token(Word).addPredicate(MatchPredicate.sameLine()), symbol(";"));

        sequence("statement", Capture.nth(0), one_of(
                sequence("expression-statement", Capture.nth(1), keyword("eval"), ref("binary-expression")),
                ref("declaration")
        ), predicate(MatchPredicate.nextLine()));

        sequence("example", Capture.nth(0), list(ref("statement")), eof());
    }
}
