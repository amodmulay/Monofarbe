package com.avm.mavdroid.mono;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BatteryOkay extends BroadcastReceiver {

    public static final String TAG = "LowBatteryMavdroid";
    boolean isSecureSettingsPermGranted = false;
    boolean isGreyScaleEnabled = false;
    SharedPreferences settings;

    public BatteryOkay() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OKAY battery broadcast received, disabling Grey Scale mode");
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        isSecureSettingsPermGranted = settings.getBoolean("isSecureSettingsPermGranted", false);
        isGreyScaleEnabled = settings.getBoolean(MainActivity.GS_AUTO, false);
        if (isSecureSettingsPermGranted) {
            if (isGreyScaleEnabled) {
                Utils.toggleGreyScale(0, context.getContentResolver());
            } else {
                Log.i(TAG, "OKAY battery broadcast received, but Grey Scale is not enabled, so skipping");
            }
        } else {
            Log.i(TAG, "OKAY battery broadcast received, but WRITE_SECURE_SETTINGS permission not granted, so skipping");
        }
    }
}
