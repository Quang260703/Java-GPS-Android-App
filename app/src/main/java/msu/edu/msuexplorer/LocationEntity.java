package msu.edu.msuexplorer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * A class representing locations in the database
 */
@Entity(tableName = "Locations")
public class LocationEntity implements Parcelable {

    // The id in the database for the location
    @PrimaryKey
    @NonNull
    int id;

    // The name of the location
    @NonNull
    String locationName;

    // The latitude of the location
    @NonNull
    double latitude;

    // The longitude of the location
    @NonNull
    double longitude;

    // The location of the image to be used
    @NonNull
    String resourceId;

    // Constructor
    public LocationEntity(int id, String locationName, double latitude, double longitude, String resourceId) {
        this.id = id;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.resourceId = resourceId;
    }

    public int GetId() {
        return id;
    }

    public void SetId(int id) {
        this.id = id;
    }

    public double GetLatitude() {
        return this.latitude;
    }

    public void SetLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double GetLongitude() {
        return this.longitude;
    }

    public void SetLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String GetResourceId() { return this.resourceId; }

    public void SetResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    protected LocationEntity(Parcel in) {
        id = in.readInt();
        locationName = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        resourceId = in.readString();
    }

    public static final Creator<LocationEntity> CREATOR = new Creator<LocationEntity>() {
        @Override
        public LocationEntity createFromParcel(Parcel in) {
            return new LocationEntity(in);
        }

        @Override
        public LocationEntity[] newArray(int size) {
            return new LocationEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(locationName);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(resourceId);
    }
}
