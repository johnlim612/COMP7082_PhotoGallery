package com.comp7082.photogallery.Structural;

import com.comp7082.photogallery.Structural.PostPhoto;
import com.comp7082.photogallery.Structural.SearchPhoto;
import com.comp7082.photogallery.Structural.SnapPhoto;

import java.io.IOException;
import java.util.Date;


public class PhotoFacade {

    private String imageUri;

    private Date startTimestamp, endTimestamp;
    private Double latitude, longitude;
    private String keywords;

    public void postPhoto(){
        PostPhoto.sharePhoto(imageUri);
    }

    public void searchPhoto(){
        SearchPhoto.findPhotos(startTimestamp, endTimestamp, latitude, longitude, keywords);
    }

    public void snapPhoto() throws IOException {
        SnapPhoto.takePhoto();
    }

}
