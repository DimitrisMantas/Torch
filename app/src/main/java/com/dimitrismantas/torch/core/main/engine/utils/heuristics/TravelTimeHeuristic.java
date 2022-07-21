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
package com.dimitrismantas.torch.core.main.engine.utils.heuristics;

import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;

/**
 * A heuristic that is used to estimate the travel time along the great circle to a reference vertex.
 *
 * @author Dimitris Mantas
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TravelTimeHeuristic extends AbstractHeuristic {
    /**
     * The inverse of the supposed minimum speed for which this heuristic is admissible in seconds per meter. The suitability of this value must be confirmed through testing.
     *
     * @since 1.0.0
     */
    // This value corresponds to 100 km/h (i.e., the Q3 + 1.5 * IQR of all edge speeds).
    private static final double INVERSE_MIN_SPEED = 0.036d;
    private final GreatCircleDistanceHeuristic distanceFrom;

    public TravelTimeHeuristic(final DeserializedVertex refVertex) {
        super(refVertex);
        // The reference latitude and longitude are read two times. This is inefficient.
        distanceFrom = new GreatCircleDistanceHeuristic(refVertex);
    }

    @Override
    public int calc(final DeserializedVertex vertex) {
       return (int) (distanceFrom.calc(vertex) * INVERSE_MIN_SPEED);
    }
}
