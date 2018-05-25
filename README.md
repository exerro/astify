
# ASTIFY

ASTify is a parsing framework focused on building intuitive ASTs directly from a token stream, filtering useless information, and making it easy to define a grammar.

It works around the idea of 'captures'. A capture represents some information extracted from a token stream. These captures are generated automatically with respect to a defined grammar.
By using callbacks, however, custom captures can be generated from sequences of sub-captures, thus providing support for selecting only relevant information from the parse tree, and resulting in a tailored AST structure during parsing.

## Using ASTify

See the [example](example) for a usage example.
