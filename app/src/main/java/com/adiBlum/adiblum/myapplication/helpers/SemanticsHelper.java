package com.adiBlum.adiblum.myapplication.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.adiBlum.adiblum.myapplication.NeuraConnection;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SemanticsHelper {

    public static final String WORK_LAT = "work_lat";
    public static final String WORK_LON = "work_lon";

    public static void saveWorkplace(final Context context, final String userToken) {
        String url = "https://wapi.theneura.com/v1/nodes_semantics/locations";
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("semantics are: " + response);
                        try {
                            saveResult(response, context);
                        } catch (JSONException e) {
                            System.out.println("semantics json error: " + e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("semantics error: " + error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                String bearer = "Bearer " + userToken;
                params.put("Authorization", bearer);
                return params;
            }
        };
        queue.add(postRequest);
    }

    private static void saveResult(String response, Context context) throws JSONException {
        JSONObject responseJson = new JSONObject(response);
        JSONArray items = responseJson.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (isWort(item)) {
                saveWorkplaceLocation(item, context);
            }
        }
    }

    private static void saveWorkplaceLocation(JSONObject item, Context context) throws JSONException {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(WORK_LAT, String.valueOf(item.getJSONObject("location").getDouble("lat")));
        editor.putString(WORK_LON, String.valueOf(item.getJSONObject("location").getDouble("lon")));
        editor.apply();
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    private static boolean isWort(JSONObject item) throws JSONException {
        return item.get("source").equals("user") &&
                item.getString("name").equals("Work") &&
                isLabelWork(item.getJSONArray("labels"));
    }

    private static boolean isLabelWork(JSONArray labels) throws JSONException {
        for (int i = 0; i < labels.length(); i++) {
            if (labels.getString(i).equals("work")) {
                return true;
            }
        }
        return false;
    }

}
