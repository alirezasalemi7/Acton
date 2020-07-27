package acton.compiler.main.symbolTable.symbolTableVariableItem;

import acton.compiler.main.ast.node.declaration.VarDeclaration;
import acton.compiler.main.ast.type.Type;

public class SymbolTableActorVariableItem extends SymbolTableVariableItem{
    
    public SymbolTableActorVariableItem(String name, Type type, int index)
    {
        super(name, type, index);
    }
    
    public SymbolTableActorVariableItem(VarDeclaration actorVar)
    {
        super(actorVar);
    }
}