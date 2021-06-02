package com.example.musicplayer;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LyricsApi {
    private Context context;
    private String Lyrics;
    private String Artist;
    private String Title;

    private RequestQueue mQueue;

    public LyricsApi(RequestQueue queue, String artist, String title) {
        mQueue = queue;
        Artist = artist;
        Title = title;
    }

    protected String jsonParse() {
        Log.e("jsonParse", "started");
        String url = "https://vocablist.000webhostapp.com/GeniusApi.php?q="+Artist+" "+Title;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("onResponse", "started");
                        Log.e("response", response.toString());
                        try {
                            JSONArray jsonArray = response.getJSONArray("lyricss");

                            JSONObject employee = jsonArray.getJSONObject(0);

                            Log.e("Lyrics", employee.getString("lyrics"));
                            Lyrics = employee.getString("lyrics");
                            Lyrics = Lyrics.replaceAll("&amp;", "&");
                            Lyrics = Lyrics.replaceAll("<br>", "\n");
                            Lyrics = "\n"+Lyrics+"\n\n\n\n\n";

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
        return Lyrics;
    }
}