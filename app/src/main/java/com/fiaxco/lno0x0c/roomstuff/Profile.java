package com.fiaxco.lno0x0c.roomstuff;

import android.content.ContentValues;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fiaxco.lno0x0c.roomstuff.ProfileContract.ProfileEntry;

@Entity(tableName = ProfileEntry.TABLE_NAME)
public class Profile {

    // DB Table columns
    // 1
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ProfileEntry._ID)
    public Integer mId;

    // 2
    @ColumnInfo(name = ProfileEntry.NAME)
    public String mName;

    // 3
    @ColumnInfo(name = ProfileEntry.AGE)
    public Integer mAge;

    // 4
    @ColumnInfo(name = ProfileEntry.GENDER)
    public Integer mGender;

    // 5
    @ColumnInfo(name = ProfileEntry.HEIGHT)
    public Integer mHeight;

    // 6
    @ColumnInfo(name = ProfileEntry.WEIGHT)
    public Integer mWeight;


    // Constructor - everything except ID
    public Profile(String name, Integer age, Integer gender, Integer height, Integer weight) {
        this.mName = name;
        this.mAge = age;
        this.mGender = gender;
        this.mHeight = height;
        this.mWeight = weight;
    }


    // Method to get a Profile object value as ContentValues object
    public ContentValues getProfileValues() {

        ContentValues values = new ContentValues();
        values.put(ProfileEntry._ID, this.mId);
        values.put(ProfileEntry.NAME, this.mName);
        values.put(ProfileEntry.AGE, this.mAge);
        values.put(ProfileEntry.GENDER, this.mGender);
        values.put(ProfileEntry.HEIGHT, this.mHeight);
        values.put(ProfileEntry.WEIGHT, this.mWeight);

        return values;
    }

}
