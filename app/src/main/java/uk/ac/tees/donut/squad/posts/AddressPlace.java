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

    public AddressPlace(String pi, String n, String i, String d, String u, String a1, String a2, String tc, String c, String pc)
    {
        super(pi, n, i, d, u);
        address1 = a1;
        address2 = a2;
        townCity = tc;
        county = c;
        postCode = pc;
    }

    //GETTERS
    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getTownCity() {
        return townCity;
    }

    public String getCounty() {
        return county;
    }

    public String getPostCode() {
        return postCode;
    }

    public String FullAddress() {
        String a = "";
        if (address1 != null)
            a = a + address1;
        if (address2 != null)
            a = a + ", " + address2;
        if (townCity != null)
            a = a + ", " + townCity;
        if (county != null)
            a = a + ", " + county;
        if (postCode != null)
            a = a + ", " + postCode;

        return a;
    }

    //SETTERS
    public void setAddress1(String a1) {
        address1 = a1;
    }

    public void setAddress2(String a2) {
        address2 = a2;
    }

    public void setTownCity(String tc) {
        townCity = tc;
    }

    public void setCounty(String c) {
        county = c;
    }

    public void setPostCode(String pc) {
        postCode = pc;
    }
}
