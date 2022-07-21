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
package com.dimitrismantas.torch.core.main.utils;

import com.dimitrismantas.torch.core.utils.annotations.O;
import com.dimitrismantas.torch.core.utils.annotations.WGS84;
import com.dimitrismantas.torch.core.math.HaversineFormula;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedGraph;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;

import java.util.Arrays;

/**
 * An implementation of a linear nearest neighbor search algorithm.
 *
 * @author Dimitris Mantas
 * @version 1.0.0
 * @since 1.0.0
 */
@O("log(n)")
public final class NearestNeighborSearch {
    private final DeserializedGraph graph;

    public NearestNeighborSearch(final DeserializedGraph graph) {
        this.graph = graph;
    }

    /**
     * Finds the nearest neighbor to a given point on the surface of the Earth.
     *
     * @param lat The latitude of the point to be used during the search, in decimal degrees.
     * @param lon The latitude of the point to be used during the search, in decimal degrees.
     * @return The nearest neighbor to this point.
     */
    @WGS84
    public DeserializedVertex run(final double lat, final double lon) {
        // This is equivalent to setting match equal to null, since graph.vertices(-1) will throw a NullPointerException.
        int matchIndex = -1;
        double minDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < graph.verticesLength(); i++) {
            final DeserializedVertex matchCandidate = graph.vertices(i);
            final double currentDistance = HaversineFormula.run(lat, lon, matchCandidate.lat(), matchCandidate.lon());
            if (minDistance > currentDistance) {
                minDistance = currentDistance;
                matchIndex = i;
            }
        }
        return graph.vertices(matchIndex);
    }

    /**
     * Finds the nearest neighbor to a given point on the surface of the Earth.
     *
     * @param lat The latitude of the point to be used during the search, in decimal degrees.
     * @param lon The latitude of the point to be used during the search, in decimal degrees.
     * @return The nearest neighbor to this point.
     * @implNote This method uses the same binary search algorithm as the {@link Arrays} class.
     */
    @WGS84
    public DeserializedVertex run1(final double lat, final double lon){
        final double key = HaversineFormula.run(lat,lon,36.4025066,20.0121613);

         int lo = 0;
         int hi = graph.verticesLength() - 1;

        while (lo <= hi) {
            final int mid = (lo + hi) >>> 1;

            final DeserializedVertex midVertex = graph.vertices(mid);
            final double midVal = HaversineFormula.run(midVertex.lat(),midVertex.lon(),36.4025066,20.0121613);

            if (midVal < key)
                lo = mid + 1;
            else if (midVal > key)
                hi = mid - 1;
            else
                // The search term corresponds to a vertex.
                return midVertex;
        }
        // The search term is does not correspond to a vertex.
        final DeserializedVertex loVertex = graph.vertices(lo);
        double loVal = HaversineFormula.run(loVertex.lat(),loVertex.lon(),36.4025066,20.0121613);

        final DeserializedVertex hiVertex = graph.vertices(hi);
        double hiVal = HaversineFormula.run(hiVertex.lat(),hiVertex.lon(),36.4025066,20.0121613);

        if (loVal<hiVal)
            return loVertex;
            else
                return hiVertex;
    }
}
