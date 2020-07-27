package acton.compiler.main.visitor.typeChecker;

import acton.compiler.main.ast.node.Main;
import acton.compiler.main.ast.node.Program;
import acton.compiler.main.ast.node.declaration.ActorDeclaration;
import acton.compiler.main.ast.node.declaration.ActorInstantiation;
import acton.compiler.main.ast.node.declaration.VarDeclaration;
import acton.compiler.main.ast.node.declaration.handler.HandlerDeclaration;
import acton.compiler.main.ast.node.declaration.handler.InitHandlerDeclaration;
import acton.compiler.main.ast.node.declaration.handler.MsgHandlerDeclaration;
import acton.compiler.main.ast.node.expression.*;
import acton.compiler.main.ast.node.statement.*;
import acton.compiler.main.ast.type.DynamicType;
import acton.compiler.main.ast.type.Type;
import acton.compiler.main.ast.type.actorType.ActorType;
import acton.compiler.main.ast.type.arrayType.ArrayType;
import acton.compiler.main.ast.type.noType.NoType;
import acton.compiler.main.ast.type.primitiveType.BooleanType;
import acton.compiler.main.ast.type.primitiveType.IntType;
import acton.compiler.main.ast.type.primitiveType.StringType;
import acton.compiler.main.symbolTable.*;
import acton.compiler.main.symbolTable.itemException.ItemNotFoundException;
import acton.compiler.main.symbolTable.symbolTableVariableItem.SymbolTableVariableItem;
import acton.compiler.main.visitor.VisitorImpl;

import java.util.ArrayList;

public class TypeChecker extends VisitorImpl {

    SymbolTable root = SymbolTable.root;
    SymbolTable handlerSymbolTable = null;
    SymbolTable actorSymbolTable = null;
    ArrayList<String> errors = new ArrayList<>();
    private boolean isInsideFor = false;

    private boolean isActorExist(String name){
        try{
            root.get(SymbolTableActorItem.STARTKEY + name);
            return true;
        }
        catch (ItemNotFoundException e){
            return false;
        }
    }

    public ArrayList<String> getErrors() {
        return this.errors;
    }

