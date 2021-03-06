package com.avm.mavdroid.mono;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

class Utils {

    private static final String TAG = "com.avm.mavdroid.mono";

    static boolean isSecureSettingsPermGranted(Context context) {
        return context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED;
    }

    static int getLowBatteryLevel() {
        int level;
        try {
            level = Resources.getSystem().getInteger(Resources.getSystem().getIdentifier("config_lowBatteryWarningLevel", "int", "android"));
        } catch (NotFoundException e) {
            level = 15;
        }
        return (level >= 15 ? level : 0);
    }

    static int getBatteryLevel(Context context) {

        final Intent batteryIntent = context
                .registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (batteryIntent == null) {
            return Math.round(50.0f);
        }

        final int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        final int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (level == -1 || scale == -1) {
            return Math.round(50.0f);
        }

        float battery_level = ((float) level / (float) scale) * 100.0f;
        return Math.round(battery_level);

    }

    static void executeCommand(final String command, boolean isSuAvailable) {
        if (isSuAvailable) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<String> output = Shell.SU.run(command);
                    if (output != null) {
                        printShellOutput(output);
                    } else {
                        Log.i(TAG, "Error occurred while executing command (" + command + ")");
                    }
                }
            });
        } else {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<String> output = Shell.SH.run(command);
                    if (output != null) {
                        printShellOutput(output);
                    } else {
                        Log.i(TAG, "Error occurred while executing command (" + command + ")");
                    }
                }
            });
        }
    }

    private static void printShellOutput(List<String> output) {
        if (!output.isEmpty()) {
            for (String s : output) {
                Log.i(TAG, s);
            }
        }
    }

    static void toggleGreyScale(int value, ContentResolver contentResolver) {
        Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", value);
        if (value == 0) {
            Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer", -1);
        } else if (value == 1) {
            Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer", 0);
        }
    }

    static void resetGreyScale(ContentResolver contentResolver) {
        Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", 0);
        Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer", -1);
    }

    private static void showRootWorkaroundInstructions(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("No-root workaround");
        builder.setMessage("If your device isn't rooted, you can manually grant the permission 'android.permission.WRITE_SECURE_SETTINGS' " +
                "to this app by executing the following ADB command from your PC (the command is one-line, not separated):\n\n" +
                "\"adb -d shell pm grant com.avm.mavdroid.mono android.permission.WRITE_SECURE_SETTINGS\"\n\n" +
                "Once you have done, please close this app and start again and you will then be able to access the app properly.\n\n" +
                "IMPORTANT: If your are in GreyScale mode and you uninstall the application you will not be able to switch back to color more.");
        builder.setPositiveButton("Okay", null);
        builder.setNegativeButton("Share command", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "adb -d shell pm grant com.avm.mavdroid.mono android.permission.WRITE_SECURE_SETTINGS");
                sendIntent.setType("text/plain");
                context.startActivity(sendIntent);

            }
        });
        builder.show();
    }

    static void showPermNotGrantedDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Error");
        builder.setMessage("android.permission.WRITE_SECURE_SETTINGS not granted");
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    static void showRootUnavailableDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Error");
        builder.setMessage("SU permission denied or not available! If you don't have root, " +
                "press 'Root workaround' to get instructions on how to use this app without root");
        builder.setPositiveButton("Close", null);
        builder.setNegativeButton("Root workaround", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                showRootWorkaroundInstructions(context);
            }
        });
        builder.show();
    }

    static void showGreyScaleActiveDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Droid Mode Active!");
        builder.setMessage("Auto Switching of Grey Scale is active.\nThe system will switch to Grey Scale at OEM set battery level (usually 15%)");
        builder.setPositiveButton("Genau!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    static void showMoreInfoDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Technical mambo jumbo");
        builder.setMessage("System switches to Grey Scale when the battery hits 'LOW' level. " +
                "\n\nThe 'LOW' level is defined by the OEM (usually 15%)." +
                "This mode is disabled when the battery reaches the OKAY level (usually 30%) as defined by the OEM." +
                "This application works on system battery intent and does not poll the battery status continuously. " +
                "Hence it does not consume battery." +
                "\n\nPhones with LCD screens will not see any advantage as LDC screens consume same amount of power " +
                "at full color or black and white. So this mode is more of fun!" +
                "\n\nPhones with AMOLED screens will save some juice as AMOLED displays switch of pixels for deep blacks"
        );
        builder.setPositiveButton("Genau!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    static void showCredits(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Credits");
        builder.setMessage(" * Code based on https://github.com/theblixguy \n" +
                "* I have made modifications to the code and added functionality as I want it.");
        builder.setPositiveButton("Genau!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}
