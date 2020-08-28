package org.likeapp.likeapp.externalevents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class ActionReceiver extends BroadcastReceiver
{
  private static final String ACTION_SMS_SEND_TO = "org.likeapp.likeapp.ACTION_SMS_SEND_TO";

  @Override
  public void onReceive (Context context, Intent intent)
  {
    String action = intent.getAction ();
    if (ACTION_SMS_SEND_TO.equals (action))
    {
      SmsManager.getDefault ().sendTextMessage (intent.getStringExtra ("number"), null, intent.getStringExtra ("message"), null, null);
    }
  }
}
