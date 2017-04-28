package uk.ac.tees.donut.squad.posts;


/**
 * Created by q5273202 on 21/04/2017.
 */

public class AddressPlace extends Place
{
    public String address1;
    public String address2;
    public String townCity;
    public String county;
    public String postCode;

    public AddressPlace()
    {
        //Empty Constructor
    }

    public AddressPlace(String pi, String n, String d, String s, String u, String a1, String a2, String tc, String c, String pc)
    {
        super(pi, n, d, s, u);
        address1 = a1.trim();
        address2 = a2.trim();
        townCity = tc.trim();
        county = c.trim();
        postCode = pc.trim().toUpperCase();
    }

    //GETTERS
    public String getAddress1()
    {
        return address1;
    }

    public String getAddress2()
    {
        return address2;
    }

    public String getTownCity()
    {
        return townCity;
    }

    public String getCounty()
    {
        return county;
    }

    public String getPostCode()
    {
        return postCode;
    }

    public String fullAddress()
    {
        String a = "";
        if (address1.length() != 0)
            a = a + address1;
        if (address2.length() != 0)
            a = a + ", " + address2;
        if (townCity.length() != 0)
            a = a + ", " + townCity;
        if (county.length() != 0)
            a = a + ", " + county;
        if (postCode.length() != 0)
            a = a + ", " + postCode;

        return a;
    }

    //SETTERS
    public void setAddress1(String a1)
    {
        this.address1 = a1.trim();
    }

    public void setAddress2(String a2)
    {
        this.address2 = a2.trim();
    }

    public void setTownCity(String tc)
    {
        this.townCity = tc.trim();
    }

    public void setCounty(String c)
    {
        this.county = c.trim();
    }

    public void setPostCode(String pc)
    {
        this.postCode = pc.trim().toUpperCase();
    }
}
