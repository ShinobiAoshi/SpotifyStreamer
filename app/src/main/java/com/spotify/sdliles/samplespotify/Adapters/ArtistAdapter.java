package com.spotify.sdliles.samplespotify.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdliles.samplespotify.Models.ParcelableArtist;
import com.spotify.sdliles.samplespotify.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Stephen on 7/27/2015.
 */
public class ArtistAdapter extends ArrayAdapter<ParcelableArtist> {

    Context context;
    int layoutResId;
    List<ParcelableArtist> artistData = null;

    public ArtistAdapter(Context context, int layoutResId, List<ParcelableArtist> artistData) {
        super(context, layoutResId, artistData);
        this.layoutResId = layoutResId;
        this.context = context;
        this.artistData = artistData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ArtistHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResId, parent, false);

            holder = new ArtistHolder();
            holder.artistImage = (ImageView) convertView.findViewById(R.id.artist_image);
            holder.artistName = (TextView) convertView.findViewById(R.id.artist_name);

            convertView.setTag(holder);
        } else {
            holder = (ArtistHolder) convertView.getTag();
        }

        ParcelableArtist artist = artistData.get(position);
        holder.artistName.setText(artist.getName());
        Picasso.with(this.context)
                .load(artist.getUrl())
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .into(holder.artistImage);

        return convertView;
    }

    public void setArtistData(List<ParcelableArtist> artists) {
        artistData = artists;
    }

    static class ArtistHolder {
        ImageView artistImage;
        TextView artistName;
    }
}