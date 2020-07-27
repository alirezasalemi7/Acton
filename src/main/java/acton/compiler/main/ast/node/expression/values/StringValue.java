package acton.compiler.main.ast.node.expression.values;

import acton.compiler.main.ast.type.Type;
import acton.compiler.main.visitor.Visitor;

public class StringValue extends Value {
    private String constant;

    public StringValue(String constant, Type type) {
        this.constant = constant;
        this.type = type;
    }

    public String getConstant() {
        return constant;
    }

    public void setConstant(String constant) {
        this.constant = constant;
    }

    @Override
    public String toString() {
        return "StringValue " + constant;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
