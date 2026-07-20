package net.norivensuu.flux.compiler;

import net.norivensuu.flux.antlr.FluxBaseVisitor;
import net.norivensuu.flux.antlr.FluxParser;
import net.norivensuu.flux.ir.ClassIrNode;
import net.norivensuu.flux.ir.EmptyIrNode;
import net.norivensuu.flux.ir.ProgramIrNode;
import net.norivensuu.flux.ir.SimpleIrNode;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.norivensuu.flux.utils.FluxUtils.*;
import static net.norivensuu.flux.antlr.FluxParser.*;

public class FluxIr<V> {
    public File irFile;
    public StringBuilder irString = new StringBuilder();
    public boolean logIr = false;

    public FluxCompiler.Program program;

    public FluxBaseVisitor<IrNode<V, ? extends ParserRuleContext>> generator;

    public IrNode<V, ? extends ParserRuleContext> baseNode;

    public interface IrVisitor<T> {
        void setIr(FluxIr<?> ir);
        FluxIr<T> getIr();

        default <K extends ParserRuleContext> IrNode<T, K> of(K ctx, Function<IrNode<T, K>, String> tokenize) {
            return getIr().of(ctx, tokenize);
        }
        default <K extends ParserRuleContext> IrNode<T, K> of(K ctx, Class<? extends IrNode<T, K>> nodeClass) {
            return getIr().of(ctx, nodeClass);
        }
        default <K extends ParserRuleContext> IrNode<T, K> of(K ctx, String nodeToken) {
            return getIr().of(ctx, nodeToken);
        }
    }

    public FluxIr(FluxBaseVisitor<IrNode<V, ? extends ParserRuleContext>> generator, FluxCompiler.Program program, ParserRuleContext baseNode) {
        if (generator instanceof IrVisitor<?> irVisitor) {
            irVisitor.setIr(this);
        }
        this.irFile = null;
        this.generator = generator;
        this.baseNode = generator.visit(baseNode);
        this.program = program;

        program.ir = this;
        program.node = (ProgramIrNode) this.baseNode;
    }

