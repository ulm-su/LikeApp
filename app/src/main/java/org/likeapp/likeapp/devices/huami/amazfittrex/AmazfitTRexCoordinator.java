/*  Copyright (C) 2017-2020 Andreas Shimokawa, Daniele Gobbetti, João
    Paulo Barraca, José Rebelo, tiparega

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
package org.likeapp.likeapp.devices.huami.amazfittrex;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.likeapp.likeapp.R;
import org.likeapp.likeapp.devices.InstallHandler;
import org.likeapp.likeapp.devices.huami.HuamiCoordinator;
import org.likeapp.likeapp.impl.GBDevice;
import org.likeapp.likeapp.impl.GBDeviceCandidate;
import org.likeapp.likeapp.model.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmazfitTRexCoordinator extends HuamiCoordinator {
    private static final Logger LOG = LoggerFactory.getLogger(AmazfitTRexCoordinator.class);

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.AMAZFITTREX;
    }

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        try {
            BluetoothDevice device = candidate.getDevice();
            String name = device.getName();
            if (name != null && name.equalsIgnoreCase("Amazfit T-Rex")) {
                return DeviceType.AMAZFITTREX;
            }
        } catch (Exception ex) {
            LOG.error("unable to check device support", ex);
        }
        return DeviceType.UNKNOWN;
    }

    @Override
    public InstallHandler findInstallHandler(Uri uri, Context context) {
        AmazfitTRexFWInstallHandler handler = new AmazfitTRexFWInstallHandler(uri, context);
        return handler.isValid() ? handler : null;
    }

    @Override
    public int getBondingStyle() {
        return BONDING_STYLE_REQUIRE_KEY;
    }

    @Override
    public boolean supportsHeartRateMeasurement(GBDevice device) {
        return true;
    }

    @Override
    public boolean supportsActivityTracks() {
        return true;
    }

    @Override
    public boolean supportsWeather() {
        return true;
    }

    @Override
    public boolean supportsMusicInfo() {
        return true;
    }
    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return new int[]{
                R.xml.devicesettings_amazfitgtsgtr,
                R.xml.devicesettings_wearlocation,
                R.xml.devicesettings_timeformat,
                R.xml.devicesettings_liftwrist_display,
                R.xml.devicesettings_disconnectnotification,
                R.xml.devicesettings_sync_calendar,
                R.xml.devicesettings_expose_hr_thirdparty,
                R.xml.devicesettings_pairingkey
        };
    }
}