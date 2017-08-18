package com.example.nicky.simplestretchtimer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nicky.simplestretchtimer.data.StretchDbContract;


public class MainActivity extends AppCompatActivity {

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    private final int NOTIFICATION_ID = 1;
    private TextView textView;
    Button playButton;
    Button resetButton;
    BroadcastReceiver tickReceiver;
    TimerService timerService;
    boolean isBound;
    TextView textView2;

    RemoteViews remoteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v("***", "onCreate");

        textView = (TextView) findViewById(R.id.test_view1);
        playButton = (Button) findViewById(R.id.button_play_pause);
        resetButton = (Button) findViewById(R.id.button_reset);
        remoteView = new RemoteViews(getPackageName(), R.layout.notification);
        textView2 = (TextView) findViewById(R.id.test_view2);


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerService.isRunning) {
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
        buildnotification();
        tickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double secsRemaining = intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1);
                textView.setText(secsRemaining + " ");

                if (timerService.isForeground) {
                    remoteView.setTextViewText(R.id.remote_text, secsRemaining + " ");
                    // TODO: make secsRemainingString
                    mNotificationManager.notify(1, mBuilder.build());
                }


                Log.v("*** - Receiver", "onRecieve");

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(tickReceiver, new IntentFilter(TimerService.TIMER_SERVICE_ONTICK_KEY));
        startService(new Intent(this, TimerService.class));
        bindService(new Intent(this, TimerService.class), serviceConnection, BIND_ABOVE_CLIENT);
        Log.v("***", "onStart");
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.v("***", "onResume");

        // TODO: make service cllbacks
    }


    public void play() {
        playButton.setText("PAUSE");
        timerService.play();

    }

    public void pause() {
        playButton.setText("Play");
        timerService.pause();
    }

    public void reset() {
        playButton.setText("Play");
        timerService.reset();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.add_entry) {
            addTestEntry();
            return true;
        }

        return super.onOptionsItemSelected(item);

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
            timerService.stopForeground(true);
            timerService.isForeground = false;
            Log.v("*** - MainActivity", "Service is bound");
            Log.v("***", timerService.toString());


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
        if (timerService.isRunning) {
            timerService.startForeground(NOTIFICATION_ID, mBuilder.build());
            timerService.isForeground = true;

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
                        .setContent(remoteView);
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

    private void populateViews() {
        textView2.setText("");
        String[] projection = {StretchDbContract.Stretches.NAME,StretchDbContract.Stretches.TIME};

        Cursor cursor = getContentResolver().query(
                StretchDbContract.Stretches.CONTENT_URI,   // The content URI of the words table
                projection,             // The columns to return for each row
                null,                   // Selection criteria
                null,                   // Selection criteria
                null);
        if (cursor == null) {
            Toast.makeText(getApplicationContext(),"Cursor is null", Toast.LENGTH_LONG).show();
            return;
        }
        cursor.moveToFirst();
        do {
            String name = cursor.getString(cursor.getColumnIndex(StretchDbContract.Stretches.NAME));
            int time = cursor.getInt(cursor.getColumnIndex(StretchDbContract.Stretches.TIME));

            textView2.append(name + ": " + time + "\n");
        } while (cursor.moveToNext());


        cursor.close();
    }

    private void addTestEntry() {

        ContentValues cv = new ContentValues();
        cv.put(StretchDbContract.Stretches.NAME,"caca");
        cv.put(StretchDbContract.Stretches.TIME,"23");

        getContentResolver().insert(StretchDbContract.Stretches.CONTENT_URI,cv);
        populateViews();
    }


}

//******Eventually stuff
//Unregister broadcast recievers




