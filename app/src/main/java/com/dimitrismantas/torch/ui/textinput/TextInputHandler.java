/*
 * Torch is a model, open-source Android application for optimal routing
 * in offline mobile devices.
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
package com.dimitrismantas.torch.ui.textinput;

import android.text.Editable;

/**
 * A utility class to hep handle text input from the user.
 *
 * @author Dimitris Mantas
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TextInputHandler {
    // This class is not meant to be instantiated.
    private TextInputHandler() {
    }

    /**
     * Determines if a given piece of text received from an {@code EditText} represents a vertex label.
     *
     * @param editable The text to check.
     * @return {@code true} if the text represents a vertex label; {@code false} otherwise.
     * @apiNote This method is meant to be used mostly for debugging purposes since the user is not expected to know the label of each vertex.
     * @see #isCoordinatePair(Editable)
     * @since 1.0.0
     */
    private static boolean isVertexLabel(final Editable editable) {
        if (!isInteger(editable)) {
            return false;
        }
        final int intRepresentation = getIntegerRepresentation(editable);
        // TODO - Replace 8436216 with an appropriate field.
        return 0 <= intRepresentation && intRepresentation <= 8436216;
    }

    /**
     * Determines if a given piece of text received from an {@code EditText} represents a pair of geographic coordinates.
     *
     * @param text The text to check.
     * @return {@code true} if the text represents a coordinate pair; {@code false} otherwise.
     * @implNote A coordinate pair is defined as a single pair of comma separated numbers, with a single spaces between them.
     * @see #isVertexLabel(Editable)
     * @since 1.0.0
     */
    private static boolean isCoordinatePair(final Editable text) {
        final String strRepresentation = text.toString();
        final long numCommas = strRepresentation.chars().filter(c -> c == ',').count();
        // The text should contain one and only one comma.
        if (numCommas != 1) {
            return false;
        }
        final String[] substrings = strRepresentation.replaceAll("\\s+", "").split(",");
        // The text should contain exactly two substrings when split at each occurrence of a comma.
        if (substrings.length != 2) {  // This check might be unnecessary.
            return false;
        }
        // Each substring must represent a number.
        for (final String substring : substrings) {
            if (!isNumber(substring)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the pair of geographic coordinates represented by a given piece of text received from an {@code EditText}.
     *
     * @param text The text to process.
     * @return The latitude and longitude represented by the text.
     * @apiNote Only use this method after {@link #isCoordinatePair(Editable)} is called, since it is assumed that the text indeed represents a coordinate pair.
     * @see #isCoordinatePair(Editable)
     * @since 1.0.0
     */
    public static double[] getCoordinates(final Editable text) {
        final String[] substrings = text.toString().replaceAll("\\s+", "").split(",");
        final double[] coordinates = new double[2];
        for (int i = 0; i < substrings.length; i++) {
            coordinates[i] = Double.parseDouble(substrings[i]);
        }
        return coordinates;
    }

    /**
     * Returns the integer representation of a given piece of text received from an {@code EditText}.
     *
     * @param text The text to process.
     * @return The integer represented by the text.
     * @apiNote Only use this method after {@link #isInteger(Editable)} is called since it is assumed that the text indeed represents an integer.
     * @see #isInteger(Editable)
     * @since 1.0.0
     */
    public static int getIntegerRepresentation(final Editable text) {
        return Integer.parseInt(text.toString());
    }

    /**
     * Determines if the string representation of a given piece of text received from an {@code EditText} represents a number.
     *
     * @param string The string to process.
     * @return {@code true} if the string represents a number; {@code false} otherwise.
     * @see #isInteger(Editable)
     * @since 1.0.0
     */
    private static boolean isNumber(final String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Determines if a given piece of text received from an {@code EditText} represents an integer.
     *
     * @param editable The text to process.
     * @return {@code true} if the text represents an integer; {@code false} otherwise.
     * @apiNote This method does not make use of {@link #isNumber(String)} nor does it need to.
     * @see #isNumber(String)
     * @since 1.0.0
     */
    private static boolean isInteger(final Editable editable) {
        try {
            Integer.parseInt(editable.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static OutputFlag processEditable(final Editable editable) {
        if (isCoordinatePair(editable)) {
            return OutputFlag.COORDINATE_PAIR;
        } else if (isVertexLabel(editable)) {
            return OutputFlag.VERTEX_LABEL;
        } else if (editable.toString().equals("")) {
            return OutputFlag.EMPTY_STRING;
        } else {
            return OutputFlag.INVALID_STRING;
        }
    }

    /**
     * The various possible output flags after processing a given piece of text received from an {@code EditText}.
     *
     * @author Dimitris Mantas
     * @version 1.0.0
     * @since 1.0.0
     */
    public enum OutputFlag {
        /**
         * The text represents a pair of geographic coordinates.
         *
         * @see #VERTEX_LABEL
         * @since 1.0.0
         */
        COORDINATE_PAIR,
        /**
         * The text represents a vertex label.
         *
         * @see #COORDINATE_PAIR
         * @since 1.0.0
         */
        VERTEX_LABEL,
        /**
         * The text represents an empty string.
         *
         * @see #INVALID_STRING
         * @since 1.0.0
         */
        EMPTY_STRING,
        /**
         * The text represents an invalid string.
         *
         * @apiNote An invalid sting is defined as a string that is not empty, but does not represent a a pair of geographic coordinates or a vertex label.
         * @see #EMPTY_STRING
         * @since 1.0.0
         */
        INVALID_STRING
    }
}
