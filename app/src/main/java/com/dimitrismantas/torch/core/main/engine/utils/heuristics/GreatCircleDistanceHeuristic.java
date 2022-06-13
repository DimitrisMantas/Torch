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
package com.dimitrismantas.torch.core.main.engine.utils.heuristics;

import com.dimitrismantas.torch.core.math.HaversineFormula;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;

/**
 * A heuristic that is used to estimate the great circle distance to a reference vertex.
 *
 * @author Dimitris Mantas
 * @version 1.0.0
 * @since 1.0.0
 */
public final class GreatCircleDistanceHeuristic extends AbstractHeuristic {
    public GreatCircleDistanceHeuristic(final DeserializedVertex refVertex) {
        super(refVertex);
    }

    @Override
    public int calc(final DeserializedVertex vertex) {
      return (int) HaversineFormula.run(vertex.lat(), vertex.lon(), refVertex.lat(), refVertex.lon());
    }
}
