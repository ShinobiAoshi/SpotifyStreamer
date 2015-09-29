package com.spotify.sdliles.samplespotify.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.spotify.sdliles.samplespotify.Models.ParcelableArtist;
import com.spotify.sdliles.samplespotify.Models.ParcelableTrack;
import com.spotify.sdliles.samplespotify.R;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ArtistSearchFragment.OnArtistSelectedListener, TopTracksFragment.OnTrackSelectedListener {

    private static final String TOPTRACKFRAGMENT_TAG = "TTTAG";
    public static final String PLAYER_KEY = "player-dialog";

    private Toolbar toolbar;
    private boolean mIsTwoPaneLayout;
    DialogFragment mPlayerDialog;

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
    public void onArtistSelected(ParcelableArtist artist) {
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

    @Override
    public void onTrackSelected(ParcelableArtist artist, List<ParcelableTrack> tracks, int position) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(ArtistSearchFragment.ARTIST_KEY, artist);
        arguments.putParcelableArrayList(TopTracksFragment.TRACKS_KEY, (ArrayList) tracks);
        arguments.putInt(TopTracksFragment.SELECTED_TRACK_INDEX, position);

        mPlayerDialog = new PlayerFragment();
        mPlayerDialog.setArguments(arguments);
        mPlayerDialog.show(getSupportFragmentManager(), PlayerFragment.class.getSimpleName());
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (mPlayerDialog != null && mPlayerDialog.isAdded()) {
            getSupportFragmentManager().putFragment(bundle, PLAYER_KEY, mPlayerDialog);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        FragmentManager fm = getSupportFragmentManager();
        mPlayerDialog = (PlayerFragment) fm.getFragment(bundle, PLAYER_KEY);
    }
}