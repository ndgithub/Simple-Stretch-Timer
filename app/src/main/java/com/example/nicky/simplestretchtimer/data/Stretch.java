package com.example.nicky.simplestretchtimer.data;

import android.support.annotation.Nullable;

/**
 * Created by Nicky on 8/4/17.
 */

public class Stretch {
    private String mName;
    private int mTime;
    private int mPos;
    private int mStretchType;
    private int mID;

    public static final int STRETCH = 0;
    public static final int BREAK = 1;




    public Stretch(@Nullable String name, int time, int addedPos, int id) {
        this.mName = name;
        this.mTime = time;

        mStretchType = (addedPos % 2 == 0 ? STRETCH : BREAK);
        this.mID = id;
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

    public int getId() {
        return mID;
    }

    public int getPosition() {
        return mPos;
    }

    public int getStretchType() {
        return this.mStretchType;
    }


}
