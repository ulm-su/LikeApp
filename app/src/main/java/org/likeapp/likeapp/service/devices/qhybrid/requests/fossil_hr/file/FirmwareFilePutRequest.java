package org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.file;

import android.content.Context;

import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.R;
import org.likeapp.likeapp.service.btle.TransactionBuilder;
import org.likeapp.likeapp.service.btle.actions.SetProgressAction;
import org.likeapp.likeapp.service.devices.qhybrid.adapter.fossil_hr.FossilHRWatchAdapter;
import org.likeapp.likeapp.util.GB;

public class FirmwareFilePutRequest extends FilePutRawRequest {
    public FirmwareFilePutRequest(byte[] firmwareBytes, FossilHRWatchAdapter adapter) {
        super((short) 0x00FF, firmwareBytes, adapter);
    }

    @Override
    public void onPacketWritten(TransactionBuilder transactionBuilder, int packetNr, int packetCount) {
        int progressPercent = (int) ((((float) packetNr) / packetCount) * 100);
        transactionBuilder.add(new SetProgressAction (GBApplication.getContext().getString(R.string.updatefirmwareoperation_update_in_progress), true, progressPercent, GBApplication.getContext()));
    }

    @Override
    public void onFilePut(boolean success) {
        Context context = GBApplication.getContext();
        if (success) {
            GB.updateInstallNotification(context.getString(R.string.updatefirmwareoperation_update_complete), false, 100, context);
        } else {
            GB.updateInstallNotification(context.getString(R.string.updatefirmwareoperation_write_failed), false, 0, context);
        }
    }
}
