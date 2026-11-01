package com.cleanroommc.kirino.gl.shader.reflect;

import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.shader.reflect.execution.*;
import com.cleanroommc.kirino.gl.shader.reflect.layout.*;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.*;
import com.cleanroommc.kirino.gl.shader.reflect.resource.*;
import com.cleanroommc.kirino.gl.shader.reflect.type.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;

final class ShaderSemantics {

    final TypeRegistry types;
    final ConstantExpressions expressions;
    final ShaderExecution execution;
    final ImmutableMap<String, Uniform> uniforms;
    final ImmutableMap<String, Variable> inputs;
    final ImmutableMap<String, Variable> outputs;
    final ImmutableMap<String, Variable> variables;
    final ImmutableMap<String, Block> uniformBlocks;
    final ImmutableMap<String, Block> storageBlocks;
    final ImmutableMap<String, Block> inputBlocks;
    final ImmutableMap<String, Block> outputBlocks;
    final ImmutableMap<String, SubroutineFunction> functions;
    private final Map<String, IntegerExpression> feedbackStrides = new HashMap<>();

    ShaderSemantics(@NonNull ShaderMeta meta) {
        expressions = new ConstantExpressions(meta);
        types = new TypeRegistry(meta, expressions);
        execution = execution(meta);

        Map<String, Uniform> uniforms = new LinkedHashMap<>();
        Map<String, Variable> inputs = new LinkedHashMap<>();
        Map<String, Variable> outputs = new LinkedHashMap<>();
        Map<String, Variable> variables = new LinkedHashMap<>();
        Map<String, Block> uniformBlocks = new LinkedHashMap<>();
        Map<String, Block> storageBlocks = new LinkedHashMap<>();
        Map<String, Block> inputBlocks = new LinkedHashMap<>();
        Map<String, Block> outputBlocks = new LinkedHashMap<>();
        Map<String, SubroutineFunction> functions = new LinkedHashMap<>();
        Map<String, LayoutQualifiers> defaults = new HashMap<>();

        defaults.put("uniform", LayoutQualifiers.bufferDefaults(BlockLayout.SHARED, MatrixOrder.COLUMN_MAJOR));
        defaults.put("buffer", LayoutQualifiers.bufferDefaults(BlockLayout.SHARED, MatrixOrder.COLUMN_MAJOR));

        if (meta.shaderType == ShaderType.VERTEX
                || meta.shaderType == ShaderType.TESS_CONTROL
                || meta.shaderType == ShaderType.TESS_EVALUATION
                || meta.shaderType == ShaderType.GEOMETRY) {

            defaults.put("out", LayoutQualifiers.EMPTY.withTransformFeedback(new TransformFeedbackLayout(
                    IntegerExpression.literal(0),
                    null,
                    null,
                    meta.shaderType == ShaderType.GEOMETRY ? IntegerExpression.literal(0) : null)));
        }

        Map<String, Precision> precisions = new HashMap<>();
        Map<String, QualifierSet> redeclarations = new HashMap<>();
        Map<String, IntegerExpression> atomicOffsets = new HashMap<>();

        for (ShaderMeta.QualifierDeclaration declaration : meta.qualifiers) {
            for (ShaderMeta.Declarator declarator : declaration.declarators()) {
                redeclarations.merge(declarator.name(), QualifierSet.from(declaration.qualifiers()), QualifierSet::overlay);
            }
        }

        for (ShaderMeta.Declaration declaration : meta.topLevelDeclarations) {
            switch (declaration) {
                case ShaderMeta.GlobalLayoutDeclaration global -> defaults.put(global.storage(),
                        layout(global.layouts(), defaults.getOrDefault(global.storage(), LayoutQualifiers.EMPTY)));
                case ShaderMeta.PrecisionDeclaration precision -> precisions.put(precision.type().name(),
                        Precision.valueOf(precision.precision().toUpperCase(java.util.Locale.ROOT)));
                case ShaderMeta.UniformDeclaration uniform -> {
                    LayoutQualifiers layout = layout(uniform.layouts(), LayoutQualifiers.EMPTY);
                    boolean atomic = types.resolve(uniform.type()).base() instanceof AtomicCounterType;
                    String atomicBinding = null;
                    IntegerExpression atomicOffset = null;

                    if (atomic) {
                        Preconditions.checkArgument(layout.binding() != null,
                                "Atomic counter declarations require a binding qualifier.");

                        atomicBinding = layout.binding().value() == null ? layout.binding().expression() : layout.binding().value().toString();
                        atomicOffset = layout.offset() != null ? layout.offset() : atomicOffsets.getOrDefault(atomicBinding, IntegerExpression.literal(0));
                        atomicOffsets.put(atomicBinding, atomicOffset);
                    }
                    Preconditions.checkArgument(atomic || !uniform.declarators().isEmpty(),
                            "A uniform declaration requires a variable.");

                    for (ShaderMeta.Declarator declarator : uniform.declarators()) {
                        GlslType type = types.resolve(uniform.type(), declarator);
                        StorageType storage = uniform.qualifiers().contains("subroutine")
                                ? StorageType.SUBROUTINE_UNIFORM
                                : type.base() instanceof AtomicCounterType ? StorageType.ATOMIC_COUNTER : StorageType.DEFAULT_UNIFORM;
                        QualifierSet qualifiers = QualifierSet.resolve(uniform.qualifiers(), type, precisions).overlay(redeclarations.getOrDefault(declarator.name(), QualifierSet.EMPTY));
                        Uniform existing = uniforms.get(declarator.name());
                        ImmutableList<ShaderMeta.SourceSpan> spans = existing == null
                                ? ImmutableList.of(uniform.span())
                                : ImmutableList.<ShaderMeta.SourceSpan>builder().addAll(existing.declarations()).add(uniform.span()).build();
                        LayoutQualifiers effectiveLayout = existing == null
                                ? layout
                                : layout(uniform.layouts(), existing.layout());

                        if (atomic) {
                            Preconditions.checkArgument(atomicOffset.value() == null || atomicOffset.value() % 4 == 0,
                                    "Atomic counter offset must be a multiple of four.");

                            effectiveLayout = effectiveLayout.withOffset(atomicOffset);

                            StringBuilder count = new StringBuilder("1");
                            while (type instanceof ArrayType(GlslType elementType, ArrayExtent extent)) {
                                Preconditions.checkArgument(extent.size() != null, "An atomic counter array must be sized.");

                                count.append("*(").append(extent.size().expression()).append(')');
                                type = elementType;
                            }

                            atomicOffset = expressions.expression("(" + atomicOffset.expression() + ")+4*(" + count.toString() + ")");
                            atomicOffsets.put(atomicBinding, atomicOffset);
                        }

                        uniforms.put(declarator.name(), new Uniform(
                                declarator.name(),
                                storage,
                                type,
                                qualifiers,
                                effectiveLayout,
                                declarator.initializer(),
                                spans));
                    }
                }
                case ShaderMeta.InputOutputDeclaration variable -> {
                    boolean input = variable.storage().equals("in")
                            || variable.storage().equals("attribute")
                            || variable.storage().equals("varying")
                                    && meta.shaderType == ShaderType.FRAGMENT;

                    Map<String, Variable> target = input ? inputs : outputs;
                    LayoutQualifiers layout = layout(variable.layouts(), defaults.getOrDefault(input ? "in" : "out", LayoutQualifiers.EMPTY));
                    for (ShaderMeta.Declarator declarator : variable.declarators()) {
                        GlslType type = types.resolve(variable.type(), declarator);
                        QualifierSet qualifiers = QualifierSet.resolve(variable.qualifiers(), type, precisions).overlay(redeclarations.getOrDefault(declarator.name(), QualifierSet.EMPTY));
                        InterfaceArraying arraying = arraying(meta.shaderType, input, qualifiers.patch());
                        if (arraying == InterfaceArraying.PER_VERTEX && type instanceof ArrayType(GlslType elementType, ArrayExtent extent)) {
                            type = new ArrayType(elementType, interfaceExtent(extent, input));
                        }
                        target.put(declarator.name(), new Variable(
                                declarator.name(),
                                input ? StorageType.STAGE_INPUT : StorageType.STAGE_OUTPUT,
                                type,
                                arraying,
                                qualifiers,
                                layout,
                                declarator.initializer()));
                    }
                }
                case ShaderMeta.InterfaceBlock block -> {
                    LayoutQualifiers layout = layout(block.layouts(), defaults.getOrDefault(block.storage(), LayoutQualifiers.EMPTY));
                    QualifierSet qualifiers = QualifierSet.from(block.qualifiers());
                    if (block.instance() != null) {
                        qualifiers = qualifiers.overlay(redeclarations.getOrDefault(block.instance().name(), QualifierSet.EMPTY));
                    }

                    StorageType storage = switch (block.storage()) {
                        case "uniform" -> StorageType.UNIFORM_BLOCK;
                        case "buffer" -> StorageType.SHADER_STORAGE_BLOCK;
                        case "in" -> StorageType.STAGE_INPUT;
                        case "out" -> StorageType.STAGE_OUTPUT;
                        default -> throw new IllegalArgumentException("Unknown interface block storage.");
                    };

                    ImmutableList.Builder<BlockMember> members = ImmutableList.builder();
                    IntegerExpression nextLocation = layout.location();
                    IntegerExpression nextFeedbackOffset = layout.transformFeedback().offset();
                    for (int i = 0; i < block.members().size(); i++) {
                        ShaderMeta.Member member = block.members().get(i);
                        for (int j = 0; j < member.declarators().size(); j++) {
                            ShaderMeta.Declarator declarator = member.declarators().get(j);
                            GlslType type = types.resolve(member.type(), declarator);
                            boolean last = i == block.members().size() - 1 && j == member.declarators().size() - 1;
                            if (storage == StorageType.SHADER_STORAGE_BLOCK
                                    && last && type instanceof ArrayType(GlslType elementType, ArrayExtent extent)
                                    && extent.size() == null) {
                                type = new ArrayType(elementType, ArrayExtent.RUNTIME_SIZED);
                            }

                            LayoutQualifiers memberDefaults = layout.memberDefaults().withLocation(nextLocation);
                            TransformFeedbackLayout feedback = memberDefaults.transformFeedback();
                            memberDefaults = memberDefaults.withTransformFeedback(new TransformFeedbackLayout(
                                    feedback.buffer(),
                                    nextFeedbackOffset,
                                    feedback.stride(),
                                    feedback.stream()));

                            LayoutQualifiers memberLayout = layout(member.layouts(), memberDefaults);
                            if (layout.location() != null) {
                                nextLocation = expressions.add(memberLayout.location(), locations(type, meta.shaderType == ShaderType.VERTEX && storage == StorageType.STAGE_INPUT));
                            }
                            if (layout.transformFeedback().offset() != null) {
                                IntegerExpression start = memberLayout.transformFeedback().offset();
                                start = expressions.round(start, feedbackAlignment(type));
                                memberLayout = memberLayout.withTransformFeedback(new TransformFeedbackLayout(
                                        memberLayout.transformFeedback().buffer(),
                                        start,
                                        memberLayout.transformFeedback().stride(),
                                        memberLayout.transformFeedback().stream()));
                                nextFeedbackOffset = expressions.add(start, feedbackSize(type));
                            }

                            QualifierSet memberQualifiers = qualifiers.overlay(QualifierSet.resolve(member.qualifiers(), type, precisions));
                            if (block.instance() == null) {
                                memberQualifiers = memberQualifiers.overlay(redeclarations.getOrDefault(declarator.name(), QualifierSet.EMPTY));
                            }

                            members.add(new BlockMember(declarator.name(), type, memberQualifiers, memberLayout));
                        }
                    }

                    BlockInstance instance = null;
                    if (block.instance() != null) {
                        InterfaceArraying arraying = arraying(meta.shaderType, storage == StorageType.STAGE_INPUT, qualifiers.patch());
                        if (storage == StorageType.UNIFORM_BLOCK || storage == StorageType.SHADER_STORAGE_BLOCK) {
                            arraying = InterfaceArraying.NONE;
                        }
                        List<ArrayExtent> dimensions = new ArrayList<>();
                        for (ShaderMeta.ArrayDimension dimension : block.instance().arrayDimensions()) {
                            dimensions.add(types.extent(dimension));
                        }
                        if (arraying == InterfaceArraying.PER_VERTEX && !dimensions.isEmpty()) {
                            dimensions.set(0, interfaceExtent(dimensions.getFirst(), storage == StorageType.STAGE_INPUT));
                        }
                        instance = new BlockInstance(block.instance().name(), ImmutableList.copyOf(dimensions), arraying);
                    }

                    ImmutableList<BlockMember> resolved = members.build();
                    Block result = new Block(
                            block.name(),
                            storage,
                            qualifiers,
                            layout,
                            instance,
                            resolved,
                            storage == StorageType.UNIFORM_BLOCK || storage == StorageType.SHADER_STORAGE_BLOCK
                                    ? BufferLayouts.compute(resolved, layout)
                                    : java.util.Optional.empty());

                    Map<String, Block> target = switch (storage) {
                        case UNIFORM_BLOCK -> uniformBlocks;
                        case SHADER_STORAGE_BLOCK -> storageBlocks;
                        case STAGE_INPUT -> inputBlocks;
                        default -> outputBlocks;
                    };

                    target.put(block.name(), result);
                }
                case ShaderMeta.VariableDeclaration variable -> {
                    for (ShaderMeta.Declarator declarator : variable.declarators()) {
                        GlslType type = types.resolve(variable.type(), declarator);
                        addVariable(variables, variable.storage(), variable.qualifiers(), variable.layouts(), declarator, type, precisions, redeclarations);
                    }
                }
                case ShaderMeta.StructDeclaration struct -> {
                    for (ShaderMeta.Declarator declarator : struct.declarators()) {
                        addVariable(variables, null, struct.qualifiers(), struct.layouts(), declarator, types.resolve(struct, declarator), precisions, redeclarations);
                    }
                }
                case ShaderMeta.SubroutineFunctionDeclaration function -> {
                    ImmutableList.Builder<SubroutineType> compatible = ImmutableList.builder();
                    ImmutableList<Parameter> parameters = function.parameters()
                            .stream()
                            .map(types::resolve)
                            .collect(ImmutableList.toImmutableList());
                    GlslType returnType = types.resolve(function.returnType());

                    for (String name : function.compatibleTypes()) {
                        SubroutineType signature = types.type(name, SubroutineType.class)
                                .orElseThrow(() -> new IllegalArgumentException("Unknown subroutine type \"" + name + "\"."));

                        Preconditions.checkArgument(GlslType.sameType(signature.returnType(), returnType)
                                        && signature.parameters().size() == parameters.size(),
                                "Function \"%s\" does not match subroutine type \"%s\".", function.name(), name);
                        for (int i = 0; i < parameters.size(); i++) {
                            Preconditions.checkArgument(GlslType.sameType(parameters.get(i).type(), signature.parameters().get(i).type())
                                            && parameters.get(i).direction() == signature.parameters().get(i).direction(),
                                    "Function \"%s\" has an incompatible parameter for subroutine type \"%s\".", function.name(), name);
                        }

                        compatible.add(signature);
                    }

                    functions.put(function.name(), new SubroutineFunction(
                            function.name(),
                            returnType,
                            QualifierSet.resolve(function.qualifiers(), returnType, precisions),
                            parameters,
                            compatible.build(),
                            layout(function.layouts(), LayoutQualifiers.EMPTY)));
                }
                case ShaderMeta.SubroutineTypeDeclaration ignored -> {
                }
                case ShaderMeta.QualifierDeclaration ignored -> {
                }
            }
        }

        this.uniforms = ImmutableMap.copyOf(uniforms);
        this.inputs = ImmutableMap.copyOf(inputs);

        outputs.replaceAll((name, value) -> new Variable(
                value.name(),
                value.storageType(),
                value.type(),
                value.interfaceArraying(),
                value.qualifiers(),
                feedbackStride(value.layout()),
                value.initializer()));

        outputBlocks.replaceAll((name, value) -> new Block(
                value.name(),
                value.storageType(),
                value.qualifiers(),
                feedbackStride(value.layout()),
                value.instance(),
                value.members()
                        .stream()
                        .map(member -> new BlockMember(member.name(), member.type(), member.qualifiers(), feedbackStride(member.layout())))
                        .collect(ImmutableList.toImmutableList()), value.computedLayout()));

        this.outputs = ImmutableMap.copyOf(outputs);
        this.variables = ImmutableMap.copyOf(variables);
        this.uniformBlocks = ImmutableMap.copyOf(uniformBlocks);
        this.storageBlocks = ImmutableMap.copyOf(storageBlocks);
        this.inputBlocks = ImmutableMap.copyOf(inputBlocks);
        this.outputBlocks = ImmutableMap.copyOf(outputBlocks);
        this.functions = ImmutableMap.copyOf(functions);
    }

