package net.norivensuu.flux.compiler;

import com.github.bogdanovmn.jaclin.CLI;
import net.norivensuu.flux.ir.ProgramIrNode;
import net.norivensuu.flux.visitor.IrGeneratorVisitor;
import net.norivensuu.flux.antlr.FluxLexer;
import net.norivensuu.flux.antlr.FluxParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

// TODO Write a custom error handler for Flux code specifically (still expose the Java one)
public class FluxCompiler {
    private static Map<Path, FluxMetadata> metadataRegistry = new HashMap<>();
    private static Map<FluxParser.ProgramContext, Program> programRegistry = new HashMap<>();

    public static Map<FluxParser.ProgramContext, Program> getProgramRegistry() {
        return programRegistry;
    }

    public static class Program {
        public Path fileName;
        public String packageString;
        public Set<String> imports = new HashSet<>();
        public FluxParser.ProgramContext ctx;
        public ProgramIrNode node;
        public FluxIr<?> ir;
//        public JavaCodeGeneratorVisitor.JavaCode javaCode;
        public boolean precompile = false;

        public Program(Path fileName, Path packagePath, FluxParser.ProgramContext ctx) {
            this.fileName = fileName;
            this.packageString = packagePath.toString().substring(0, packagePath.toString().lastIndexOf("\\")).replace("\\", "/");
            this.ctx = ctx;
        }

        public Program(Path fileName, Path packagePath, FluxParser.ProgramContext ctx, boolean precompile) {
            this.fileName = fileName;
            this.packageString = packagePath.toString().substring(0, packagePath.toString().lastIndexOf("\\")).replace("\\", "/");
            this.ctx = ctx;
            this.precompile = precompile;
        }

        public FluxParser.DeclarationContext addImport(String importPath) {
            return FluxCompiler.addImport(importPath, ctx);
        }
    }

    static void main(String[] args) throws Exception {
        new CLI("flux-compiler", "A Flux programming language to Java compiler.")

                .withOptions()
                    .strArg("project", "A .flux source project folder to recursively compile.")
                        .requires("output")
                    .strArg("flux", "A .flux source file to compile.")
                    .strArg("output", "An output .jar file or compiled project path.")
                    .flag("verbose", "Enables Flux verbose compilation mode.")
                .flag("enable-precompile", "Currently not working.")
                .withRestrictions()
                .atLeastOneShouldBeUsed("project", "flux")

                .withEntryPoint(
                        options -> {
                            if (options.has("enable-precompile")) {
                                System.out.println("Precompile is currently unavailable! Please do not use this option.");
                            }
                            else if (options.has("project")) {
                                Path projectPath = Path.of(options.get("project"));
                                Path metadata = projectPath.resolve("inherited.flux");
                                Path outputPath = Path.of(options.get("output"));

                                System.out.println("Starting compilation of project: " + projectPath);

                                if (Files.exists(metadata))
                                    loadOrCreateMetadata(metadata);
                                else
                                    metadataRegistry.put(projectPath, new FluxMetadata().appendDefault());

                                try (Stream<Path> paths = Files.walk(projectPath)) {
                                    var pathsList = paths.toList();
                                    if (pathsList.stream().noneMatch((s) -> s.toString().endsWith(".flux") || s.toString().endsWith(".flux.meta"))) {
                                        throw new FileNotFoundException("No .flux file found in the project folder. Are you sure you're using the right folder?");
                                    }

                                    pathsList.stream().filter(Files::isRegularFile)
                                            .forEach(p -> {
                                                String fileName = p.toString();

                                                if (fileName.endsWith(".flux")) {
                                                    compileFile(projectPath, p, outputPath, options.has("enable-precompile"), options.has("verbose"));
                                                } else if (fileName.endsWith(".flux.meta")) {
                                                    FluxMetadata defaultMeta = new FluxMetadata().appendDefault();
                                                    metadataRegistry.put(p, defaultMeta);
                                                }
                                            });
                                }
                            }
                            else if (options.has("flux")) {
                                Path filePath = Path.of(options.get("flux"));
                                var str = filePath.toAbsolutePath().toString();
                                Path outputPath = options.has("output") ? Path.of(options.get("output")) : filePath.resolveSibling(str.substring(0, str.lastIndexOf(".")) + ".java");
                                compileFile(filePath, filePath, outputPath, options.has("enable-precompile"), options.has("verbose"));
                            }
                        }
                )

                .run(args);
    }

    private static void compileFile(Path originalPath, Path filePath, Path outputPath, boolean enablePrecompile) {
        compileFile(originalPath, filePath, outputPath, enablePrecompile, true);
    }

