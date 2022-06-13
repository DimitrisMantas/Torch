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

import static com.dimitrismantas.torch.ui.map.markers.extensions.DragGestureHandler.DRAG;
import static com.dimitrismantas.torch.ui.map.markers.extensions.DragGestureHandler.DROP;
import static com.dimitrismantas.torch.ui.map.markers.extensions.DragGestureHandler.PICK_UP;

import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerInterface;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;

import java.util.List;

public class ExtendedItemizedLayer extends ItemizedLayer implements GestureListener {
    private final DragManager mDragManager;

    public ExtendedItemizedLayer(Map map, List<MarkerInterface> markerItems, MarkerSymbol markerSymbol, OnItemGestureListener<MarkerInterface> listener) {
        super(map, markerItems, markerSymbol, listener);
        mDragManager = new DragManager(this, map);
    }

    @Override
    protected boolean activateSelectedItems(MotionEvent event, ActiveItem task) {
        return super.activateSelectedItems(event, task);
    }

    @Override
    public boolean onGesture(Gesture gesture, MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        if (gesture == PICK_UP) {
            return mDragManager.pickUp(event, getGeoPoint(event));
        } else if (gesture == DRAG) {
            return mDragManager.dragTo(getGeoPoint(event));
        } else if (gesture == DROP) {
            return mDragManager.dropAt(getGeoPoint(event));
        } else {
            mDragManager.noDrag();
        }
        return super.onGesture(gesture, event);
    }

    private GeoPoint getGeoPoint(MotionEvent event) {
        return map().viewport().fromScreenPoint(event.getX(), event.getY());
    }

    protected List<MarkerInterface> getMarkerItems() {
        return mItemList;
    }
}