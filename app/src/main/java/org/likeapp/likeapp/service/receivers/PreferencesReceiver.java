package org.likeapp.likeapp.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.likeapp.likeapp.BuildConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PreferencesReceiver extends BroadcastReceiver
{
  public static final String ACTION_PREFERENCES_SAVE = "org.likeapp.PREFERENCES_SAVE";
  public static final String ACTION_PREFERENCES_RESTORE = "org.likeapp.PREFERENCES_RESTORE";
  public static final String ACTION_PREFERENCES_REQUEST = "org.likeapp.PREFERENCES_REQUEST";

  public static final String EXTRA_PREF_NAME = "__PREF_NAME__";
  public static final String[] EMPTY = new String[0];

  @Override
  public void onReceive (Context context, Intent intent)
  {
    String action = intent.getAction ();
    if (ACTION_PREFERENCES_SAVE.equals (action))
    {
      savePreferences (context, intent, action);
    }
    else if (ACTION_PREFERENCES_RESTORE.equals (action))
    {
      restorePreferences (context, intent);
    }
    else if (ACTION_PREFERENCES_REQUEST.equals (action))
    {
      requestPreferences (context, intent);
    }
  }

  private void requestPreferences (Context context, Intent intent)
  {
    String prefName = intent.getStringExtra (EXTRA_PREF_NAME);
    if (prefName != null)
    {
      //noinspection ConstantConditions
      sendPreferencesTo (context, ACTION_PREFERENCES_RESTORE, "org.likeapp.likeapp".equals (BuildConfig.APPLICATION_ID) ? "org.likeapp.action" : "org.likeapp.likeapp", prefName, null);
    }
  }

  private void savePreferences (Context context, Intent intent, String prefix)
  {
    String prefName = intent.getStringExtra (EXTRA_PREF_NAME);
    if (prefName != null)
    {
      Bundle bundle = intent.getExtras ();
      assert bundle != null;

      SharedPreferences.Editor editor = context.getApplicationContext ().getSharedPreferences (prefix + prefName, Context.MODE_PRIVATE).edit ();
      editor.clear ();

      Set<String> keys = bundle.keySet ();
      for (String key : keys)
      {
        if (!key.equals (EXTRA_PREF_NAME))
        {
          Object o = bundle.get (key);
          if (o instanceof String[])
          {
            editor.putStringSet (key, new HashSet<> (Arrays.asList ((String[]) o)));
          }
          else if (o instanceof String)
          {
            editor.putString (key, (String) o);
          }
          else if (o instanceof Integer)
          {
            editor.putInt (key, (int) o);
          }
          else if (o instanceof Long)
          {
            editor.putLong (key, (long) o);
          }
          else if (o instanceof Float)
          {
            editor.putFloat (key, (float) o);
          }
          else if (o instanceof Boolean)
          {
            editor.putBoolean (key, (boolean) o);
          }
        }
      }

      editor.apply ();
    }
  }

  private void restorePreferences (Context context, Intent intent)
  {
    savePreferences (context, intent, "");
  }

  public static void sendPreferencesTo (Context context, String packageName, String prefName, String permission)
  {
    sendPreferencesTo (context, ACTION_PREFERENCES_SAVE, packageName, prefName, permission);
  }

  private static void sendPreferencesTo (Context context, String action, String packageName, String prefName, String permission)
  {
    String prefNameSaved = ACTION_PREFERENCES_SAVE.equals (action) ? prefName : ACTION_PREFERENCES_SAVE + prefName;
    SharedPreferences prefs = context.getApplicationContext ().getSharedPreferences (prefNameSaved, Context.MODE_PRIVATE);
    Map<String, ?> all = prefs.getAll ();
    if (!all.isEmpty ())
    {
      Intent intent = new Intent (action).putExtra (EXTRA_PREF_NAME, prefName).setPackage (packageName);

      Set<String> keys = all.keySet ();
      for (String key : keys)
      {
        if (!EXTRA_PREF_NAME.equals (key))
        {
          Object o = all.get (key);
          if (o instanceof Set)
          {
            //noinspection unchecked
            intent.putExtra (key, ((Set<String>) o).toArray (EMPTY));
          }
          else if (o instanceof String)
          {
            intent.putExtra (key, (String) o);
          }
          else if (o instanceof Integer)
          {
            intent.putExtra (key, (int) o);
          }
          else if (o instanceof Long)
          {
            intent.putExtra (key, (long) o);
          }
          else if (o instanceof Float)
          {
            intent.putExtra (key, (float) o);
          }
          else if (o instanceof Boolean)
          {
            intent.putExtra (key, (boolean) o);
          }
        }
      }

      context.sendBroadcast (intent, permission);
    }
  }

  public static void requestPreferencesIfEmpty (Context context, String packageName, String prefName, String permission)
  {
    SharedPreferences prefs = context.getApplicationContext ().getSharedPreferences ("PreferencesReceiver", Context.MODE_PRIVATE);
    if (!prefs.contains (prefName))
    {
      if (context.getApplicationContext ().getSharedPreferences (prefName, Context.MODE_PRIVATE).getAll ().isEmpty ())
      {
        context.sendBroadcast (new Intent (ACTION_PREFERENCES_REQUEST).putExtra (EXTRA_PREF_NAME, prefName).setPackage (packageName), permission);
      }
      else
      {
        prefs.edit ().putBoolean (prefName, true).apply ();
      }
    }
  }
}
