package uk.ac.tees.donut.squad.posts;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by q5273202 on 11/04/2017.
 */

public class Place {
    public String placeId;

    public String name;
    public String interest;
    public String description;

    public String userId;


    public Place() {
        //empty constructor
    }

    public Place(String pi, String n, String i, String d, String u) {
        placeId = pi;

        name = n;
        interest = i;
        description = d;

        userId = u;

    }

    //GETTERS
    public String getPlaceId() {
        return placeId;
    }

    public String getName() {
        return name;
    }

    public String getInterest() {
        return interest;
    }

    public String getDescription() {
        return description;
    }

    public String getUserId() {
        return userId;
    }

    //SETTERS
    public void setPlaceId(String pi) {
        placeId = pi;
    }

    public void setName(String n) {
        name = n;
    }

    public void setInterest(String i) {
        interest = i;
    }

    public void setDescription(String d) {
        description = d;
    }

    public void setUserId(String u) {
        userId = u;
    }
}