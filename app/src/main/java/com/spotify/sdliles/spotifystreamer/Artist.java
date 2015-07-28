package com.spotify.sdliles.spotifystreamer;

/**
 * Created by Stephen on 7/27/2015.
 */
public class Artist
{
    private String name;
    private String imageUrl;

    public Artist(String name, String imageUrl)
    {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName()
    {
        return name;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
