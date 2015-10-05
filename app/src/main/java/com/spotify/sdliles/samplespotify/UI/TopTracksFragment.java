package com.spotify.sdliles.samplespotify.UI;

import android.app.Activity;
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

public class TopTracksFragment extends Fragment {



    private View mRootView;
    private ParcelableArtist mArtist;
    private TrackAdapter mTrackAdapter;
    private List<ParcelableTrack> mParcelableTracks;
    private SpotifyService mSpotify;
    private ListView mTrackList;
    private ProgressDialog mProgressDialog;
    private ActionBar mToolbar;
    private Activity mParentActivity;

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
        mRootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        mParcelableTracks = new ArrayList<>();

        if (savedInstanceState == null) {
            loadArguments();
        } else {
            loadSavedInstanceState(savedInstanceState);
        }

        bindValues();

        if (mArtist != null && savedInstanceState == null) {
            new FetchTracksTask().execute();
        }

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void loadArguments() {
        Bundle arguments = getArguments();
        mArtist = arguments.getParcelable(ArtistSearchFragment.ARTIST_KEY);
    }

    private void bindValues() {
        mParentActivity = getActivity();
        mTrackList = (ListView) mRootView.findViewById(R.id.top_tracks_list_view);
        mTrackAdapter = new TrackAdapter(mParentActivity, R.layout.list_top_tracks_item, mParcelableTracks);
        mTrackList.setAdapter(mTrackAdapter);

        mTrackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParcelableTrack track = (ParcelableTrack) mTrackList.getItemAtPosition(position);
                if (track != null) {
                    ((OnTrackSelectedListener) mParentActivity).onTrackSelected(mArtist, (ArrayList) mParcelableTracks, position);
                }
                mPosition = position;
            }
        });

        if (mPosition != ListView.INVALID_POSITION) {
            mTrackList.setSelection(mPosition);
        }
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
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
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

                mToolbar.setTitle(mArtist.getName());
                mToolbar.setSubtitle(R.string.top_tracks);

                mProgressDialog.dismiss();
            }
        }
    }
}
