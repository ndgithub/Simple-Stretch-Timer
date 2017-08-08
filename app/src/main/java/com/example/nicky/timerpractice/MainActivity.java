package com.example.nicky.timerpractice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
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
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.test_view1);

        timesArray = new ArrayList<>();
        timesArray.add(3);
        timesArray.add(5);
        timesArray.add(7);
        timerFunction(timesArray.get(0));
    }

    public void timerFunction(final Integer sec) {
        textView.setText(sec.toString());
        timesArray.remove(0);

        CountDownTimer countDownTimer = new CountDownTimer(sec * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textView.setText("seconds remaining: " + millisUntilFinished / 1000);

            }

            @Override
            public void onFinish() {
                Log.v("***",timesArray.size() + " ");
                if (timesArray.size() > 0) {
                    Toast.makeText(getApplicationContext(), "Ding", Toast.LENGTH_SHORT).show();
                    timerFunction(timesArray.get(0));
                } else {
                    Toast.makeText(getApplicationContext(), "Ding, Ding Ding!!!", Toast.LENGTH_SHORT).show();
                }

            }
        };

        countDownTimer.start();
    }



}



