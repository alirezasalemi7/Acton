package acton.compiler.main.visitor.nameAnalyser;

import acton.compiler.main.ast.node.*;
import acton.compiler.main.ast.node.Program;
import acton.compiler.main.ast.node.declaration.*;
import acton.compiler.main.ast.node.declaration.handler.HandlerDeclaration;
import acton.compiler.main.ast.node.declaration.handler.MsgHandlerDeclaration;
import acton.compiler.main.ast.node.expression.*;
import acton.compiler.main.ast.node.expression.values.*;
import acton.compiler.main.ast.node.statement.*;
import acton.compiler.main.ast.type.arrayType.ArrayType;
import acton.compiler.main.symbolTable.*;
import acton.compiler.main.symbolTable.itemException.*;
import acton.compiler.main.symbolTable.symbolTableVariableItem.*;
import acton.compiler.main.visitor.VisitorImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NameAnalyser extends VisitorImpl {
    private SymbolTableConstructor symbolTableConstructor;
    private TraverseState traverseState;
    private SymbolTableActorParentLinker symbolTableActorLinker;
    private ArrayList<String> nameErrors;
    private int lastIndexOfVariable;

    public NameAnalyser()
    {
        symbolTableConstructor = new SymbolTableConstructor();
        symbolTableActorLinker = new SymbolTableActorParentLinker();
        nameErrors = new ArrayList<>();
        lastIndexOfVariable = 0;
        setState(TraverseState.symbolTableConstruction);
    }

    public int numOfErrors()
    {
        return nameErrors.size();
    }

    private void switchState()
    {
        if(traverseState == TraverseState.symbolTableConstruction)
            setState(TraverseState.errorCatching);
        else if(traverseState == TraverseState.errorCatching)
            setState(TraverseState.PrintError);
        else
            setState(TraverseState.Exit);
    }

    private void setState(TraverseState traverseState)
    {
        this.traverseState = traverseState;
    }

    private void checkForPropertyRedefinition(ActorDeclaration actorDeclaration)
    {
        String name = actorDeclaration.getName().getName();
        if(name.indexOf('$') != -1)
            nameErrors.add("Line:" + actorDeclaration.getName().getLine() + ":Redefinition of actor " + name.substring(name.indexOf('$') + 1));
        try
        {
            SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.getInCurrentScope(SymbolTableActorItem.STARTKEY + name);
            SymbolTable next = actorItem.getActorSymbolTable();
            SymbolTable.push(next);
        }
        catch(ItemNotFoundException itemNotFound)
        {
        }
    }

    private void checkForPropertyRedefinitionInParentScopes(VarDeclaration varDeclaration)
    {
        String name = varDeclaration.getIdentifier().getName();
        Set<SymbolTable> visitedSymbolTables = new HashSet<>();
        String variableKey = SymbolTableVariableItem.STARTKEY + name;
        SymbolTable current = SymbolTable.top.getPreSymbolTable();
        visitedSymbolTables.add(SymbolTable.top);
        while(current != null &&  !visitedSymbolTables.contains(current))
        {
            visitedSymbolTables.add(current);
            try {
                current.getInCurrentScope(variableKey);
                SymbolTableVariableItem variable = (SymbolTableVariableItem) SymbolTable.top.getInCurrentScope(variableKey);
//                variable.setName("$" + variable.getName());
//                SymbolTable.top.updateInCurrentScope(variableKey , variable);
                nameErrors.add("Line:" + varDeclaration.getIdentifier().getLine() + ":Redefinition of variable " + name.substring(name.indexOf('$') + 1));
                return;
            }
            catch(ItemNotFoundException itemNotFound)
            {
                current = current.getPreSymbolTable();
            }
        }
    }

    private void checkForPropertyRedefinition(VarDeclaration varDeclaration)
    {
        String name = varDeclaration.getIdentifier().getName();
        if(name.indexOf('$') != -1) {
            nameErrors.add("Line:" + varDeclaration.getIdentifier().getLine() + ":Redefinition of variable " + name.substring(name.indexOf('$') + 1));
            return;
        }
        try {
            SymbolTableVariableItem varItem = (SymbolTableVariableItem) SymbolTable.top.getInCurrentScope(SymbolTableVariableItem.STARTKEY + name);
            varItem.setIndex(lastIndexOfVariable++);
            SymbolTable.top.updateInCurrentScope(SymbolTableVariableItem.STARTKEY + name , varItem);
            if(varItem instanceof SymbolTableActorVariableItem || varItem instanceof SymbolTableKnownActorItem)
                checkForPropertyRedefinitionInParentScopes(varDeclaration);
        }
        catch(ItemNotFoundException itemNotFound)
        {
        }
    }

    private void checkForPropertyRedefinitionInParentScope(MsgHandlerDeclaration msgHandlerDeclaration)
    {
        String name = msgHandlerDeclaration.getName().getName();
        String methodKey = SymbolTableHandlerItem.STARTKEY + name;
        SymbolTable current = SymbolTable.top.getPreSymbolTable();
        Set<SymbolTable> visitedSymbolTables = new HashSet<>();
        visitedSymbolTables.add(SymbolTable.top);
        while(current != null && !visitedSymbolTables.contains(current))
        {
            visitedSymbolTables.add(current);
            try {
                current.getInCurrentScope(methodKey);
                SymbolTableHandlerItem method = (SymbolTableHandlerItem) SymbolTable.top.getInCurrentScope(methodKey);
//                method.setName("$" + method.getName());
//                SymbolTable.top.updateInCurrentScope(methodKey , method);
                nameErrors.add("Line:" + msgHandlerDeclaration.getName().getLine() + ":Redefinition of msghandler " + name.substring(name.indexOf('$') + 1));
                return;
            }
            catch(ItemNotFoundException itemNotFound)
            {
                current = current.getPreSymbolTable();
            }
        }
    }

    private void checkForPropertyRedefinition(HandlerDeclaration handlerDeclaration)
    {
        String name = handlerDeclaration.getName().getName();
        SymbolTable next;
        String methodKey = SymbolTableHandlerItem.STARTKEY + name;
        try
        {
            next = ((SymbolTableHandlerItem)SymbolTable.top.getInCurrentScope(methodKey)).getHandlerSymbolTable();
        }
        catch(ItemNotFoundException itemNotFound)
        {
            return;
        }
        if(name.indexOf('$') != -1) {
            nameErrors.add("Line:" + handlerDeclaration.getName().getLine() + ":Redefinition of msghandler " + name.substring(name.indexOf('$') + 1));
            SymbolTable.push(next);
            return;
        }
        if(handlerDeclaration instanceof MsgHandlerDeclaration)
            checkForPropertyRedefinitionInParentScope((MsgHandlerDeclaration) handlerDeclaration);

        SymbolTable.push(next);
    }

    private void pushMainSymbolTable(){
        try{
            SymbolTableMainItem mainItem = (SymbolTableMainItem) SymbolTable.root.getInCurrentScope(SymbolTableMainItem.STARTKEY + "main");
            SymbolTable next = mainItem.getMainSymbolTable();
            SymbolTable.push(next);
        }
        catch(ItemNotFoundException itemNotFound)
        {
        }
    }

    @Override
    public Void visit(Program program){

        while(traverseState != TraverseState.Exit) {
            if (traverseState == TraverseState.symbolTableConstruction)
                symbolTableConstructor.constructProgramSymbolTable();
            else if (traverseState == TraverseState.errorCatching)
                symbolTableActorLinker.findActorsParents(program);
            else if(traverseState == TraverseState.PrintError) {
                for (String error : nameErrors)
                    System.out.println(error);
                return null;
            }

            for(ActorDeclaration actorDeclaration : program.getActors())
                actorDeclaration.accept(this);
            program.getMain().accept(this);
            switchState();
        }
        return null;
    }

    @Override
    public Void visit(ActorDeclaration actorDeclaration) {
        if (traverseState == TraverseState.symbolTableConstruction)
            symbolTableConstructor.construct(actorDeclaration);
        else if (traverseState == TraverseState.errorCatching) {
            checkForPropertyRedefinition(actorDeclaration);
            //TODO: check cyclic inheritance
            if(symbolTableActorLinker.hasCyclicInheritance(actorDeclaration)){
                nameErrors.add("Line:" + actorDeclaration.getLine() + ":Cyclic inheritance involving actor " + actorDeclaration.getName().getName());
            }
            if (actorDeclaration.getQueueSize() <= 0) {
                nameErrors.add("Line:" + actorDeclaration.getLine() + ":Queue size must be positive");
            }
        }

        visitExpr(actorDeclaration.getName());
        visitExpr(actorDeclaration.getParentName());
        for(VarDeclaration varDeclaration: actorDeclaration.getKnownActors())
            varDeclaration.accept(this);
        for(VarDeclaration varDeclaration: actorDeclaration.getActorVars())
            varDeclaration.accept(this);
        if(actorDeclaration.getInitHandler() != null)
            actorDeclaration.getInitHandler().accept(this);
        for(MsgHandlerDeclaration msgHandlerDeclaration: actorDeclaration.getMsgHandlers())
            msgHandlerDeclaration.accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(HandlerDeclaration handlerDeclaration) {
        if(handlerDeclaration == null)
            return null;
        if (traverseState == TraverseState.symbolTableConstruction)
            symbolTableConstructor.construct(handlerDeclaration);
        else if (traverseState == TraverseState.errorCatching)
            checkForPropertyRedefinition(handlerDeclaration);

        visitExpr(handlerDeclaration.getName());
        for(VarDeclaration argDeclaration: handlerDeclaration.getArgs())
            argDeclaration.accept(this);
        for(VarDeclaration localVariable: handlerDeclaration.getLocalVars())
            localVariable.accept(this);
        for(Statement statement : handlerDeclaration.getBody())
            visitStatement(statement);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(VarDeclaration varDeclaration) {
        if(varDeclaration == null)
            return null;
        if(traverseState == TraverseState.errorCatching) {
            checkForPropertyRedefinition(varDeclaration);
            if(varDeclaration.getType() instanceof ArrayType){
                if (((ArrayType)varDeclaration.getType()).getSize() <= 0){
                    nameErrors.add("Line:" + varDeclaration.getType().getLine() + ":Array size must be positive");
                }
            }
        }
        visitExpr(varDeclaration.getIdentifier());
        return null;
    }

    @Override
    public Void visit(Main programMain) {
        if(programMain == null)
            return null;

        if (traverseState == TraverseState.symbolTableConstruction)
            symbolTableConstructor.construct(programMain);
        else if (traverseState == TraverseState.errorCatching)
            pushMainSymbolTable();

        for(ActorInstantiation mainActor : programMain.getMainActors())
            mainActor.accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(ActorInstantiation actorInstantiation) {
        if(actorInstantiation == null)
            return null;

        if (traverseState == TraverseState.errorCatching)
            checkForPropertyRedefinition(actorInstantiation);

        visitExpr(actorInstantiation.getIdentifier());
        for(Identifier knownActor : actorInstantiation.getKnownActors())
            visitExpr(knownActor);
        for(Expression initArg : actorInstantiation.getInitArgs())
            visitExpr(initArg);
        return null;
    }

    @Override
    public Void visit(UnaryExpression unaryExpression) {
        if(unaryExpression == null)
            return null;

        visitExpr(unaryExpression.getOperand());
        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        if(binaryExpression == null)
            return null;

        visitExpr(binaryExpression.getLeft());
        visitExpr(binaryExpression.getRight());
        return null;
    }

    @Override
    public Void visit(ArrayCall arrayCall) {

        visitExpr(arrayCall.getArrayInstance());
        visitExpr(arrayCall.getIndex());
        return null;
    }

    @Override
    public Void visit(ActorVarAccess actorVarAccess) {
        if(actorVarAccess == null)
            return null;

        visitExpr(actorVarAccess.getVariable());
        return null;
    }

    @Override
    public Void visit(Identifier identifier) {
        if(identifier == null)
            return null;
        return null;
    }

    @Override
    public Void visit(Self self) {
        return null;
    }

    @Override
    public Void visit(Sender sender) {
        return null;
    }

    @Override
    public Void visit(BooleanValue value) {
        return null;
    }

    @Override
    public Void visit(IntValue value) {
        return null;
    }

    @Override
    public Void visit(StringValue value) {
        return null;
    }

    @Override
    public Void visit(MsgHandlerCall msgHandlerCall) {
        if(msgHandlerCall == null) {
            return null;
        }
        try {
            visitExpr(msgHandlerCall.getInstance());
            visitExpr(msgHandlerCall.getMsgHandlerName());
            for (Expression argument : msgHandlerCall.getArgs())
                visitExpr(argument);
        }
        catch(NullPointerException npe) {
        }
        return null;
    }

    @Override
    public Void visit(Block block) {
        if(block == null)
            return null;
        for(Statement statement : block.getStatements())
            visitStatement(statement);
        return null;
    }

    @Override
    public Void visit(Conditional conditional) {
        visitExpr(conditional.getExpression());
        visitStatement(conditional.getThenBody());
        visitStatement(conditional.getElseBody());
        return null;
    }

    @Override
    public Void visit(For loop) {
        visitStatement(loop.getInitialize());
        visitExpr(loop.getCondition());
        visitStatement(loop.getUpdate());
        visitStatement(loop.getBody());
        return null;
    }

    @Override
    public Void visit(Break b) {
        return null;
    }

    @Override
    public Void visit(Continue c) {
        return null;
    }

    @Override
    public Void visit(Print print) {
        if(print == null)
            return null;
        visitExpr(print.getArg());
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        visitExpr(assign.getlValue());
        visitExpr(assign.getrValue());
        return null;
    }
}
