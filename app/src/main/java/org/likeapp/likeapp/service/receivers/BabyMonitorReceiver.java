package org.likeapp.likeapp.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.likeapp.likeapp.activities.ControlCenterv2;

public class BabyMonitorReceiver extends BroadcastReceiver
{
  public static final String ACTION_BABY_MONITOR = "org.likeapp.likeapp.BABY_MONITOR";
  public static final String DISABLE = "disable";
  public static final String ENABLE = "enable";
  public static final String LIMIT_UP = "limit up";
  public static final String LIMIT_DOWN = "limit down";

  @Override
  public void onReceive (Context context, Intent intent)
  {
    if (ACTION_BABY_MONITOR.equals (intent.getAction ()))
    {
      Bundle extras = intent.getExtras ();
      if (extras != null)
      {
        if (intent.hasExtra (BabyMonitorReceiver.ENABLE) && !ControlCenterv2.isLive ())
        {
          context.startActivity (new Intent (context, ControlCenterv2.class).addFlags (Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        LocalBroadcastManager.getInstance (context).sendBroadcast (new Intent (ACTION_BABY_MONITOR).putExtras (extras));
      }
    }
  }
}
