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
package com.dimitrismantas.torch.ui.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;

import com.dimitrismantas.torch.ui.map.markers.extensions.DragGestureHandler;

import org.oscim.android.MapView;
import org.oscim.map.Map;
import org.oscim.utils.Parameters;

public class ExtendedMapView extends MapView {
    private DragGestureHandler dragGestureHandler;

    public ExtendedMapView(Context applicationContext) {
        super(applicationContext);
    }

    public ExtendedMapView(Context applicationContext, AttributeSet attributeSet) {
        super(applicationContext, attributeSet);
        if (!Parameters.MAP_EVENT_LAYER2) {
            dragGestureHandler = new DragGestureHandler(mMap);
            mGestureDetector = new GestureDetector(applicationContext, dragGestureHandler);
            mGestureDetector.setOnDoubleTapListener(dragGestureHandler);
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent motionEvent) {
        if (!isClickable()) {
            return false;

        }
        if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
            if (dragGestureHandler.isScrollingEnabled()) {
                dragGestureHandler.setScrolling(false);
                return ((Map) mMap).handleGesture(DragGestureHandler.DROP, mMotionEvent.wrap(motionEvent));
            }
        }
        return super.onTouchEvent(motionEvent);
    }
}
