package com.ece251.gongxl.transcanner;

/**
 * Created by david on 3/11/15.
 */
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

/**
 * Created by david on 3/11/15.
 */
public class LocationService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{
    private GoogleApiClient googleApiClient;
    private Location location;
    private LocationRequest locationRequest;
    private Context context;
    private Geocoder geocoder;
    public LocationService(Context context) {
        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        this.context = context;
        this.geocoder = new Geocoder(context);
    }

    public String getLocation() {
        if(location == null) {
            Toast.makeText(context,
                    R.string.prompt_start_location_service,
                    Toast.LENGTH_LONG);
            return null;
        }
        return location.toString();
    }

    public void startService() {
        if(googleApiClient.isConnected()
                || googleApiClient.isConnecting())
            Toast.makeText(context,
                    R.string.prompt_already_connected,
                    Toast.LENGTH_LONG).show();
        else googleApiClient.connect();
    }

    public void stopService() {
        if(googleApiClient.isConnected()
                || googleApiClient.isConnecting())
            googleApiClient.disconnect();
        else
            Toast.makeText(context,
                    R.string.prompt_already_disconnected,
                    Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("GoogleApiClient connection has been suspend");
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("GoogleApiClient connection has failed");
    }

    public String getAddress() {
        StringBuilder stringBuilder = new StringBuilder();
        List<Address> addressList = null;
        try {
            addressList =  geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(),
                    2);
            if(addressList == null)
                throw new Exception("fail to get the address");
            for(Address address : addressList) {
                stringBuilder.append("Country: " + address.getCountryName() + '\n');
                stringBuilder.append("AdminArea: " + address.getAdminArea() + '\n');
                stringBuilder.append("AddressLine1: " + address.getAddressLine(0) + '\n');
                stringBuilder.append("AddressLine2: " + address.getAddressLine(1) + '\n');
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
