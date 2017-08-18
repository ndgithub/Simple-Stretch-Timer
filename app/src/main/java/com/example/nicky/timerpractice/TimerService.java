package com.example.nicky.timerpractice;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Nicky on 8/13/17.
 */

public class TimerService extends Service {
    private final IBinder mBinder = new TimerBinder();
    MyCountdownTimer cdt;
    LocalBroadcastManager localBroadcaster;
    static final public String TIMER_SERVICE_ONTICK_KEY = "com.example.nicky.timerpractice.timerservice.TIMEUPDATE";
    static final public String MILS_UNTIL_FINISHED_KEY = "mils til fin";
    private boolean isRunning;

    static ArrayList<Integer> timesArray;
    private int timerPos;
    public static long timeElapsed;
    public long startingTime;
    TextView textView;

    MyCountdownTimer countDownTimer;
    Button playButton;
    Button resetButton;
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


    public void() {
        isRunning = true;
        Log.v("*** - Service", "PLAY");
        cdt = new MyCountdownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("*** - Service ", "millisUntilFinished: " + millisUntilFinished);
                Log.v("*** - Service", "Secs Rem: " + Math.ceil(millisUntilFinished / 1000.));
                broadcastTick(millisUntilFinished);

            }

            @Override
            public void onFinish() {
                Log.v("***", "onFinish");
                pause();
                Toast.makeText(getApplicationContext(), "finished!", Toast.LENGTH_SHORT).show();

            }
        };
        cdt.start();
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

    private void play() {
        playButton.setText("pause");

        Log.v("***", "PLAY");
        startTimer(returnCountdownTime());
        isRunning = true;
    }


    private void pause() {
        timeElapsed = (SystemClock.elapsedRealtime() - startingTime) + timeElapsed;
        playButton.setText("play");

        Log.v("***", "PAUSE");
        countDownTimer.cancel();
        isRunning = false;
    }

    private void reset() {
        timerPos = 0;
        timeElapsed = 0;
        textView.setText(timesArray.get(0) + " ");
        //TODO:Should reset also pause, (Google timer does)
        if (isRunning) {
            countDownTimer.cancel();
            startTimer(returnCountdownTime());
        }

    }

    private long returnCountdownTime() {
        return (timesArray.get(timerPos) * 1000) - timeElapsed;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("*** - Service ", "onDestroy");

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


}


// TODO: dont' create new intent on every tick .