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
package com.dimitrismantas.torch.ui.map.scalebar;

import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Canvas;
import org.oscim.backend.canvas.Color;
import org.oscim.backend.canvas.Paint;
import org.oscim.map.Map;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.DistanceUnitAdapter;
import org.oscim.scalebar.ImperialUnitAdapter;

public class ScaleBar extends org.oscim.scalebar.MapScaleBar {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int HORIZONTAL_MARGIN = 16;
    private static final int VERTICAL_MARGIN = 8;
    private static final int EXTENSION_LINE_LENGTH = 10;
    private static final int EXTERNAL_STROKE = 1;
    private static final int INTERNAL_STROKE = 2;
    private static final int NUMERAL_MARGIN = 3;
    private static final int NUMERAL_EXTERNAL_STROKE = 0;
    private static final int NUMERAL_INTERNAL_STROKE = 1;
    private static final float SCREEN_SCALE = CanvasAdapter.getScale();
    private static final DefaultMapScaleBar.ScaleBarMode PRIMARY_OR_SECONDARY_UNITS = DefaultMapScaleBar.ScaleBarMode.BOTH;
    private static final DistanceUnitAdapter SECONDARY_UNITS = ImperialUnitAdapter.INSTANCE;
    private final Paint paintScaleBar;
    private final Paint paintScaleBarStroke;
    private final Paint paintScaleText;
    private final Paint paintScaleTextStroke;

    public ScaleBar(Map map) {
        super(map, (int) (WIDTH * SCREEN_SCALE), (int) (HEIGHT * SCREEN_SCALE), SCREEN_SCALE);
        setMarginHorizontal((int) (HORIZONTAL_MARGIN * SCREEN_SCALE));
        setMarginVertical((int) (VERTICAL_MARGIN * SCREEN_SCALE));
        this.paintScaleBar = defineBarColor(Color.BLACK, INTERNAL_STROKE, Paint.Style.FILL);
        this.paintScaleBarStroke = defineBarColor(Color.WHITE, EXTERNAL_STROKE, Paint.Style.STROKE);
        this.paintScaleText = defineTextColor(Color.BLACK, NUMERAL_INTERNAL_STROKE, Paint.Style.FILL);
        this.paintScaleTextStroke = defineTextColor(Color.WHITE, NUMERAL_EXTERNAL_STROKE, Paint.Style.STROKE);
    }

