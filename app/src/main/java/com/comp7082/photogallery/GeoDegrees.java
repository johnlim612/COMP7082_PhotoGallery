package com.comp7082.photogallery;

import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

public class GeoDegrees {
    public Double Latitude, Longitude;

    public Double getLatitude() {
        return Latitude;
    }
    public Double getLongitude() {
        return Longitude;
    }
    public void geoDegrees(ExifInterface exif){
        String attrLATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String attrLATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String attrLONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String attrLONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

        if((attrLATITUDE !=null)
                && (attrLATITUDE_REF !=null)
                && (attrLONGITUDE != null)
                && (attrLONGITUDE_REF !=null))
        {
            Latitude = convertToDegree(attrLATITUDE);
            Longitude = convertToDegree(attrLONGITUDE);
        }
    };

    private Double convertToDegree(String stringDMS){
        Double result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = Double.valueOf(stringD[0]);
        Double D1 = Double.valueOf(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = Double.valueOf(stringM[0]);
        Double M1 = Double.valueOf(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = Double.valueOf(stringS[0]);
        Double S1 = Double.valueOf(stringS[1]);
        Double FloatS = S0/S1;

        result = FloatD + (FloatM/60) + (FloatS/3600);

        return result;
    };
}
