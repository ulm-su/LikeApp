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
import org.likeapp.likeapp.devices.DeviceManager;
import org.likeapp.likeapp.impl.GBDevice;
import org.likeapp.likeapp.model.ItemWithDetails;
import org.likeapp.likeapp.util.GB;

import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.likeapp.likeapp.impl.GBDevice.DEVINFO_HW_VER;
import static org.likeapp.likeapp.impl.GBDevice.DEVINFO_SN;

public class LikeAppActionInstallReceiver extends BroadcastReceiver
{
  private static final String GET_SELECTED_DEVICE = BuildConfig.APPLICATION_ID + ".GET_SELECTED_DEVICE";
  private static final String INSTALL_DEVICE_INFO = "org.likeapp.action.INSTALL_DEVICE_INFO";

  private static final String EXTRA_TOKEN = "TOKEN";
  private static final String EXTRA_NAME = "NAME";
  private static final String EXTRA_TYPE = "TYPE";
  private static final String EXTRA_SN = "SN";
  private static final String EXTRA_VERSION_HW = "VERSION_HW";

  @Override
  public void onReceive (Context context, Intent intent)
  {
    if ("com.android.vending".equals (context.getPackageManager ().getInstallerPackageName (context.getPackageName ())))
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
              if (device != null)
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
                  }
                }
                else
                {
                  GB.toast (context, context.getString (R.string.charge_device), Toast.LENGTH_SHORT, GB.INFO);
                }
              }
            }
          }
        }
      }
    }
  }
}
