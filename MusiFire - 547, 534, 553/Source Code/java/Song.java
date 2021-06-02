package com.example.musicplayer;

import android.graphics.Bitmap;

public class Song {
    String Title;
    String Artist;
    String Album;
    String Duration;
    Bitmap AlbumArt;
    String AlbumArtUrl;
    String Url;

    public Song() {
        Title = "";
        Artist = "";
        Album = "";
        Duration = "";
        AlbumArt = null;
        AlbumArtUrl = "";
        Url = "";
    }

    public Song(String title, String artist, String album, String duration,Bitmap albumArt, String url, String albumArtUrl) {
        Title = title;
        Artist = artist;
        Album = album;
        Duration = duration;
        AlbumArt = albumArt;
        AlbumArtUrl = albumArtUrl;
        Url = url;
    }
}
