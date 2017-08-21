package com.example.nicky.simplestretchtimer.TimerActivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.nicky.simplestretchtimer.R;

import butterknife.BindView;

/**
 * Created by Nicky on 8/19/17.
 */


public class DisplayFragment extends Fragment {
    @BindView(R.id.button_play_pause)
    Button mPlayButton;
    @BindView(R.id.button_reset)
    Button mResetButton;
    @BindView(R.id.time_remaining) TextView mTimeRemaining;

    ControlsListeners mCallback;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mCallback.onPlayButton();
            }
        });

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();

            }
        });
        return inflater.inflate(R.layout.display_frag, container, false);


    }

    public void updateTime(String secsRemaining) {
        mTimeRemaining.setText(secsRemaining);
    }

    // TODO: Make these part of interface to activity
    public void play() {
        mPlayButton.setText("PAUSE");
        mTimerService.play();

    }



    public void reset() {
        mPlayButton.setText("Play");
        mTimerService.reset();
    }


    // Container Activity must implement this interface
    public interface ControlsListeners {
        public void onPlayButton();
        public void onReset();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ControlsListeners) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}
