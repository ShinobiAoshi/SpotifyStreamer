package com.spotify.sdliles.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchFragment extends Fragment
{

    public ArtistSearchFragment ()
    {
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);
        EditText artistSearchEditText = (EditText) rootView.findViewById(
                R.id.artist_search_edit_text);
        artistSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction (TextView v, int actionId, KeyEvent event)
            {
                if(actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    ArtistAdapter artistAdapter = mockData();
                    ListView artistList = (ListView) rootView.findViewById(R.id.artist_search_list_view);
                    artistList.setAdapter(artistAdapter);
                    return true;
                }
                return false;
            }
        });
        return rootView;
    }

    private ArtistAdapter mockData()
    {
        Artist[] artists = new Artist[6];
        artists[0] = new Artist("Linkin Park", "http://i.imgur.com/UbQK3Ww.jpg");
        artists[1] = new Artist("Linkin Park 2", "http://i.imgur.com/UbQK3Ww.jpg");
        artists[2] = new Artist("Linkin Park 3", "http://i.imgur.com/UbQK3Ww.jpg");
        artists[3] = new Artist("Linkin Park 4", "http://i.imgur.com/UbQK3Ww.jpg");
        artists[4] = new Artist("Linkin Park 5", "http://i.imgur.com/UbQK3Ww.jpg");
        artists[5] = new Artist("Linkin Park 6", "http://i.imgur.com/UbQK3Ww.jpg");

        return new ArtistAdapter(getActivity(), R.layout.list_artist_search_result, artists);
    }
}
