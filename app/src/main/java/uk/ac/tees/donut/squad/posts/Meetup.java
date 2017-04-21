package uk.ac.tees.donut.squad.posts;

import uk.ac.tees.donut.squad.squads.Interest;
import uk.ac.tees.donut.squad.users.User;

/**
 * Created by Scott Taylor, Thomas Wheatley
 */

public class Meetup
{
    String id,
            name,
            description,
            interest,
            user;

    // Temp location variables
    Double longitude, latitude;

    public Meetup()
    {
        // Empty constructor
    }

    // Constructor for meetup to be post to Firebase
    public Meetup(String i, String n, String in, String d, String u)
    {
        id = i;
        name = n;
        interest = in;
        description = d;
        user = u;
    }

    // Constructor for meetup lists
    public Meetup(String i, String n, String in)
    {
        id = i; name = n; interest = in;
    }


    // Constructor with temp location variables
    public Meetup(String i, String n, String in, String d, String u, double longi, double lat)
    {
        id = i;
        name = n;
        interest = in;
        description = d;
        longitude = longi;
        latitude = lat;
    }

    // GETTERS
    public String getId()
    {
        return id;
    }
    public String getName()
    {
        return name;
    }
    public String getInterest()
    {
        return interest;
    }
    public String getDescription()
    {
        return description;
    }
    public String getUser()
    {
        return user;
    }
    // Temp location getters


    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    // SETTERS
    public void setId(String i)
    {
        id = i;
    }
    public void setName(String n)
    {
        name = n;
    }
    public void setInterest(String in)
    {
        interest = in;
    }
    public void setDescription(String d)
    {
        description = d;
    }
    public void setUser(String u)
    {
        user = u;
    }
    // Temp location setter

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
