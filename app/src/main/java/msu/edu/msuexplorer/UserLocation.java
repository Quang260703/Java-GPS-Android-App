package msu.edu.msuexplorer;

import android.annotation.SuppressLint;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class UserLocation {
    // Able to use functions from the Points class
    private Points points = new Points();

    private double[] coordinatesArray;

    private LocationDeterminationListener listener;

    /**
     * Retrieves the user location
     * @param fusedLocationClient the client used for location
     */
    @SuppressLint("MissingPermission")
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

        // This warning says the user may not have accepted/rejected the permissions. However, this code is only called if the user accepted the permissions.
        fusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.getToken()).addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()) {
                    // Received Location
                    Location location = task.getResult();
                    if (location == null)
                    {
                        listener.onLocationDetermined(0, 0);
                    } else {
                        coordinatesArray = new double[]{location.getLatitude(), location.getLongitude()};

                        int calculatedPoints = points.calculatePoints(distanceBetweenLocations(coordinatesArray, destinationCoords));
                        double distance = distanceBetweenLocations(coordinatesArray, destinationCoords);
                        // Notify the listener
                        listener.onLocationDetermined(calculatedPoints, distance);
                    }
                } else {
                    listener.onLocationDetermined(0, 0);
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
        void onLocationDetermined(int points, double distanceToLocation);
    }

    public void setLocationDeterminationListener(LocationDeterminationListener listener) {
        this.listener = listener;
    }
}
