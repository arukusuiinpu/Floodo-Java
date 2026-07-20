package net.norivensuu.flux.compiler;

import net.norivensuu.flux.antlr.FluxBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import static net.norivensuu.flux.utils.FluxUtils.*;

public class FluxBytecodeGenerator extends FluxIr<Void> {

    public FluxBytecodeGenerator(FluxBaseVisitor<IrNode<Void, ? extends ParserRuleContext>> generator, FluxCompiler.Program program, ParserRuleContext baseNode) {
        super(generator, program, baseNode);
    }

    public FluxBytecodeGenerator(FluxBaseVisitor<IrNode<Void, ? extends ParserRuleContext>> generator, FluxCompiler.Program program, ParserRuleContext baseNode, File irFile) {
        super(generator, program, baseNode, irFile);
    }

    public Map<String, byte[]> generateClasses() {

        prepassBase();

        visitBase();

        return program.node.classNodes.stream()
                .collect(Collectors.toMap((s) -> s.name, (s) -> s.cw.toByteArray()));
    }
}