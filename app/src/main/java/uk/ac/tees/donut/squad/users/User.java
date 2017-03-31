package uk.ac.tees.donut.squad.users;

import java.io.Serializable;
import java.util.ArrayList;

import uk.ac.tees.donut.squad.posts.Meetup;
import uk.ac.tees.donut.squad.squads.Interest;

/**
 * Created by Scott on 3/3/2017.
 */

public class User implements Serializable
{
    static int id;
    static String name;
    static ArrayList<Interest> mySquads;
    static ArrayList<String> myMeetups;    //arraylist containing the ids for the meetups they are attending

    public User()
    {
        //empty constructor
    }
    public User(String n)
    {
        name = n;
        mySquads = new ArrayList<Interest>();
        myMeetups = new ArrayList<String>();
    }

    //GETTERS
    static public int getId()
    {
        return id;
    }
    static public String getName()
    {
        return name;
    }

    //SETTERS
    static public void setName(String n)
    {
        name = n;
    }

    //MYSQUADS MANAGEMENT
    static public void addInterest(Interest in)
    {
//        mySquads.add(in);
    }
    static public void removeInterest(Interest in)
    {
        mySquads.remove(in);
    }
    //returns the Interest if you have its index
    static public Interest getInterest(int i)
    {
        return mySquads.get(i);
    }

    //MYMEETUPS MANAGEMENT
    static public void addMeetup(String m)
    {
        myMeetups.add(m);
    }
    static public void removeMeetup(String m)
    {
        myMeetups.remove(m);
    }
    //returns the Meetup id you have its index
    static public String getMeetup(int i)
    {
        return myMeetups.get(i);
    }
    static public void clearMyMeetups()
    {
        myMeetups.clear();
    }
    static public boolean myMeetupsContains(String id)
    {
        boolean contains = false;
        for(int i = 0; i < myMeetups.size(); i++)
        {
            if(getMeetup(i).equals(id))
            {
                contains = true;
            }
        }

        return contains;
    }
}
