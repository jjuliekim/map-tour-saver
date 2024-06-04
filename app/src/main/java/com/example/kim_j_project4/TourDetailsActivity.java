package com.example.kim_j_project4;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class TourDetailsActivity extends AppCompatActivity {
    private String username;
    private Tour tour;
    private EditText editTourName;
    private EditText editTourDesc;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("HERE TOUR DETAILS", "creating");
        Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tour_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // xml contents
        editTourName = findViewById(R.id.edit_tour_name);
        editTourDesc = findViewById(R.id.edit_tour_description);
        WebView webView = findViewById(R.id.web_view);
        videoView = findViewById(R.id.videoView);
        Button saveButton = findViewById(R.id.save_button);
        Button shareButton = findViewById(R.id.share_button);
        Button playAudioButton = findViewById(R.id.play_audio_button);
        Button playVideoButton = findViewById(R.id.play_video_button);
        Log.i("HERE TOUR DETAILS", "creating map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(googleMap -> {
            for (LatLng location : tour.getLocations()) {
                googleMap.addMarker(new MarkerOptions().position(location));
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tour.getLocations().get(0), 10));
        });

        Intent myIntent = getIntent();
        username = myIntent.getStringExtra("username");
        tour = myIntent.getParcelableExtra("tour");

        // display tour details
        editTourName.setText(tour.getName());
        editTourDesc.setText(tour.getDescription());
        if (!tour.getWebLink().isEmpty()) {
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(tour.getWebLink());
        }
        if (!tour.getMediaPath().isEmpty()) {
            if (tour.getMediaPath().endsWith("mp4")) {
                playAudioButton.setVisibility(View.INVISIBLE);
            } else {
                playVideoButton.setVisibility(View.INVISIBLE);
            }
        } else {
            playAudioButton.setVisibility(View.INVISIBLE);
            playVideoButton.setVisibility(View.INVISIBLE);
        }

        saveButton.setOnClickListener(v -> saveTourEdits());
        shareButton.setOnClickListener(v -> shareTour());
        playVideoButton.setOnClickListener(v -> playVideo());
        playAudioButton.setOnClickListener(v -> playAudio());

    }

    // save edits and go back to dashboard
    private void saveTourEdits() {
        String newName = editTourName.getText().toString();
        String newDescription = editTourDesc.getText().toString();
        if (newName.isEmpty() || newDescription.isEmpty()) {
            Toast.makeText(this, "Empty Entries", Toast.LENGTH_SHORT).show();
            return;
        }
        // update tour list
        ArrayList<Tour> tourList = loadTours();
        for (int i = 0; i < tourList.size(); i++) {
            if (tourList.get(i).getName().equals(tour.getName())) {
                tour.setName(newName);
                tour.setDescription(newDescription);
                tourList.set(i, tour);
                break;
            }
        }
        saveToursToJson(tourList);

        Intent nextIntent = new Intent(TourDetailsActivity.this, DashboardActivity.class);
        nextIntent.putExtra("username", username);
        startActivity(nextIntent);
    }

    // share tour's name and description via email or text using implicit intent
    private void shareTour() {
        String tourName = tour.getName();
        String tourDescription = tour.getDescription();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Tour: " + tourName + "!\n" + tourDescription);
        startActivity(Intent.createChooser(shareIntent, "Share Tour"));
    }

    // play video
    private void playVideo() {
        try {
            Uri videoUri = Uri.parse(tour.getMediaPath());
            videoView.setVideoURI(videoUri);
            videoView.start();
        } catch (Exception e) {
            Log.i("HERE TOUR DETAILS", "video e: " + e.getMessage());
        }
    }

    // play audio
    private void playAudio() {
        if (!tour.getMediaPath().endsWith(".3gp")) {
            Log.i("HERE TOUR DETAILS", "not audio");
            return;
        }
        Uri audioUri = Uri.parse(tour.getMediaPath());
        MediaPlayer mp = MediaPlayer.create(TourDetailsActivity.this, audioUri);
        mp.start();
    }

    // load tours from json file
    private ArrayList<Tour> loadTours() {
        ArrayList<Tour> tourList = new ArrayList<>();
        try {
            FileInputStream fis = openFileInput(username + ".json");
            Scanner scanner = new Scanner(fis);
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
            }
            scanner.close();
            fis.close();

            String jsonString = stringBuilder.toString();
            JSONObject userJson = new JSONObject(jsonString);
            JSONArray tourArray = userJson.getJSONArray("tourList");

            tourList = new ArrayList<>();
            for (int i = 0; i < tourArray.length(); i++) {
                JSONObject tourJson = tourArray.getJSONObject(i);
                String tourName = tourJson.getString("tourName");
                String description = tourJson.getString("description");
                String webLink = tourJson.getString("webLink");
                String mediaPath = tourJson.getString("mediaPath");

                JSONArray locationArray = tourJson.getJSONArray("locations");
                ArrayList<LatLng> locations = new ArrayList<>();
                for (int j = 0; j < locationArray.length(); j++) {
                    JSONObject locationJson = locationArray.getJSONObject(j);
                    double latitude = locationJson.getDouble("latitude");
                    double longitude = locationJson.getDouble("longitude");
                    locations.add(new LatLng(latitude, longitude));
                }

                Tour tour = new Tour(tourName, description, webLink, mediaPath, locations);
                tourList.add(tour);
            }
            Log.i("HERE VIEW TOURS", "loaded tours");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load tours", Toast.LENGTH_SHORT).show();
            Log.i("HERE VIEW TOURS", "Failed to load tours", e);
        }
        return tourList;
    }

    // save tours to json file
    private void saveToursToJson(ArrayList<Tour> tourList) {
        try {
            JSONArray tourArray = new JSONArray();
            for (Tour tour : tourList) {
                JSONObject tourJson = new JSONObject();
                tourJson.put("tourName", tour.getName());
                tourJson.put("description", tour.getDescription());
                tourJson.put("webLink", tour.getWebLink());
                tourJson.put("mediaPath", tour.getMediaPath());
                JSONArray locationArray = new JSONArray();
                for (LatLng location : tour.getLocations()) {
                    JSONObject locationJson = new JSONObject();
                    locationJson.put("latitude", location.latitude);
                    locationJson.put("longitude", location.longitude);
                    locationArray.put(locationJson);
                }
                tourJson.put("locations", locationArray);
                tourArray.put(tourJson);
            }
            JSONObject userJson = new JSONObject();
            userJson.put("username", username);
            userJson.put("tourList", tourArray);
            // save to file
            String json = userJson.toString();
            FileOutputStream fos = openFileOutput(username + ".json", Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
            Toast.makeText(this, "Tour Saved", Toast.LENGTH_SHORT).show();
            Log.i("HERE DASHBOARD", "tour saved");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save tour", Toast.LENGTH_SHORT).show();
            Log.e("DashboardActivity", "Failed to save tour", e);
        }
    }
}
