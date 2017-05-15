package uk.ac.tees.donut.squad.users;

import java.util.HashMap;

/**
 * Class used to represent a User in Firebase.
 */
public class FBUser
{
    private String name;
    private String bio;
    private String picture;
    private HashMap<String, Boolean> squads;
    private HashMap<String, Boolean> meetups;
    private HashMap<String, Boolean> hosting;
    private Boolean secret;

    /**
     * Empty constructor for Firebase.
     */
    public FBUser()
    {

    }

    // Getters
    public String getName()
    {
        return name;
    }

    public String getBio()
    {
        return bio;
    }

    public String getPicture()
    {
        return picture;
    }

    public HashMap<String, Boolean> getSquads()
    {
        return squads;
    }

    public HashMap<String, Boolean> getMeetups()
    {
        return meetups;
    }

    public HashMap<String, Boolean> getHosting()
    {
        return hosting;
    }

    public Boolean getSecret()
    {
        return secret;
    }


    // Setters
    public void setName(String name)
    {
        this.name = name;
    }

    public void setBio(String bio)
    {
        this.bio = bio;
    }

    public void setPicture(String picture)
    {
        this.picture = picture;
    }

    public void setSquads(HashMap<String, Boolean> squads)
    {
        this.squads = squads;
    }

    public void setMeetups(HashMap<String, Boolean> meetups)
    {
        this.meetups = meetups;
    }

    public void setHosting(HashMap<String, Boolean> hosting)
    {
        this.hosting = hosting;
    }

    public void setSecret(Boolean secret)
    {
        this.secret = secret;
    }
}
