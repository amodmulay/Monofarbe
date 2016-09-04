package com.avm.mavdroid.mono;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BatteryLow extends BroadcastReceiver {

    public static final String TAG = "LowBatteryMonofarbe";
    boolean isSecureSettingsPermGranted = false;
    boolean isMonochromeEnabled = false;
    SharedPreferences settings;

    public BatteryLow() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "LOW battery broadcast received, enabling monochrome mode");
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        isSecureSettingsPermGranted = settings.getBoolean("isSecureSettingsPermGranted", false);
        isMonochromeEnabled = settings.getBoolean("isMonochromeEnabledAuto", false);
        if (isSecureSettingsPermGranted) {
            if (isMonochromeEnabled) {
                Utils.toggleMonochrome(1, context.getContentResolver());
            } else {
                Log.i(TAG, "LOW battery broadcast received, but Monochrome is not enabled, so skipping");
            }
        } else {
            Log.i(TAG, "LOW battery broadcast received, but WRITE_SECURE_SETTINGS permission not granted, so skipping");
        }
    }
}
