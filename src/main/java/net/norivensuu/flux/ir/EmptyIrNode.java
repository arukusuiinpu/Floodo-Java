package net.norivensuu.flux.ir;

import net.norivensuu.flux.compiler.FluxBytecodeGenerator;
import net.norivensuu.flux.compiler.FluxIr;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;

public class EmptyIrNode extends FluxIr.IrNode<Void, ParserRuleContext> {

    public EmptyIrNode(FluxBytecodeGenerator ir, ParserRuleContext ctx) {
        super(ir, ctx);
    }

    @Override
    public String getBaseToken() {
        return "null";
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
