package org.iconic.ea.operator.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link Xor}
 *
 * @author Jack Newley
 */

public class XorTest {

    @DisplayName("Test Xor using doubles")
    @MethodSource("doubleListProvider")
    @ParameterizedTest
    void subtractDoublesTest(final List<Double> args, final double expected) {
        final FunctionalPrimitive<Double, Double> xor = new Xor();
        final double delta = 0.001d;
        final double actual = xor.apply(args);

        assertEquals(expected, actual, delta);
    }

    /**
     * <p>
     * Returns a stream of double n-tuples, returns 1 if one of the two arguments are greater than 0,
     * 0 if otherwise
     * </p>
     *
     * @return a stream of double n-tuples
     */
    private static Stream<Arguments> doubleListProvider() {
        return Stream.of(
                Arguments.of(Arrays.asList(0.d, 0.d), 0.d),
                Arguments.of(Arrays.asList(10.d, 0.d), 1.d),
                Arguments.of(Arrays.asList(10.d, 10.d), 0.d),
                Arguments.of(Arrays.asList(-1.d, 10.d), 1.d)

        );
    }
}
