package com.example.musicplayer.MediaPlayer;

import android.media.MediaPlayer;

public class SeekTrack {
    MediaPlayer mediaPlayer;
    public void seek() {
        mediaPlayer.seekTo(0);
    }
}