    private void addVariable(
            Map<String, Variable> variables,
            String storage,
            List<String> declarations,
            List<ShaderMeta.Layout> layouts,
            ShaderMeta.Declarator declarator,
            GlslType type,
            Map<String, Precision> precisions,
            Map<String, QualifierSet> redeclarations) {

        StorageType kind = "shared".equals(storage)
                ? StorageType.SHARED
                : declarations.contains("const") ? StorageType.CONSTANT : StorageType.GLOBAL;
        QualifierSet qualifiers = QualifierSet.resolve(declarations, type, precisions)
                .overlay(redeclarations.getOrDefault(declarator.name(), QualifierSet.EMPTY));

        if (kind == StorageType.SHARED) {
            qualifiers = qualifiers.overlay(new QualifierSet(
                    null,
                    null,
                    null,
                    false,
                    false,
                    false,
                    ImmutableSet.of(MemoryQualifier.COHERENT),
                    ImmutableList.of()));
        }

        variables.put(declarator.name(), new Variable(
                declarator.name(),
                kind,
                type,
                InterfaceArraying.NONE,
                qualifiers,
                layout(layouts, LayoutQualifiers.EMPTY),
                declarator.initializer()));
    }

    private LayoutQualifiers layout(List<ShaderMeta.Layout> declarations, LayoutQualifiers defaults) {
        LayoutQualifiers result = LayoutQualifiers.resolve(declarations, expressions, defaults);
        TransformFeedbackLayout feedback = result.transformFeedback();
        if (feedback.buffer() != null && declarations.stream().anyMatch(value -> value.name().equalsIgnoreCase("xfb_stride"))) {
            feedbackStrides.put(feedback.buffer().value() == null ? feedback.buffer().expression() : feedback.buffer().value().toString(), feedback.stride());
        }
        return feedbackStride(result);
    }

