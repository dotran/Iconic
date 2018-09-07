package org.iconic.ea.operator.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link NaturalLog}
 *
 * @author Jack Newley
 */

public class NaturalLogTest {
    @DisplayName("Test NaturalLog using doubles")
    @MethodSource("doubleListProvider")
    @ParameterizedTest
    void addDoublesTest(final List<Double> args, final double expected) {
        final FunctionalPrimitive<Double, Double> naturalLog = new NaturalLog();
        final double delta = 0.001d;
        final double actual = naturalLog.apply(args);
        assertEquals(expected, actual, delta);
    }

    /**
     * <p>
     * Returns a stream of double n-tuples, where the last member of the tuple is the natural log of the first argument
     * </p>
     *
     * @return a stream of double n-tuples
     */
    private static Stream<Arguments> doubleListProvider() {
        return Stream.of(
                Arguments.of(Collections.singletonList(2.d), 0.6931471805599453),
                Arguments.of(Collections.singletonList(0.d), NEGATIVE_INFINITY),
                Arguments.of(Collections.singletonList(Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY),
                Arguments.of(Collections.singletonList(-1.d), NaN)

        );
    }
}