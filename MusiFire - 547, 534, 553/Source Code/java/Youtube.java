package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

public class Youtube extends AppCompatActivity {

    SimpleExoPlayerView exoPlayerView;
    SimpleExoPlayer exoPlayer;
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        exoPlayerView = (SimpleExoPlayerView)findViewById(R.id.exoplayer);
//        mQueue = Volley.newRequestQueue(this);

        setExoplayer("https://cdn32.xxxsextube.tv/remote_control.php?time=1579783460&cv=8053e91a8efad80bae9fee30658f7710&lr=273000&cv2=acf0d1103d497b48655bacaaee255446&file=%2Fvideos%2F14000%2F14137%2F14137.mp4&cv3=c7a643aef7575b9c4c296c3a0bd5d5a1&cv4=4ab0b3d2e5820280b78ca463a0f08bfd");
    }

    private void setExoplayer(String videoUrl) {
        exoPlayerView = (SimpleExoPlayerView)findViewById(R.id.exoplayer);
//        exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//        exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        try {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            Uri videoUri = Uri.parse(videoUrl);
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ExtractorMediaSource(videoUri, dataSourceFactory, extractorsFactory, null,
                    null);
            exoPlayerView.setPlayer(exoPlayer);
            exoPlayer.prepare(mediaSource);

//            setView(""+exoPlayer.isLoading());
            exoPlayer.setPlayWhenReady(true);
        } catch (Exception e) {
            Log.e("VideoActivity", "Exoplayer Error" + e.toString());
        }
    }
}
