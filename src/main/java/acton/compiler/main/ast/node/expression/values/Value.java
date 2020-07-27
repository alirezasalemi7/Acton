package acton.compiler.main.ast.node.expression.values;

import acton.compiler.main.ast.node.expression.Expression;
import acton.compiler.main.ast.type.Type;

public abstract class Value extends Expression {
    protected Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}