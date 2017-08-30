package com.example.nicky.simplestretchtimer.TimerActivity;

import android.app.Notification;
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
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.data.Stretch;
import com.example.nicky.simplestretchtimer.data.StretchDbContract;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    NotificationCompat.Builder mBuilder;
    private final int NOTIFICATION_ID = 1;

    private BroadcastReceiver mActivityTickReceiver;
    private BroadcastReceiver mNotificationTickReciever;
    private BroadcastReceiver mPosChangeReceiver;
    private TimerService mTimerService;
    ServiceConnection serviceConnection;


    private final String TIMER_TEXT_KEY = "Timer Text Key";

    private NotificationManager mNotificationManager;
    boolean isBound;
    private final String IS_BOUND = "isBound";

    private int mTimerPos;

    private LinearLayoutManager mLinearLayoutManager;
    private StretchAdapter mAdapter;
    private ArrayList<Stretch> mStretchArray;
    private RemoteViews mRemoteView;
    private int newPos;

    private LocalBroadcastManager mLocalBroadcastManager;

    @BindView(R.id.test_view1)
    TextView mDisplayText;
    @BindView(R.id.button_play_pause)
    Button mPlayPauseButton;
    @BindView(R.id.button_reset)
    Button mResetButton;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.add_button)
    Button mAddButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("Act. LifeCycle", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mDisplayText.setText(savedInstanceState.getString(TIMER_TEXT_KEY));
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRemoteView = new RemoteViews(getPackageName(), R.layout.notification);

        mPlayPauseButton.setOnClickListener(v -> onPlayPauseClick());
        mAddButton.setOnClickListener(v -> addStretch("New Stretch: ", 5));
        mResetButton.setOnClickListener(v -> reset());

        setupRecyclerView();
        initializeLoader();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mPosChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                newPos = intent.getIntExtra(TimerService.NEW_POSITION_KEY, 0);
                if (mLinearLayoutManager.getChildCount() >= mTimerPos) {
                    Log.v("!!***", mTimerPos + " Position ChangeReciever");
                    setCurrentStretch(newPos);
                }
            }
        };
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mPosChangeReceiver, new IntentFilter(TimerService.POSITION_CHANGED_KEY));

    }


    @Override
    protected void onStart() {
        super.onStart();
        mLocalBroadcastManager.unregisterReceiver(mNotificationTickReciever);

        mNotificationManager.cancelAll();


        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                TimerService.TimerBinder timerBinder = (TimerService.TimerBinder) service;
                mTimerService = timerBinder.getService();
                isBound = true;
                mTimerPos = mTimerService.getTimerPos();
                Log.v("***", "service connected");

                mNotificationManager.cancelAll();
                mTimerService.updateStretches(mStretchArray);
                mTimerService.stopForeground(true);

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
                Log.v("*** - MainActivity", "Service is un-bound");
            }
        };

        startService(new Intent(this, TimerService.class));
        bindService(new Intent(this, TimerService.class), serviceConnection, BIND_ABOVE_CLIENT);

        mActivityTickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double secsRemaining = intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1);
                mDisplayText.setText(secsRemaining + " ");
            }
        };
        mLocalBroadcastManager.registerReceiver(mActivityTickReceiver, new IntentFilter(TimerService.ONTICK_KEY));


        Log.v("Act. LifeCycle", "onStart");

