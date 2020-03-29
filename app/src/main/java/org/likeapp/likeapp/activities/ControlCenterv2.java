/*  Copyright (C) 2016-2020 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Johannes Tysiak, Taavi Eomäe, vanous

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package org.likeapp.likeapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.likeapp.likeapp.model.DeviceService;
import org.likeapp.likeapp.model.NotificationSpec;
import org.likeapp.likeapp.model.NotificationType;
import org.likeapp.likeapp.service.audio.MicReader;
import org.likeapp.likeapp.service.audio.MicReaderListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.cketti.library.changelog.ChangeLog;
import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.R;
import org.likeapp.likeapp.adapter.GBDeviceAdapterv2;
import org.likeapp.likeapp.devices.DeviceManager;
import org.likeapp.likeapp.impl.GBDevice;
import org.likeapp.likeapp.model.RecordedDataTypes;
import org.likeapp.likeapp.util.AndroidUtils;
import org.likeapp.likeapp.util.GB;
import org.likeapp.likeapp.util.Prefs;

import javax.net.ssl.HttpsURLConnection;

//TODO: extend AbstractGBActivity, but it requires actionbar that is not available
public class ControlCenterv2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GBActivity, MicReaderListener
{
    @SuppressLint ("StaticFieldLeak")
    private static ControlCenterv2 activity;

    public static void requestPermissions (String[] permissions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (activity != null)
            {
                activity.requestPermissions (permissions, 0);
            }
            else
            {
                Context context = GBApplication.getContext ();
                context.getApplicationContext ().startActivity (new Intent (context, ControlCenterv2.class).putExtra ("PERMISSIONS", permissions));
            }
        }
    }

    //needed for KK compatibility
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private DeviceManager deviceManager;

    private GBDeviceAdapterv2 mGBDeviceAdapter;
    private RecyclerView deviceListView;
    private FloatingActionButton fab;

    private boolean isLanguageInvalid = false;
    private SeekBar babyMonitorProgress;
    private SeekBar babyMonitorLimit;
    private CheckBox babyMonitorEnable;

    public static final int MENU_REFRESH_CODE=1;

    private static PhoneStateListener fakeStateListener;
    private static long babyMonitorLastTime;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (Objects.requireNonNull(action)) {
                case GBApplication.ACTION_LANGUAGE_CHANGE:
                    setLanguage(GBApplication.getLanguage(), true);
                    break;
                case GBApplication.ACTION_QUIT:
                    finish();
                    break;
                case DeviceManager.ACTION_DEVICES_CHANGED:
                    refreshPairedDevices();
                    break;
                case GBApplication.ACTION_DISABLE_BABY_MONITOR:
                    // Отключить радионяню
                    babyMonitorEnable.setChecked (false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AbstractGBActivity.init(this, AbstractGBActivity.NO_ACTIONBAR);

        super.onCreate(savedInstanceState);

        ControlCenterv2.activity = this;

        setContentView(R.layout.activity_controlcenterv2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Выполнить запрос количества пройденных шагов
        DeviceService deviceService = GBApplication.deviceService ();
        deviceService.onEnableRealtimeSteps (true);
        deviceService.onEnableRealtimeSteps (false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.controlcenter_navigation_drawer_open, R.string.controlcenter_navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList (null);

        //end of material design boilerplate
        deviceManager = ((GBApplication) getApplication()).getDeviceManager();

        deviceListView = findViewById(R.id.deviceListView);
        deviceListView.setHasFixedSize(true);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));

        List<GBDevice> deviceList = deviceManager.getDevices();
        mGBDeviceAdapter = new GBDeviceAdapterv2(this, deviceList);

        deviceListView.setAdapter(this.mGBDeviceAdapter);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDiscoveryActivity();
            }
        });

        showFabIfNeccessary();

        /* uncomment to enable fixed-swipe to reveal more actions

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT , ItemTouchHelper.RIGHT) {
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if(dX>50)
                    dX = 50;
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                GB.toast(getBaseContext(), "onMove", Toast.LENGTH_LONG, GB.ERROR);

                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                GB.toast(getBaseContext(), "onSwiped", Toast.LENGTH_LONG, GB.ERROR);

            }

            @Override
            public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                        int actionState, boolean isCurrentlyActive) {
            }
        });

        swipeToDismissTouchHelper.attachToRecyclerView(deviceListView);
        */

        registerForContextMenu(deviceListView);

        IntentFilter filterLocal = new IntentFilter();
        filterLocal.addAction(GBApplication.ACTION_LANGUAGE_CHANGE);
        filterLocal.addAction(GBApplication.ACTION_QUIT);
        filterLocal.addAction(DeviceManager.ACTION_DEVICES_CHANGED);
        filterLocal.addAction (GBApplication.ACTION_DISABLE_BABY_MONITOR);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filterLocal);

        refreshPairedDevices();

        /*
         * Ask for permission to intercept notifications on first run.
         */
        Prefs prefs = GBApplication.getPrefs();
        if (prefs.getBoolean("firstrun", true)) {
            prefs.getPreferences().edit().putBoolean("firstrun", false).apply();
            Intent enableIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(enableIntent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        ChangeLog cl = createChangeLog();
        if (false && cl.isFirstRun()) { // FIXME Отключёна возможность отображения окна со списком изменений приложения "LikeApp", во время его запуска
            try {
                cl.getLogDialog().show();
            } catch (Exception ignored){
                GB.toast(getBaseContext(), "Error showing Changelog", Toast.LENGTH_LONG, GB.ERROR);

            }
        }

        GBApplication.deviceService().start();

        if (GB.isBluetoothEnabled() && deviceList.isEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startActivity(new Intent(this, DiscoveryActivity.class));
        } else {
            GBApplication.deviceService().requestDeviceInfo();
        }

        babyMonitorProgress = findViewById (R.id.babyMonitorProgress);
        babyMonitorProgress.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener ()
        {
            @Override
            public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser)
            {
                updateBabyMonitorText ();
            }

            @Override
            public void onStartTrackingTouch (SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch (SeekBar seekBar)
            {
            }
        });

        babyMonitorLimit = findViewById (R.id.babyMonitorLimit);
        babyMonitorLimit.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener ()
        {
            @Override
            public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser)
            {
                MicReader.setLimit (progress);
                updateBabyMonitorText ();
            }

            @Override
            public void onStartTrackingTouch (SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch (SeekBar seekBar)
            {
            }
        });

        babyMonitorEnable = findViewById (R.id.babyMonitorEnable);
        babyMonitorEnable.setChecked (prefs.getBoolean ("BABY MONITOR ENABLED", false));
        babyMonitorEnable.setOnClickListener (new View.OnClickListener ()
        {
            @Override
            public void onClick (View v)
            {
                if (babyMonitorEnable.isChecked ())
                {
                    MicReader.start ();

                    NotificationSpec notificationSpec = new NotificationSpec ();
                    notificationSpec.type = NotificationType.BLUETOOTH;
                    notificationSpec.sender = getString (R.string.baby_monitor_enabled);
                    notificationSpec.subject = getString (R.string.baby_monitor_limit, (int) MicReader.getValue (), (int) MicReader.getLimit ());
                    GBApplication.deviceService ().onNotification (notificationSpec);
                }
                else
                {
                    MicReader.stop ();
                }
            }
        });

        babyMonitorLimit.setProgress (prefs.getInt ("BABY MONITOR LIMIT",  (int) MicReader.getLimit ()));
        babyMonitorEnable.callOnClick ();

        MicReader.addListener (this);

        findViewById (R.id.babyMonitor).setVisibility (MicReader.isEnabled () ? View.VISIBLE : View.GONE);
        updateBabyMonitorText ();

        String[] permissions = getIntent ().getStringArrayExtra ("PERMISSIONS");
        if (permissions != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions (permissions, 0);
            }
        }

        changeAd ();
    }

    private void changeAd ()
    {
        new Thread (new Runnable ()
        {
            @Override
            public void run ()
            {
                try
                {
                    String url = "https://likeapp.org/constr/?slogan&lang=" + Locale.getDefault ().getLanguage ();
                    HttpsURLConnection connection = (HttpsURLConnection) new URL (url).openConnection ();
                    connection.setRequestMethod ("GET");

                    try (BufferedReader in = new BufferedReader (new InputStreamReader (connection.getInputStream ())))
                    {
                        final String line = in.readLine ();
                        if (line != null && line.length () < 500)
                        {
                            runOnUiThread (new Runnable ()
                            {
                                @Override
                                public void run ()
                                {
                                    TextView ad = findViewById (R.id.ad);
                                    ad.setText (line);
                                }
                            });
                        }
                    }
                    catch (IOException ignored)
                    {
                    }

                    connection.disconnect ();
                }
                catch (IOException ignored)
                {
                }
            }
        }).start ();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLanguageInvalid) {
            isLanguageInvalid = false;
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        Prefs prefs = GBApplication.getPrefs();
        SharedPreferences.Editor editor = prefs.getPreferences ().edit ();
        editor.putBoolean ("BABY MONITOR ENABLED", babyMonitorEnable.isChecked ());
        editor.putInt ("BABY MONITOR LIMIT", babyMonitorLimit.getProgress ());
        editor.apply ();

        MicReader.stop ();
        MicReader.removeListener (this);
        unregisterForContextMenu(deviceListView);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MENU_REFRESH_CODE) {
            showFabIfNeccessary();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, MENU_REFRESH_CODE);
                return true;
            case R.id.action_debug:
                Intent debugIntent = new Intent(this, DebugActivity.class);
                startActivity(debugIntent);
                return true;
            case R.id.fetch_debug_logs:
                GBApplication.deviceService ().onFetchRecordedData (RecordedDataTypes.TYPE_DEBUGLOGS);
                return true;
            case R.id.action_db_management:
                Intent dbIntent = new Intent(this, DbManagementActivity.class);
                startActivity(dbIntent);
                return true;
            case R.id.action_blacklist:
                Intent blIntent = new Intent(this, AppBlacklistActivity.class);
                startActivity(blIntent);
                return true;
            case R.id.device_action_discover:
                launchDiscoveryActivity();
                return true;
            case R.id.action_quit:
                GBApplication.quit();
                return true;
            case R.id.donation_link:
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://liberapay.com/Gadgetbridge")); //TODO: centralize if ever used somewhere else
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return true;
            case R.id.external_changelog:
                ChangeLog cl = createChangeLog();
                try {
                    cl.getLogDialog().show();
                } catch (Exception ignored) {
                    GB.toast(getBaseContext(), "Error showing Changelog", Toast.LENGTH_LONG, GB.ERROR);
                }
                return true;
        }

        return true;
    }

    private ChangeLog createChangeLog() {
        String css = ChangeLog.DEFAULT_CSS;
        css += "body { "
                + "color: " + AndroidUtils.getTextColorHex(getBaseContext()) + "; "
                + "background-color: " + AndroidUtils.getBackgroundColorHex(getBaseContext()) + ";" +
                "}";
        return new ChangeLog(this, css);
    }

    private void launchDiscoveryActivity() {
        startActivity(new Intent(this, DiscoveryActivity.class));
    }

    private void refreshPairedDevices() {
        mGBDeviceAdapter.notifyDataSetChanged();
    }

    private void showFabIfNeccessary() {
        if (GBApplication.getPrefs().getBoolean("display_add_device_fab", true)) {
            fab.show();
        } else {
            if (deviceManager.getDevices().isEmpty ()) {
                fab.show();
            } else {
                fab.hide();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermissions() {
        List<String> wantedPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CONTACTS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.CALL_PHONE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CALL_LOG);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_PHONE_STATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.RECEIVE_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.SEND_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CALENDAR);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.RECORD_AUDIO);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.MEDIA_CONTENT_CONTROL) == PackageManager.PERMISSION_DENIED)
                wantedPermissions.add(Manifest.permission.MEDIA_CONTENT_CONTROL);
        } catch (Exception ignored){
        }

        if (!wantedPermissions.isEmpty())
            ActivityCompat.requestPermissions(this, wantedPermissions.toArray(new String[0]), 0);

        // HACK: On Lineage we have to do this so that the permission dialog pops up
        if (fakeStateListener == null) {
            fakeStateListener = new PhoneStateListener();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(fakeStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            telephonyManager.listen(fakeStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    public void setLanguage(Locale language, boolean invalidateLanguage) {
        if (invalidateLanguage) {
            isLanguageInvalid = true;
        }
        AndroidUtils.setLanguage(this, language);
    }

    @Override
    public void micLimitExceeded (double value)
    {
        long time = System.currentTimeMillis ();
        if (time - babyMonitorLastTime >= 5000)
        {
            babyMonitorLastTime = time;
            GBApplication.deviceService ().onFindDevice (true);
        }
    }

    @Override
    public void micUpdate (double value)
    {
        babyMonitorProgress.setProgress ((int) value);
    }

    private void updateBabyMonitorText ()
    {
        babyMonitorEnable.setText (getString (R.string.baby_monitor_limit, babyMonitorProgress.getProgress (), (int) MicReader.getLimit ()));
    }
}
