package com.example.nicky.simplestretchtimer.TimerActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.data.Stretch;
import com.example.nicky.simplestretchtimer.data.StretchDbContract;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements DisplayFragment.ControlsListeners {

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    private final int NOTIFICATION_ID = 1;

    private TextView mTextView;


    private BroadcastReceiver mTickReceiver;
    private TimerService mTimerService;
    boolean isBound;
    private RemoteViews mRemoteView;
    private DisplayFragment displayFragment;
    private StretchesFragment stretchesFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("***", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        final DisplayFragment displayFragment = new DisplayFragment();
        StretchesFragment stretchesFragment = new StretchesFragment();
        fragmentTransaction.add(R.id.display_fragment, displayFragment);
        fragmentTransaction.add(R.id.stretches_fragment,stretchesFragment);
        fragmentTransaction.commit();



        mTextView = (TextView) findViewById(R.id.test_view1);

        mRemoteView = new RemoteViews(getPackageName(), R.layout.notification);


        buildnotification();
        mTickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String secsRemaining = String.valueOf(intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1));
                displayFragment.updateTime(String.valueOf(secsRemaining));

                if (mTimerService.isForeground) {
                    mRemoteView.setTextViewText(R.id.remote_text, secsRemaining);
                    mNotificationManager.notify(1, mBuilder.build());
                }
                Log.v("*** - Receiver", "onRecieve");
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(mTickReceiver, new IntentFilter(TimerService.TIMER_SERVICE_ONTICK_KEY));
        startService(new Intent(this, TimerService.class));
        bindService(new Intent(this, TimerService.class), serviceConnection, BIND_ABOVE_CLIENT);
        Log.v("***", "onStart");
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.v("***", "onResume");

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
            mTimerService = timerBinder.getService();
            isBound = true;
            mTimerService.stopForeground(true);
            mTimerService.isForeground = false;
            Log.v("*** - MainActivity", "Service is bound");
            Log.v("***", mTimerService.toString());


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
        if (mTimerService.isRunning) {
            mTimerService.startForeground(NOTIFICATION_ID, mBuilder.build());
            mTimerService.isForeground = true;

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("***", "onDestroy");

    }


    private void buildnotification() {
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_grade_black_18dp)
                        .setContent(mRemoteView);
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }


    @Override
    public void onPlayButton(int position) {
        if (mTimerService.isRunning) {
            pause();
        }

        mTimerService.play();
    }

    public void pause() {
        mPlayButton.setText("Play");
        mTimerService.pause();
    }
}

//******Eventually stuff
//Unregister broadcast recievers




