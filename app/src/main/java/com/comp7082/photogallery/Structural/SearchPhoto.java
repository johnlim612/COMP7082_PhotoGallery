package com.comp7082.photogallery.Structural;

import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.comp7082.photogallery.GeoDegrees;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SearchPhoto {

    private static Double impossibleCoordinates;

    public static ArrayList<String> findPhotos(Date startTimestamp, Date endTimestamp, Double latitude, Double longitude, String keywords) {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.comp7082.photogallery/files/Pictures");
        ArrayList<String> newPhotos = new ArrayList<>();
        File[] fList = file.listFiles();

        if (fList != null) {
            ArrayList<File> fileList = new ArrayList<>();
            fileList.addAll(Arrays.asList(fList));
            int i = 0;
            fileList.forEach((f) -> {
                Log.d("file", f.toString());
                if (((latitude.equals(impossibleCoordinates)
                        && longitude.equals(impossibleCoordinates))
                        || (withinApproxLoc(latitude, getGeo(f.getAbsolutePath())[0])
                        && withinApproxLoc(longitude, getGeo(f.getAbsolutePath())[1])))
                ) {
                    if (((startTimestamp == null && endTimestamp == null) || (f.lastModified() >= startTimestamp.getTime()
                            && f.lastModified() <= endTimestamp.getTime())) //&&
                            && (keywords.equals("") || f.getPath().contains(keywords)))
                        newPhotos.add(f.getPath());
                }
            });
        }
        Log.d("New Photos: ", newPhotos.toString());
        return newPhotos;
    }

    public static Double[] getGeo(String absolutePath) {
        try {
            ExifInterface exif = new ExifInterface(absolutePath);
            GeoDegrees geoDegrees = new GeoDegrees();
            geoDegrees.geoDegrees(null);

            Double lat = geoDegrees.getLatitude();
            Double lng = geoDegrees.getLongitude();
            Log.d("Exif", "Lat: "+ lat + " Long: " + lng);
            return new Double[]{lat, lng};
        } catch (IOException e) {
            Log.e("PictureActivity", e.getLocalizedMessage());
        }
        return null;
    }

    public static boolean withinApproxLoc(Double searchDegrees, Double geoDegrees) {
        if (geoDegrees != null) {
            double difference = searchDegrees - geoDegrees;
            Log.d("Search", "Search: " + searchDegrees + " Retrieved: " + geoDegrees);
            Log.d("Search", "Difference in search from retrieved value: " + difference);
            Log.d("Return Bool", String.valueOf(!(Math.abs(difference) > 1)));
            return !(Math.abs(difference) > 1);
        }
        return false;
    }
}
