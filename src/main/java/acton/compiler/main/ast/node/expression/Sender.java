package acton.compiler.main.ast.node.expression;

import acton.compiler.main.visitor.Visitor;

public class Sender extends Expression {
    @Override
    public String toString() {
         return "Sender";
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
