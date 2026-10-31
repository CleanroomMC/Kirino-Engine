package com.cleanroommc.kirino.gl.shader.reflect;

import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConstantExpressions {

    private enum Kind {
        INT,
        UINT,
        BOOL
    }

    private record Value(int bits, Kind kind) {

        long number() {
            return kind == Kind.UINT ? Integer.toUnsignedLong(bits) : bits;
        }

        boolean truth() {
            return bits != 0;
        }
    }

    private interface Node {

        Value evaluate(Map<String, Value> values);

        default Kind kind() {
            return Kind.INT;
        }
    }

    private final Map<String, Value> values = new LinkedHashMap<>();
    private final ImmutableMap<String, IntegerExpression> constants;

    @NonNull
    public ImmutableMap<String, IntegerExpression> constants() {
        return constants;
    }

    @NonNull
    public OptionalLong constant(@NonNull String name) {
        Preconditions.checkNotNull(name);

        Value value = values.get(name);
        return value == null ? OptionalLong.empty() : OptionalLong.of(value.number());
    }

    @NonNull
    public IntegerExpression expression(@NonNull String expression) {
        Preconditions.checkNotNull(expression);

        Value value = evaluateValue(expression);
        return new IntegerExpression(expression, value == null || value.kind() == Kind.BOOL ? null : value.number());
    }

    public ConstantExpressions(@NonNull ShaderMeta meta) {
        Preconditions.checkNotNull(meta);

        ImmutableMap.Builder<String, IntegerExpression> constants = ImmutableMap.builder();
        for (ShaderMeta.Declaration declaration : meta.topLevelDeclarations) {
            if (!(declaration instanceof ShaderMeta.VariableDeclaration variable)
                    || !variable.qualifiers().contains("const")
                    || !variable.type().arrayDimensions().isEmpty()
                    || variable.type().name() == null
                    || !List.of("int", "uint", "bool").contains(variable.type().name())) {
                continue;
            }

            for (ShaderMeta.Declarator declarator : variable.declarators()) {
                if (declarator.initializer() == null || !declarator.arrayDimensions().isEmpty()) {
                    continue;
                }

                Value value = evaluateValue(declarator.initializer());
                if (value != null
                        && !variable.type().name().equals(value.kind().name().toLowerCase(Locale.ROOT))
                        && !(variable.type().name().equals("uint") && value.kind() == Kind.INT)) {
                    value = null;
                }

                if (value != null) {
                    value = cast(variable.type().name(), value);
                    values.put(declarator.name(), value);
                }

                constants.put(declarator.name(), new IntegerExpression(declarator.initializer(), value == null ? null : value.number()));
            }
        }
        this.constants = constants.buildOrThrow();
    }

    @Nullable
    private Value evaluateValue(@NonNull String expression) {
        try {
            Parser parser = new Parser(expression, values);
            Node node = parser.conditional();
            Preconditions.checkArgument(parser.position == parser.tokens.size(),
                    "Unexpected expression token.");

            return node.evaluate(values);
        } catch (IllegalArgumentException | ArithmeticException ignored) {
            return null;
        }
    }

    @NonNull
    private static Value bool(boolean value) {
        return new Value(value ? 1 : 0, Kind.BOOL);
    }

    @NonNull
    private static Value cast(@NonNull String type, @NonNull Value value) {
        return switch (type) {
            case "int" -> new Value(value.bits(), Kind.INT);
            case "uint" -> new Value(value.bits(), Kind.UINT);
            case "bool" -> bool(value.truth());
            default -> throw new IllegalArgumentException("Unknown scalar constructor.");
        };
    }

    @NonNull
    private static Node typed(@NonNull Kind kind, @NonNull Node node) {
        return new Node() {

            @Override
            public Value evaluate(Map<String, Value> values) {
                return node.evaluate(values);
            }

            @Override
            public Kind kind() {
                return kind;
            }
        };
    }

    @NonNull
    private static Kind common(@NonNull Kind left, @NonNull Kind right) {
        Preconditions.checkArgument((left == Kind.BOOL) == (right == Kind.BOOL), "Boolean and numeric operands cannot be mixed.");

        return left == Kind.BOOL ? Kind.BOOL : left == Kind.UINT || right == Kind.UINT ? Kind.UINT : Kind.INT;
    }

    @NonNull
    private static Value binary(String operator, Value left, Value right) {
        if (operator.equals("&&") || operator.equals("||") || operator.equals("^^")) {
            return bool(switch (operator) {
                case "&&" -> left.truth() && right.truth();
                case "||" -> left.truth() || right.truth();
                default -> left.truth() ^ right.truth();
            });
        }

        boolean unsigned = left.kind() == Kind.UINT || right.kind() == Kind.UINT;
        long a = unsigned ? Integer.toUnsignedLong(left.bits()) : left.bits();
        long b = unsigned ? Integer.toUnsignedLong(right.bits()) : right.bits();
        switch (operator) {
            case "==": return bool(a == b);
            case "!=": return bool(a != b);
            case "<": return bool(a < b);
            case "<=": return bool(a <= b);
            case ">": return bool(a > b);
            case ">=": return bool(a >= b);
        }
        Preconditions.checkArgument(left.kind() != Kind.BOOL && right.kind() != Kind.BOOL,
                "Arithmetic requires numeric operands.");

        if (operator.equals("<<") || operator.equals(">>")) {
            Preconditions.checkArgument(b >= 0 && b < 32, "Invalid shift count.");

            int bits = operator.equals("<<") ? left.bits() << b : left.kind() == Kind.UINT ? left.bits() >>> b : left.bits() >> b;
            return new Value(bits, left.kind());
        }
        Preconditions.checkArgument(!operator.equals("%") || a >= 0 && b >= 0,
                "Remainders with negative operands are undefined.");

        long result = switch (operator) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            case "%" -> a % b;
            case "&" -> a & b;
            case "|" -> a | b;
            case "^" -> a ^ b;
            default -> throw new IllegalArgumentException("Unknown binary operator.");
        };

        if (!unsigned && operator.equals("/") && result > Integer.MAX_VALUE) {
            throw new ArithmeticException("Signed constant expression overflow.");
        }

        return new Value((int) result, unsigned ? Kind.UINT : Kind.INT);
    }

    private static final class Parser {

        private static final Pattern TOKEN = Pattern.compile("\\s*(0[xX][0-9a-fA-F]+[uU]?|[0-9]+[uU]?|[a-zA-Z_][a-zA-Z_0-9]*|<<|>>|<=|>=|==|!=|&&|\\|\\||\\^\\^|\\+\\+|--|[()+*/%<>&|^!~?:,\\-])");
        private static final List<List<String>> OPERATORS = List.of(
                List.of("||"),
                List.of("^^"),
                List.of("&&"),
                List.of("|"),
                List.of("^"),
                List.of("&"),
                List.of("==", "!="),
                List.of("<", "<=", ">", ">="),
                List.of("<<", ">>"),
                List.of("+", "-"),
                List.of("*", "/", "%"));

        private final List<String> tokens = new ArrayList<>();
        private final Map<String, Value> values;
        private int position;

        Parser(String expression, Map<String, Value> values) {
            this.values = values;
            Matcher matcher = TOKEN.matcher(expression.strip());
            int end = 0;
            while (matcher.find()) {
                Preconditions.checkArgument(matcher.start() == end, "Unsupported expression syntax.");

                tokens.add(matcher.group(1));
                end = matcher.end();
            }
            Preconditions.checkArgument(end == expression.strip().length(), "Unsupported expression syntax.");
        }

        private boolean take(String token) {
            if (position < tokens.size() && tokens.get(position).equals(token)) {
                position++;
                return true;
            }
            return false;
        }

        private Node conditional() {
            Node condition = binary(0);
            if (!take("?")) {
                return condition;
            }
            Node yes = conditional();
            Preconditions.checkArgument(take(":"), "Missing expression delimiter.");
            Node no = conditional();
            Preconditions.checkArgument(condition.kind() == Kind.BOOL,
                    "A conditional expression requires a Boolean condition.");

            Kind kind = common(yes.kind(), no.kind());
            return typed(kind, values -> {
                Value result = condition.evaluate(values).truth() ? yes.evaluate(values) : no.evaluate(values);
                return new Value(result.bits(), kind);
            });
        }

        private Node binary(int precedence) {
            if (precedence == OPERATORS.size()) {
                return unary();
            }
            Node left = binary(precedence + 1);
            while (position < tokens.size() && OPERATORS.get(precedence).contains(tokens.get(position))) {
                String operator = tokens.get(position++);
                Node first = left;
                Node right = binary(precedence + 1);
                boolean logical = List.of("&&", "||", "^^").contains(operator);
                boolean equality = operator.equals("==") || operator.equals("!=");

                Preconditions.checkArgument(logical
                        ? first.kind() == Kind.BOOL && right.kind() == Kind.BOOL
                        : equality
                                ? (first.kind() == Kind.BOOL) == (right.kind() == Kind.BOOL)
                                : first.kind() != Kind.BOOL && right.kind() != Kind.BOOL, "Invalid constant-expression operand types.");

                Kind kind = List.of("&&", "||", "^^", "==", "!=", "<", "<=", ">", ">=").contains(operator)
                        ? Kind.BOOL
                        : operator.equals("<<") || operator.equals(">>")
                                ? first.kind()
                                : common(first.kind(), right.kind());

                left = typed(kind, values -> {
                    Value a = first.evaluate(values);
                    if (operator.equals("&&") && !a.truth()) {
                        return bool(false);
                    }
                    if (operator.equals("||") && a.truth()) {
                        return bool(true);
                    }
                    return ConstantExpressions.binary(operator, a, right.evaluate(values));
                });
            }
            return left;
        }

        private Node unary() {
            Preconditions.checkArgument(position < tokens.size(),
                    "Missing expression operand.");

            String token = tokens.get(position++);

            if (List.of("+", "-", "!", "~").contains(token)) {
                Node operand = unary();
                Preconditions.checkArgument(token.equals("!") == (operand.kind() == Kind.BOOL),
                        "Invalid unary operand type.");

                return typed(token.equals("!") ? Kind.BOOL : operand.kind(), values -> {
                    Value value = operand.evaluate(values);
                    if (token.equals("!")) {
                        return bool(!value.truth());
                    }
                    Preconditions.checkArgument(value.kind() != Kind.BOOL, "Expected a numeric operand.");

                    return new Value(token.equals("-") ? -value.bits() : token.equals("~") ? ~value.bits() : value.bits(), value.kind());
                });
            }

            if (token.equals("(")) {
                Node node = conditional();
                Preconditions.checkArgument(take(")"), "Missing expression delimiter.");
                return node;
            }

            if (token.equals("true") || token.equals("false")) {
                return typed(Kind.BOOL, values -> bool(token.equals("true")));
            }

            if (Character.isDigit(token.charAt(0))) {
                boolean unsigned = token.endsWith("u") || token.endsWith("U");
                String literal = unsigned ? token.substring(0, token.length() - 1) : token;
                long number = literal.startsWith("0x") || literal.startsWith("0X")
                        ? Long.parseLong(literal.substring(2), 16)
                        : Long.parseLong(literal, literal.length() > 1 && literal.startsWith("0") ? 8 : 10);
                Preconditions.checkArgument(number <= 0xFFFFFFFFL, "Integer literal is out of range.");

                Value value = new Value((int) number, unsigned ? Kind.UINT : Kind.INT);
                return typed(value.kind(), values -> value);
            }
            if (take("(")) {
                List<Node> arguments = new ArrayList<>();
                if (!take(")")) {
                    do {
                        arguments.add(conditional());
                    } while (take(","));
                    Preconditions.checkArgument(take(")"), "Missing expression delimiter.");
                }
                Kind kind = callKind(token, arguments);
                return typed(kind, values -> call(token, arguments.stream().map(node -> node.evaluate(values)).toList()));
            }
            Preconditions.checkArgument(values.containsKey(token), "Unknown constant.");

            Kind kind = values.get(token).kind();
            return typed(kind, values -> {
                Value value = values.get(token);
                Preconditions.checkArgument(value != null, "Unknown constant.");

                return value;
            });
        }

        private static Kind callKind(String name, List<Node> arguments) {
            if (List.of("int", "uint", "bool").contains(name) && arguments.size() == 1) {
                return Kind.valueOf(name.toUpperCase(java.util.Locale.ROOT));
            }

            if ((name.equals("abs") || name.equals("sign"))
                    && arguments.size() == 1
                    && arguments.getFirst().kind() == Kind.INT) {
                return Kind.INT;
            }

            if ((List.of("min", "max").contains(name) && arguments.size() == 2
                    || name.equals("clamp") && arguments.size() == 3)
                    && arguments.stream().noneMatch(node -> node.kind() == Kind.BOOL)) {
                return arguments.stream().anyMatch(node -> node.kind() == Kind.UINT) ? Kind.UINT : Kind.INT;
            }

            throw new IllegalArgumentException("Unsupported constant function or operand types.");
        }

        private static Value call(String name, List<Value> arguments) {
            if (List.of("int", "uint", "bool").contains(name) && arguments.size() == 1) {
                return cast(name, arguments.getFirst());
            }

            if (List.of("min", "max").contains(name) && arguments.size() == 2) {
                Value a = arguments.get(0);
                Value b = arguments.get(1);
                Value result = ConstantExpressions.binary(name.equals("min") ? "<" : ">", a, b).truth() ? a : b;
                return new Value(result.bits(), a.kind() == Kind.UINT || b.kind() == Kind.UINT ? Kind.UINT : result.kind());
            }

            if (name.equals("clamp") && arguments.size() == 3) {
                if (arguments.stream().anyMatch(value -> value.kind() == Kind.UINT)) {
                    arguments = arguments.stream().map(value -> cast("uint", value)).toList();
                }
                Preconditions.checkArgument(!ConstantExpressions.binary(">", arguments.get(1), arguments.get(2)).truth(),
                        "Clamp bounds are reversed.");

                return call("min", List.of(call("max", arguments.subList(0, 2)), arguments.get(2)));
            }

            if ((name.equals("abs") || name.equals("sign")) && arguments.size() == 1) {
                Value argument = arguments.getFirst();
                Preconditions.checkArgument(argument.kind() == Kind.INT, "This function requires signed integers.");

                int value = argument.bits();
                if (name.equals("abs") && value == Integer.MIN_VALUE) {
                    throw new ArithmeticException("Signed constant expression overflow.");
                }

                return new Value(name.equals("abs") ? Math.abs(value) : Integer.signum(value), Kind.INT);
            }

            throw new IllegalArgumentException("Unsupported constant function.");
        }
    }
}
