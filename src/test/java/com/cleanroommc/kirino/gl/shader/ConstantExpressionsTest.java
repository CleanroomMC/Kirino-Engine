package com.cleanroommc.kirino.gl.shader;

import com.cleanroommc.kirino.gl.shader.reflect.ConstantExpressions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ConstantExpressionsTest {

    private static ConstantExpressions expressions(String source) {
        ShaderMeta meta = new DefaultShaderAnalyzer().analyze(source, ShaderType.VERTEX);
        assertNotNull(meta);

        return new ConstantExpressions(meta);
    }

    private static final ConstantExpressions EMPTY = expressions("");

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource(delimiter = ';', textBlock = """
            0; 0
            123; 123
            0123; 83
            0xFF; 255
            0Xff; 255
            123u; 123
            123U; 123
            0xffffffff; -1
            0x80000000; -2147483648
            3000000000; -1294967296
            0xFFFFFFFFu; 4294967295
            1 + 2 * 3; 7
            (1 + 2) * 3; 9
            20 / 2 / 2; 5
            20 - 5 - 3; 12
            1 + 2 << 3; 24
            16 >> 1 + 1; 4
            1 << 2 + 1; 8
            1 | 2 & 4; 1
            1 ^ 3 & 1; 0
            1 | 2 ^ 3; 1
            +5; 5
            -5; -5
            -(-5); 5
            - -5; 5
            4 + +2; 6
            ~0; -1
            ~0u; 4294967295
            ~~3; 3
            2147483647 + 1; -2147483648
            2147483647 + 2; -2147483647
            -2147483648 - 1; 2147483647
            -2147483648; -2147483648
            -(-2147483648); -2147483648
            65536 * 65536; 0
            0u - 1u; 4294967295
            0xFFFFFFFFu + 1u; 0
            0xFFFFFFFFu * 2u; 4294967294
            0xFFFFFFFFu * 0xFFFFFFFFu; 1
            1 << 31; -2147483648
            1u << 31; 2147483648
            -8 >> 2; -2
            0x80000000u >> 2; 536870912
            0xFFFFFFFFu >> 31; 1
            7 / 3; 2
            -7 / 3; -2
            7 / -3; -2
            -7 / -3; 2
            7 % 3; 1
            0xFFFFFFFFu / 2u; 2147483647
            0xFFFFFFFFu % 2u; 1
            1u + 2; 3
            1 + 2u; 3
            0u - 1; 4294967295
            -1 + 1u; 0
            1 << 2u; 4
            1u << 2; 4
            -1 >> 1u; -1
            0xFFFFFFFFu >> 1; 2147483647
            int(5u); 5
            uint(-1); 4294967295
            int(true); 1
            int(false); 0
            int(0xFFFFFFFFu); -1
            uint(0x80000000); 2147483648
            abs(-10); 10
            sign(-10); -1
            sign(0); 0
            sign(10); 1
            min(3, 5); 3
            max(3, 5); 5
            clamp(10, 0, 5); 5
            clamp(-3, 0, 5); 0
            clamp(3, 0, 5); 3
            min(max(2, 4), 3); 3
            abs(sign(-20) - 2); 3
            min(-1, 1u); 1
            max(-1, 1u); 4294967295
            clamp(-1, 0u, 10u); 10
            min(3u, 5); 3
            max(3, 5u); 5
            clamp(5u, 0, 10); 5
            true ? 1 : 2 + 3; 1
            false ? 1 : 2 + 3; 5
            true || false ? 7 : 8; 7
            false ? 1 : true ? 2 : 3; 2
            true ? false ? 1 : 2 : 3; 2
            (false ? true : false) ? 1 : 2; 2
            true ? 10 : 1 / 0; 10
            false ? 1 / 0 : 20; 20
            true ? 1 : uint(2); 1
            false ? 1 : uint(2); 2
            true ? 1u : -1; 1
            false ? 1u : -1; 4294967295
            (true ? -1 : 1u) >> 1; 2147483647
            uint(true ? -1 : 1u) / 2u; 2147483647
            int((true ? -1 : 1u) < 0); 0
            int((1 << 31u) < 0); 1
            int((min(3u, 5) - 4) > 0); 1
            """)
    void testIntegers(String source, long expected) {
        assertEquals(expected, EMPTY.expression(source).resolved().orElseThrow());
    }

    @ParameterizedTest(name = "Boolean {0} = {1}")
    @CsvSource(delimiter = ';', textBlock = """
            true; 1
            false; 0
            !true; 0
            !!true; 1
            5 == 2 + 3; 1
            3 > 2 && 2 > 1; 1
            -1 < 1u; 0
            -1 > 1u; 1
            -1 == 0xFFFFFFFFu; 1
            0xffffffff < 0; 1
            1 != 2; 1
            2 <= 2; 1
            3 >= 4; 0
            true == false; 0
            true ^^ false; 1
            true ^^ true; 0
            bool(0); 0
            bool(-1); 1
            bool(1u << 31); 1
            true ? false : true; 0
            false && (1 / 0 == 0); 0
            true || (1 / 0 == 0); 1
            """)
    void testBooleans(String source, long expected) {
        assertTrue(EMPTY.expression(source).resolved().isEmpty());
        assertEquals(expected, expressions("const bool result = " + source + ";").constant("result").orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "4294967296u",
            "0xfffffffff",
            "999999999999999999999999999999999",
            "08",
            "0x",
            "123abc",
            "--5",
            "++1",
            "1++",
            "4++2",
            "!0",
            "~true",
            "-true",
            "+true",
            "1 << 32",
            "1 << -1",
            "1u >> 32u",
            "10 / 0",
            "10 % 0",
            "(-2147483647 - 1) / -1",
            "7 % -3",
            "-7 % 3",
            "-7 % -3",
            "abs(-2147483647 - 1)",
            "1 + true",
            "true + false",
            "true & false",
            "true == 1",
            "1 && 2",
            "1 ^^ 0",
            "true << 1",
            "!1u",
            "1 ? 2 : 3",
            "true ? 1 : false",
            "int()",
            "int(1, 2)",
            "min(1)",
            "max(1, 2, 3)",
            "clamp(1, 2)",
            "clamp(1, 10, 0)",
            "clamp(5u, -1, 10)",
            "abs(1u)",
            "sign(true)",
            "min(true, false)",
            "min(1, true)",
            "unknown(1)",
            "true && (1 / 0 == 0)",
            "false || (1 / 0 == 0)",
            "true ^^ (1 / 0 == 0)",
            "true ? 1 / 0 : 20",
            "false ? 10 : 1 / 0",
            "true ? 5 : unknownIdentifier",
            "false && unknown(1)",
            "true || (missing == 0)",
            "true ? 1 : abs(1u)",
            "1 +",
            "* 2",
            "1 2",
            "(1 + 2",
            "1 + 2)",
            "true ? 1",
            "true ? : 2",
            "true ? 1 : ",
            "1, 2",
            "1.0",
            "1e3",
            "1 /* comment */",
            "a[0]",
            "vec2(1)",
            "1 = 2"})
    void testUnresolved(String source) {
        var expression = assertDoesNotThrow(() -> EMPTY.expression(source));

        assertEquals(source, expression.expression());
        assertTrue(expression.resolved().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            " 1 + 2 ",
            "\n1 + 2\n",
            "1+2",
            "1  +   2",
            "( 1 + 2 )"})
    void testWhitespace(String source) {
        assertEquals(3, EMPTY.expression(source).requireValue());
        assertEquals(source, EMPTY.expression(source).expression());
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', textBlock = """
            int; 10; 10
            uint; 10u; 10
            uint; 10; 10
            uint; -1; 4294967295
            bool; true; 1
            bool; false; 0
            int; int(10u); 10
            uint; uint(-1); 4294967295
            int; 1 + 2 * 3; 7
            int; min(10, 20); 10
            bool; bool(1); 1
            """)
    void testInitializers(String type, String initializer, long expected) {
        var expressions = expressions("const " + type + " A = " + initializer + ";");

        assertEquals(expected, expressions.constant("A").orElseThrow());
        assertEquals(expected, Objects.requireNonNull(expressions.constants().get("A")).requireValue());
    }

    @Test
    void testDeclOrder() {
        var expressions = expressions("""
                const int A = 2, B = A + 3, C = B * 2;
                const uint U = -1;
                const bool COMPARE = U > C;
                const int D = int(COMPARE) + C;
                """);

        assertEquals(List.of("A", "B", "C", "U", "COMPARE", "D"), expressions.constants().keySet().asList());
        assertEquals(2, expressions.constant("A").orElseThrow());
        assertEquals(5, expressions.constant("B").orElseThrow());
        assertEquals(10, expressions.constant("C").orElseThrow());
        assertEquals(11, expressions.constant("D").orElseThrow());
        assertEquals(4294967295L, expressions.expression("true ? U : -1").requireValue());
        assertTrue(expressions.constant("missing").isEmpty());
    }

    @Test
    void testBadDecls() {
        var expressions = expressions("""
                const int A = UNKNOWN, B = A + 1, C = 42;
                const int FORWARD = LATER;
                const int LATER = 5;
                const int SELF = SELF + 1;
                const int WRONG_INT = 1u;
                const bool WRONG_BOOL = 1;
                const int DIVISION = 1 / 0;
                const int AFTER = LATER + C;
                """);

        for (String name : List.of("A", "B", "FORWARD", "SELF", "WRONG_INT", "WRONG_BOOL", "DIVISION")) {
            assertTrue(expressions.constant(name).isEmpty(), name);
            assertTrue(expressions.constants().containsKey(name), name);
            assertTrue(Objects.requireNonNull(expressions.constants().get(name)).resolved().isEmpty(), name);
        }

        assertEquals("UNKNOWN", Objects.requireNonNull(expressions.constants().get("A")).expression());
        assertEquals("A+1", Objects.requireNonNull(expressions.constants().get("B")).expression());
        assertEquals(42, expressions.constant("C").orElseThrow());
        assertEquals(47, expressions.constant("AFTER").orElseThrow());
    }

    @Test
    void testSkipUnsupported() {
        var expressions = expressions("""
                int nonConstant = 3;
                uniform int uniformValue;
                const float floating = 3.0;
                const int array[2] = int[2](1, 2);
                const int[2] typedArray = int[2](1, 2);
                const int uninitialized;
                const int GLOBAL = 7;
                void helper() { const int GLOBAL = 99; const int LOCAL = 8; }
                const int AFTER = GLOBAL + 1;
                """);

        assertEquals(List.of("GLOBAL", "AFTER"), expressions.constants().keySet().asList());
        assertEquals(7, expressions.constant("GLOBAL").orElseThrow());
        assertEquals(8, expressions.constant("AFTER").orElseThrow());
    }

    @Test
    void testDupDecl() {
        assertThrows(IllegalArgumentException.class,
            () -> expressions("const int A = 1; const int A = 2;"));
    }
}
