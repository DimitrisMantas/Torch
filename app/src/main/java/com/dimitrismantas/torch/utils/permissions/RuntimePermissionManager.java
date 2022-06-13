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
package com.dimitrismantas.torch.utils.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.dimitrismantas.torch.R;

public class RuntimePermissionManager implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "RuntimePermissionManager";
    private static final String PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int PERMISSION_ID = 0;

//    @Override
//    protected void onCreate(final Bundle savedInstanceState) {
//        final SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
//        super.onCreate(savedInstanceState);
//        // This ensures no hiccups while the splash screen is active.
//        splashScreen.setKeepOnScreenCondition(() -> true);
//        checkRuntimePermissionStatus();
//    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Runtime permission has been granted.");
//                startMainActivity();

            } else {
                Log.e(TAG, "Runtime permission has been denied.");
            }
        }
    }

    public static boolean checkRuntimePermissionStatus(final Activity parent) {
        if (ActivityCompat.checkSelfPermission(parent.getApplicationContext(), PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestRuntimePermission(parent);
            return false;
        }
    }

    private static void requestRuntimePermission(final Activity parent) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(parent, PERMISSION)) {
            System.out.println("HERE1");
            showPermissionRationaleDialog(R.string.permission_rationale_title, R.string.permission_rationale_message, parent);
        } else {
            Log.e(TAG, "Runtime permission has been denied.");
            ActivityCompat.requestPermissions(parent, new String[]{PERMISSION}, PERMISSION_ID);
        }
    }

    private static void showPermissionRationaleDialog(final int title, final int message, final Activity parent) {
        System.out.println("HERE2");
        AlertDialog.Builder builder = new AlertDialog.Builder(parent.getApplicationContext());
        builder.setTitle(title).setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(parent, new String[]{PERMISSION}, PERMISSION_ID);
            }
        });
        builder.create().show();
    }

//    private void startMainActivity() {
//        ThreadManager.unpackCriticalAssets(getApplicationContext());
//        Intent intent = new Intent(this, MapActivity.class);
//        startActivity(intent);
//        this.finish();
//    }
}
