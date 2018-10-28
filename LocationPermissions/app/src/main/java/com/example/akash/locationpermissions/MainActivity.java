package com.example.akash.locationpermissions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.openxc.VehicleManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    LocationManager locationManager;
    Double lat,lng;
    Button nav,finish;
    Firebase firebase;
    Calendar c,calendar;
    TextView streetAddress, localityAddress, countryAddress, postalCode;
    ArrayList<Address> listAddress;
    String street,locality,country,postalcodeString,completeAddress,tripNameString;
    EditText tripName;
    Boolean didNavigate = false;
    Color theme;
    Firebase firebasepage2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        nav = (Button) findViewById(R.id.nav);
        finish = (Button) findViewById(R.id.finish);
        tripName = (EditText) findViewById(R.id.tripName);
        listAddress = new ArrayList<Address>();
        streetAddress = (TextView) findViewById(R.id.streetAddress);
        localityAddress = (TextView) findViewById(R.id.locality);
        countryAddress = (TextView) findViewById(R.id.Country);
        postalCode = (TextView) findViewById(R.id.postalCode);
        calendar = Calendar.getInstance();
        theme = new Color();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String formattedDate = df.format(calendar.getTime());
        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://statefair-d2825.firebaseio.com/");
        firebasepage2 = new Firebase("https://searchmytripstatefair.firebaseio.com/");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            System.out.println("Permission Checked");
            checkLocationPermission();
        } else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDisabledAlertToUser();
            }
        }

        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripNameString = tripName.getText().toString();
                if (tripNameString.length() < 1) {
                    Toast.makeText(getApplicationContext(), "Please enter a trip name", Toast.LENGTH_SHORT).show();
                } else {
                    c = Calendar.getInstance();
                    final String fromtime = c.getTime().toString();

                    Firebase tripNameData = firebase.child(tripNameString);

                    tripNameData.child("From-Date").setValue(formattedDate);
                    tripNameData.child("From-Time").setValue(fromtime);
                    tripNameData.child("From-Address").setValue(completeAddress);
                    tripNameData.child("From-Latitude").setValue(lat);
                    tripNameData.child("From-Longitude").setValue(lng);

                    Firebase searchData = firebasepage2.child(tripNameString);

                    searchData.child("From-Date").setValue(formattedDate);
                    searchData.child("From-Time").setValue(fromtime);
                    searchData.child("From-Address").setValue(completeAddress);
                    searchData.child("From-Latitude").setValue(lat);
                    searchData.child("From-Longitude").setValue(lng);

                    Toast.makeText(MainActivity.this, "Started recording data", Toast.LENGTH_SHORT).show();
                    didNavigate = true;
                }
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripNameString = tripName.getText().toString();
                if(tripNameString.length() < 1){
                    Toast.makeText(getApplicationContext(), "Please enter a trip name and navigate...", Toast.LENGTH_SHORT).show();
                } else if(didNavigate == false) {
                    Toast.makeText(getApplicationContext(), "Please navigate first....", Toast.LENGTH_SHORT).show();
                } else {
                    c = Calendar.getInstance();
                    final String totime = c.getTime().toString();

                    Firebase finishingData = firebase.child(tripNameString);

                    finishingData.child("To-Date").setValue(formattedDate);
                    finishingData.child("To-Time").setValue(totime);
                    finishingData.child("To-Address").setValue(completeAddress);
                    finishingData.child("To-Latitude").setValue(lat);
                    finishingData.child("To-Longitude").setValue(lng);

                    Firebase finishSearchData = firebasepage2.child(tripNameString);

                    finishSearchData.child("To-Date").setValue(formattedDate);
                    finishSearchData.child("To-Time").setValue(totime);
                    finishSearchData.child("To-Address").setValue(completeAddress);
                    finishSearchData.child("To-Latitude").setValue(lat);
                    finishSearchData.child("To-Longitude").setValue(lng);

                    tripName.setText("");

                    Toast.makeText(MainActivity.this, "Finished recording data", Toast.LENGTH_SHORT).show();
                }
            }
        });
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this); //if app does not work,
        //locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    private void showGPSDisabledAlertToUser()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else
        {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                showGPSDisabledAlertToUser();
            }
            return true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.US);

        try {
            listAddress = (ArrayList<Address>) geocoder.getFromLocation(lat,lng,1);
            street = listAddress.get(0).getAddressLine(0).toString();
            locality = listAddress.get(0).getLocality().toString();
            country = listAddress.get(0).getCountryName().toString();
            postalcodeString = listAddress.get(0).getPostalCode().toString();
            completeAddress = street + ", " + locality + ", " + country + ", " + postalcodeString;

            if(listAddress != null && listAddress.size()>0){
                streetAddress.setText("Street: " + street);
                localityAddress.setText("Locality: " + locality);
                countryAddress.setText("Country: " + country);
                postalCode.setText("Postal Code: " + postalcodeString);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