    private LayoutQualifiers feedbackStride(LayoutQualifiers layout) {
        TransformFeedbackLayout feedback = layout.transformFeedback();
        if (feedback.buffer() == null) {
            return layout;
        }

        return layout.withTransformFeedback(new TransformFeedbackLayout(
                feedback.buffer(),
                feedback.offset(),
                feedbackStrides.get(feedback.buffer().value() == null ? feedback.buffer().expression() : feedback.buffer().value().toString()),
                feedback.stream()));
    }

    private static String locations(GlslType type, boolean vertexInput) {
        return switch (type) {
            case ArrayType array ->
                    "(" + (array.extent().size() == null ? "unresolved_array_length" : array.extent().size().expression()) + ")*("
                    + locations(array.elementType(), vertexInput) + ")";
            case StructType struct -> struct.members()
                    .stream()
                    .map(member -> "(" + locations(member.type(), vertexInput) + ")")
                    .collect(Collectors.joining("+"));
            case MatrixType matrix ->
                    "(" + matrix.columns() + ")*("
                    + locations(new VectorType(matrix.componentType(), matrix.rows()), vertexInput) + ")";
            case VectorType vector -> !vertexInput
                    && vector.componentType() == ScalarType.DOUBLE
                    && vector.components() > 2 ? "2" : "1";
            case ScalarType ignored -> "1";
            default -> "unresolved_location_count";
        };
    }

