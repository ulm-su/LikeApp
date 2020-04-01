package org.likeapp.likeapp.externalevents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.likeapp.likeapp.util.Weather;

public class WeatherReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive (Context context, Intent intent)
  {
    Weather.set (intent);
  }
}
