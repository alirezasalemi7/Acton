package acton.compiler.main.ast.node.statement;

import acton.compiler.main.visitor.Visitor;

public class Continue extends Statement {

	@Override
    public String toString() {
        return "Continue";
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
