package com.fiaxco.lno0x0c.lnoviewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fiaxco.lno0x0c.repository.LnoRepository;
import com.fiaxco.lno0x0c.roomstuff.Profile;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    // Repository to get all the data into ViewModel
    private LnoRepository.LnoDBRepository mRepository;
    // All Profile List
    private LiveData<List<Profile>> mAllProfiles;

    // Constructor
    public ProfileViewModel(@NonNull Application application) {
        super(application);

        mRepository = new LnoRepository.LnoDBRepository(application);
        mAllProfiles = mRepository.getAllProfiles();
    }

    // Methods to handle data through view model
    public LiveData<List<Profile>> getAllProfiles() {
        return mAllProfiles;
    }

    public void insert(Profile profile) { mRepository.insert(profile); }

    public void delete(Profile profile) {
        mRepository.delete(profile);
    }

    public void deleteAll() {
        mRepository.deleteAll();
    }

}
