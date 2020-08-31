/*  Copyright (C) 2015-2020 Andreas Böhler, Andreas Shimokawa, Carsten
    Pfeiffer, Daniele Gobbetti, José Rebelo, Sebastian Kranz, Taavi Eomäe

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
package org.likeapp.likeapp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import org.likeapp.likeapp.activities.DebugActivity;
import org.likeapp.likeapp.deviceevents.LikeAppDeviceEventDebugLog;
import org.likeapp.likeapp.deviceevents.LikeAppDeviceEventSleep;
import org.likeapp.likeapp.devices.miband.MiBandCoordinator;
import org.likeapp.likeapp.service.audio.MicReader;
import org.likeapp.likeapp.service.receivers.BabyMonitorReceiver;
import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.R;
import org.likeapp.likeapp.activities.FindPhoneActivity;
import org.likeapp.likeapp.activities.appmanager.AbstractAppManagerFragment;
import org.likeapp.likeapp.deviceevents.GBDeviceEvent;
import org.likeapp.likeapp.deviceevents.GBDeviceEventAppInfo;
import org.likeapp.likeapp.deviceevents.GBDeviceEventBatteryInfo;
import org.likeapp.likeapp.deviceevents.GBDeviceEventCallControl;
import org.likeapp.likeapp.deviceevents.GBDeviceEventDisplayMessage;
import org.likeapp.likeapp.deviceevents.GBDeviceEventFindPhone;
import org.likeapp.likeapp.deviceevents.GBDeviceEventFmFrequency;
import org.likeapp.likeapp.deviceevents.GBDeviceEventLEDColor;
import org.likeapp.likeapp.deviceevents.GBDeviceEventMusicControl;
import org.likeapp.likeapp.deviceevents.GBDeviceEventNotificationControl;
import org.likeapp.likeapp.deviceevents.GBDeviceEventScreenshot;
import org.likeapp.likeapp.deviceevents.GBDeviceEventVersionInfo;
import org.likeapp.likeapp.externalevents.NotificationListener;
import org.likeapp.likeapp.impl.GBDevice;
import org.likeapp.likeapp.model.BatteryState;
import org.likeapp.likeapp.service.receivers.GBCallControlReceiver;
import org.likeapp.likeapp.service.receivers.GBMusicControlReceiver;
import org.likeapp.likeapp.util.GB;
import org.likeapp.likeapp.util.Prefs;

import static org.likeapp.likeapp.util.GB.NOTIFICATION_CHANNEL_HIGH_PRIORITY_ID;
import static org.likeapp.likeapp.util.GB.NOTIFICATION_CHANNEL_ID;

// TODO: support option for a single reminder notification when notifications could not be delivered?
// conditions: app was running and received notifications, but device was not connected.
// maybe need to check for "unread notifications" on device for that.

/**
 * Abstract implementation of DeviceSupport with some implementations for
 * common functionality. Still transport independent.
 */
