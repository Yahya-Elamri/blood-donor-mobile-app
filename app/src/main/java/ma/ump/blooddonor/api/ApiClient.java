package ma.ump.blooddonor.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ma.ump.blooddonor.entity.Donor;

public class ApiClient {
    private static ApiClient instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private static final String BASE_URL = "http://10.0.2.2:8080/api"; // Adjust for device/emulator

    private ApiClient(Context context) {
        ctx = context.getApplicationContext();
        requestQueue = Volley.newRequestQueue(ctx);
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public void addToRequestQueue(Request<?> req) {
        getRequestQueue().add(req);
    }

    public void login(String email, String password,
                      Response.Listener<JSONObject> listener,
                      Response.ErrorListener errorListener) {
        String url = BASE_URL + "/auth/login";

        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, new JSONObject(params),
                listener, errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        addToRequestQueue(request);
    }

    // Register donor method
    public void registerDonor(Donor donor,
                              Response.Listener<JSONObject> listener,
                              Response.ErrorListener errorListener) {
        String url = BASE_URL + "/auth/signup/donor";

        JSONObject donorJson = new JSONObject();
        try {
            donorJson.put("email", donor.getEmail());
            donorJson.put("password", donor.getPassword());
            donorJson.put("nom", donor.getNom());
            donorJson.put("prenom", donor.getPrenom());
            donorJson.put("groupeSanguin", donor.getGroupeSanguin());
            donorJson.put("localisation", donor.getLocalisation());
        } catch (JSONException e) {
            e.printStackTrace();
            if (errorListener != null) {
                errorListener.onErrorResponse(null);
            }
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, donorJson,
                listener, errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        addToRequestQueue(request);
    }

    // Logout method
    public void logout(Response.Listener<JSONObject> listener,
                       Response.ErrorListener errorListener) {
        String url = BASE_URL + "/auth/logout";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, null,
                listener, errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        addToRequestQueue(request);
    }
}
