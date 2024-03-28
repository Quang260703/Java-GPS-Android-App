package msu.edu.msuexplorer;

import android.app.Application;
import android.location.Location;

import androidx.lifecycle.LiveData;

import java.util.List;

class LocationRepository {
    private LocationDao locationDao;

    private List<LocationEntity> allLocations;
    private LocationEntity locationsById;

    LocationRepository(Application application) {
        ExplorerDatabase db = ExplorerDatabase.getDatabase(application);
        locationDao = db.locationDao();
        allLocations = locationDao.getAllLocations();
    }

    // Notifies the observer when the data has changed
    List<LocationEntity> getAllLocations() {
        return allLocations;
    }

    // Get a location by its ID
    LocationEntity getLocationsById(int id) { return locationDao.getLocationById(id); }
}
