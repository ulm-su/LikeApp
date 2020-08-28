package org.likeapp.likeapp.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.likeapp.likeapp.BuildConfig;
import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.R;
import org.likeapp.likeapp.activities.ControlCenterv2;
import org.likeapp.likeapp.activities.FwAppInstallerActivity;
import org.likeapp.likeapp.activities.devicesettings.DeviceSettingsActivity;
import org.likeapp.likeapp.devices.DeviceManager;
import org.likeapp.likeapp.devices.huami.HuamiConst;
import org.likeapp.likeapp.impl.GBDevice;
import org.likeapp.likeapp.model.ItemWithDetails;
import org.likeapp.likeapp.util.GB;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.likeapp.likeapp.impl.GBDevice.DEVINFO_HW_VER;
import static org.likeapp.likeapp.impl.GBDevice.DEVINFO_SN;
import static org.likeapp.likeapp.model.DeviceType.AMAZFITBIP;

public class LikeAppActionInstallReceiver extends BroadcastReceiver
{
  private static final String OPEN_DEVICE_SETTINGS = BuildConfig.APPLICATION_ID + ".OPEN_DEVICE_SETTINGS";
  private static final String DISPLAY_ITEMS_SORT = BuildConfig.APPLICATION_ID + ".DISPLAY_ITEMS_SORT";
  private static final String GET_SELECTED_DEVICE = BuildConfig.APPLICATION_ID + ".GET_SELECTED_DEVICE";
  private static final String INSTALL_DEVICE_INFO = "org.likeapp.action.INSTALL_DEVICE_INFO";
  private static final String INSTALL_STATUS = "org.likeapp.action.INSTALL_STATUS";

  private static final String EXTRA_TOKEN = "TOKEN";
  private static final String EXTRA_NAME = "NAME";
  private static final String EXTRA_TYPE = "TYPE";
  private static final String EXTRA_SN = "SN";
  private static final String EXTRA_VERSION_HW = "VERSION_HW";
  private static final String EXTRA_STATUS = "STATUS";

