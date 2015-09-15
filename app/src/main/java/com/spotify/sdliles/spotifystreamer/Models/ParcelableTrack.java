package com.spotify.sdliles.spotifystreamer.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.spotify.sdliles.spotifystreamer.Util.Utility;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Stephen on 9/14/2015.
 */
public class ParcelableTrack implements Parcelable {

    private String trackName;
    private String albumName;
    private String albumArtLargeUrl;
    private String albumArtSmallUrl;
    private String previewUrl;

    public ParcelableTrack(Track track) {
        trackName = track.name;
        albumName = track.album.name;
        previewUrl = track.preview_url;

        if (track.album.images.size() > 0) {
            albumArtSmallUrl = Utility.getImageUrl(track.album.images, 200);
            albumArtLargeUrl = Utility.getImageUrl(track.album.images, 640);
        } else {
            albumArtSmallUrl = "https://d1qb2nb5cznatu.cloudfront.net/startups/i/113563-0d042ff7bd710ce95c303aa3f93c8522-medium_jpg.jpg?buster=1431803239";
            albumArtSmallUrl = "https://d1qb2nb5cznatu.cloudfront.net/startups/i/113563-0d042ff7bd710ce95c303aa3f93c8522-medium_jpg.jpg?buster=1431803239";
        }
    }

    public String getTrackName() { return trackName; }

    public String getAlbumName() { return albumName; }

    public String getAlbumArtLargeUrl() {
        return albumArtLargeUrl;
    }

    public String getAlbumArtSmallUrl() { return albumArtSmallUrl; }

    public String getPreviewUrl() { return previewUrl; }

    @Override
    public String toString() {
        return "Track Name: " + getTrackName() + ", Album Name: " + getAlbumName() + ", Album Art Large: "
                + getAlbumArtLargeUrl() + ", Album Art Small: " + getAlbumArtSmallUrl() + ", Preview URL: " + getPreviewUrl();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trackName);
        dest.writeString(albumName);
        dest.writeString(albumArtSmallUrl);
        dest.writeString(albumArtLargeUrl);
        dest.writeString(previewUrl);
    }

    private ParcelableTrack(Parcel in) {
        trackName = in.readString();
        albumName = in.readString();
        albumArtSmallUrl = in.readString();
        albumArtLargeUrl = in.readString();
        previewUrl = in.readString();
    }

    public static final Parcelable.Creator<ParcelableTrack> CREATOR = new Parcelable.Creator<ParcelableTrack>() {
        public ParcelableTrack createFromParcel(Parcel in) {
            return new ParcelableTrack(in);
        }

        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };
}
