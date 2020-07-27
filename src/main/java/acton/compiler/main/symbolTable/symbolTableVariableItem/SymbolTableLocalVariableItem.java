package acton.compiler.main.symbolTable.symbolTableVariableItem;

import acton.compiler.main.ast.node.declaration.VarDeclaration;
import acton.compiler.main.ast.type.Type;

public class SymbolTableLocalVariableItem extends SymbolTableVariableItem {
    
    public SymbolTableLocalVariableItem(String name, Type type, int index)
    {
        super(name ,type ,index);
    }
    
    public SymbolTableLocalVariableItem(VarDeclaration localVar)
    {
        super(localVar);
    }
}
