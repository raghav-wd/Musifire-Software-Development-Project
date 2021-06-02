package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mpatric.mp3agic.Mp3File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;

public class RetagActivity extends AppCompatActivity {
    RequestQueue mQueue;
    Context context = this;
    byte[] AlbumCoverByte;
    private static final int FILE_SELECT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_tag);

        mQueue = Volley.newRequestQueue(this);

        final EditText pathET;
        final EditText titleET;
        final EditText albumET;
        final EditText artistET;
        final EditText yearET;
        final EditText lyricsET;

        //Setting Data to SoundTrackBox
        TextView textView = findViewById(R.id.SoundTrack);
        textView.setText(getIntent().getStringExtra("Title"));
        textView = findViewById(R.id.AlbumT);
        textView.setText(getIntent().getStringExtra("Album"));
        textView = findViewById(R.id.ArtistL);
        textView.setText(getIntent().getStringExtra("Artist"));
        textView = findViewById(R.id.DurationL);
        textView.setText(getIntent().getStringExtra("Duration"));

        if(getIntent().hasExtra("Album Art")){
            byte[] byteImage = getIntent().getByteArrayExtra("Album Art");
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
            ((ImageView)findViewById(R.id.AlbumArtL)).setImageBitmap(bitmap);
        }

        //Editing fields
        pathET = findViewById(R.id.File_RA_Edit);
        titleET = findViewById(R.id.Title_RA_Edit);
        albumET = findViewById(R.id.Album_RA_Edit);
        artistET = findViewById(R.id.Artist_RA_Edit);
        yearET = findViewById(R.id.Year_RA_Edit);
        lyricsET = findViewById(R.id.Lyrics_RA_Edit);

        pathET.setText(getIntent().getStringExtra("Url"));
        titleET.setText(getIntent().getStringExtra("Title"));
        albumET.setText(getIntent().getStringExtra("Album"));
        artistET.setText(getIntent().getStringExtra("Artist"));
        try{
            Mp3File mp3File = new Mp3File(getIntent().getStringExtra("Url"));
            yearET.setText(mp3File.getId3v2Tag().getYear());
            lyricsET.setText(mp3File.getId3v2Tag().getLyrics());

        } catch (Exception e) {
            Log.e("RA", "onCreate: ", e);
        }

        //Listeners
        ((Button)findViewById(R.id.fetchInfo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Song song = getTrackInfo(titleET.getText().toString(), artistET.getText().toString());
            }
        });
        ((Button)findViewById(R.id.retagbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retagTrack(getIntent().getStringExtra("Url"), pathET.getText().toString(), titleET.getText().toString(), albumET.getText().toString(), artistET.getText().toString(), yearET.getText().toString(), lyricsET.getText().toString(), AlbumCoverByte);
            }
        });
    }

    private Song getTrackInfo(String Title, String Artist) {
        final Song song = new Song();
        String url = "https://vocablist.000webhostapp.com/GeniusTrackInfoApi.php?q="+Artist+"-"+Title;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("onResponseGEtTrack", "started");
                        Log.e("response GetTrack", response.toString());
                        try {
                            JSONArray jsonArray = response.getJSONArray("lyricss");

                            JSONObject employee = jsonArray.getJSONObject(0);

                            song.Album = employee.getString("album");
                            song.AlbumArtUrl = employee.getString("albumArt");

                            TextView textView = findViewById(R.id.Album_RA_Edit);
                            textView.setText(song.Album);

                            ImageView imageView = findViewById(R.id.albumArt_RT);
                            Glide.with(context).load(song.AlbumArtUrl).into(imageView);
                            //Setting AlbumCoverByte from Fetched Album Cover (Glide)->(byte[])
                            Glide.with(context)
                                    .asBitmap()
                                    .load(song.AlbumArtUrl)
                                    .into(new CustomTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            resource.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                            AlbumCoverByte = stream.toByteArray();
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {
                                        }
                                    });
                        } catch (JSONException e) {
                            Toast.makeText(context, "No Resource found", Toast.LENGTH_SHORT).show();
                            Log.e("getTrackInfo", "error occured"+e);
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
        Log.e("getTrackInfo", song.toString());
        return song;
    }

    private void retagTrack(String pathOld, String pathNew, String Title, String Album, String Artist, String Year, String Lyrics, byte[] AlbumCoverByte){
        final String TAG = "retagTrack()";
        Log.e(TAG, "retagTrack: Url"+ pathOld);
        try {
            Mp3File mp3File = new Mp3File(pathOld);
            mp3File.getId3v2Tag().setTitle(Title);
            mp3File.getId3v2Tag().setAlbum(Album);
            mp3File.getId3v2Tag().setArtist(Artist);
            mp3File.getId3v2Tag().setLyrics(Lyrics);
            mp3File.getId3v2Tag().setAlbumImage(AlbumCoverByte, "image/jpeg");

            mp3File.save("/storage/emulated/0/Music/New Song.mp3");
            File file = new File(pathOld);
            file.delete();

            File from = new File("/storage/emulated/0/Music/New Song.mp3");
            File to = new File(pathNew);
            from.renameTo(to);
            Toast.makeText(context, "Changes may appear on Restart of App", Toast.LENGTH_SHORT).show();
        } catch (Exception e)
        {
            Log.e(TAG, "retagTrack: ", e);
            Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show();
        }
    }

    public void showFileChooser(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 10);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case 10:
                if (resultCode == RESULT_OK){
                    String path = data.getData().getPath();
                    path = "/storage/emulated/0/"+path.substring(18);
                    Toast.makeText(context, path, Toast.LENGTH_SHORT).show();
                    Log.e("RA", path);
                    File file = new File(path);

                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    ((ImageView)findViewById(R.id.albumArt_RT)).setImageBitmap(bitmap);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    byte[] albumArtByte = stream.toByteArray();
                    AlbumCoverByte = albumArtByte;
                }
                break;
        }
    }
}
