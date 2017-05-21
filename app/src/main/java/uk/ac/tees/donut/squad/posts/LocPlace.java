package uk.ac.tees.donut.squad.posts;

/**
 * Created by q5273202 on 21/04/2017.
 */

public class LocPlace extends AddressPlace
{
    private Double locLat;
    private Double locLong;

    public LocPlace()
    {
        //Empty constructor
    }

    public LocPlace(String pi, String n, String d, String s, String u, String a1, String a2, String tc, String c, String pc, double la, double lo)
    {
        super(pi, n, d, s, u, a1, a2, tc, c, pc);
        locLat = la;
        locLong = lo;
    }


    //GETTERS
    public double getLocLat()
    {
        return locLat;
    }

    public double getLocLong()
    {
        return locLong;
    }


}
