package com.example.nicky.timerpractice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private TextView textView;
    boolean isRunning;
    Button playButton;
    Button resetButton;
    BroadcastReceiver tickReceiver;
    TimerService timerService;
    boolean isBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v("***", "onCreate");

        textView = (TextView) findViewById(R.id.test_view1);
        playButton = (Button) findViewById(R.id.button_play_pause);
        resetButton = (Button) findViewById(R.id.button_reset);

        playButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                if (!isRunning) {
                    play();
                } else {
                    pause();
                }
            }
        });

        tickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double secsRemaining = intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY,1);
                textView.setText(secsRemaining + " ");
                Log.v("*** - Receiver","onRecieve");
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("***", "onStart");
        LocalBroadcastManager.getInstance(this).registerReceiver(tickReceiver, new IntentFilter(TimerService.TIMER_SERVICE_ONTICK_KEY));

        startService(new Intent(this, TimerService.class));
        bindService(new Intent(this, TimerService.class), serviceConnection, BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("***", "onResume");
    }


    public void play() {
        playButton.setText("PAUSE");
        timerService.play();

    }

    public void pause() {
        playButton.setText("Play");
        timerService.pause();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("***", "onSaveInstanceState");
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder timerBinder = (TimerService.TimerBinder) service;
            timerService = timerBinder.getService();
            isBound = true;
            Log.v("*** - MainActivity", "Service is bound");

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Log.v("*** - MainActivity", "Service is un-bound");
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        Log.v("***", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("***", "onStop");
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(tickReceiver);

        // TODO: 8/17/17 Create notification
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("***", "onDestroy");
    }

}

//******Eventually stuff


//TODO: Combine everything with other project.
// startService() and binding together will prevent from being destroyed
//Service should be destroyed when fully finished



