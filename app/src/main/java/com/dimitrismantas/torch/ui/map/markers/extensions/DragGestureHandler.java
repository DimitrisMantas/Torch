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

import android.view.MotionEvent;

import org.oscim.android.input.AndroidMotionEvent;
import org.oscim.android.input.GestureHandler;
import org.oscim.event.Gesture;
import org.oscim.map.Map;

public class DragGestureHandler extends GestureHandler {
    public static final Gesture PICK_UP = new Gesture() {
    };
    public static final Gesture DRAG = new Gesture() {
    };
    public static final Gesture DROP = new Gesture() {
    };
    private final Map map;
    private final AndroidMotionEvent motionEvent;
    private boolean isScrollingEnabled;

    public DragGestureHandler(final Map map) {
        super(map);
        motionEvent = new AndroidMotionEvent();
        this.map = map;
    }

    public boolean isScrollingEnabled() {
        return isScrollingEnabled;
    }

    public void setScrolling(boolean scrollingEnabled) {
        this.isScrollingEnabled = scrollingEnabled;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        map.handleGesture(PICK_UP, motionEvent.wrap(e));
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        isScrollingEnabled = true;
        return map.handleGesture(DRAG, motionEvent.wrap(e2));
    }
}
