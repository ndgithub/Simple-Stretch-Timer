package com.example.nicky.simplestretchtimer.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import timber.log.Timber;

/**
 * Created by Nicky on 7/28/17.
 */

//TODO: Complete Provider

public class StretchProvider extends ContentProvider {
    private SQLiteOpenHelper dbHelper;
    private static final int STRETCHES = 0;
    private static final int STRETCHES_ID = 1;



    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StretchDbContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, StretchDbContract.PATH_STRETCHES, STRETCHES);
        matcher.addURI(authority, StretchDbContract.PATH_STRETCHES + "/#", STRETCHES_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new StretchDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case STRETCHES:
                cursor = database.query(StretchDbContract.Stretches.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case STRETCHES_ID:
                selection = StretchDbContract.Stretches._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(StretchDbContract.Stretches.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long id = database.insert(StretchDbContract.Stretches.TABLE_NAME, null, values);
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STRETCHES:
                if (id == -1) {
                    //TODO: Replace with Timber
                    Timber.v("Failed to insert row for " + uri);
                    Toast.makeText(getContext(),"Woops! Couldn't add stretch",Toast.LENGTH_SHORT).show();
                    return null;
                }
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STRETCHES:
                return database.delete(StretchDbContract.Stretches.TABLE_NAME, selection, selectionArgs);
            case STRETCHES_ID:
                selection = StretchDbContract.Stretches._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return database.delete(StretchDbContract.Stretches.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        switch (match) {
            case STRETCHES:
                return database.update(StretchDbContract.Stretches.TABLE_NAME, values, selection, selectionArgs);
            case STRETCHES_ID:
                selection = StretchDbContract.Stretches._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return database.update(StretchDbContract.Stretches.TABLE_NAME, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
}
