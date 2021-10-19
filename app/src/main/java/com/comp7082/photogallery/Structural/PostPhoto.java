package com.comp7082.photogallery.Structural;

import android.content.Intent;
import android.app.Activity;
import android.net.Uri;

public class PostPhoto {

    private static Activity activity;

    public PostPhoto(Activity activity){
        this.activity = activity;
    }

    public static void sharePhoto(String imageUri) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");

        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUri));
        activity.startActivity(Intent.createChooser(share, "Share Image"));
    }
}