    private static int feedbackAlignment(GlslType type) {
        return switch (type) {
            case ScalarType scalar -> scalar == ScalarType.DOUBLE ? 8 : 4;
            case VectorType vector -> feedbackAlignment(vector.componentType());
            case MatrixType matrix -> feedbackAlignment(matrix.componentType());
            case ArrayType array -> feedbackAlignment(array.elementType());
            case StructType struct -> struct.members().stream().mapToInt(member -> feedbackAlignment(member.type())).max().orElse(4);
            default -> 4;
        };
    }

    private static String feedbackSize(GlslType type) {
        return switch (type) {
            case ScalarType scalar -> Integer.toString(feedbackAlignment(scalar));
            case VectorType vector -> Integer.toString(feedbackAlignment(vector) * vector.components());
            case MatrixType matrix -> Integer.toString(feedbackAlignment(matrix) * matrix.columns() * matrix.rows());
            case ArrayType array ->
                    "(" + (array.extent().size() == null ? "unresolved_array_length" : array.extent().size().expression()) + ")*("
                    + feedbackSize(array.elementType()) + ")";
            case StructType struct -> {
                String cursor = "0";
                for (StructMember member : struct.members()) {
                    int alignment = feedbackAlignment(member.type());
                    cursor = "((" + cursor + ")+" + (alignment - 1) + ")/" + alignment + "*" + alignment + "+(" + feedbackSize(member.type()) + ")";
                }
                int alignment = feedbackAlignment(struct);
                yield "((" + cursor + ")+" + (alignment - 1) + ")/" + alignment + "*" + alignment;
            }
            default -> "unresolved_feedback_size";
        };
    }

