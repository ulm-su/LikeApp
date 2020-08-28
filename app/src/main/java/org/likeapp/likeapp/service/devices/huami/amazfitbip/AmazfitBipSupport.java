/*  Copyright (C) 2017-2020 Andreas Shimokawa, Carsten Pfeiffer, DerFetzer,
    Matthieu Baerts, Roi Greenberg

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
package org.likeapp.likeapp.service.devices.huami.amazfitbip;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.R;
import org.likeapp.likeapp.devices.huami.HuamiConst;
import org.likeapp.likeapp.service.audio.MicReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.likeapp.likeapp.devices.huami.HuamiFWHelper;
import org.likeapp.likeapp.devices.huami.HuamiService;
import org.likeapp.likeapp.devices.huami.amazfitbip.AmazfitBipFWHelper;
import org.likeapp.likeapp.devices.huami.amazfitbip.AmazfitBipService;
import org.likeapp.likeapp.model.CallSpec;
import org.likeapp.likeapp.model.NotificationSpec;
import org.likeapp.likeapp.model.RecordedDataTypes;
import org.likeapp.likeapp.service.btle.TransactionBuilder;
import org.likeapp.likeapp.service.devices.huami.HuamiSupport;
import org.likeapp.likeapp.service.devices.huami.operations.FetchActivityOperation;
import org.likeapp.likeapp.service.devices.huami.operations.FetchSportsSummaryOperation;
import org.likeapp.likeapp.service.devices.huami.operations.HuamiFetchDebugLogsOperation;
import org.likeapp.likeapp.service.devices.miband.NotificationStrategy;

public class AmazfitBipSupport extends HuamiSupport {

    private static final Logger LOG = LoggerFactory.getLogger(AmazfitBipSupport.class);

    public AmazfitBipSupport() {
        super(LOG);
    }

    @Override
    public NotificationStrategy getNotificationStrategy() {
        return new AmazfitBipTextNotificationStrategy(this);
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        super.sendNotificationNew(notificationSpec, false);
    }

    @Override
    public void onFindDevice(boolean start) {
        CallSpec callSpec = new CallSpec();
        callSpec.command = start ? CallSpec.CALL_INCOMING : CallSpec.CALL_END;
        callSpec.name = MicReader.isRunning () ? getContext ().getString (R.string.baby_monitor, (int) MicReader.getValue ()) : "LikeApp";
        onSetCallState(callSpec);
    }

    @Override
    protected AmazfitBipSupport setDisplayItems(TransactionBuilder builder) {
        if (gbDevice.getFirmwareVersion() == null) {
            LOG.warn("Device not initialized yet, won't set menu items");
            return this;
        }

        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress());
        String[] items = getContext().getResources().getStringArray(R.array.pref_bip_display_items_default);
        Set<String> pages = prefs.getStringSet(HuamiConst.PREF_DISPLAY_ITEMS, new HashSet<>(Arrays.asList(items)));

        LOG.info("Setting display items to " + (pages == null ? "none" : pages));
        byte[] command = AmazfitBipService.COMMAND_CHANGE_SCREENS.clone();

        String sort = prefs.getString (HuamiConst.PREF_DISPLAY_ITEMS_SORT, "");
        assert sort != null;

        for (int i = 0; i < items.length; i++)
        {
            int index = sort.indexOf (items[i]);
            if (index > 0)
            {
                command[i + 4] = (byte) (sort.charAt (index - 1) - '0');
            }
        }

        boolean shortcut_weather = false;
        boolean shortcut_alipay = false;

        if (pages != null) {
            if (pages.contains("status")) {
                command[1] |= 0x02;
            }
            if (pages.contains("activity")) {
                command[1] |= 0x04;
            }
            if (pages.contains("weather")) {
                command[1] |= 0x08;
            }
            if (pages.contains("alarm")) {
                command[1] |= 0x10;
            }
            if (pages.contains("timer")) {
                command[1] |= 0x20;
            }
            if (pages.contains("compass")) {
                command[1] |= 0x40;
            }
            if (pages.contains("settings")) {
                command[1] |= 0x80;
            }
            if (pages.contains("alipay")) {
                command[2] |= 0x01;
            }
            if (pages.contains("shortcut_weather")) {
                shortcut_weather = true;
            }
            if (pages.contains("shortcut_alipay")) {
                shortcut_alipay = true;
            }
            builder.write(getCharacteristic(HuamiService.UUID_CHARACTERISTIC_3_CONFIGURATION), command);
            setShortcuts(builder, shortcut_weather, shortcut_alipay);
        }

        return this;
    }

    private void setShortcuts(TransactionBuilder builder, boolean weather, boolean alipay) {
        LOG.info("Setting shortcuts: weather=" + weather + " alipay=" + alipay);

        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress());
        boolean exchange = weather && alipay && prefs.getBoolean (HuamiConst.PREF_DISPLAY_SHORTCUTS_EXCHANGE, false);

        byte codeWeather = (byte) (exchange ? 1 : 2);
        byte codeAlipay = (byte) (exchange ? 2 : 1);

        // Basically a hack to put weather first always, if alipay is the only enabled one
        // there are actually two alipays set but the second one disabled.... :P
        byte[] command = new byte[]{0x10,
                (byte) ((alipay || weather) ? 0x80 : 0x00), weather ? codeWeather : codeAlipay,
                (byte) ((alipay && weather) ? 0x81 : 0x01), codeAlipay,
        };

        builder.write(getCharacteristic(HuamiService.UUID_CHARACTERISTIC_3_CONFIGURATION), command);
    }

    @Override
    public void onFetchRecordedData(int dataTypes) {
        try {
            // FIXME: currently only one data type supported, these are meant to be flags
            if (dataTypes == RecordedDataTypes.TYPE_ACTIVITY) {
                new FetchActivityOperation(this).perform();
            } else if (dataTypes == RecordedDataTypes.TYPE_GPS_TRACKS) {
                new FetchSportsSummaryOperation(this).perform();
            } else if (dataTypes == RecordedDataTypes.TYPE_DEBUGLOGS) {
                new HuamiFetchDebugLogsOperation(this).perform();
            } else {
                LOG.warn("fetching multiple data types at once is not supported yet");
            }
        } catch (IOException ex) {
            LOG.error("Unable to fetch recorded data types" + dataTypes, ex);
        }
    }

    @Override
    public void phase2Initialize(TransactionBuilder builder) {
        super.phase2Initialize(builder);
        LOG.info("phase2Initialize...");
        setLanguage(builder);
        requestGPSVersion(builder);
    }

    @Override
    public HuamiFWHelper createFWHelper(Uri uri, Context context) throws IOException {
        return new AmazfitBipFWHelper(uri, context);
    }
}
