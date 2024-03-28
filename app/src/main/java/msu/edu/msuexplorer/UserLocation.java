package msu.edu.msuexplorer;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class UserLocation {
    // TODO - These member variables (except points) are for TESTING. Will be removed.

    // How I calculated POINTS_RADIUS in Points class. First, calculated distance between top and bottom
    // Second, calculated distance between left and right. Then, took average (1850). As I type this
    // out, I realize I should have just coded it and not have done it by hand - whoops
    private double[] bottomCampus = {42.72077, -84.48193};
    private double[] topCampus = {42.73439, -84.48195};
    private double[] leftCampus = {42.72384, -84.489364};
    private double[] rightCampus = {42.72359, -84.46255};

    private double[] stemBuilding = {42.72662, -84.48304};
    private double[] stemBuildingClose = {42.72662, -84.48299};
    private double[] stemBuildingNorth = {42.72681, -84.48316};

    private double[] chemistryBuidling = {42.72487, -84.47618};

    private double[] wilsonHall = {42.72284, -84.48904};
    private double[] akersHall = {42.72426, -84.46475};

    // Able to use functions from the Points class
    private Points points = new Points();

    private double[] coordinatesArray;

    private LocationDeterminationListener listener;

    /**
     * Retrieves the user location
     * @param fusedLocationClient the client used for location
     */
    public void determineLocation(FusedLocationProviderClient fusedLocationClient, double[] destinationCoords) {
        // Used https://developers.google.com/android/reference/com/google/android/gms/location/CurrentLocationRequest.Builder
        // and https://youtu.be/M0kUd2dpxo4?si=omrkKS9WFPh57pVK to help create the CurrentLocationRequest
        CurrentLocationRequest.Builder builder = new CurrentLocationRequest.Builder();
        builder.setGranularity(Granularity.GRANULARITY_FINE);
        builder.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        builder.setMaxUpdateAgeMillis(0);
        builder.setDurationMillis(10000);
        CurrentLocationRequest currentLocationRequest = builder.build();

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        // Used https://developer.android.com/develop/sensors-and-location/location/retrieve-current
        // and https://youtu.be/M0kUd2dpxo4?si=omrkKS9WFPh57pVK to retrieve the user's location
        // TODO - This warning says the user may not have accepted/rejected the permissions. However, this code is only called if the user accepted the permissions.
        fusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.getToken()).addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()) {
                    // Received Location
                    Location location = task.getResult();
                    coordinatesArray = new double[]{location.getLatitude(), location.getLongitude()};

                    int calculatedPoints = points.calculatePoints(distanceBetweenLocations(coordinatesArray, destinationCoords));
                    // Notify the listener
                    listener.onLocationDetermined(calculatedPoints);

                    /*
                    // TODO - This is for testing - will be deleted later
                    Log.i("getCurrentLocation", "YOUR LOCATION - Please type it into Google Maps to see if it's correct: " + location.getLatitude() + ", " + location.getLongitude());

                    int calculatedPoints = points.calculatePoints(distanceBetweenLocations(coordinatesArray, stemBuilding));
                    Log.i("getCurrentLocation", "Points for YOUR CURRENT LOCATION-STEM Building: " + calculatedPoints);

                    calculatedPoints = points.calculatePoints(distanceBetweenLocations(coordinatesArray, chemistryBuidling));
                    Log.i("getCurrentLocation", "Points for YOUR CURRENT LOCATION-Department of Chemistry: " + calculatedPoints);

                    calculatedPoints = points.calculatePoints(distanceBetweenLocations(coordinatesArray, wilsonHall));
                    Log.i("getCurrentLocation", "Points for YOUR CURRENT LOCATION-Wilson Hall: " + calculatedPoints);

                    calculatedPoints = points.calculatePoints(distanceBetweenLocations(coordinatesArray, akersHall));
                    Log.i("getCurrentLocation", "Points for YOUR CURRENT LOCATION-Akers Hall: " + calculatedPoints);


                    calculatedPoints = points.calculatePoints(distanceBetweenLocations(stemBuilding, chemistryBuidling));
                    Log.i("getCurrentLocation", "Points for STEM Building-Department of Chemistry: " + calculatedPoints);

                    calculatedPoints = points.calculatePoints(distanceBetweenLocations(wilsonHall, chemistryBuidling));
                    Log.i("getCurrentLocation", "Points for Wilson Hall-Department of Chemistry: " + calculatedPoints);

                    calculatedPoints = points.calculatePoints(distanceBetweenLocations(akersHall, chemistryBuidling));
                    Log.i("getCurrentLocation", "Points for Akers Hall-Department of Chemistry: " + calculatedPoints);

                    calculatedPoints = points.calculatePoints(distanceBetweenLocations(wilsonHall, akersHall));
                    Log.i("getCurrentLocation", "Points for Wilson Hall-Akers Hall: " + calculatedPoints);

                    calculatedPoints = points.calculatePoints(distanceBetweenLocations(stemBuilding, stemBuildingClose));
                    Log.i("getCurrentLocation", "Points for STEM Building-Very Close to same spot (STEM Building): " + calculatedPoints);

                    calculatedPoints = points.calculatePoints(distanceBetweenLocations(stemBuilding, stemBuildingNorth));
                    Log.i("getCurrentLocation", "Points for STEM Building and North STEM Building: " + calculatedPoints);*/
                }
            }
        });
    }

    /**
     * Calculates the distance between two locations
     * @param locationOne The first location
     * @param locationTwo The second location
     * @return The distance between two locations (in meters)
     */
    public double distanceBetweenLocations(Location locationOne, Location locationTwo) {
        // Log.i("disBetweenLocations", "" + locationOne.distanceTo(locationTwo));
        return locationOne.distanceTo(locationTwo);
    }

    /**
     * Calculates the distance between two sets of latitude and longitude coordinates
     * @param coordinatesOne The first set of coordinates
     * @param coordinatesTwo The second set of coordinates
     * @return the distance between the two sets of coordinates (in meters)
     */
    public double distanceBetweenLocations(double[] coordinatesOne, double[] coordinatesTwo) {
        Location locationA = new Location("A");
        locationA.setLatitude(coordinatesOne[0]);
        locationA.setLongitude(coordinatesOne[1]);

        Location locationB = new Location("B");
        locationB.setLatitude(coordinatesTwo[0]);
        locationB.setLongitude(coordinatesTwo[1]);

        return this.distanceBetweenLocations(locationA, locationB);
    }

    public interface LocationDeterminationListener {
        void onLocationDetermined(int points);
    }

    public void setLocationDeterminationListener(LocationDeterminationListener listener) {
        this.listener = listener;
    }
}
