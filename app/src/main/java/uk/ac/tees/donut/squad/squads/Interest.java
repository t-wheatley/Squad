package uk.ac.tees.donut.squad.squads;

/**
 * Created by Tom on 17/03/2017.
 */

public class Interest
{
    String id, name;

    public Interest()
    {
    }

    public Interest(String i, String n)
    {
        id = i; name = n;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
