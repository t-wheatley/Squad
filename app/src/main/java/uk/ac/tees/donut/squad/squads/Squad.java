package uk.ac.tees.donut.squad.squads;

import java.util.List;

/**
 * Created by Scott on 14/03/2017.
 */

public class Squad
{
    String name;
    Interest interest;
    String description;
    List users;

    public Squad(String n, Interest i, String d)
    {
        name = n;
        interest = i;
        description = d;
    }



    //GETTERS
    public String getName()
    {
        return name;
    }
    public Interest getInterest()
    {
        return interest;
    }
    public String getDescription()
    {
        return description;
    }
    public List getUsers() {
        return users;
    }

    //SETTERS
    public void setName(String n)
    {
        name = n;
    }
    public void setInterest(Interest in)
    {
        interest = in;
    }
    public void setDescription(String d)
    {
        description = d;
    }
    public void setUsers(List users) {
        this.users = users;
    }
}
