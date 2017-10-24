package com.example.nicky.simplestretchtimer.timeractivity;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.data.Stretch;

import java.util.ArrayList;


/**
 * Created by Nicky on 8/13/17.
 */

public class TimerService extends Service {

    private final IBinder mBinder = new TimerBinder();
    LocalBroadcastManager localBroadcaster;

    static final public String ONTICK_KEY = "com.example.nicky.timerpractice.timerservice.TIMEUPDATE";
    static final public String MILS_UNTIL_FINISHED_KEY = "milsRemaining";

    static final public String POSITION_CHANGED_KEY = "com.example.nicky.timerpractice.timerservice.POSITIONUPDATE";
    static final public String NEW_POSITION_KEY = "position";

    static final public String STRETCHES_COMPLETE_KEY = "com.example.nicky.timerpractice.timerservice.STRETCHESCOMPLETE";


    private boolean mTicking;
    private boolean mForeground;

    static ArrayList<Integer> timesArray;
    public int mTimerPos;
    private static long mTimeElapsed;
    public long mStartingTime;

    MyCountdownTimer countDownTimer;
    private final int TICK_INTERVAL = 1000;

    public boolean isTicking() {
        return mTicking;
    }

    public boolean isForeground() {
        return mForeground;
    }

    public void setForegroundState(boolean foreground) {
        mForeground = foreground;
    }


    private MediaPlayer mMediaPlayerDing;
    private MediaPlayer mMediaPlayerDingFinish;

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            // Now that the sound file has finished playing, release the media player resources.
            releaseMediaPlayer();
        }
    };

    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mMediaPlayerDing != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mMediaPlayerDing.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mMediaPlayerDing = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcaster = LocalBroadcastManager.getInstance(this);
        mTimerPos = 0;
        mMediaPlayerDing = MediaPlayer.create(getApplicationContext(), R.raw.bell);
        mMediaPlayerDingFinish = MediaPlayer.create(getApplicationContext(), R.raw.ding_finish);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stopForeground(true);
        setForegroundState(false);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public void play() {
        startTimer(returnCountdownTime());
    }


    public void pause() {
        mTimeElapsed = (SystemClock.elapsedRealtime() - mStartingTime) + mTimeElapsed;
        stopTicking();
    }

    public void reset() {
        if (mTicking) {
            stopTicking();
        }
        goToStretchPosition(0);
    }


    public void startTimer(long countdownTime) {
        long leftover = countdownTime % TICK_INTERVAL;
        mStartingTime = SystemClock.elapsedRealtime();
        countDownTimer = new MyCountdownTimer(countdownTime - leftover, TICK_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                broadcastTick(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerFinished();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(() -> countDownTimer.start(), leftover);
        mTicking = true;
    }


    public void stopTicking() {
        countDownTimer.cancel();
        mTicking = false;
    }

    private long returnCountdownTime() {
        return (timesArray.get(mTimerPos) * 1000) - mTimeElapsed;
    }


    public void timerFinished() {

        if (isStretchesRemaining()) {
            goToStretchPosition(mTimerPos + 1);
            startTimer(returnCountdownTime());
            if (mTimerPos % 2 == 1) {
                mMediaPlayerDing.start();
            }
        } else {
            reset();
            mMediaPlayerDingFinish.start();
            broadcastAllStretchesComplete();
        }
    }


    private boolean isStretchesRemaining() {
        return mTimerPos < timesArray.size() - 1;
    }

    private void goToStretchPosition(int timerPosition) {
        mTimerPos = timerPosition;
        mTimeElapsed = 0;
        broadcastPositionChange(mTimerPos);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }


    private void broadcastTick(long milsUntilFinished) {
        Intent tickIntent = new Intent(ONTICK_KEY);
        tickIntent.putExtra(MILS_UNTIL_FINISHED_KEY, Math.ceil(milsUntilFinished / 1000.));
        localBroadcaster.sendBroadcast(tickIntent);

    }

    private void broadcastPositionChange(int newPosition) {
        Intent posIntent = new Intent(POSITION_CHANGED_KEY);
        posIntent.putExtra(NEW_POSITION_KEY, newPosition);
        localBroadcaster.sendBroadcast(posIntent);
    }

    private void broadcastAllStretchesComplete() {
        Intent intent = new Intent(STRETCHES_COMPLETE_KEY);
        localBroadcaster.sendBroadcast(intent);
    }


    public class TimerBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    public void updateStretches(ArrayList<Stretch> stretches) {
        timesArray = new ArrayList<>();
        for (Stretch stretch : stretches) {
            timesArray.add(stretch.getTime());
        }

    }

    public int getTimerPos() {
        return mTimerPos;
    }

    public void adjustTimerPos(int adj) {
        this.mTimerPos = mTimerPos + adj;
    }

}
