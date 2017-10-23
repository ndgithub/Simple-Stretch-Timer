package com.example.nicky.simplestretchtimer;

/**
 * Created by Nicky on 10/18/17.
 */

public class Utils {

    public static String formatTime(int secsRemaining) {
        if (secsRemaining < 60) {
            return secsRemaining + "";
        } else if (secsRemaining % 60 >= 10) {
            return secsRemaining / 60 + ":" + secsRemaining % 60;
        } else {
            return secsRemaining / 60 + ":0" + secsRemaining % 60;
        }
    }

    public static String formatTimeWithText(int secsRemaining) {
        if (secsRemaining < 60) {
            return secsRemaining + "s";
        } else  {
            return secsRemaining / 60 + "m " + secsRemaining % 60 + "s";
        }
    }
}
