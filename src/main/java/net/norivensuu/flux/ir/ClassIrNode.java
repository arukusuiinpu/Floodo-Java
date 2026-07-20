package net.norivensuu.flux.ir;

import net.norivensuu.flux.antlr.FluxParser;
import net.norivensuu.flux.compiler.FluxBytecodeGenerator;
import net.norivensuu.flux.compiler.FluxCompiler;
import net.norivensuu.flux.compiler.FluxIr;
import org.antlr.v4.runtime.ParserRuleContext;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.function.Function;

import static net.norivensuu.flux.utils.FluxUtils.*;

public class ClassIrNode extends FluxIr.IrNode<Void, FluxParser.ClassDeclContext> {
    public FluxCompiler.Program program;
    public FluxBytecodeGenerator ir;

    public ClassWriter cw = new ClassWriter(
            ClassWriter.COMPUTE_MAXS);

    public String name;

    @Override
    public Function<FluxIr.FluxContext<Void>, FluxIr.FluxContext<Void>> getContextTransformer() {
        return (s) -> s.addContext("class", this);
    }

    public ClassIrNode(FluxBytecodeGenerator ir, FluxParser.ClassDeclContext ctx) {
        super(ir, ctx);

        this.ir = ir;
        this.program = ir.program;
        this.name = ctx.mainClass.getText();
    }
    public ClassIrNode(FluxBytecodeGenerator ir, String name) {
        super(ir, null);

        this.ir = ir;
        this.program = ir.program;
        this.name = name;
    }
    public ClassIrNode(FluxBytecodeGenerator ir, String name, FluxIr.IrNode<Void, ? extends ParserRuleContext> parent) {
        super(ir, null);

        this.ir = ir;
        this.program = ir.program;
        this.name = name;

        parent.addChildren(this);
    }

    @Override
    public String getBaseToken() {
        return String.format("class %s", name);
    }

    @Override
    public Void prepass() {
        prepassChildren();

        return null;
    }

    @Override
    public Void visit() {

        cw.visit(
                Opcodes.V21,
                Opcodes.ACC_PUBLIC,
                program.packageString + "/" + name,
                null,
                "java/lang/Object",
                null
        );

        MethodVisitor mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V",
                false);

        visitChildren();

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0,0);
        mv.visitEnd();

        return null;
    }
}
