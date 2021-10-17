package com.comp7082.photogallery;

import java.util.ArrayList;

public class PhotosSingleton {

    private static PhotosSingleton singleton = null;

    private ArrayList<String> photos;

    private PhotosSingleton()
    {
        photos = new ArrayList<String>();
    }

    public static PhotosSingleton getSingleton()
    {
        if (singleton == null)
        {
            singleton = new PhotosSingleton();
        }
        return singleton;
    }

    public ArrayList<String> getPhotos()
    {
        return photos;
    }

    public void setPhotos(ArrayList<String> newPhotos)
    {
        photos = newPhotos;
    }

    public void renamePhoto(int index, String newName)
    {
        photos.set(index, newName);
    }
}
