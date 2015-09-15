package com.spotify.sdliles.spotifystreamer.Util;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Stephen on 9/14/2015.
 */
public class Utility {

    public static String getImageUrl(List<Image> imagesList, int requiredSize) {
        String imageUrl = null;

        Image image;
        int imageSize;

        // Image sizes come bigger first, small last
        for(int i = imagesList.size() - 1; i >= 0; i--) {
            image = imagesList.get(i);
            imageSize = Math.max(image.height, image.width);
            if(imageSize >= requiredSize) {
                imageUrl = image.url;
                break;
            }
        }

        return imageUrl;
    }
}
