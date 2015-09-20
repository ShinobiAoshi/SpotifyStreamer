package com.spotify.sdliles.spotifystreamer.UI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.spotify.sdliles.spotifystreamer.Models.ParcelableArtist;
import com.spotify.sdliles.spotifystreamer.R;

/**
 * Created by Stephen on 9/17/2015.
 */
public class TopTracksActivity extends AppCompatActivity {

    ParcelableArtist artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            artist = getIntent().getParcelableExtra(ArtistSearchFragment.ARTIST_KEY);
            arguments.putParcelable(ArtistSearchFragment.ARTIST_KEY, artist);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_tracks_container, fragment)
                    .commit();
        }
    }
}
