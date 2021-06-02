package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.Cache.DatabaseHelper;
import com.example.musicplayer.MediaPlayer.CurrentTrack;
import com.example.musicplayer.MediaPlayer.CurrentTrackPosition;
import com.example.musicplayer.MediaPlayer.HomeLyricsView;
import com.example.musicplayer.Objects.Time;
import com.example.musicplayer.Utilities.MsecToMin;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<String> mSongListUrls = new ArrayList<>();
    ArrayList<Song> cSongs = new ArrayList<>();//cached songs list

    List<Song> mSongs = new ArrayList<>();
    RecyclerViewAdapter adapter;
    int CurrentTrackId;

    MediaPlayer mediaPlayer = new MediaPlayer();
    DatabaseHelper databaseHelper;
    String TAG = "Main Activity";
    ImageView Pause;
    Boolean autoplay = false;
    Boolean lockLyricsActivity = false;

    public void setAutoplay(Boolean autoplay) {
        this.autoplay = autoplay;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        ((TextView)findViewById(R.id.totalTracks)).setText(databaseHelper.getTotalTracks()+"");

        lockLyricsActivity = databaseHelper.getLockLyricsActivity().equals("true");
        if(lockLyricsActivity)
            findViewById(R.id.lockLyricsActivityIcon).setBackgroundResource(R.drawable.ic_lock_black_24dp);
        else
            findViewById(R.id.lockLyricsActivityIcon).setBackgroundResource(R.drawable.ic_lock_open_black_24dp);
        //Setting Track Position to View
        CurrentTrackPosition currentTrackPosition = new CurrentTrackPosition(this, mediaPlayer);
        currentTrackPosition.start();
        Pause = findViewById(R.id.pause);
        Pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    Pause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }
                else{
                    mediaPlayer.start();
                    Pause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                }
            }
        });
        final Context context = this;

        Load load = new Load();
        load.start();
        initRecyclerView();
    }

    class Load extends Thread {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Last Id: " + databaseHelper.isCached(), Toast.LENGTH_LONG).show();
                }
            });

            if(databaseHelper.isCached() == "-1")
            {
                freshSongs(1);
                databaseHelper.setCached("1");
                cachedSongs(1);
            }
            else if(databaseHelper.isCached().equals("1"))
            {
                Log.e(TAG, "run: 1 called");
                cachedSongs(1);
                freshSongs(0);
                databaseHelper.setCached("0");
            }
            else if(databaseHelper.isCached().equals("0")){
                Log.e(TAG, "run: 0 called");
                cachedSongs(0);
                freshSongs(1);
                databaseHelper.setCached("1");
            }
            else
                Log.e(TAG, "isCached: No test cases matched."+databaseHelper.isCached());
        }
    }

    private void freshSongs(int db) {
        databaseHelper.deleteAll(db);
        Log.e(TAG, "Directory listing started...");
        File file = Environment.getRootDirectory();
        Log.e("File", file.toString());
        File directory = new File("/storage/emulated/0/Music");
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            mSongListUrls.add(files[i].getName());
            String path = "/storage/emulated/0/Music/" + files[i].getName();
            getTrackInfo(path, db);
        }
        Log.e(TAG, "Directory listing ended...");
    }

    private void cachedSongs(int db) {
        Log.e(TAG, "cachedSongs: Caching Started...");
        Cursor data = databaseHelper.getData(db);

        while (data.moveToNext()) {
            try {
                Song song = new Song();
                song.Title = data.getString(1);
                song.Album = data.getString(2);
                song.Artist = data.getString(3);
                song.Duration = data.getString(4);
                song.AlbumArt = (data.getBlob(5)!=null)?BitmapFactory.decodeByteArray(data.getBlob(5), 0, data.getBlob(5).length):null;
                song.Url = data.getString(6);
                mSongs.add(song);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemInserted(mSongs.size());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "cachedSongs: ", e);
            }
        }
    }

    private void addData(int db, String item2, String item3, String item4, String item5, byte[] item6, String item7)
    {
        boolean insertData = databaseHelper.addData(db, item2, item3, item4, item5, item6, item7);
        if(insertData)
            Log.e(TAG, "addData: Success");
        else
            Log.e(TAG, "addData: Failed");
    }

    byte[] bytes;
    private void getTrackInfo(String path, int db) {
        Song song = new Song();
        song.Url = path;

        Mp3File mp3File;
        try {
            mp3File = new Mp3File(path);
            song.Title = (mp3File.getId3v2Tag().getTitle() != null)?mp3File.getId3v2Tag().getTitle():path.substring(path.lastIndexOf('/')+1, path.lastIndexOf(".mp3"));
            song.Artist = (mp3File.getId3v2Tag().getArtist() != null)?mp3File.getId3v2Tag().getArtist():"";
            song.Album = (mp3File.getId3v2Tag().getAlbum() != null)?mp3File.getId3v2Tag().getAlbum():"";
            MsecToMin msecToMin = new MsecToMin();
            Time time = msecToMin.convert(mp3File.getLengthInMilliseconds());
            song.Duration = time.Minutes+":"+time.Seconds;

//          Bitmap songImage = BitmapFactory.decodeByteArray(mp3File.getId3v2Tag().getAlbumImage(), 0, mp3File.getId3v2Tag().getAlbumImage().length);
//          song.AlbumArt = songImage;
            bytes = mp3File.getId3v2Tag().getAlbumImage();
//          mSongs.add(song);
            addData(db, song.Title, song.Album, song.Artist, song.Duration, bytes, song.Url);
        } catch (Exception e) {
            Log.e(TAG, "getTrackInfo: ", e);
        }
    }

    public void lockLyricsActivity(View view){
        lockLyricsActivity = !lockLyricsActivity;
        if(lockLyricsActivity)
        {
            (findViewById(R.id.lockLyricsActivityIcon)).setBackgroundResource(R.drawable.ic_lock_black_24dp);
//            boolean result = databaseHelper.setLockLyricsActivity("false");
//            Log.e(TAG, "lockLyricsActivity: "+result);
        }
        else
        {
            (findViewById(R.id.lockLyricsActivityIcon)).setBackgroundResource(R.drawable.ic_lock_open_black_24dp);
//            boolean result= databaseHelper.setLockLyricsActivity("true");
//            Log.e(TAG, "lockLyricsActivity: "+result);
        }
    }

    public Boolean getLockLyricsActivity() {
        return lockLyricsActivity;
    }

    Context context = this;
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        adapter = new RecyclerViewAdapter(this, mediaPlayer, mSongs);
        adapter.notifyItemInserted(mSongs.size());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(gridLayoutManager);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer MediaPlayer) {
                if(autoplay)//To stop AutoPlay at app startup
                {
                    Log.e(TAG, "onCompletion: called");
                    CurrentTrackId = adapter.getCurrentTrackIndex();
                    //End of Playlist
                    if(CurrentTrackId == mSongs.size()-1)
                        CurrentTrackId = -1;
                    Log.e(TAG, "onCompletion: "+CurrentTrackId);
                    MediaPlayer.reset();
                    //Play Song
                    try {
                        String path = mSongs.get(CurrentTrackId+1).Url;
                        Log.e("MediaPlayer URL", path);
                        MediaPlayer.setDataSource(path);
                        Log.e("MediaPlayer", "2");
                        MediaPlayer.prepare();
                        MediaPlayer.start();
                        ImageView playpause = findViewById(R.id.pause);
                        CurrentTrack currentTrack = new CurrentTrack(context, mSongs.get(CurrentTrackId+1).Title);
                        HomeLyricsView homeLyricsView = new HomeLyricsView(context, mSongs.get(CurrentTrackId+1).Title, mSongs.get(CurrentTrackId+1).Album, mSongs.get(CurrentTrackId+1).Url);
                        homeLyricsView.setLyrics();
                        currentTrack.setTrackName();
                        playpause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                    } catch (Exception e) {
                        Log.e("MediaPlayer", "" + e);
                        e.printStackTrace();
                    }
                    adapter.setCurrentTrackId(++CurrentTrackId);
                }
            }
        });
    }
}