package uk.ac.tees.donut.squad.posts;

import android.os.Build;

import uk.ac.tees.donut.squad.users.User;

/**
 * Created by Scott on 3/4/2017.
 */

public class Post
{
    int id;
    String text;
    User user;

    public Post(String t, User u)
    {
        text = t; user = u;
    }

    //GETTERS
    public int getId()
    {
        return id;
    }
    public String getText()
    {
        return text;
    }
    public User getUser()
    {
        return user;
    }
    public int getUserId()
    {
        return user.getId();
    }

    //SETTERS
    public void setText(String t)
    {
        text = t;
    }
}
