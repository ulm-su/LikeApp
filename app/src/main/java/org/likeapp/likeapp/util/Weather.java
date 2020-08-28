package org.likeapp.likeapp.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.ActivityCompat;

import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.activities.ControlCenterv2;
import org.likeapp.likeapp.impl.GBDevice;
import org.likeapp.likeapp.model.ItemWithDetails;
import org.likeapp.likeapp.model.WeatherSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Weather
{
  private static final Logger LOG = LoggerFactory.getLogger (Weather.class);

  public static final String[] PERMISSIONS = new String[]
    {
      "org.likeapp.action.permission.WEATHER",
    };

  private static final String GET_WEATHER = "org.likeapp.action.GET_WEATHER";

  private static final String EXTRA_WEATHER = "weather";
  private static final String EXTRA_CITY = "weather_city";
  private static final String EXTRA_LANGUAGE = "weather_language";
  private static final String EXTRA_HAS_MOON = "weather_has_moon";
  private static final String EXTRA_HAS_FEELS_LIKE = "weather_has_feels_like";
  private static final String EXTRA_HAS_PRECIP_PROBABILITY = "weather_has_precip_probability";
  private static final String EXTRA_HAS_PHRASES = "weather_phrases";
  private static final String EXTRA_HAS_SHORT_PHRASES = "weather_short_phrases";
  private static final String EXTRA_SERVER = "weather_server";
  private static final String EXTRA_UPDATE_AGPS = "update_agps";
  private static final String EXTRA_UID = "UID";

  public static void update (GBDevice device)
  {
    LOG.info ("--- WEATHER: device " + device);
    Context context = GBApplication.getContext ();
//    if ("com.android.vending".equals (context.getPackageManager ().getInstallerPackageName (context.getPackageName ())))
    {
      String permission = PERMISSIONS[0];
      if (ActivityCompat.checkSelfPermission (context, permission) == PERMISSION_GRANTED)
      {
        Prefs prefs = GBApplication.getPrefs ();

        Intent intent = new Intent (GET_WEATHER);
        intent.setPackage ("org.likeapp.action");
        intent.putExtra (EXTRA_CITY, prefs.getString (EXTRA_CITY, null));
        intent.putExtra (EXTRA_LANGUAGE, prefs.getString (EXTRA_LANGUAGE, "en"));
        intent.putExtra (EXTRA_HAS_MOON, prefs.getBoolean (EXTRA_HAS_MOON, false));
        intent.putExtra (EXTRA_HAS_FEELS_LIKE, prefs.getBoolean (EXTRA_HAS_FEELS_LIKE, false));
        intent.putExtra (EXTRA_HAS_PRECIP_PROBABILITY, prefs.getBoolean (EXTRA_HAS_PRECIP_PROBABILITY, false));
        intent.putExtra (EXTRA_HAS_PHRASES, prefs.getBoolean (EXTRA_HAS_PHRASES, false));
        intent.putExtra (EXTRA_HAS_SHORT_PHRASES, prefs.getBoolean (EXTRA_HAS_SHORT_PHRASES, false));
        intent.putExtra (EXTRA_SERVER, prefs.getBoolean (EXTRA_SERVER, false));
        intent.putExtra (EXTRA_UPDATE_AGPS, prefs.getBoolean (EXTRA_UPDATE_AGPS, false));

        ItemWithDetails itemWithDetails = device.getDeviceInfo (GBDevice.DEVINFO_UID);
        intent.putExtra (EXTRA_UID, itemWithDetails != null ? itemWithDetails.getDetails () : null);

        context.sendBroadcast (intent, permission);
        LOG.info ("--- WEATHER: SEND");
      }
      else
      {
        LOG.info ("--- WEATHER: requestPermissions " + PERMISSIONS[0]);
        ControlCenterv2.requestPermissions (PERMISSIONS);
      }
    }
  }

  public static void set (Intent intent)
  {
    LOG.info ("--- WEATHER: SET " + intent);
//    if ("com.android.vending".equals (context.getPackageManager ().getInstallerPackageName (context.getPackageName ())))
    {
      WeatherSpec weatherSpec = intent.getParcelableExtra (EXTRA_WEATHER);
      org.likeapp.likeapp.model.Weather.getInstance ().setWeatherSpec (weatherSpec);
      GBApplication.deviceService ().onSendWeather (weatherSpec);
    }
  }
}
