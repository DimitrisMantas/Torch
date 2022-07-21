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
package com.dimitrismantas.torch.ui.routing;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

import com.dimitrismantas.torch.R;
import com.dimitrismantas.torch.core.main.Path;
import com.dimitrismantas.torch.core.main.engine.AStar;

public final class RouteAttributeStringBuilder {
    private static final int TO_KILO = 1000;
    private static final int TO_MIN = 60;
    private static final int TO_HR = 3600;
    private static final String SPACE = " ";
    private static final String LINE_SEPARATOR = "\n";
    private static final String OPENING_PARENTHESIS = "(";
    private static final String CLOSING_PARENTHESIS = ")";
    private final Path route;
    private final AStar.OptimizationMode optimizationMode;
    private final Context appContext;
    private static final float PRIMARY_FONT_SIZE = 14f;
    private static final float SECONDARY_FONT_SIZE = 12f;

    public RouteAttributeStringBuilder(final AStar.OptimizationMode optimizationMode, final Path route, final Context appContext) {
        this.route = route;
        this.optimizationMode = optimizationMode;
        this.appContext = appContext;
    }

    private String buildLengthString() {
        final int meters = route.getLength();
        final String string;
        if (meters < TO_KILO) {
            string = meters + SPACE + "m";
        } else {
            // This produces a whole number.
            string = meters / TO_KILO + SPACE + "km";
        }
        return string;
    }

    private String buildTravelTimeString() {
        final int seconds = route.getTravelTime();
        final String string;
        if (seconds < TO_MIN) {
            string = seconds + SPACE + "sec";
        } else if (seconds < TO_HR) {
            string = seconds / TO_MIN + SPACE + "min";
        } else if (seconds == TO_HR) {
            string = 1 + SPACE + "hr";
        } else {
            final double hours = (double) seconds / TO_HR;
            final int wholeHours = (int) hours;
            final int wholeMinutes = (int) Math.round((hours - wholeHours) * TO_MIN);
            if (wholeMinutes == 0) {
                string = wholeHours + SPACE + "hr";
            } else {
                string = wholeHours + SPACE + "hr" + SPACE + wholeMinutes + SPACE + "min";
            }
        }
        return string;
    }

    public Spannable buildRouteAttributeString() {
        final String lengthString = buildLengthString();
        final String travelTimeString = buildTravelTimeString();
        final Spannable spannable;
        switch (optimizationMode) {
            case MINIMIZE_DISTANCE:
                spannable = new SpannableString(lengthString + SPACE + OPENING_PARENTHESIS + travelTimeString + CLOSING_PARENTHESIS + LINE_SEPARATOR + "Shortest route assuming free flow speed.");
                break;
            case MINIMIZE_TRAVEL_TIME:
                spannable = new SpannableString(travelTimeString + SPACE + OPENING_PARENTHESIS + lengthString + CLOSING_PARENTHESIS + LINE_SEPARATOR + "Fastest route assuming free flow speed.");
                break;
            default:
                spannable = null;
        }
        spannable.setSpan(new ForegroundColorSpan(appContext.getColor(R.color.colorPrimaryVariant)), 0, spannable.toString().indexOf(OPENING_PARENTHESIS), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(appContext.getColor(R.color.colorSecondary)), spannable.toString().indexOf(OPENING_PARENTHESIS), spannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new RelativeSizeSpan(SECONDARY_FONT_SIZE / PRIMARY_FONT_SIZE), spannable.toString().indexOf(CLOSING_PARENTHESIS), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}
