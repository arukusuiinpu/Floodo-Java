package net.norivensuu.flux.ir;

import net.norivensuu.flux.antlr.FluxParser;
import net.norivensuu.flux.compiler.FluxBytecodeGenerator;
import net.norivensuu.flux.compiler.FluxIr;

public class ImportIrNode extends FluxIr.IrNode<Void, FluxParser.ImportDeclContext> {
    public ImportIrNode(FluxBytecodeGenerator ir, FluxParser.ImportDeclContext ctx) {
        super(ir, ctx);
    }

    @Override
    public String getBaseToken() {
        return String.format("import %s\n", ctx.qualifiedId().getText());
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