    private static void compileFile(Path originalPath, Path filePath, Path outputPath, boolean enablePrecompile, boolean verbose) {
        if (verbose)
            System.out.println("\n--- Compiling: " + filePath.getFileName() + " ---");
        try {
            FluxMetadata metadata = buildClosestMetadata(originalPath, filePath.getParent());

            var fullPackageFileStr = originalPath.relativize(filePath).toString().replace("\\", ".");

            var relativePackageFileStr = !fullPackageFileStr.isEmpty() ? fullPackageFileStr.substring(0, fullPackageFileStr.lastIndexOf(".")) : "";

            InputStream input = new FileInputStream(filePath.toFile());
            CharStream cs = CharStreams.fromStream(input);

            FluxLexer lexer = new FluxLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            FluxParser parser = new FluxParser(tokens);

            var program = new Program(filePath.getFileName(), originalPath.relativize(filePath), parser.program());

            FluxParser.ProgramContext tree = getProgramContext(program, metadata);

            if (verbose)
                System.out.println("Successfully parsed and enriched " + filePath.getFileName());

            String irFilename = filePath.getFileName().toString().replace(".flux", ".ir");
            Path relativePath = originalPath.relativize(filePath);

            Path relativeParent = relativePath.getParent();
            Path outputDirectory = (relativeParent != null)
                    ? outputPath.resolve(relativeParent)
                    : outputPath;

            Files.createDirectories(outputDirectory);

//            Path irPath = outputDirectory.resolve(irFilename);
            var bytecodeGenerator = new FluxBytecodeGenerator(new IrGeneratorVisitor(), program, tree/*, irPath.toFile()*/);

//            if (enablePrecompile) {
//
//                var precompile = tree.precompile();
//
//                if (precompile != null) {
//                    var precompileProgram = new FluxParser.ProgramContext(null, -1);
//                    var precompilePath = filePath.getParent().resolve("Precompile.java");
//                    var prog = new Program(Path.of("Precompile"), precompileProgram, true);
//
//                    tree.children = tree.children.stream().filter((s) -> !(s instanceof FluxParser.PrecompileContext)).toList();
//
//                    getProgramRegistry().put(precompileProgram, prog);
//
//                    for (var i : precompile.classBlock().classLines().children) {
//                        precompileProgram.addChild((RuleContext) i);
//                        i.setParent(precompileProgram);
//                    }
//
//                    var fullPackageFileStr = originalPath.relativize(precompilePath).toString().replace("\\", ".");
//                    var relativePackageFileStr = fullPackageFileStr.substring(0, fullPackageFileStr.lastIndexOf("."));
//                    String precompileString = "package " + relativePackageFileStr.substring(0, relativePackageFileStr.lastIndexOf(".")) + ";\n" + generator.visit(precompileProgram);
//
//                    if (verbose)
//                        System.out.printf("--- Executing: %s precompile ---\n", filePath.getFileName());
//
//                    var precompileFile = writeGeneratedJavaFile(originalPath, precompilePath, precompileString, outputPath);
//                    assert precompileFile != null;
//                    executePrecompile(precompileFile);
//                }
//            }

//            var packageName = !relativePackageFileStr.isEmpty() ? relativePackageFileStr.substring(0, relativePackageFileStr.lastIndexOf(".")) : "flux";

            String fileName = filePath.getFileName().toString().split("\\.")[0];
            for (var entry : bytecodeGenerator.generateClasses().entrySet()) {
                String name = fileName;
                if (!fileName.equals(entry.getKey()))
                    name += "$" + entry.getKey();

                writeGeneratedFile(name + ".class", originalPath, filePath, entry.getValue(), outputPath);
            }
            System.out.println(bytecodeGenerator);

        } catch (IOException e) {
            System.err.println("Error processing file: " + filePath);
            e.printStackTrace();
        }
    }

    private static FluxParser.ProgramContext getProgramContext(Program program, FluxMetadata metadata) {
        FluxParser.ProgramContext tree = program.ctx;

        programRegistry.put(tree, program);

        if (metadata != null && metadata.imports != null) {
            if (tree.children != null) {
                for (ParseTree child : tree.children) {
                    if (child instanceof FluxParser.ImportDeclContext decl) {
                        if (decl.qualifiedId() != null) {
                            program.imports.add(decl.qualifiedId().getText() + (decl.wildcard != null ? decl.wildcard.getText() : ".*"));
                        }
                    }
                }
            }

            for (String importPath : metadata.imports) {
                if (program.imports.contains(importPath)) {
                    continue;
                }

                addImport(importPath, tree);
            }
        }

        return tree;
    }

