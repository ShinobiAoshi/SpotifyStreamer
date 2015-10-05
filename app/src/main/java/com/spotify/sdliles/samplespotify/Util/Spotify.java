package com.spotify.sdliles.samplespotify.Util;

/**
 * Created by Stephen on 8/4/2015.
 */

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class Spotify {

    private static SpotifyService service;

    private Spotify() {

    }

    public static SpotifyService getInstance() {
        if (service != null) return service;

        SpotifyApi api = new SpotifyApi();
        service = api.getService();
        return service;
    }
}
