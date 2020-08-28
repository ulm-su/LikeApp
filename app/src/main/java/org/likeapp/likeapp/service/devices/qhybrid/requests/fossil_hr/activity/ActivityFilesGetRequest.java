package org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.activity;

import org.likeapp.likeapp.service.devices.qhybrid.adapter.fossil_hr.FossilHRWatchAdapter;
import org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.file.FileEncryptedGetRequest;

public class ActivityFilesGetRequest extends FileEncryptedGetRequest
{
    public ActivityFilesGetRequest(FossilHRWatchAdapter adapter) {
        super((short) 0x0101, adapter);
    }

    @Override
    public void handleFileData(byte[] fileData) {
        assert Boolean.TRUE;
    }
}
