/**
 * Copyright 2018 Iconic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.iconic.ea.operator.objective.error;

import java.util.List;

/**
 * <p>Defines a functional interface for an error function
 *
 * <p>
 * An error function is used to calculate the error between an evaluated output and an expected output.
 *
 */
@FunctionalInterface
public interface ErrorFunction {
    /**
     * <p>Applies this error function to the given results
     *
     * @param calculated The calculated results
     * @param expected The expected results
     * @return the amount of error between the calculated and expected results
     */
    double apply(final List<Double> calculated, final List<Double> expected);
}
