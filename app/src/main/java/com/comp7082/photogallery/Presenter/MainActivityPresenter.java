package com.comp7082.photogallery.Presenter;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.comp7082.photogallery.Contract.MainActivityContract;
import com.comp7082.photogallery.Model.MainActivityModel;
import com.comp7082.photogallery.R;

import java.io.IOException;

public class MainActivityPresenter extends AppCompatActivity implements MainActivityContract.Presenter {
    private MainActivityContract.View mainView;
    private MainActivityContract.Model mainModel;

    public MainActivityPresenter(MainActivityContract.View mainView, MainActivityModel mainModel) {
        this.mainView = mainView;
        this.mainModel = mainModel;
        if (this.mainModel.getPhotos().size() == 0) {
            displayPhoto(null);
        } else {
            displayPhoto(this.mainModel.getPhotos().get(this.mainModel.getIndex()));
        }
    }

    @Override
    public boolean photoPermissionCheck() {
        return mainModel.photoPermissionCheck();
    }

    @Override
    public void displayPhoto(String path) {
        if (path == null || path.equals("")) {
            mainView.setGallery(R.mipmap.ic_launcher);
            mainView.setTimeStamp("");
            mainView.setCaption("");
        } else {
            mainView.setGalleryBitmap(BitmapFactory.decodeFile(path));
            String[] attr = path.split("_");
            mainView.setTimeStamp(attr[2]);
            mainView.setCaption(attr[1]);
        }
    }

    public void takePhoto() throws IOException {
        mainModel.takePhoto();
    }

    public void nextPhoto(String caption) {
        if (mainModel.getPhotos().size() > 0) {
            mainModel.nextPhoto(caption);
            displayPhoto(mainModel.getPhotos().get(mainModel.getIndex()));
        }
    }

    public void previousPhoto(String caption) {
        if (mainModel.getPhotos().size() > 0) {
            mainModel.previousPhoto(caption);
            displayPhoto(mainModel.getPhotos().get(mainModel.getIndex()));
        }
    }

    public void activityResult(int requestCode, int resultCode, Intent data, ImageView image) {
        displayPhoto(mainModel.activityResult(requestCode, resultCode, data, image));
    }

    public void searchPhoto() {
        mainModel.searchPhoto();
    }

    public void sharePhoto() {
        mainModel.sharePhoto();
    }
}
