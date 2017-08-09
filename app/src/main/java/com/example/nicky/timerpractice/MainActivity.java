package com.example.nicky.timerpractice;

import android.app.NotificationManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    private final int NOTIFICATION_ID = 1;
    static ArrayList<Integer> timesArray;
    static int timerPos;
    public static int timeElapsed;
    TextView textView;
    boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.test_view1);
        final Button playButton = (Button) findViewById(R.id.button_play_pause);

        isRunning = false;
        timerPos = 0;
        timeElapsed = 0;

        timesArray = new ArrayList<>();
        timesArray.add(3);
        timesArray.add(5);
        timesArray.add(7);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    playButton.setText("play");
                    isRunning = false;
                } else {
                    playButton.setText("pause");
                    timerFunction(timerPos, 0);
                    isRunning = true;
                }

            }
        });
    }


    public void timerFunction(final int timerPos, final int timeElapsed) {

        int countdownTime = (timesArray.get(timerPos) * 1000) - timeElapsed;

        CountDownTimer countDownTimer = new CountDownTimer(countdownTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textView.setText("seconds remaining: " + millisUntilFinished / 1000);

            }

            @Override
            public void onFinish() {
                Log.v("***", timesArray.size() + " ");
                if (timesArray.size() > 0) {
                    Toast.makeText(getApplicationContext(), "Ding", Toast.LENGTH_SHORT).show();
                    timerFunction(timerPos, 0);
                } else {
                    Toast.makeText(getApplicationContext(), "Ding, Ding Ding!!!", Toast.LENGTH_SHORT).show();
                }

            }
        };

        countDownTimer.start();
    }


}



