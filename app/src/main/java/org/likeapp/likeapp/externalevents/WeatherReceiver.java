package org.likeapp.likeapp.externalevents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.likeapp.likeapp.BuildConfig;
import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.impl.GBDevice;
import org.likeapp.likeapp.util.Weather;

import java.util.List;

public class WeatherReceiver extends BroadcastReceiver
{
  private static final String REQUEST_WEATHER = "org.likeapp.likeapp.REQUEST_WEATHER";
  private static final String SET_WEATHER = "org.likeapp.likeapp.SET_WEATHER";
  private static final String REQUEST_VERSION = "org.likeapp.likeapp.REQUEST_VERSION";

  @Override
  public void onReceive (Context context, Intent intent)
  {
    String action = intent.getAction ();
    if (SET_WEATHER.equals (action))
    {
      Weather.set (intent);
    }
    else if (REQUEST_WEATHER.equals (action))
    {
      List<GBDevice> devices = GBApplication.app ().getDeviceManager ().getDevices ();
      for (GBDevice device : devices)
      {
        Weather.update (device);
      }
    }
    // Если получен запрос версии
    else if (REQUEST_VERSION.equals (action))
    {
      context.sendBroadcast (new Intent ("org.likeapp.action.SET_VERSION").
        setPackage ("org.likeapp.action").
        putExtra ("application", "LikeApp").
        putExtra ("version", BuildConfig.VERSION_NAME), Weather.PERMISSIONS[0]);
    }
  }
}
