package com.spotify.sdliles.samplespotify.UI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.spotify.sdliles.samplespotify.Models.ParcelableArtist;
import com.spotify.sdliles.samplespotify.Models.ParcelableTrack;
import com.spotify.sdliles.samplespotify.R;

import java.util.ArrayList;


public class PlayerActivity extends AppCompatActivity {

    ParcelableArtist mArtist;
    int mPosition;
    ArrayList<ParcelableTrack> mTracks;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);

        if (savedInstanceState == null) {

            mArtist = getIntent().getParcelableExtra(ArtistSearchFragment.ARTIST_KEY);
            mPosition = getIntent().getIntExtra(TopTracksFragment.SELECTED_TRACK_INDEX, -1);
            mTracks = getIntent().getParcelableArrayListExtra(TopTracksFragment.TRACKS_KEY);

            Bundle arguments = new Bundle();
            arguments.putParcelable(ArtistSearchFragment.ARTIST_KEY, mArtist);
            arguments.putInt(TopTracksFragment.SELECTED_TRACK_INDEX, mPosition);
            arguments.putParcelableArrayList(TopTracksFragment.TRACKS_KEY, mTracks);

            PlayerFragment fragment = new PlayerFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.player_container, fragment)
                    .commit();
        }
    }
}