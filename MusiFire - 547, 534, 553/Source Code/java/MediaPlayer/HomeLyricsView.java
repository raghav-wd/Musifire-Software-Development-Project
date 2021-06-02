package com.example.musicplayer.MediaPlayer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.musicplayer.R;
import com.mpatric.mp3agic.Mp3File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeLyricsView {
    Context context;
    String TITLE;
    String ARTIST;
    String URL;
    RequestQueue Queue;

    String TAG = "HomeLyricsView";

    public HomeLyricsView(Context context, String TITLE, String ARTIST, String URL) {
        this.context = context;
        this.TITLE = TITLE;
        this.ARTIST = ARTIST;
        this.URL = URL;
    }

    public void setLyrics(){
        final TextView LyricsViewHome = ((Activity)context).findViewById(R.id.lyricsViewHome);
        try {
            Mp3File mp3File = new Mp3File(URL);
            String Lyric = mp3File.getId3v2Tag().getLyrics();
            if(Lyric != null){
                try{
                    LyricsViewHome.setText(mp3File.getId3v2Tag().getLyrics());
                } catch (Exception e){
                    Log.e("LyricsView", "jsonParse: error", e);
                }
                return;
            }
        } catch (Exception e)
        {
            Log.e("LA", "jsonParse: ", e);
        }
        LyricsViewHome.setText("\nFetching Lyrics♪♫♪\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        Queue = Volley.newRequestQueue(context);
        String url = "https://vocablist.000webhostapp.com/GeniusApi.php?q="+ARTIST+" "+TITLE;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("onResponse", "started");
                        Log.e("response", response.toString());
                        try {
                            String Lyrics;
                            JSONArray jsonArray = response.getJSONArray("lyricss");

                            JSONObject employee = jsonArray.getJSONObject(0);

                            Log.e("Lyrics", employee.getString("lyrics"));
                            Lyrics = employee.getString("lyrics");
                            Lyrics = Lyrics.replaceAll("&amp;", "&");
                            Lyrics = Lyrics.replaceAll("<br>", "\n");
                            Lyrics = "\n"+Lyrics+"\n\n\n\n\n";
                            LyricsViewHome.setText(Lyrics);
                            Log.e(TAG, "onResponse: Lyrics Fetched Successfully");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onResponse: Lyrics Couldn't be fetched", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        Queue.add(request);
    }
}
