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
package com.dimitrismantas.torch.utils.data;

import com.dimitrismantas.torch.core.main.Path;
import com.dimitrismantas.torch.core.main.engine.AStar;
import com.dimitrismantas.torch.core.main.utils.NearestNeighborSearch;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedGraph;
import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;

import org.oscim.core.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public final class DataManager {
    private static DeserializedGraph graph;
    private static Path route;
    private static NearestNeighborSearch nearestNeighborSearch;
    private static AStar aStar;

    private DataManager() {
    }

    public static List<GeoPoint> toGeoPoints(final List<DeserializedVertex> vertices) {
        final List<GeoPoint> geoPoints = new ArrayList<>(vertices.size());
        for (final DeserializedVertex vertex : vertices) {
            geoPoints.add(new GeoPoint(vertex.lat(), vertex.lon()));
        }
        return geoPoints;
    }

    public static NearestNeighborSearch getNearestNeighborSearch() {
        return nearestNeighborSearch;
    }

    public static void setNearestNeighborSearch(NearestNeighborSearch nearestNeighborSearch) {
        DataManager.nearestNeighborSearch = nearestNeighborSearch;
    }

    public static AStar getaStar() {
        return aStar;
    }

    public static void setaStar(AStar aStar) {
        DataManager.aStar = aStar;
    }

    public static DeserializedGraph getGraph() {
        return graph;
    }

    public static void setGraph(final DeserializedGraph graph) {
        DataManager.graph = graph;
    }

    public static boolean areRoutingServicesAvailable() {
        return aStar != null;
    }

    public static Path getRoute() {
        return route;
    }

    public static void setRoute(final Path route) {
        DataManager.route = route;
    }
}
