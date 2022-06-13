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
package com.dimitrismantas.torch.utils.multithreading;

import android.content.Context;
import android.util.Log;

import com.dimitrismantas.torch.core.main.engine.AStar;
import com.dimitrismantas.torch.core.main.utils.NearestNeighborSearch;
import com.dimitrismantas.torch.utils.data.AssetManager;
import com.dimitrismantas.torch.utils.data.DataManager;
import com.dimitrismantas.torch.core.utils.serialization.DeserializationManager;

public final class ThreadManager {
    /**
     * The class tag for logging purposes.
     */
    private static final String TAG = "ThreadManager";

    // This class is static.
    private ThreadManager() {
    }

    public static void unpackCriticalAssets(final Context appCtx) {
        Thread t = new Thread(() -> {
            AssetManager.writeToFile("map/grc.map", appCtx);
        });
        t.start();
        try {
            // Keep the splash screen visible until the assets have been unpacked.
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to unpack critical application assets", e);
        }
    }

    public static void instantiateRoutingServices(final Context appCtx) {
        Thread thread = new Thread(() -> {
            AssetManager.writeToFile("bin/grc.bin", appCtx);
            DataManager.setGraph(DeserializationManager.deserializeGraph("bin/grc.bin"));
            DataManager.setNearestNeighborSearch(new NearestNeighborSearch(DataManager.getGraph()));
            DataManager.setaStar(new AStar(DataManager.getGraph()));
        });
        thread.start();
    }
}
