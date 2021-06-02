package com.example.musicplayer.Utilities;

import com.example.musicplayer.Objects.Time;

public class MsecToMin {
    public MsecToMin() {
    }

    public Time convert(long milliseconds) {
        Time time = new Time();
        int minutes = (int)(milliseconds / 1000)  / 60;
        time.Minutes = ""+minutes;
        int seconds = (int)((milliseconds / 1000) % 60);
        time.Seconds = seconds+"";
        if(seconds<10)
            time.Seconds = "0"+time.Seconds;
        return time;
    }
}
