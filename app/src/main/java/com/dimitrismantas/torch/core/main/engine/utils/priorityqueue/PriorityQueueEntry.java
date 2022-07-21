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

package com.dimitrismantas.torch.core.main.engine.utils.priorityqueue;

public class PriorityQueueEntry {
    private final int key;
    private final int vertexLabel;

    public PriorityQueueEntry(final int key, final int vertexLabel) {
        this.key = key;
        this.vertexLabel = vertexLabel;
    }

    public int getKey() {
        return key;
    }

    public int getVertexLabel() {
        return vertexLabel;
    }
}
