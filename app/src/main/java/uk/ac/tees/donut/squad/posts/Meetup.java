package uk.ac.tees.donut.squad.posts;

import uk.ac.tees.donut.squad.users.User;

/**
 * Created by Scott Taylor on 3/3/2017.
 * if you contribute to this class put your name here as well
 */

public class Meetup
{
    int id;
    String name,
            interest,
            description;
    User user;

    //temp location variables
    String address, postCode;

    public Meetup()
    {
        //empty constructor
    }
    //constructor for meetup lists
    public Meetup(int i, String n, String in)
    {
        id = i; name = n; interest = in;
    }
    //constructor with temp location variables
    public Meetup(int i, String n, String in, String d, String a, String p)
    {
        id = i;
        name = n;
        interest = in;
        description = d;
        address = a;
        postCode = p;
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
    public String getInterest()
    {
        return interest;
    }
    public String getDescription()
    {
        return description;
    }
    //temp location getters
    public String getAddress()
    {
        return address;
    }
    public String getPostCode()
    {
        return postCode;
    }

    //SETTERS
    public void setName(String n)
    {
        name = n;
    }
    public void setInterest(String in)
    {
        interest = in;
    }
    public void setDescription(String d)
    {
        description = d;
    }
    //temp location setter
    public void setAddress(String a)
    {
        address = a;
    }
    public void setPostCode(String p)
    {
        postCode = p;
    }
}