    private static InterfaceArraying arraying(ShaderType stage, boolean input, boolean patch) {
        return !patch
                && (input && (stage == ShaderType.GEOMETRY
                || stage == ShaderType.TESS_CONTROL
                || stage == ShaderType.TESS_EVALUATION)
                || !input && stage == ShaderType.TESS_CONTROL)
                        ? InterfaceArraying.PER_VERTEX
                        : InterfaceArraying.NONE;
    }

    private ArrayExtent interfaceExtent(ArrayExtent extent, boolean input) {
        if (extent.size() != null) {
            return extent;
        }

        IntegerExpression size = null;
        if (execution instanceof GeometryExecution geometry && geometry.input() != null) {
            long count = switch (geometry.input()) {
                case POINTS -> 1;
                case LINES -> 2;
                case LINES_ADJACENCY -> 4;
                case TRIANGLES -> 3;
                case TRIANGLES_ADJACENCY -> 6;
            };
            size = IntegerExpression.literal(count);
        } else if (!input && execution instanceof TessControlExecution(IntegerExpression outputVertices)) {
            size = outputVertices;
        }

        return size == null ? extent : ArrayExtent.sized(size);
    }

    private static <T extends Enum<T>> T select(Map<String, IntegerExpression> layouts, T[] values) {
        for (T value : values) {
            if (layouts.containsKey(value.name().toLowerCase(Locale.ROOT))) {
                return value;
            }
        }
        return null;
    }

