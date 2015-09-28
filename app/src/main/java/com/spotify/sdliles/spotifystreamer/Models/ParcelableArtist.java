package com.spotify.sdliles.spotifystreamer.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.spotify.sdliles.spotifystreamer.Util.Utility;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Stephen on 8/4/2015.
 */

public class ParcelableArtist implements Parcelable {

    private String id;
    private String name;
    private String url;

    public ParcelableArtist(Artist artist) {
        this.id = artist.id;
        this.name = artist.name;

        if (artist.images.size() > 0) {
            this.url = Utility.getImageUrl(artist.images, 200);
        } else
            this.url = "https://d1qb2nb5cznatu.cloudfront.net/startups/i/113563-0d042ff7bd710ce95c303aa3f93c8522-medium_jpg.jpg?buster=1431803239";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "id: " + getId() + ", name: " + getName() + ", url : " + getUrl();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.url);
    }

    private ParcelableArtist(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.url = in.readString();
    }

    public static final Parcelable.Creator<ParcelableArtist> CREATOR = new Parcelable.Creator<ParcelableArtist>() {
        public ParcelableArtist createFromParcel(Parcel in) {
            return new ParcelableArtist(in);
        }

        public ParcelableArtist[] newArray(int size) {
            return new ParcelableArtist[size];
        }
    };
}
