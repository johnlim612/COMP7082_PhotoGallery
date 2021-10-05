package com.comp7082.photogallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 3;
    String mCurrentPhotoPath;
    private ArrayList<String> photos = null;
    private int index = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private final int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private final Double impossibleCoordinates= 5000.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_main);
        photos = findPhotos(new Date(Long.MIN_VALUE), new Date(), 0.0, 0.0, "");
        if (photos.size() == 0) {
            displayPhoto(null);
        } else {
            displayPhoto(photos.get(index));
        }
    }

    public void takePhoto(View v) {
        //check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        } else {
            // already permission granted
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    //create image file
                    photoFile = createImageFile();

                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    // get location here
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            wayLatitude = location.getLatitude();
                            wayLongitude = location.getLongitude();
                            Log.d("Location", "Latitude: " + wayLatitude + ", Longitude: " + wayLongitude);
                        }
                        else {
                            Log.d("Location Error", "Location failed to obtain");
                        }
                    });

                    Uri photoURI = FileProvider.getUriForFile(this, "com.comp7082.photogallery.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    public void geoTag(){
        ExifInterface exif;
        try {
            exif = new ExifInterface(mCurrentPhotoPath);
            /*
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, Double.toString(wayLatitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, Double.toString(wayLongitude));
             */
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

    public Float[] getGeo(String absolutePath) {
        try {
            ExifInterface exif = new ExifInterface(absolutePath);
            GeoDegrees geoDegrees = new GeoDegrees();
            geoDegrees.geoDegrees(exif);

            Float lat = geoDegrees.getLatitude();
            Float lng = geoDegrees.getLongitude();
            return new Float[]{lat, lng};
        } catch (IOException e) {
            Log.e("PictureActivity", e.getLocalizedMessage());
        }
        return null;
    }

    public void sharePhoto(View v) {
        Bitmap currentPhoto = BitmapFactory.decodeFile(photos.get(index));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        currentPhoto.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), currentPhoto, "photo", null);
        Uri currentImage = Uri.parse(path);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, currentImage);
        shareIntent.setType("image/jpg");
        startActivity(Intent.createChooser(shareIntent, "Image"));
    }

    private boolean withinApproxLoc(Double searchDegrees, Float geoDegrees) {
        double difference = searchDegrees - geoDegrees;
        Log.d("Search", "Difference in search from retrieved value: " + difference);
        return !(Math.abs(difference * 100) > 1);
    }

    private ArrayList<String> findPhotos(Date startTimestamp, Date endTimestamp, Double latitude, Double longitude, String keywords) {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.comp7082.photogallery/files/Pictures");
        ArrayList<String> newPhotos = new ArrayList<>();
        File[] fList = file.listFiles();
        if (fList != null) {
            for (File f : fList) {
                if (((startTimestamp == null && endTimestamp == null) || (f.lastModified() >= startTimestamp.getTime()
                        && f.lastModified() <= endTimestamp.getTime())) &&
                        (((latitude.equals(impossibleCoordinates)
                        && longitude.equals(impossibleCoordinates))
                        || (withinApproxLoc(latitude, getGeo(f.getAbsolutePath())[0])
                        && withinApproxLoc(longitude, getGeo(f.getAbsolutePath())[1])))
                ) && (keywords.equals("") || f.getPath().contains(keywords)))
                    Log.d("File Values", Double.toString(getGeo(f.getAbsolutePath())[0]));
                    newPhotos.add(f.getPath());
            }
        }
        return newPhotos;
    }

    public void nextPhoto(View v) {
        if (photos.size() > 0) {
            updatePhoto(photos.get(index), ((EditText) findViewById(R.id.etCaption)).getText().toString(), index);
            if (index < (photos.size() - 1)) {
                index++;
            }
            displayPhoto(photos.get(index));

        }
    }

    public void previousPhoto(View v) {
        if (photos.size() > 0) {
            updatePhoto(photos.get(index), ((EditText) findViewById(R.id.etCaption)).getText().toString(), index);
            if (index > 0) {
                index--;
            }
            displayPhoto(photos.get(index));
        }
    }

    public void searchPhoto(View v) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
    }

    private void displayPhoto(String path) {
        ImageView iv = (ImageView) findViewById(R.id.ivGallery);
        TextView tv = (TextView) findViewById(R.id.tvTimestamp);
        EditText et = (EditText) findViewById(R.id.etCaption);
        if (path == null || path.equals("")) {
            iv.setImageResource(R.mipmap.ic_launcher);
            et.setText("");
            tv.setText("");
        } else {
            iv.setImageBitmap(BitmapFactory.decodeFile(path));
            String[] attr = path.split("_");
            et.setText(attr[1]);
            tv.setText(attr[2]);

            //testing if location is properly obtained
            /*Log.d("Location", path);
            Float[] location = getGeo(path);
            Log.d("ObtainedLocation", location[0] + ", " + location[1]);*/
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "_caption_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
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
                        //Log.d("Location", "Latitude: " + wayLatitude + ", Longitude: " + wayLongitude);
                    }
                });
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePhoto(String path, String caption, int selectedIndex) {
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                    displayPhoto(null);
                } else {
                    displayPhoto(photos.get(index));
                }
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ImageView mImageView = (ImageView) findViewById(R.id.ivGallery);
            mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
            geoTag();
            photos = findPhotos(new Date(Long.MIN_VALUE), new Date(), impossibleCoordinates, impossibleCoordinates,  "");
            displayPhoto(photos.get(index));
        }
    }
}