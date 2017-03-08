package uk.ac.tees.donut.squad.users;

/**
 * Created by Scott on 3/3/2017.
 */

public class User
{
    int id;
    String name;

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
}
