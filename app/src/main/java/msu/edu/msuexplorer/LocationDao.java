package msu.edu.msuexplorer;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LocationDao {
    @Query("SELECT * FROM Locations ORDER BY id ASC")
    List<LocationEntity> getAllLocations();

    @Query("SELECT * FROM Locations WHERE id=:x")
     LocationEntity getLocationById(int x);
}
