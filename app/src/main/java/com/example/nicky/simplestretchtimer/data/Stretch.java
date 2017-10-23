package com.example.nicky.simplestretchtimer.data;

import android.support.annotation.Nullable;

/**
 * Created by Nicky on 8/4/17.
 */

public class Stretch {
    private String mName;
    private int mTime;
    private int mStretchType;
    private Integer mId;


    public Stretch(@Nullable String name, int time, int stretchType,@Nullable Integer dbId) {
        this.mName = name;
        this.mTime = time;
        this.mStretchType = stretchType;
        this.mId = dbId;
    }

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }


    public int getTime() {
        return this.mTime;
    }

    public void setTime(int time) {
        this.mTime = time;
    }

    public int getId() {
        return this.mId;
    }

}
