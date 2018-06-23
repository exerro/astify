
# ASTify Example

This example shows how `astify-gdl` can be used to generate a parser, and how a file can be parsed using that, with the result being printed to the console.

See `example-grammar.txt` for a GDL syntax example.

Use `astify-gdl example/example-grammar.txt` to generate `ExampleGrammar.java` and `ExampleGrammarPatternBuilder.java`.

See `ExampleParser.java` to see how the generated code can be used to parse a file and display/handle the result.
