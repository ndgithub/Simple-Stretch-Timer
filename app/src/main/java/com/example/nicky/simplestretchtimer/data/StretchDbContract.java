package com.example.nicky.simplestretchtimer.data;

import android.net.Uri;

/**
 * Created by Nicky on 7/28/17.
 */

public final class StretchDbContract {

    //Private constructor so can't be instantiated
    private StretchDbContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.nicky.simplestretchtimer.stretches";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_STRETCHES = ".";


    public static class Stretches {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STRETCHES);

        /**
         * Matches: /items/
         */
        public static Uri buildDirUri() {
            return BASE_CONTENT_URI.buildUpon().appendPath("items").build();
        }

        /**
         * Matches: /items/[_id]/
         */
        public static Uri buildItemUri(long _id) {
            return BASE_CONTENT_URI.buildUpon().appendPath("items").appendPath(Long.toString(_id)).build();
        }

        /**
         * Read item ID item detail URI.
         */
        public static long getItemId(Uri itemUri) {
            return Long.parseLong(itemUri.getPathSegments().get(1));
        }

        public static final String TABLE_NAME = "stretches";
        public static final String _ID = "_id";
        public static final String NAME = "stretch_name";
        public static final String TIME = "seconds";
    }

}
