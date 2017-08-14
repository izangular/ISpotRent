package com.example.karmali.homexperts;

/**
 * Created by karmali on 14-08-2017.
 */
import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

public class AddressGlobal extends Application{
    private String addressString;
    private LatLng addresslatLng;

    public String getAddress(){
        return addressString;
    }
    public void setAddress(String addString){
        addressString=addString;
    }

    public LatLng getAddresslatLng(){
        return addresslatLng;
    }
    public void setAddressLatLng(LatLng latLng){
        addresslatLng=latLng;
    }

}
