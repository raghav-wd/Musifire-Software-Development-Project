package com.example.musicplayer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.MediaPlayer.CurrentTrack;
import com.example.musicplayer.MediaPlayer.HomeLyricsView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    String TAG = "RecyclerViewAdapter";

    private List<Song> mSongs = new ArrayList<>();
    private MediaPlayer mMediaPlayer;
    public int mCurrentTrackId;

    private String Lyrics;
    Context mContext;


    public RecyclerViewAdapter(Context mContext, MediaPlayer mMediaPlayer, List<Song> mSongs) {
        this.mSongs = mSongs;
        this.mContext = mContext;
        this.mMediaPlayer = mMediaPlayer;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_tile_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, final int position){
            final Song song = mSongs.get(position);

            holder.Title1.setText(song.Title);
            holder.Album1.setText(song.Album);
            holder.Artist1.setText(song.Artist);
            holder.Duration1.setText(song.Duration);
            if(song.AlbumArt != null) {
                holder.AlbumArt1.setImageBitmap(song.AlbumArt);
                Log.e("onBindViewH", "onClick: AlbumArt 1");
            }
            else
            {
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.noalbumart);
                holder.AlbumArt1.setImageBitmap(bitmap);
                Log.e("onBindViewH", "onClick: AlbumArt 2");
            }
            Random rnd = new Random();
            int color = Color.argb(150, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            holder.InfoBox.setBackgroundColor(color);

            holder.AlbumArt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)mContext).setAutoplay(true);//Stops autoplay glitch
                mCurrentTrackId = position;
                Log.e(TAG, "onBindViewHolder: CurrentTrackIndex = "+ mCurrentTrackId);
                HomeLyricsView homeLyricsView = new HomeLyricsView(mContext, mSongs.get(position).Title, mSongs.get(position).Artist, mSongs.get(position).Url);
                homeLyricsView.setLyrics();
                Intent intent = new Intent(mContext, LyricsActivity.class);
                intent.putExtra("Title", mSongs.get(position).Title);
                intent.putExtra("Album", mSongs.get(position).Album);
                intent.putExtra("Artist", mSongs.get(position).Artist);
                intent.putExtra("Duration", mSongs.get(position).Duration);
                intent.putExtra("Url", mSongs.get(position).Url);
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    mSongs.get(position).AlbumArt.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    byte[] albumArtByte = stream.toByteArray();
                    intent.putExtra("Album Art", albumArtByte);
                } catch (Exception e) {
                    Log.e("ByteArrayOutput", "Error Occured");
                }
                if (true) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }

                //Play Song
                try {
                    String path = mSongs.get(position).Url;
                    Log.e("MediaPlayer URL", path);
                    mMediaPlayer.setDataSource(path);
                    Log.e("MediaPlayer", "2");
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    CurrentTrack currentTrack = new CurrentTrack(mContext, mSongs.get(position).Title);
                    currentTrack.setTrackName();
                    ImageView playpause = ((Activity) mContext).findViewById(R.id.pause);
                    playpause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                } catch (Exception e) {
                    Log.e("MediaPlayer", "" + e);
                    e.printStackTrace();
                }
                if(!((MainActivity)mContext).getLockLyricsActivity())
                mContext.startActivity(intent);
            }
            });
    }

    public int getCurrentTrackIndex() {
        return mCurrentTrackId;
    }

    public void setCurrentTrackId(int mCurrentTrackId) {
        this.mCurrentTrackId = mCurrentTrackId;
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView Title1;
        TextView Album1;
        TextView Artist1;
        TextView Duration1;
        ImageView AlbumArt1;
        RelativeLayout InfoBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Title1 = itemView.findViewById(R.id.Title1);
            Album1 = itemView.findViewById(R.id.Album1);
            Artist1 = itemView.findViewById(R.id.Artist1);
            Duration1 = itemView.findViewById(R.id.Duration1);
            AlbumArt1 = itemView.findViewById(R.id.albumArt1);
            InfoBox = itemView.findViewById(R.id.infoBox);
        }
    }
}
