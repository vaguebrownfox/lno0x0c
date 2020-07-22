package com.fiaxco.lno0x0c;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fiaxco.lno0x0c.lnoviewmodel.ProfileListAdapter;
import com.fiaxco.lno0x0c.lnoviewmodel.ProfileViewModel;
import com.fiaxco.lno0x0c.roomstuff.Profile;
import com.fiaxco.lno0x0c.roomstuff.ProfileContract.ProfileEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class CatalogActivity extends AppCompatActivity
        implements ProfileListAdapter.OnProfileListener {

    private ProfileViewModel mProfileViewModel;
    private List<Profile> mProfileList;

    public static final String EDITOR_ACTIVITY_UPDATE_PROFILE_VALUE_EXTRA = "com.fiaxco.lno0x0c.CatalogActivity.VALUE";
    private static final int EDITOR_ACTIVITY_ADD_PROFILE_REQUEST_CODE = 3000;
    private static final int EDITOR_ACTIVITY_UPDATE_PROFILE_REQUEST_CODE = 3001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // View Model
        mProfileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);


        // Recycler View Stuff
        // Get adapter
        final ProfileListAdapter recyclerViewAdapter =
                new ProfileListAdapter(this, this);
        // Recycler view helper method
        setupRecyclerView(recyclerViewAdapter);


        // Add observer for live data returned by view model to observer
        mProfileViewModel.getAllProfiles().observe(this, profiles -> {
            mProfileList = profiles;
            if (mProfileList != null) {
                TextView emptyListView = findViewById(R.id.empty_list_text_view);
                if (mProfileList.size() < 1) {
                    emptyListView.setVisibility(View.VISIBLE);
                } else
                    emptyListView.setVisibility(View.INVISIBLE);
            }
            recyclerViewAdapter.setProfile(mProfileList);
        });


        // FAB - editor activity : Add profile
        FloatingActionButton editorActivityStart =
                findViewById(R.id.floatingActionButton_catalog_add_profile);
        editorActivityStart.setOnClickListener(v -> {
            Intent editorActivityIntent =
                    new Intent(CatalogActivity.this, EditorActivity.class);
            startActivityForResult(editorActivityIntent, EDITOR_ACTIVITY_ADD_PROFILE_REQUEST_CODE);
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            assert data != null;
            ContentValues values = data.getParcelableExtra(EditorActivity.EXTRA_VALUE_REPLY);
            assert values != null;
            Profile profile = createProfileFromValues(values);

            switch (requestCode) {

                case EDITOR_ACTIVITY_ADD_PROFILE_REQUEST_CODE:
                    mProfileViewModel.insert(profile);
                    makeToast("Profile added");
                    break;

                case EDITOR_ACTIVITY_UPDATE_PROFILE_REQUEST_CODE:
                    profile.mId = values.getAsInteger(ProfileEntry._ID);
                    mProfileViewModel.insert(profile);
                    makeToast("Profile updated");
                    break;
            }
        } else
            makeToast("Not saved");
    }



    /*-------------- Menu stuff ---------------*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override    // On clicking menu items
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_delete_all_entries:
                mProfileViewModel.deleteAll();
                makeToast("All profiles deleted");
                return true;

            case R.id.action_dummy_profile:
                makeToast("Insert test profile");
                addTestProfile();
                return true;

            case R.id.action_info:
                makeToast("Info");
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    /*-------------- /Menu stuff ---------------*/






    /*-------------- helper methods ---------------*/


    // setup Recycler view
    private void setupRecyclerView(ProfileListAdapter recyclerViewAdapter) {

        RecyclerView profileListRecyclerView = findViewById(R.id.main_profile_list);

        // Delete on swipe left
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Profile profileToDelete = recyclerViewAdapter.getProfileAtPosition(position);
                        mProfileViewModel.delete(profileToDelete);
                        makeToast(profileToDelete.mName + "'s profile deleted");
                    }
                }
        );

        // Divider line
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);

        // setup
        profileListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        profileListRecyclerView.setAdapter(recyclerViewAdapter);
        profileListRecyclerView.addItemDecoration(dividerItemDecoration);
        helper.attachToRecyclerView(profileListRecyclerView);

    }

    @Override // start editor activity from list item
    public void onProfileClick(int position) {
        Profile profile = mProfileList.get(position);
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
        intent.putExtra(EDITOR_ACTIVITY_UPDATE_PROFILE_VALUE_EXTRA, profile.getProfileValues());
        startActivityForResult(intent, EDITOR_ACTIVITY_UPDATE_PROFILE_REQUEST_CODE);
    }


    // menu action test profile
    private void addTestProfile() {
        Profile profile = new Profile("Han Solo", 48, 1, 182, 80);
        mProfileViewModel.insert(profile);
    }

    // activity result ok create profile
    public static Profile createProfileFromValues(ContentValues values) {

        String name = values.getAsString(ProfileEntry.NAME);
        Integer age = values.getAsInteger(ProfileEntry.AGE);
        Integer gender = values.getAsInteger(ProfileEntry.GENDER);
        Integer height = values.getAsInteger(ProfileEntry.HEIGHT);
        Integer weight = values.getAsInteger(ProfileEntry.WEIGHT);

        return new Profile(name, age, gender, height, weight);
    }

    // Toast in catalog context
    private void makeToast(String msg) {
        Toast.makeText(CatalogActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    /*-------------- /helper methods ---------------*/
}