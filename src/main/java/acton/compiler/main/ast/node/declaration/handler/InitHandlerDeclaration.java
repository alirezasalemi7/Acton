package acton.compiler.main.ast.node.declaration.handler;

import acton.compiler.main.ast.node.expression.Identifier;
import acton.compiler.main.visitor.Visitor;

public class InitHandlerDeclaration extends HandlerDeclaration {

    public InitHandlerDeclaration(Identifier name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "InitHandlerDeclaration";
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
