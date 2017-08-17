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

    private MyCountdownTimer cdt;
    private TextView textView;
    boolean isRunning;
    Button playButton;
    Button resetButton;
    BroadcastReceiver tickReceiver;

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
                // TODO: 8/17/17 grab time from intent
                // TODO: update ui
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("***", "onStart");
        LocalBroadcastManager.getInstance(this).registerReceiver(tickReceiver, new IntentFilter(TimerService.TIMER_SERVICE_ONTICK_KEY));
startService(new Intent(this,TimerService.class));

        // TODO: 8/17/17 get rid of notification


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("***", "onResume");
    }


    public void play() {
        playButton.setText("PAUSE");
        // TODO: Play in Service
    }

    public void pause() {
        playButton.setText("Play");
        // TODO: Pause in Service
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("***", "onPause");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("***", "onSaveInstanceState");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("***", "onStop");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tickReceiver);


        // TODO: 8/17/17 Create notification 
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("***", "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v("***", "onConfigurationChanged");
    }


}

//******Eventually stuff


//TODO: Combine everything with other project.
// startService() and binding together will prevent from being destroyed



