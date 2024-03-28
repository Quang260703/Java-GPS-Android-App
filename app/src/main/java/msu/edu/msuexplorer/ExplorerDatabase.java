package msu.edu.msuexplorer;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class for instantiating and connecting to the app's database
 * Followed this guide:
 * https://developer.android.com/codelabs/android-room-with-a-view#7
 */
@Database(entities = LocationEntity.class, version = 1, exportSchema = false)
public abstract class ExplorerDatabase extends RoomDatabase {
    // A reference to the database's DAO
    public abstract LocationDao locationDao();

    // The instance of the database
    private static volatile ExplorerDatabase INSTANCE;

    // An executor for writing to the database
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Returns a reference to the database's current instance
    static ExplorerDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ExplorerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ExplorerDatabase.class, "explorer_database").createFromAsset("ExplorerDB.db").allowMainThreadQueries().build();

                }
            }
        }

        return INSTANCE;
    }
}
