package acton.compiler.main.symbolTable.symbolTableVariableItem;

import acton.compiler.main.ast.node.declaration.VarDeclaration;
import acton.compiler.main.ast.type.Type;

public class SymbolTableKnownActorItem extends SymbolTableVariableItem {
    
    public SymbolTableKnownActorItem(String name, Type type, int index)
    {
        super(name ,type ,index);
    }
    
    public SymbolTableKnownActorItem(VarDeclaration localVar)
    {
        super(localVar);
    }
}
