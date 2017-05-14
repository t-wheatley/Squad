package uk.ac.tees.donut.squad.squads;

import java.util.HashMap;

/**
 * Class used to represent a Squad.
 */
public class Squad
{
    String id;
    String name;
    String description;
    HashMap<String, Boolean> users;

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
}
