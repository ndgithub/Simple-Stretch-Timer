package com.example.nicky.simplestretchtimer.TimerActivity;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.nicky.simplestretchtimer.data.Stretch;

import java.util.ArrayList;

import timber.log.Timber;

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


    private boolean mRunning;
    private boolean mForeground;


    static ArrayList<Integer> timesArray;
    public int mTimerPos;
    private static long mTimeElapsed;
    public long mStartingTime;

    MyCountdownTimer countDownTimer;
    private final int TICK_INTERVAL = 1000;

    public boolean isRunning() {
        return mRunning;
    }

    public boolean isForeground() {
        return mForeground;
    }

    public void setForegroundState(boolean foreground) {
        mForeground = foreground;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("*** - Service ", "onCreate");
        localBroadcaster = LocalBroadcastManager.getInstance(this);
        mTimerPos = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service onStartCommand", Toast.LENGTH_SHORT).show();
        Log.v("*** - Service ", "onStartCommand");
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
        Log.v("***", "PLAY");
    }


    public void pause() {
        mTimeElapsed = (SystemClock.elapsedRealtime() - mStartingTime) + mTimeElapsed;
        stopRunning();
    }

    public void reset() {
        if (mRunning) {
            stopRunning();
        }
        goToStretchPosition(0);
    }


    private void startTimer(long countdownTime) {
        final int TICK_INTERVAL = 1000;
        long leftover = countdownTime % TICK_INTERVAL;
        mStartingTime = SystemClock.elapsedRealtime();
        countDownTimer = new MyCountdownTimer(countdownTime - leftover, TICK_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                broadcastTick(millisUntilFinished);
                Log.v("***", "millisUntilFinished: " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerFinished();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                countDownTimer.start();
            }
        }, leftover);
        mRunning = true;
    }


    private void stopRunning() {
        countDownTimer.cancel();
        mRunning = false;

    }

    private long returnCountdownTime() {
      return (timesArray.get(mTimerPos) * 1000) - mTimeElapsed;
    }

    private void timerFinished() {

        if (isStretchesRemaining()) {
            Timber.v("Timer Position: " + mTimerPos);
            goToStretchPosition(mTimerPos + 1);
            startTimer(returnCountdownTime());
            Toast.makeText(getApplicationContext(), "Ding", Toast.LENGTH_SHORT).show();
        } else {
            reset();
            Toast.makeText(getApplicationContext(), "Ding, Ding Ding!!!", Toast.LENGTH_SHORT).show();
            stopSelf();

        }
    }

    private boolean isStretchesRemaining() {
        return mTimerPos < timesArray.size() - 1;
    }

    private void goToStretchPosition(int timerPosition) {
        Log.v("***", mTimerPos + "    goToStretchPosition ");
        mTimerPos = timerPosition;
        mTimeElapsed = 0;
        broadcastPositionChange(mTimerPos);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("*** - Service ", "onDestroy");
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

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
        Log.v("!!***", newPosition + "    Broadcast (Position Change)");
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


}


// TODO: dont' create new intent on every tick .
// TODO: program on interfaces check .