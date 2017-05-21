package uk.ac.tees.donut.squad.squads;

import java.util.HashMap;

/**
 * Class used to represent a Squad.
 */
public class Squad
{
    private String id;
    private String name;
    private String description;
    private HashMap<String, Boolean> users;
    private HashMap<String, Boolean> meetups;
    private HashMap<String, Boolean> places;

    /**
     * Empty constructor for Firebase.
     */
    public Squad()
    {

    }

    /**
     * Default constructor for a new Squad.
     *
     * @param i The unique id of the Squad.
     * @param n The name of the Squad.
     * @param d The description of the Squad.
     */
    public Squad(String i, String n, String d)
    {
        id = i;
        name = n;
        description = d;
    }

    //GETTERS
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

    public void setUsers(HashMap<String, Boolean> users)
    {
        this.users = users;
    }

    public HashMap<String, Boolean> getMeetups()
    {
        return meetups;
    }

    public HashMap<String, Boolean> getPlaces()
    {
        return places;
    }

    //SETTERS
    public void setId(String id)
    {
        this.id = id;
    }

    public void setName(String n)
    {
        this.name = n;
    }

    public void setDescription(String d)
    {
        this.description = d;
    }

    public HashMap<String, Boolean> getUsers()
    {
        return users;
    }

    public void setMeetups(HashMap<String, Boolean> meetups)
    {
        this.meetups = meetups;
    }

    public void setPlaces(HashMap<String, Boolean> places)
    {
        this.places = places;
    }
}