    @Override
    public Void visit(Program program) {
        if(program!=null){
            ArrayList<ActorDeclaration> declarations = program.getActors();
            for(ActorDeclaration declaration : declarations){
                declaration.accept(this);
            }
            Main main = program.getMain();
            if(main!=null){
                main.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visit(ActorDeclaration actorDeclaration) {
        if(actorDeclaration!=null){
            if(actorDeclaration.getParentName()!=null){
                if(!isActorExist(actorDeclaration.getParentName().getName())){
                    //compile err: 4
                    errors.add("Line:" + actorDeclaration.getLine() + ":actor " + actorDeclaration.getParentName().getName() + " is not declared");
                }
            }
            ArrayList<VarDeclaration> knownActors = actorDeclaration.getKnownActors();
            if(knownActors!=null){
                for(VarDeclaration knownActor : knownActors) {
                    ActorType type = (ActorType) knownActor.getType();
                    if(!isActorExist(type.getName().getName())) {
                        // compile err: 4
                        errors.add("Line:" + knownActor.getIdentifier().getLine() + ":actor " + knownActor.getType().toString() + " is not declared");
                        try {
                            ((SymbolTableVariableItem)((SymbolTableActorItem)root.get(SymbolTableActorItem.STARTKEY+actorDeclaration.getName().getName())).getActorSymbolTable().get(SymbolTableVariableItem.STARTKEY + knownActor.getIdentifier().getName())).setType(new NoType());
                        } catch (ItemNotFoundException e) {

                        }
                    }
                }
            }
            InitHandlerDeclaration initHandler = actorDeclaration.getInitHandler();
            ArrayList<MsgHandlerDeclaration> msgHandlers = actorDeclaration.getMsgHandlers();
            SymbolTable actorTable = null;
            SymbolTable handlerTable = null;
            if(initHandler!=null){
                try {
                    actorTable = ((SymbolTableActorItem)root.get(SymbolTableActorItem.STARTKEY+actorDeclaration.getName().getName())).getActorSymbolTable();
                    handlerTable = ((SymbolTableHandlerItem)actorTable.get(SymbolTableHandlerItem.STARTKEY+initHandler.getName().getName())).getHandlerSymbolTable();
                }
                catch (ItemNotFoundException e){
                    // err : handler or actor not found but I think it will never happened
                }
                this.actorSymbolTable = actorTable;
                this.handlerSymbolTable = handlerTable;
                visit(initHandler);
            }
            if(msgHandlers!=null){
                for(MsgHandlerDeclaration msgHandlerDeclaration : msgHandlers){
                    try {
                        actorTable = ((SymbolTableActorItem)root.get(SymbolTableActorItem.STARTKEY+actorDeclaration.getName().getName())).getActorSymbolTable();
                        handlerTable = ((SymbolTableHandlerItem)actorTable.get(SymbolTableHandlerItem.STARTKEY+msgHandlerDeclaration.getName().getName())).getHandlerSymbolTable();
                    }
                    catch (ItemNotFoundException e){
                        // err : handler or actor not found but I think it will never happened
                    }
                    this.actorSymbolTable = actorTable;
                    this.handlerSymbolTable = handlerTable;
                    visit(msgHandlerDeclaration);
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(HandlerDeclaration handlerDeclaration) {
        if(handlerDeclaration!=null) {
            ArrayList<Statement> statements = handlerDeclaration.getBody();
            if(statements!=null) {
                for(Statement statement : statements) {
                    visitStatement(statement);
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(VarDeclaration varDeclaration) {
        return null;
    }

    @Override
    public Void visit(Main mainActors) {
        if(mainActors!=null){
            try {
                SymbolTableMainItem main = (SymbolTableMainItem) root.get(SymbolTableMainItem.STARTKEY+"main");
                handlerSymbolTable = main.getMainSymbolTable();
            }
            catch (ItemNotFoundException e){
                // err :? main not exist
                errors.add("Line:" + mainActors.getLine() + ": main doesn't exist.");
            }
            ArrayList<ActorInstantiation> instantiations = mainActors.getMainActors();
            if(instantiations!=null){
                for(ActorInstantiation instantiation : instantiations){
                    visit(instantiation);
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(ActorInstantiation actorInstantiation) {
        if(actorInstantiation != null){
            ActorType type = (ActorType) actorInstantiation.getType();
            ActorDeclaration declaration;
            SymbolTable main = null;
            try {
                try {
                    main = ((SymbolTableMainItem) root.get(SymbolTableMainItem.STARTKEY + "main")).getMainSymbolTable();
                } catch(ItemNotFoundException ex) {}
                declaration = ((SymbolTableActorItem) root.get(SymbolTableActorItem.STARTKEY + type.toString().trim())).getActorDeclaration();
                ArrayList<Identifier> knownActorInput = actorInstantiation.getKnownActors();
                ArrayList<Expression> initArgsInput = actorInstantiation.getInitArgs();
                ArrayList<VarDeclaration> knownActorDefine = new ArrayList<>();
                if (declaration.getKnownActors() != null) {
                    knownActorDefine = declaration.getKnownActors();
                }
                ArrayList<VarDeclaration> initArgsDefine = new ArrayList<>();
                if (declaration.getInitHandler() != null) {
                    initArgsDefine = declaration.getInitHandler().getArgs();
                }
                if(knownActorInput.size() != knownActorDefine.size()){
                    // compile err: 8
                    errors.add("Line:" + actorInstantiation.getLine() + ": knownactors does not match with definition");
                    for (Identifier id : knownActorInput) {
                        ExpressionChecker checker = new ExpressionChecker(root,((SymbolTableMainItem)root.get(SymbolTableMainItem.STARTKEY + "main")).getMainSymbolTable(),handlerSymbolTable, errors);
                        checker.check(id);
                    }
                }
                else {
                    for(int i = 0;i < knownActorInput.size(); i++) {
                        ExpressionChecker checker = new ExpressionChecker(root,((SymbolTableMainItem)root.get(SymbolTableMainItem.STARTKEY + "main")).getMainSymbolTable(),handlerSymbolTable, errors);
                        Type checkedType = checker.check(knownActorInput.get(i));
                        if(!ExpressionChecker.isSubType(checkedType,knownActorDefine.get(i).getType())){
                            // compile err: 8
                            if (!(knownActorDefine.get(i).getType() instanceof NoType)) {
                                errors.add("Line:" + actorInstantiation.getLine() + ": knownactors does not match with definition");
                            }
                            break;
                        }
                    }
                }
                if(initArgsInput.size()!=initArgsDefine.size()) {
                    // compile err: not listed
                    errors.add("Line:" + actorInstantiation.getLine() + ": number of passed arguments(" + initArgsInput.size() + ") does not match number of required(" + initArgsDefine.size() + ").");
                    ExpressionChecker checker = new ExpressionChecker(root, main, handlerSymbolTable, errors);
                    for (Expression argument : initArgsInput) {
                            checker.check(argument);
                    }
                }
                else {
                    for(int i=0;i<initArgsInput.size();i++){
                        ExpressionChecker checker = new ExpressionChecker(root, main, handlerSymbolTable, errors);
                        Type checkedType = checker.check(initArgsInput.get(i));
                        if (!ExpressionChecker.isSubType(checkedType, initArgsDefine.get(i).getType())) {
                            // compile err: not listed
                            errors.add("Line:" + initArgsInput.get(i).getLine() + ":type of passed argument doesn't match with required type.");
                            break;
                        }
                    }
                }
            }
            catch (ItemNotFoundException e) {
                errors.add("Line:" + actorInstantiation.getLine() + ":actor " + type.getName().getName() + " is not declared");
                try {
                    ((SymbolTableVariableItem)main.get(SymbolTableVariableItem.STARTKEY + actorInstantiation.getIdentifier().getName())).setType(new NoType());
                } catch (ItemNotFoundException ex) {

                }
                ArrayList<Identifier> knownActorInput = actorInstantiation.getKnownActors();
                ArrayList<Expression> initArgsInput = actorInstantiation.getInitArgs();
                ExpressionChecker checker = new ExpressionChecker(root, main, handlerSymbolTable, errors);
                for(Identifier id : knownActorInput){
                    checker.check(id);
                }
                ExpressionChecker checker1 = new ExpressionChecker(root, main, handlerSymbolTable, errors);
                for(Expression argument : initArgsInput){
                    checker1.check(argument);
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(Block block) {
        if(block!=null){
            ArrayList<Statement> statements = block.getStatements();
            for(Statement statement : statements){
                visitStatement(statement);
            }
        }
        return null;
    }

    @Override
    public Void visit(Conditional conditional) {
        if(conditional!=null){
            Expression expression = conditional.getExpression();
            ExpressionChecker checker = new ExpressionChecker(root,actorSymbolTable,handlerSymbolTable, errors);
            if(!ExpressionChecker.isSubType(checker.check(expression),new BooleanType())) {
                // compile err: 3
                errors.add("Line:" + conditional.getLine() + ":condition type must be Boolean");
            }
            Statement thenBody = conditional.getThenBody();
            if(thenBody!=null){
                visitStatement(thenBody);
            }
            Statement elseBody = conditional.getElseBody();
            if(elseBody!=null){
                visitStatement(elseBody);
            }
        }
        return null;
    }

    @Override
    public Void visit(For loop) {
        if(loop!=null){
            Assign assignInit = loop.getInitialize();
            if(assignInit!=null){
                visitStatement(assignInit);
            }
            Expression condition = loop.getCondition();
            ExpressionChecker checker = new ExpressionChecker(root,actorSymbolTable,handlerSymbolTable, errors);
            if(!ExpressionChecker.isSubType(checker.check(condition),new BooleanType())){
                // compile err : 3
                errors.add("Line:" + condition.getLine() + ": condition type must be Boolean");
            }
            Assign assignUpdate = loop.getUpdate();
            if(assignUpdate!=null){
                visitStatement(assignUpdate);
            }
            Statement body = loop.getBody();
            if(body!=null){
                boolean isInsideForPrev = this.isInsideFor;
                this.isInsideFor = true;
                visitStatement(body);
                this.isInsideFor = isInsideForPrev;
            }
        }
        return null;
    }

    @Override
    public Void visit(Break breakLoop) {
        if(breakLoop!=null){
            if(!isInsideFor){
                // compile err : 10
                errors.add("Line:" + breakLoop.getLine() + ": break statement not within loop");
            }
        }
        return null;
    }

    @Override
    public Void visit(Continue continueLoop) {
        if(continueLoop!=null){
            if(!isInsideFor){
                // compile err : 10
                errors.add("Line:" + continueLoop.getLine() + ": continue statement not within loop");
            }
        }
        return null;
    }

    @Override
    public Void visit(MsgHandlerCall msgHandlerCall) {
//        SymbolTableHandlerItem handlerItem;
        if(msgHandlerCall!=null) {
            Expression instance = msgHandlerCall.getInstance();
            ExpressionChecker Checker = new ExpressionChecker(root,actorSymbolTable,handlerSymbolTable, errors);
            Type instanceType = Checker.check(instance);
            // ok
            if(instanceType instanceof DynamicType) {
                ExpressionChecker checker = new ExpressionChecker(root, actorSymbolTable, handlerSymbolTable, errors);
                ArrayList<Expression> msgHandlerCallArgs = msgHandlerCall.getArgs();
                for (Expression arg : msgHandlerCallArgs) {
                    checker.check(arg);
                }
            }
            else if(instanceType instanceof ActorType) {
                ActorType type = (ActorType) instanceType;
                SymbolTableActorItem currentItem = null;
                try {
                    currentItem = (SymbolTableActorItem) root.get(SymbolTableActorItem.STARTKEY + type.getName().getName());
                    boolean isDeclared = true;
                    SymbolTableHandlerItem handlerItem = null;
                    try {
                        handlerItem = (SymbolTableHandlerItem) currentItem.getActorSymbolTable().get(SymbolTableHandlerItem.STARTKEY + msgHandlerCall.getMsgHandlerName().getName());
                    } catch (ItemNotFoundException exception) {
                        isDeclared = false;
                    }
                    if(isDeclared){
                        ArrayList<Type> argTypes = handlerItem.getArgTypes();
                        ArrayList<Expression> msgHandlerArgs = msgHandlerCall.getArgs();
                        if (argTypes.size() != msgHandlerArgs.size()) {
                            //error : different size -> passed arguments with required args
                            errors.add("Line:" + msgHandlerCall.getLine() + ": number of passed arguments(" + argTypes.size() + ") is not the same as number of required(" + msgHandlerArgs.size() + ")");
                            for (Expression argument : msgHandlerArgs) {
                                ExpressionChecker checker = new ExpressionChecker(root, actorSymbolTable, handlerSymbolTable, errors);
                                checker.check(argument);
                            }
                        } else {
                            ExpressionChecker checker = new ExpressionChecker(root, actorSymbolTable, handlerSymbolTable, errors);
                            for (int i = 0; i < argTypes.size(); i++) {
                                if (!ExpressionChecker.isSubType(checker.check(msgHandlerArgs.get(i)), argTypes.get(i))) {
//                                    compile error : argument doesn't have same type
                                    errors.add("Line:" + msgHandlerArgs.get(i).getLine() + ":argument types is not the same as message handler args.");
                                    break;
                                }
                            }
                        }
                    } else {
                        errors.add("Line:" + msgHandlerCall.getLine() + ": there is no msghandler name " + msgHandlerCall.getMsgHandlerName().getName()
                                    + " in actor " + currentItem.getActorDeclaration().getName().getName());
                        ExpressionChecker checker = new ExpressionChecker(root, actorSymbolTable, handlerSymbolTable, errors);
                        ArrayList<Expression> msgHandlerCallArgs = msgHandlerCall.getArgs();
                        for (Expression arg : msgHandlerCallArgs) {
                            checker.check(arg);
                        }
                    }
                }
                catch (ItemNotFoundException e) {
                    //err: 4
                    errors.add("Line:" + msgHandlerCall.getLine() + ":actor " + type.getName().getName() + " is not declared");
                }
            }
            else {
                //err : 5
                if (!(instanceType instanceof NoType)) {
                    errors.add("Line:" + instance.getLine() + ": variable " + ((Identifier) instance).getName() + " is not callable");
                }
                ExpressionChecker checker = new ExpressionChecker(root, actorSymbolTable, handlerSymbolTable, errors);
                ArrayList<Expression> msgHandlerCallArgs = msgHandlerCall.getArgs();
                for (Expression arg : msgHandlerCallArgs) {
                    checker.check(arg);
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(Print print) {
        if(print!=null){
            Expression expression = print.getArg();
            if(expression!=null){
                ExpressionChecker checker = new ExpressionChecker(root,actorSymbolTable,handlerSymbolTable, errors);
                Type type = checker.check(expression);
                int size = 0;
                if(type instanceof ArrayType){
                    size = ((ArrayType) type).getSize();
                }
                boolean isValid = ExpressionChecker.isSubType(type,new IntType()) || ExpressionChecker.isSubType(type,new StringType()) || ExpressionChecker.isSubType(type,new BooleanType()) || ExpressionChecker.isSubType(type,new ArrayType(size));
                if(!isValid){
                    // compile err : 6
                    errors.add("Line:" + print.getLine() + ": unsupported type for print");
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        if(assign!=null){
            ExpressionChecker checker = new ExpressionChecker(root,actorSymbolTable,handlerSymbolTable, errors);
            Type LType = checker.check(assign.getlValue());
            Type RType = checker.check(assign.getrValue());
            boolean isLValue = ExpressionChecker.isLeftValue(assign.getlValue());
            if(!isLValue) {
                // compile err : 7
                errors.add("Line:" + assign.getlValue().getLine() + ": left side of assignment must be a valid lvalue");
            }
            if(!(LType instanceof NoType) && !ExpressionChecker.isSubType(RType,LType)) {
                //TODO : change subtype rule or this part of code because of notype may appears as leftvalue of assignment
                // compile err : 2
                errors.add("Line:" + assign.getlValue().getLine() + ": unsupported operand type for assign");
            }
        }
        return null;
    }
}
