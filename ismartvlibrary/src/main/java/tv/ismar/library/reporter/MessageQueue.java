package tv.ismar.library.reporter;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by LongHai on 17-4-12.
 */

public class MessageQueue {

    private static final String TAG = "LH/MessageQueue";

    private static ArrayList<String> messageQueueList;

    static {
        messageQueueList = new ArrayList<String>();
    }

    private static synchronized void addQueue(String log) {
        Log.i(TAG, "Add : " + log);
        messageQueueList.add(log);
    }

    public static synchronized void removeAll() {
        messageQueueList.clear();
    }

    public static synchronized ArrayList<String> getQueueList() {
        return messageQueueList;
    }


    public static class DataCollectionTask extends
            AsyncTask<Object, Void, Void> {

        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Object... params) {
            if (params != null && params.length > 0) {
                String eventName = (String) params[0];
                HashMap<String, Object> properties = null;
                if (params.length > 1 && params[1] != null) {
                    properties = (HashMap<String, Object>) params[1];
                }
                try {
                    JSONObject propertiesJson = new JSONObject();
                    propertiesJson.put("time", System.currentTimeMillis() / 1000);
                    if (properties != null) {
                        Set<String> set = properties.keySet();
                        for (String key : set) {
                            propertiesJson.put(key, properties.get(key));
                        }
                    }
                    JSONObject logJson = new JSONObject();
                    logJson.put("event", eventName);
                    logJson.put("properties", propertiesJson);
                    MessageQueue.addQueue(logJson.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
