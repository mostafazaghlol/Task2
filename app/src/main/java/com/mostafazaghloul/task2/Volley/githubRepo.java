package com.mostafazaghloul.task2.Volley;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class githubRepo extends StringRequest {
    //private static final String Rigster_REQUEST_URL = "https://api.github.com/orgs/square/repos?per_page=5&page=5";
    private Map<String, String> params;

    public githubRepo(String request_url,
                  Response.Listener<String> listener) {
        super(Method.GET, request_url, listener, null);
        params = new HashMap<>();

    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}