    public FluxIr(FluxBaseVisitor<IrNode<V, ? extends ParserRuleContext>> generator, FluxCompiler.Program program, ParserRuleContext baseNode, File irFile) {
        if (generator instanceof IrVisitor<?> irVisitor) {
            irVisitor.setIr(this);
        }
        this.generator = generator;
        this.baseNode = generator.visit(baseNode);
        this.irFile = irFile;
        this.program = program;

        program.ir = this;
        program.node = (ProgramIrNode) this.baseNode;

        this.logIr = true;

        if (this.logIr) {
            try {
                Files.writeString(irFile.toPath(), "");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public V visitBase() {
        return visit(this.baseNode);
    }

    public V prepassBase() {
        return prepass(this.baseNode);
    }

    public <T, K extends ParserRuleContext> T visit(IrNode<T, K> node) {
        return node.visit();
    }

    public <T, K extends ParserRuleContext> T prepass(IrNode<T, K> node) {
        return node.supplyAndIr(node::prepass);
    }

    public <K extends ParserRuleContext> IrNode<V, K> of(K ctx, Class<? extends IrNode<V, K>> nodeClass) {
        try {
            return nodeClass.getDeclaredConstructor(this.getClass(), ctx.getClass()).newInstance(this, ctx);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            var mod = nodeClass.getModifiers();

            if (Modifier.isAbstract(mod)) {
                e.initCause(new InstantiationException(String.format("The class %s is abstract, cannot instantiate.", nodeClass)));
            }
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            e.initCause(new NoSuchMethodException(String.format("Can't find constructor with parameters: %s in the %s. Make sure to add it.", List.of(this.getClass().getSimpleName(), ctx.getClass().getSimpleName()), nodeClass)));

            throw new RuntimeException(e);
        }
    }

    public <K extends ParserRuleContext> IrNode<V, K> of(K ctx, Function<IrNode<V, K>, String> tokenizer) {
        return new SimpleIrNode<>(this, ctx, tokenizer);
    }
    public <K extends ParserRuleContext> IrNode<V, K> of(K ctx, String nodeToken) {
        return of(ctx, (s) -> nodeToken);
    }

    public static class FluxContext<T> {

        public Map<String, IrNode<T, ? extends ParserRuleContext>> dict;

        public FluxContext(Map<String, IrNode<T, ? extends ParserRuleContext>> dict) {
            this.dict = dict;
        }

        public FluxContext() {
            this.dict = new HashMap<>();
        }

        public FluxContext<T> addContext(String key, IrNode<T, ? extends ParserRuleContext> value) {
            return new FluxContext<>(new HashMap<>(dict) {{
                put(key, value);
            }} );
        }

        public FluxContext<T> addContext(Map<String, IrNode<T, ? extends ParserRuleContext>> newContext) {
            return new FluxContext<>(new HashMap<>(dict) {{
                this.putAll(newContext);
            }} );
        }

        public <V extends IrNode<T, ? extends ParserRuleContext>> V get(String key, Class<V> clazz) {
            return (clazz.cast(dict.get(key)));
        }
    }

    public abstract static class IrNode<T, K extends ParserRuleContext> {
        public K ctx;
        public FluxIr<T> ir;
        public FluxContext<T> context;
        public Function<FluxContext<T>, FluxContext<T>> getContextTransformer() {
            return (s) -> s;
        }

        public File getIrFile() {
            return ir.irFile;
        }
        public FluxBaseVisitor<IrNode<T, ? extends ParserRuleContext>> getGenerator() {
            return ir.generator;
        }
        public boolean getLogIr() {
            return ir.logIr;
        }

        public IrNode(FluxIr<T> ir, K ctx) {
            this.ctx = ctx;
            this.ir = ir;
        }

        public String getNodeToken() {
            return tokenize();
        }

        public List<IrNode<T, ? extends ParserRuleContext>> children;
        public IrNode<T, ? extends ParserRuleContext> parent;

        public abstract String getBaseToken();

        public String tokenize() {
            String token = getBaseToken();

            writeIr(token);

            return token;
        }

        @Override
        public String toString() {
            return String.format("<%s%s>", getBaseToken()
                    .strip(), children != null ? String.format(" %s", children) : "");
        }

        public T supplyAndIr(Supplier<T> supplier) {
            getNodeToken();

            return supplier.get();
        }

        public abstract T prepass();

        public abstract T visit();

        public void visitChildren() {
            supplyChildren(IrNode::visit);
        }

        public void prepassChildren() {
            supplyChildren(IrNode::prepass);
        }

        public final <V extends IrNode<T, ? extends ParserRuleContext>> void addChildren(Collection<V> children) {
            if (this.children == null) {
                this.children = new ArrayList<>();
            }
            this.children.addAll(children);
            this.children.forEach((s) -> {
                s.parent = this;
                s.context = s.getContextTransformer().apply(this.context);
            });
        }
        @SafeVarargs
        public final <V extends IrNode<T, ? extends ParserRuleContext>> void addChildren(V... children) {
            addChildren(List.of(children));
        }

        public final <V extends ParseTree> List<? extends IrNode<T, ? extends ParserRuleContext>> makeChildren(Collection<V> children) {
            List<IrNode<T, ? extends ParserRuleContext>> newChildren = new ArrayList<>();

            if (children != null && this.children == null) {
                for (var child : children) {
                    if (!(child instanceof TerminalNodeImpl || child instanceof FluxParser.TerminatorContext)) {
                        if (this.children == null) {
                            this.children = new ArrayList<>();
                        }
                        var node = ir.generator.visit(child);

                        if (node != null) {
                            if (!(node instanceof EmptyIrNode)) {
                                newChildren.add(node);

                                this.children.add(node);
                                node.parent = this;
                                node.context = node.getContextTransformer().apply(this.context);
                            }
                        } else {
                            throw new FluxIr.UnimplementedIrNodeException(String.format("Could not construct node from %s, not implemented.", child.getClass().getSimpleName()));
                        }
                    }
                }
            }
            return newChildren;
        }
        @SafeVarargs
        public final <V extends ParseTree> List<? extends IrNode<T, ? extends ParserRuleContext>> makeChildren(V... children) {
            return makeChildren(List.of(children));
        }

        public void supplyChildren(Function<IrNode<T, ? extends ParserRuleContext>, T> function) {
            if (children == null && ctx != null) makeChildren(ctx.children);

            if (children != null) {
                for (var node : children) {
                    node.supplyAndIr(() -> function.apply(node));
                }
            }
        }

        public void writeIr(String token) {
            if (getIrFile() != null && getLogIr()) {
                ir.irString.append(token).append(token.endsWith("\n") ? "" : " ");

                try {
                    Files.writeString(getIrFile().toPath(), ir.irString);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class UnimplementedIrNodeException extends RuntimeException {
        public UnimplementedIrNodeException() {
            super();
        }

        public UnimplementedIrNodeException(String message) {
            super(message);
        }

        public UnimplementedIrNodeException(String message, Throwable cause) {
            super(message, cause);
        }

        public UnimplementedIrNodeException(Throwable cause) {
            super(cause);
        }

        protected UnimplementedIrNodeException(String message, Throwable cause,
                                               boolean enableSuppression,
                                               boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), baseNode.toString());
    }
}
