package com.example.nicky.simplestretchtimer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;

/**
 * Created by Nicky on 7/28/17.
 */

public class StretchDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "stretches";
    private static final int DATABASE_VERSION = 1;

    public StretchDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {


        String CREATE_TABLE_QUERY = "CREATE TABLE " + StretchDbContract.Stretches.TABLE_NAME + "(" +
                StretchDbContract.Stretches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                StretchDbContract.Stretches.NAME + " TEXT," +
                StretchDbContract.Stretches.TIME + " INT NOT NULL)";


        String CREATE_FIRST_ENTRY_STATEMENT = "INSERT INTO " +
                StretchDbContract.Stretches.TABLE_NAME + " (" +
                StretchDbContract.Stretches.NAME + "," +
                StretchDbContract.Stretches.TIME +
                ") VALUES ('First Stretch',5)";

        Timber.v(CREATE_TABLE_QUERY);
        Timber.v(CREATE_FIRST_ENTRY_STATEMENT);

        db.execSQL(CREATE_TABLE_QUERY);
        db.execSQL(CREATE_FIRST_ENTRY_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
//TODO: Review the android MVP pattern.
}
