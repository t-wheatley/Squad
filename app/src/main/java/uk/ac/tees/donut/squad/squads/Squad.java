package uk.ac.tees.donut.squad.squads;

/**
 * Created by Scott on 14/03/2017.
 */

public class Squad
{
    String id;
    String name;
    String description;

    public Squad()
    {

    }

    public Squad(String i, String n, String d)
    {
        id = i;
        name = n;
        description = d;
    }



    //GETTERS
    public String getName()
    {
        return name;
    }
    public String getDescription()
    {
        return description;
    }


    //SETTERS
    public void setName(String n)
    {
        name = n;
    }
    public void setDescription(String d)
    {
        description = d;
    }

}
