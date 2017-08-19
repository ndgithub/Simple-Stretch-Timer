package com.example.nicky.simplestretchtimer.TimerActivity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.data.Stretch;

import java.util.ArrayList;

/**
 * Created by Nicky on 8/18/17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.StretchHolder> {
    ArrayList<Stretch> mStretches;

    public RecyclerAdapter(ArrayList<Stretch> stretches) {
        mStretches = stretches;
    }


    @Override
    public StretchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_list_item, parent, false);
        return new StretchHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(StretchHolder holder, int position) {
        Stretch stretch = mStretches.get(position);
        holder.bindStretch(stretch);
    }

    @Override
    public int getItemCount() {
        return mStretches.size();
    }

    public static class StretchHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mStretchName;
        private TextView mStretchTime;

        public StretchHolder(View view) {
            super(view);
            mStretchName = (TextView) view.findViewById(R.id.stretch_name);
            mStretchTime = (TextView) view.findViewById(R.id.stretch_time);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //TODO: open new activity or edit screen
        }

        public void bindStretch(Stretch stretch) {
            mStretchName.setText(stretch.getName());
            // TODO: make this string
            mStretchTime.setText(stretch.getTime() + " ");
        }


    }
}
