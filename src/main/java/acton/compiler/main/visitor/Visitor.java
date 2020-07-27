package acton.compiler.main.visitor;

import acton.compiler.main.ast.node.*;
import acton.compiler.main.ast.node.Program;
import acton.compiler.main.ast.node.declaration.*;
import acton.compiler.main.ast.node.declaration.VarDeclaration;
import acton.compiler.main.ast.node.declaration.handler.HandlerDeclaration;
import acton.compiler.main.ast.node.expression.*;
import acton.compiler.main.ast.node.expression.values.BooleanValue;
import acton.compiler.main.ast.node.expression.values.IntValue;
import acton.compiler.main.ast.node.expression.values.StringValue;
import acton.compiler.main.ast.node.statement.*;

public interface Visitor <T> {

    T visit(Program program);

    //Declarations
    T visit(ActorDeclaration actorDeclaration);
    T visit(HandlerDeclaration handlerDeclaration);
    T visit(VarDeclaration varDeclaration);

    //main
    T visit(Main mainActors);
    T visit(ActorInstantiation actorInstantiation);

    //Expressions
    T visit(UnaryExpression unaryExpression);
    T visit(BinaryExpression binaryExpression);
    T visit(ArrayCall arrayCall);
    T visit(ActorVarAccess actorVarAccess);
    T visit(Identifier identifier);
    T visit(Self self);
    T visit(Sender sender);
    T visit(BooleanValue value);
    T visit(IntValue value);
    T visit(StringValue value);

    //Statements
    T visit(Block block);
    T visit(Conditional conditional);
    T visit(For loop);
    T visit(Break breakLoop);
    T visit(Continue continueLoop);
    T visit(MsgHandlerCall msgHandlerCall);
    T visit(Print print);
    T visit(Assign assign);
}
