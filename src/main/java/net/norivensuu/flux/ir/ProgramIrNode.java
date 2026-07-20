package net.norivensuu.flux.ir;

import net.norivensuu.flux.antlr.FluxParser;
import net.norivensuu.flux.compiler.FluxBytecodeGenerator;
import net.norivensuu.flux.compiler.FluxCompiler;
import net.norivensuu.flux.compiler.FluxIr;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.norivensuu.flux.utils.FluxUtils.*;

public class ProgramIrNode extends FluxIr.IrNode<Void, FluxParser.ProgramContext> {

    public FluxCompiler.Program program;

    public List<FluxParser.ClassDeclContext> classes;
    public List<ClassIrNode> classNodes = new ArrayList<>();

    public FluxParser.ClassDeclContext mainClass;

    public ProgramIrNode(FluxBytecodeGenerator ir, FluxParser.ProgramContext ctx) {
        super(ir, ctx);

        context = new FluxIr.FluxContext<>();

        program = FluxCompiler.getProgramRegistry().get(ctx);
        classes = ctx.declaration().stream().filter((s) -> s instanceof FluxParser.ClassDeclContext).map((s) -> (FluxParser.ClassDeclContext) s).toList();
        mainClass = classes.stream().filter((s) -> s.mainClass.getText().equals(program.fileName.toString().split("\\.")[0])).findFirst().orElse(null);
    }

    @Override
    public String getBaseToken() {
        return "program\n";
    }

    @Override
    public Void prepass() {

        var nodeChildren = makeChildren(ctx.children);

        var classChildren = nodeChildren.stream().filter((s) -> s instanceof ClassIrNode).toList();
        var nonClassChildren = nodeChildren.stream().filter((s) -> !(s instanceof ClassIrNode)).toList();

        classNodes.addAll((Collection<? extends ClassIrNode>) classChildren);

        if (mainClass == null) {
            var classNode = new ClassIrNode((FluxBytecodeGenerator) ir, program.fileName.toString().split("\\.")[0], this);

            classNode.addChildren((Collection<? extends FluxIr.IrNode<Void, FluxParser.ClassDeclContext>>) nonClassChildren);

            classNodes.add(classNode);
        }

        for (var cl : classNodes) {
            cl.prepass();
        }

//        boolean hadClass = ctx.children.stream().anyMatch((s) -> s instanceof FluxParser.DeclarationContext c && c.classDecl() != null && c.classDecl().mainClass.getText().equals(program.fileName.toString().split("\\.")[0]));
//
//        FluxParser.ClassLinesContext ctxDecl = null;
//        if (!hadClass) {
//            ctxDecl = synthesizeProgramClass(program.precompile, ctx).classDecl().classBlock().classLines();
//        }
//        else {
//            var candidate = ctx.children.stream().filter((s) -> s instanceof FluxParser.DeclarationContext c && c.classDecl() != null && c.classDecl().mainClass.getText().equals(program.fileName.toString().split("\\.")[0])).map((s) -> ((FluxParser.DeclarationContext) s).classDecl().classBlock().classLines()).findFirst();
//            if (candidate.isPresent()) {
//                ctxDecl = candidate.get();
//            }
//        }
//
//        if (!hadClass && ctxDecl.declaration().stream().noneMatch((s) -> s.functionDecl() != null && s.functionDecl() instanceof FluxParser.RunnableFunctionDeclContext void_ && void_.ID().getText().equalsIgnoreCase("main"))) {
//            synthesizeMainFunction(ctxDecl);
//        }
//        else if (hadClass && ctxDecl == null &&
//                ctx.declaration().stream().noneMatch((s) -> s.functionDecl() != null && s.functionDecl() instanceof FluxParser.RunnableFunctionDeclContext void_ && void_.ID().getText().equalsIgnoreCase("main"))
//        ) {
//            synthesizeMainFunction(ctx);
//        }
//        else if (hadClass && ctxDecl != null &&
//                ctx.declaration().stream().noneMatch((s) -> s.functionDecl() != null && s.functionDecl() instanceof FluxParser.RunnableFunctionDeclContext void_ && void_.ID().getText().equalsIgnoreCase("main"))  &&
//                ctxDecl.declaration().stream().noneMatch((s) -> s.functionDecl() != null && s.functionDecl() instanceof FluxParser.RunnableFunctionDeclContext void_ && void_.ID().getText().equalsIgnoreCase("main"))
//        ) {
//            synthesizeMainFunction(ctxDecl);
//        }
//
//        if (!program.precompile) {
//
//            while (program.javaCode.checkDeclarations) {
//                program.javaCode.resetJavaCode();
//                program.javaCode.checkDeclarations = false;
//
//                mapDeclarations(ctx, program);
//                if (program.javaCode.hasUnknowns) { // TODO: Implement a proper way of handling recursive functions, bottom up declaration handling doesn't account for this automatically like it does with everything else
//                    program.javaCode.declarationsPass++;
//                    mapDeclarations(ctx, program);
//                }
//
//                for (var decl : ctx.declaration()) {
//                    processDeclaration(decl, program);
//                }
//            }
//        }
//        else {
//            program.javaCode.checkDeclarations = false;
//
//            for (var decl : ctx.declaration()) {
//                processDeclaration(decl, program);
//            }
//        }
//
//        return program.javaCode.toString();


        return null;
    }

    @Override
    public Void visit() {

        for (var cl : classNodes) {
            cl.visit();
        }

        return null;
    }
}
