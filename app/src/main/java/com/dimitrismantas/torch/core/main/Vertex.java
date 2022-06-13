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
package com.dimitrismantas.torch.core.main;

import java.util.ArrayList;
import java.util.List;

public final class Vertex {
    private final List<Edge> edges = new ArrayList<>();
    private final int lbl;
    private final float lat;
    private final float lon;
    private final int actualCostFromSource = 0;
    private final int pqKey = 0;
    private final short numInitialized = 0;
    private final int predecessorLabel = -1;

    Vertex(final int lbl, final float lat, final float lon) {
        this.lbl = lbl;
        this.lat = lat;
        this.lon = lon;
    }

    public int getPqKey() {
        return pqKey;
    }

    public int getLabel() {
        return this.lbl;
    }

    public float getLatitude() {
        return this.lat;
    }

    public float getLongitude() {
        return this.lon;
    }

    public short getNumInitialized() {
        return this.numInitialized;
    }

    public int getPredecessorLabel() {
        return this.predecessorLabel;
    }

    public int getActualCostFromSource() {
        return this.actualCostFromSource;
    }

    public List<Edge> getOutEdges() {
        return this.edges;
    }

    public void addOutboundEdge(final Edge edge) {
        this.edges.add(edge);
    }
}
