package com.example.nicky.timerpractice;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service onStartCommand", Toast.LENGTH_SHORT).show();
        Log.v("*** - Service ", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void play() {
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

    public void pause() {
        Log.v("*** - Service", "PAUSE");
        cdt.cancel();
        isRunning = false;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("*** - Service ", "onCreate");
        localBroadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("*** - Service ", "onDestroy");

    }
    private void broadcastTick(long milsUntilFinished) {
        Intent tickIntent = new Intent(TIMER_SERVICE_ONTICK_KEY);
        tickIntent.putExtra(MILS_UNTIL_FINISHED_KEY,Math.ceil(milsUntilFinished / 1000.));
        localBroadcaster.sendBroadcast(tickIntent);
    }

    public class TimerBinder extends Binder {

        TimerService getService() {
            return TimerService.this;
        }

    }


}


// TODO: dont' create new intent on every tick .