package com.spotify.sdliles.spotifystreamer.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.spotify.sdliles.spotifystreamer.Models.ParcelableArtist;
import com.spotify.sdliles.spotifystreamer.R;


public class MainActivity extends AppCompatActivity implements ArtistSearchFragment.Callback{

    private static final String TOPTRACKFRAGMENT_TAG = "TTTAG";

    private Toolbar toolbar;
    private boolean mIsTwoPaneLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        mIsTwoPaneLayout = (findViewById(R.id.top_tracks_container) != null);
        if (mIsTwoPaneLayout && savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, new TopTracksFragment())
                    .commit();
        }
    }

    @Override
    public void onItemSelected(ParcelableArtist artist) {
        if(mIsTwoPaneLayout) {
            Bundle args = new Bundle();
            args.putParcelable(ArtistSearchFragment.ARTIST_KEY, artist);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, fragment, TOPTRACKFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, TopTracksActivity.class);
            intent.putExtra(ArtistSearchFragment.ARTIST_KEY, artist);
            startActivity(intent);
        }
    }
}