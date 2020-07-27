package acton.compiler.main.ast.node.statement;

import acton.compiler.main.ast.node.expression.Expression;
import acton.compiler.main.visitor.Visitor;

public class Print extends Statement {
    private Expression arg;

    public Print(Expression arg) {
        this.arg = arg;
    }

    public Expression getArg() {
        return arg;
    }

    public void setArg(Expression arg) {
        this.arg = arg;
    }

    @Override
    public String toString() {
        return "Print";
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
