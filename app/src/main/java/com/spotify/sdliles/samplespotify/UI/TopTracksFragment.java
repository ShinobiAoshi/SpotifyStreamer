package com.spotify.sdliles.samplespotify.UI;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.spotify.sdliles.samplespotify.Adapters.TrackAdapter;
import com.spotify.sdliles.samplespotify.Models.ParcelableArtist;
import com.spotify.sdliles.samplespotify.Models.ParcelableTrack;
import com.spotify.sdliles.samplespotify.R;
import com.spotify.sdliles.samplespotify.Util.Spotify;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by Stephen on 8/10/2015.
 */
public class TopTracksFragment extends Fragment {



    private View rootView;
    private ParcelableArtist mArtist;
    private TrackAdapter mTrackAdapter;
    private List<ParcelableTrack> mParcelableTracks;
    private SpotifyService mSpotify;
    private ListView trackList;
    private ProgressDialog progressDialog;
    private ActionBar toolbar;

    private int mPosition = ListView.INVALID_POSITION;
    public static final String SELECTED_TRACK_INDEX = "track_index";
    public static final String TRACKS_KEY = "tracks";

    public TopTracksFragment() {
    }

    public interface OnTrackSelectedListener {
        void onTrackSelected(ParcelableArtist artist, List<ParcelableTrack> tracks, int position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        mParcelableTracks = new ArrayList<>();

        if (savedInstanceState == null) {
            loadArguments();
        } else {
            loadSavedInstanceState(savedInstanceState);
        }

        trackList = (ListView) rootView.findViewById(R.id.top_tracks_list_view);
        mTrackAdapter = new TrackAdapter(getActivity(), R.layout.list_top_tracks_item, mParcelableTracks);
        trackList.setAdapter(mTrackAdapter);

        trackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParcelableTrack track = (ParcelableTrack) trackList.getItemAtPosition(position);
                if (track != null) {
                    ((OnTrackSelectedListener) getActivity()).onTrackSelected(mArtist, (ArrayList) mParcelableTracks, position);
                }
                mPosition = position;
            }
        });

        if (mPosition != ListView.INVALID_POSITION) {
            trackList.setSelection(mPosition);
        }

        // TODO: Execute after onArtistSelectedListener??
        if (mArtist != null && savedInstanceState == null) {
            new FetchTracksTask().execute();
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void loadArguments() {
        Bundle arguments = getArguments();
        mArtist = arguments.getParcelable(ArtistSearchFragment.ARTIST_KEY);
    }

    private void loadSavedInstanceState(Bundle savedInstanceState) {
        mArtist = savedInstanceState.getParcelable(ArtistSearchFragment.ARTIST_KEY);
        mParcelableTracks = savedInstanceState.getParcelableArrayList(TRACKS_KEY);
        mPosition = savedInstanceState.getInt(SELECTED_TRACK_INDEX);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_TRACK_INDEX, mPosition);
        }
        if(mParcelableTracks != null && !mParcelableTracks.isEmpty()) {
            outState.putParcelableArrayList(TRACKS_KEY, (ArrayList) mParcelableTracks);
        }
        if(mArtist != null) {
            outState.putParcelable(ArtistSearchFragment.ARTIST_KEY, mArtist);
        }
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

                toolbar.setTitle(mArtist.getName());
                toolbar.setSubtitle(R.string.top_tracks);

                progressDialog.dismiss();
            }
        }
    }
}
