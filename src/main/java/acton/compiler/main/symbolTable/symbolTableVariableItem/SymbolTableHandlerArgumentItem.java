package acton.compiler.main.symbolTable.symbolTableVariableItem;

import acton.compiler.main.ast.node.declaration.VarDeclaration;
import acton.compiler.main.ast.type.Type;

public class SymbolTableHandlerArgumentItem extends SymbolTableVariableItem {
    
    public SymbolTableHandlerArgumentItem(String name, Type type, int index)
    {
        super(name, type, index);
    }

    public SymbolTableHandlerArgumentItem(VarDeclaration argDeclaration)
    {
        super(argDeclaration);
    }
}
