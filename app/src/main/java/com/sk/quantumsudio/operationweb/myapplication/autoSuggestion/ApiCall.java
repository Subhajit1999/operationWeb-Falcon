package com.sk.quantumsudio.operationweb.myapplication.autoSuggestion;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class ApiCall {
    private static ApiCall mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private ApiCall(Context ctx) {
        mCtx = ctx;
        mRequestQueue = getRequestQueue();
    }

    private static synchronized ApiCall getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ApiCall(context);
        }
        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static void make(Context ctx, String query, Response.Listener<String>
            listener, Response.ErrorListener errorListener){
        String url = "https://itunes.apple.com/search?term=" +query
                + "&country=US";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                listener, errorListener);
        ApiCall.getInstance(ctx).addToRequestQueue(stringRequest);
    }
}
