package acton.compiler.main.visitor;

import acton.compiler.main.ast.node.*;
import acton.compiler.main.ast.node.Program;
import acton.compiler.main.ast.node.declaration.*;
import acton.compiler.main.ast.node.declaration.VarDeclaration;
import acton.compiler.main.ast.node.declaration.handler.*;
import acton.compiler.main.ast.node.expression.*;
import acton.compiler.main.ast.node.expression.values.BooleanValue;
import acton.compiler.main.ast.node.expression.values.IntValue;
import acton.compiler.main.ast.node.expression.values.StringValue;
import acton.compiler.main.ast.node.statement.*;

public class VisitorImpl implements Visitor<Void> {

    protected void visitStatement( Statement stat )
    {
        if( stat == null )
            return;
        else if( stat instanceof MsgHandlerCall )
            this.visit( ( MsgHandlerCall ) stat );
        else if( stat instanceof Block )
            this.visit( ( Block ) stat );
        else if( stat instanceof Conditional )
            this.visit( ( Conditional ) stat );
        else if( stat instanceof For )
            this.visit( ( For ) stat );
        else if( stat instanceof Break )
            this.visit( ( Break ) stat );
        else if( stat instanceof Continue )
            this.visit( ( Continue ) stat );
        else if( stat instanceof Print )
            this.visit( ( Print ) stat );
        else if( stat instanceof Assign )
            this.visit( ( Assign ) stat );
    }

    protected void visitExpr( Expression expr )
    {
        if( expr == null )
            return;
        else if( expr instanceof UnaryExpression )
            this.visit( ( UnaryExpression ) expr );
        else if( expr instanceof BinaryExpression )
            this.visit( ( BinaryExpression ) expr );
        else if( expr instanceof ArrayCall )
            this.visit( ( ArrayCall ) expr );
        else if( expr instanceof ActorVarAccess )
            this.visit( ( ActorVarAccess ) expr );
        else if( expr instanceof Identifier )
            this.visit( ( Identifier ) expr );
        else if( expr instanceof Self )
            this.visit( ( Self ) expr );
        else if( expr instanceof Sender )
            this.visit( ( Sender ) expr );
        else if( expr instanceof BooleanValue )
            this.visit( ( BooleanValue ) expr );
        else if( expr instanceof IntValue )
            this.visit( ( IntValue ) expr );
        else if( expr instanceof StringValue )
            this.visit( ( StringValue ) expr );
    }


    @Override
    public Void visit(Program program) {
        return null;
    }

    @Override
    public Void visit(ActorDeclaration actorDeclaration) {
        return null;
    }

    @Override
    public Void visit(HandlerDeclaration handlerDeclaration) {
        return null;
    }

    @Override
    public Void visit(VarDeclaration varDeclaration) {
        return null;
    }

    @Override
    public Void visit(Main mainActors) {
        return null;
    }

    @Override
    public Void visit(ActorInstantiation actorInstantiation) {
        return null;
    }

    @Override
    public Void visit(UnaryExpression unaryExpression) {
        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        return null;
    }

    @Override
    public Void visit(ArrayCall arrayCall) {
        return null;
    }

    @Override
    public Void visit(ActorVarAccess actorVarAccess) {
        return null;
    }

    @Override
    public Void visit(Identifier identifier) {
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
    public Void visit(Block block) {
        return null;
    }

    @Override
    public Void visit(Conditional conditional) {
        return null;
    }

    @Override
    public Void visit(For loop) {
        return null;
    }

    @Override
    public Void visit(Break breakLoop) {
        return null;
    }

    @Override
    public Void visit(Continue continueLoop) {
        return null;
    }

    @Override
    public Void visit(MsgHandlerCall msgHandlerCall) {
        return null;
    }

    @Override
    public Void visit(Print print) {
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        return null;
    }
}
