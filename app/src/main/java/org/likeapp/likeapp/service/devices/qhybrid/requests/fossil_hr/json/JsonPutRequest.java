package org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.json;

import org.json.JSONObject;
import org.likeapp.likeapp.service.devices.qhybrid.adapter.fossil_hr.FossilHRWatchAdapter;
import org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.file.FilePutRawRequest;

public class JsonPutRequest extends FilePutRawRequest
{
    public JsonPutRequest(JSONObject object, FossilHRWatchAdapter adapter) {
        super((short)(0x0500 | (adapter.getJsonIndex() & 0xFF)), object.toString().getBytes(), adapter);
    }
}
