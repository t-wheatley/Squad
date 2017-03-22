package uk.ac.tees.donut.squad.users;

/**
 * Created by Scott on 3/22/2017.
 */

public class CurrentUser
{
    public static User u;

    //GETTER
    public User getUser()
    {
        return u;
    }

    //SETTER
    public void setUser(User us)
    {
        u = us;
    }
}
