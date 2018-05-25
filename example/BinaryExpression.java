
package example;

import astify.Capture;

import java.util.List;

public class BinaryExpression extends Expression {
    private final Expression leftOperand, rightOperand;
    private final String operator;

    public BinaryExpression(Expression leftOperand, Expression rightOperand, String operator) {
        super(leftOperand.getPosition().to(rightOperand.getPosition()));
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }

    public Expression getLeftOperand() {
        return leftOperand;
    }

    public Expression getRightOperand() {
        return rightOperand;
    }

    public String getOperator() {
        return operator;
    }

    static Capture create(List<Capture> captures) {
        assert captures.size() == 3 : "Invalid number of captures";
        assert captures.get(0) instanceof Capture.ObjectCapture : "Left operand is not an object";
        assert captures.get(1) instanceof Capture.TokenCapture : "Operator is not a token";
        assert captures.get(2) instanceof Capture.ObjectCapture : "Right operand is not an object";

        Expression leftOperand = (Expression) captures.get(0);
        Expression rightOperand = (Expression) captures.get(2);
        Capture.TokenCapture operator = (Capture.TokenCapture) captures.get(1);

        return new BinaryExpression(leftOperand, rightOperand, operator.token.value);
    }

    @Override public String toString() {
        return leftOperand.toString() + " " + operator + " " + rightOperand.toString();
    }
}
