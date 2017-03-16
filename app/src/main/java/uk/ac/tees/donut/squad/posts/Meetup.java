package uk.ac.tees.donut.squad.posts;

import uk.ac.tees.donut.squad.squads.Interest;
import uk.ac.tees.donut.squad.users.User;

/**
 * Created by Scott Taylor, Thomas Wheatley
 */

public class Meetup
{
    String id;
    String name,
            description,
            interest;
    User user;

    // Temp location variables
    String address, postCode;

    public Meetup()
    {
        // Empty constructor
    }

    // Constructor for meetup to be post to Firebase
    public Meetup(String i, String n, String in, String d)
    {
        id = i;
        name = n;
        interest = in;
        description = d;
    }

    // Constructor for meetup lists
    public Meetup(String i, String n, String in)
    {
        id = i; name = n; interest = in;
    }


    // Constructor with temp location variables
    public Meetup(String i, String n, String in, String d, String a, String p)
    {
        id = i;
        name = n;
        interest = in;
        description = d;
        address = a;
        postCode = p;
    }

    // GETTERS
    public String getId()
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
    // Temp location getters
    public String getAddress()
    {
        return address;
    }
    public String getPostCode()
    {
        return postCode;
    }

    // SETTERS
    public void setId(String i)
    {
        id = i;
    }
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
    // Temp location setter
    public void setAddress(String a)
    {
        address = a;
    }
    public void setPostCode(String p)
    {
        postCode = p;
    }
}
