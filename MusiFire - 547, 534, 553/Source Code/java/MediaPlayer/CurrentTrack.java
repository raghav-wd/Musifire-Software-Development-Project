package com.example.musicplayer.MediaPlayer;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.example.musicplayer.R;

public class CurrentTrack {
    public Context context;
    private String TRACK_NAME;

    public CurrentTrack(Context context, String TRACK_NAME) {
        this.context = context;
        this.TRACK_NAME = TRACK_NAME;
    }

    public void setTrackName() {
        TextView CurrentTrack = ((Activity) context).findViewById(R.id.trackPlaying);
        CurrentTrack.setText(TRACK_NAME);
    }
}

