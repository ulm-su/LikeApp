package org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.menu;

import org.json.JSONException;
import org.json.JSONObject;
import org.likeapp.likeapp.service.devices.qhybrid.adapter.fossil_hr.FossilHRWatchAdapter;
import org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.json.JsonPutRequest;

public class SetCommuteMenuMessage extends JsonPutRequest
{
    public SetCommuteMenuMessage(String message, boolean finished, FossilHRWatchAdapter adapter) {
        super(createObject(message, finished), adapter);
    }

    private static JSONObject createObject(String message, boolean finished) {
        try {
            return new JSONObject()
                    .put("push", new JSONObject()
                            .put("set", new JSONObject()
                                    .put("commuteApp._.config.commute_info", new JSONObject()
                                            .put("message", message)
                                            .put("type", finished ? "end" : "in_progress")
                                    )
                            )
                    );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
