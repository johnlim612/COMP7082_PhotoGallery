package com.comp7082.photogallery.Model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.comp7082.photogallery.Contract.MainActivityContract;
import com.comp7082.photogallery.GeoDegrees;
import com.comp7082.photogallery.Presenter.MainActivityPresenter;
import com.comp7082.photogallery.R;
import com.comp7082.photogallery.SearchActivity;
import com.comp7082.photogallery.View.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivityModel extends AppCompatActivity implements MainActivityContract.Model {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 3;

    private final Context context;
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;

    private String mCurrentPhotoPath;
    private ArrayList<String> photos;

    private double wayLatitude, wayLongitude = 0.0;
    private final int locationRequestCode = 1000;
    private final Double impossibleCoordinates= 5000.0;
    private int index = 0;

    public MainActivityModel(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        photos = findPhotos(new Date(Long.MIN_VALUE), new Date(), impossibleCoordinates, impossibleCoordinates, "");
    }

    @Override
    public boolean photoPermissionCheck() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
            return true;
        } else {
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                    }
                });
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void takePhoto() throws IOException {
        //check for location permission
        if (!photoPermissionCheck()) {
            // already permission granted
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                File photoFile = createImageFile();
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    // get location here
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            setCoordinates(location.getLatitude(), location.getLongitude());
                        }
                        else {
                            Log.d("Location Error", "Location failed to obtain");
                        }
                    });

                    Uri photoURI = FileProvider.getUriForFile(this, "com.comp7082.photogallery.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "_caption_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public boolean withinApproxLoc(Double searchDegrees, Double geoDegrees) {
        if (geoDegrees != null) {
            double difference = searchDegrees - geoDegrees;
            Log.d("Search", "Search: " + searchDegrees + " Retrieved: " + geoDegrees);
            Log.d("Search", "Difference in search from retrieved value: " + difference);
            Log.d("Return Bool", String.valueOf(!(Math.abs(difference) > 1)));
            return !(Math.abs(difference) > 1);
        }
        return false;
    }
    public ArrayList<String> findPhotos(Date startTimestamp, Date endTimestamp, Double latitude, Double longitude, String keywords) {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.comp7082.photogallery/files/Pictures");
        ArrayList<String> newPhotos = new ArrayList<>();
        File[] fList = file.listFiles();
        if (fList != null) {
            for (File f : fList) {
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
            }
        }
        return newPhotos;
    }

    public void nextPhoto(String caption) {
        updatePhoto(photos.get(index), caption, index);
        if (index < (photos.size() - 1)) {
            index++;
        }
    }

    public void previousPhoto(String caption) {
        updatePhoto(photos.get(index), caption, index);
        if (index > 0) {
            index--;
        }
    }

    public void updatePhoto(String path, String caption, int selectedIndex) {
        String[] attr = path.split("_");
        if (attr.length >= 3) {
            String newName = attr[0] + "_" + caption + "_" + attr[2] + "_" + attr[3] + "_" + ".jpg";
            File to = new File(newName);
            File from = new File(path);
            if (from.renameTo(to)) {
                photos.set(selectedIndex, newName);
            }
        }
    }

    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }
    public void setCoordinates(double wayLatitude, double wayLongitude) {
        this.wayLatitude = wayLatitude;
        this.wayLongitude = wayLongitude;
        Log.d("Location", "Latitude: " + wayLatitude + ", Longitude: " + wayLongitude);
    }
    public ArrayList<String> getPhotos() {
        return photos;
    }
    public int getIndex() {
        return index;
    }

    public void geoTag(){
        ExifInterface exif;
        try {
            exif = new ExifInterface(mCurrentPhotoPath);
            Log.d("Checking", "It goes through getTag()");

            int num1Lat = (int)Math.floor(wayLatitude);
            int num2Lat = (int)Math.floor((wayLatitude - num1Lat) * 60);
            double num3Lat = (wayLatitude - ((double)num1Lat+((double)num2Lat/60))) * 3600000;

            int num1Lon = (int)Math.floor(wayLongitude);
            int num2Lon = (int)Math.floor((wayLongitude - num1Lon) * 60);
            double num3Lon = (wayLongitude - ((double)num1Lon+((double)num2Lon/60))) * 3600000;

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat+"/1,"+num2Lat+"/1,"+num3Lat+"/1000");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon+"/1,"+num2Lon+"/1,"+num3Lon+"/1000");


            if (wayLatitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            }

            if (wayLongitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            }

            exif.saveAttributes();

        } catch (IOException e) {
            Log.e("PictureActivity", e.getLocalizedMessage());
        }
    }
    public Double[] getGeo(String absolutePath) {
        try {
            ExifInterface exif = new ExifInterface(absolutePath);
            GeoDegrees geoDegrees = new GeoDegrees();
            geoDegrees.geoDegrees(exif);

            Double lat = geoDegrees.getLatitude();
            Double lng = geoDegrees.getLongitude();
            Log.d("Exif", "Lat: "+ lat + " Long: " + lng);
            return new Double[]{lat, lng};
        } catch (IOException e) {
            Log.e("PictureActivity", e.getLocalizedMessage());
        }
        return null;
    }

    public void sharePhoto() {
        Bitmap currentPhoto = BitmapFactory.decodeFile(photos.get(index));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        currentPhoto.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getApplicationContext().getContentResolver(), currentPhoto, "photo", null);
        Uri currentImage = Uri.parse(path);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, currentImage);
        shareIntent.setType("image/jpg");
        activity.startActivity(Intent.createChooser(shareIntent, "Image"));
    }

    public void searchPhoto() {
        Intent intent = new Intent(context, SearchActivity.class);
        activity.startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public String activityResult(int requestCode, int resultCode, Intent data, ImageView image) {
        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DateFormat format = new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
                Date startTimestamp , endTimestamp;
                Double latitude, longitude;
                try {
                    String from = (String) data.getStringExtra("STARTTIMESTAMP");
                    String to = (String) data.getStringExtra("ENDTIMESTAMP");
                    startTimestamp = format.parse(from);
                    endTimestamp = format.parse(to);
                } catch (Exception ex) {
                    startTimestamp = null;
                    endTimestamp = null;
                }
                try {
                    latitude = Double.valueOf(data.getStringExtra("Latitude"));
                    longitude = Double.valueOf(data.getStringExtra("Longitude"));
                } catch (Exception ex) {
                    latitude = impossibleCoordinates;
                    longitude = impossibleCoordinates;
                }

                String keywords = (String) data.getStringExtra("KEYWORDS");
                index = 0;
                photos = findPhotos(startTimestamp, endTimestamp, latitude, longitude, keywords);
                if (photos.size() == 0) {
                    return null;
                } else {
                    return photos.get(index);
                }
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ImageView mImageView = image;
            mImageView.setImageBitmap(BitmapFactory.decodeFile(getCurrentPhotoPath()));
            geoTag();
            photos = findPhotos(new Date(Long.MIN_VALUE), new Date(), impossibleCoordinates, impossibleCoordinates,  "");
            return photos.get(index);
        }
        return null;
    }
}
