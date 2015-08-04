package com.spotify.sdliles.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Stephen on 7/27/2015.
 */
public class ArtistAdapter extends ArrayAdapter<Artist> {

    Context context;
    int layoutResId;
    Artist[] artistData = null;

    public ArtistAdapter(Context context, int layoutResId, Artist[] artistData) {
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

        Artist artist = artistData[position];
        holder.artistName.setText(artist.name);
        //Picasso.with(this.context).load(artist.images.get(0).url).into(holder.artistImage);


        return convertView;
    }

    static class ArtistHolder {
        ImageView artistImage;
        TextView artistName;
    }
}