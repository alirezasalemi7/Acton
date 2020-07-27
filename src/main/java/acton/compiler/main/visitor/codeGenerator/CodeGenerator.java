package acton.compiler.main.visitor.codeGenerator;

import acton.compiler.main.ast.node.Main;
import acton.compiler.main.ast.node.Program;
import acton.compiler.main.ast.node.declaration.ActorDeclaration;
import acton.compiler.main.ast.node.declaration.ActorInstantiation;
import acton.compiler.main.ast.node.declaration.VarDeclaration;
import acton.compiler.main.ast.node.declaration.handler.HandlerDeclaration;
import acton.compiler.main.ast.node.declaration.handler.InitHandlerDeclaration;
import acton.compiler.main.ast.node.declaration.handler.MsgHandlerDeclaration;
import acton.compiler.main.ast.node.expression.*;
import acton.compiler.main.ast.node.expression.operators.BinaryOperator;
import acton.compiler.main.ast.node.expression.operators.UnaryOperator;
import acton.compiler.main.ast.node.expression.values.BooleanValue;
import acton.compiler.main.ast.node.expression.values.IntValue;
import acton.compiler.main.ast.node.expression.values.StringValue;
import acton.compiler.main.ast.node.statement.*;
import acton.compiler.main.ast.type.DynamicType;
import acton.compiler.main.ast.type.Type;
import acton.compiler.main.ast.type.actorType.ActorType;
import acton.compiler.main.ast.type.arrayType.ArrayType;
import acton.compiler.main.ast.type.primitiveType.BooleanType;
import acton.compiler.main.ast.type.primitiveType.IntType;
import acton.compiler.main.ast.type.primitiveType.StringType;
import acton.compiler.main.symbolTable.*;
import acton.compiler.main.symbolTable.itemException.ItemNotFoundException;
import acton.compiler.main.symbolTable.symbolTableVariableItem.SymbolTableActorVariableItem;
import acton.compiler.main.symbolTable.symbolTableVariableItem.SymbolTableKnownActorItem;
import acton.compiler.main.symbolTable.symbolTableVariableItem.SymbolTableVariableItem;
import acton.compiler.main.visitor.Visitor;
import acton.compiler.main.visitor.typeChecker.ExpressionChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CodeGenerator implements Visitor<Object> {

    private SymbolTable root;
    private JasminFile defaultActorFile;
    private JasminMethod currentMethod = null;
    private SymbolTable currentScope = null;
    private int labelIndex = 0;
    private String labelAfter = "";
    private String labelBreak = "";
    private String labelContinue = "";
    private String currentActorName = "";
    private HashMap<String,Boolean> seenMethods = new HashMap<>();

    public CodeGenerator(){
        root = SymbolTable.root;
        defaultActorFile = new JasminFile("DefaultActor","java/lang/Thread");
        defaultActorFile.addMethod(defaultActorInit());
    }

    String newLabel(){
        String nlabel = "label_" + labelIndex;
        labelIndex++;
        return nlabel;
    }

    private int stackSizeLimit(){
        return 16;
    }

    private int sizeOfSlots(ArrayList<VarDeclaration> declarations,ArrayList<VarDeclaration> args){
        return declarations.size() + 2 + args.size();
    }

    String getVarType(Type type){
        if(type instanceof IntType){
            return "I";
        }
        else if(type instanceof BooleanType){
            return "I";
        }
        else if(type instanceof StringType){
            return "Ljava/lang/String;";
        }
        else if(type instanceof ArrayType){
            return "[I";
        }
        else if(type instanceof ActorType){
            ActorType actorType = (ActorType)type;
            return "L" + actorType.getName().getName()+";";
        }
        return null;
    }

    JasminMethod defaultActorMethodMaker(String msgHandlerName,String inputSig,String outputSig,int locals){
        JasminMethod method = new JasminMethod(2,locals,"send_"+msgHandlerName,inputSig,outputSig);
        method.addByteCode(InstructionGenerator.getStatic("java/lang/System/out","Ljava/io/PrintStream;"));
        method.addByteCode(InstructionGenerator.pushString("\"there is no msghandler named " + msgHandlerName + " in sender\""));
        method.addByteCode(InstructionGenerator.invokeVirtual("java/io/PrintStream/println","Ljava/lang/String;","V"));
        method.addByteCode(InstructionGenerator.returnVoid());
        return method;
    }

    JasminMethod defultConstructor(String inits){
        JasminMethod method = new JasminMethod(5,2,"<init>","I","V");
        method.addByteCode(InstructionGenerator.aload(0));
        method.addByteCode(InstructionGenerator.iload(1));
        method.addByteCode(InstructionGenerator.invokeSpecial("Actor/<init>","I","V"));
        method.addByteCode(inits);
        method.addByteCode(InstructionGenerator.returnVoid());
        return method;
    }

    JasminMethod defaultActorInit(){
        JasminMethod method = new JasminMethod(1,1,"<init>","","V");
        method.addByteCode(InstructionGenerator.aload(0));
        method.addByteCode(InstructionGenerator.invokeSpecial("java/lang/Thread/<init>","","V"));
        method.addByteCode(InstructionGenerator.returnVoid());
        return method;
    }

    @Override
    public String visit(Program program) {
        if(program!=null){
            ArrayList<ActorDeclaration> declarations = program.getActors();
            if(declarations!=null){
                for(ActorDeclaration actor : declarations){
                    try {
                        currentScope = ((SymbolTableActorItem)root.get(SymbolTableActorItem.STARTKEY + actor.getName().getName())).getActorSymbolTable();
                    }
                    catch (ItemNotFoundException e){

                    }
                    currentActorName = actor.getName().getName();
                    JasminFile file = visit(actor);
                    file.writeFile();
                }
            }
            Main main = program.getMain();
            if(main!=null){
                try {
                    currentScope = ((SymbolTableMainItem)root.get(SymbolTableMainItem.STARTKEY + "main")).getMainSymbolTable();
                }
                catch (ItemNotFoundException e){

                }
                JasminFile file = visit(main);
                file.writeFile();
            }
            defaultActorFile.writeFile();
        }
        return null;
    }

    @Override
    public JasminFile visit(ActorDeclaration actorDeclaration) {
        if(actorDeclaration!=null){
            String className = actorDeclaration.getName().getName();
            JasminFile actorFile = new JasminFile(className,"Actor");
            ArrayList<VarDeclaration> knownActors = actorDeclaration.getKnownActors();
            JasminMethod setKnownActorsMethod = new JasminMethod(16,knownActors.size()+1,"setKnownActors",getInputSigniture(knownActors),"V");
            int i = 1;
            for(VarDeclaration knownActor : knownActors){
                String knownActorName = knownActor.getIdentifier().getName();
                String type = getVarType(knownActor.getType());
                actorFile.addField(InstructionGenerator.fieldDefinition(knownActorName,type,null));
                setKnownActorsMethod.addByteCode(InstructionGenerator.aload(0));
                setKnownActorsMethod.addByteCode(InstructionGenerator.aload(i));
                setKnownActorsMethod.addByteCode(InstructionGenerator.putField(actorDeclaration.getName().getName()+"/"+knownActor.getIdentifier().getName(),getVarType(knownActor.getType())));
                i++;
            }
            setKnownActorsMethod.addByteCode(InstructionGenerator.returnVoid());
            ArrayList<VarDeclaration> actorVars = actorDeclaration.getActorVars();
            StringBuilder inits = new StringBuilder();
            for(VarDeclaration var : actorVars){
                String varName = var.getIdentifier().getName();
                String varType = getVarType(var.getType());
                inits.append(InstructionGenerator.aload(0));
                if(var.getType() instanceof ArrayType){
                    inits.append(InstructionGenerator.bipush(((ArrayType) var.getType()).getSize()));
                    inits.append(InstructionGenerator.inewarray());
                    inits.append(InstructionGenerator.putField(actorDeclaration.getName().getName()+"/"+var.getIdentifier().getName(),"[I"));
                }
                else if(var.getType() instanceof StringType){
                    inits.append(InstructionGenerator.pushString("\"\""));
                    inits.append(InstructionGenerator.putField(actorDeclaration.getName().getName()+"/"+var.getIdentifier().getName(),"Ljava/lang/String;"));
                }
                else if(var.getType() instanceof IntType || var.getType() instanceof BooleanType) {
                    inits.append(InstructionGenerator.bipush(0));
                    inits.append(InstructionGenerator.putField(actorDeclaration.getName().getName()+"/"+var.getIdentifier().getName(),"I"));
                }
                actorFile.addField(InstructionGenerator.fieldDefinition(varName,varType,null));
            }
            actorFile.addMethod(defultConstructor(inits.toString()));
            actorFile.addMethod(setKnownActorsMethod);
            InitHandlerDeclaration initHandler = actorDeclaration.getInitHandler();
            SymbolTable temp = currentScope;
            if(initHandler!=null){
                try{
                    currentScope = ((SymbolTableHandlerItem)currentScope.get(SymbolTableHandlerItem.STARTKEY + initHandler.getName().getName())).getHandlerSymbolTable();
                }
                catch (ItemNotFoundException e){

                }
                JasminMethod method = visit(initHandler);
                actorFile.addMethod(method);
            }
            currentScope = temp;
            ArrayList<MsgHandlerDeclaration> handlerDeclarations = actorDeclaration.getMsgHandlers();
            for(MsgHandlerDeclaration handlerDeclaration : handlerDeclarations){
                try{
                    currentScope = ((SymbolTableHandlerItem)currentScope.get(SymbolTableHandlerItem.STARTKEY + handlerDeclaration.getName().getName())).getHandlerSymbolTable();
                }
                catch (ItemNotFoundException e){

                }
                JasminMethod method = visit(handlerDeclaration);
                method.addByteCode(InstructionGenerator.returnVoid());
                actorFile.addMethod(method);
                if(!seenMethods.containsKey(handlerDeclaration.getName().getName()+"@"+getInputSigniture(handlerDeclaration.getArgs()))){
                    defaultActorFile.addMethod(defaultActorMethodMaker(method.getMethodName(),method.getInputSig(),method.getOutputSig(),method.getLocalsLimit()));
                    seenMethods.put(handlerDeclaration.getName().getName()+"@"+getInputSigniture(handlerDeclaration.getArgs()),true);
                }
                JasminFile methodClass = actor_msgHnandlerFile(actorDeclaration,handlerDeclaration);
                methodClass.writeFile();
                JasminMethod send__msg = send_msgHandler(actorDeclaration,handlerDeclaration);
                actorFile.addMethod(send__msg);
            }
            return actorFile;
        }
        return null;
    }

    JasminMethod send_msgHandler(ActorDeclaration actor,MsgHandlerDeclaration msgHandlerDeclaration){
        ArrayList<VarDeclaration> args = msgHandlerDeclaration.getArgs();
        String methodName = "send_"+msgHandlerDeclaration.getName().getName();
        String inputSig = getInputSigniture(args);
        JasminMethod method = new JasminMethod(16,args.size()+2,methodName,"LActor;"+inputSig,"V");
        method.addByteCode(InstructionGenerator.aload(0));
        String messagerCls = actor.getName().getName()+"_"+msgHandlerDeclaration.getName().getName();
        method.addByteCode(InstructionGenerator.anew(messagerCls));
        method.addByteCode(InstructionGenerator.dup());
        method.addByteCode(InstructionGenerator.aload(0));
        method.addByteCode(InstructionGenerator.aload(1));
        int i = 2;
        for(VarDeclaration declaration : args){
            String sig = getVarType(declaration.getType());
            if(sig.equals("I")){
                method.addByteCode(InstructionGenerator.iload(i));
            }
            else{
                method.addByteCode(InstructionGenerator.aload(i));
            }
            i++;
        }
        String callSig = "L"+actor.getName().getName()+";LActor;"+inputSig;
        method.addByteCode(InstructionGenerator.invokeSpecial(messagerCls+"/<init>",callSig,"V"));
        method.addByteCode(InstructionGenerator.invokeVirtual(actor.getName().getName()+"/send","LMessage;","V"));
        method.addByteCode(InstructionGenerator.returnVoid());
        return method;
    }

    JasminFile actor_msgHnandlerFile(ActorDeclaration actor,MsgHandlerDeclaration msgHandlerDeclaration){
        String clsName = actor.getName().getName()+"_"+msgHandlerDeclaration.getName().getName();
        JasminFile file = new JasminFile(clsName,"Message");
        String actorSig = "L"+actor.getName().getName()+";";
        file.addField(InstructionGenerator.fieldDefinition("receiver",actorSig,null));
        file.addField(InstructionGenerator.fieldDefinition("sender","LActor;",null));
        ArrayList<VarDeclaration> args = msgHandlerDeclaration.getArgs();
        for(VarDeclaration declaration : args){
            file.addField(InstructionGenerator.fieldDefinition(declaration.getIdentifier().getName(),getVarType(declaration.getType()),null));
        }
        JasminMethod initMethod = new JasminMethod(16,args.size()+3,"<init>",actorSig+"LActor;"+getInputSigniture(args),"V");
        initMethod.addByteCode(InstructionGenerator.aload(0));
        initMethod.addByteCode(InstructionGenerator.invokeSpecial("Message/<init>","","V"));
        initMethod.addByteCode(InstructionGenerator.aload(0));
        initMethod.addByteCode(InstructionGenerator.aload(1));
        initMethod.addByteCode(InstructionGenerator.putField(clsName+"/receiver",actorSig));
        initMethod.addByteCode(InstructionGenerator.aload(0));
        initMethod.addByteCode(InstructionGenerator.aload(2));
        initMethod.addByteCode(InstructionGenerator.putField(clsName+"/sender","LActor;"));
        int i = 3;
        for(VarDeclaration declaration : args){
            String sig = getVarType(declaration.getType());
            initMethod.addByteCode(InstructionGenerator.aload(0));
            if(sig.equals("I")){
                initMethod.addByteCode(InstructionGenerator.iload(i));
            }
            else{
                initMethod.addByteCode(InstructionGenerator.aload(i));
            }
            initMethod.addByteCode(InstructionGenerator.putField(clsName+"/"+declaration.getIdentifier().getName(),sig));
            i++;
        }
        initMethod.addByteCode(InstructionGenerator.returnVoid());
        file.addMethod(initMethod);
        JasminMethod executeMethod = new JasminMethod(16,1,"execute","","V");
        executeMethod.addByteCode(InstructionGenerator.aload(0));
        executeMethod.addByteCode(InstructionGenerator.getField(clsName+"/receiver",actorSig));
        executeMethod.addByteCode(InstructionGenerator.aload(0));
        executeMethod.addByteCode(InstructionGenerator.getField(clsName+"/sender","LActor;"));
        for(VarDeclaration declaration : args){
            String sig = getVarType(declaration.getType());
            executeMethod.addByteCode(InstructionGenerator.aload(0));
            executeMethod.addByteCode(InstructionGenerator.getField(clsName+"/"+declaration.getIdentifier().getName(),sig));
        }
        String inputSig = getInputSigniture(args);
        executeMethod.addByteCode(InstructionGenerator.invokeVirtual(actor.getName().getName()+"/"+msgHandlerDeclaration.getName().getName(),"LActor;"+inputSig,"V"));
        executeMethod.addByteCode(InstructionGenerator.returnVoid());
        file.addMethod(executeMethod);
        return file;
    }

    String getInputSigniture(ArrayList<VarDeclaration> declarations){
        StringBuilder builder = new StringBuilder();
        for(VarDeclaration declaration : declarations){
            builder.append(getVarType(declaration.getType()));
        }
        return builder.toString();
    }

    @Override
    public JasminMethod visit(HandlerDeclaration handlerDeclaration) {
        if(handlerDeclaration!=null){
            String methodName = handlerDeclaration.getName().getName();
            int locals = sizeOfSlots(handlerDeclaration.getLocalVars(),handlerDeclaration.getArgs());
            String temp = (handlerDeclaration instanceof InitHandlerDeclaration)? "":"LActor;";
            JasminMethod method = new JasminMethod(stackSizeLimit(),locals,methodName,temp+getInputSigniture(handlerDeclaration.getArgs()),"V");
            this.currentMethod = method;
            method.setSlotMapper(handlerDeclaration.getArgs(),handlerDeclaration.getLocalVars(),handlerDeclaration.getName().getName().equals("initial"));
            for(VarDeclaration declaration : handlerDeclaration.getLocalVars()){
                if(declaration.getType() instanceof StringType){
                    method.addByteCode(InstructionGenerator.pushString("\"\""));
                    method.addByteCode(InstructionGenerator.astore(method.getMapper().slotOf(declaration.getIdentifier())));
                }
                else if(declaration.getType() instanceof ArrayType){
                    method.addByteCode(InstructionGenerator.bipush(((ArrayType) declaration.getType()).getSize()));
                    method.addByteCode(InstructionGenerator.inewarray());
                    method.addByteCode(InstructionGenerator.astore(method.getMapper().slotOf(declaration.getIdentifier())));
                }
                else if(declaration.getType() instanceof IntType || declaration.getType() instanceof BooleanType){
                    method.addByteCode(InstructionGenerator.bipush(0));
                    method.addByteCode(InstructionGenerator.istore(method.getMapper().slotOf(declaration.getIdentifier())));
                }
            }
            ArrayList<Statement> statements = handlerDeclaration.getBody();
            for(Statement statement : statements){
                String byteCode = callStatement(statement);
                method.addByteCode(byteCode);
            }
            method.addByteCode(InstructionGenerator.returnVoid());
            this.currentMethod = null;
            return method;
        }
        return null;
    }

    String callStatement(Statement stat){
        if( stat == null )
            return null;
        else if( stat instanceof MsgHandlerCall )
            return this.visit( ( MsgHandlerCall ) stat );
        else if( stat instanceof Block )
            return this.visit( ( Block ) stat );
        else if( stat instanceof Conditional )
            return this.visit( ( Conditional ) stat );
        else if( stat instanceof For )
            return this.visit( ( For ) stat );
        else if( stat instanceof Break )
            return this.visit( ( Break ) stat );
        else if( stat instanceof Continue )
            return this.visit( ( Continue ) stat );
        else if( stat instanceof Print )
            return this.visit( ( Print ) stat );
        else if( stat instanceof Assign )
            return this.visit( ( Assign ) stat );
        return null;
    }

    String callExpression(Expression expr,boolean ...lValue){
        if( expr == null )
            return null;
        else if( expr instanceof UnaryExpression )
            return this.visit( ( UnaryExpression ) expr );
        else if( expr instanceof BinaryExpression )
            return this.visit( ( BinaryExpression ) expr );
        else if( expr instanceof ArrayCall ) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.visit((ArrayCall) expr));
            if(lValue.length==0){
                builder.append(InstructionGenerator.iaload());
                return builder.toString();
            }
            return builder.toString();
        }
        else if( expr instanceof ActorVarAccess )
            return this.visit( ( ActorVarAccess ) expr );
        else if( expr instanceof Identifier )
            return this.visit( ( Identifier ) expr );
        else if( expr instanceof Self )
            return this.visit( ( Self ) expr );
        else if( expr instanceof Sender )
            return this.visit( ( Sender ) expr );
        else if( expr instanceof BooleanValue )
            return this.visit( ( BooleanValue ) expr );
        else if( expr instanceof IntValue )
            return this.visit( ( IntValue ) expr );
        else if( expr instanceof StringValue )
            return this.visit( ( StringValue ) expr );
        return null;
    }

    @Override
    public String visit(VarDeclaration varDeclaration) {        // never reached
        return null;
    }

    int getActorQueueSize(String actorname){
        try {
            SymbolTableActorItem item = (SymbolTableActorItem) root.get(SymbolTableActorItem.STARTKEY + actorname);
            return item.getActorDeclaration().getQueueSize();
        }
        catch (ItemNotFoundException e){

        }
        return -1;
    }

    ActorDeclaration getActorDeclaration(String actorName){
        try {
            SymbolTableActorItem item = (SymbolTableActorItem) root.get(SymbolTableActorItem.STARTKEY + actorName);
            return item.getActorDeclaration();
        }
        catch (ItemNotFoundException e){

        }
        return null;
    }

    JasminMethod objectInit(){
        JasminMethod method = new JasminMethod(1,1,"<init>","","V");
        method.addByteCode(InstructionGenerator.aload(0));
        method.addByteCode(InstructionGenerator.invokeSpecial("java/lang/Object/<init>","","V"));
        method.addByteCode(InstructionGenerator.returnVoid());
        return method;
    }

    @Override
    public JasminFile visit(Main mainActors) {
        JasminFile mainFile = new JasminFile("Main","java/lang/Object");
        mainFile.addMethod(objectInit());
        JasminMethod mainMethod = new JasminMethod(16,mainActors.getMainActors().size()+1,"main","[Ljava/lang/String;","V");
        mainMethod.staticMthd = true;
        int i = 1;
        Map<String,Integer> slots = new HashMap<>();
        ArrayList<ActorInstantiation> actors = mainActors.getMainActors();
        for(ActorInstantiation actor : actors){
            String className = ((ActorType)actor.getType()).getName().getName();
            mainMethod.addByteCode(InstructionGenerator.anew(className));
            mainMethod.addByteCode(InstructionGenerator.dup());
            mainMethod.addByteCode(InstructionGenerator.bipush(getActorQueueSize(className)));
            mainMethod.addByteCode(InstructionGenerator.invokeSpecial(className+"/<init>","I","V"));
            mainMethod.addByteCode(InstructionGenerator.astore(i));
            slots.put(actor.getIdentifier().getName(),i);
            i++;
        }
        i = 1;
        for(ActorInstantiation actor : actors){
            String className = ((ActorType)actor.getType()).getName().getName();
            ArrayList<Identifier> knownActors = actor.getKnownActors();
            if(knownActors.size()>0){
                mainMethod.addByteCode(InstructionGenerator.aload(i));
                for(Identifier knownActor : knownActors){
                    mainMethod.addByteCode(InstructionGenerator.aload(slots.get(knownActor.getName())));
                }
                String inputSig = getInputSigniture(getActorDeclaration(className).getKnownActors());
                mainMethod.addByteCode(InstructionGenerator.invokeVirtual(className+"/setKnownActors",inputSig,"V"));
            }
            i++;
        }
        i = 1;
        for(ActorInstantiation actor : actors){
            String className = ((ActorType)actor.getType()).getName().getName();
            ArrayList<Expression> initArgs = actor.getInitArgs();
            if(getActorDeclaration(className).getInitHandler()!=null){
                // todo :: here error
                mainMethod.addByteCode(InstructionGenerator.aload(i));
                for(Expression expression : initArgs){
                    mainMethod.addByteCode(callExpression(expression));
                }
                String inputSig = getInputSigniture(getActorDeclaration(className).getInitHandler().getArgs());
                mainMethod.addByteCode(InstructionGenerator.invokeVirtual(className+"/initial",inputSig,"V"));
            }
            i++;
        }
        i = 1;
        for(ActorInstantiation actor : actors){
            String className = ((ActorType)actor.getType()).getName().getName();
            mainMethod.addByteCode(InstructionGenerator.aload(i));
            mainMethod.addByteCode(InstructionGenerator.invokeVirtual(className+"/start","","V"));
            i++;
        }
        mainMethod.addByteCode(InstructionGenerator.returnVoid());
        mainFile.addMethod(mainMethod);
        return mainFile;
    }

    @Override
    public String visit(ActorInstantiation actorInstantiation) {        // never reached
        return null;
    }

    @Override
    public String visit(UnaryExpression unaryExpression) {
        UnaryOperator operator = unaryExpression.getUnaryOperator();
        StringBuilder builder = new StringBuilder();
        if(operator == UnaryOperator.minus){
            builder.append(callExpression(unaryExpression.getOperand()));
            builder.append(InstructionGenerator.ineg());
        }
        else if(operator == UnaryOperator.not){
            builder.append(callExpression(unaryExpression.getOperand()));
            String labelTrue = newLabel();
            String labelEnd = newLabel();
            builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.eq));
            builder.append(InstructionGenerator.bipush(0));
            builder.append(InstructionGenerator.gotoLabel(labelEnd));
            builder.append(labelTrue+":\n");
            builder.append(InstructionGenerator.bipush(1));
            builder.append(labelEnd+":\n");
        }
        else if(operator == UnaryOperator.predec || operator == UnaryOperator.preinc){
            builder.append(storePostAndPre(operator,unaryExpression));
        }
        else if(operator == UnaryOperator.postdec || operator == UnaryOperator.postinc){
            builder.append(storePostAndPre(operator,unaryExpression));
        }
        return builder.toString();
    }

    String storePostAndPre(UnaryOperator operator,UnaryExpression unaryExpression){
        StringBuilder builder = new StringBuilder();
        StringBuilder value = new StringBuilder();
        // make value
        value.append(callExpression(unaryExpression.getOperand()));
        value.append(InstructionGenerator.bipush(1));
        value.append((operator==UnaryOperator.postdec || operator == UnaryOperator.predec)?InstructionGenerator.isub():InstructionGenerator.iadd());
        //
        if(operator==UnaryOperator.postdec || operator==UnaryOperator.postinc){
            builder.append(callExpression(unaryExpression.getOperand()));
        }
        if(unaryExpression.getOperand() instanceof Identifier){
            builder.append(storeVar(unaryExpression.getOperand(),value.toString(),false));
        }
        else if(unaryExpression.getOperand() instanceof ArrayCall){
            ArrayCall call = (ArrayCall) unaryExpression.getOperand();
            if(call.getArrayInstance() instanceof ActorVarAccess){
                builder.append(InstructionGenerator.aload(0));
                Identifier id = ((ActorVarAccess) call.getArrayInstance()).getVariable();
                builder.append(InstructionGenerator.getField(currentActorName+"/"+id.getName(),"[I"));
                builder.append(callExpression(call.getIndex()));
                builder.append(value.toString());
                builder.append(InstructionGenerator.iastore());
            }
            else{
                Identifier id =(Identifier) call.getArrayInstance();
                int slot = currentMethod.getMapper().slotOf(call.getArrayInstance());
                if(slot!=-1){
                    builder.append(InstructionGenerator.aload(slot));
                    builder.append(callExpression(call.getIndex()));
                    builder.append(value.toString());
                    builder.append(InstructionGenerator.iastore());
                }
                else{
                    builder.append(InstructionGenerator.aload(0));
                    builder.append(InstructionGenerator.getField(currentActorName+"/"+id.getName(),"[I"));
                    builder.append(callExpression(call.getIndex()));
                    builder.append(value.toString());
                    builder.append(InstructionGenerator.iastore());
                }
            }
        }
        else if(unaryExpression.getOperand() instanceof ActorVarAccess){
            Expression operand = ((ActorVarAccess) unaryExpression.getOperand()).getVariable();
            builder.append(storeVar(operand,value.toString(),true));
        }
        if(operator==UnaryOperator.predec || operator == UnaryOperator.preinc){
            builder.append(callExpression(unaryExpression.getOperand()));
        }
        return builder.toString();
    }

    String storeVar(Expression var,String value,boolean self){
        StringBuilder builder = new StringBuilder();
        if(var instanceof Identifier){
            int res = currentMethod.getMapper().slotOf(var);
            if(res!=-1 && self!=true){
                ExpressionChecker checker = new ExpressionChecker(root,currentScope.getPreSymbolTable(),currentScope,null);
                Type type = checker.check(var);
                builder.append(value);
                if(var instanceof Identifier){
                    if(type instanceof ActorType || type instanceof ArrayType || type instanceof StringType){
                        builder.append(InstructionGenerator.astore(res));
                    }
                    else{
                        builder.append(InstructionGenerator.istore(res));
                    }
                }
            }
            else{
                ExpressionChecker checker = new ExpressionChecker(root,currentScope.getPreSymbolTable(),currentScope.getPreSymbolTable(),null);
                Type type = checker.check(var);
                Identifier identifier = (Identifier) var;
                builder.append(InstructionGenerator.aload(0));
                builder.append(value);
                if(type instanceof ActorType){
                    String field = currentActorName + "/"+identifier.getName();
                    builder.append(InstructionGenerator.putField(field,"L"+((ActorType) type).getName()+";"));
                }
                else if(type instanceof IntType || type instanceof BooleanType){
                    String field = currentActorName + "/"+identifier.getName();
                    builder.append(InstructionGenerator.putField(field,"I"));
                }
                else if(type instanceof StringType){
                    String field = currentActorName + "/"+identifier.getName();
                    builder.append(InstructionGenerator.putField(field,"Ljava/lang/String;"));
                }
                else if(type instanceof ArrayType){
                    String field = currentActorName + "/"+identifier.getName();
                    builder.append(InstructionGenerator.putField(field,"[I"));
                }
            }
        }
        return builder.toString();
    }

    @Override
    public String visit(BinaryExpression binaryExpression) {
        String left = callExpression(binaryExpression.getLeft());
        String right = callExpression(binaryExpression.getRight());
        StringBuilder builder = new StringBuilder();
        if(binaryExpression.getBinaryOperator()!= BinaryOperator.assign){
            builder.append(left);
        }
        switch (binaryExpression.getBinaryOperator()){
            case mult: {
                builder.append(right);
                builder.append(InstructionGenerator.imul());
            }break;
            case add:{
                builder.append(right);
                builder.append(InstructionGenerator.iadd());
            }break;
            case sub:{
                builder.append(right);
                builder.append(InstructionGenerator.isub());
            }break;
            case div:{
                builder.append(right);
                builder.append(InstructionGenerator.idiv());
            }break;
            case mod:{
                builder.append(right);
                builder.append(InstructionGenerator.irem());
            }break;
            case or:{
                String labelTrue = newLabel();
                String labelEnd = newLabel();
                builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.ne));
                builder.append(right);
                builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.ne));
                builder.append(InstructionGenerator.bipush(0));
                builder.append(InstructionGenerator.gotoLabel(labelEnd));
                builder.append(labelTrue+":\n");
                builder.append(InstructionGenerator.bipush(1));
                builder.append(labelEnd+":\n");
            }break;
            case and:{
                String labelFalse = newLabel();
                String labelEnd = newLabel();
                builder.append(InstructionGenerator.branchIf(labelFalse,InstructionGenerator.IfConditionMode.eq));
                builder.append(right);
                builder.append(InstructionGenerator.branchIf(labelFalse,InstructionGenerator.IfConditionMode.eq));
                builder.append(InstructionGenerator.bipush(1));
                builder.append(InstructionGenerator.gotoLabel(labelEnd));
                builder.append(labelFalse+":\n");
                builder.append(InstructionGenerator.bipush(0));
                builder.append(labelEnd+":\n");
            }break;
            case lt:{
                builder.append(right);
                String labelTrue = newLabel();
                String labelEnd = newLabel();
                builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.multiOperandLt));
                builder.append(InstructionGenerator.bipush(0));
                builder.append(InstructionGenerator.gotoLabel(labelEnd));
                builder.append(labelTrue+":\n");
                builder.append(InstructionGenerator.bipush(1));
                builder.append(labelEnd+":\n");
            }break;
            case gt:{
                builder.append(right);
                String labelTrue = newLabel();
                String labelEnd = newLabel();
                builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.multiOperandGt));
                builder.append(InstructionGenerator.bipush(0));
                builder.append(InstructionGenerator.gotoLabel(labelEnd));
                builder.append(labelTrue+":\n");
                builder.append(InstructionGenerator.bipush(1));
                builder.append(labelEnd+":\n");
            }break;
            case neq:{
                builder.append(right);
                ExpressionChecker checker = new ExpressionChecker(root,currentScope.getPreSymbolTable(),currentScope,null);
                Type typeL = checker.check(binaryExpression.getLeft());
                Type typeR = checker.check(binaryExpression.getRight());
                if(typeL instanceof StringType && typeR instanceof StringType){
                    String labelTrue = newLabel();
                    String labelEnd = newLabel();
                    builder.append(InstructionGenerator.invokeVirtual("java/lang/String/equals","Ljava/lang/Object;","Z"));
                    builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.eq));
                    builder.append(InstructionGenerator.bipush(0));
                    builder.append(InstructionGenerator.gotoLabel(labelEnd));
                    builder.append(labelTrue+":\n");
                    builder.append(InstructionGenerator.bipush(1));
                    builder.append(labelEnd+":\n");
                }
                else if(typeL instanceof ArrayType && typeR instanceof ArrayType){
                    String labelTrue = newLabel();
                    String labelEnd = newLabel();
                    builder.append(InstructionGenerator.invokeStatic("java/util/Arrays/equals","[I[I","Z"));
                    builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.eq));
                    builder.append(InstructionGenerator.bipush(0));
                    builder.append(InstructionGenerator.gotoLabel(labelEnd));
                    builder.append(labelTrue+":\n");
                    builder.append(InstructionGenerator.bipush(1));
                    builder.append(labelEnd+":\n");
                }
                else if((typeL instanceof ActorType || typeL instanceof DynamicType) && ExpressionChecker.isSubType(typeL,typeR)){
                    String labelTrue = newLabel();
                    String labelEnd = newLabel();
                    builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.ane));
                    builder.append(InstructionGenerator.bipush(0));
                    builder.append(InstructionGenerator.gotoLabel(labelEnd));
                    builder.append(labelTrue+":\n");
                    builder.append(InstructionGenerator.bipush(1));
                    builder.append(labelEnd+":\n");
                }
                else {
                    String labelTrue = newLabel();
                    String labelEnd = newLabel();
                    builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.multiOperandNe));
                    builder.append(InstructionGenerator.bipush(0));
                    builder.append(InstructionGenerator.gotoLabel(labelEnd));
                    builder.append(labelTrue+":\n");
                    builder.append(InstructionGenerator.bipush(1));
                    builder.append(labelEnd+":\n");
                }
            }break;
            case eq:{
                builder.append(right);
                ExpressionChecker checker = new ExpressionChecker(root,currentScope.getPreSymbolTable(),currentScope,null);
                Type typeL = checker.check(binaryExpression.getLeft());
                Type typeR = checker.check(binaryExpression.getRight());
                if(typeL instanceof StringType && typeR instanceof StringType){
                    builder.append(InstructionGenerator.invokeVirtual("java/lang/String/equals","Ljava/lang/Object;","Z"));
                }
                else if(typeL instanceof ArrayType && typeR instanceof ArrayType){
                    String labelTrue = newLabel();
                    String labelEnd = newLabel();
                    builder.append(InstructionGenerator.invokeStatic("java/util/Arrays/equals","[I[I","Z"));
                    builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.ne));
                    builder.append(InstructionGenerator.bipush(0));
                    builder.append(InstructionGenerator.gotoLabel(labelEnd));
                    builder.append(labelTrue+":\n");
                    builder.append(InstructionGenerator.bipush(1));
                    builder.append(labelEnd+":\n");
                }
                else if((typeL instanceof ActorType || typeL instanceof DynamicType)&& ExpressionChecker.isSubType(typeL,typeR)){
                    String labelTrue = newLabel();
                    String labelEnd = newLabel();
                    builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.aeq));
                    builder.append(InstructionGenerator.bipush(0));
                    builder.append(InstructionGenerator.gotoLabel(labelEnd));
                    builder.append(labelTrue+":\n");
                    builder.append(InstructionGenerator.bipush(1));
                    builder.append(labelEnd+":\n");
                }
                else {
                    String labelTrue = newLabel();
                    String labelEnd = newLabel();
                    builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.multiOperandEq));
                    builder.append(InstructionGenerator.bipush(0));
                    builder.append(InstructionGenerator.gotoLabel(labelEnd));
                    builder.append(labelTrue+":\n");
                    builder.append(InstructionGenerator.bipush(1));
                    builder.append(labelEnd+":\n");
                }
            }break;
            case assign:{
                Expression leftexpr = binaryExpression.getLeft();
                if(leftexpr instanceof Identifier){
                    builder.append(storeVar(leftexpr,right,false));
                }
                else if(leftexpr instanceof ActorVarAccess){
                    builder.append(storeVar(((ActorVarAccess) leftexpr).getVariable(),right,true));
                }
                else if(leftexpr instanceof ArrayCall){
                    if(((ArrayCall) leftexpr).getArrayInstance() instanceof Identifier){
                        builder.append(callExpression(((ArrayCall) leftexpr).getArrayInstance()));
                        builder.append(callExpression(((ArrayCall) leftexpr).getIndex()));
                        builder.append(right);
                        builder.append(InstructionGenerator.iastore());
                    }
                    else if(((ArrayCall) leftexpr).getArrayInstance() instanceof ActorVarAccess) {
                        builder.append(InstructionGenerator.aload(0));
                        builder.append(right);
                        builder.append(InstructionGenerator.putField(currentActorName+"/"+((ActorVarAccess) ((ArrayCall) leftexpr).getArrayInstance()).getVariable().getName(),"I"));
                    }
                }
                builder.append(callExpression(leftexpr));
            }break;
        }
        return builder.toString();
    }

    @Override
    public String visit(ArrayCall arrayCall) {
        StringBuilder builder = new StringBuilder();
        builder.append(callExpression(arrayCall.getArrayInstance()));
        builder.append(callExpression(arrayCall.getIndex()));
        return builder.toString();
    }

    @Override
    public String visit(ActorVarAccess actorVarAccess) {
        SymbolTable table = currentScope.getPreSymbolTable();
        StringBuilder builder = new StringBuilder();
        try {
            SymbolTableVariableItem item = (SymbolTableVariableItem) table.get(SymbolTableActorVariableItem.STARTKEY+actorVarAccess.getVariable().getName());
            builder.append(InstructionGenerator.aload(currentMethod.getMapper().slotOf(actorVarAccess.getSelf())));
            builder.append(InstructionGenerator.getField(currentActorName+"/"+actorVarAccess.getVariable().getName(),getVarType(item.getType())));
        }
        catch (ItemNotFoundException e){

        }
        return builder.toString();
    }

    @Override
    public String visit(Identifier identifier) {
        StringBuilder builder = new StringBuilder();
        try {
            SymbolTableVariableItem item = (SymbolTableVariableItem) currentScope.getInCurrentScope(SymbolTableVariableItem.STARTKEY+identifier.getName());
            if(item.getType() instanceof IntType || item.getType() instanceof BooleanType){
                builder.append(InstructionGenerator.iload(currentMethod.getMapper().slotOf(identifier)));
            }
            else {
                builder.append(InstructionGenerator.aload(currentMethod.getMapper().slotOf(identifier)));
            }
            return builder.toString();
        }
        catch (ItemNotFoundException e){}
        try {
            SymbolTableVariableItem item = (SymbolTableVariableItem) currentScope.get(SymbolTableVariableItem.STARTKEY + identifier.getName());
            if(item instanceof SymbolTableKnownActorItem){
                ActorType type = (ActorType) item.getType();
                builder.append(InstructionGenerator.aload(0));
                builder.append(InstructionGenerator.getField(currentActorName+"/"+identifier.getName(),"L"+type.getName().getName()+";"));
            }
            else{
                Type type = item.getType();
                String desc = getVarType(type);
                builder.append(InstructionGenerator.aload(0));
                builder.append(InstructionGenerator.getField(currentActorName+"/"+identifier.getName(),desc));
            }
        }
        catch (ItemNotFoundException e){

        }
        return builder.toString();
    }

    @Override
    public String visit(Self self) {
        return InstructionGenerator.aload(currentMethod.getMapper().slotOf(self));
    }

    @Override
    public String visit(Sender sender) {
        return InstructionGenerator.aload(currentMethod.getMapper().slotOf(sender));
    }

    @Override
    public String visit(BooleanValue value) {
        return InstructionGenerator.bipush((value.getConstant()? 1: 0));
    }

    @Override
    public String visit(IntValue value) {
        return InstructionGenerator.bipush(value.getConstant());
    }

    @Override
    public String visit(StringValue value) {
        return InstructionGenerator.pushString(value.getConstant());
    }

    @Override
    public String visit(Block block) {
        StringBuilder builder = new StringBuilder();
        ArrayList<Statement> statements = block.getStatements();
        for(Statement statement : statements){
            builder.append(callStatement(statement));
        }
        return builder.toString();
    }

    @Override
    public String visit(Conditional conditional) {
        StringBuilder builder = new StringBuilder();
        builder.append(callExpression(conditional.getExpression()));
        String labelTrue = newLabel();
        String labelEnd = newLabel();
        builder.append(InstructionGenerator.branchIf(labelTrue,InstructionGenerator.IfConditionMode.ne));
        if(conditional.getElseBody()!=null){
            builder.append(callStatement(conditional.getElseBody()));
        }
        builder.append(InstructionGenerator.gotoLabel(labelEnd));
        builder.append(labelTrue+":\n");
        builder.append(callStatement(conditional.getThenBody()));
        builder.append(labelEnd+":\n");
        return builder.toString();
    }

    @Override
    public String visit(For loop) {
        StringBuilder builder = new StringBuilder();
        String labelLoop = newLabel();
        String labelEnd = newLabel();
        String labelUpdate = newLabel();
        String prevCont = labelContinue;
        String prevBreak = labelBreak;
        String prevAfter = labelAfter;
        labelContinue = labelUpdate;
        labelBreak = labelEnd;
        labelAfter = labelEnd;
        if(loop.getInitialize()!=null){
            builder.append(callStatement(loop.getInitialize()));
        }
        builder.append(labelLoop + ":\n");
        if(loop.getCondition()!=null){
            builder.append(callExpression(loop.getCondition()));
        }
        else builder.append(InstructionGenerator.bipush(1));
        builder.append(InstructionGenerator.branchIf(labelAfter,InstructionGenerator.IfConditionMode.eq));
        builder.append(callStatement(loop.getBody()));
        builder.append(labelUpdate + ":\n");
        if(loop.getUpdate()!=null){
            builder.append(callStatement(loop.getUpdate()));
        }
        builder.append(InstructionGenerator.gotoLabel(labelLoop));
        builder.append(labelAfter+":\n");
        labelContinue = prevCont;
        labelBreak = prevBreak;
        labelAfter = prevAfter;
        return builder.toString();
    }

    @Override
    public String visit(Break breakLoop) {
        return InstructionGenerator.gotoLabel(labelBreak);
    }

    @Override
    public String visit(Continue continueLoop) {
        return InstructionGenerator.gotoLabel(labelContinue);
    }

    @Override
    public String visit(MsgHandlerCall msgHandlerCall) {
        StringBuilder builder = new StringBuilder();
        builder.append(callExpression(msgHandlerCall.getInstance()));
        ExpressionChecker checker = new ExpressionChecker(root,currentScope.getPreSymbolTable(),currentScope,null);
        ArrayList<Expression> args = msgHandlerCall.getArgs();
        StringBuilder type = new StringBuilder();
        builder.append(InstructionGenerator.aload(0));
        for(Expression expression : args){
            builder.append(callExpression(expression));
            type.append(getVarType(checker.check(expression)));
        }
        builder.append(InstructionGenerator.invokeVirtual("Actor/send_"+msgHandlerCall.getMsgHandlerName().getName(),"LActor;"+type.toString(),"V"));
        return builder.toString();
    }

    @Override
    public String visit(Print print) {
        if(print!=null){
            StringBuilder builder = new StringBuilder();
            String expr = callExpression(print.getArg());
            builder.append(InstructionGenerator.getStatic("java/lang/System/out","Ljava/io/PrintStream;"));
            builder.append(expr);
            Expression expression = print.getArg();
            ExpressionChecker checker = new ExpressionChecker(root,currentScope.getPreSymbolTable(),currentScope,null);
            Type type = checker.check(expression);
            String sig = "";
            if(type instanceof StringType){
                sig = "Ljava/lang/String;";
            }
            else if(type instanceof IntType){
                sig = "I";
            }
            else if(type instanceof BooleanType){
                sig = "Z";
            }
            else if(type instanceof ArrayType){
                builder.append(InstructionGenerator.invokeStatic("java/util/Arrays/toString","[I","Ljava/lang/String;"));
                sig = "Ljava/lang/String;";
            }
            builder.append(InstructionGenerator.invokeVirtual("java/io/PrintStream/println",sig,"V"));
            return builder.toString();
        }
        return null;
    }

    @Override
    public String visit(Assign assign) {
        StringBuilder builder = new StringBuilder();
        Expression lvalue = assign.getlValue();
        Expression rvalue = assign.getrValue();
        if(lvalue instanceof ArrayCall){
            builder.append(callExpression(lvalue,true));
            builder.append(callExpression(rvalue));
            builder.append(InstructionGenerator.iastore());
            return builder.toString();
        }
        else if(lvalue instanceof ActorVarAccess){
            builder.append(InstructionGenerator.aload(currentMethod.getMapper().slotOf(((ActorVarAccess) lvalue).getSelf())));
            builder.append(callExpression(assign.getrValue()));
            ExpressionChecker checker = new ExpressionChecker(root,currentScope.getPreSymbolTable(),currentScope,null);
            String type = getVarType(checker.check(assign.getrValue()));
            builder.append(InstructionGenerator.putField(currentActorName+"/"+((ActorVarAccess) lvalue).getVariable().getName(),type));
        }
        else{
            builder.append(storeVar(lvalue,callExpression(assign.getrValue()),false));
        }
        return builder.toString();
    }
}
