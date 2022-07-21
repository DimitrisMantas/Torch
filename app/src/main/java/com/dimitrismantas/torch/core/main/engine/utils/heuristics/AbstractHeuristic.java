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
 * A contract for a generic heuristic, which all heuristics must follow.
 *
 * @author Dimitris Mantas
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AbstractHeuristic {
    /**
     * The vertex that is used as a reference when calculating the value of the heuristic corresponding to a given vertex.
     */
    protected final DeserializedVertex refVertex;

    public AbstractHeuristic(final DeserializedVertex refVertex) {
        this.refVertex = refVertex;
    }

    /**
     * Calculates the value of the heuristic that corresponds to a given vertex.
     *
     * @param vertex The vertex to be used during the calculation.
     * @return The value of the heuristic that corresponds to this vertex.
     */
    public abstract int calc(final DeserializedVertex vertex);
}
