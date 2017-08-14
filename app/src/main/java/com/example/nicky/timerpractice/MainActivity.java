package com.example.nicky.timerpractice;

import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<Integer> timesArray;
    private int timerPos;
    public static long timeElapsed;
    public long startingTime;
    TextView textView;
    boolean isRunning;
    MyCountdownTimer countDownTimer;
    Button playButton;
    Button resetButton;
    private final int TICK_INTERVAL = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.test_view1);

        playButton = (Button) findViewById(R.id.button_play_pause);
        resetButton = (Button) findViewById(R.id.button_reset);

        timesArray = new ArrayList<>();
        timesArray.add(3);
        timesArray.add(5);
        timesArray.add(7);

        reset();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRunning) {
                    play();
                } else {
                    pause();
                }
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }

    private void play() {
        playButton.setText("pause");

        startTimer(returnCountdownTime());
        isRunning = true;
    }

    private void pause() {
        timeElapsed = (SystemClock.elapsedRealtime() - startingTime) + timeElapsed;
        playButton.setText("play");
        countDownTimer.cancel();
        isRunning = false;
    }

    private void reset() {
        timerPos = 0;
        timeElapsed = 0;
        textView.setText(timesArray.get(0) + " ");

        if (isRunning) {
            countDownTimer.cancel();
            startTimer(returnCountdownTime());
        }

    }

    private void startTimer(long countdownTime) {
        long leftover = countdownTime % TICK_INTERVAL;
        final Handler handler = new Handler();
        startingTime = SystemClock.elapsedRealtime();
        countDownTimer = new MyCountdownTimer(countdownTime - leftover, TICK_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                textView.setText("seconds remaining: " + Math.ceil(millisUntilFinished / 1000.));
                Log.v("***", "millisUntilFinished: " + millisUntilFinished );
            }

            @Override
            public void onFinish() {
                textView.setText("caca");
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
            pause();
            reset();
            Toast.makeText(getApplicationContext(), "Ding, Ding Ding!!!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isStretchesRemaining() {
        return timerPos < timesArray.size();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

//TODO: Handle orientation change





