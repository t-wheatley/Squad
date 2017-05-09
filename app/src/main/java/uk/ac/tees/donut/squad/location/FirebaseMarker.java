package uk.ac.tees.donut.squad.location;

/**
 * Created by q5233000 on 09/05/2017.
 */

//http://stackoverflow.com/questions/39517835/how-to-use-firebase-realtime-database-for-android-google-map-app
public class FirebaseMarker {
    public String name;
    public String description;
    public double latitiude;
    public double longitude;
    public long startDateTime, endDateTime;

    public FirebaseMarker(){

    }

    public FirebaseMarker(String n, String d, double lat, double lng, long str, long end){
        name = n;
        description = d;
        latitiude = lat;
        longitude = lng;
        startDateTime = str;
        endDateTime = end;
    }

    public long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public long getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(long endDateTime) {
        this.endDateTime = endDateTime;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitiude() {
        return latitiude;
    }

    public void setLatitiude(double latitiude) {
        this.latitiude = latitiude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
