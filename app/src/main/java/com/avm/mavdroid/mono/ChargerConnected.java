package com.avm.mavdroid.mono;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ChargerConnected extends BroadcastReceiver {

    public static final String TAG = "LowBatteryMavdroid";
    boolean isSecureSettingsPermGranted = false;
    boolean isGreyScaleEnabled = false;
    SharedPreferences settings;

    @Override
    public void onReceive(Context context, Intent intent) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        isSecureSettingsPermGranted = settings.getBoolean("isSecureSettingsPermGranted", false);
        isGreyScaleEnabled = settings.getBoolean(MainActivity.GS_AUTO, false);
        if (isSecureSettingsPermGranted) {
            if (isGreyScaleEnabled) {
                Log.i(TAG, "POWER_CONNECTED broadcast received, disabling Grey Scale mode");
                Utils.toggleGreyScale(0, context.getContentResolver());
            } else {
                Log.i(TAG, "POWER_CONNECTED broadcast received, but Grey Scale is not enabled, so skipping");
            }
        } else {
            Log.i(TAG, "POWER_CONNECTED broadcast received, but WRITE_SECURE_SETTINGS permission not granted, so skipping");
        }
    }
}
