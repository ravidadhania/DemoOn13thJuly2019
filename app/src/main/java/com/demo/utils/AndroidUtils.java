package com.demo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class AndroidUtils {

    public static boolean requestRuntimePermission(final Activity activity, String title, String message, final String[] permissions, final int requestCode) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            boolean isAllpermissionGranted = true;
            boolean shouldShowPermissionalRationale = false;
            final List<String> permissionNotGranted = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(activity, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    isAllpermissionGranted = false;
                    permissionNotGranted.add(permissions[i]);
                }
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                    shouldShowPermissionalRationale = true;
                }
            }
            if (isAllpermissionGranted) {
                return true;
            }

            if (shouldShowPermissionalRationale) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(activity, android.R.style.ThemeOverlay_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(activity);
                }
                builder.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, permissionNotGranted.toArray(new String[permissionNotGranted.size()]), requestCode);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();

            } else {
                ActivityCompat.requestPermissions(activity, permissionNotGranted.toArray(new String[permissionNotGranted.size()]), requestCode);
            }
            return false;
        }else{
            return true;
        }
    }
}
