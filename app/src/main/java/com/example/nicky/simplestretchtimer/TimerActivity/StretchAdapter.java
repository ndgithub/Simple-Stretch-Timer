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
import com.squareup.haha.perflib.Main;

import java.util.ArrayList;

/**
 * Created by Nicky on 8/18/17.
 */

public class StretchAdapter extends RecyclerView.Adapter<StretchAdapter.StretchHolder> {
    ArrayList<Stretch> mStretches;

    private int mTimerPos;
    private Context mContext;
    private Activity mActivity;

    public StretchAdapter(ArrayList<Stretch> stretches, int timerPos, Context c) {
        mStretches = stretches;
        mTimerPos = timerPos;
        mContext = c;
    }

    @Override
    public StretchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_list_item, parent, false);
        return new StretchHolder(inflatedView);
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);

    }


    @Override
    public void onBindViewHolder(StretchHolder holder, int position) {
        mTimerPos = ((MainActivity) mContext).getTimerPos();

        Stretch stretch = mStretches.get(position);
        holder.mStretchName.setText(stretch.getName());
        holder.mStretchTime.setText(stretch.getTime() + " Actual Position: " + stretch.getAddedPos()
                + "  Recycler Position: " + position);
        if (position == mTimerPos) {
            holder.mContainerView.setBackgroundColor(0xffcccccc);
        } else {
            holder.mContainerView.setBackgroundColor(0xffccffcc);
        }


            Log.v("***Adapter", "position: " + position);

    }

    @Override
    public int getItemCount() {
        //Adding one to size so last position can be footer (Add Stretch)
        return mStretches.size();
    }


    public class StretchHolder extends RecyclerView.ViewHolder {

        public TextView mStretchName;
        public TextView mStretchTime;
        public LinearLayout mContainerView;

        public StretchHolder(View view) {
            super(view);
            mStretchName = (TextView) view.findViewById(R.id.stretch_name);
            mStretchTime = (TextView) view.findViewById(R.id.stretch_time);
            mContainerView = (LinearLayout) view.findViewById(R.id.list_item_container);
        }

    }


}



