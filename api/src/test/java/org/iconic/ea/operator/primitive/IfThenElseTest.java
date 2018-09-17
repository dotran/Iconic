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
 * Test class for {@link IfThenElse}
 * @author Jack Newley
 */

public class IfThenElseTest {


    @DisplayName("Test GreaterThan using doubles")
    @MethodSource("doubleListProvider")
    @ParameterizedTest
    void addDoublesTest(final List<Double> args, final double expected) {
        final FunctionalPrimitive<Double, Double> ifThenElse = new IfThenElse();
        final double delta = 0.001d;
        final double actual = ifThenElse.apply(args);

        assertEquals(expected, actual, delta);
    }

    /**
     * <p>
     * Returns a stream of double n-tuples, if the first argument is > 0, the last tuple is the second argument,
     * or the third argument if otherwise
     * </p>
     *
     * @return a stream of double n-tuples
     */
    private static Stream<Arguments> doubleListProvider() {
        return Stream.of(
                Arguments.of(Arrays.asList(0.0,2.0,3.0), 3.0),
                Arguments.of(Arrays.asList(1.0,-1.0,-2.0), -1.0),
                Arguments.of(Arrays.asList(-1.0,1.0,-2.0), -2.0)
        );
    }
}