  @Override
  public void onReceive (Context context, Intent intent)
  {
//    if ("com.android.vending".equals (context.getPackageManager ().getInstallerPackageName (context.getPackageName ())))
    {
      String action = intent.getAction ();
      if (GET_SELECTED_DEVICE.equals (action))
      {
        String token = intent.getStringExtra (EXTRA_TOKEN);
        if (token != null)
        {
          GBApplication app = GBApplication.app ();
          if (app != null)
          {
            DeviceManager dm = app.getDeviceManager ();
            if (dm != null)
            {
              GBDevice device = dm.getSelectedDevice ();
              if (device != null && device.isConnected ())
              {
                if (device.hasBatteryLevelForInstall ())
                {
                  List<ItemWithDetails> details = device.getDeviceInfos ();
                  if (details != null)
                  {
                    String versionHW = null;
                    String sn = null;
                    for (ItemWithDetails item : details)
                    {
                      String name = item.getName ();
                      if (DEVINFO_HW_VER.equals (name))
                      {
                        versionHW = item.getDetails ();
                      }
                      else if (DEVINFO_SN.equals (name))
                      {
                        sn = item.getDetails ();
                      }
                    }

                    if (sn != null && versionHW != null)
                    {
                      String permission = FwAppInstallerActivity.PERMISSIONS[0];
                      if (ActivityCompat.checkSelfPermission (context, permission) == PERMISSION_GRANTED)
                      {
                        Intent intentSend = new Intent (INSTALL_DEVICE_INFO);
                        intentSend.setPackage ("org.likeapp.action");
                        intentSend.putExtra (EXTRA_NAME, device.getName ());
                        intentSend.putExtra (EXTRA_TYPE, device.getType ().getKey ());
                        intentSend.putExtra (EXTRA_TOKEN, token);
                        intentSend.putExtra (EXTRA_SN, sn);
                        intentSend.putExtra (EXTRA_VERSION_HW, versionHW);
                        context.sendBroadcast (intentSend, permission);
                      }
                      else
                      {
                        ControlCenterv2.requestPermissions (FwAppInstallerActivity.PERMISSIONS);
                      }
                    }
                    else
                    {
                      sendMessageNotConnected (context, token);
                    }
                  }
                  else
                  {
                    sendMessageNotConnected (context, token);
                  }
                }
                else
                {
                  sendMessageLowBattery (context, token);
                }
              }
              else
              {
                sendMessageNotConnected (context, token);
              }
            }
            else
            {
              sendMessageNotStarted (context, token);
            }
          }
          else
          {
            sendMessageNotStarted (context, token);
          }
        }
        else
        {
          GB.toast (context, context.getString (R.string.ota_token_invalid), Toast.LENGTH_SHORT, GB.INFO);
        }
      }
      else if (DISPLAY_ITEMS_SORT.equals (action))
      {
        GBApplication app = GBApplication.app ();
        if (app != null)
        {
          DeviceManager dm = app.getDeviceManager ();
          if (dm != null)
          {
            List<GBDevice> devices = dm.getDevices ();
            if (devices != null)
            {
              for (GBDevice d : devices)
              {
                if (d.getType () == AMAZFITBIP)
                {
                  Set<String> enables = new HashSet<> (Arrays.asList (intent.getStringArrayExtra (HuamiConst.PREF_DISPLAY_ITEMS)));
                  String sorted = intent.getStringExtra (HuamiConst.PREF_DISPLAY_ITEMS_SORT);
                  boolean shortcutsExchange = intent.getBooleanExtra (HuamiConst.PREF_DISPLAY_SHORTCUTS_EXCHANGE, false);

                  GBApplication.getDeviceSpecificSharedPrefs (d.getAddress ()).edit ()
                    .putStringSet (HuamiConst.PREF_DISPLAY_ITEMS, enables)
                    .putString (HuamiConst.PREF_DISPLAY_ITEMS_SORT, sorted)
                    .putBoolean (HuamiConst.PREF_DISPLAY_SHORTCUTS_EXCHANGE, shortcutsExchange)
                    .apply ();

                  GBApplication.deviceService ().onSendConfiguration (HuamiConst.PREF_DISPLAY_ITEMS);
                }
              }
            }
          }
        }
      }
      else if (OPEN_DEVICE_SETTINGS.equals (action))
      {
        GBApplication app = GBApplication.app ();
        DeviceManager dm = app.getDeviceManager ();
        if (dm != null)
        {
          List<GBDevice> devices = dm.getDevices ();
          if (devices != null)
          {
            for (GBDevice d : devices)
            {
              if (d.getType () == AMAZFITBIP)
              {
                context.startActivity (new Intent (context, DeviceSettingsActivity.class)
                  .addFlags (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                  .putExtra (GBDevice.EXTRA_DEVICE, d));
              }
            }
          }
        }
      }
    }
  }

  private void sendMessageLowBattery (Context context, String token)
  {
    sendStatusToLikeAppPlus (context, token, R.string.charge_device, "LOW_BATTERY");
  }

  private void sendMessageNotConnected (Context context, String token)
  {
    sendStatusToLikeAppPlus (context, token, R.string.ota_connect_to_watch, "LIKEAPP_NOT_CONNECTED");
  }

  private void sendMessageNotStarted (Context context, String token)
  {
    sendStatusToLikeAppPlus (context, token, R.string.ota_start_app, "LIKEAPP_NOT_STARTED");
  }

  private void sendStatusToLikeAppPlus (Context context, String token, int id, String status)
  {
    GB.toast (context, context.getString (id), Toast.LENGTH_SHORT, GB.INFO);

    Intent intentSend = new Intent (INSTALL_STATUS);
    intentSend.setPackage ("org.likeapp.action");
    intentSend.putExtra (EXTRA_TOKEN, token);
    intentSend.putExtra (EXTRA_STATUS, status);
    context.sendBroadcast (intentSend, FwAppInstallerActivity.PERMISSIONS[0]);
  }
}
