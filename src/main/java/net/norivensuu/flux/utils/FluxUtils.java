package net.norivensuu.flux.utils;

import com.thoughtworks.qdox.JavaProjectBuilder;
import net.norivensuu.flux.antlr.FluxParser;
import one.util.streamex.StreamEx;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class FluxUtils {

    public static final class StandardFluxLibs {
        public static final String FLUX_UTILS = "net.norivensuu.flux.utils.FluxUtils.*";
        public static final String STATIC_FLUX_UTILS = "static " + FLUX_UTILS;

        public static final String MATH_UTILS = "net.norivensuu.flux.utils.MathUtils.*";
        public static final String STATIC_MATH_UTILS = "static " + MATH_UTILS;
    }

    public static List<Method> getAccessibleMethods(Path projectRoot, Class<?> clazz) {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.addSourceTree(new File( (projectRoot + "/" + clazz.getPackageName()).replace("\\", "/").replace(".", "/")  ));

        var methods = new ArrayList<Method>();

        for (var source : builder.getSources()) {
            List<String> imports = source.getImports();

            for (String imp : imports)
            {
                try {
                    var c = Class.forName(imp);
                    methods.addAll(Arrays.asList(getClassMethods(c)));
                } catch (ClassNotFoundException _) {}
            }
        }

        return methods;
    }

    // Source - https://stackoverflow.com/a/69383261
    // Posted by Oussama ZAGHDOUD, modified by community. See post 'Timeline' for change history
    // Retrieved 2026-06-12, License - CC BY-SA 4.0
    public static Method[] getClassMethods(Class<?> clazz) {
        List<Method> result = new ArrayList<>();
        while (clazz != null) {
            result.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            clazz = clazz.getSuperclass();
        }
        return result.toArray(new Method[0]);
    }

    public static class ComplexParameter {
        public String name;
        public Parameter parameter;

        public ComplexParameter(String name, Parameter parameter) {
            this.name = name;
            this.parameter = parameter;
        }

        @Override
        public String toString() {
            return String.format("ComplexParameter(%s <%s>)", name, parameter);
        }
    }

    public static HashMap<List<ComplexParameter>, Method> getFunctionParameterTypes(Path projectRoot, String functionName, Class<?> visibleFrom) {
        var methods = getAccessibleMethods(projectRoot, visibleFrom).stream().filter((s) -> s.getName().equals(functionName) && s.isAnnotationPresent(Kwargs.class)).toList();

        HashMap<List<ComplexParameter>, Method> map = new HashMap<>();
        for (var method : methods) {
            List<ComplexParameter> complexParameters = new ArrayList<>();

            var parameters = method.getParameters();

            int i = 0;
            for (var parameter : method.getAnnotation(Kwargs.class).value()) {
                complexParameters.add(new ComplexParameter(parameter, parameters[i]));

                i++;
            }

            map.put(complexParameters, method);
        }

        return map;
    }

    public static void Initialize() {

    }

    public static class KnownFunction {
        public String name;
        public Function<String, String> mappingFunction;
        public Function<String, ComplexType> typeFunction;
        public List<String> argumentTypes = new ArrayList<>();
        public List<String> imports = new ArrayList<>();

        public KnownFunction(String name, Function<String, String> mappingFunction, Function<String, ComplexType> typeFunction, List<String> argumentTypes) {
            this.name = name;
            this.mappingFunction = mappingFunction;
            this.typeFunction = typeFunction;
            this.argumentTypes = argumentTypes;
        }

        public KnownFunction(String name, Function<String, String> mappingFunction, Function<String, ComplexType> typeFunction, List<String> argumentTypes, List<String> imports) {
            this.name = name;
            this.mappingFunction = mappingFunction;
            this.typeFunction = typeFunction;
            this.argumentTypes = argumentTypes;
            this.imports = imports;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static TypeTreeElement typeTree = new TypeTreeElement("",
            null,
            new HashMap<>() {{
                put("map", new TypeTreeElement("map",
                        new KnownFunction("map",
                                (s) -> String.format("map(%s.class, %s)", convertFluxToClassType(s.substring(s.indexOf("(")+1, s.indexOf(","))), s.substring(s.indexOf(",")+1, s.lastIndexOf(")")).strip()),
                                (s) -> ctm.of(s.contains(",") ? convertClassToJavaType(s.substring(s.indexOf("(")+1, s.indexOf(",")).replace(".class", "")) : "invalid"),
                                null,
                                List.of(StandardFluxLibs.STATIC_FLUX_UTILS)
                        ),
                        new HashMap<>()));
                put("input", new TypeTreeElement("input",
                        new KnownFunction("input",
                                (s) -> s,
                                (s) -> ctm.of("string"),
                                null,
                                List.of(StandardFluxLibs.STATIC_FLUX_UTILS)
                        ),
                        new HashMap<>() {{
                            put("split", new TypeTreeElement("split",
                                    new KnownFunction("split",
                                            (s) -> !s.substring(s.indexOf("(")+1, s.lastIndexOf(")")).isEmpty() ? s : String.format("%s\" \"%s", s.substring(0, s.indexOf("(")+1), s.substring(s.lastIndexOf(")"))),
                                            (s) -> ctm.of("array","string"),
                                            List.of(""),
                                            List.of(StandardFluxLibs.STATIC_FLUX_UTILS)
                                    ),
                                    new HashMap<>()));
                        }}));
                put("abs", new TypeTreeElement("abs",
                        new KnownFunction("abs",
                                (s) -> s,
                                (s) -> ctm.of("double"),
                                null,
                                List.of(StandardFluxLibs.STATIC_MATH_UTILS)
                        ),
                        new HashMap<>()));
                put("print", new TypeTreeElement("print",
                        new KnownFunction("print",
                                (s) -> s,
                                (s) -> ctm.of("function"),
                                List.of("vararg"),
                                List.of(StandardFluxLibs.STATIC_FLUX_UTILS)
                        ),
                        new HashMap<>()));
                put("range", new TypeTreeElement("range",
                        new KnownFunction("range",
                                (s) -> s,
                                (s) -> ctm.of("list", "int"),
                                null,
                                List.of(StandardFluxLibs.STATIC_FLUX_UTILS)
                        ),
                        new HashMap<>()));
                put("approximately", new TypeTreeElement("approximately",
                        new KnownFunction("approximately",
                                (s) -> s,
                                (s) -> ctm.of("bool"),
                                null,
                                List.of(StandardFluxLibs.STATIC_FLUX_UTILS)
                        ),
                        new HashMap<>()));
                put("power", new TypeTreeElement("power",
                        new KnownFunction("power",
                                (s) -> s,
                                (s) -> ctm.of("double"),
                                null,
                                List.of(StandardFluxLibs.STATIC_FLUX_UTILS)
                        ),
                        new HashMap<>()));
                put("Integer", new TypeTreeElement("Integer",
                        null,
                        new HashMap<>() {{
                            put("getInteger", new TypeTreeElement("getInteger",
                                    new KnownFunction("getInteger",
                                            (s) -> s,
                                            (s) -> ctm.of("int"),
                                            null,
                                            List.of()
                                    ),
                                    new HashMap<>()));
                        }}));
            }});

    public static class TypeTreeElement {
        public Object ref;
        public KnownFunction function;
        public HashMap<Object, TypeTreeElement> children;

        public TypeTreeElement(Object ref, KnownFunction function, HashMap<Object, TypeTreeElement> children) {
            this.ref = ref;
            this.function = function;
            this.children = children;
        }

        public boolean containsKey(Object key) {
            return children.containsKey(key);
        }

        public List<ParseTree> buildAccessNodeList(FluxParser.VariableAccessExprContext acc) {
            List<ParseTree> nodeList = new ArrayList<>();
            if (acc.acc instanceof FluxParser.VariableAccessExprContext var) {
                nodeList.addAll(buildAccessNodeList(var));
            }

            if (acc.acc instanceof FluxParser.IdExprContext var) {
                nodeList.add(var);
            }
            else if (acc.acc instanceof FluxParser.FunctionCallExprContext var) {
                nodeList.add(var);
            }

            if (acc.variable instanceof FluxParser.IdExprContext var) {
                nodeList.add(var);
            }
            else if (acc.variable instanceof FluxParser.FunctionCallExprContext var) {
                nodeList.add(var);
            }

            return nodeList;
        }

        public List<Object> buildAccessIdList(FluxParser.VariableAccessExprContext acc) {
            List<Object> idList = new ArrayList<>();
            if (acc.acc instanceof FluxParser.VariableAccessExprContext var) {
                idList.addAll(buildAccessIdList(var));
            }

            if (acc.acc instanceof FluxParser.IdExprContext var) {
                idList.add(var.qualifiedId().getText());
            }
            else if (acc.acc instanceof FluxParser.FunctionCallExprContext var) {
                idList.add(var.ID().getText());
            }

            if (acc.variable instanceof FluxParser.IdExprContext var) {
                idList.add(var.qualifiedId().getText());
            }
            else if (acc.variable instanceof FluxParser.FunctionCallExprContext var) {
                idList.add(var.ID().getText());
            }

            return idList;
        }

        public KnownFunction get(FluxParser.VariableAccessExprContext variableAccessExpr) {
            var idList = buildAccessIdList(variableAccessExpr);

            return get(idList);
        }

        public KnownFunction get(Object first) {
            return get(new ArrayList<>() {{add(first);}});
        }

        public KnownFunction get(List<Object> idList) {
            if (idList.isEmpty()) {
                return function;
            }

            var first = idList.getFirst();
            if (children.containsKey(first))
                return children.get(first).get(idList.subList(1, idList.size()));

            return null;
        }

        public String map(FluxParser.VariableAccessExprContext variableAccessExpr) {
            var nodeList = buildAccessNodeList(variableAccessExpr);
            var idList = buildAccessIdList(variableAccessExpr);

            List<String> sb = new ArrayList<>();

            int lastCutId = 0;
            for (int i = 0; i < idList.size(); i++) {
                var cutIdList = idList.subList(0, i+1);
                var lastNode = nodeList.get(i);

                if (get(cutIdList) != null) {
                    sb.addAll(cutIdList.subList(lastCutId, cutIdList.size()-1).stream().map(Object::toString).toList());
                    sb.add(get(cutIdList).mappingFunction.apply(lastNode.getText()));
                    lastCutId = i;
                }
            }

            return String.join(".", sb);
        }

        public void add(String address, TypeTreeElement element) {
            add(Arrays.stream(address.split("\\.")).toList(), element);
        }

        public void add(List<String> address, TypeTreeElement element) {
            var lowestElement = this;
            for (var key : address) {
                lowestElement = children.get(key);
            }

            lowestElement.children.put(address.getLast(), element);
        }
    }

    public static ComplexTypeMap ctm = new ComplexTypeMap(List.of(
            new ComplexType("bool", "boolean", "Boolean", "Z"),
            new ComplexType("string", "String", "String", "Ljava/lang/String;"),
            new ComplexType("char", "char", "Character", "C"),
            new ComplexType("float", "float", "Float", "F"),
            new ComplexType("int", "int", "Integer", "I"),
            new ComplexType("double", "double", "Double", "D"),
            new ComplexType("long", "long", "Long", "J"),
            new ComplexType("object", "Object", "Object", "Ljava/lang/Object;"),

            new ComplexType("var", "var", null, null),
            new ComplexType("void", "void", null, "()V"),
            new ComplexType("function", "function", null, null),

            new ComplexType("illegal"),
            new ComplexType("unknown"),

            new ComplexType("import", "import", null, null),
            new ComplexType("importstatic", "static import", null, null),
            new ComplexType("import static", "static import", null, null),

            new ComplexType("class", "class", null, null),
            new ComplexType("library", null, null, null),

            new ComplexType("list", "List", "List", "Ljava/util/List;", 1),
            new ComplexType("array", "Array", "Array", "Ljava/lang/Object;", 1),

            new ComplexType("dict", "HashMap", "HashMap", "Ljava/util/HashMap;", 2)
    ));

    public static String convertFluxType(String type) {
        var t = ctm.getFromFlux(type);
        return t != null ? t.getJavaType() : type;
    }

    public static String convertClassToJavaType(String type) {
        var t = ctm.getFromJavaClass(type);
        return t != null ? t.getJavaType() : type;
    }

    public static String convertFluxToClassType(String type) {
        var t = ctm.getFromFlux(type);
        return t != null ? t.getJavaClassType() : type;
    }

    public static class ComplexType {
        private final String fluxType;
        private final String javaType;
        private final String javaClassType;
        private final String jvmType;
        public List<ComplexType> subtypes = List.of();
        public int subtypesCapacity = 0;

        public String getFluxType() {
            if (!is("array")) {
                return String.format("%s%s", fluxType, !subtypes.isEmpty() ? String.format("<%s>", String.join(", ", subtypes.stream().map(ComplexType::getFluxType).toList())) : "");
            }
            else {
                return subtypes.getFirst().getJavaClassType() + "[]";
            }
        }
        public String getJavaType() {
            if (!is("array")) {
                return String.format("%s%s", javaType, !subtypes.isEmpty() ? String.format("<%s>", String.join(", ", subtypes.stream().map(ComplexType::getJavaClassType).toList())) : "");
            }
            else {
                return subtypes.getFirst().getJavaClassType() + "[]";
            }
        }
        public String getJavaClassType() {
            if (!is("array")) {
                return String.format("%s%s", javaClassType, !subtypes.isEmpty() ? String.format("<%s>", String.join(", ", subtypes.stream().map(ComplexType::getJavaClassType).toList())) : "");
            }
            else {
                return subtypes.getFirst().getJavaClassType() + "[]";
            }
        }
        public String getJvmType() {
            if (!is("array")) {
                return jvmType;
            }
            else {
                return "[" + subtypes.getFirst().getJvmType();
            }
        }

        public ComplexType(ComplexType complexType, ComplexType... subtypes) {
            this.fluxType = complexType.fluxType;
            this.javaType = complexType.javaType;
            this.javaClassType = complexType.javaClassType;
            this.jvmType = complexType.jvmType;
            this.subtypes = new ArrayList<>(){{
                addAll(complexType.subtypes);
                addAll(Arrays.stream(subtypes).toList());
            }};
        }
        public ComplexType(ComplexType complexType, List<ComplexType> subtypes) {
            this.fluxType = complexType.fluxType;
            this.javaType = complexType.javaType;
            this.javaClassType = complexType.javaClassType;
            this.jvmType = complexType.jvmType;
            this.subtypes = new ArrayList<>(){{
                addAll(complexType.subtypes);
                addAll(subtypes);
            }};
        }

        public ComplexType(String uniType) {
            this.fluxType = uniType;
            this.javaType = uniType;
            this.javaClassType = uniType;
            this.jvmType = uniType;
        }
        public ComplexType(String fluxType, String javaType, String javaClassType, String jvmType) {
            this.fluxType = fluxType;
            this.javaType = javaType;
            this.javaClassType = javaClassType;
            this.jvmType = jvmType;
        }
        public ComplexType(String fluxType, String javaType, String javaClassType, String jvmType, int subtypesCapacity) {
            this.fluxType = fluxType;
            this.javaType = javaType;
            this.javaClassType = javaClassType;
            this.jvmType = jvmType;
            this.subtypesCapacity = subtypesCapacity;
        }

        public boolean is(String type) {
            return fluxType.equals(type);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ComplexType ct) {
                return ct.is(fluxType) && subtypes.equals(ct.subtypes);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public String toString() {
            return String.format("<FluxType %s>%s", fluxType, !subtypes.isEmpty() ? String.format("{%s}", subtypes) : "");
        }
    }

    public static class ComplexTypeMap {
        public List<ComplexType> types;

        private List<ComplexType> sortedTypes;

        public List<ComplexType> getSortedTypes() {
            if (sortedTypes == null) {
                sortedTypes = types.stream().sorted(Comparator.comparingInt(a -> a.subtypesCapacity)).toList();
            }
            return sortedTypes;
        }

        public ComplexTypeMap(List<ComplexType> types) {
            this.types = types;
        }

        public boolean containsKey(String key) {
            for (var type : getSortedTypes()) {
                if (Objects.equals(type.fluxType, key) || Objects.equals(type.javaType, key) || Objects.equals(type.javaClassType, key)) {
                    return true;
                }
            }
            return false;
        }

        public ComplexType get(String key, List<ComplexType> complexTypes) {
            return getFromAny(key, complexTypes);
        }
        public ComplexType get(String key, ComplexType... complexTypes) {
            return getFromAny(key, complexTypes);
        }

        public ComplexType getFromAny(String key, List<ComplexType> complexTypes) {
            if (key.contains("<")) {
                String newKey = key.substring(0, key.indexOf("<"));
                // HashMap<String, String>
                // HashMap<>
                // String, String

                // HashMap<HashMap<String, String>, HashMap<String, String>>
                // HashMap<>
                // HashMap<String, String>, HashMap<String, String>

                // HashMap<HashMap<HashMap<String, String>, HashMap<String, String>>, HashMap<HashMap<String, String>, HashMap<String, String>>>
                // HashMap<>
                // HashMap<HashMap<String, String>, HashMap<String, String>>, HashMap<HashMap<String, String>, HashMap<String, String>>
                // HashMap<HashMap<String, String>, HashMap<String, String>>
                // HashMap<HashMap<String, String>, HashMap<String, String>>
                String subtypes = key.substring(key.indexOf("<")+1, key.lastIndexOf(">"));

                List<ComplexType> subList;
                if (!subtypes.contains(">")) {
                    subList = Arrays.stream(subtypes.split(",")).map((s) -> getFromAny(s.strip())).toList();
                }
                else if (subtypes.contains(">>")) {
                    var strip = new ArrayList<>(Arrays.stream(subtypes.split(">>,")).toList());
                    for (int i = 0; i < strip.size(); i++) {
                        if (i != strip.size()-1) {
                            strip.set(i, strip.get(i) + ">>");
                        }
                    }
                    subList = strip.stream().map((s) -> getFromAny(s.strip())).toList();
                }
                else {
                    var strip = new ArrayList<>(Arrays.stream(subtypes.split(">,")).toList());
                    for (int i = 0; i < strip.size(); i++) {
                        if (i != strip.size()-1) {
                            strip.set(i, strip.get(i) + ">");
                        }
                    }
                    subList = strip.stream().map((s) -> getFromAny(s.strip())).toList();
                }

                complexTypes = new ArrayList<>(complexTypes);
                complexTypes.addAll(subList);

                return getFromAny(newKey, subList);
            }
            else if (key.contains("[]")) {
                return ctm.of("array", key.substring(0, key.indexOf("[")));
            }

            for (var type : getSortedTypes()) {
                if (Objects.equals(type.fluxType, key) || Objects.equals(type.javaType, key) || Objects.equals(type.javaClassType, key)) {
                    return new ComplexType(type, complexTypes);
                }
            }
            return null;
        }
        public ComplexType getFromAny(String key, ComplexType... complexTypes) {
            return getFromAny(key, Arrays.stream(complexTypes).toList());
        }

        public ComplexType getFromFlux(String fluxType) {
            for (var type : getSortedTypes()) {
                if (Objects.equals(type.fluxType, fluxType)) {
                    return type;
                }
            }
            return null;
        }

        public ComplexType getFromJava(String javaType) {
            for (var type : getSortedTypes()) {
                if (Objects.equals(type.javaType, javaType)) {
                    return type;
                }
            }
            return null;
        }

        public ComplexType getFromJavaClass(String javaClassType) {
            for (var type : getSortedTypes()) {
                if (Objects.equals(type.javaClassType, javaClassType)) {
                    return type;
                }
            }
            return null;
        }

        public ComplexType of(String fluxType, String... subtypes) {
            return new ComplexType(get(fluxType), Arrays.stream(subtypes).map(this::get).toList());
        }
        public ComplexType of(String fluxType, List<String> subtypes) {
            return new ComplexType(get(fluxType), subtypes.stream().map(this::get).toList());
        }

        public ComplexType of(String fluxType, String javaType, String javaClassType, String jvmType) {
            return new ComplexType(fluxType, javaType, javaClassType, jvmType);
        }
        public ComplexType of(String fluxType, String javaType, String javaClassType, String jvmType, int subtypesCapacity) {
            return new ComplexType(fluxType, javaType, javaClassType, jvmType, subtypesCapacity);
        }
    }

    public static <T> List<T> map(Class<T> cl, Object[] array) {

        return Stream.of(array).map((k) -> {
            try {
                return cl.getDeclaredConstructor(k.getClass()).newInstance(k);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    public static <T> List<T> map(Class<T> cl, Collection<?> collection) {

        return collection.stream().map((k) -> {
            try {
                return cl.getDeclaredConstructor(k.getClass()).newInstance(k);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    public static String input(String prepend) {
        Scanner in = new Scanner(System.in);

        if (!prepend.isEmpty())
            print(prepend, "");
        return in.nextLine();
    }

    public static String input() {
        return input("");
    }

    public static <T> void Print(T[] out, String end) {
        StringBuilder builder = new StringBuilder();
        for (var o : out) {
            if (o instanceof ParseTree parseTree) {
                builder.append(parseTree.getText()).append(" ");
            }
            else builder.append(o).append(" ");
        }
        System.out.print(builder + end);
    }

    @Kwargs({"outvarargs", "end"}) // Kwargs must declare method parameters names
    public static <T> void print(T[] out, String end) {
        Print(out, end);
    }

    @SafeVarargs
    @Kwargs("out") // Kwargs must declare method parameters names
    public static <T> void print(T... out) {
        Print(out, "\n");
    }

    public static boolean approximately(double leftValue, double rightValue) {
        return approximately(1, leftValue, rightValue);
    }
    public static boolean approximately(int precision, double... values) {
        return StreamEx.of(Arrays.spliterator(values)).pairMap((a, b) -> Math.abs(a - b) < Math.pow(10, -precision)).allMatch((s) -> s == true);
    }

    // TODO: IMPLEMENT ANY() LOL
    @SafeVarargs
    public static <T> boolean any(T... vars) {
        for (var var : vars) {
            print(var);
        }
        return true;
    }
    // TODO: IMPLEMENT ALL() LOL
    @SafeVarargs
    public static <T> boolean all(T... vars) {
        for (var var : vars) {
            print(var);
        }
        return true;
    }

    public static List<Integer> range(int endExclusive) {
        return range(0, endExclusive, 1);
    }
    public static List<Integer> range(int startInclusive, int endExclusive) {
        return range(startInclusive, endExclusive, 1);
    }
    public static List<Integer> range(int startInclusive, int endExclusive, int step) {
        return new Range(startInclusive, endExclusive, step).toList();
    }

    public static class Range implements Iterable<Integer>, Collection<Integer> {
        private final int startInclusive;
        private final int endExclusive;
        private final int step;

        public Range(int startInclusive, int endExclusive, int step) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
            this.step = step;

            if (step == 0) {
                throw new IllegalArgumentException("Parameter step or Range can't be 0.");
            }
        }

        @Override
        public int size() {
            int n = 0;
            for (int i = startInclusive; step > 0 ? i < endExclusive : i > endExclusive; i += step) {
                n++;
            }
            return n;
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public boolean contains(Object o) {
            for (int i = startInclusive; step > 0 ? i < endExclusive : i > endExclusive; i += step) {
                if (o.equals(i)) return true;
            }
            return false;
        }

        public Iterator<Integer> iterator() {
            return new Iterator<Integer>() {
                private int cur = startInclusive;
                private int count = Range.this.endExclusive;
                public boolean hasNext() {
                    if (step > 0)
                        return count != 0;
                    if (step < 0)
                        return cur != 0;
                    return false;
                }

                public Integer next() {
                    count -= step;
                    return cur += step;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public List<Integer> toList() {
            List<Integer> objects = new ArrayList<>();
            for (int i = startInclusive; step > 0 ? i < endExclusive : i > endExclusive; i += step) {
                objects.add(i);
            }

            return objects;
        }

        @Override
        public Object[] toArray() {
            return toList().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return (T[]) toList().toArray();
        }

        @Override
        public boolean add(Integer integer) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return new HashSet<>(toList()).containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Integer> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }
    }
}
