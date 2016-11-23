/**
 * Many thanks to https://github.com/theblixguy for the opensource code
 * http://www.suyashsrijan.com/
 * <p/>
 * I have made modifications to the code and added functionality as I wanted it.
 * My source shall always be open source for this application
 */
package com.avm.mavdroid.mono;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nanotasks.BackgroundWork;
import com.nanotasks.Completion;
import com.nanotasks.Tasks;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "com.avm.mavdroid.mono";
    public static final String GS_AUTO = "isGreyScaleEnabledAuto";
    public static final String GS_MANUAL = "isGreyScaleEnabledManual";

    boolean isSuAvailable = false;
    private boolean isSecureSettingsPermGranted = false;
    private boolean isGreyScaleEnabledAuto = false;
    private boolean isGreyScaleEnabledManual = false;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private MaterialDialog progressDialog;
    private Switch toggleAutoSwitch;
    private Switch toggleManualSwitch;

    private void initSettings() {
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isSuAvailable = settings.getBoolean("isSuAvailable", false);
        isSecureSettingsPermGranted = settings.getBoolean("isSecureSettingsPermGranted", false);
        toggleAutoSwitch = (Switch) findViewById(R.id.switchAuto);
        toggleManualSwitch = (Switch) findViewById(R.id.switchManual);

    }

    private void setComponentState() {
        isGreyScaleEnabledAuto = settings.getBoolean(GS_AUTO, false);
        isGreyScaleEnabledManual = settings.getBoolean(GS_MANUAL, false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0.0f);
        }

        if (isGreyScaleEnabledAuto) {
            toggleAutoSwitch.setChecked(true);
        } else {
            toggleAutoSwitch.setChecked(false);
        }

        if (isGreyScaleEnabledManual) {
            toggleManualSwitch.setChecked(true);
        } else {
            toggleManualSwitch.setChecked(false);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        //standard code
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSettings();
        setComponentState();
        //register change listener for button action
        toggleAutoSwitch.setOnCheckedChangeListener(this);
        toggleManualSwitch.setOnCheckedChangeListener(this);

        if (!Utils.isSecureSettingsPermGranted(getApplicationContext())) {
            progressDialog = new MaterialDialog.Builder(this)
                    .title("Please wait")
                    .autoDismiss(false)
                    .cancelable(false)
                    .content("Requesting Super User access")
                    .progress(true, 0)
                    .show();
            Log.i(TAG, "Request Super User permission if Super User is available");

            Tasks.executeInBackground(MainActivity.this, new BackgroundWork<Boolean>() {
                @Override
                public Boolean doInBackground() throws Exception {
                    return Shell.SU.available();
                }
            }, new Completion<Boolean>() {
                @Override
                public void onSuccess(Context context, Boolean result) {
                    if (progressDialog != null) {
                        progressDialog.cancel();
                    }
                    isSuAvailable = result;
                    Log.i(TAG, "SU available: " + Boolean.toString(result));
                    if (isSuAvailable) {
                        Log.i(TAG, "android.permission.WRITE_SECURE_SETTINGS to com.avm.mavdroid.mono....");
                        Utils.executeCommand("pm grant com.avm.mavdroid.mono android.permission.WRITE_SECURE_SETTINGS", isSuAvailable);
                        editor = settings.edit();
                        editor.putBoolean("isSuAvailable", true);
                        isSecureSettingsPermGranted = true;
                        editor.putBoolean("isSecureSettingsPermGranted", true);
                        editor.apply();

                    } else {
                        Log.i(TAG, "Root access not available");

                        Utils.showRootUnavailableDialog(MainActivity.this);
                    }
                }

                @Override
                public void onError(Context context, Exception e) {
                    Log.e(TAG, "Error querying SU: " + e.getMessage());
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        if (Utils.isSecureSettingsPermGranted(this)) {
            editor = settings.edit();
            isSecureSettingsPermGranted = true;
            editor.putBoolean("isSecureSettingsPermGranted", true);
            editor.apply();
        }

        Log.i(TAG, "WRITE_SECURE_SETTINGS granted: " + Boolean.toString(isSecureSettingsPermGranted));
    }

    public void resetGreyScale() {
        if (Utils.isSecureSettingsPermGranted(getApplicationContext())) {
            Utils.resetGreyScale(getContentResolver());
        } else {
            Utils.showPermNotGrantedDialog(this);
        }
        setBooleanValueInSharedPreferences(GS_AUTO, false);
        setBooleanValueInSharedPreferences(GS_MANUAL, false);
        setComponentState();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_grey_scale_more_info:
                Utils.showMoreInfoDialog(this);
                break;
            case R.id.action_reset_grey_scale:
                resetGreyScale();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if (compoundButton.getId() == R.id.switchAuto) {
            if (b) {
                setBooleanValueInSharedPreferences(GS_AUTO, false);
                Utils.showGreyScaleActiveDialog(MainActivity.this);
            } else {
                setBooleanValueInSharedPreferences(GS_AUTO, false);
            }
        }

        if (compoundButton.getId() == R.id.switchManual) {
            if (b) {
                setBooleanValueInSharedPreferences(GS_MANUAL, true);
                Utils.toggleGreyScale(1, getApplicationContext().getContentResolver());
            } else {
                setBooleanValueInSharedPreferences(GS_MANUAL, false);
                Utils.toggleGreyScale(0, getApplicationContext().getContentResolver());
            }

        }
    }

    private void setBooleanValueInSharedPreferences(final String identifier, final boolean bool) {
        editor = settings.edit();
        editor.putBoolean(identifier, bool);
        editor.apply();
    }
}
