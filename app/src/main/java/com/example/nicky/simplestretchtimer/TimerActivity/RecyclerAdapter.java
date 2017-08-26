package com.example.nicky.simplestretchtimer.TimerActivity;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.data.Stretch;

import java.util.ArrayList;

/**
 * Created by Nicky on 8/18/17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Stretch> mStretches;

    private TextView mStretchName;
    private TextView mStretchTime;
    private LinearLayout mContainerView;
    private int mTimerPos;


    public RecyclerAdapter(ArrayList<Stretch> stretches, int timerPos) {
        mStretches = stretches;
        mTimerPos = timerPos;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_list_item, parent, false);
        return new StretchHolder(inflatedView);
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);

    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Stretch stretch = mStretches.get(position);
        mStretchName.setText(stretch.getName());
        mStretchTime.setText(String.valueOf(stretch.getTime()));
        if (position == mTimerPos) {
            mContainerView.setBackgroundColor(0xffccffcc);
        }

        Log.v("***Adapter", "position: " + position);

    }

    @Override
    public int getItemCount() {
        //Adding one to size so last position can be footer (Add Stretch)
        return mStretches.size();
    }


    public class StretchHolder extends RecyclerView.ViewHolder {

        public StretchHolder(View view) {
            super(view);
            mStretchName = (TextView) view.findViewById(R.id.stretch_name);
            mStretchTime = (TextView) view.findViewById(R.id.stretch_time);
            mContainerView = (LinearLayout) view.findViewById(R.id.list_item_container);
        }

    }


}



