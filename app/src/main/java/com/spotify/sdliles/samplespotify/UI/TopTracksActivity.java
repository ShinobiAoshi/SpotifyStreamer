package com.spotify.sdliles.samplespotify.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.spotify.sdliles.samplespotify.Models.ParcelableArtist;
import com.spotify.sdliles.samplespotify.Models.ParcelableTrack;
import com.spotify.sdliles.samplespotify.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stephen on 9/17/2015.
 */
public class TopTracksActivity extends AppCompatActivity implements TopTracksFragment.OnTrackSelectedListener {

    ParcelableArtist mArtist;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);

        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            mArtist = getIntent().getParcelableExtra(ArtistSearchFragment.ARTIST_KEY);
            arguments.putParcelable(ArtistSearchFragment.ARTIST_KEY, mArtist);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_tracks_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onTrackSelected(ParcelableArtist artist, List<ParcelableTrack> tracks, int position) {
        Intent showTopTracksIntent = new Intent(this, PlayerActivity.class);
        showTopTracksIntent.putExtra(ArtistSearchFragment.ARTIST_KEY, artist);
        showTopTracksIntent.putExtra(TopTracksFragment.SELECTED_TRACK_INDEX, position);
        showTopTracksIntent.putExtra(TopTracksFragment.TRACKS_KEY, (ArrayList) tracks);

        startActivity(showTopTracksIntent);
    }
}
