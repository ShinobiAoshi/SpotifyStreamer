package com.spotify.sdliles.spotifystreamer.Util;

/**
 * Created by Stephen on 8/4/2015.
 */

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class Spotify {

    private static SpotifyService service;

    public static SpotifyService getInstance() {
        if (service != null) return service;

        SpotifyApi api = new SpotifyApi();
        service = api.getService();
        return service;
    }

    private Spotify() {

    }
}
