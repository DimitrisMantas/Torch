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

import com.dimitrismantas.torch.core.utils.serialization.DeserializedVertex;

import java.util.ArrayList;
import java.util.List;

public final class Path {
    private final List<DeserializedVertex> endpoints = new ArrayList<>();
    private int length;
    private int travelTime;
    private int N;

    public Path() {
    }

    public List<DeserializedVertex> getEndpoints() {
        return endpoints;
    }

    public void addEndpoint(final DeserializedVertex enpoint) {
        this.endpoints.add(0, enpoint);
    }

    public int getLength() {
        return length;
    }

    public void incrementLength(final int length) {
        this.length += length;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public void incrementTravelTime(final int travelTime) {
        this.travelTime += travelTime;
    }

    public int getN() {
        return N;
    }

    public void setN(final int n) {
        this.N = n;
    }
}
