/*  Copyright (C) 2015-2020 Andreas Shimokawa, Carsten Pfeiffer

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

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.os.Handler;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;


import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.externalevents.NotificationListener;
import org.likeapp.likeapp.util.Prefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.likeapp.likeapp.deviceevents.GBDeviceEventCallControl;

import java.util.List;

public class GBCallControlReceiver extends BroadcastReceiver
{
    public static final String ACTION_CALLCONTROL = "org.likeapp.likeapp.callcontrol";
    private static final Logger LOG = LoggerFactory.getLogger (GBCallControlReceiver.class);

    private static final KeyEvent HEADSETHOOK_DOWN = new KeyEvent (KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK);
    private static final KeyEvent HEADSETHOOK_UP = new KeyEvent (KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);

    @Override
    public void onReceive (final Context context, Intent intent)
    {
      GBDeviceEventCallControl.Event callCmd = GBDeviceEventCallControl.Event.values ()[intent.getIntExtra ("event", 0)];
      switch (callCmd)
      {
        case END:
        case REJECT:
        {
          boolean success = false;
          try
          {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
              TelecomManager tm = (TelecomManager) context.getApplicationContext ().getSystemService (Context.TELECOM_SERVICE);
              if (tm != null)
              {
                LOG.debug ("Manifest.permission.ANSWER_PHONE_CALLS");
                if (context.checkSelfPermission (Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED)
                {
                  LOG.debug ("tm.endCall()");
                  tm.endCall ();
                  success = true;
                }
              }
            }

            if (!success)
            {
              LOG.debug ("TelephonyManager.endCall()");
              TelephonyManager tm = (TelephonyManager) context.getApplicationContext ().getSystemService (Context.TELEPHONY_SERVICE);
              if (tm != null)
              {
                LOG.debug ("tm.endCall()");
                tm.getClass ().getMethod ("endCall").invoke (tm);
              }
            }
          }
          catch (Exception e)
          {
            LOG.warn ("could not hangup call", e);
          }
          catch (NoSuchMethodError e)
          {
            LOG.error ("could not hangup call", e);
          }

          break;
        }

        case START:
        case ACCEPT:
        {
          boolean success = false;
          LOG.debug ("Start call ...");
          setSpeakerOn (context);

          try
          {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
              TelecomManager tm = (TelecomManager) context.getApplicationContext ().getSystemService (Context.TELECOM_SERVICE);
              if (tm != null)
              {
                LOG.debug ("Manifest.permission.ANSWER_PHONE_CALLS");
                if (context.checkSelfPermission (Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED)
                {
                  LOG.debug ("tm.acceptRingingCall()");
                  tm.acceptRingingCall ();
                  success = true;
                }
              }
            }

            if (!success)
            {
              LOG.debug ("TelephonyManager.answerRingingCall()");
              TelephonyManager tm = (TelephonyManager) context.getApplicationContext ().getSystemService (Context.TELEPHONY_SERVICE);
              if (tm != null)
              {
                LOG.debug ("tm.answerRingingCall()");
                tm.getClass ().getMethod ("answerRingingCall").invoke (tm);
                success = true;
              }
            }
          }
          catch (Exception e)
          {
            LOG.warn ("could not start call", e);
          }
          catch (NoSuchMethodError e)
          {
            LOG.error ("could not start call", e);
          }

          if (!success)
          {
            try
            {
              success = sendHeadsetHookLollipop (context);
            }
            catch (Exception e)
            {
              LOG.warn ("could not start call", e);
            }

            if (!success)
            {
              new Handler ().post (new Runnable ()
              {
                @Override
                public void run ()
                {
                  LOG.debug ("Run KEYCODE_HEADSETHOOK");
                  context.sendOrderedBroadcast (new Intent (Intent.ACTION_MEDIA_BUTTON).putExtra (Intent.EXTRA_KEY_EVENT, HEADSETHOOK_DOWN), "android.permission.CALL_PRIVILEGED");
                  context.sendOrderedBroadcast (new Intent (Intent.ACTION_MEDIA_BUTTON).putExtra (Intent.EXTRA_KEY_EVENT, HEADSETHOOK_UP), "android.permission.CALL_PRIVILEGED");
                }
              });
            }
          }

          if (success)
          {
            LOG.debug ("Start call success");
            setSpeakerOn (context);
          }

          break;
        }
      }
    }

    private boolean sendHeadsetHookLollipop (Context context)
    {
        boolean success = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            MediaSessionManager mediaSessionManager = (MediaSessionManager) context.getApplicationContext ().getSystemService (Context.MEDIA_SESSION_SERVICE);
            List<MediaController> mediaControllerList = mediaSessionManager.getActiveSessions (new ComponentName (context.getApplicationContext (), NotificationListener.class));

            for (MediaController m : mediaControllerList)
            {
                if ("com.android.server.telecom".equals (m.getPackageName ()))
                {
                    LOG.debug ("HEADSETHOOK sent to telecom server");
                    m.dispatchMediaButtonEvent (HEADSETHOOK_DOWN);
                    m.dispatchMediaButtonEvent (HEADSETHOOK_UP);
                    success = true;
                    break;
                }
            }
        }

        return success;
    }

    private void setSpeakerOn (Context context)
    {
      LOG.debug ("setSpeakerOn()");
      Prefs prefs = GBApplication.getPrefs ();
      if ("answer_speakerphone".equals (prefs.getString ("notification_mode_calls_press_button", "disabled")))
      {
        LOG.debug ("Enable SPEAKER");
        AudioManager audioManager = (AudioManager) context.getApplicationContext ().getSystemService (Context.AUDIO_SERVICE);

        if (!audioManager.isSpeakerphoneOn ())
        {
//          int mode = AudioManager.MODE_IN_CALL; // this doesnt work without android.permission.MODIFY_PHONE_STATE
          int mode = AudioManager.MODE_NORMAL; // weirdly this works. This is important

          LOG.debug ("MODE " + mode);
          audioManager.setMode (mode);
          audioManager.setSpeakerphoneOn (true);

          try
          {
            int volume = Integer.parseInt (prefs.getString ("notification_mode_calls_speaker_volume", "no"));
            LOG.debug ("VOLUME " + volume);
            audioManager.setStreamVolume (AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume (AudioManager.STREAM_VOICE_CALL) * volume / 100, 0/*AudioManager.FLAG_SHOW_UI*/);
          }
          catch (NumberFormatException ignore)
          {
          }

          // note the phone interface won't show speaker phone is enabled
          // but the phone speaker will be on
          // remember to turn it back off when your done ;)
        }
      }
    }
}
