package uk.ac.tees.donut.squad.posts;

import java.util.HashMap;

/**
 * Class to represent a Place.
 */
public class Place
{

    private String placeId;
    private String name;
    private String description;
    private String squad;
    private String host;
    private HashMap<String, Boolean> meetups;
    private HashMap<String, String> pictures;

    /**
     * Empty constructor for Firebase.
     */
    public Place()
    {
        //empty constructor
    }

    /**
     * Constructor for a default Place.
     *
     * @param pi The unique id of the Place.
     * @param n  The name of the Place.
     * @param d  The description of the Place.
     * @param s  The Squad the Place belongs to.
     * @param h  The User who created the Place.
     */
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

    public HashMap<String, String> getPictures()
    {
        return pictures;
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

    public void setPictures(HashMap<String, String> pictures)
    {
        this.pictures = pictures;
    }
}