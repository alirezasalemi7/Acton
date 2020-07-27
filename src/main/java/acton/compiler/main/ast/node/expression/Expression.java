package acton.compiler.main.ast.node.expression;

import acton.compiler.main.ast.node.Node;
import acton.compiler.main.ast.type.Type;

public abstract class Expression extends Node{
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}