    private ShaderExecution execution(ShaderMeta meta) {
        Map<String, IntegerExpression> in = new LinkedHashMap<>();
        Map<String, IntegerExpression> out = new LinkedHashMap<>();
        for (ShaderMeta.GlobalLayoutDeclaration declaration : meta.globalLayouts) {
            if (!declaration.storage().equals("in") && !declaration.storage().equals("out")) {
                continue;
            }

            Map<String, IntegerExpression> target = declaration.storage().equals("in") ? in : out;
            for (ShaderMeta.Layout layout : declaration.layouts()) {
                target.put(layout.name().toLowerCase(java.util.Locale.ROOT),
                        layout.expression() == null ? null : expressions.expression(layout.expression()));
            }
        }

        return switch (meta.shaderType) {
            case VERTEX -> VertexExecution.INSTANCE;
            case FRAGMENT -> new FragmentExecution(in.containsKey("early_fragment_tests"));
            case COMPUTE -> new ComputeExecution(
                    in.getOrDefault("local_size_x", IntegerExpression.literal(1)),
                    in.getOrDefault("local_size_y", IntegerExpression.literal(1)),
                    in.getOrDefault("local_size_z", IntegerExpression.literal(1)));
            case TESS_CONTROL -> new TessControlExecution(out.get("vertices"));
            case TESS_EVALUATION -> new TessEvaluationExecution(
                    select(in, TessellationPrimitive.values()),
                    select(in, Spacing.values()) == null ? Spacing.EQUAL_SPACING : Preconditions.checkNotNull(select(in, Spacing.values())),
                    select(in, Winding.values()) == null ? Winding.CCW : Preconditions.checkNotNull(select(in, Winding.values())),
                    in.containsKey("point_mode"));
            case GEOMETRY -> new GeometryExecution(
                    select(in, GeometryInput.values()),
                    in.getOrDefault("invocations", IntegerExpression.literal(1)),
                    select(out, GeometryOutput.values()),
                    out.get("max_vertices"));
        };
    }
}
