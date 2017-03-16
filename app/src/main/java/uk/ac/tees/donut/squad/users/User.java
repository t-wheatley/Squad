package uk.ac.tees.donut.squad.users;

import java.util.ArrayList;

import uk.ac.tees.donut.squad.squads.Interest;

/**
 * Created by Scott on 3/3/2017.
 */

public class User
{
    int id;
    String name;
    ArrayList<Interest> mySquads;

    public User()
    {
        //empty constructor
    }
    public User(String n)
    {
        name = n;
    }

    //GETTERS
    public int getId()
    {
        return id;
    }
    public String getName()
    {
        return name;
    }

    //SETTERS
    public void setName(String n)
    {
        name = n;
    }

    //mySquads management
    public void addInterest(Interest in)
    {
//        mySquads.add(in);
    }
    public void removeInterest(Interest in)
    {
        mySquads.remove(in);
    }
    //returns the Interest ENUM if you have its index
    public void getInterest(int i)
    {
        mySquads.get(i);
    }
}