// TODO: let service know when foreground or not.

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("Act. LifeCycle", "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActivityTickReceiver);


        if (mTimerService != null && mTimerService.isTicking()) {
            Intent resultIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, resultIntent, 0);
            Notification notification =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_grade_black_18dp)
                            .setContent(mRemoteView)
                            .setContentIntent(pendingIntent).build();

            mTimerService.startForeground(NOTIFICATION_ID, notification);
            mTimerService.setForegroundState(true);
            mNotificationTickReciever = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (mTimerService.isForeground()) {
                        double secsRemaining = intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1);
                        mRemoteView.setTextViewText(R.id.remote_text, secsRemaining + " ");
                        mNotificationManager.notify(1, notification);
                        Log.v("!!***", mTimerPos + "    Notification Revciever");
                    }
                }
            };
            LocalBroadcastManager.getInstance(this).
                    registerReceiver(mNotificationTickReciever, new IntentFilter(TimerService.ONTICK_KEY));
        } else {
            mTimerService.stopSelf();
            unbindService(serviceConnection);
            Log.v("!***", "unbind service");
        }


        Log.v("Act. LifeCycle", "onStop");
    }

    // TODO: if foregrounded, notification should not disappear when time up.  

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("Act. LifeCycle", "onDestroy");
        mLocalBroadcastManager.unregisterReceiver(mPosChangeReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_BOUND, isBound);
        outState.putString("Play Button Text", mPlayPauseButton.getText().toString());
        outState.putString(TIMER_TEXT_KEY, mDisplayText.getText().toString());
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
            addStretch("asdf", 5);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }


    private void onPlayPauseClick() {
        if (!mTimerService.isTicking()) {
            playTimer();
        } else {
            pauseTimer();
        }
    }

    public void playTimer() {
        mPlayPauseButton.setText("PAUSE");
        mTimerService.play();

    }

    public void pauseTimer() {
        mPlayPauseButton.setText("Play");
        mTimerService.pause();
    }

    public void reset() {
        mPlayPauseButton.setText("Play");
        mTimerService.reset();
        mDisplayText.setText(String.valueOf(mStretchArray.get(0).getTime()));
    }


    private void setupRecyclerView() {

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mStretchArray = new ArrayList<>();
        mAdapter = new StretchAdapter(mStretchArray, mTimerPos, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemViewCacheSize(0);
        //initializeLoader must stay at end of this function.
    }


    public void addStretch(String name, int time) {
        // TODO: scroll to bottom of recycyler view
        ContentValues cv = new ContentValues();
        cv.put(StretchDbContract.Stretches.NAME, name);
        cv.put(StretchDbContract.Stretches.TIME, time);
        getContentResolver().insert(StretchDbContract.Stretches.CONTENT_URI, cv);
    }


    //----------------------- CursorLoader Stuff -----------------------//
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.v("**Loader", "onCreateLoader");
        String[] projection = {StretchDbContract.Stretches._ID, StretchDbContract.Stretches.NAME, StretchDbContract.Stretches.TIME};
        CursorLoader cursorLoader = new CursorLoader(this, StretchDbContract.Stretches.CONTENT_URI, projection, null, null, null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        mStretchArray.clear();
        cursor.moveToFirst();
        do {
            String name = cursor.getString(cursor.getColumnIndex(StretchDbContract.Stretches.NAME));
            int time = cursor.getInt(cursor.getColumnIndex(StretchDbContract.Stretches.TIME));
            String addedPos = cursor.getString(0);
            mStretchArray.add(new Stretch(name, time, addedPos));
        } while (cursor.moveToNext());

        mAdapter.notifyDataSetChanged();
        Log.v("*****Loader", "onLoadFinished");
        if (mTimerService != null) {
            mTimerService.updateStretches(mStretchArray);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.notifyDataSetChanged();
        Log.v("******", "onLoaderReset");
    }


    private void setCurrentStretch(int newPos) {
        int minVisPos = mLinearLayoutManager.findFirstVisibleItemPosition();
        int maxVisPos = mLinearLayoutManager.findLastVisibleItemPosition();
        Log.v("!!***", mTimerPos + "    highlightCurrentStretch()");

        if (newPos >= minVisPos && newPos <= maxVisPos) {
            Log.v("!!***", mTimerPos + "    Highlight Current");
            LinearLayout currentStretch = (LinearLayout) mLinearLayoutManager.findViewByPosition(newPos);
            currentStretch.setBackgroundColor(0xffcccccc);
        }

        if (mTimerPos >= minVisPos && mTimerPos <= maxVisPos) {
            Log.v("!!***", mTimerPos + "    Unhighlight Old");
            LinearLayout previousStretch = (LinearLayout) mLinearLayoutManager.findViewByPosition(mTimerPos);
            previousStretch.setBackgroundColor(0xffccffcc);
        }

        mTimerPos = newPos;

// TODO: retain button text after orientatoin change

    }
//
//    private void buildNotification() {
//
//
////        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
////        stackBuilder.addParentStack(MainActivity.class);
////        stackBuilder.addNextIntent(resultIntent);
////        PendingIntent resultPendingIntent =
////                stackBuilder.getPendingIntent(
////                        0,
////                        PendingIntent.FLAG_UPDATE_CURRENT
////                );
////        mBuilder.setContentIntent(resultPendingIntent);
//        //mNotificationManager =
//                //(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//    }

    public void initializeLoader() {
        getSupportLoaderManager().initLoader(1, null, this);

    }

    public int getTimerPos() {
        return mTimerPos;
    }

}

//----------------------- Eventually Stuff -----------------------//

// Change position between stretches
// Skip to next stretch button  -NO
// Settings to adjust break
// Save text onSaveInstance
//Maintain scroll position on orientation change.
// UI
// Notifications when foregrounded
// Sounds


//----------------------- Keep in Mind Stuff -----------------------//

// the services onCreate gets called when service is first started(gets called
// before onStartCommand). If service is already running, it does not get called.

//onLoadFinished of cursor adapter gets called everytime data changes.
