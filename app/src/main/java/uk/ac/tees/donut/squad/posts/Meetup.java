package uk.ac.tees.donut.squad.posts;

import java.util.HashMap;

/**
 * Class used to represent a Meetup.
 */
public class Meetup
{
    // Info
    String id,
            name,
            description,
            squad,
            host,
            place,
            address1,
            address2,
            townCity,
            county,
            postCode;

    // Attendees
    HashMap<String, Boolean> users;

    // DateTime
    Long startDateTime, endDateTime;

    //  Status
    //0 = upcoming, 1 = ongoing, 2 = happened/expired
    int status;

    // Location
    Double longitude, latitude;

    /**
     * Empty constructor for Firebase.
     */
    public Meetup()
    {

    }

    /**
     * Default constructor for a new Meetup.
     *
     * @param i     The unique id of the Meetup.
     * @param n     The name of the Meetup.
     * @param d     The description of the Meetup.
     * @param s     The Squad the Meetup belongs to.
     * @param h     The Host User of the Meetup.
     * @param sd    The starting DateTime of the Meetup.
     * @param ed    The ending DateTime of the Meetup.
     * @param longi The longitude of the Meetup.
     * @param lat   The latitude of the Meetup.
     */
    public Meetup(String i, String n, String d, String s, String h, long sd, long ed, double longi, double lat, String a1, String a2, String town, String c, String pc)
    {
        id = i;
        name = n;
        description = d;
        squad = s;
        host = h;
        startDateTime = sd;
        endDateTime = ed;
        longitude = longi;
        latitude = lat;
        address1 = a1;
        address2 = a2;
        townCity = town;
        county = c;
        postCode = pc;

        updateStatus();
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

    public String getDescription()
    {
        return description;
    }

    public String getSquad()
    {
        return squad;
    }

    public String getHost()
    {
        return host;
    }

    public String getPlace()
    {
        return place;
    }

    public HashMap<String, Boolean> getUsers()
    {
        return users;
    }

    public Long getStartDateTime()
    {
        return startDateTime;
    }

    public Long getEndDateTime()
    {
        return endDateTime;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public String getAddress1(){return address1;}

    public String getAddress2(){return address2;}

    public String getTownCity(){return townCity;}

    public String getCounty(){return county;}

    public String getPostCode(){return postCode;}

    public int gimmeStatus()    //not 'get' so doesn't get sent to firebase
    {
        return status;
    }

    // SETTERS
    public void setId(String i)
    {
        this.id = i;
    }
    public void setName(String n)
    {
        this.name = n;
    }

    public void setDescription(String d)
    {
        this.description = d;
    }

    public void setSquad(String s)
    {
        this.squad = s;
    }

    public void setHost(String h)
    {
        this.host = h;
    }

    public void setPlace(String place)
    {
        this.place = place;
    }

    public void setUsers(HashMap<String, Boolean> users)
    {
        this.users = users;
    }

    public void setStartDateTime(Long startDateTime)
    {
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(Long endDateTime)
    {
        this.endDateTime = endDateTime;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public void setAddress2(String address2) {this.address2 = address2;}

    public void setCounty(String c) {county = c;}

    public void setPostCode(String postCode) {this.postCode = postCode;}

    public void setStatus(int status) {this.status = status;}

    public void setTownCity(String townCity) {this.townCity = townCity;}

    public void setAddress1(String a1) {
        address1 = a1;}

    public void changeStatus(int st)    //not 'set' so firebase api doesn't do anything with it
    {
        status = st;
    }

    //Updating Status
    public void updateStatus()
    {
        Long current = System.currentTimeMillis() / 1000L;
        if (current < getStartDateTime())
            status = 0;
        else if (current > getStartDateTime() && current < getEndDateTime())
            status = 1;
        else
            status = 2;
    }
}
