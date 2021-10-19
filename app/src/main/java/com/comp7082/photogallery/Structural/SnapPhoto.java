package com.comp7082.photogallery.Structural;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.app.Activity;

public class SnapPhoto {

    private static Context context;
    private static Activity activity;
    private static Uri photoURI;
    private static final int REQUEST_TAKE_PHOTO = 1;
    static String currentPhotoPath;
    static String timeStamp;

    public SnapPhoto(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @SuppressLint("MissingPermission")
    public static void takePhoto() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.d("fail", "no photo file");
        }
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(context,
                    "com.comp7082.photogallery",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    private static File createImageFile() throws IOException {
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "images");
        try {
            storageDir.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File image = new File(storageDir, imageFileName + ".jpg");
        Log.d("uri", image.getAbsolutePath());
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