    public static FluxParser.DeclarationContext addImport(String importPath, FluxParser.ProgramContext tree) {
        var program = programRegistry.get(tree);

        FluxParser.DeclarationContext declarationContext = new FluxParser.DeclarationContext(tree, -1);
        FluxParser.ImportDeclContext syntheticImport = new FluxParser.ImportDeclContext(declarationContext);

        syntheticImport.addAnyChild(new TerminalNodeImpl(new CommonToken(FluxLexer.T__0, "import ")));

        FluxParser.QualifiedIdContext qualIdCtx = new FluxParser.QualifiedIdContext(syntheticImport, -1);
        qualIdCtx.addAnyChild(new TerminalNodeImpl(new CommonToken(FluxLexer.ID, importPath)));
        syntheticImport.addAnyChild(qualIdCtx);

        Token wildcardToken = new CommonToken(FluxLexer.WILDCARD, ".*");
        syntheticImport.addAnyChild(new TerminalNodeImpl(wildcardToken));

        FluxParser.TerminatorContext terminator = new FluxParser.TerminatorContext(declarationContext, -1);
        terminator.addAnyChild(new TerminalNodeImpl(new CommonToken(FluxLexer.TERMINATOR, ";")));

        if (tree.children == null) {
            tree.addAnyChild(syntheticImport);
        } else {
            tree.children.addFirst(syntheticImport);
        }
        syntheticImport.parent = tree;

        program.imports.add(importPath);

        return declarationContext;
    }

    private static FluxMetadata buildClosestMetadata(Path originalPath, Path directory) {
        Path current = directory;
        FluxMetadata accumulatedMetadata = null;

        while (current != null && current.startsWith(originalPath)) {
            FluxMetadata levelMetadata = metadataRegistry.get(current);

            if (levelMetadata == null) {
                levelMetadata = findMetadataInDirectory(current);
            }

            if (levelMetadata != null) {
                accumulatedMetadata = applyMetadata(accumulatedMetadata, directory, levelMetadata);

                if (levelMetadata.top) {
                    accumulatedMetadata.top = true;
                    break;
                }
            }

            if (current.equals(originalPath)) {
                break;
            }

            current = current.getParent();
        }

        if (accumulatedMetadata == null) {
            accumulatedMetadata = new FluxMetadata().appendDefault();
        }

        metadataRegistry.put(directory, accumulatedMetadata);
        return accumulatedMetadata;
    }

    private static FluxMetadata findMetadataInDirectory(Path dir) {
        try (var stream = Files.list(dir)) {
            Optional<Path> metaFile = stream
                    .filter(p -> p.toString().endsWith(".flux.meta"))
                    .findFirst();


            if (metaFile.isEmpty()) return null;

            Path p = metaFile.get();
            try {
                return FluxMetadata.fromJson(Files.readString(p));
            } catch (Exception e) {
                System.err.println("Invalid metadata at " + p + ". Replacing with default.");
                FluxMetadata freshMeta = new FluxMetadata().appendDefault();
                Files.writeString(p, freshMeta.toJson());
                return freshMeta;
            }
        } catch (IOException e) {
            return null;
        }
    }

    private static FluxMetadata applyMetadata(FluxMetadata accumulator, Path originalDir, FluxMetadata found) {
        if (accumulator == null)
            accumulator = new FluxMetadata();
        accumulator.imports.addAll(found.imports);
        metadataRegistry.put(originalDir, accumulator);
        return accumulator;
    }

    private static FluxMetadata loadOrCreateMetadata(Path fluxPath) throws IOException {
        return loadOrCreateMetadata(fluxPath, false);
    }
    private static FluxMetadata loadOrCreateMetadata(Path targetPath, boolean top) throws IOException {
        String pathStr = targetPath.toString();
        Path metaPath = pathStr.endsWith(".flux")
                ? Paths.get(pathStr + ".meta")
                : targetPath.resolve(".flux.meta");

        FluxMetadata meta;
        try {
            if (Files.exists(metaPath)) {
                meta = FluxMetadata.fromJson(Files.readString(metaPath));
            } else {
                meta = new FluxMetadata(top).appendDefault();
            }
        } catch (Exception e) {
            meta = new FluxMetadata(top).appendDefault();
        }

        Files.writeString(metaPath, meta.toJson());
        return meta;
    }

    private static File writeGeneratedFile(String fileName, Path originalPath, Path originalFluxPath, byte[] bytecode, Path outputRoot) {
        try {
            Path relativePath = originalPath.relativize(originalFluxPath);

            Path relativeParent = relativePath.getParent();
            Path outputDirectory = (relativeParent != null)
                    ? outputRoot.resolve(relativeParent)
                    : outputRoot;

            Files.createDirectories(outputDirectory);

            Path outputPath = outputDirectory.resolve(fileName);

            Files.write(outputPath, bytecode);

            System.out.println("Generated Class file: " + outputPath);

            return new File(outputPath.toUri());
        } catch (IOException e) {
            System.err.println("Error writing generated Java file: " + e.getMessage());
        }

        return null;
    }
}