package net.norivensuu.flux.ir;

import net.norivensuu.flux.antlr.FluxParser;
import net.norivensuu.flux.compiler.FluxBytecodeGenerator;
import net.norivensuu.flux.compiler.FluxIr;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

import static net.norivensuu.flux.compiler.FluxIr.IrNode;
import static net.norivensuu.flux.utils.FluxUtils.*;

public class FunctionIrNode extends IrNode<Void, ParserRuleContext> {

    public String name;
    public FluxParser.FunctionModifiersContext modifiersCtx;
    public ComplexType type;

    ClassWriter cw;

    public int parseModifiers() {
        int modifiers = 0;

        Map<String, Integer> tokenConverter = new HashMap<>() {{
           put("public", Opcodes.ACC_PUBLIC);
           put("private", Opcodes.ACC_PRIVATE);
           put("static", Opcodes.ACC_STATIC);
        }};

        if (modifiersCtx.children != null) {
            for (var modifier : modifiersCtx.children) {
                if (tokenConverter.containsKey(modifier.getText().strip())) {
                    if (modifiers == 0)
                        modifiers = tokenConverter.get(modifier.getText());
                    else
                        modifiers |= tokenConverter.get(modifier.getText());
                }
            }
        }

        return modifiers;
    }

    public FunctionIrNode(FluxBytecodeGenerator ir, FluxParser.RunnableFunctionDeclContext ctx) {
        super(ir, ctx);

        name = ctx.ID().getText();
        modifiersCtx = ctx.functionModifiers();
        type = ctm.of("void");
    }

    public FunctionIrNode(FluxBytecodeGenerator ir, FluxParser.VarFunctionDeclContext ctx) {
        super(ir, ctx);

        name = ctx.ID().getText();
        modifiersCtx = ctx.functionModifiers();
        type = ctm.of("var");
    }

    public FunctionIrNode(FluxBytecodeGenerator ir, FluxParser.ConsumerFunctionDeclContext ctx) {
        super(ir, ctx);

        name = ctx.ID().getText();
        modifiersCtx = ctx.functionModifiers();
        type = ctm.of(ctx.type().getText());
    }

    @Override
    public String getBaseToken() {
        return String.format("%sfunction %s\n", !modifiersCtx.getText().isEmpty() ? modifiersCtx.getText() + " " : "", name);
    }

    @Override
    public String tokenize() {
        return super.tokenize();
    }

    @Override
    public Void prepass() {
        if (type.is("var")) {
            print(name);
        }

        prepassChildren();

        return null;
    }

    @Override
    public Void visit() {
//        print(parseModifiers(), name, type.getJvmType());

        cw = context.get("class", ClassIrNode.class).cw;

        MethodVisitor mv = cw.visitMethod(
                parseModifiers(),
                name,
                type.getJvmType(),
                null,
                null);

        mv.visitCode();

        visitChildren();

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0,0);
        mv.visitEnd();

        return null;
    }
}

