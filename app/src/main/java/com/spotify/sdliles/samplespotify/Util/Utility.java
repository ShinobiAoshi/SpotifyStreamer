package com.spotify.sdliles.samplespotify.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

public class Utility {

    public static String getImageUrl(List<Image> imagesList, int requiredSize) {
        String imageUrl = null;

        Image image;
        int imageSize;

        for (int i = imagesList.size() - 1; i >= 0; i--) {
            image = imagesList.get(i);
            imageSize = Math.max(image.height, image.width);
            if (imageSize >= requiredSize) {
                imageUrl = image.url;
                break;
            }
        }

        return imageUrl;
    }

    public static boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public static void noInternetToast(Context context) {
        Toast noInternetToast = Toast.makeText(context,
                "Could not connect to internet.",
                Toast.LENGTH_SHORT);

        noInternetToast.show();
    }

    public static void noResultsToast(Context context) {
        Toast noResultsToast = Toast.makeText(context,
                "No results found.",
                Toast.LENGTH_SHORT);

        noResultsToast.show();
    }
}
