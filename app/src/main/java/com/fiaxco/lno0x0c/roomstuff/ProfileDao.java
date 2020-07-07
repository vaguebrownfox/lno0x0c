package com.fiaxco.lno0x0c.roomstuff;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProfileDao {

    // DB CRUD implementation

    // Create and Update
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Profile profile);

    // Request
    @Query("SELECT * FROM " + ProfileContract.ProfileEntry.TABLE_NAME)
    LiveData<List<Profile>> getAllProfiles();


    // Delete
    // single
    @Delete
    void deleteProfile(Profile profile);

    // all
    @Query("DELETE FROM " + ProfileContract.ProfileEntry.TABLE_NAME)
    void deleteAllProfiles();

}
