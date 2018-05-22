
package example;

import astify.Capture;

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

        define("binary-operator", one_of(
                symbol("+"),
                symbol("-"),
                symbol("*"),
                symbol("/")
        ));

        define("binary-expression", one_of(
                sequence(BinaryExpression::create, ref("primary-expression"), ref("binary-operator"), ref("binary-expression")),
                ref("primary-expression")
        ));

        sequence("main", Capture.nth(0), list(ref("binary-expression")));
    }
}