public abstract class AbstractDeviceSupport implements DeviceSupport {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDeviceSupport.class);
    private static final int NOTIFICATION_ID_SCREENSHOT = 8000;
    private static final String RINGER_MODE_ON_FELL_ASLEEP = "RINGER_MODE_ON_FELL_ASLEEP";

    protected GBDevice gbDevice;
    private BluetoothAdapter btAdapter;
    private Context context;
    private boolean autoReconnect;

    @Override
    public void setContext(GBDevice gbDevice, BluetoothAdapter btAdapter, Context context) {
        this.gbDevice = gbDevice;
        this.btAdapter = btAdapter;
        this.context = context;
    }

    /**
     * Default implementation just calls #connect()
     */
    @Override
    public boolean connectFirstTime() {
        return connect();
    }

    @Override
    public boolean isConnected() {
        return gbDevice.isConnected();
    }

    /**
     * Returns true if the device is not only connected, but also
     * initialized.
     *
     * @see GBDevice#isInitialized()
     */
    protected boolean isInitialized() {
        return gbDevice.isInitialized();
    }

    @Override
    public void setAutoReconnect(boolean enable) {
        autoReconnect = enable;
    }

    @Override
    public boolean getAutoReconnect() {
        return autoReconnect;
    }

    @Override
    public GBDevice getDevice() {
        return gbDevice;
    }

    @Override
    public BluetoothAdapter getBluetoothAdapter() {
        return btAdapter;
    }

    @Override
    public Context getContext() {
        return context;
    }

    public void evaluateGBDeviceEvent(GBDeviceEvent deviceEvent) {
        if (deviceEvent instanceof GBDeviceEventMusicControl) {
            handleGBDeviceEvent((GBDeviceEventMusicControl) deviceEvent);
        } else if (deviceEvent instanceof GBDeviceEventCallControl) {
            handleGBDeviceEvent((GBDeviceEventCallControl) deviceEvent);
        } else if (deviceEvent instanceof GBDeviceEventVersionInfo) {
            handleGBDeviceEvent((GBDeviceEventVersionInfo) deviceEvent);
        } else if (deviceEvent instanceof GBDeviceEventAppInfo) {
            handleGBDeviceEvent((GBDeviceEventAppInfo) deviceEvent);
        } else if (deviceEvent instanceof GBDeviceEventScreenshot) {
            handleGBDeviceEvent((GBDeviceEventScreenshot) deviceEvent);
        } else if (deviceEvent instanceof GBDeviceEventNotificationControl) {
            handleGBDeviceEvent((GBDeviceEventNotificationControl) deviceEvent);
        } else if (deviceEvent instanceof GBDeviceEventBatteryInfo) {
            handleGBDeviceEvent((GBDeviceEventBatteryInfo) deviceEvent);
        } else if (deviceEvent instanceof GBDeviceEventFindPhone) {
            handleGBDeviceEvent((GBDeviceEventFindPhone) deviceEvent);
        } else if (deviceEvent instanceof GBDeviceEventLEDColor) {
            handleGBDeviceEvent((GBDeviceEventLEDColor) deviceEvent);
        } else if (deviceEvent instanceof GBDeviceEventFmFrequency) {
            handleGBDeviceEvent((GBDeviceEventFmFrequency) deviceEvent);
        } else if (deviceEvent instanceof LikeAppDeviceEventDebugLog) {
            handleGBDeviceEvent((LikeAppDeviceEventDebugLog) deviceEvent);
        } else if (deviceEvent instanceof LikeAppDeviceEventSleep) {
            handleGBDeviceEvent((LikeAppDeviceEventSleep) deviceEvent);
        }
    }

    private void handleGBDeviceEvent(GBDeviceEventFindPhone deviceEvent) {
        Context context = getContext();
        LOG.info("Got GBDeviceEventFindPhone");
        switch (deviceEvent.event) {
            case START:
                handleGBDeviceEventFindPhoneStart();
                break;
            case STOP:
                Intent intent = new Intent(FindPhoneActivity.ACTION_FOUND);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                break;
            default:
                LOG.warn("unknown GBDeviceEventFindPhone");
        }
    }

    private void handleGBDeviceEventFindPhoneStart() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // this could be used if app in foreground // TODO: Below Q?
            Intent startIntent = new Intent(getContext(), FindPhoneActivity.class);
            startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startIntent);
        } else {
            handleGBDeviceEventFindPhoneStartNotification();
        }
    }

    private void handleGBDeviceEventFindPhoneStartNotification() {
        LOG.info("Got handleGBDeviceEventFindPhoneStartNotification");
        Intent intent = new Intent(context, FindPhoneActivity.class);

        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_HIGH_PRIORITY_ID );
        notification
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(false)
                .setFullScreenIntent(pi, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentTitle(  context.getString( R.string.find_my_phone_notification ) );
        notification.setGroup("BackgroundService");

        notificationManager.notify( GB.NOTIFICATION_ID_PHONE_FIND, notification.build());
    }


    private void handleGBDeviceEvent(GBDeviceEventMusicControl musicEvent) {
        Context context = getContext();
        LOG.info("Got event for MUSIC_CONTROL");
        Intent musicIntent = new Intent(GBMusicControlReceiver.ACTION_MUSICCONTROL);
        musicIntent.putExtra("event", musicEvent.event.ordinal());
        musicIntent.setPackage(context.getPackageName());
        context.sendBroadcast(musicIntent);
    }

    private void handleGBDeviceEvent(GBDeviceEventCallControl callEvent) {
        Context context = getContext();
        LOG.info("Got event for CALL_CONTROL");
        if(callEvent.event == GBDeviceEventCallControl.Event.IGNORE) {
            LOG.info("Sending intent for mute");
            Intent broadcastIntent = new Intent(context.getPackageName() + ".MUTE_CALL");
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
            return;
        }

        if(callEvent.event == GBDeviceEventCallControl.Event.REJECT)
        {
            // Если включена радионяня
            if (MicReader.isRunning ())
            {
                // Отправить запрос на отключение радионяни
                LOG.info ("Sending intent for disable baby monitor");
                Intent intent = new Intent (BabyMonitorReceiver.ACTION_BABY_MONITOR);
                intent.putExtra (BabyMonitorReceiver.DISABLE, true);
                LocalBroadcastManager.getInstance (context).sendBroadcast (intent);
                return;
            }
        }

        Intent callIntent = new Intent(GBCallControlReceiver.ACTION_CALLCONTROL);
        callIntent.putExtra("event", callEvent.event.ordinal());
        callIntent.setPackage(context.getPackageName());
        context.sendBroadcast(callIntent);
    }

    protected void handleGBDeviceEvent(GBDeviceEventVersionInfo infoEvent) {
        Context context = getContext();
        LOG.info("Got event for VERSION_INFO: " + infoEvent);
        if (gbDevice == null) {
            return;
        }
        gbDevice.setFirmwareVersion(infoEvent.fwVersion);
        gbDevice.setModel(infoEvent.hwVersion);
        gbDevice.sendDeviceUpdateIntent(context);
    }

    protected void handleGBDeviceEvent(GBDeviceEventLEDColor colorEvent) {
        Context context = getContext();
        LOG.info("Got event for LED Color: #" + Integer.toHexString(colorEvent.color).toUpperCase());
        if (gbDevice == null) {
            return;
        }
        gbDevice.setExtraInfo("led_color", colorEvent.color);
        gbDevice.sendDeviceUpdateIntent(context);
    }

    protected void handleGBDeviceEvent(GBDeviceEventFmFrequency frequencyEvent) {
        Context context = getContext();
        LOG.info("Got event for FM Frequency");
        if (gbDevice == null) {
            return;
        }
        gbDevice.setExtraInfo("fm_frequency", frequencyEvent.frequency);
        gbDevice.sendDeviceUpdateIntent(context);
    }

    private void handleGBDeviceEvent(GBDeviceEventAppInfo appInfoEvent) {
        Context context = getContext();
        LOG.info("Got event for APP_INFO");

        Intent appInfoIntent = new Intent(AbstractAppManagerFragment.ACTION_REFRESH_APPLIST);
        int appCount = appInfoEvent.apps.length;
        appInfoIntent.putExtra("app_count", appCount);
        for (int i = 0; i < appCount; i++) {
            appInfoIntent.putExtra("app_name" + i, appInfoEvent.apps[i].getName());
            appInfoIntent.putExtra("app_creator" + i, appInfoEvent.apps[i].getCreator());
            appInfoIntent.putExtra("app_uuid" + i, appInfoEvent.apps[i].getUUID().toString());
            appInfoIntent.putExtra("app_type" + i, appInfoEvent.apps[i].getType().ordinal());
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(appInfoIntent);
    }

    private void handleGBDeviceEvent(GBDeviceEventScreenshot screenshot) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-hhmmss", Locale.US);
        String filename = "screenshot_" + dateFormat.format(new Date()) + ".bmp";

        try {
            String fullpath = GB.writeScreenshot(screenshot, filename);
            Bitmap bmp = BitmapFactory.decodeFile(fullpath);
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            Uri screenshotURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".screenshot_provider", new File(fullpath));
            intent.setDataAndType(screenshotURI, "image/*");

            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, screenshotURI);

            PendingIntent pendingShareIntent = PendingIntent.getActivity(context, 0, Intent.createChooser(shareIntent, "share screenshot"),
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action action = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_share, "share", pendingShareIntent).build();

            Notification notif = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("Screenshot taken")
                    .setTicker("Screenshot taken")
                    .setContentText(filename)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bmp))
                    .setContentIntent(pIntent)
                    .addAction(action)
                    .setAutoCancel(true)
                    .build();

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID_SCREENSHOT, notif);
        } catch (IOException ex) {
            LOG.error("Error writing screenshot", ex);
        }
    }

    private void handleGBDeviceEvent(GBDeviceEventNotificationControl deviceEvent) {
        Context context = getContext();
        LOG.info("Got NOTIFICATION CONTROL device event");
        String action = null;
        switch (deviceEvent.event) {
            case DISMISS:
                action = NotificationListener.ACTION_DISMISS;
                break;
            case DISMISS_ALL:
                action = NotificationListener.ACTION_DISMISS_ALL;
                break;
            case OPEN:
                action = NotificationListener.ACTION_OPEN;
                break;
            case MUTE:
                action = NotificationListener.ACTION_MUTE;
                break;
            case REPLY:
                if (deviceEvent.phoneNumber == null) {
                    deviceEvent.phoneNumber = (String) GBApplication.getIDSenderLookup().lookup((int) (deviceEvent.handle >> 4));
                }
                if (deviceEvent.phoneNumber != null) {
                    LOG.info("Got notification reply for SMS from " + deviceEvent.phoneNumber + " : " + deviceEvent.reply);
                    SmsManager.getDefault().sendTextMessage(deviceEvent.phoneNumber, null, deviceEvent.reply, null, null);
                } else {
                    LOG.info("Got notification reply for notification id " + deviceEvent.handle + " : " + deviceEvent.reply);
                    action = NotificationListener.ACTION_REPLY;
                }
                break;
        }
        if (action != null) {
            Intent notificationListenerIntent = new Intent(action);
            notificationListenerIntent.putExtra("handle", deviceEvent.handle);
            notificationListenerIntent.putExtra("title", deviceEvent.title);
            if (deviceEvent.reply != null) {
                Prefs prefs = GBApplication.getPrefs();
                String suffix = prefs.getString("canned_reply_suffix", null);
                if (suffix != null && !Objects.equals(suffix, "")) {
                    deviceEvent.reply += suffix;
                }
                notificationListenerIntent.putExtra("reply", deviceEvent.reply);
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(notificationListenerIntent);
        }
    }

    protected void handleGBDeviceEvent(GBDeviceEventBatteryInfo deviceEvent) {
        Context context = getContext();
        LOG.info("Got BATTERY_INFO device event");
        gbDevice.setBatteryLevel(deviceEvent.level);
        gbDevice.setBatteryState(deviceEvent.state);
        gbDevice.setBatteryVoltage(deviceEvent.voltage);

        if (deviceEvent.lastChargeTime != null)
        {
            int offsetInHours = MiBandCoordinator.getDeviceTimeOffsetHours (gbDevice.getAddress ());
            gbDevice.setBatteryLife ((int) ((System.currentTimeMillis () - deviceEvent.lastChargeTime.getTimeInMillis ()) / (1000 * 60 * 60)) - offsetInHours);
        }

        if (deviceEvent.level == GBDevice.BATTERY_UNKNOWN) {
            // no level available, just "high" or "low"
            if (BatteryState.BATTERY_LOW.equals(deviceEvent.state)) {
                GB.updateBatteryNotification(context.getString(R.string.notif_battery_low, gbDevice.getName()),
                        deviceEvent.extendedInfoAvailable() ?
                                context.getString(R.string.notif_battery_low_extended, gbDevice.getName(),
                                        context.getString(R.string.notif_battery_low_bigtext_last_charge_time, DateFormat.getDateTimeInstance().format(deviceEvent.lastChargeTime.getTime())) +
                                        context.getString(R.string.notif_battery_low_bigtext_number_of_charges, String.valueOf(deviceEvent.numCharges)))
                                : ""
                        , context);
            } else {
                GB.removeBatteryNotification(context);
            }
        } else {
            //show the notification if the battery level is below threshold and only if not connected to charger
            if (deviceEvent.level <= gbDevice.getBatteryThresholdPercent() &&
                    (BatteryState.BATTERY_LOW.equals(deviceEvent.state) ||
                            BatteryState.BATTERY_NORMAL.equals(deviceEvent.state))
                    ) {
                GB.updateBatteryNotification(context.getString(R.string.notif_battery_low_percent, gbDevice.getName(), String.valueOf(deviceEvent.level)),
                        deviceEvent.extendedInfoAvailable() ?
                                context.getString(R.string.notif_battery_low_percent, gbDevice.getName(), String.valueOf(deviceEvent.level)) + "\n" +
                                        context.getString(R.string.notif_battery_low_bigtext_last_charge_time, DateFormat.getDateTimeInstance().format(deviceEvent.lastChargeTime.getTime())) +
                                        context.getString(R.string.notif_battery_low_bigtext_number_of_charges, String.valueOf(deviceEvent.numCharges))
                                : ""
                        , context);
            }
            else if ((deviceEvent.level == 100 && BatteryState.BATTERY_CHARGING.equals (deviceEvent.state)) || BatteryState.BATTERY_CHARGING_FULL.equals (deviceEvent.state))
            {
                String text = context.getString (R.string.notif_battery_low_percent, gbDevice.getName (), String.valueOf (deviceEvent.level));
                GB.updateFullBatteryNotification (text, deviceEvent.extendedInfoAvailable ()
                    ? text + "\n" +
                    context.getString (R.string.notif_battery_low_bigtext_last_charge_time, DateFormat.getDateTimeInstance ().format (deviceEvent.lastChargeTime.getTime ())) +
                    context.getString (R.string.notif_battery_low_bigtext_number_of_charges, String.valueOf (deviceEvent.numCharges))
                    : ""
                  , context);
            } else {
                GB.removeBatteryNotification(context);
            }
        }

        gbDevice.sendDeviceUpdateIntent(context);
    }

    public void handleGBDeviceEvent(GBDeviceEventDisplayMessage message) {
        GB.log(message.message, message.severity, null);

        Intent messageIntent = new Intent(GB.ACTION_DISPLAY_MESSAGE);
        messageIntent.putExtra(GB.DISPLAY_MESSAGE_MESSAGE, message.message);
        messageIntent.putExtra(GB.DISPLAY_MESSAGE_DURATION, message.duration);
        messageIntent.putExtra(GB.DISPLAY_MESSAGE_SEVERITY, message.severity);

        LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);
    }

    private void handleGBDeviceEvent (LikeAppDeviceEventDebugLog event)
    {
        Context context = getContext ();
        Intent intent = new Intent (DebugActivity.ACTION_DEBUG);

        if (event.log != null)
        {
            intent.putExtra (DebugActivity.EXTRA_LOG, event.log);
        }

        if (event.displaySize != null)
        {
            intent.putExtra (DebugActivity.EXTRA_DISPLAY_SIZE, event.displaySize);
        }

        if (event.displayData != null)
        {
            intent.putExtra (DebugActivity.EXTRA_DISPLAY_DATA, event.displayData);
        }

        if (event.version != null)
        {
            intent.putExtra (DebugActivity.EXTRA_VERSION, event.version);
        }

        if (event.value != null)
        {
            intent.putExtra (DebugActivity.EXTRA_VALUE, event.value);
        }

        intent.setPackage (context.getPackageName ());
        context.sendBroadcast (intent);
    }

    public String customStringFilter(String inputString) {
        return inputString;
    }

    private void handleGBDeviceEvent (LikeAppDeviceEventSleep event)
    {
        if (event.fellAsleep)
        {
            Context context = getContext ();
            String fell_asleep = GBApplication.getPrefs ().getString ("pref_fell_asleep", context.getString (R.string.p_fell_asleep_off));

            AudioManager audioManager = (AudioManager) context.getSystemService (Context.AUDIO_SERVICE);
            int ringerMode = audioManager.getRingerMode ();
            int ringerModeNew = -1;

            if (fell_asleep.equals (context.getString (R.string.p_fell_asleep_silent)))
            {
                ringerModeNew = AudioManager.RINGER_MODE_SILENT;
            }
            else if (fell_asleep.equals (context.getString (R.string.p_fell_asleep_vibtare)))
            {
                ringerModeNew = AudioManager.RINGER_MODE_VIBRATE;
            }

            if (ringerModeNew >=0)// && ringerMode != ringerModeNew)
            {
                audioManager.setRingerMode (ringerModeNew);

                Prefs prefs = new Prefs (GBApplication.getDeviceSpecificSharedPrefs (gbDevice.getAddress ()));
                SharedPreferences.Editor editor = prefs.getPreferences ().edit ();
                editor.putInt (RINGER_MODE_ON_FELL_ASLEEP, ringerMode);
                editor.apply ();
            }
        }

        if (event.wokeUp)
        {
            Context context = getContext ();
            String woke_up = GBApplication.getPrefs ().getString ("pref_woke_up", context.getString (R.string.p_woke_up_off));
            if (woke_up.equals (context.getString (R.string.p_woke_up_restore_ringer_mode)))
            {
                AudioManager audioManager = (AudioManager) context.getSystemService (Context.AUDIO_SERVICE);

                Prefs prefs = new Prefs (GBApplication.getDeviceSpecificSharedPrefs (gbDevice.getAddress ()));
                int ringerMode = prefs.getInt (RINGER_MODE_ON_FELL_ASLEEP, -1);

                if (ringerMode >= 0)
                {
                    audioManager.setRingerMode (ringerMode);
                }
            }
            else if (woke_up.equals (context.getString (R.string.pref_woke_up_ringer_normal)))
            {
                AudioManager audioManager = (AudioManager) context.getSystemService (Context.AUDIO_SERVICE);
                audioManager.setRingerMode (AudioManager.RINGER_MODE_NORMAL);
            }
            else if (woke_up.equals (context.getString (R.string.pref_woke_up_ringer_vibrate)))
            {
                AudioManager audioManager = (AudioManager) context.getSystemService (Context.AUDIO_SERVICE);
                audioManager.setRingerMode (AudioManager.RINGER_MODE_VIBRATE);
            }
        }
    }
}
