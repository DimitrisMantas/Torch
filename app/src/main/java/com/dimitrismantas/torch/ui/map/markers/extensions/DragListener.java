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
package com.dimitrismantas.torch.ui.map.markers.extensions;

import com.dimitrismantas.torch.ui.map.markers.ExtendedMarkerItem;

import org.oscim.core.GeoPoint;

public interface DragListener {
    void pickUp(ExtendedMarkerItem item, GeoPoint eventLocation);

    void drag(ExtendedMarkerItem item, GeoPoint eventLocation);

    void drop(ExtendedMarkerItem item, GeoPoint eventLocation);
}
