package com.fiaxco.lno0x0c.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.fiaxco.lno0x0c.roomstuff.Profile;
import com.fiaxco.lno0x0c.roomstuff.ProfileDao;
import com.fiaxco.lno0x0c.roomstuff.ProfileRoomDatabase;

import java.util.List;

public class LnoRepository {

    // Repository for Profile DB
    public static class LnoDBRepository {

        // DB CRUD Methods
        private ProfileDao mProfileDao;

        // List of Profiles in DB
        private LiveData<List<Profile>> mAllProfiles;


        // Constructor
        public LnoDBRepository(Application application) {

            // Room DB object
            ProfileRoomDatabase db = ProfileRoomDatabase.getDatabase(application);

            mProfileDao = db.profileDao();
            mAllProfiles = mProfileDao.getAllProfiles();
        }

        // method to fetch list of all profiles in DB
        public LiveData<List<Profile>> getAllProfiles() {
            return mAllProfiles;
        }

        // method to insert a profile into DB
        public void insert(final Profile profile) {
            ProfileRoomDatabase.databaseWriteExecutor
                    .execute(() -> mProfileDao.insert(profile));
        }

        // method to delete a profile in DB
        public void delete(final Profile profile) {
            ProfileRoomDatabase.databaseWriteExecutor
                    .execute(() -> mProfileDao.deleteProfile(profile));
        }

        // method to delete all the profiles in DB
        public void deleteAll() {
            ProfileRoomDatabase.databaseWriteExecutor
                    .execute(() -> mProfileDao.deleteAllProfiles());
        }
    }

}
