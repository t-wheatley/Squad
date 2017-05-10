package uk.ac.tees.donut.squad.posts;

import java.util.HashMap;

public class Place
{

    public String placeId;
    public String name;
    public String description;
    public String squad;
    public String host;
    HashMap<String, Boolean> meetups;


    public Place()
    {
        //empty constructor
    }

    public Place(String pi, String n, String d, String s, String h)
    {
        placeId = pi;
        name = n;
        description = d;
        squad = s;
        host = h;

    }

    //GETTERS
    public String getPlaceId()
    {
        return placeId;
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

    public HashMap<String, Boolean> getMeetups()
    {
        return meetups;
    }

    //SETTERS
    public void setPlaceId(String pi)
    {
        this.placeId = pi;
    }

    public void setName(String n)
    {
        this.name = n;
    }

    public void setDescription(String d)
    {
        this.description = d;
    }

    public void setSquad(String squad)
    {
        this.squad = squad;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setMeetups(HashMap<String, Boolean> meetups)
    {
        this.meetups = meetups;
    }
}