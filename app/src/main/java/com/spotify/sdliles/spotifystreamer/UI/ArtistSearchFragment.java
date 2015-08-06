package com.spotify.sdliles.spotifystreamer.UI;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
    ActionBar toolbar;
    ArtistAdapter artistAdapter;
    ProgressDialog progressDialog;
    List<ParcelableArtist> parcelableArtists = new ArrayList<ParcelableArtist>();
    SpotifyService spotify;

    public ArtistSearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);
        artistAdapter = new ArtistAdapter(getActivity(), R.layout.list_artist_search_result, parcelableArtists);
        ListView artistList = (ListView) rootView.findViewById(R.id.artist_search_list_view);
        artistList.setAdapter(artistAdapter);
        final EditText artistSearchEditText = (EditText) rootView.findViewById(
                R.id.artist_search_edit_text);
        artistSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    new FetchArtistsTask().execute(artistSearchEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_artist_search_fragment, menu);
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
