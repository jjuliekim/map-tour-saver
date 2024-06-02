package com.example.kim_j_project4;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Tour implements Parcelable {
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

    // parcelable implementation
    protected Tour(Parcel in) {
        name = in.readString();
        description = in.readString();
        webLink = in.readString();
        mediaPath = in.readString();
        locations = in.createTypedArrayList(LatLng.CREATOR);
    }

    public static final Creator<Tour> CREATOR = new Creator<Tour>() {
        @Override
        public Tour createFromParcel(Parcel in) {
            return new Tour(in);
        }

        @Override
        public Tour[] newArray(int size) {
            return new Tour[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(webLink);
        dest.writeString(mediaPath);
        dest.writeTypedList(locations);
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
