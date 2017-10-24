package com.example.nicky.simplestretchtimer.timeractivity;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.Utils;
import com.example.nicky.simplestretchtimer.data.Stretch;
import com.example.nicky.simplestretchtimer.data.StretchDbContract;

import java.util.ArrayList;

/**
 * Created by Nicky on 8/18/17.
 */

public class StretchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Stretch> mStretches;

    private int mTimerPos;
    private Context mContext;
    private RecyclerView mRecyclerView;

    private final int STRETCH_VIEW_TYPE = 0;
    private final int BREAK_VIEW_TYPE = 1;

    StretchAdapter(ArrayList<Stretch> stretches, int timerPos, Context c, RecyclerView recyclerView) {
        mStretches = stretches;
        mTimerPos = timerPos;
        mContext = c;
        mRecyclerView = recyclerView;
    }


    public class StretchHolder extends RecyclerView.ViewHolder {

        public TextView mStretchName;
        public TextView mStretchTime;
        public RelativeLayout mContainerView;
        public ImageView mDeleteButton;

        public StretchHolder(View view) {
            super(view);
            mStretchName = view.findViewById(R.id.stretch_name);
            mStretchTime = view.findViewById(R.id.stretch_time);
            mContainerView = view.findViewById(R.id.list_item_container);
            mDeleteButton = view.findViewById(R.id.delete_button);
        }

    }

    public class BreakHolder extends RecyclerView.ViewHolder {
        public BreakHolder(View view) {
            super(view);
        }
    }


    @Override
    public int getItemViewType(int position) {
        return position % 2;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case STRETCH_VIEW_TYPE:
                View inflatedStretchView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycler_list_item, parent, false);

                inflatedStretchView.setOnClickListener(v -> {
                    int position = mRecyclerView.getChildAdapterPosition(v);
                    Stretch stretch = mStretches.get(position);
                    AddStretchFragment dialog = AddStretchFragment.newInstance(stretch.getTime(), stretch.getName(), stretch.getId(), AddStretchFragment.TYPE_EDIT);
                    dialog.show(((MainActivity) mContext).getSupportFragmentManager(), "NoticeDialogFragment");
                });

                return new StretchHolder(inflatedStretchView);
            case BREAK_VIEW_TYPE:
                View inflatedBreakView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.break_list_item, parent, false);
                return new BreakHolder(inflatedBreakView);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mTimerPos = ((MainActivity) mContext).getTimerPos();
        switch (holder.getItemViewType()) {

            case STRETCH_VIEW_TYPE:
                StretchHolder stretchHolder = (StretchHolder) holder;
                Stretch stretch = mStretches.get(position);
                int secs = stretch.getTime();
                stretchHolder.mStretchName.setText(stretch.getName());
                stretchHolder.mStretchTime.setText(Utils.formatTimeWithText(secs));
                if (position == mTimerPos) {
                    stretchHolder.mContainerView.setBackgroundColor(mContext.getResources().getColor(R.color.stretchItemBgCurrent));
                } else {
                    stretchHolder.mContainerView.setBackgroundColor(mContext.getResources().getColor(R.color.stretchItemBg));
                }
                stretchHolder.mDeleteButton.setOnClickListener(v -> {
                    Uri currentPetUri = ContentUris.withAppendedId(StretchDbContract.Stretches.CONTENT_URI, stretch.getId());
                    int rowsDeleted = mContext.getContentResolver().delete(currentPetUri, null, null);
                    ((MainActivity) mContext).onDeleteStretch(position);
                });
                break;
            case BREAK_VIEW_TYPE:
                BreakHolder breakHolder = (BreakHolder) holder;
                break;
        }


    }

    @Override
    public int getItemCount() {
        return mStretches.size();
    }


}



