package ma.ump.blooddonor.utils;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

// EmergencyAlertUtil.java
public class EmergencyAlertUtil {
    public static void sendEmergencyAlert(Context context, String bloodType, int requiredUnits) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("bloodType", bloodType)
                    .put("requiredUnits", requiredUnits)
                    .put("hospitalId", 1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
