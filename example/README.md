
# ASTify Example

This example shows how `astify-gdl` can be used to generate a parser, and how a file can be parsed using that, with the result being printed to the console.

See `example-grammar.gdl` for a GDL syntax example.

Use `bin/astify-gdl example/example-grammar.gdl` to generate `ExampleGrammar.java` and `ExampleGrammarPatternBuilderBase.java`. `ExampleGrammar.java` contains the generated AST structure, and `ExampleGrammarPatternBuilderBase.java` (what a mouthful) contains the generated syntax definitions and callbacks to create instances of the AST structure classes.

See `ExampleParser.java` to see how the generated code can be used to parse a file and display/handle the result.

> Note that `ExampleGrammar.java` and `ExampleGrammarPatternBuilderBase.java` are both generated in full by `astify-gdl`. In practice, these should be left as-is. In some cases, it is required to extend `ExampleGrammarPatternBuilderBase` (hence the `Base` in the name), for example when using `extern` functions. This is used for utility functions, e.g. `extern Block createSingleStatementBlock(Statement statement)`.
