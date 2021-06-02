package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mpatric.mp3agic.Mp3File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class LyricsActivity extends AppCompatActivity {

    protected ImageView AlbumArtView;
    protected TextView SoundTrackView;
    protected TextView AlbumView;
    protected TextView ArtistView;
    protected TextView DurationView;

    private TextSwitcher LyricsView;

    private String Lyrics;

    private RequestQueue mQueue;
    private RequestQueue mQueue2;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        Log.e("Oncreate", "called");

        AlbumArtView = findViewById(R.id.AlbumArtL);

        SoundTrackView = findViewById(R.id.SoundTrack);
        AlbumView = findViewById(R.id.AlbumT);
        ArtistView = findViewById(R.id.ArtistL);
        DurationView = findViewById(R.id.DurationL);
        LyricsView = findViewById(R.id.lyrics);

        LyricsView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(LyricsActivity.this);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(16);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                return textView;
            }
        });

        SoundTrackView.setText(getIntent().getStringExtra("Title"));
        AlbumView.setText(getIntent().getStringExtra("Album"));
        ArtistView.setText(getIntent().getStringExtra("Artist"));
        DurationView.setText(getIntent().getStringExtra("Duration"));
        LinearLayout soundtrackInfoBox = findViewById(R.id.SoundTrackInfo);
        final Context context = this;
        soundtrackInfoBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("LyricsView", "soundtrackInfoBox: Clciked");
                Intent intent = new Intent(context, RetagActivity.class);
                intent.putExtra("Title", getIntent().getStringExtra("Title"));
                intent.putExtra("Album", getIntent().getStringExtra("Album"));
                intent.putExtra("Artist", getIntent().getStringExtra("Artist"));
                intent.putExtra("Duration", getIntent().getStringExtra("Duration"));
                intent.putExtra("Url", getIntent().getStringExtra("Url"));
                if (getIntent().hasExtra("Album Art")) {
                    intent.putExtra("Album Art", getIntent().getByteArrayExtra("Album Art"));
                }
                context.startActivity(intent);
            }
        });

        mQueue = Volley.newRequestQueue(this);
        mQueue2 = Volley.newRequestQueue(this);

        jsonParse(getIntent().getStringExtra("Url"), getIntent().getStringExtra("Title"), getIntent().getStringExtra("Artist"));
        if (getIntent().hasExtra("Album Art")) {
            byte[] byteArray = getIntent().getByteArrayExtra("Album Art");
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            soundtrackInfoBox.setBackgroundColor(getDominantColor(bmp));
            AlbumArtView.setImageBitmap(bmp);
        }
    }

    private void jsonParse(final String Path, String Title, String Artist) {
        if(Path != "refreshL"){
            try {
                Mp3File mp3File = new Mp3File(Path);
                String Lyric = mp3File.getId3v2Tag().getLyrics();
                if(Lyric != null){
                    try{
                        LyricsView.setText(mp3File.getId3v2Tag().getLyrics());
                    } catch (Exception e){
                        Log.e("LyricsView", "jsonParse: error", e);
                    }
                    return;
                }
            } catch (Exception e)
            {
                Log.e("LA", "jsonParse: ", e);
            }
        }

        String url = "https://vocablist.000webhostapp.com/GeniusApi.php?q=" + Artist + " " + Title;
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
                            Lyrics = "\n" + Lyrics + "\n\n\n\n\n";
                            LyricsView.setText(Lyrics);
                            if(Path != "refreshL")
                                retagTrack(Path, Lyrics);
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
    }

    private void retagTrack(String path, String Lyrics){
        final String TAG = "LA";
        Log.e(TAG, "retagTrack: Url"+ path);
        try {
            Mp3File mp3File = new Mp3File(path);
            mp3File.getId3v2Tag().setLyrics(Lyrics);
            mp3File.save("/storage/emulated/0/Music/New Song.mp3");
            File file = new File(path);
            file.delete();
            File from = new File("/storage/emulated/0/Music/New Song.mp3");
            File to = new File(path);
            from.renameTo(to);
        } catch (Exception e)
        {
            Log.e(TAG, "retagTrack: ", e);
        }
    }

    public void saveFetchedLyrics(View view){
        TextView tv = (TextView) LyricsView.getCurrentView();
        retagTrack(getIntent().getStringExtra("Url"), tv.getText().toString());
        Toast.makeText(context, "Lyrics Saved", Toast.LENGTH_SHORT).show();
    }

    public void refreshLyrics(View view) {
        Log.e("LA", "refreshLyrics: called");

        ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f).setDuration(300).start();
        jsonParse("refreshL", getIntent().getStringExtra("Title"), getIntent().getStringExtra("Artist"));
    }


    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();

        return color;
    }
}

