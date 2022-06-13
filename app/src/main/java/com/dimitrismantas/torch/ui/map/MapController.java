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

import android.util.Log;

import com.dimitrismantas.torch.core.math.SupplementalMath;

import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Color;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Tile;
import org.oscim.layers.vector.VectorLayer;
import org.oscim.layers.vector.geometries.RectangleDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import java.util.List;

public final class MapController {
    private static final String TAG = "MapController";
    private final Map map;
    private final List<GeoPoint> pointsOnMap;
    private float mapBearing;
    private float mapTilt;
    private double mapZoomLevel;

    public MapController(final Map map, List<GeoPoint> pointsOnMap) {
        this.map = map;
        this.pointsOnMap = pointsOnMap;
    }

    public void resetMap(final InputEvent inputEvent) {
        invalidateMapPosition();
//        invalidateLocalPointsOnMap(pointsOnMap);
        final GeoPoint mapCenter = map.getMapPosition().getGeoPoint();
        final GeoPoint newMapCenter;
        switch (this.pointsOnMap.size()) {
            case 0:
                newMapCenter = mapCenter;
                break;
            case 1:
                newMapCenter = this.pointsOnMap.get(0);
                break;
            default:
                final BoundingBox boundingBox = new BoundingBox(this.pointsOnMap);
                System.out.println(String.format("Map Bounding Box: %s", boundingBox.toString()));
                newMapCenter = boundingBox.getCenterPoint();
                mapZoomLevel = fitBoundingBox(boundingBox);
//                newMapCenter=mapCenter;
//                fitBoundingBox(boundingBox);
        }
        switch (inputEvent) {
            case CLICK:
                // This block ensures that the map bearing an tilt are not reset until the map is centered.
                if (!SupplementalMath.almostEquals(mapCenter.getLatitude(), newMapCenter.getLatitude()) && !SupplementalMath.almostEquals(mapCenter.getLongitude(), newMapCenter.getLongitude())) {
                    System.out.println("skjdfnkjf");
                    break;
                }
                if (!SupplementalMath.almostEquals(mapBearing,0)) {
                    mapBearing = 0F;
                } else if (!SupplementalMath.almostEquals(mapTilt,0)) {
                    mapTilt = 0F;
                }
                break;
            case LONG_CLICK:
                mapBearing = mapTilt = 0F;
                break;
            default:
                Log.e(TAG, "", new IllegalAccessException("Failed to recognize input event."));
        }
        map.animator().animateTo(new MapPosition(newMapCenter.getLatitude(), newMapCenter.getLongitude(), SupplementalMath.exp2(mapZoomLevel)).setBearing(mapBearing).setTilt(mapTilt));
    }

    //  Each map position is tied to its own MapPosition instance, which invalidates all previous map positions. This means that the map position is effectively passed by value and needs to be invalidated whenever the map controller uses it.
    private void invalidateMapPosition() {
        final MapPosition mapPosition = map.getMapPosition();
        mapBearing = mapPosition.getBearing();
        mapTilt = mapPosition.getTilt();
        mapZoomLevel = mapPosition.getZoom();
    }


    private double fitBoundingBox(final BoundingBox boundingBox) {
//
//        final BoundingBox maxBoundingBox = maxBoundingBox();
//
//
//        VectorLayer vectorLayer1 = new VectorLayer(map);
//        vectorLayer1.add(new RectangleDrawable(boundingBox.getMinLatitude(),
//                boundingBox.getMinLongitude(),
//                boundingBox.getMaxLatitude(),
//                boundingBox.getMaxLongitude(),
//                Style.builder().buffer(0.5).fillColor(Color.RED).fillAlpha(0.15F).build()));
//        map.layers().add(vectorLayer1);

//        VectorLayer vectorLayer2 = new VectorLayer(map);
//        vectorLayer1.add(new RectangleDrawable(maxBoundingBox.getMinLatitude(),
//                maxBoundingBox.getMinLongitude(),
//                maxBoundingBox.getMaxLatitude(),
//                maxBoundingBox.getMaxLongitude(),
//                Style.builder().buffer(0.5).fillColor(Color.BLUE).fillAlpha(0.15F).build()));
//        map.layers().add(vectorLayer2);
//        map.render();
//
//        System.out.println(String.format("Max Bounding Box: %s", maxBoundingBox.toString()));
//
//
//
//        final double zoomLevelAlongXAxis = fitBoundingBoxInAxis(boundingBox.getLongitudeSpan(), maxBoundingBox.getLongitudeSpan());
//        final double zoomLevelAlongYAxis = fitBoundingBoxInAxis(boundingBox.getLatitudeSpan(), maxBoundingBox.getLatitudeSpan());
//        return Math.min(zoomLevelAlongYAxis, zoomLevelAlongXAxis);

        double x = fit(boundingBox);
        System.out.println(x);
        return x;
    }



    private double fit(BoundingBox boundingBox) {
        double minX = MercatorProjection.longitudeToX(boundingBox.getMinLongitude());
        double minY = MercatorProjection.latitudeToY(boundingBox.getMaxLatitude());

        double dx = Math.abs(
                MercatorProjection.longitudeToX(boundingBox.getMaxLongitude()) - minX);
        double dy = Math.abs(
                MercatorProjection.latitudeToY(boundingBox.getMinLatitude()) - minY);

        double dz = Math.hypot(dx, dy);
        System.out.println(dx);
        System.out.println(dy);
        System.out.println(dz);

        double zX = (map.getWidth() - 16*CanvasAdapter.getScale()) / (dz * Tile.SIZE);
        double zY = (map.getHeight() - 16*CanvasAdapter.getScale()) / (dz * Tile.SIZE);

        double scale = Math.min(zX, zY);

        return SupplementalMath.log2( scale );
    }

    public void zoomOut() {

        invalidateMapPosition();
        map.animator().animateTo(map.getMapPosition().setBearing(mapBearing).setScale(SupplementalMath.exp2(mapZoomLevel - 1d)).setTilt(mapTilt));
    }

    public enum InputEvent {
        CLICK, LONG_CLICK
    }
}
