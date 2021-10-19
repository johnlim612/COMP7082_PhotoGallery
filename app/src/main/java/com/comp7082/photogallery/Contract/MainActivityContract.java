package com.comp7082.photogallery.Contract;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public interface MainActivityContract {
    interface Presenter {
        boolean photoPermissionCheck();
        void displayPhoto(String path);

        void takePhoto() throws IOException;
        void nextPhoto(String caption);
        void previousPhoto(String caption);
        void searchPhoto();
        void sharePhoto();
        void activityResult(int requestCode, int resultCode, Intent data, ImageView image);
    }
    interface Model {
        String activityResult(int requestCode, int resultCode, Intent data, ImageView image);

        void updatePhoto(String path, String caption, int selectedIndex);

        void takePhoto() throws IOException;
        void nextPhoto(String caption);
        void previousPhoto(String caption);

        void searchPhoto();
        void sharePhoto();

        boolean photoPermissionCheck();

        void setCoordinates(double wayLatitude, double wayLongitude);
        ArrayList<String> getPhotos();
        int getIndex();
    }
    interface View {
        void setGallery(int s);
        void setGalleryBitmap(Bitmap bitmap);
        void setTimeStamp(String s);
        void setCaption(String s);
    }
}
