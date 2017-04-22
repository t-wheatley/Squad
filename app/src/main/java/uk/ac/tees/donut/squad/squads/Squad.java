package uk.ac.tees.donut.squad.squads;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Scott on 14/03/2017.
 */

public class Squad
{
    String id;
    String name;
    String description;
    HashMap<String, String> users;

    public Squad()
    {

    }

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
    public void setUsers(HashMap<String, String> users)
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
    public HashMap<String, String> getUsers()
    {
        return users;
    }
}
