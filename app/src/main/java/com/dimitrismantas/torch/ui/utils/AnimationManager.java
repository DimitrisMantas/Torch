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
package com.dimitrismantas.torch.ui.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.dimitrismantas.torch.R;

public final class AnimationManager {
    private static final long SHORT_ANIMATION_DURATION = 250L;
    private static final long LONG_ANIMATION_DURATION = 1000L;

    private AnimationManager() {
    }

    public static void fade(final View view, final FadeMode mode) {
        if (mode == FadeMode.IN) {
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1.0F).setDuration(SHORT_ANIMATION_DURATION);
        } else {
            view.animate().alpha(0.0F).setDuration(SHORT_ANIMATION_DURATION);
            view.setVisibility(View.GONE);
        }
    }

    public static void slide(final View view, final SlideMode mode, final Context appContext) {
        if (mode == SlideMode.DOWN) {
            view.setVisibility(View.VISIBLE);
            view.startAnimation(AnimationUtils.loadAnimation(appContext, R.anim.anim_slide_down));
        } else {
            view.startAnimation(AnimationUtils.loadAnimation(appContext, R.anim.anim_slide_up));
            view.setVisibility(View.GONE);
        }
    }

    public static void spin(final View view) {
        view.setVisibility(View.VISIBLE);
        final RotateAnimation animation = new RotateAnimation(0, 360F, Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(LONG_ANIMATION_DURATION);
        animation.setRepeatCount(Animation.INFINITE);
        view.startAnimation(animation);
    }

    public static void makeDisappear(final View v) {
        v.clearAnimation();
        v.setVisibility(View.GONE);
    }

    public enum SlideMode {
        UP, DOWN;
    }

    public enum FadeMode {
        IN, OUT
    }
}
