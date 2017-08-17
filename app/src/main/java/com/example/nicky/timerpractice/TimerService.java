package com.example.nicky.timerpractice;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
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
    LocalBroadcastManager localBroadcaster;
    static final public String TIMER_SERVICE_ONTICK_KEY = "com.example.nicky.timerpractice.timerservice.TIMEUPDATE";
    static final public String MILS_UNTIL_FINISHED_KEY = "mils til fin";
    MyCountdownTimer cdt;
    private boolean isRunning;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("*** - Service ", "onDestroy");

    }

    public void play() {

        isRunning = true;

        Log.v("***", "PLAY");
        cdt = new MyCountdownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                broadcastTick(millisUntilFinished);
                Log.v("***", "millisUntilFinished: " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                Log.v("***", "onFinish");
                Toast.makeText(getApplicationContext(), "finished!", Toast.LENGTH_SHORT).show();
                stopService(new Intent(getApplicationContext(), TimerService.class));
            }
        };
        cdt.start();
    }

    public void pause() {
        Log.v("***", "PAUSE");
        cdt.cancel();
        isRunning = false;
    }

    private void broadcastTick(long milsUntilFinished) {
        Intent tickIntent = new Intent(TIMER_SERVICE_ONTICK_KEY);
        tickIntent.putExtra(MILS_UNTIL_FINISHED_KEY,Math.ceil(milsUntilFinished / 1000.));
        localBroadcaster.sendBroadcast(tickIntent);
    }




}



// TODO: dont' create new intent on every tick .