/*  Copyright (C) 2017-2020 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti

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
package org.likeapp.likeapp.externalevents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.model.NotificationSpec;
import org.likeapp.likeapp.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmClockReceiver extends BroadcastReceiver {
    private static final Logger LOG = LoggerFactory.getLogger (AlarmClockReceiver.class);

    /**
     * AlarmActivity and AlarmService (when unbound) listen for this broadcast intent
     * so that other applications can snooze the alarm (after ALARM_ALERT_ACTION and before
     * ALARM_DONE_ACTION).
     */
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";

    /**
     * AlarmActivity and AlarmService listen for this broadcast intent so that other
     * applications can dismiss the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
     */
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";

    /** A public action sent by AlarmService when the alarm has started. */
    public static final String[] ALARM_ALERT_ACTIONS = new String[]
    {
        "times_up", // Сработал таймер (Lenovo)
        "com.android.alarmclock.ALARM_ALERT",
        "com.android.alarmclock.AlarmClock",
        "com.android.deskclock.ALARM_ALERT",
        "com.android.deskclock.AlarmClock",
        "com.android.deskclock.DeskClock",
        "com.android.deskclock.action.ALARM_ALERT",
        "com.android.deskclock.action.TIMER_EXPIRED",
        "com.android.deskclock.action.UPDATE_ALARM_INSTANCES",
        "com.android.deskclock.timer.TimerReceiver",
        "com.cn.google.AlertClock.ALARM_ALERT",
        "com.google.android.deskclock.action.ALARM_ALERT",
        "com.google.android.deskclock.action.TIMER_ALERT",
        "com.htc.android.worldclock.ALARM_ALERT",
        "com.htc.android.worldclock.intent.action.ALARM_ALERT",
        "com.htc.worldclock.ALARM_ALERT",
        "com.lenovo.deskclock.ALARM_ALERT",
        "com.lenovomobile.deskclock.ALARM_ALERT",
        "com.lge.clock.alarmclock.ALARM_ALERT",
        "com.lge.alarm.alarmclocknew",
        "com.motorola.blur.alarmclock.AlarmClock",
        "com.nubia.deskclock.ALARM_ALERT",
        "com.oppo.alarmclock.alarmclock.ALARM_ALERT",
        "com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT",
        "com.sonyericsson.alarm.ALARM_ALERT",
        "com.splunchy.android.alarmclock.ALARM_ALERT",
        "com.urbandroid.sleep.alarmclock.ALARM_ALERT",
        "com.zdworks.android.zdclock.ACTION_ALARM_ALERT",
    };

    /** A public action sent by AlarmService when the alarm has stopped for any reason. */
    public static final String[] ALARM_DONE_ACTIONS = new String[]
    {
        "alarm_killed",
        "notif_times_up_stop", // Выключен таймер (Lenovo)
        "com.android.alarmclock.ALARM_DONE",
        "com.android.alarmclock.alarm_killed",
        "com.android.deskclock.ALARM_DONE",
        "com.android.deskclock.action.ALARM_DONE",
        "com.android.deskclock.action.SET_INSTANCE_STATE",
        "com.cn.google.AlertClock.ALARM_DONE",
        "com.google.android.deskclock.action.ALARM_DONE",
        "com.google.android.deskclock.action.TIMER_DONE",
        "com.htc.android.worldclock.ALARM_DONE",
        "com.htc.android.worldclock.intent.action.ALARM_DONE",
        "com.htc.worldclock.ALARM_DONE",
        "com.lenovo.deskclock.ALARM_DONE",
        "com.lenovomobile.deskclock.ALARM_DONE",
        "com.lge.clock.alarmclock.ALARM_DONE",
        "com.oppo.alarmclock.alarmclock.ALARM_DONE",
        "com.samsung.sec.android.clockpackage.alarm.ALARM_DONE",
        "com.sonyericsson.alarm.ALARM_DONE",
    };

    private int lastId;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        for (String a : ALARM_ALERT_ACTIONS)
        {
            if (a.equals (action))
            {
                LOG.debug ("ALARM ALERT ACTION: " + a);
                sendAlarm (true);
                return;
            }
        }

        for (String a : ALARM_DONE_ACTIONS)
        {
            if (a.equals (action))
            {
                LOG.debug ("ALARM DONE ACTION: " + a);
                sendAlarm (false);
                return;
            }
        }
    }



    private synchronized void sendAlarm(boolean on) {
        dismissLastAlarm();
        if (on) {
            NotificationSpec notificationSpec = new NotificationSpec();
            //TODO: can we attach a dismiss action to the notification and not use the notification ID explicitly?
            lastId = notificationSpec.getId();
            notificationSpec.type = NotificationType.GENERIC_ALARM_CLOCK;
            notificationSpec.sourceName = "ALARMCLOCKRECEIVER";
            notificationSpec.attachedActions = new ArrayList<>();

            // DISMISS ALL action
            NotificationSpec.Action dismissAllAction = new NotificationSpec.Action();
            dismissAllAction.title = "Dismiss All";
            dismissAllAction.type = NotificationSpec.Action.TYPE_SYNTECTIC_DISMISS_ALL;
            notificationSpec.attachedActions.add(dismissAllAction);

            // can we get the alarm title somehow?
            GBApplication.deviceService().onNotification(notificationSpec);
        }
    }

    private void dismissLastAlarm() {
        if (lastId != 0) {
            GBApplication.deviceService().onDeleteNotification(lastId);
            lastId = 0;
        }
    }

}
