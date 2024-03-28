package msu.edu.msuexplorer;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LocationViewModel extends AndroidViewModel {
    private LocationRepository locationRepository;
    private final List<LocationEntity> allLocations;

    public LocationViewModel(Application application) {
        super(application);
        locationRepository = new LocationRepository(application);
        allLocations = locationRepository.getAllLocations();
    }

    List<LocationEntity> getAllLocations() {
        return allLocations;
    }

    // Get a location by its ID
    LocationEntity getLocationByID(int id) { return locationRepository.getLocationsById(id); }
}
