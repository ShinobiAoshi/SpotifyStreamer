package com.spotify.sdliles.spotifystreamer.UI;

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
import android.widget.ListView;

import com.spotify.sdliles.spotifystreamer.Adapters.ArtistAdapter;
import com.spotify.sdliles.spotifystreamer.Models.ParcelableArtist;
import com.spotify.sdliles.spotifystreamer.R;
import com.spotify.sdliles.spotifystreamer.Util.Spotify;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchFragment extends Fragment {
    View rootView;
    ActionBar toolbar;
    ArtistAdapter artistAdapter;
    ProgressDialog progressDialog;
    List<ParcelableArtist> parcelableArtists;
    SpotifyService spotify;
    ListView artistList;

    public ArtistSearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);
        if(savedInstanceState != null && savedInstanceState.containsKey("artists")) {
            parcelableArtists = savedInstanceState.getParcelableArrayList("artists");
        } else {
            parcelableArtists = new ArrayList<>();
        }
        setHasOptionsMenu(true);
        return rootView;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        artistList = (ListView) rootView.findViewById(R.id.artist_search_list_view);
        artistAdapter = new ArtistAdapter(getActivity(), R.layout.list_artist_search_result, parcelableArtists);
        ViewGroup parentGroup = (ViewGroup) artistList.getParent();
        View emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_artist_search, parentGroup, false);
        parentGroup.addView(emptyView);
        artistList.setEmptyView(emptyView);
        artistList.setAdapter(artistAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("artists", (ArrayList<? extends Parcelable>) parcelableArtists);
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, ArtistsPager> {
        @Override
        protected ArtistsPager doInBackground(String... params) {
            spotify = Spotify.getInstance();
            return spotify.searchArtists(params[0]);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Please Wait...");
            progressDialog.setMessage("Searching...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            if (artistsPager != null) {

                parcelableArtists = new ArrayList<>();
                for (Artist artist : artistsPager.artists.items) {
                    ParcelableArtist parcelableArtist = new ParcelableArtist(artist);
                    parcelableArtists.add(parcelableArtist);
                }

                artistAdapter.clear();
                artistAdapter.addAll(parcelableArtists);

                progressDialog.dismiss();
            }
        }
    }
}
