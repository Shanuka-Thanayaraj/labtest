package com.s23010188.shanuka; // REPLACE WITH YOUR ACTUAL PACKAGE NAME

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText editTextAddress;
    private Button buttonShowLocation;
    private Button buttonContinue;

    // ActivityResultLauncher for requesting location permissions at runtime
    private ActivityResultLauncher<String[]> locationPermissionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize UI components
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonShowLocation = findViewById(R.id.buttonShowLocation);
        buttonContinue = findViewById(R.id.buttonContinue);

        // Initialize the ActivityResultLauncher for permissions
        locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (fineLocationGranted != null && fineLocationGranted) {
                Toast.makeText(this, "Fine location permission granted.", Toast.LENGTH_SHORT).show();
                initializeMap();
            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                Toast.makeText(this, "Coarse location permission granted.", Toast.LENGTH_SHORT).show();
                initializeMap();
            } else {
                Toast.makeText(this, "Location permission denied. Map functionality may be limited.", Toast.LENGTH_LONG).show();
                initializeMap();
            }
        });

        // Request location permissions when the activity is created
        requestLocationPermissions();

        // Set up click listener for "Show Location" button
        buttonShowLocation.setOnClickListener(v -> showLocationOnMap());

        // Set up click listener for "Continue" button
        buttonContinue.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, SensorActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Requests necessary location permissions from the user at runtime.
     */
    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            initializeMap();
        }
    }

    /**
     * Initializes the Google Map Fragment.
     */
    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error: Map fragment not found.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Callback method that is invoked when the map is ready to be used.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set default location to Sri Lanka (Colombo) on initial load
        LatLng sriLanka = new LatLng(6.9271, 79.8612);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 10));

        // Enable user's current location if permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    /**
     * Checks if the device has an active internet connection.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Converts an address string to LatLng coordinates and marks it on the map.
     */
    private void showLocationOnMap() {
        String addressString = editTextAddress.getText().toString().trim();
        if (addressString.isEmpty()) {
            Toast.makeText(this, "Please enter an address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for network connectivity
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // Get up to 3 results to handle ambiguous addresses
            List<Address> addresses = geocoder.getFromLocationName(addressString, 3);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0); // Take the first result
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Clear previous markers
                mMap.clear();
                // Add a new marker at the obtained location
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(addressString)
                        .snippet(address.getAddressLine(0))); // Show full address in marker snippet
                // Move camera to the new location with animation
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                // Provide more detailed feedback
                String locationDetails = address.getFeatureName() != null ? address.getFeatureName() : address.getAddressLine(0);
                Toast.makeText(this, "Location found: " + locationDetails, Toast.LENGTH_LONG).show();

                // Log additional results if available for debugging
                if (addresses.size() > 1) {
                    Toast.makeText(this, "Multiple locations found. Showing the first one.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Address not found. Try a more specific address (e.g., city, country).", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: Could not geocode address. Check network or try again.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
}