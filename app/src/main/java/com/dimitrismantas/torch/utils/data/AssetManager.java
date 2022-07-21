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
package com.dimitrismantas.torch.utils.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class AssetManager {
    private static final String TAG = "AssetManager";
    private static final int BUFFER_SIZE = 8192;

    private AssetManager() {
    }

    public static void writeToFile(final String relInPath, final Context appCtx) {
        File absOutPath;
        if (relInPath.lastIndexOf("/") > 0) {
            final String[] relInDirAndFile = relInPath.split("/");
            FileManager.mkdirs(relInDirAndFile[0], appCtx);
            absOutPath = new File(FileManager.concatenateNestedPaths(FileManager.getPrimaryStorageDevicePath(), relInDirAndFile[0]), relInDirAndFile[1]);
        } else {
            FileManager.setPrimaryStorageDevicePath(appCtx);
            absOutPath = new File(FileManager.concatenateNestedPaths(FileManager.getPrimaryStorageDevicePath(), relInPath));
        }
        if (!absOutPath.exists()) {
            try (final InputStream iStream = appCtx.getAssets().open(relInPath); final OutputStream oStream = new FileOutputStream(absOutPath)) {
                writeToOutputStream(iStream, oStream);
            } catch (final IOException e) {
                Log.e(TAG, "Failed to write asset to file.", e);
            }
        }
    }

    private static void writeToOutputStream(final InputStream iStream, final OutputStream oStream) {
        final byte[] buffer = new byte[BUFFER_SIZE];
        try {
            int length;
            while ((length = iStream.read(buffer)) != -1) {
                oStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to write input stream to output stream.", e);
        }
    }

    public static android.graphics.Bitmap toBitmap(final int id, final Context appCtx) {
        final Drawable drawable = ResourcesCompat.getDrawable(appCtx.getResources(), id, null);
        if (drawable == null) {
            Log.e(TAG, "Failed to locate drawable.", new FileNotFoundException());
            return null;
        }
        final Canvas canvas = new Canvas();
        final Bitmap bitmap = android.graphics.Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
