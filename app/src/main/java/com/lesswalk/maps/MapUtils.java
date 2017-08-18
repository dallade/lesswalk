package com.lesswalk.maps;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by elad on 17/08/17.
 */

public class MapUtils {

    public static List<Address> searchAddress(Context context, String addressStr, int maxResults) {
        Geocoder coder = new Geocoder(context.getApplicationContext());
        List<Address> addressList = null;
        try {
            addressList = coder.getFromLocationName(addressStr, maxResults);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return addressList;
    }

    public static Address getAddress(Context context, String addressStr) {
        List<Address> addresses = searchAddress(context, addressStr, 1);
        try {
            return addresses.get(0);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Address getAddress(Context context, LatLng latLng){
        Address address = null;
        Geocoder geocoder = new Geocoder(context.getApplicationContext());
        try {
            address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}
