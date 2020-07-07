package com.fiaxco.lno0x0c;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.fiaxco.lno0x0c.roomstuff.ProfileContract.ProfileEntry;

public class EditorActivity extends AppCompatActivity {

    public static final String
            EXTRA_VALUE_REPLY = "com.fiaxco.lno0x0b.EditorActivity.REPLY",  // reply result to catalog activity
            EDITOR_ACTIVITY_PROFILE_VALUE_EXTRA = "com.fiaxco.lno0x0b.EditorActivity.EXTRA_VALUE"; // send current profile to record activity

    private EditText mEditTextName, mEditTextAge, mEditTextHeight, mEditTextWeight;
    private Spinner mGenderSpinner;
    private int mGender = 0;        // selected gender value
    private ContentValues mProfile; // current selected profile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Initialize Layout Items
        mEditTextName = findViewById(R.id.edit_profile_name);
        mEditTextAge = findViewById(R.id.edit_profile_age);
        mEditTextHeight = findViewById(R.id.edit_profile_height);
        mEditTextWeight = findViewById(R.id.edit_profile_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);
        setupSpinner();

        // If Profile is passed with intent
        Intent editorIntent = getIntent();
        Button nextButton = findViewById(R.id.button_editor_next);
        mProfile = editorIntent
                .getParcelableExtra(CatalogActivity.EDITOR_ACTIVITY_UPDATE_PROFILE_VALUE_EXTRA);
        if (mProfile != null) {
            setTitle("Edit Profile");
            fillForm(); // initialize all fields with passed values

            // Start Record activity
            nextButton.setOnClickListener(v -> {
                Intent recordActivityIntent =
                        new Intent(EditorActivity.this, RecordActivity.class);
                recordActivityIntent.putExtra(EDITOR_ACTIVITY_PROFILE_VALUE_EXTRA, mProfile);
                startActivity(recordActivityIntent);
            });

        } else {
            nextButton.setVisibility(View.INVISIBLE);
        }

    }




    /*-------------- Menu stuff ---------------*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:
                menuActionSave();
                return true;

            case R.id.action_delete:
                makeToast();
                return true;

            case android.R.id.home :
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Menu icon click helper methods
    private void menuActionSave() {
        // setting up reply result intent to CatalogActivity
        Intent replyIntent = new Intent();
        if (checkForEmptyEditText()) {
            setResult(RESULT_CANCELED, replyIntent);
        } else {
            // create a new parcelable then pass it to CatalogActivity`
            ContentValues values = new ContentValues();
            values.put(ProfileEntry.NAME, mEditTextName.getText().toString().trim());
            values.put(ProfileEntry.AGE, Integer.parseInt(mEditTextAge.getText().toString()));
            values.put(ProfileEntry.GENDER, mGender);
            values.put(ProfileEntry.HEIGHT, Integer.parseInt(mEditTextHeight.getText().toString()));
            values.put(ProfileEntry.WEIGHT, Integer.parseInt(mEditTextWeight.getText().toString()));
            if (mProfile != null) {
                // Add id to profile for utilizing replace strategy of insert to update
                values.put(ProfileEntry._ID, mProfile.getAsInteger(ProfileEntry._ID));
            }

            replyIntent.putExtra(EXTRA_VALUE_REPLY, values);
            setResult(RESULT_OK, replyIntent);
        }
        finish();
    }

    /*-------------- /Menu stuff ---------------*/




    /*-------------- helper methods ---------------*/

    // for Editor Update Activity
    private void fillForm() {
        mEditTextName.setText(mProfile.getAsString(ProfileEntry.NAME));
        mEditTextAge.setText(mProfile.getAsString(ProfileEntry.AGE));
        mEditTextHeight.setText(mProfile.getAsString(ProfileEntry.HEIGHT));
        mEditTextWeight.setText(mProfile.getAsString(ProfileEntry.WEIGHT));
        switch (mProfile.getAsInteger(ProfileEntry.GENDER)) {
            case ProfileEntry.GENDER_MALE :
                mGenderSpinner.setSelection(1);
                break;
            case ProfileEntry.GENDER_FEMALE :
                mGenderSpinner.setSelection(2);
                break;
            default:
                mGenderSpinner.setSelection(0);
                break;
        }
    }

    // setup gender spinner
    private void setupSpinner() {
        ArrayAdapter<CharSequence> genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male_cap))) {
                        mGender = ProfileEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female_cap))) {
                        mGender = ProfileEntry.GENDER_FEMALE;
                    } else {
                        mGender = ProfileEntry.GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0;
            }
        });
    }

    // returns true if empty
    private boolean checkForEmptyEditText() {
        return TextUtils.isEmpty(mEditTextName.getText()) ||
                TextUtils.isEmpty(mEditTextAge.getText()) ||
                TextUtils.isEmpty(mEditTextHeight.getText()) ||
                TextUtils.isEmpty(mEditTextWeight.getText());
    }

    // Toast in editor context
    private void makeToast() {
        Toast.makeText(EditorActivity.this, "Go back and swipe left to delete", Toast.LENGTH_SHORT).show();
    }

    /*-------------- /helper methods ---------------*/
}