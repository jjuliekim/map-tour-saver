package com.example.kim_j_project4;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.kim_j_project4.databinding.ActivityDashboardBinding;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityDashboardBinding binding;
    private String username;
    private DashboardViewModel dashboardViewModel;
    private LocationManager locationManager;
    private String mediaPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // set greeting text
        Intent myIntent = getIntent();
        username = myIntent.getStringExtra("username");
        TextView welcomeText = findViewById(R.id.welcome_text);
        welcomeText.setText(String.format("Welcome, %s!", username));

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // add media button
        Button addMediaButton = findViewById(R.id.add_media_button);
        addMediaButton.setOnClickListener(v -> pickMedia());

        // save tour button
        Button saveButton = findViewById(R.id.save_tour_button);
        saveButton.setOnClickListener(v -> saveTour());

        // view tours button
        Button viewToursButton = findViewById(R.id.view_tours_button);
        viewToursButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // view tours
            }
        });

        // refreshing/updating map
        dashboardViewModel.getLocations().observe(this, locations -> {
            if (mMap != null) {
                mMap.clear();
                for (LatLng location : locations) {
                    mMap.addMarker(new MarkerOptions().position(location));
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // check permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // get user's current location and move camera there
        mMap.setMyLocationEnabled(true);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Log.i("HERE DASHBOARD", "curr loc: " + location.getLatitude() + ", " + location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                locationManager.removeUpdates(this);
            }
        });

        // allow users to mark locations of interest on the map (store that in ViewModel)
        mMap.setOnMapClickListener(location -> dashboardViewModel.addLocation(location));

    }

    // save tour information
    private void saveTour() {
        EditText nameEditText = findViewById(R.id.tour_name);
        EditText descriptionEditText = findViewById(R.id.tour_description);
        EditText webLinkEditText = findViewById(R.id.tour_link);
        String tourName = nameEditText.getText().toString();
        String tourDescription = descriptionEditText.getText().toString();
        String webLink = webLinkEditText.getText().toString();
        ArrayList<LatLng> locations = dashboardViewModel.getLocations().getValue();

        // check for valid entries
        if (tourName.isEmpty() || tourDescription.isEmpty() || locations == null || locations.isEmpty()) {
            Toast.makeText(this, "Empty Entries", Toast.LENGTH_SHORT).show();
            return;
        }

        // save tour to json file
        Tour tour = new Tour(tourName, tourDescription, webLink, mediaPath, locations);
        Gson gson = new Gson();
        String tourJson = gson.toJson(tour);
        try (FileWriter writer = new FileWriter(getFilesDir() + "/" + tourName + ".json")) {
            writer.write(tourJson);
            Toast.makeText(this, "Tour saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save tour", Toast.LENGTH_SHORT).show();
        }
    }

    // pick media
    private void pickMedia() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri selectedMediaUri = data.getData();
            if (selectedMediaUri != null) {
                mediaPath = selectedMediaUri.toString();
            }
        }
    }
}