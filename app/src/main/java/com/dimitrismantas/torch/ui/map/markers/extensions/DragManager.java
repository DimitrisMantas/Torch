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
package com.dimitrismantas.torch.ui.map.markers.extensions;

import com.dimitrismantas.torch.ui.map.markers.ExtendedMarkerItem;

import org.oscim.core.GeoPoint;
import org.oscim.event.MotionEvent;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerLayer;
import org.oscim.map.Map;

class DragManager {
    private final ExtendedItemizedLayer layer;
    private final DragListener listener;
    private ExtendedMarkerItem item;

    public DragManager(ExtendedItemizedLayer layer, Map map) {
        this.layer = layer;
        this.listener = createDragItemAndRedrawListener(layer, map);
    }

    private DragListener createDragItemAndRedrawListener(final MarkerLayer markerLayer, final Map map) {
        return new DragListener() {
            @Override
            public void pickUp(ExtendedMarkerItem item, GeoPoint eventLocation) {
                item.getDragListener().pickUp(item, eventLocation);
                updateLocationOfMarkerItemAndRedraw(item, eventLocation);
            }

            @Override
            public void drag(ExtendedMarkerItem item, GeoPoint eventLocation) {
                item.getDragListener().drag(item, eventLocation);
                updateLocationOfMarkerItemAndRedraw(item, eventLocation);
            }

            @Override
            public void drop(ExtendedMarkerItem item, GeoPoint eventLocation) {
                item.getDragListener().drop(item, eventLocation);
                updateLocationOfMarkerItemAndRedraw(item, eventLocation);
            }

            private void updateLocationOfMarkerItemAndRedraw(MarkerItem markerItem, GeoPoint location) {
                markerItem.geoPoint = location;
                markerLayer.populate();
                map.render();
            }
        };
    }

    public boolean pickUp(MotionEvent event, final GeoPoint geoPoint) {
        item = null;
        return layer.activateSelectedItems(event, index -> {
            item = (ExtendedMarkerItem) layer.getMarkerItems().get(index);
            listener.pickUp(item, geoPoint);
            return true;
        });
    }

    public boolean dragTo(GeoPoint geoPoint) {
        if (item == null) {
            return false;
        }
        listener.drag(item, geoPoint);
        return true;
    }

    public boolean dropAt(GeoPoint geoPoint) {
        if (item == null) {
            return false;
        }
        listener.drop(item, geoPoint);
        return true;
    }

    public void noDrag() {
        item = null;
    }
}