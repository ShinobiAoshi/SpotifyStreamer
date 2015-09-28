package com.spotify.sdliles.spotifystreamer.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdliles.spotifystreamer.Models.ParcelableTrack;
import com.spotify.sdliles.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Stephen on 9/14/2015.
 */
public class TrackAdapter extends ArrayAdapter<ParcelableTrack> {

    Context context;
    int layoutResId;
    List<ParcelableTrack> trackData = null;

    public TrackAdapter(Context context, int layoutResId, List<ParcelableTrack> trackData) {
        super(context, layoutResId, trackData);
        this.layoutResId = layoutResId;
        this.context = context;
        this.trackData = trackData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TrackHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResId, parent, false);

            holder = new TrackHolder();
            holder.albumArt = (ImageView) convertView.findViewById(R.id.album_art);
            holder.albumName = (TextView) convertView.findViewById(R.id.album_name);
            holder.trackName = (TextView) convertView.findViewById(R.id.track_name);

            convertView.setTag(holder);
        } else {
            holder = (TrackHolder) convertView.getTag();
        }

        ParcelableTrack track = trackData.get(position);
        holder.trackName.setText(track.getTrackName());
        holder.albumName.setText(track.getAlbumName());
        Picasso.with(this.context).load(track.getAlbumArtSmallUrl()).into(holder.albumArt);

        return convertView;
    }

    public void setArtistData(List<ParcelableTrack> tracks) {
        trackData = tracks;
    }

    static class TrackHolder {
        ImageView albumArt;
        TextView trackName;
        TextView albumName;
    }
}
