package acton.compiler.main.visitor.codeGenerator;

import acton.compiler.main.ast.node.declaration.VarDeclaration;
import acton.compiler.main.ast.node.expression.Expression;
import acton.compiler.main.ast.node.expression.Identifier;
import acton.compiler.main.ast.node.expression.Self;
import acton.compiler.main.ast.node.expression.Sender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SlotMapper {

    Map<String,Integer> map;

    public SlotMapper(ArrayList<VarDeclaration> declarations,boolean init){
        map = slotMapper(declarations,init);
    }

    private Map<String,Integer> slotMapper(ArrayList<VarDeclaration> declarations,boolean init){
        Map<String,Integer> map = new HashMap<>();
        int index = init ? 1 : 2;
        for(VarDeclaration varDeclaration : declarations){
            map.put(varDeclaration.getIdentifier().getName(),index);
            index++;
        }
        return map;
    }

    public int slotOf(Expression id){
        if(id instanceof Identifier){
            if(map.containsKey(((Identifier) id).getName())){
                return map.get(((Identifier) id).getName());
            }
            return -1;
        }
        else if(id instanceof Self){
            return 0;
        }
        else if(id instanceof Sender){
            return 1;
        }
        return -1;
    }
}
