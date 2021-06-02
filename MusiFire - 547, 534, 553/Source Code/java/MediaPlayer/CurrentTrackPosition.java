package com.example.musicplayer.MediaPlayer;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.Objects.Time;
import com.example.musicplayer.R;
import com.example.musicplayer.Utilities.MsecToMin;

public class CurrentTrackPosition extends Thread{

    private Context context;
    @SuppressWarnings("WeakerAccess")
    SeekBar progressBar;
    @SuppressWarnings("WeakerAccess")
    MediaPlayer mediaPlayer;
    @SuppressWarnings("WeakerAccess")
    TextView CurrentTrackDuration;

    Boolean trackChange = false;
    Boolean notTrackSeeking = true;

    public CurrentTrackPosition(Context context, MediaPlayer mediaPlayer) {
        this.context = context;
        this.mediaPlayer = mediaPlayer;
    }
    @Override
    public void run() {
        CurrentTrackDuration = ((Activity)context).findViewById(R.id.currentDurationView);
        progressBar = ((Activity)context).findViewById(R.id.trackProgress);
        seek();
        for(int i = 0; ; i++){
            try {
                Thread.sleep(1000);
                if(mediaPlayer.getDuration() != 0 && notTrackSeeking) {
                    int percentCompleted = mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration();
                    progressBar.setProgress(percentCompleted);
                    final MsecToMin msecToMin = new MsecToMin();
                    final Time time = msecToMin.convert(mediaPlayer.getCurrentPosition());
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CurrentTrackDuration.setText(time.Minutes+":"+time.Seconds);
                        }
                    });
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    int tempSeekTo;
    int seekTo;
    public void seek() {
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tempSeekTo = i;
                MsecToMin msecToMin = new MsecToMin();
                Time time = msecToMin.convert(tempSeekTo*mediaPlayer.getDuration()/100);
                CurrentTrackDuration.setText(time.Minutes+":"+time.Seconds);
                if(trackChange){
                    Log.e("CurrentTrackPosition", "onProgressChanged: Changed");
                    mediaPlayer.seekTo(mediaPlayer.getDuration()*seekTo/100);
                    Log.e("CurrentTrackPosition", "onProgressChanged: " + seekTo);
                    //Once manual Seek is done no more seekingmediaPlayer.getDuration()/(i*100)
                    trackChange = false;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                notTrackSeeking = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo = tempSeekTo;
                trackChange = true;
                notTrackSeeking = true;
            }
        });
    }
}