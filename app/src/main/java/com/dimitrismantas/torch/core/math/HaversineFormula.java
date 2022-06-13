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
package com.dimitrismantas.torch.core.math;

public final class HaversineFormula {
    private static final double R = 6371008.771415059454739D;

    private HaversineFormula() {
    }

    public static double run(final double lat1, final double lon1, final double lat2, final double lon2) {
//        // The points are the same since their coordinates are the same.
//        if (SupplementalMath.almostEquals(lat1, lat2) && SupplementalMath.almostEquals(lon1, lon2)) {
//            return 0.0D;
//        }
//        // The points are the same since one of them has been placed at the location of the other after one full rotation around the Earth.
//        if ((SupplementalMath.almostEquals(lat1, lat2) && SupplementalMath.almostEquals(Math.abs(lon1 - lon2), 360.0D)) || SupplementalMath.almostEquals(Math.abs(lat1 - lat2), 360.0D) && SupplementalMath.almostEquals(lon1, lon2)) {
//            return 0.0D;
//        }
        final double dLat = Math.toRadians(lat1 - lat2);
        final double sLat = Math.toRadians(lat1 + lat2);
        final double dLon = Math.toRadians(lon1 - lon2);
        final double angularDistance = SupplementalMath.hav(dLat) + (1.0D - SupplementalMath.hav(dLat) - SupplementalMath.hav(sLat)) * SupplementalMath.hav(dLon);
        final double constrainedAngularDistance = Math.min(Math.max(angularDistance, 0.0D), 1.0D);
        return R * SupplementalMath.ahav(constrainedAngularDistance);
    }
}
