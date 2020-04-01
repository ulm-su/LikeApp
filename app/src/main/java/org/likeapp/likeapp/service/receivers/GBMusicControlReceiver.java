/*  Copyright (C) 2015-2020 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Gabe Schrecker, vanous

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
package org.likeapp.likeapp.service.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.SystemClock;
import android.view.KeyEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.deviceevents.GBDeviceEventMusicControl;
import org.likeapp.likeapp.externalevents.NotificationListener;
import org.likeapp.likeapp.util.Prefs;

public class GBMusicControlReceiver extends BroadcastReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(GBMusicControlReceiver.class);

    public static final String ACTION_MUSICCONTROL = "org.likeapp.likeapp.musiccontrol";

    @Override
    public void onReceive(Context context, Intent intent) {
        Prefs prefs = GBApplication.getPrefs ();
        if (!prefs.getBoolean ("audio_player_enabled", true))
        {
            return;
        }

        GBDeviceEventMusicControl.Event musicCmd = GBDeviceEventMusicControl.Event.values()[intent.getIntExtra("event", 0)];
        int keyCode = -1;
        int volumeAdjust = AudioManager.ADJUST_LOWER;

        switch (musicCmd) {
            case NEXT:
                keyCode = KeyEvent.KEYCODE_MEDIA_NEXT;
                break;
            case PREVIOUS:
                keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
                break;
            case PLAY:
                keyCode = KeyEvent.KEYCODE_MEDIA_PLAY;
                break;
            case PAUSE:
                keyCode = KeyEvent.KEYCODE_MEDIA_PAUSE;
                break;
            case PLAYPAUSE:
                keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                break;
            case REWIND:
                keyCode = KeyEvent.KEYCODE_MEDIA_REWIND;
                break;
            case FORWARD:
                keyCode = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
                break;
            case VOLUMEUP:
                // change default and fall through, :P
                volumeAdjust = AudioManager.ADJUST_RAISE;
            case VOLUMEDOWN:
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, volumeAdjust, 0);
                break;
            default:
                return;
        }

        if (keyCode != -1) {
            String audioPlayer = getAudioPlayer(context);

            LOG.debug("keypress: " + musicCmd.toString() + " sent to: " + audioPlayer);

            long eventtime = SystemClock.uptimeMillis();

            Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, keyCode, 0);
            downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
            if (!"default".equals(audioPlayer)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                {
                    MediaSessionManager mediaSessionManager = (MediaSessionManager) context.getSystemService (Context.MEDIA_SESSION_SERVICE);

                    try
                    {
                        boolean doStart = true;
                        List<MediaController> controllers = mediaSessionManager.getActiveSessions (new ComponentName (context, NotificationListener.class));
                        for (MediaController controller : controllers)
                        {
                            if (controller.getPackageName ().equals (audioPlayer))
                            {
                                doStart = false;
                                break;
                            }
                        }

                        if (doStart)
                        {
                            PackageManager pm = context.getPackageManager ();
                            Intent launchIntent = pm.getLaunchIntentForPackage (audioPlayer);
                            if (launchIntent != null)
                            {
                                LOG.debug ("Start activity for " + audioPlayer);
                                context.startActivity (launchIntent);
                                return;
                            }
                        }
                    }
                    catch (SecurityException ignore)
                    {
                    }
                }

                downIntent.setPackage(audioPlayer);
            }
            context.sendOrderedBroadcast(downIntent, null);

            Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, keyCode, 0);
            upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
            if (!"default".equals(audioPlayer)) {
                upIntent.setPackage(audioPlayer);
            }
            context.sendOrderedBroadcast(upIntent, null);
        }
    }

    private String getAudioPlayer(Context context) {
        Prefs prefs = GBApplication.getPrefs();
        String audioPlayer = prefs.getString("audio_player", "default");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            MediaSessionManager mediaSessionManager =
                    (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
            try {
                List<MediaController> controllers = mediaSessionManager.getActiveSessions(
                        new ComponentName(context, NotificationListener.class));

                if (!controllers.isEmpty ())
                {
                    MediaController controller = controllers.get(0);
                    audioPlayer = controller.getPackageName();
                }
            } catch (SecurityException e) {
                LOG.warn("No permission to get media sessions - did not grant notification access?", e);
            }
        }
        return audioPlayer;
    }
}
