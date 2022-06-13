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
package com.dimitrismantas.torch.core.utils.serialization;

import android.util.Log;

import com.dimitrismantas.torch.core.main.engine.utils.exceptions.EqualEndpointException;
import com.dimitrismantas.torch.utils.data.FileManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DeserializationManager {
    /**
     * The class tag for logging purposes.
     */
    private static final String TAG = "SerializationManager";

    // This class is static.
    private DeserializationManager() {
    }

    // This allows random access, do not change!!
    public static DeserializedGraph deserializeGraph(final String relPath) {
        final File absPath = new File(FileManager.concatenateNestedPaths(FileManager.getPrimaryStorageDevicePath(), relPath));

        MappedByteBuffer bb = null;
        try (final FileChannel fc = new RandomAccessFile(absPath, "rw").getChannel()) {
            bb = fc.map(FileChannel.MapMode.PRIVATE, 0, fc.size());
        } catch (IOException e) {
            Log.e(TAG, "Failed to deserialize graph.", e);
            return null;
        }
        return DeserializedGraph.getRootAsDeserializedGraph(bb);
    }

    /**
     * Determines if two given vertices are equal to each other.
     *
     * @param a The first vertex.
     * @param b The second vertex.
     * @return {@code true} of the numbers are almost equal; {@code false} otherwise.
     * @see EqualEndpointException
     */
    public static boolean equals(final DeserializedVertex a, final DeserializedVertex b) {
        return a == b || (a != null && a.lbl() == b.lbl());
    }
}
