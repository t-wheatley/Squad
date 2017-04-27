package uk.ac.tees.donut.squad.posts;

import java.util.HashMap;

public class Meetup
{
    // Info
    String id,
            name,
            description,
            squad,
            host;

    // Attendees
    HashMap<String, Boolean> users;

    // Location
    Double longitude, latitude;

    public Meetup()
    {
        // Empty constructor
    }

    // Constructor for meetup to be post to Firebase
    public Meetup(String i, String n, String d, String s, String h)
    {
        id = i;
        name = n;
        description = d;
        squad = s;
        host = h;
    }

    // Constructor for meetup lists
    public Meetup(String i, String n, String s)
    {
        id = i;
        name = n;
        squad = s;
    }


    // Constructor with location
    public Meetup(String i, String n, String d, String s, String h, double longi, double lat)
    {
        id = i;
        name = n;
        description = d;
        squad = s;
        host = h;
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

    public String getDescription()
    {
        return description;
    }

    public String getSquad()
    {
        return squad;
    }

    public String getHost()
    {
        return host;
    }

    public HashMap<String, Boolean> getUsers()
    {
        return users;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    // SETTERS
    public void setId(String i)
    {
        this.id = i;
    }

    public void setName(String n)
    {
        this.name = n;
    }

    public void setDescription(String d)
    {
        this.description = d;
    }

    public void setSquad(String s)
    {
        this.squad = s;
    }

    public void setHost(String h)
    {
        this.host = h;
    }

    public void setAttendees(HashMap<String, Boolean> users)
    {
        this.users = users;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }
}
