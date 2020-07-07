package com.fiaxco.lno0x0c.roomstuff;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Profile.class}, version = 1, exportSchema = false)
public abstract class ProfileRoomDatabase  extends RoomDatabase {

    // DB CRUD methods
    public abstract ProfileDao profileDao();

    private static volatile ProfileRoomDatabase INSTANCE;

    // Background thread pool for database operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Return an instance of room database
    public static ProfileRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            // prevent multiple instance creation
            synchronized (ProfileRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ProfileRoomDatabase.class, ProfileContract.ProfileEntry.DATABASE_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
