package com.spotify.sdliles.spotifystreamer.UI;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.spotify.sdliles.spotifystreamer.Adapters.TrackAdapter;
import com.spotify.sdliles.spotifystreamer.Models.ParcelableArtist;
import com.spotify.sdliles.spotifystreamer.Models.ParcelableTrack;
import com.spotify.sdliles.spotifystreamer.R;
import com.spotify.sdliles.spotifystreamer.Util.Spotify;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by Stephen on 8/10/2015.
 */
public class TopTracksFragment extends Fragment {

    static final String TOP_TRACKS = "TopTracks";

    private View rootView;
    private ParcelableArtist mArtist;
    private TrackAdapter mTrackAdapter;
    private List<ParcelableTrack> mParcelableTracks;
    private SpotifyService mSpotify;
    private ListView trackList;
    private ProgressDialog progressDialog;
    private ActionBar toolbar;

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();

        if (arguments != null) {
            mArtist = arguments.getParcelable(TOP_TRACKS);
        }

        rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        if(savedInstanceState != null && savedInstanceState.containsKey("tracks")) {
            mParcelableTracks = savedInstanceState.getParcelableArrayList("tracks");
        } else {
            mParcelableTracks = new ArrayList<>();
        }
        if (mArtist != null) {
            new FetchTracksTask().execute();
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        trackList = (ListView) rootView.findViewById(R.id.top_tracks_list_view);
        mTrackAdapter = new TrackAdapter(getActivity(), R.layout.list_top_tracks_item, mParcelableTracks);
        trackList.setAdapter(mTrackAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("tracks", (ArrayList<? extends Parcelable>) mParcelableTracks);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchTracksTask extends AsyncTask<Void, Void, Tracks> {
        @Override
        protected Tracks doInBackground(Void... params) {
            mSpotify = Spotify.getInstance();
            return mSpotify.getArtistTopTrack(mArtist.getId(), "US");
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            if (tracks != null) {

                mParcelableTracks = new ArrayList<>();
                for (Track track : tracks.tracks) {
                    ParcelableTrack parcelabletrack = new ParcelableTrack(track);
                    mParcelableTracks.add(parcelabletrack);
                }

                mTrackAdapter.clear();
                mTrackAdapter.addAll(mParcelableTracks);

                progressDialog.dismiss();
            }
        }
    }
}
