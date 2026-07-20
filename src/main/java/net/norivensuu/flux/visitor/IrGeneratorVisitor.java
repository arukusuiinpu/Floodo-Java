package net.norivensuu.flux.visitor;

import net.norivensuu.flux.antlr.FluxBaseVisitor;
import net.norivensuu.flux.compiler.FluxIr;
import net.norivensuu.flux.compiler.FluxIr.IrNode;
import net.norivensuu.flux.compiler.FluxIr.IrVisitor;
import net.norivensuu.flux.ir.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.objectweb.asm.ClassWriter;

import static net.norivensuu.flux.antlr.FluxParser.*;
import static net.norivensuu.flux.utils.FluxUtils.*;

public class IrGeneratorVisitor extends FluxBaseVisitor<IrNode<Void, ? extends ParserRuleContext>> implements IrVisitor<Void> {

    public FluxIr<Void> ir;

    @Override
    @SuppressWarnings("unchecked")
    public void setIr(FluxIr<?> ir) {
        this.ir = (FluxIr<Void>) ir;
    }

    @Override
    public FluxIr<Void> getIr() {
        return ir;
    }

    //  PROGRAM

    @Override
    public IrNode<Void, ProgramContext> visitProgram(ProgramContext ctx) {
        return of(ctx, ProgramIrNode.class);
    }

    @Override
    public IrNode<Void, PrecompileContext> visitPrecompile(PrecompileContext ctx) {
        return of(ctx, "precompile\n");
    }

    @Override
    public IrNode<Void, QualifiedIdContext> visitQualifiedId(QualifiedIdContext ctx) {
        return of(ctx, String.format("(%s)", ctx.getText()));
    }

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitPackedId(PackedIdContext ctx) {
        return of(ctx, String.format("(%s)", ctx.getText()));
    }

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitType(TypeContext ctx) {
        return of(ctx, String.format("type (%s)", ctx.getText()));
    }

    // FUNCTIONS

    @Override
    public IrNode<Void, FunctionModifiersContext> visitFunctionModifiers(FunctionModifiersContext ctx) {
        return of(ctx, String.format("modifiers (%s)", ctx.getText()));
    }

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitRunnableFunctionDecl(RunnableFunctionDeclContext ctx) {
        return of(ctx, FunctionIrNode.class);
    }

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitVarFunctionDecl(VarFunctionDeclContext ctx) {
        return of(ctx, FunctionIrNode.class);
    }

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitConsumerFunctionDecl(ConsumerFunctionDeclContext ctx) {
        return of(ctx, FunctionIrNode.class);
    }

    // DECLARATIONS

    @Override
    public IrNode<Void, ClassDeclContext> visitClassDecl(ClassDeclContext ctx) {
        return of(ctx, ClassIrNode.class);
    }

    @Override
    public IrNode<Void, ImportDeclContext> visitImportDecl(ImportDeclContext ctx) {
        return of(ctx, ImportIrNode.class);
    }

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitVarDecl(VarDeclContext ctx) {
        return of(ctx, DeclarationIrNode.class);
    }

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitExpressionStatement(ExpressionStatementContext ctx) {
        return of(ctx, StatementIrNode.class);
    }

    // STATEMENTS


    // BLOCKS

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitClassBlock(ClassBlockContext ctx) {
        return of(ctx, "block");
    }

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitVoidBlock(VoidBlockContext ctx) {
        return of(ctx, "block");
    }
    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitReturnBlock(ReturnBlockContext ctx) {
        return of(ctx, "block");
    }


    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitFormalParameters(FormalParametersContext ctx) {
        return of(ctx, "formalParameters");
    }

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitFormalParameter(FormalParameterContext ctx) {
        return of(ctx, "formalParameter");
    }

    @Override
    public IrNode<Void, ? extends ParserRuleContext> visitExpressionLines(ExpressionLinesContext ctx) {
        return super.visitExpressionLines(ctx);
    }
}
