package uk.ac.tees.donut.squad.location;

/**
 * Created by Anthony Ward on 17/03/2017.
 */

public final class LocContants
{
    //Geocode result variables
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;

    //Variables deciding whether to geocode based on address of location
    public static final int USE_ADDRESS_NAME = 1;
    public static final int USE_ADDRESS_LOCATION = 2;

    //Geocode package
    public static final String PACKAGE_NAME =
            "com.sample.foo.simplelocationapp";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    //Result Variables
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String RESULT_ADDRESS = PACKAGE_NAME + ".RESULT_ADDRESS";
    public static final String LOCATION_LATITUDE_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_LATITUDE_DATA_EXTRA";
    public static final String LOCATION_LONGITUDE_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_LONGITUDE_DATA_EXTRA";
    public static final String LOCATION_NAME_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_NAME_DATA_EXTRA";
    public static final String FETCH_TYPE_EXTRA = PACKAGE_NAME + ".FETCH_TYPE_EXTRA";
}
