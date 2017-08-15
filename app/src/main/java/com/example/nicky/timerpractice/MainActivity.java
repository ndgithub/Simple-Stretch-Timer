package com.example.nicky.timerpractice;

import android.content.Intent;
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
    private final int TICK_INTERVAL = 1000;

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


    }

    private void play() {
        startService(new Intent(getApplicationContext(), TimerService.class));


        isRunning = true;
        playButton.setText("PAUSE");
        Log.v("***", "PLAY");
        cdt = new MyCountdownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textView.setText("seconds remaining: " + Math.ceil(millisUntilFinished / 1000.));
                Log.v("***", "millisUntilFinished: " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                Log.v("***", "onFinish");
                Toast.makeText(MainActivity.this, "finished!", Toast.LENGTH_SHORT).show();
            }
        };
        cdt.start();
    }

    private void pause() {
        playButton.setText("play");
        Log.v("***", "PAUSE");
        cdt.cancel();
        isRunning = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("***", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("***", "onResume");
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
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("***", "onDestroy");
    }
}






