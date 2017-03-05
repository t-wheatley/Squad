package uk.ac.tees.donut.squad.events;

/**
 * Created by Scott Taylor on 3/3/2017.
 * if you contribute to this class put your name here as well
 */

public class Event
{
    int id;
    String name,
            interest,
            description;

    //temp location variables
    String address, postCode;

    public Event()
    {
        //empty constructor
    }
    public Event(String n, String i, String d)
    {
        name = n; interest = i; description = d;
    }
    //constructor with temp location variable
    public Event(String n, String i, String d, String a, String p)
    {
        name = n;
        interest = i;
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
    public void setInterest(String i)
    {
        interest = i;
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
