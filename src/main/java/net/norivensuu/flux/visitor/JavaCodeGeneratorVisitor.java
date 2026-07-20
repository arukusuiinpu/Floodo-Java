//package net.norivensuu.flux.visitor;
//
//import net.norivensuu.flux.antlr.FluxBaseVisitor;
//import net.norivensuu.flux.antlr.FluxLexer;
//import net.norivensuu.flux.compiler.FluxCompiler;
//import org.antlr.v4.runtime.CommonToken;
//import org.antlr.v4.runtime.ParserRuleContext;
//import org.antlr.v4.runtime.Token;
//import org.antlr.v4.runtime.misc.OrderedHashSet;
//import org.antlr.v4.runtime.tree.ParseTree;
//import org.antlr.v4.runtime.tree.TerminalNodeImpl;
//
//import java.util.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.function.BiFunction;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import static net.norivensuu.flux.antlr.FluxParser.*;
//import static net.norivensuu.flux.utils.FluxUtils.*;
//
//public class JavaCodeGeneratorVisitor extends FluxBaseVisitor<String> {
//
//    public static class Assertions implements Iterable<String> {
//        public Map<Integer, List<String>> pendingAssertions = new TreeMap<>();
//
//        public Assertions() {
//        }
//
//        public void add(String assertion) {
//            add(0, assertion);
//        }
//
//        public void add(int layer, String assertion) {
//            if (!pendingAssertions.containsKey(layer)) {
//                pendingAssertions.put(layer, new ArrayList<>());
//            }
//
//            pendingAssertions.get(layer).add(assertion);
//        }
//
//        public void clear() {
//            for (var layer : pendingAssertions.values()) {
//                layer.clear();
//            }
//        }
//
//        @Override
//        public Iterator<String> iterator() {
//            var s = new ArrayList<String>();
//            for (var layer : pendingAssertions.values()) {
//                s.addAll(layer);
//            }
//            return s.iterator();
//        }
//
//        @Override
//        public String toString() {
//            return pendingAssertions.toString();
//        }
//    }
//
//    public final Assertions pendingAssertions = new Assertions();
//
//    public class JavaCode {
//        public StringBuilder builder;
//        public int indentLevel;
//        public int atIndex;
//        public int lastImportsLine;
//        public boolean checkDeclarations = true;
//        public int declarationsPass = 0;
//        public boolean hasUnknowns = false;
//
//        public boolean checkPass() {
//            return !hasUnknowns || declarationsPass > 0;
//        }
//
//        public Map<DeclarationContext, Integer> programDeclarations = new HashMap<>();
//
//        public Map<ParseTree, Declaration> declarations = new HashMap<>();
//
//        public JavaCode() {
//            builder = new StringBuilder();
//        }
//
//        public StringBuilder addLine(String line, int at) {
//            return builder.insert(at, line + "\n");
//        }
//
//        public void checkDeclaration(DeclarationContext ctx, int lastIndex) {
//            atIndex += lastIndex;
//            programDeclarations.put(ctx, lastIndex);
//        }
//
//        public void resetJavaCode() {
////            indentLevel = 0;
//            atIndex = 0;
//            checkDeclarations = true;
//        }
//
//        public void HashDeclaration(ParseTree ctx, Declaration declaration) {
//            if (
//                    !declarations.containsKey(ctx) &&
//                    declaration != null &&
//                    declaration.type != null &&
//                    !declaration.type.is("unknown")
//            ) {
//                declarations.put(ctx, declaration);
//            }
//        }
//
//        public void HashDeclaration(String id, ParseTree ctx) {
//            HashDeclaration(ctx, simplifyDeclaration(ctx));
//        }
//
//        public void HashDeclaration(ParseTree ctx, ComplexType type, String id) {
//            HashDeclaration(ctx, new Declaration(type, id, ctx));
//        }
//
//        public boolean existsDeclaration(String id) {
//            for (var decl : declarations.values()) {
//                if (decl.id.equals(id)) return true;
//            }
//            return false;
//        }
//
//        @Override
//        public String toString() {
//            return builder.toString();
//        }
//    }
//
//    public FluxCompiler.Program getProgram(ProgramContext ctx) {
//        return FluxCompiler.getProgramRegistry().get(ctx);
//    }
//
//    private final Map<ParseTree, FluxCompiler.Program> dynamicProgramHash = new HashMap<>();
//
//    public FluxCompiler.Program getProgram(ParseTree ctx) {
//        if (dynamicProgramHash.containsKey(ctx))
//            return dynamicProgramHash.get(ctx);
//
//        var program = FluxCompiler.getProgramRegistry().get(getToClosestParent(ctx, ProgramContext.class));
//
//        dynamicProgramHash.put(ctx, program);
//        return program;
//    }
//
//    @Override
//    public String visitProgram(ProgramContext ctx) {
//        var program = FluxCompiler.getProgramRegistry().get(ctx);
//        program.javaCode = new JavaCode();
//
//        boolean hadClass = ctx.children.stream().anyMatch((s) -> s instanceof DeclarationContext c && c.classDecl() != null && c.classDecl().mainClass.getText().equals(program.fileName.toString().split("\\.")[0]));
//
//        ClassLinesContext ctxDecl = null;
//        if (!hadClass) {
//            ctxDecl = synthesizeProgramClass(program.precompile, ctx).classDecl().classBlock().classLines();
//        }
//        else {
//            var candidate = ctx.children.stream().filter((s) -> s instanceof DeclarationContext c && c.classDecl() != null && c.classDecl().mainClass.getText().equals(program.fileName.toString().split("\\.")[0])).map((s) -> ((DeclarationContext) s).classDecl().classBlock().classLines()).findFirst();
//            if (candidate.isPresent()) {
//                ctxDecl = candidate.get();
//            }
//        }
//
//        if (!hadClass && ctxDecl.declaration().stream().noneMatch((s) -> s.functionDecl() != null && s.functionDecl() instanceof RunnableFunctionDeclContext void_ && void_.ID().getText().equalsIgnoreCase("main"))) {
//            synthesizeMainFunction(ctxDecl);
//        }
//        else if (hadClass && ctxDecl == null &&
//                ctx.declaration().stream().noneMatch((s) -> s.functionDecl() != null && s.functionDecl() instanceof RunnableFunctionDeclContext void_ && void_.ID().getText().equalsIgnoreCase("main"))
//        ) {
//            synthesizeMainFunction(ctx);
//        }
//        else if (hadClass && ctxDecl != null &&
//                ctx.declaration().stream().noneMatch((s) -> s.functionDecl() != null && s.functionDecl() instanceof RunnableFunctionDeclContext void_ && void_.ID().getText().equalsIgnoreCase("main"))  &&
//                ctxDecl.declaration().stream().noneMatch((s) -> s.functionDecl() != null && s.functionDecl() instanceof RunnableFunctionDeclContext void_ && void_.ID().getText().equalsIgnoreCase("main"))
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
//    }
//
//    public List<ParseTree> buildBottomUpDepthFirstChildren(ParseTree ctx) {
//        class BottomUpDepthFirst {
//            public List<AbstractMap.SimpleEntry<Integer, ParseTree>> layers = new ArrayList<>();
//
//            public List<ParseTree> traverse(ParseTree ctx) {
//                return traverse(ctx, 0);
//            }
//
//            private List<ParseTree> traverse(ParseTree ctx, int depth) {
//                if (ctx != null) {
//                    for (int i = 0; i < ctx.getChildCount(); i++) {
//                        traverse(ctx.getChild(ctx.getChildCount()-i-1), depth+1);
//                    }
//                }
//
//                layers.add(new AbstractMap.SimpleEntry<>(depth, ctx));
//
//                if (depth == 0) {
//                    layers.sort(Comparator.comparingInt(AbstractMap.SimpleEntry::getKey));
//                    layers = layers.reversed();
//
//                    return layers.stream().map(AbstractMap.SimpleEntry::getValue).toList();
//                }
//
//                return null;
//            }
//        }
//        return new BottomUpDepthFirst().traverse(ctx);
//    }
//
//    public void mapDeclarations(ParseTree ctx, FluxCompiler.Program program) {
//        if (ctx != null) {
//            var children = buildBottomUpDepthFirstChildren(ctx);
//
//            for (var child : children) {
//                if (isDeclaration(child) && !program.javaCode.declarations.containsKey(child)) {
//                    var declaration = simplifyDeclaration(child);
//
//                    if (declaration != null) {
//                        program.javaCode.HashDeclaration(child, declaration);
//                    }
//                }
//            }
//        }
//    }
//
//    public Declaration simplifyDeclaration(LocalVarDeclContext ctx) {
//        if (ctx != null) {
//
//            var type = simplifyLocalVarType(ctx);
//
//            return new Declaration(type.type, type.id, ctx, ctm.of("var"));
//        }
//        return null;
//    }
//
//    public Declaration simplifyDeclaration(DeclarationContext ctx) {
//        if (ctx.varDecl() != null) {
//            var globalDecl = ctx.varDecl();
//
//            var localType = simplifyLocalVarType(ctx.varDecl().localVarDecl());
//
//            return new Declaration(localType.type, localType.id, globalDecl.localVarDecl(), ctm.of("var"));
//        } else if (ctx.functionDecl() != null) {
//            return simplifyDeclaration(ctx.functionDecl());
//        } else if (ctx.importDecl() != null) {
//            return simplifyDeclaration(ctx.importDecl());
//        }
//        return null;
//    }
//
//    public Declaration simplifyDeclaration(ImportDeclContext ctx) {
//        ComplexType declType = ctm.of(String.format("import%s", ctx.qualifiedId().getText().startsWith("static") ? " static" : ""));
//        String declId = ctx.qualifiedId().getText().replace("static ", "");
//
//        return new Declaration(declType, declId, ctx, ctm.of("library"));
//    }
//
//    public Declaration simplifyDeclaration(VarDeclContext ctx) {
//        var localType = simplifyLocalVarType(ctx.localVarDecl());
//
//        return new Declaration(localType.type, localType.id, ctx.localVarDecl(), ctm.of("var"));
//    }
//
//    public Declaration simplifyDeclaration(FunctionDeclContext ctx) {
//
//        ComplexType type = null;
//        if (ctx instanceof RunnableFunctionDeclContext decl) {
//            type = ctm.of("void");
//        } else if (ctx instanceof ConsumerFunctionDeclContext decl) {
//            type = ctm.of(visit(decl.type()));
//        } else if (ctx instanceof VarFunctionDeclContext decl) {
//            Set<ComplexType> types = new HashSet<>();
//
//            collectReturnTypes(decl.returnBlock(), types);
//
//            if (types.size() == 1) {
//                type = types.stream().findFirst().get();
//            } else {
//                type = ctm.of("illegal");
//            }
//        }
//
//        String id = "";
//        if (ctx instanceof RunnableFunctionDeclContext decl) {
//            id = decl.ID().getText();
//        } else if (ctx instanceof ConsumerFunctionDeclContext decl) {
//            id = decl.ID().getText();
//        } else if (ctx instanceof VarFunctionDeclContext decl) {
//            id = decl.ID().getText();
//        }
//
//        return new Declaration(type, id, ctx, ctm.of("function"));
//    }
//
//    public Declaration simplifyDeclaration(ClassDeclContext ctx) {
//        return new Declaration(ctm.of("class"), ctx.mainClass.getText(), ctx, ctm.of("class"));
//    }
//
//    public Declaration simplifyDeclaration(FormalParameterContext ctx) {
//        return new Declaration(ctm.of(visit(ctx.type())), ctx.packedId().getText(), ctx, ctm.of("var"));
//    }
//
//    private void collectReturnTypes(ParseTree ctx, Set<ComplexType> types) {
//        var children = buildBottomUpDepthFirstChildren(ctx);
//        for (var child : children) {
//            if (child instanceof ExpressionReturnContext ret) {
//                var type = getAutoType(ret.expression());
//
//                if (!type.is("unknown")) {
//                    types.add(type);
//                }
//            }
//        }
//    }
//
//    public boolean isDeclaration(Object ctx) {
//        if (ctx instanceof LocalVarDeclContext decl) {
//            return true;
//        } else if (ctx instanceof DeclarationContext decl) {
//            return true;
//        } else if (ctx instanceof VarDeclStatementContext decl) {
//            return true;
//        } else if (ctx instanceof FunctionDeclStatementContext decl) {
//            return true;
//        } else if (ctx instanceof VarDeclContext decl) {
//            return true;
//        } else if (ctx instanceof FunctionDeclContext decl) {
//            return true;
//        } else if (ctx instanceof ImportDeclContext decl) {
//            return true;
//        } else if (ctx instanceof ClassDeclContext decl) {
//            return true;
//        } else if (ctx instanceof FormalParameterContext decl) {
//            return true;
//        }
//        return false;
//    }
//
//    public Declaration simplifyDeclaration(Object ctx) {
//        if (ctx instanceof LocalVarDeclContext decl) {
//            return simplifyDeclaration(decl);
//        } else if (ctx instanceof DeclarationContext decl) {
//            return simplifyDeclaration(decl);
//        } else if (ctx instanceof VarDeclStatementContext decl) {
//            return simplifyDeclaration(decl.varDecl());
//        } else if (ctx instanceof FunctionDeclStatementContext decl) {
//            return simplifyDeclaration(decl.functionDecl());
//        } else if (ctx instanceof VarDeclContext decl) {
//            return simplifyDeclaration(decl);
//        } else if (ctx instanceof FunctionDeclContext decl) {
//            return simplifyDeclaration(decl);
//        } else if (ctx instanceof ImportDeclContext decl) {
//            return simplifyDeclaration(decl);
//        } else if (ctx instanceof ClassDeclContext decl) {
//            return simplifyDeclaration(decl);
//        } else if (ctx instanceof FormalParameterContext decl) {
//            return simplifyDeclaration(decl);
//        }
//        return null;
//    }
//
//    private boolean hasIDField(Object obj) {
//        Class<?> clazz = obj.getClass();
//        while (clazz != null) {
//            try {
//                clazz.getDeclaredField("ID");
//                return true;
//            } catch (NoSuchFieldException e) {
//                clazz = clazz.getSuperclass();
//            }
//        }
//        return false;
//    }
//
//    private void processDeclaration(DeclarationContext declarationCtx, FluxCompiler.Program program) {
//        if (program.javaCode.checkDeclarations) return;
//
//        if (!program.javaCode.programDeclarations.containsKey(declarationCtx)) {
//            String javaString = "";
//
//            if (declarationCtx.importDecl() != null) {
//                javaString = visit(declarationCtx.importDecl());
//                program.javaCode.addLine(javaString, program.javaCode.atIndex);
//                program.javaCode.lastImportsLine += javaString.length() + 1;
//            } else if (declarationCtx.functionDecl() != null) {
//                String functionCode = visit(declarationCtx.functionDecl());
//                if (functionCode != null) {
//                    javaString = functionCode;
//                    program.javaCode.addLine(javaString, program.javaCode.atIndex);
//                }
//            } else if (declarationCtx.varDecl() != null) {
//                javaString = visit(declarationCtx.varDecl());
//                program.javaCode.addLine(javaString, program.javaCode.atIndex);
//            } else if (declarationCtx.classDecl() != null) {
//                visit(declarationCtx.classDecl());
//            }
//
//            program.javaCode.checkDeclaration(declarationCtx, javaString.length() + 1);
//        }
//    }
//
//    @Override
//    public String visitClassDecl(ClassDeclContext ctx) {
//        var program = getProgram(ctx);
//
//        String javaString = String.format("%sclass %s%s {", !ctx.functionModifiers().getText().isEmpty() ? ctx.functionModifiers().getText() + " " : "", ctx.mainClass.getText(), ctx.extendsClass != null ? String.format(" extends %s", ctx.extendsClass.getText()) : "");
//
//        program.javaCode.addLine(javaString, program.javaCode.atIndex);
//        program.javaCode.atIndex += javaString.length() + 1;
//
//        javaString = visit(ctx.classBlock().classLines());
//
//        program.javaCode.addLine(javaString, program.javaCode.atIndex);
//        program.javaCode.atIndex += javaString.length() + 1;
//
//        javaString = "}";
//
//        program.javaCode.addLine(javaString, program.javaCode.atIndex);
//        program.javaCode.atIndex += javaString.length() + 1;
//
//        return null;
//    }
//
//    @Override
//    public String visitType(TypeContext ctx) {
//        StringBuilder builder = new StringBuilder();
//        if (!ctx.type().isEmpty()) {
//            if (ctx.getText().contains("[]")) {
//                builder.append(convertFluxType(visit(ctx.type(0)))).append("[]");
//            }
//            else {
//                builder.append(convertFluxType(visit(ctx.type(0)))).append("<").append(convertFluxType(visit(ctx.type(1)))).append(">");
//            }
//        } else {
//            if (ctx.ID() != null) {
//                return convertFluxType(visit(ctx.ID()));
//            }
//
//            return convertFluxType(ctx.getText());
//        }
//
//        return builder.toString();
//    }
//
//    private DeclarationContext synthesizeProgramClass(boolean precompile, ProgramContext parent) {
//        DeclarationContext declCtx = new DeclarationContext(parent, -1);
//
//        ClassDeclContext classDecl = new ClassDeclContext(declCtx, -1);
//
//        FunctionModifiersContext modifiers = new FunctionModifiersContext(classDecl, -1);
//
//        AccessModifierContext accessCtx = new AccessModifierContext(modifiers, -1);
//
//        if (!precompile)
//            accessCtx.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.T__0, "public")));
//
//        modifiers.addChild(accessCtx);
//
//        classDecl.addChild(modifiers);
//
//        FluxCompiler.Program program = getProgram(parent);
//
//        ClassNameContext nameContext = new ClassNameContext(classDecl, -1);
//
//        QualifiedIdContext nameId = new QualifiedIdContext(nameContext, -1);
//        nameId.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.ID, program.fileName.toString().split("\\.")[0])));
//
//        nameContext.addChild(nameId);
//
//        classDecl.mainClass = nameContext;
//
//        ClassBlockContext classBlock = new ClassBlockContext(classDecl, -1);
//        classBlock.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.T__3, "{"))); // '{'
//
//        ClassLinesContext classLines = new ClassLinesContext(classBlock, -1);
//
//        classBlock.addChild(classLines);
//        classBlock.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.T__4, "}"))); // '}'
//
//        classDecl.addChild(classBlock);
//        declCtx.addChild(classDecl);
//
//        if (parent.children == null) {
//
//        } else {
//            if (classLines.children == null) {
//                classLines.children = new ArrayList<>();
//            }
//            var classChildren = new ArrayList<ParseTree>();
//            var parentChildren = new ArrayList<ParseTree>();
//            for (var child : parent.children) {
//                if (child instanceof TerminalNodeImpl node && node.symbol.getType() == Token.EOF)
//                    continue;
//
//                if ((child instanceof DeclarationContext declaration && declaration.importDecl() != null) || (child instanceof TerminalNodeImpl node && node.symbol.getType() == Token.EOF)) {
//                    parentChildren.add(child);
//                }
//                else {
//                    classChildren.add(child);
//                }
//            }
//
//            classLines.children = classChildren;
//            parent.children = parentChildren;
//        }
//        parent.addChild(declCtx);
//        parent.addChild(new TerminalNodeImpl(new CommonToken(Token.EOF, "<EOF>")));
//
//        return declCtx;
//    }
//
//    private DeclarationContext synthesizeMainFunction(ParserRuleContext parent) {
//        DeclarationContext declCtx = new DeclarationContext(parent, -1);
//
//        FunctionDeclContext funcDecl = new FunctionDeclContext(declCtx, -1);
//
//        RunnableFunctionDeclContext mainFunc = new RunnableFunctionDeclContext(funcDecl);
//
//        FunctionModifiersContext modifiers = new FunctionModifiersContext(funcDecl, -1);
//
//        AccessModifierContext accessCtx = new AccessModifierContext(modifiers, -1);
//
//        accessCtx.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.T__0, "public")));
//
//        modifiers.addChild(accessCtx);
//
//        mainFunc.addChild(modifiers);
//
//        mainFunc.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.VOID, "void")));
//        TerminalNodeImpl idNode = new TerminalNodeImpl(new CommonToken(FluxLexer.ID, "main"));
//        mainFunc.addChild(idNode);
//
//        mainFunc.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.T__1, "(")));
//        mainFunc.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.T__2, ")")));
//
//        VoidBlockContext voidBlock = new VoidBlockContext(mainFunc, -1);
//        voidBlock.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.FIGURE_BRACKET_L, "{"))); // '{'
//
//        VoidLinesContext voidLines = new VoidLinesContext(voidBlock, -1);
//
//        voidBlock.addChild(voidLines);
//        voidBlock.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.FIGURE_BRACKET_R, "}"))); // '}'
//
//        mainFunc.addChild(voidBlock);
//        funcDecl.addChild(mainFunc);
//        declCtx.addChild(funcDecl);
//
//        if (parent.children == null) {
//        } else {
//            if (voidLines.children == null) {
//                voidLines.children = new ArrayList<>();
//            }
//            var classChildren = new ArrayList<ParseTree>();
//            var parentChildren = new ArrayList<ParseTree>();
//            for (var child : parent.children) {
//                if (child instanceof StatementContext) {
//                    classChildren.add(child);
//                    child.setParent(voidLines);
//                }
//                else {
//                    parentChildren.add(child);
//                    child.setParent(parent);
//                }
//            }
//
//            voidLines.children = classChildren;
//            parent.children = parentChildren;
//        }
//        parent.addChild(declCtx);
//
//        return declCtx;
//    }
//
//    public void ensureGeneratorDeclarations(GeneratorExprContext ctx) {
//        var program = getProgram(ctx);
//
//        for (var iter : ctx.generator_for().stream().map((s) -> List.of(s.iterId, s.collection)).toList()) {
//            if (program.javaCode.existsDeclaration(iter.getFirst().getText())) return;
//
//            var strictLocalDecl = new StrictlyTypedLocalVarContext(new LocalVarDeclContext(ctx, -1));
//
//            var typeCtx = new TypeContext(strictLocalDecl, -1);
//            var qualifiedId = new QualifiedIdContext(typeCtx, -1);
//            typeCtx.addChild(qualifiedId);
//
//            var type = getAutoType(iter.get(1));
//
//            qualifiedId.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.ID, type.getFluxType())));
//
//            var packedId = new PackedIdContext(strictLocalDecl, -1);
//            packedId.addChild(iter.getFirst());
//
//            var arrayAccess = new ArrayAccessExprContext(new ExpressionContext(strictLocalDecl, -1));
//
//            arrayAccess.collection = (ExpressionContext) iter.get(1);
//
//            var intExpr = new IntExprContext(new ExpressionContext(arrayAccess, -1));
//            intExpr.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.T__0, "[")));
//            intExpr.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.INT, "0")));
//            intExpr.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.T__1, "]")));
//
//            arrayAccess.element = intExpr;
//
//            strictLocalDecl.addChild(typeCtx);
//            strictLocalDecl.addChild(packedId);
//            strictLocalDecl.addChild(new TerminalNodeImpl(new CommonToken(FluxLexer.T__0, "=")));
//            strictLocalDecl.addChild(arrayAccess);
//
//            program.javaCode.HashDeclaration(strictLocalDecl, type.subtypes.getFirst(), iter.getFirst().getText());
//        }
//    }
//
//    private List<GeneratorExprContext> generatorExprContexts = new ArrayList<>();
//    @Override
//    public String visitGeneratorExpr(GeneratorExprContext ctx) {
//        ensureGeneratorDeclarations(ctx);
//
//        var item = ctx.item;
//
//        String uniqueId = "";
//        if (item != null) {
//            ComplexType listType = getAutoType(item);
//
//            String gs = String.join("", ctx.generator_for().stream().map((g) -> g.iterId.getText()).toList());
//
//            uniqueId = String.format("_generator_%s_%s", gs, ctx.hashCode());
//
//            if (!generatorExprContexts.contains(ctx))
//                pendingAssertions.add(-1, String.format("List<%s> %s = new ArrayList<>();", listType.getJavaClassType(), uniqueId));
//
//        }
//
//        if (!generatorExprContexts.contains(ctx)) {
//            pendingAssertions.add(-1, "{");
//            int n = 0;
//            for (var generator : ctx.generator_for()) {
//                pendingAssertions.add(-1, String.format("for (var %s : %s) {", generator.iterId.getText(), visit(generator.collection)));
//                n++;
//            }
//
//            if (item != null) {
//                pendingAssertions.add(-1, String.format("%s.add(%s);", uniqueId, visit(item)));
//            } else {
//                pendingAssertions.add(-1, String.format("%s", visit(ctx.blk)));
//            }
//
//            for (int i = 0; i < n; i++) {
//                pendingAssertions.add(-1, "}");
//            }
//            pendingAssertions.add(-1, "}");
//
//            generatorExprContexts.add(ctx);
//        }
//
//        return uniqueId;
//    }
//
//    @Override
//    public String visitRangeExpr(RangeExprContext ctx) { //TODO: Finish
//        ensureImport(getProgram(ctx), StandardFluxLibs.STATIC_FLUX_UTILS);
//
//        if (ctx.rangeToken().size() == 1) {
//            return String.format("range(%s)", ctx.rangeToken(0));
//        }
//        return "";
//    }
//
//    @Override
//    public String visitListExpr(ListExprContext ctx) {
//        return String.format("new ArrayList<>(List.of(%s))", String.join(", ", ctx.expressionList() != null ? ctx.expressionList().expression().stream().map(this::visit).toList() : List.of()));
//    }
//
//    @Override
//    public String visitArrayAccessExpr(ArrayAccessExprContext ctx) {
//        var element = visit(ctx.element);
//
//        var declaration = getDeclaration(visit(ctx.collection), ctx);
//
//        var getter = element.equals("0") ? ".getFirst()" : String.format(".get(%s)", element);
//
//        if (declaration != null && declaration.type.is("array")) {
//            getter = String.format("[%s]", element);
//        }
//
//        return String.format("%s%s", visit(ctx.collection), getter);
//    }
//
//    @Override
//    public String visitSetExpr(SetExprContext ctx) {
//        return String.format("new ArrayList<>(new HashSet<>(List.of(%s)))", String.join(", ", ctx.expressionList() != null ? ctx.expressionList().expression().stream().map(this::visit).toList() : List.of()));
//    }
//
//    @Override
//    public String visitDictExpr(DictExprContext ctx) {
//        return String.format("new HashMap<>(Map.of(%s))", String.join(", ", ctx.expressionDict() != null ? ctx.expressionDict().dictElement().stream().map((s) -> String.join(", ", List.of(visit(s.key), visit(s.value)))).toList() : List.of()));
//    }
//
//    @Override
//    public String visitNullExpr(NullExprContext ctx) {
//        return "null";
//    }
//
//    @Override
//    public String visitImportDecl(ImportDeclContext ctx) {
//        return String.format("import%s %s%s;", ctx.static_ != null ? " static" : "", ctx.qualifiedId().getText(), ctx.wildcard != null ? ctx.wildcard.getText() : "");
//    }
//
//    @Override
//    public String visitIntExpr(IntExprContext ctx) {
//        return ctx.INT().getText().toUpperCase();
//    }
//
//    @Override
//    public String visitAddSubExpr(AddSubExprContext ctx) {
//        return String.format("%s %s %s", visit(ctx.expression(0)), ctx.operator.getText(), visit(ctx.expression(1)));
//    }
//
//    @Override
//    public String visitQualifiedId(QualifiedIdContext ctx) {
//        return ctx.getText();
//    }
//
//    @Override
//    public String visitShiftExpr(ShiftExprContext ctx) {
//        return String.format("%s %s %s", visit(ctx.expression(0)), ctx.operator.getText(), visit(ctx.expression(1)));
//    }
//
//    @Override
//    public String visitTernaryExpr(TernaryExprContext ctx) {
//        return String.format("((%s) ? %s : %s)", visit(ctx.condition), visit(ctx.true_), visit(ctx.false_));
//    }
//
//    @Override
//    public String visitVariableAccessExpr(VariableAccessExprContext ctx) {
//        var mapped = typeTree.map(ctx);
//
//        return !mapped.isEmpty() ? mapped : String.format("%s.%s", visit(ctx.acc), visit(ctx.variable));
//    }
//
//    @Override
//    public String visitMulDivExpr(MulDivExprContext ctx) {
//        String operator = ctx.operator.getText();
//
//        if (operator.equals("mod")) operator = "%";
//
//        return String.format("%s %s %s", visit(ctx.expression(0)), operator, visit(ctx.expression(1)));
//    }
//
//    @Override
//    public String visitEqualityExpr(EqualityExprContext ctx) {
//        return String.format("%s %s %s", visit(ctx.expression(0)), ctx.operator.getText(), visit(ctx.expression(1)));
//    }
//
//    @Override
//    public String visitRelationalExpr(RelationalExprContext ctx) {
//        return visitRelation(ctx, ctx.expression(0), ctx.expression(1), ctx.operator.getText());
//    }
//
//    @Override
//    public String visitApproximatelyExpr(ApproximatelyExprContext ctx) {
//        ensureImport(getProgram(ctx), StandardFluxLibs.STATIC_FLUX_UTILS);
//
//        var precision = "";
//        var minus = "";
//        if (ctx.precision != null) {
//            precision = visit(ctx.precision) + ", ";
//            minus = ctx.minus != null ? "-" : "";
//        }
//        else if (ctx.neg_precision != null) {
//            precision = visit(ctx.neg_precision) + ", ";
//            minus = ctx.minus == null ? "-" : "";
//        }
//
//        return String.format("approximately(%s%s%s, %s)", minus, precision, visit(ctx.expression(0)), visit(ctx.expression(1)));
//    }
//
//    public String visitRelation(ParseTree ctx, ParseTree leftExpr, ParseTree rightExpr, String operator) {
//        var parent = ctx.getParent();
//        String left = visit(leftExpr);
//        String right = visit(rightExpr);
//
//        if (parent instanceof RelationalExprContext) {
//            if (rightExpr instanceof FunctionCallExprContext idExpr) {
//                var declaration = getDeclaration(idExpr.ID().getText(), ctx);
//
//                if (declaration != null) {
//                    String varName = "_" + declaration.id + "$" + ctx.hashCode();
//                    String typePrefix = (getDeclaration(varName, ctx) == null) ? declaration.type.getJavaType() + " " : "";
//
//                    pendingAssertions.add(String.format("%s%s = %s;", typePrefix, varName, right));
//
//                    return String.format("%s %s %s && %s", left, operator, varName, varName);
//                }
//            }
//            return String.format("%s %s %s && %s", left, operator, right, right);
//        }
//
//        return String.format("%s %s %s", left, operator, right);
//    }
//
//    @SafeVarargs
//    public static <T> List<T> listNonNull(T... objects) {
//        return Stream.of(objects)
//                .filter(Objects::nonNull).toList();
//    }
//
//    @SafeVarargs
//    public static <T> T firstNonNull(T... objects) {
//        return Stream.of(objects)
//                .filter(Objects::nonNull)
//                .findFirst()
//                .orElse(null);
//    }
//
//    @Override
//    public String visitBitwiseANDExpr(BitwiseANDExprContext ctx) {
//        return String.format("%s & %s", visit(ctx.expression(0)), visit(ctx.expression(1)));
//    }
//
//    @Override
//    public String visitBitwiseXORExpr(BitwiseXORExprContext ctx) {
//        return String.format("%s ^ %s", visit(ctx.expression(0)), visit(ctx.expression(1)));
//    }
//
//    @Override
//    public String visitBitwiseORExpr(BitwiseORExprContext ctx) {
//        return String.format("%s | %s", visit(ctx.expression(0)), visit(ctx.expression(1)));
//    }
//
//    @Override
//    public String visitLogicalANDExpr(LogicalANDExprContext ctx) {
//        return String.format("%s && %s", visit(ctx.expression(0)), visit(ctx.expression(1)));
//    }
//
//    @Override
//    public String visitCastExpr(CastExprContext ctx) {
//        return String.format("(%s)%s", visit(ctx.type()), visit(ctx.expression()));
//    }
//
//    @Override
//    public String visitLogicalORExpr(LogicalORExprContext ctx) {
//        return String.format("%s || %s", visit(ctx.expression(0)), visit(ctx.expression(1)));
//    }
//
//    @Override
//    public String visitUnaryExpr(UnaryExprContext ctx) {
//        return ctx.operator.getText() + visit(ctx.expression());
//    }
//
//    @Override
//    public String visitDecimalExpr(DecimalExprContext ctx) {
//        ComplexType type = null;
//        String text = ctx.DECIMAL().getText();
//
//        var varDecl = getToClosestParent(ctx, VarDeclContext.class);
//        if (varDecl != null) {
//            var localType = simplifyLocalVarType(varDecl.localVarDecl());
//
//            type = localType.type;
//        }
//        var varStatDecl = getToClosestParent(ctx, VarDeclStatementContext.class);
//        if (varStatDecl != null) {
//            var localType = simplifyLocalVarType(varStatDecl.varDecl().localVarDecl());
//
//            type = localType.type;
//        }
//        var assignmentStat = getToClosestParent(ctx, AssignmentStatContext.class);
//        if (assignmentStat != null) {
//            var assignment = getAssignment(assignmentStat);
//
//            var declaration = getDeclaration(visit(assignment.expressionContext), ctx);
//            if (declaration != null) {
//                type = declaration.type;
//            }
//        }
//        var functionCallStat = getToClosestParent(ctx, FunctionCallExprContext.class);
//        if (functionCallStat != null) {
//            var functionDeclStat = firstNonNull(
//                    Objects.requireNonNull(getToClosestSibling(ctx, FunctionDeclStatementContext.class)).functionDecl(),
//                    getToClosestSibling(ctx, FunctionDeclContext.class)
//            );
//            var formalParameters = functionDeclStat instanceof RunnableFunctionDeclContext ? ((RunnableFunctionDeclContext) functionDeclStat).formalParameters() : ((ConsumerFunctionDeclContext) functionDeclStat).formalParameters();
//            if (formalParameters != null) {
//                int i = 0;
//                int j = whatParameterAmI(ctx, functionCallStat.kwargs());
//
//                for (var parameter : formalParameters.formalParameter()) {
//                    if (i == j) {
//                        var declaration = getDeclaration(parameter.packedId().getText(), ctx);
//                        if (declaration != null) {
//                            type = declaration.type;
//                        }
//                    }
//                    i += 1;
//                }
//            }
//        }
//
//        if (type != null) {
//            if (type.is("float"))
//                return text.endsWith("f") || text.endsWith("F") ? text : text + "f";
//        }
//        return ctx.DECIMAL().getText();
//    }
//
//    public static class LocalVarType {
//        public ComplexType type;
//        public String id;
//        public ExpressionContext expression;
//
//        public LocalVarType(ComplexType type, String id, ExpressionContext expression) {
//            this.type = type;
//            this.id = id;
//            this.expression = expression;
//        }
//
//        @Override
//        public String toString() {
//            return String.format("<%s %s>", type, id);
//        }
//    }
//
//    public LocalVarType simplifyLocalVarType(LocalVarDeclContext ctx) {
//        if (ctx instanceof StrictlyTypedLocalVarContext localCtx) {
//            return new LocalVarType(ctm.of(visit(localCtx.type())), localCtx.packedId().getText(), localCtx.expression());
//        } else if (ctx instanceof LooselyTypedLocalVarContext localCtx) {
//            return new LocalVarType(getAutoType(localCtx.expression()), localCtx.packedId().getText(), localCtx.expression());
//        }
//        return null;
//    }
//
//    public int whatParameterAmI(ParseTree ctx, KwargsContext parent) {
//        if (parent.expression() != null) {
//            return whatParameterAmI(ctx, parent.expression());
//        }
//        else {
//            return whatParameterAmI(ctx, parent.kwarg().stream().map(KwargContext::expression).toList());
//        }
//    }
//
//    public int whatParameterAmI(ParseTree ctx, List<ExpressionContext> expressions) {
//        for (int i = 0; i < expressions.size(); i++) {
//            if (expressions.get(i).equals(ctx)) return i;
//        }
//        return -1;
//    }
//
//    public int whatParameterAmI(ParseTree ctx, ExpressionListContext parent) {
//        return whatParameterAmI(ctx, parent.expression());
//    }
//
//    public int whatChildAmI(ParseTree ctx) {
//        return whatChildAmI(ctx, ctx.getParent());
//    }
//
//    public int whatChildAmI(ParseTree ctx, ParseTree parent) {
//        for (int i = 0; i < parent.getChildCount(); i++) {
//            if (parent.getChild(i).equals(ctx)) return i;
//        }
//        return -1;
//    }
//
//    public static class Declaration {
//        public ComplexType type;
//        public String id;
//        public ParseTree declaration;
//        public ParserRuleContext scope;
//        private List<ParserRuleContext> scopeList;
//
//        public ComplexType declarationType = ctm.of("var");
//
//        public Declaration(ComplexType type, String id, ParseTree declaration) {
//            this.type = type;
//            this.id = id;
//            this.declaration = declaration;
//            this.scope = getScopeList().getFirst();
//        }
//
//        public Declaration(ComplexType type, String id, ParseTree declaration, ComplexType declarationType) {
//            this.type = type;
//            this.id = id;
//            this.declaration = declaration;
//            this.scope = getScopeList().getFirst();
//
//            this.declarationType = declarationType;
//        }
//
//        public List<ParserRuleContext> getScopeList() {
//            if (scopeList == null) {
//                scopeList = Declaration.getScopeList(declaration);
//            }
//            return scopeList;
//        }
//
//        public static List<ParserRuleContext> getScopeList(ParseTree ctx) {
//            return getToClosestParentsOfTypes(ctx,
//                            GeneratorExprContext.class,
//                            ReturnBlockContext.class,
//                            VoidBlockContext.class,
//                            ClassBlockContext.class,
//                            ProgramContext.class
//                    ).stream().filter((s) -> s instanceof ParserRuleContext).map((s) -> (ParserRuleContext) s).toList();
//        }
//
//        @Override
//        public String toString() {
//            return String.format("[%s %s%s]", type, id, declarationType.is("function") ? "()" : "");
//        }
//    }
//
//    public Declaration getDeclaration(ParseTree ctx) {
//        var program = getProgram(ctx);
//
//        return program.javaCode.declarations.get(ctx);
//    }
//
//    public List<ParserRuleContext> getChildrenPath(List<ParseTree> children, ParserRuleContext me) {
//        class ChildrenPathClass {
//            public static final List<ParserRuleContext> childrenMap = new ArrayList<>();
//
//            public static List<ParserRuleContext> getChildrenPath(List<ParseTree> children, ParserRuleContext me, AtomicBoolean foundMe) {
//                if (foundMe.get()) return childrenMap;
//
//                var validChildren = children.stream().filter((s) -> s instanceof ParserRuleContext).map((s) -> (ParserRuleContext) s).toList();
//                for (var child : validChildren) {
//                    if (foundMe.get()) break;
//
//                    if (!childrenMap.contains(child)) {
//
//                        childrenMap.add(child);
//                    }
//
//                    if (child.equals(me)) {
//                        foundMe.set(true);
//                        return childrenMap;
//                    }
//
//                    if (child.children != null)
//                        getChildrenPath(child.children, me, foundMe);
//                }
//                return childrenMap;
//            }
//        }
//
//        return ChildrenPathClass.getChildrenPath(children, me, new AtomicBoolean(false));
//    }
//
//    public Declaration getDeclaration(String id, ParseTree ctx) {
//        var program = getProgram(ctx);
//
//        var scopeList = Declaration.getScopeList(ctx);
//
//        Declaration declaration = null;
//        for (var scope : scopeList) {
//            var declarations = program.javaCode.declarations.entrySet().stream().filter((s) -> s.getValue().scope == scope);
//
//            var candidates = declarations.filter((s) -> s.getValue().id.equals(id)).map(Map.Entry::getValue).toList();
//
//            if (!candidates.isEmpty()) {
//                declaration = candidates.getLast();
//                break;
//            }
//        }
//
//        return declaration;
//    }
//
//    @Override
//    public String visitPostfixExpr(PostfixExprContext ctx) {
//        return String.format("%s%s", visit(ctx.expression()), ctx.operator.getText());
//    }
//
//    @Override
//    public String visitIdExpr(IdExprContext ctx) {
//        return ctx.getText();
//    }
//
//    @Override
//    public String visitExpExpr(ExpExprContext ctx) {
//        return visitExp(ctx.expression(0), ctx.expression(1), ctx);
//    }
//
//    public String visitExp(Object exp1, Object exp2, ParseTree ctx) {
//        ensureImport(ctx, StandardFluxLibs.STATIC_MATH_UTILS);
//
//        return visitBinaryOp(exp1, exp2, ctx,
//                (e1, e2) -> String.format("power(%s, %s)", e1, e2));
//    }
//
//    @Override
//    public String visitTetrExpr(TetrExprContext ctx) {
//        return visitTetr(ctx.expression(0), ctx.expression(1), ctx);
//    }
//
//    public String visitTetr(Object exp1, Object exp2, ParseTree ctx) {
//        ensureImport(ctx, StandardFluxLibs.STATIC_MATH_UTILS);
//
//        return visitBinaryOp(exp1, exp2, ctx,
//                (e1, e2) -> String.format("estimateTetration(%s, %s)", e1, e2));
//    }
//
//    public DeclarationContext ensureImport(ParseTree ctx, String importPath) {
//        return ensureImport(getProgram(ctx), importPath);
//    }
//
//    public DeclarationContext ensureImport(FluxCompiler.Program program, String importPath) {
//        if (program != null && !program.imports.contains(importPath)) {
//            var declarationCtx = program.addImport(importPath);
//
//            ImportDeclContext declCtx = declarationCtx.importDecl();
//
//            int indent = program.javaCode.indentLevel;
//
//            String javaString = visit(declCtx);
//
//            program.javaCode.indentLevel = 0;
//            program.javaCode.addLine(javaString, program.javaCode.lastImportsLine);
//            program.javaCode.indentLevel = indent;
//
//            int index = javaString.length() + 1;
//
//            program.javaCode.lastImportsLine += index;
//            program.javaCode.checkDeclaration(declarationCtx, index);
//
//            return declarationCtx;
//        }
//        return null;
//    }
//
//    @Override
//    public String visitFloorDivExpr(FloorDivExprContext ctx) {
//        return visitFloorDiv(ctx.expression(0), ctx.expression(1), ctx);
//    }
//
//    public String visitFloorDiv(Object exp1, Object exp2, ParseTree ctx) {
//        ensureImport(getProgram(ctx), StandardFluxLibs.STATIC_MATH_UTILS);
//
//        return visitBinaryOp(exp1, exp2, ctx,
//                (e1, e2) -> String.format("Math.floor(%s / %s)", e1, e2));
//    }
//
//    @Override
//    public String visitCeilDivExpr(CeilDivExprContext ctx) {
//        return visitCeilDiv(ctx.expression(0), ctx.expression(1), ctx);
//    }
//
//    public String visitCeilDiv(Object exp1, Object exp2, ParseTree ctx) {
//        ensureImport(getProgram(ctx), StandardFluxLibs.STATIC_MATH_UTILS);
//
//        return visitBinaryOp(exp1, exp2, ctx,
//                (e1, e2) -> String.format("Math.ceil(%s / %s)", e1, e2));
//    }
//
//    @Override
//    public String visitCreationExpr(CreationExprContext ctx) {
//        String args = "";
//        if (ctx.expressionList() != null) {
//            args = ctx.expressionList().expression().stream()
//                    .map(this::visit)
//                    .collect(Collectors.joining(", "));
//        }
//
//        String block = "";
//        if (ctx.block() != null) {
//            block = visit(ctx.block());
//        }
//
//        String typeString = visit(ctx.type());
//        var declarationContext = getToClosestParent(ctx, VarDeclContext.class);
//        if (declarationContext != null) {
//            var declaration = simplifyDeclaration(declarationContext);
//
//            getProgram(ctx).javaCode.HashDeclaration(ctx, declaration);
//            var type = declaration.type;
//            if (!type.getJavaType().isEmpty() && ctx.type().type().isEmpty()) {
//                typeString = typeString + "<>";
//            }
//        }
//
//        return String.format("new %s(%s) %s", typeString, args, block);
//    }
//
//    public String getCastString(ParseTree ctx, Object... expressions) {
//        String castString = "";
//        for (var expression : expressions) {
//            if (expression instanceof String) {
//                continue;
//            }
//
//            String id = "";
//            if (expression instanceof IdExprContext expr) {
//                id = expr.qualifiedId().getText();
//            }
//            else if (expression instanceof QualifiedIdContext expr) {
//                id = expr.getText();
//            }
//            else if (expression instanceof PackedIdContext expr) {
//                id = expr.getText();
//            }
//
//            if (!id.isEmpty()) {
//                var decl = getDeclaration(id, ctx);
//
//                castString = String.format("(%s) ", decl.type.getFluxType());
//            }
//        }
//        return castString;
//    }
//
//    public boolean canAutoPromote(ComplexType... types) {
//        Set<ComplexType> uniqueTypes = Arrays.stream(types).collect(Collectors.toSet());
//
//        Set<ComplexType> digits = Set.of(
//                ctm.of("double"),
//                ctm.of("float"),
//                ctm.of("long"),
//                ctm.of("int")
//        );
//
//        if (digits.containsAll(uniqueTypes)) {
//            return true;
//        }
//        if (uniqueTypes.contains(ctm.of("string"))) {
//            return true;
//        }
//
//        return false;
//    }
//
//    public ComplexType autoPromote(ComplexType... types) {
//        Set<ComplexType> uniqueTypes = Arrays.stream(types).collect(Collectors.toSet());
//
//        Set<ComplexType> digits = new OrderedHashSet<>(){{
//                add(ctm.of("double"));
//                add(ctm.of("float"));
//                add(ctm.of("long"));
//                add(ctm.of("int"));
//        }};
//
//        if (digits.containsAll(uniqueTypes)) {
//            for (var digit : digits) {
//                if (uniqueTypes.contains(digit)) {
//                    return digit;
//                }
//            }
//        }
//        if (uniqueTypes.contains(ctm.of("string"))) {
//            return ctm.of("string");
//        }
//
//        return null;
//    }
//
//    public ComplexType getAutoType(Object object) {
//        FluxCompiler.Program program = null;
//        if (object instanceof ParseTree expr) {
//            program = getProgram(expr);
//        }
//
//        ComplexType type = new ComplexType("unknown");
//        if (object instanceof IdExprContext expr) {
//            var decl = getDeclaration(expr.qualifiedId().getText(), expr);
//
//            if (decl != null) {
//                type = decl.type;
//            }
//            else type = ctm.of(expr.getText());
//        } else if (object instanceof CastExprContext expr) {
//            type = ctm.of(visit(expr.type()));
//        } else if (object instanceof IntExprContext expr) {
//            type = ctm.of(expr.getText().toLowerCase().contains("l") ? "long" : "int");
//        } else if (object instanceof DecimalExprContext expr) {
//            if (expr.getText().toLowerCase().contains("f")) {
//                type = ctm.of("float");
//            } else {
//                type = ctm.of("double");
//            }
//        } else if (object instanceof BoolExprContext expr) {
//            type = ctm.of("boolean");
//        } else if (object instanceof FStringExprContext expr) {
//            type = ctm.of("String");
//        } else if (object instanceof StringExprContext expr) {
//            type = ctm.of("String");
//        } else if (object instanceof ParenthesizedExprContext expr) {
//            type = getAutoType(expr.expression());
//        } else if (object instanceof CharExprContext expr) {
//            type = ctm.of("char");
//        } else if (object instanceof AddSubExprContext expr) {
//            var typeLeft = getAutoType(expr.expression(0));
//            var typeRight = getAutoType(expr.expression(1));
//
//            if (canAutoPromote(typeLeft, typeRight)) {
//                type = autoPromote(typeLeft, typeRight);
//            }
//            else if (!typeLeft.equals(typeRight)) {
//                if (program.javaCode.checkPass())
//                    throw new UnsupportedOperationException("Cannot add/subtract values of two different types");
//            }
//            else {
//                type = typeLeft;
//            }
//        } else if (object instanceof MulDivExprContext expr) {
//            var typeLeft = getAutoType(expr.expression(0));
//            var typeRight = getAutoType(expr.expression(1));
//
//            if (canAutoPromote(typeLeft, typeRight)) {
//                type = autoPromote(typeLeft, typeRight);
//            }
//            else if (!typeLeft.equals(typeRight)) {
//                if (program.javaCode.checkPass())
//                    throw new UnsupportedOperationException("Cannot multiply/divide values of two different types");
//            }
//            else {
//                type = typeLeft;
//            }
//        } else if (object instanceof ExpExprContext expr) {
//            var typeLeft = getAutoType(expr.expression(0));
//            var typeRight = getAutoType(expr.expression(1));
//
//            if (canAutoPromote(typeLeft, typeRight)) {
//                type = autoPromote(typeLeft, typeRight);
//            }
//            else if (!typeLeft.equals(typeRight)) {
//                if (program.javaCode.checkPass())
//                    throw new UnsupportedOperationException("Cannot exponentiate values of two different types");
//            }
//            else {
//                type = typeLeft;
//            }
//        } else if (object instanceof TetrExprContext expr) {
//            var typeLeft = getAutoType(expr.expression(0));
//            var typeRight = getAutoType(expr.expression(1));
//
//            if (canAutoPromote(typeLeft, typeRight)) {
//                type = autoPromote(typeLeft, typeRight);
//            }
//            else if (!typeLeft.equals(typeRight)) {
//                if (program.javaCode.checkPass())
//                    throw new UnsupportedOperationException("Cannot tetrate values of two different types");
//            }
//            else {
//                type = typeLeft;
//            }
//        } else if (object instanceof CeilDivExprContext expr) {
//            var typeLeft = getAutoType(expr.expression(0));
//            var typeRight = getAutoType(expr.expression(1));
//
//            if (canAutoPromote(typeLeft, typeRight)) {
//                type = autoPromote(typeLeft, typeRight);
//            }
//            else if (!typeLeft.equals(typeRight)) {
//                if (program.javaCode.checkPass())
//                    throw new UnsupportedOperationException("Cannot use ceil on values of two different types");
//            }
//            else {
//                type = typeLeft;
//            }
//        } else if (object instanceof FloorDivExprContext expr) {
//            var typeLeft = getAutoType(expr.expression(0));
//            var typeRight = getAutoType(expr.expression(1));
//
//            if (canAutoPromote(typeLeft, typeRight)) {
//                type = autoPromote(typeLeft, typeRight);
//            }
//            else if (!typeLeft.equals(typeRight)) {
//                if (program.javaCode.checkPass())
//                    throw new UnsupportedOperationException("Cannot use floor on values of two different types");
//            }
//            else {
//                type = typeLeft;
//            }
//        } else if (object instanceof FunctionCallExprContext expr) {
//            if (typeTree.containsKey(expr.ID().getText())) {
//                type = typeTree.get(expr.ID().getText()).typeFunction.apply(visit(expr));
//            } else {
//                var declaration = getDeclaration(expr.ID().getText(), expr);
//
//                if (declaration != null) {
//                    type = declaration.type;
//                }
//            }
//        } else if (object instanceof VarFunctionDeclContext expr) {
//            if (typeTree.containsKey(visit(expr.ID()))) {
//                type = typeTree.get(visit(expr.ID())).typeFunction.apply(visit(expr));
//            } else {
//                var declaration = simplifyDeclaration(expr);
//
//                if (declaration != null) {
//                    type = declaration.type;
//                }
//            }
//        } else if (object instanceof VariableAccessExprContext expr) {
//            var knownFunc = typeTree.get(expr);
//            if (knownFunc != null) {
//                type = knownFunc.typeFunction.apply(visit(expr));
//            } else {
//                var declaration = simplifyDeclaration(expr);
//
//                if (declaration != null) {
//                    type = declaration.type;
//                }
//            }
//        } else if (object instanceof ArrayExprContext expr) {
//            var localType = ctm.of("Object");
//            if (expr.expressionList() != null) {
//                localType = getAutoType(expr.expressionList().expression(0));
//                for (var el : expr.expressionList().expression()) {
//                    if (!Objects.equals(localType, getAutoType(el))) {
//                        localType = ctm.of("Object");
//                        break;
//                    }
//                }
//            }
//
//            type = ctm.get("list", localType);
//        } else if (object instanceof SetExprContext expr) {
//            var localType = ctm.of("Object");
//            if (expr.expressionList() != null) {
//                localType = getAutoType(expr.expressionList().expression(0));
//                for (var el : expr.expressionList().expression()) {
//                    if (!Objects.equals(localType, getAutoType(el))) {
//                        localType = ctm.of("Object");
//                        break;
//                    }
//                }
//            }
//
//            type = ctm.get("list", localType);
//        } else if (object instanceof DictExprContext expr) {
//            var localType = new ArrayList<>(List.of(ctm.of("Object"), ctm.of("Object")));
//            if (expr.expressionDict() != null) {
//                localType.set(0, getAutoType(expr.expressionDict().dictElement(0).key));
//                localType.set(1, getAutoType(expr.expressionDict().dictElement(0).value));
//                for (var el : expr.expressionDict().dictElement()) {
//                    if (!Objects.equals(localType.getFirst(), getAutoType(el.key))) {
//                        localType.set(0, ctm.of("Object"));
//                        break;
//                    }
//                }
//                for (var el : expr.expressionDict().dictElement()) {
//                    if (!Objects.equals(localType.get(1), getAutoType(el.value))) {
//                        localType.set(1, ctm.of("Object"));
//                        break;
//                    }
//                }
//            }
//
//            type = ctm.get("dict", localType);
//        } else if (object instanceof ArrayAccessExprContext expr) {
//            type = getAutoType(expr.collection).subtypes.getFirst();
//        } else if (object instanceof GeneratorExprContext expr) {
//            ensureGeneratorDeclarations(expr);
//
//            var collectionType = getAutoType(expr.item);
//            type = ctm.get("list", collectionType);
//        } else if (object instanceof ParseTree expr) {
//            type = ctm.of("var");
//        }
//
//        if (type.is("unknown") && program != null) {
//            program.javaCode.hasUnknowns = true;
//        }
//
//        return type;
//    }
//
//    private String visitBinaryOp(Object exp1, Object exp2, ParseTree ctx, BiFunction<String, String, String> formatter) {
//        String str1 = (exp1 instanceof String s) ? s : (exp1 instanceof ParseTree t) ? visit(t) : null;
//        String str2 = (exp2 instanceof String s) ? s : (exp2 instanceof ParseTree t) ? visit(t) : null;
//
//        if (str1 == null || str2 == null) return null;
//
//        String castString = getCastString(ctx, exp1, exp2);
//        return castString + formatter.apply(str1, str2);
//    }
//
//    @Override
//    public String visitParenthesizedExpr(ParenthesizedExprContext ctx) {
//        return String.format("(%s)", visit(ctx.expression()));
//    }
//
//    @Override
//    public String visitNotExpr(NotExprContext ctx) {
//        return '!' + visit(ctx.expression());
//    }
//
//    @Override
//    public String visitBoolExpr(BoolExprContext ctx) {
//        return ctx.getText().toLowerCase();
//    }
//
//    @Override
//    public String visitStringExpr(StringExprContext ctx) {
//        return String.format("\"%s\"", ctx.getText().substring(1, ctx.getText().length() - 1)).replace("\\'", "'");
//    }
//
//    @Override
//    public String visitFStringExpr(FStringExprContext ctx) {
//        var st = new ArrayList<>(){{
//            add(ctx.fstring().FSTRING_OPENING());
//            addAll(ctx.fstring().FSTRING_MIDDLE());
//            add(ctx.fstring().FSTRING_CLOSING());
//        }}.stream().map(Object::toString).toList();
//
//        String expressions = String.join(", ", ctx.fstring().expression().stream().map(this::visit).toList());
//
//        return "String.format(" + String.join("", st).replace("{}", "%s") + (!expressions.isEmpty() ? ", "  + expressions : "") + ")";
//    }
//
//    @Override
//    public String visitCharExpr(CharExprContext ctx) {
//        return ctx.getText();
//    }
//
//    @Override
//    public String visitVoidBlockStatement(VoidBlockStatementContext ctx) {
//        return visitBlock(ctx.voidBlock().voidLines());
//    }
//
//    @Override
//    public String visitVoidBlock(VoidBlockContext ctx) {
//        return visitBlock(ctx.voidLines());
//    }
//
//    @Override
//    public String visitClassBlock(ClassBlockContext ctx) {
//        return "{" + visit(ctx.classLines()) + "\n" + "}";
//    }
//
//    @Override
//    public String visitClassLines(ClassLinesContext ctx) {
//        var program = getProgram(ctx);
//
//        StringBuilder blockCode = new StringBuilder();
//
//        var lines = new ArrayList<>(ctx.children);
//
//        var otherLines = new ArrayList<ParseTree>();
//        for (var line : lines) {
//            if (line instanceof DeclarationContext declaration) {
//                processDeclaration(declaration, program);
//            } else {
//                otherLines.add(line);
//            }
//        }
//
//        for (var line : otherLines) {
//            var s = visit(line);
//
//            for (var assertion : pendingAssertions) {
//                blockCode.append("\n").append(assertion);
//            }
//            pendingAssertions.clear();
//
//            if (line != null && !(line instanceof TerminatorContext)) {
//                blockCode.append("\n").append(s);
//            }
//        }
//
//        return blockCode.toString();
//    }
//
//    @Override
//    public String visitReturnBlock(ReturnBlockContext ctx) {
//        return visitBlock(ctx.expressionLines());
//    }
//
//    @Override
//    public String visitExpressionReturn(ExpressionReturnContext ctx) {
//        var program = getProgram(ctx);
//        var statementBuilder = new StringBuilder();
//
//        var expression = visit(ctx.expression());
//
//        for (var assertion : pendingAssertions) {
//            statementBuilder.append("\n").append(assertion);
//        }
//        pendingAssertions.clear();
//
//        statementBuilder.append(String.format("return %s;", expression));
//
//        return statementBuilder.toString();
//    }
//
//    public String visitBlock(ParseTree lines) {
//        if (lines != null) {
//            return "{" + visitLines(lines) + "}";
//        }
//        return "{}";
//    }
//
//    public String visitLines(ParseTree lines) {
//        var program = getProgram(lines);
//
//        StringBuilder blockCode = new StringBuilder();
//        for (int i = 0; i < lines.getChildCount(); i++) {
//            var line = lines.getChild(i);
//            if (line != null && !(line instanceof TerminatorContext)) {
//
//                if (isDeclaration(line)) {
//                    program.javaCode.HashDeclaration(line, simplifyDeclaration(line));
//                }
//
//                var l = visit(line);
//
//                for (var assertion : pendingAssertions) {
//                    blockCode.append("\n").append(assertion);
//                }
//                pendingAssertions.clear();
//
//                blockCode.append("\n").append(l);
//            }
//        }
//
//        blockCode.append("\n");
//        return blockCode.toString();
//    }
//
//    @Override
//    public String visitVoidLines(VoidLinesContext ctx) {
//        return visitLines(ctx);
//    }
//
//    @Override
//    public String visitExpressionLines(ExpressionLinesContext ctx) {
//        return visitLines(ctx);
//    }
//
//    @Override
//    public String visitIfStatement(IfStatementContext ctx) {
//        StringBuilder builderElseifString = new StringBuilder();
//        for (int i = 1; i < ctx.expression().size(); i++) {
//            builderElseifString.append(String.format("else if (%s) %s", visit(ctx.expression(i)), visit(ctx.block(i))));
//        }
//        String elseString = "";
//        if (ctx.else_ != null) {
//            elseString = String.format("else %s", visit(ctx.block(ctx.block().size() - 1)));
//        }
//        return String.format("if (%s) %s%s%s", visit(ctx.expression(0)), visit(ctx.block(0)), builderElseifString, elseString);
//    }
//
//    @Override
//    public String visitForStatement(ForStatementContext ctx) {
//        return String.format("for (%s; %s; %s) %s", visit(ctx.localVarDecl()), visit(ctx.expression()), visit(ctx.assignmentStat()), visit(ctx.block()));
//    }
//
//    @Override
//    public String visitForeachStatement(ForeachStatementContext ctx) {
//        var program = getProgram(ctx);
//
//        var collection = ctx.expression();
//
//        var type = getAutoType(collection);
//
//        List<String> idList;
//        if (ctx.packedId().idList() != null) {
//            idList = ctx.packedId().idList().ID().stream().map(ParseTree::getText).toList();
//        }
//        else {
//            idList = List.of(ctx.packedId().ID().getText());
//        }
//
//        if (type.is("dict") && idList.size() > 2) {
//            throw new UnsupportedOperationException("Cannot map dictionaries to any amount of values other than 2.");
//        }
//
//        StringBuilder statementBuilder = new StringBuilder();
//
//        var collectionValue = visit(collection);
//
//        for (var assertion : pendingAssertions) {
//            statementBuilder.append("\n").append(assertion);
//        }
//        pendingAssertions.clear();
//
//        statementBuilder.append("for (var ");
//
//        String splitListName = String.format("_%s_%s", String.join("", idList), collection.hashCode());
//
//        statementBuilder.append(idList.size() == 2 ? splitListName : idList.getFirst()).append(" : ").append(collectionValue).append(type.is("dict") ? ".entrySet()" : "").append(") {");
//
//        if (type.is("dict") && idList.size() == 2) {
//            statementBuilder.append("\n");
//
//            int i = 0;
//            for (var id : idList) {
//                statementBuilder.append(String.format("%s %s = %s", type.subtypes.get(i), id, splitListName));
//
//                if (i == 0)
//                    statementBuilder.append(".getKey()");
//                else
//                    statementBuilder.append(".getValue()");
//                if (i < idList.size() - 1) statementBuilder.append("; ");
//
//                i++;
//            }
//            statementBuilder.append(";");
//        }
//        else if (!type.is("dict") && idList.size() > 1) {
//            statementBuilder.append("\n");
//
//            int i = 0;
//            for (var id : idList) {
//                statementBuilder.append(String.format("%s %s = %s", type.getJavaType(), id, splitListName));
//
//                if (i != 0)
//                    statementBuilder.append(String.format(".get(%s %s %s.size())", i, "%", splitListName));
//                else
//                    statementBuilder.append(".getFirst()");
//                if (i < idList.size() - 1) statementBuilder.append("; ");
//
//                i++;
//            }
//            statementBuilder.append(";");
//        }
//
//        var block = ctx.block();
//
//        String blockLines = "";
//        if (block instanceof VoidBlockOptionContext bl) {
//            blockLines = visit(bl.voidBlock().voidLines());
//        }
//        else if (block instanceof ReturnBlockOptionContext bl) {
//            blockLines = visit(bl.returnBlock().expressionLines());
//        }
//
//        statementBuilder.append("\n").append(blockLines);
//
//        statementBuilder.append("\n").append("}");
//
//        return statementBuilder.toString();
//    }
//
//    public ParseTree getDeclarationValue(Declaration declaration) {
//        ParseTree ctx = declaration.declaration;
//        if (ctx instanceof VarDeclContext decl) {
//            if (decl.localVarDecl() instanceof LooselyTypedLocalVarContext d) {
//                return d.expression();
//            }
//            else if (decl.localVarDecl() instanceof StrictlyTypedLocalVarContext d) {
//                return d.expression();
//            }
//        }
//        else if (ctx instanceof LooselyTypedLocalVarContext decl) {
//            return decl.expression();
//        }
//        else if (ctx instanceof StrictlyTypedLocalVarContext decl) {
//            return decl.expression();
//        }
//        print(ctx.getClass());
//        throw new RuntimeException("Unsupported declaration value unpacking: " + declaration);
//    }
//
//    public ParseTree getNode(ParseTree ctx) {
//        if (ctx instanceof IdExprContext expr) {
//            return getDeclarationValue(getDeclaration(expr.qualifiedId().getText(), ctx));
//        } else if (ctx instanceof PackedIdContext expr) {
//            return getDeclarationValue(getDeclaration(expr.getText(), ctx));
//        }
//        return ctx;
//    }
//
//    @Override
//    public String visitPackedId(PackedIdContext ctx) {
//        return ctx.getText();
//    }
//
//    @Override
//    public String visit(ParseTree ctx) {
////        print("Visiting", ctx.getClass().getSimpleName(), ctx.getText().substring(0, Math.min(20, ctx.getText().length())));
//        if (ctx != null) {
//            if (ctx instanceof TerminalNodeImpl terminalNode) return terminalNode.getText();
//
//            return super.visit(ctx);
//        }
//        return "";
//    }
//
//    @Override
//    public String visitRunnableFunctionDecl(RunnableFunctionDeclContext ctx) {
//        getProgram(ctx).javaCode.HashDeclaration(ctx, simplifyDeclaration(ctx));
//        return visitFunction(ctx.annotation(),
//                ctm.of("void"),
//                ctx.ID().getText(),
//                ctx.formalParameters(),
//                ctx.voidBlock(),
//                ctx.functionModifiers()
//        );
//    }
//
//    @Override
//    public String visitConsumerFunctionDecl(ConsumerFunctionDeclContext ctx) {
//        getProgram(ctx).javaCode.HashDeclaration(ctx, simplifyDeclaration(ctx));
//        return visitFunction(ctx.annotation(),
//                ctm.of(visit(ctx.type())),
//                ctx.ID().getText(),
//                ctx.formalParameters(),
//                ctx.returnBlock(),
//                ctx.functionModifiers()
//        );
//    }
//
//    @Override
//    public String visitVarFunctionDecl(VarFunctionDeclContext ctx) {
//        getProgram(ctx).javaCode.HashDeclaration(ctx, simplifyDeclaration(ctx));
//        return visitFunction(ctx.annotation(),
//                getAutoType(ctx),
//                ctx.ID().getText(),
//                ctx.formalParameters(),
//                ctx.returnBlock(),
//                ctx.functionModifiers()
//        );
//    }
//
//    public String visitFunction(List<AnnotationContext> annotations, ComplexType returnType, String functionName, FormalParametersContext formalParameters, ParseTree block, FunctionModifiersContext funcModifiers) {
//
//        String parameters = "";
//        if (formalParameters != null) {
//            parameters = formalParameters.formalParameter().stream()
//                    .map(this::visitFormalParameter)
//                    .collect(Collectors.joining(", "));
//        }
//
//        String body = visit(block);
//
//        String functionModifiers = parseFunctionModifiers(funcModifiers);
//
//        return String.format("%s%s%s %s(%s) %s", annotations != null ? String.join("\n", annotations.stream().map(this::visit).toList()) + "\n" : "", functionModifiers, returnType.getJavaType(), functionName, parameters, body);
//    }
//
//    public String parseFunctionModifiers(FunctionModifiersContext ctx) {
//        StringBuilder builder = new StringBuilder();
//
//        if (ctx.accessModifier() != null) {
//            builder.append(ctx.accessModifier().getText()).append(" ");
//        }
//        if (ctx.implementationModifier() != null) {
//            builder.append(ctx.implementationModifier().getText()).append(" ");
//        }
//        if (ctx.finalMd() != null) {
//            builder.append(ctx.finalMd().getText()).append(" ");
//        }
//        if (ctx.transientMd() != null) {
//            builder.append(ctx.transientMd().getText()).append(" ");
//        }
//        if (ctx.volatileMd() != null) {
//            builder.append(ctx.volatileMd().getText()).append(" ");
//        }
//        if (ctx.synchronizedMd() != null) {
//            builder.append(ctx.synchronizedMd().getText()).append(" ");
//        }
//        if (ctx.nativeMd() != null) {
//            builder.append(ctx.nativeMd().getText()).append(" ");
//        }
//        if (ctx.strictfpMd() != null) {
//            builder.append(ctx.strictfpMd().getText()).append(" ");
//        }
//        return builder.toString();
//    }
//
//    @Override
//    public String visitFunctionCallExpr(FunctionCallExprContext ctx) {
//        String functionName = ctx.ID().getText();
//
//        if (typeTree.containsKey(functionName)) {
//            for (var imp : typeTree.get(functionName).imports) {
//                ensureImport(ctx, imp);
//            }
//        }
//
//        String localClassString = "";
//
//        var declaration = getDeclaration(functionName, ctx);
//
//        if (declaration != null) {
//
//            var parent1 = firstNonNull(
//                    getToClosestParent(declaration.declaration, RunnableFunctionDeclContext.class),
//                    getToClosestParent(declaration.declaration, ConsumerFunctionDeclContext.class));
//            var parent2 = firstNonNull(
//                    getToClosestParent(ctx, RunnableFunctionDeclContext.class),
//                    getToClosestParent(ctx, ConsumerFunctionDeclContext.class));
//            if (parent1 != null && parent2 != null) {
//                if (declaration.id.equals(ctx.ID().getText()) && parent1.equals(parent2)) {
//                    localClassString = String.format("_class_%s$%s.", declaration.id, declaration.declaration.hashCode());
//                }
//            }
//        }
//
//        String args = "";
//        if (ctx.kwargs() != null) {
//            if (ctx.kwargs().expression() != null && !ctx.kwargs().expression().isEmpty()) {
//                args = ctx.kwargs().expression().stream()
//                        .map(this::visit)
//                        .collect(Collectors.joining(", "));
//            }
//            else {
//                args = ctx.kwargs().kwarg().stream()
//                        .map(this::visit)
//                        .collect(Collectors.joining(", "));
//            }
//
//        }
//        var candidate = functionName + "(" + args + ")";
//
//        KnownFunction knownFunc = typeTree.get(functionName);
//
//        return localClassString + (knownFunc != null ? knownFunc.mappingFunction.apply(candidate) : candidate);
//    }
//
//    @Override
//    public String visitKwarg(KwargContext ctx) {
//        return String.format("%s=%s", ctx.ID().getText(), visit(ctx.expression()));
//    }
//
//    public String visitFormalParameter(FormalParameterContext ctx) {
//        var type = visit(ctx.type());
//        var id = ctx.packedId().getText();
//
//        return type + " " + id;
//    }
//
//    @Override
//    public String visitVoidReturnStatement(VoidReturnStatementContext ctx) {
//        return "return;";
//    }
//
//    @Override
//    public String visitUnaryAssigmnent(UnaryAssigmnentContext ctx) {
//        return String.format("%s%s", visit(ctx.as), ctx.operator.getText());
//    }
//
//    @Override
//    public String visitExpressionReturnStatement(ExpressionReturnStatementContext ctx) {
//        var program = getProgram(ctx);
//        var statementBuilder = new StringBuilder();
//
//        var expression = visit(ctx.expressionReturn().expression());
//
//        for (var assertion : pendingAssertions) {
//            statementBuilder.append("\n").append(assertion);
//        }
//        pendingAssertions.clear();
//
//        statementBuilder.append(String.format("return %s;", expression));
//
//        return statementBuilder.toString();
//    }
//
//    @Override
//    public String visitFunctionDeclStatement(FunctionDeclStatementContext ctx) {
//        var program = getProgram(ctx);
//
//        var functionDecl = ctx.functionDecl();
//        var declaration = simplifyDeclaration(functionDecl);
//
//        program.javaCode.HashDeclaration(ctx, declaration);
//
//        String uniqueId = String.format("%s$%s", declaration.id, functionDecl.hashCode());
//        String text = String.format("class _Class_%s {%s\n}\nvar _class_%s = new _Class_%s();", uniqueId, "\n" + visit(ctx.functionDecl()), uniqueId, uniqueId);
//
//        return text;
//    }
//
//
//
//    @Override
//    public String visitAssignmentStatement(AssignmentStatementContext ctx) {
//        var assignment = getAssignment(ctx);
//
//        if (assignment != null) {
//            if (assignment.assignmentType.equals("default")) {
//                String idString = visit(assignment.expressionContext);
//
//                return String.format("%s %s %s;", idString, assignment.operator, visit(assignment.expression));
//            } else if (assignment.assignmentType.equals("exp")) {
//                String idString = visit(assignment.expressionContext);
//
//                return String.format("%s = %s;", idString, visitExp(assignment.expressionContext, assignment.expression, ctx));
//            } else if (assignment.assignmentType.equals("floorDiv")) {
//                String idString = visit(assignment.expressionContext);
//
//                return String.format("%s = %s;", idString, visitExp(assignment.expressionContext, assignment.expression, ctx));
//            }
//        }
//        return "";
//    }
//
//    public Assignment getAssignment(AssignmentStatContext ctx) {
//        if (ctx instanceof DefaultAssigmnentContext assignment) {
//            return new Assignment(assignment.as, assignment.value, assignment.operator, "default");
//        } else if (ctx instanceof ExpAssigmnentContext assignment) {
//            return new Assignment(assignment.as, assignment.value, assignment.operator, "exp");
//        } else if (ctx instanceof FloorDivAssigmnentContext assignment) {
//            return new Assignment(assignment.as, assignment.value, assignment.operator, "floorDiv");
//        } else if (ctx instanceof CeilDivAssigmnentContext assignment) {
//            return new Assignment(assignment.as, assignment.value, assignment.operator, "ceilDiv");
//        }
//        return null;
//    }
//
//    public Assignment getAssignment(AssignmentStatementContext ctx) {
//        return getAssignment(ctx.assignmentStat());
//    }
//
//    public class Assignment {
//        public ExpressionContext expressionContext;
//        public String assignmentType;
//
//        public ExpressionContext expression;
//        public String operator;
//
//        public Assignment(ExpressionContext expressionContext, ExpressionContext expression, Token operator, String assignmentType) {
//            this.expressionContext = expressionContext;
//            this.assignmentType = assignmentType;
//            this.expression = expression;
//            this.operator = operator.getText();
//        }
//    }
//
//    @Override
//    public String visitExpressionStatement(ExpressionStatementContext ctx) {
//        return visit(ctx.expression()) + ";";
//    }
//
//    @Override
//    public String visitVarDecl(VarDeclContext ctx) {
//        String variableModifiers = parseVariableModifiers(ctx.variableModifiers());
//        return visitVar(ctx.localVarDecl(), variableModifiers, ctx) + ";\n";
//    }
//
//    @Override
//    public String visitLooselyTypedLocalVar(LooselyTypedLocalVarContext ctx) {
//        return visitVar(ctx, "", ctx);
//    }
//
//    @Override
//    public String visitStrictlyTypedLocalVar(StrictlyTypedLocalVarContext ctx) {
//        return visitVar(ctx, "", ctx);
//    }
//
//    private List<AbstractMap.SimpleEntry<ParseTree, String>> varList = new ArrayList<>();
//    public String visitVar(LocalVarDeclContext ctx, String variableModifiers, ParseTree declCtx) {
//        var localType = simplifyLocalVarType(ctx);
//
//        ComplexType type = localType.type;
//        String Id = localType.id;
//
//        List<String> idList = Id.contains(",") ? List.of(Id.split(",")) : List.of(Id);
//
//        if (type.is("dict") && idList.size() > 2) {
//            throw new UnsupportedOperationException("Cannot map dictionaries to any amount of values other than 2.");
//        }
//
//        StringBuilder statementBuilder = new StringBuilder();
//
//        boolean declaresType;
//        ExpressionContext expression = null;
//        if (ctx instanceof StrictlyTypedLocalVarContext localCtx) {
//            expression = localCtx.expression();
//            declaresType = true;
//        } else if (ctx instanceof LooselyTypedLocalVarContext localCtx) {
//            expression = localCtx.expression();
//            declaresType = localCtx.var != null;
//        } else {
//            declaresType = false;
//        }
//
//        String splitListName = String.format("_%s_%s", String.join("", idList), expression != null ? expression.hashCode() : "");
//        if (idList.size() > 1) {
//            String expressionValue = visit(expression);
//            pendingAssertions.add(String.format("List<%s> %s = %s;", type.getJavaClassType(), splitListName, expressionValue));
//        }
//
//        for (int i = 0; i < idList.size(); i++) {
//            var id = idList.get(i);
//
//            var declaration = getDeclaration(id, ctx);
//
//            String javaType = type.getJavaType() + " ";
//            if (varList.stream().anyMatch((s) -> s.getValue().equals(id) && (s.getKey().equals(declaration.scope) || !declaresType))) {
//                javaType = "";
//            }
//            else {
//                varList.add(new AbstractMap.SimpleEntry<>(declaration.scope, id));
//            }
//
//            if (expression != null) {
//
//                String expressionValue = idList.size() > 1 ? splitListName : visit(expression);
//
//                if (type.is("var")) {
//                    type = getAutoType(expression);
//                }
//                if (type.is("unknown")) {
//                    type = ctm.of("var"); // At least try to capture the type using native java var handling
//                }
//
//                for (var assertion : pendingAssertions) {
//                    statementBuilder.append("\n").append(assertion);
//                }
//                pendingAssertions.clear();
//
//                statementBuilder.append(String.format("%s%s%s = %s", variableModifiers, javaType, id, expressionValue));
//
//            } else {
//                for (var assertion : pendingAssertions) {
//                    statementBuilder.append("\n").append(assertion);
//                }
//                pendingAssertions.clear();
//
//                statementBuilder.append(String.format("%s%s%s", variableModifiers, javaType, id));
//            }
//
//            if (idList.size() > 1) {
//                if (i != 0)
//                    statementBuilder.append(String.format(".get(%s %s %s.size())", i, "%", splitListName));
//                else
//                    statementBuilder.append(".getFirst()");
//                if (i < idList.size() - 1) statementBuilder.append("; ");
//            }
//        }
//
//        return statementBuilder.toString();
//    }
//
//    private static final HashMap<List<Object>, ParseTree> getToClosestParentCache = new HashMap<>();
//    public static <T extends ParseTree> T getToClosestParent(ParseTree ctx, Class<T> targetType) {
//        var key = List.of(ctx, targetType);
//        if (!getToClosestParentCache.containsKey(key)) {
//            ParseTree parent = ctx.getParent();
//
//            if (parent != null) {
//                if (targetType.isInstance(parent)) {
//                    var cast = targetType.cast(parent);
//                    getToClosestParentCache.put(key, cast);
//
//                    return cast;
//                } else {
//                    return getToClosestParent(parent, targetType);
//                }
//            }
//
//            return null;
//        }
//
//        return (T) getToClosestParentCache.get(key);
//    }
//
//    private static final HashMap<List<Object>, List<ParseTree>> getToClosestParentsOfTypesCache = new HashMap<>();
//    public static List<ParseTree> getToClosestParentsOfTypes(ParseTree ctx, Class<? extends ParseTree>... targetTypes) {
//        class ClosestParentsOfTypes {
//            public final List<ParseTree> parents = new ArrayList<>();
//
//            public List<ParseTree> getToClosestParentsOfTypes(ParseTree ctx, Class<? extends ParseTree>... targetTypes) {
//                var key = List.of(ctx, targetTypes);
//                if (!getToClosestParentsOfTypesCache.containsKey(key)) {
//                    ParseTree parent = ctx.getParent();
//
//                    if (parent != null) {
//                        for (var type : targetTypes) {
//                            if (type.isInstance(parent)) {
//                                var cast = type.cast(parent);
//
//                                parents.add(cast);
//
//                                getToClosestParentsOfTypesCache.put(key, parents);
//                            }
//                        }
//
//                        getToClosestParentsOfTypes(parent, targetTypes);
//                        return parents;
//                    }
//
//                    return parents;
//                }
//
//                return getToClosestParentsOfTypesCache.get(key);
//            }
//        }
//
//        return new ClosestParentsOfTypes().getToClosestParentsOfTypes(ctx, targetTypes);
//    }
//
//    private static final HashMap<List<Object>, ParseTree> getToClosestSiblingCache = new HashMap<>();
//    public static <T extends ParseTree> T getToClosestSibling(ParseTree ctx, Class<T> targetType) {
//        var key = List.of(ctx, targetType);
//        if (!getToClosestSiblingCache.containsKey(key)) {
//            ParseTree parent = ctx.getParent();
//
//            if (parent != null) {
//                for (int i = 0; i < parent.getChildCount(); i++) {
//                    var sibling = parent.getChild(i);
//                    if (targetType.isInstance(sibling)) {
//                        var cast = targetType.cast(sibling);
//                        getToClosestSiblingCache.put(key, cast);
//
//                        return cast;
//                    }
//                }
//                return getToClosestSibling(parent, targetType);
//            }
//
//            return null;
//        }
//
//        return (T) getToClosestSiblingCache.get(key);
//    }
//
//    private static final HashMap<List<Object>, List<? extends ParseTree>> getToSiblingsOfTypeCache = new HashMap<>();
//    public static <T extends ParseTree> List<T> getToSiblingsOfType(ParseTree ctx, Class<T> targetType) {
//        var key = List.of(ctx, targetType);
//        if (!getToSiblingsOfTypeCache.containsKey(key)) {
//            ParseTree parent = ctx.getParent();
//            List<T> siblings = new ArrayList<>();
//
//            if (parent != null) {
//                boolean found = false;
//                for (int i = 0; i < parent.getChildCount(); i++) {
//                    var sibling = parent.getChild(i);
//                    if (targetType.isInstance(sibling)) {
//                        found = true;
//                        siblings.add(targetType.cast(sibling));
//                    }
//                }
//                if (!found) {
//                    for (var sibling : getToSiblingsOfType(parent, targetType)) {
//                        siblings.add(targetType.cast(sibling));
//                    }
//                }
//            }
//
//            getToSiblingsOfTypeCache.put(key, siblings);
//
//            return siblings;
//        }
//        return (List<T>) getToSiblingsOfTypeCache.get(key);
//    }
//
//    public String parseVariableModifiers(VariableModifiersContext ctx) {
//        StringBuilder builder = new StringBuilder();
//
//        if (ctx.accessModifier() != null) {
//            builder.append(ctx.accessModifier().getText()).append(" ");
//        }
//        if (ctx.staticMd() != null) {
//            builder.append(ctx.staticMd().getText()).append(" ");
//        }
//        if (ctx.finalMd() != null) {
//            builder.append(ctx.finalMd().getText()).append(" ");
//        }
//        if (ctx.transientMd() != null) {
//            builder.append(ctx.transientMd().getText()).append(" ");
//        }
//        if (ctx.volatileMd() != null) {
//            builder.append(ctx.volatileMd().getText()).append(" ");
//        }
//        return builder.toString();
//    }
//
//    @Override
//    public String visitPrecompile(PrecompileContext ctx) {
//        return super.visitPrecompile(ctx);
//    }
//
//    @Override
//    public String visitLambdaExpr(LambdaExprContext ctx) {
//        return String.format("(%s) -> %s", ctx.idList().getText(), visit(ctx.expression()));
//    }
//
//    @Override
//    public String visitAnnotation(AnnotationContext ctx) {
//        return String.format("@%s", visit(ctx.expression()));
//    }
//}