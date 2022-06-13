/*
 * Torch is an Android application for the optimal routing of offline
 * mobile devices.
 * Copyright (C) 2021-2022  DIMITRIS(.)MANTAS(@outlook.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.dimitrismantas.torch.core.math;

/**
 * A collection of supplemental mathematical operations used by Torch that are not directly supported by the standard {@link Math} package.
 *
 * @author Dimitris Mantas
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SupplementalMath {
    /**
     * The inverse value of the common logarithm of the number two.
     */
    private static final double INV_LOG2 = 1.4426950408889633870046509400709D;

    // This class is static.
    private SupplementalMath() {
    }

    /**
     * Computes the haversine of a given angle.
     *
     * @param theta The angle in radians.
     * @return The haversine of the angle.
     */
    public static double hav(final double theta) {
        return pow2(Math.sin(0.5D * theta));
    }

    /**
     * Computes the inverse haversine of a given angle.
     *
     * @param theta The angle in radians.
     * @return The haversine of the angle.
     */
    public static double ahav(final double theta) {
        return 2.0D * Math.asin(Math.sqrt(theta));
    }

    /**
     * Computes the exponential of given number using the number two as a base.
     *
     * @param x The number
     * @return The exponential of the number
     */
    public static double exp2(final double x) {
        return Math.pow(2.0D, x);
    }

    /**
     * Computes the binary logarithm of a given number.
     *
     * @param x The number.
     * @return The binary logarithm of the number.
     */
    public static double log2(final double x) {
        return Math.log(x) * INV_LOG2;
    }

    /**
     * Computes the square of a given number.
     *
     * @param x The number.
     * @return The square.
     */
    public static double pow2(final double x) {
        return Math.pow(x, 2D);
    }

    /**
     * Determines if two given numbers of type {@code double} are <i>almost</i> equal to each other.
     * <p>
     * This method is not transitive, meaning that if {@code almostEquals(a, b)} and {@code almostEquals(b, c)}, then {@code almostEquals(a, c)} is not necessarily true.
     *
     * @param a The first number.
     * @param b The second number.
     * @return {@code true} of the numbers are almost equal; {@code false} otherwise.
     * @apiNote In contrast to other similar methods, {@code NaN == NaN} and {@code -0 == 0}.
     */
    public static boolean almostEquals(final double a, final double b) {
        final double eps = Math.max(Math.ulp(a), Math.ulp(b));
        return a == b || Math.abs(a - b) <= eps;
    }
}
