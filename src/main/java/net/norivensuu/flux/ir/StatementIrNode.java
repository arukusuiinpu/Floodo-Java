package net.norivensuu.flux.ir;

import net.norivensuu.flux.antlr.FluxParser;
import net.norivensuu.flux.compiler.FluxBytecodeGenerator;
import net.norivensuu.flux.compiler.FluxIr;
import org.antlr.v4.runtime.ParserRuleContext;

public class StatementIrNode extends FluxIr.IrNode<Void, ParserRuleContext> {
    public StatementIrNode(FluxBytecodeGenerator ir, ParserRuleContext ctx) {
        super(ir, ctx);
    }

    public StatementIrNode(FluxBytecodeGenerator ir, FluxParser.ExpressionStatementContext ctx) {
        super(ir, ctx);
    }

    @Override
    public String getBaseToken() {
        return "statement";
    }

    @Override
    public Void prepass() {
        return null;
    }

    @Override
    public Void visit() {
        return null;
    }
}
