/**
 * Copyright (C) 2018 Iconic
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.iconic.ea.operator.primitive;

import static java.lang.Math.abs;

public class Root extends ArithmeticPrimitive<Number> {
    public Root() {
        super(
                args -> {
                    final double delta = 0.001;
                    if (args.get(0) >= 0 + delta && args.get(0) >= 0 - delta) {
                        return Math.pow(args.get(0), 1 / args.get(1));
                    } else if (args.get(0) < 0 - delta) {
                        double result = Math.pow(abs(args.get(0)), 1 / args.get(1));

                        return (args.get(1) % 2 == 0) ? result : -result;
                    }
                    return Double.NaN;
                },
                2, "ROOT", "Returns the b-th root of a if a is greater than 0, NaN otherwise.", 5
        );
    }
}
