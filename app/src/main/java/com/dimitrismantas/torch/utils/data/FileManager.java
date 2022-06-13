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
package com.dimitrismantas.torch.utils.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

public final class FileManager {
    private static final String TAG = "FileManager";
    private static String psdPath;

    private FileManager() {
    }

    public static void mkdirs(final String relDirPath, final Context appCtx) {
        setPrimaryStorageDevicePath(appCtx);
        final String absDirPath = concatenateNestedPaths(psdPath, relDirPath);
        final File f = new File(absDirPath);
        if (f.exists()) {
            Log.e(TAG, "Failed to create directory.", new FileAlreadyExistsException(absDirPath));
            return;
        }
        final boolean isCreated = f.mkdirs();
        if (!isCreated) {
            Log.e(TAG, "Failed to create directory.", new IOException());
        }
    }

    public static String concatenateNestedPaths(final String a, final String b) {
        return a + "/" + b;
    }

    public static String getPrimaryStorageDevicePath() {
        if (psdPath==null){
           psdPath= getPrimaryStorageDevicePath();
        }
        return psdPath;
    }

    protected static void setPrimaryStorageDevicePath(final Context appCtx) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !Environment.isExternalStorageEmulated()) {
            psdPath = appCtx.getExternalFilesDir(null).toString();
        } else {
            psdPath = appCtx.getFilesDir().toString();
        }
    }
}
