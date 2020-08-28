/*  Copyright (C) 2017-2020 Andreas Shimokawa, Carsten Pfeiffer

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
package org.likeapp.likeapp.service.devices.huami.amazfitgtr;

import android.content.Context;
import android.net.Uri;

import org.likeapp.likeapp.devices.huami.HuamiFWHelper;
import org.likeapp.likeapp.devices.huami.amazfitgtr.AmazfitGTRFWHelper;
import org.likeapp.likeapp.model.NotificationSpec;
import org.likeapp.likeapp.service.btle.TransactionBuilder;
import org.likeapp.likeapp.service.devices.huami.amazfitbip.AmazfitBipSupport;
import org.likeapp.likeapp.service.devices.huami.operations.UpdateFirmwareOperationNew;

import java.io.IOException;

public class AmazfitGTRSupport extends AmazfitBipSupport {

    @Override
    public byte getCryptFlags() {
        return (byte) 0x80;
    }
    
    @Override
    protected byte getAuthFlags() {
        return 0x00;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        super.sendNotificationNew(notificationSpec, true);
    }

    @Override
    public HuamiFWHelper createFWHelper(Uri uri, Context context) throws IOException {
        return new AmazfitGTRFWHelper (uri, context);
    }

}