    private Paint defineBarColor(int color, float strokeWidth, Paint.Style style) {
        Paint paint = CanvasAdapter.newPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth * SCREEN_SCALE);
        paint.setStyle(style);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    private Paint defineTextColor(int color, float strokeWidth, Paint.Style style) {
        Paint paint = CanvasAdapter.newPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth * SCREEN_SCALE);
        paint.setStyle(style);
        paint.setTypeface(Paint.FontFamily.DEFAULT, Paint.FontStyle.BOLD);
        paint.setTextSize(12 * SCREEN_SCALE);
        return paint;
    }

    @Override
    protected void redraw(Canvas canvas) {
        canvas.fillColor(Color.TRANSPARENT);
        ScaleBarLengthAndValue lengthAndValue = this.calculateScaleBarLengthAndValue();
        ScaleBarLengthAndValue lengthAndValue2;
        if (PRIMARY_OR_SECONDARY_UNITS == org.oscim.scalebar.DefaultMapScaleBar.ScaleBarMode.BOTH) {
            lengthAndValue2 = this.calculateScaleBarLengthAndValue(SECONDARY_UNITS);
        } else {
            lengthAndValue2 = new ScaleBarLengthAndValue(0, 0);
        }
        drawScaleBar(canvas, lengthAndValue.scaleBarLength, lengthAndValue2.scaleBarLength, this.paintScaleBarStroke);
        drawScaleBar(canvas, lengthAndValue.scaleBarLength, lengthAndValue2.scaleBarLength, this.paintScaleBar);
        String scaleText1 = this.distanceUnitAdapter.getScaleText(lengthAndValue.scaleBarValue);
        String scaleText2 = PRIMARY_OR_SECONDARY_UNITS == org.oscim.scalebar.DefaultMapScaleBar.ScaleBarMode.BOTH ? SECONDARY_UNITS.getScaleText(lengthAndValue2.scaleBarValue) : "";
        drawScaleText(canvas, scaleText1, scaleText2, this.paintScaleTextStroke);
        drawScaleText(canvas, scaleText1, scaleText2, this.paintScaleText);
    }

    private void drawScaleBar(Canvas canvas, int scaleBarLength1, int scaleBarLength2, Paint paint) {
        int maxScaleBarLength = Math.max(scaleBarLength1, scaleBarLength2);
        if (scaleBarLength2 == 0) {
            canvas.drawLine(Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f), Math.round(canvas.getHeight() - EXTENSION_LINE_LENGTH * ScaleBar.SCREEN_SCALE), Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f + maxScaleBarLength), Math.round(canvas.getHeight() - EXTENSION_LINE_LENGTH * ScaleBar.SCREEN_SCALE), paint);
            canvas.drawLine(Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f), Math.round(canvas.getHeight() * 0.5f), Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f), Math.round(canvas.getHeight() - EXTENSION_LINE_LENGTH * ScaleBar.SCREEN_SCALE), paint);
            canvas.drawLine(Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f + maxScaleBarLength), Math.round(canvas.getHeight() * 0.5f), Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f + maxScaleBarLength), Math.round(canvas.getHeight() - EXTENSION_LINE_LENGTH * ScaleBar.SCREEN_SCALE), paint);
        } else {
            canvas.drawLine(Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f), Math.round(canvas.getHeight() * 0.5f), Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f + maxScaleBarLength), Math.round(canvas.getHeight() * 0.5f), paint);
            canvas.drawLine(Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f), Math.round(EXTENSION_LINE_LENGTH * ScaleBar.SCREEN_SCALE), Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f), Math.round(canvas.getHeight() - EXTENSION_LINE_LENGTH * ScaleBar.SCREEN_SCALE), paint);
            canvas.drawLine(Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f + scaleBarLength1), Math.round(EXTENSION_LINE_LENGTH * ScaleBar.SCREEN_SCALE), Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f + scaleBarLength1), Math.round(canvas.getHeight() * 0.5f), paint);
            canvas.drawLine(Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f + scaleBarLength2), Math.round(canvas.getHeight() * 0.5f), Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f + scaleBarLength2), Math.round(canvas.getHeight() - EXTENSION_LINE_LENGTH * ScaleBar.SCREEN_SCALE), paint);
        }
    }

    private void drawScaleText(Canvas canvas, String scaleText1, String scaleText2, Paint paint) {
        if (scaleText2.length() == 0) {
            canvas.drawText(scaleText1, Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE + NUMERAL_MARGIN * ScaleBar.SCREEN_SCALE), Math.round(canvas.getHeight() - EXTENSION_LINE_LENGTH * ScaleBar.SCREEN_SCALE - EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f - NUMERAL_MARGIN * ScaleBar.SCREEN_SCALE), paint);
        } else {
            canvas.drawText(scaleText1, Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE + NUMERAL_MARGIN * ScaleBar.SCREEN_SCALE), Math.round(canvas.getHeight() * 0.5f - EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f - NUMERAL_MARGIN * ScaleBar.SCREEN_SCALE), paint);
            canvas.drawText(scaleText2, Math.round(EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE + NUMERAL_MARGIN * ScaleBar.SCREEN_SCALE), Math.round(canvas.getHeight() * 0.5f + EXTERNAL_STROKE * ScaleBar.SCREEN_SCALE * 0.5f + NUMERAL_MARGIN * ScaleBar.SCREEN_SCALE + this.paintScaleTextStroke.getTextHeight(scaleText2)), paint);
        }
    }
}
