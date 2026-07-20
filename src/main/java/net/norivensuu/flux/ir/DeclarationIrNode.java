package net.norivensuu.flux.ir;

import net.norivensuu.flux.antlr.FluxParser;
import net.norivensuu.flux.compiler.FluxBytecodeGenerator;
import net.norivensuu.flux.compiler.FluxIr;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;

public class DeclarationIrNode extends FluxIr.IrNode<Void, ParserRuleContext> {

    public DeclarationIrNode(FluxBytecodeGenerator ir, ParserRuleContext ctx) {
        super(ir, ctx);
    }

    public DeclarationIrNode(FluxBytecodeGenerator ir, FluxParser.VarDeclContext ctx) {
        super(ir, ctx);
    }

    @Override
    public String getBaseToken() {
        return "declaration";
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
