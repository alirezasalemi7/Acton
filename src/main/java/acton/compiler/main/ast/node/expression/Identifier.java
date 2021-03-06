package acton.compiler.main.ast.node.expression;

import acton.compiler.main.visitor.Visitor;

public class Identifier extends Expression {
    private String name;

    public Identifier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Identifier " + name;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
