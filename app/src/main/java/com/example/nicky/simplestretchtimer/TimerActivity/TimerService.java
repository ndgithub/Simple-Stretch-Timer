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

import java.util.ArrayList;

/**
 * Created by Nicky on 8/13/17.
 */

public class TimerService extends Service {
    private final IBinder mBinder = new TimerBinder();
    LocalBroadcastManager localBroadcaster;
    static final public String TIMER_SERVICE_ONTICK_KEY = "com.example.nicky.timerpractice.timerservice.TIMEUPDATE";
    static final public String MILS_UNTIL_FINISHED_KEY = "mils til fin";
    public boolean isRunning;
    public boolean isForeground;

    static ArrayList<Integer> timesArray;
    private int timerPos;
    public static long timeElapsed;
    public long startingTime;

    MyCountdownTimer countDownTimer;
    private final int TICK_INTERVAL = 1000;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("*** - Service ", "onCreate");
        localBroadcaster = LocalBroadcastManager.getInstance(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service onStartCommand", Toast.LENGTH_SHORT).show();
        Log.v("*** - Service ", "onStartCommand");
        timesArray = new ArrayList<>();
        timesArray.add(3);
        timesArray.add(5);
        timesArray.add(7);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private void startTimer(long countdownTime) {
        final int TICK_INTERVAL = 1000;
        long leftover = countdownTime % TICK_INTERVAL;
        final Handler handler = new Handler();
        startingTime = SystemClock.elapsedRealtime();
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                countDownTimer.start();
            }
        }, leftover);

    }

    public void play() {
        startTimer(returnCountdownTime());
        isRunning = true;
        Log.v("***", "PLAY");
    }


    public void pause() {
        timeElapsed = (SystemClock.elapsedRealtime() - startingTime) + timeElapsed;
        countDownTimer.cancel();
        isRunning = false;
        Log.v("***", "PAUSE");
    }

    public void reset() {
        timerPos = 0;
        timeElapsed = 0;
        pause();
    }

    private long returnCountdownTime() {
        return (timesArray.get(timerPos) * 1000) - timeElapsed;
    }

    private void timerFinished() {
        timerPos++;
        if (isStretchesRemaining()) {
            timeElapsed = 0;
            startTimer(returnCountdownTime());
            Toast.makeText(getApplicationContext(), "Ding", Toast.LENGTH_SHORT).show();
        } else {
            reset();
            Toast.makeText(getApplicationContext(), "Ding, Ding Ding!!!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isStretchesRemaining() {
        return timerPos < timesArray.size();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("*** - Service ", "onDestroy");
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

    }

    private void broadcastTick(long milsUntilFinished) {
        Intent tickIntent = new Intent(TIMER_SERVICE_ONTICK_KEY);
        tickIntent.putExtra(MILS_UNTIL_FINISHED_KEY, Math.ceil(milsUntilFinished / 1000.));
        localBroadcaster.sendBroadcast(tickIntent);
    }

    public class TimerBinder extends Binder {

        TimerService getService() {
            return TimerService.this;
        }

    }

public void onActivityResume() {

}



}


// TODO: dont' create new intent on every tick .