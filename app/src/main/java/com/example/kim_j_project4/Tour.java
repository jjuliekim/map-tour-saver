package com.example.kim_j_project4;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class Tour implements Serializable {
    private String name;
    private String description;
    private String webLink;
    private String mediaPath;
    private ArrayList<LatLng> locations;

    // constructor
    public Tour(String name, String description, String webLink, String mediaPath, ArrayList<LatLng> locations) {
        this.name = name;
        this.description = description;
        this.webLink = webLink;
        this.mediaPath = mediaPath;
        this.locations = locations;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<LatLng> getLocations() {
        return locations;
    }

    public String getWebLink() {
        return webLink;
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public void setLocations(ArrayList<LatLng> locations) {
        this.locations = locations;
    }
}
