package com.example.kim_j_project4;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
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

public class TourDetailsActivity extends AppCompatActivity {
    private String username;
    private Tour tour;
    private EditText editTourName;
    private EditText editTourDesc;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(googleMap -> {
            for (LatLng location : tour.getLocations()) {
                googleMap.addMarker(new MarkerOptions().position(location));
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tour.getLocations().get(0), 10));
        });

        Intent myIntent = getIntent();
        username = myIntent.getStringExtra("username");
        tour = (Tour) myIntent.getSerializableExtra("tour");

        // display tour details
        editTourName.setText(tour.getName());
        editTourDesc.setText(tour.getDescription());
        if (!tour.getWebLink().isEmpty()) {
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
        tour.setName(newName);
        tour.setDescription(newDescription);

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
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, tourName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, tourDescription);
        startActivity(Intent.createChooser(shareIntent, "Share Tour"));
    }

    // play video
    private void playVideo() {
        videoView.setVideoPath(tour.getMediaPath());
        videoView.start();
    }

    // play audio
    private void playAudio() {
        Uri audioUri = Uri.parse(tour.getMediaPath());
        MediaPlayer mp = MediaPlayer.create(TourDetailsActivity.this, audioUri);
        mp.start();
    }
}
