// Generated from /media/alireza/02CE30BBCE30A935/notable projects/Acton compiler/src/main/java/acton/compiler/antlr-files/acton.g4 by ANTLR 4.8
package acton.compiler.parsers;

    import acton.compiler.main.ast.node.*;
    import acton.compiler.main.ast.node.declaration.*;
    import acton.compiler.main.ast.node.declaration.handler.*;
    import acton.compiler.main.ast.node.statement.*;
    import acton.compiler.main.ast.node.expression.*;
    import acton.compiler.main.ast.node.expression.operators.*;
    import acton.compiler.main.ast.node.expression.values.*;
    import acton.compiler.main.ast.type.primitiveType.*;
    import acton.compiler.main.ast.type.arrayType.*;
    import acton.compiler.main.ast.type.actorType.*;
    import acton.compiler.main.ast.type.*;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link actonParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface actonVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link actonParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(actonParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#actorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActorDeclaration(actonParser.ActorDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#mainDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMainDeclaration(actonParser.MainDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#actorInstantiation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActorInstantiation(actonParser.ActorInstantiationContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#initHandlerDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitHandlerDeclaration(actonParser.InitHandlerDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#msgHandlerDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsgHandlerDeclaration(actonParser.MsgHandlerDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#argDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgDeclarations(actonParser.ArgDeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#varDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDeclarations(actonParser.VarDeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#varDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDeclaration(actonParser.VarDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(actonParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#blockStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStmt(actonParser.BlockStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#printStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrintStmt(actonParser.PrintStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#assignStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignStmt(actonParser.AssignStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(actonParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#forStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStmt(actonParser.ForStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#ifStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStmt(actonParser.IfStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#continueStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueStmt(actonParser.ContinueStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#breakStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakStmt(actonParser.BreakStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#msgHandlerCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsgHandlerCall(actonParser.MsgHandlerCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(actonParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#orExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpression(actonParser.OrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#andExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(actonParser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#equalityExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpression(actonParser.EqualityExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#relationalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpression(actonParser.RelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#additiveExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(actonParser.AdditiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpression(actonParser.MultiplicativeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#preUnaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreUnaryExpression(actonParser.PreUnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#postUnaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostUnaryExpression(actonParser.PostUnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#postUnaryOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostUnaryOp(actonParser.PostUnaryOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#otherExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherExpression(actonParser.OtherExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#arrayCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayCall(actonParser.ArrayCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#actorVarAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActorVarAccess(actonParser.ActorVarAccessContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(actonParser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(actonParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link actonParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(actonParser.ValueContext ctx);
}