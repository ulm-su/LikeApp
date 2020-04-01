/*  Copyright (C) 2016-2020 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti

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
package org.likeapp.likeapp.devices.huami.amazfitgtr;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.likeapp.likeapp.devices.huami.HuamiFWHelper;
import org.likeapp.likeapp.service.devices.huami.amazfitgtr.AmazfitGTRFirmwareInfo;

import java.io.IOException;

public class AmazfitGTRFWHelper extends HuamiFWHelper
{

    public AmazfitGTRFWHelper(Uri uri, Context context) throws IOException {
        super(uri, context);
    }

    @Override
    protected void determineFirmwareInfo(byte[] wholeFirmwareBytes) {
        firmwareInfo = new AmazfitGTRFirmwareInfo (wholeFirmwareBytes);
        if (!firmwareInfo.isHeaderValid()) {
            throw new IllegalArgumentException("Not a an Amazifit GTR firmware");
        }
    }
}
