package com.example.nicky.simplestretchtimer.data;

import android.support.annotation.Nullable;

/**
 * Created by Nicky on 8/4/17.
 */

public class Stretch {
    private String mName;
    private int mTime;
    private String mAddedPos;

    public Stretch(@Nullable String name, int time, String addedPos) {
        this.mName = name;
        this.mTime = time;
        this.mAddedPos = addedPos;
    }

    public String getName() {
        return mName;
    }

    public int getTime() {
        return mTime;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setTime(int time) {
        this.mTime = time;
    }

    public String getAddedPos() {
        return mAddedPos;
    }

}
