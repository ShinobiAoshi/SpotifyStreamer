package com.spotify.sdliles.samplespotify.UI;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.spotify.sdliles.samplespotify.Adapters.ArtistAdapter;
import com.spotify.sdliles.samplespotify.Models.ParcelableArtist;
import com.spotify.sdliles.samplespotify.R;
import com.spotify.sdliles.samplespotify.Util.Spotify;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class ArtistSearchFragment extends Fragment {

    private View mRootView;
    private ActionBar mToolbar;
    private ArtistAdapter mArtistAdapter;
    private ProgressDialog mProgressDialog;
    private List<ParcelableArtist> mParcelableArtists;
    private SpotifyService mSpotify;
    private ListView mArtistList;
    private Activity mParentActivity;

    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_ARTIST_INDEX = "selected_artist";
    public static final String ARTIST_KEY = "artist";
    public static final String ARTIST_LIST_KEY = "artists";


    public interface OnArtistSelectedListener {
        void onArtistSelected(ParcelableArtist artist);
    }

    public ArtistSearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_artist_search, container, false);
        mParentActivity = getActivity();
        mParcelableArtists = new ArrayList<>();

        if(savedInstanceState != null) {
            loadSavedInstanceState(savedInstanceState);
        }

        bindValues();

        setHasOptionsMenu(true);
        return mRootView;
    }

    private void loadSavedInstanceState(Bundle savedInstanceState) {
        mParcelableArtists = savedInstanceState.getParcelableArrayList(ARTIST_LIST_KEY);
        mPosition = savedInstanceState.getInt(SELECTED_ARTIST_INDEX);
    }

    private void bindValues() {
        mArtistList = (ListView) mRootView.findViewById(R.id.artist_search_list_view);
        mArtistAdapter = new ArtistAdapter(mParentActivity, R.layout.list_artist_search_result, mParcelableArtists);
        ViewGroup parentGroup = (ViewGroup) mArtistList.getParent();
        View emptyView = mParentActivity.getLayoutInflater().inflate(R.layout.empty_list, parentGroup, false);
        parentGroup.addView(emptyView);
        mArtistList.setEmptyView(emptyView);
        mArtistList.setAdapter(mArtistAdapter);

        mArtistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParcelableArtist artist = (ParcelableArtist) mArtistList.getItemAtPosition(position);
                if (artist != null) {
                    ((OnArtistSelectedListener) mParentActivity)
                            .onArtistSelected(artist);
                }
                mPosition = position;
            }
        });

        if (mPosition != ListView.INVALID_POSITION) {
            mArtistList.setSelection(mPosition);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_artist_search_fragment, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                new FetchArtistsTask().execute(query);
                searchView.setQuery("", false);
                searchView.setIconified(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_ARTIST_INDEX, mPosition);
        }
        if(mParcelableArtists != null && !mParcelableArtists.isEmpty()) {
            outState.putParcelableArrayList(ARTIST_LIST_KEY, (ArrayList<? extends Parcelable>) mParcelableArtists);
        }
        super.onSaveInstanceState(outState);
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, ArtistsPager> {
        @Override
        protected ArtistsPager doInBackground(String... params) {
            mSpotify = Spotify.getInstance();
            return mSpotify.searchArtists(params[0]);
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            if (artistsPager != null) {

                mParcelableArtists = new ArrayList<>();
                for (Artist artist : artistsPager.artists.items) {
                    ParcelableArtist parcelableArtist = new ParcelableArtist(artist);
                    mParcelableArtists.add(parcelableArtist);
                }

                mArtistAdapter.clear();
                mArtistAdapter.addAll(mParcelableArtists);

                if(mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        }
    }
}
