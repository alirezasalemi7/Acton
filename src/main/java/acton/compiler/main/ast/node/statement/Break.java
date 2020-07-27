package acton.compiler.main.ast.node.statement;

import acton.compiler.main.visitor.Visitor;

public class Break extends Statement {
	
	@Override
    public String toString() {
        return "Break";
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}