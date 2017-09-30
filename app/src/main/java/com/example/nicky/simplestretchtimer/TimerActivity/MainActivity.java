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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.data.Stretch;
import com.example.nicky.simplestretchtimer.data.StretchDbContract;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AddStretchFragment.NoticeDialogListener {

    @BindView(R.id.display_time)
    TextView mDisplayText;
    @BindView(R.id.button_play_pause)
    ImageView mPlayPauseButton;
    @BindView(R.id.button_reset)
    ImageView mResetButton;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.add_button)
    ImageView mAddButton;
    @BindView(R.id.display_container)
    CardView mDisplayContainer;
    @BindView(R.id.timer_controls)
    CardView mTimerControls;
    @BindView(R.id.stretch_count_value)
    TextView mStretchCountValue;
    @BindView(R.id.total_time_value)
    TextView mTotalTime;
    @BindView(R.id.progress_bar)
    FrameLayout mProgressBar;
    @BindView(R.id.progress_container)
    FrameLayout mProgressContainer;


    NotificationCompat.Builder mBuilder;
    private final int NOTIFICATION_ID = 1;

    private BroadcastReceiver mActivityTickReceiver;
    private BroadcastReceiver mNotificationTickReciever;
    private BroadcastReceiver mPosChangeReceiver;
    private BroadcastReceiver mAllStretchesCompleteReciever;

    private TimerService mTimerService;
    ServiceConnection serviceConnection;

    private final String TIMER_TEXT_KEY = "Timer Text Key";
    private final String TIMER_POS_KEY = "Timer Position Key";

    private NotificationManager mNotificationManager;
    boolean isBound;
    private final String IS_BOUND = "isBound";

    private Stretch mCurrentStretch;

    private int mTimerPos;
    int mCurrentStretchLength;

    private LinearLayoutManager mLinearLayoutManager;
    private StretchAdapter mAdapter;
    private ArrayList<Stretch> mStretchArray;
    private RemoteViews mRemoteView;

    private int mScreenWidth;

    private int newPos;
    private int mCurrentStretchtimeRemaining;
    private String mStretchOrBreak;
    static final public String TYPE_BREAK = "break";
    static final public String TYPE_STRETCH = "stretch";


    private LocalBroadcastManager mLocalBroadcastManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        mDisplayContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mDisplayContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mScreenWidth = mDisplayContainer.getWidth();
                // mTimerControls.setMinimumHeight((int) Math.ceil(mDisplayContainer.getHeight() * Math.pow(.618,3)));
                Log.v("***w", "width: " + mDisplayContainer.getMeasuredWidth());
            }
        });

        if (savedInstanceState != null) {
            mDisplayText.setText(savedInstanceState.getString(TIMER_TEXT_KEY));
            mTimerPos = savedInstanceState.getInt(TIMER_POS_KEY);
        }

        mRemoteView = new RemoteViews(getPackageName(), R.layout.notification);

        mPlayPauseButton.setOnClickListener(v -> onPlayPauseClick());
        mAddButton.setOnClickListener(v -> showAddStretchDialog());// addStretch("New Stretch: ", 5));
        mResetButton.setOnClickListener(v -> reset());

        setupRecyclerView();
        initializeLoader();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        mPosChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                newPos = intent.getIntExtra(TimerService.NEW_POSITION_KEY, 0);
                mCurrentStretch = mStretchArray.get(newPos);
                Log.v("***", "position changed to: " + mCurrentStretch.getPosition());
                onPositionChanged();
            }
        };

        mAllStretchesCompleteReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPlayPauseButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
            }
        };

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mPosChangeReceiver, new IntentFilter(TimerService.POSITION_CHANGED_KEY));
        mLocalBroadcastManager.registerReceiver(mAllStretchesCompleteReciever, new IntentFilter(TimerService.STRETCHES_COMPLETE_KEY));

        mStretchOrBreak = TYPE_STRETCH;


    }

    private void onPositionChanged() {

        updateHighlightedStretch(mCurrentStretch.getPosition());
        updateCurrentStretchDisplay(mCurrentStretch.getPosition());
        updateUiTotalTimeRemaining(0);


        // Highlighted
        // Display - Count
        // Display - Total Remaining

        // Display - Stretch or Break stuff


    }

    private void updateUiTotalTimeRemaining(int currentStretchRemaining) {
        mTotalTime.setText(returnTotalStretchTime() + currentStretchRemaining + "");
    }

    private void newStretchType(int newPos) {
        mStretchOrBreak = newPos % 2 == 0 ? TYPE_STRETCH : TYPE_BREAK;

    }

    private void updateCurrentStretchDisplay(int newPos) {
        mStretchCountValue.setText(((newPos + 2) / 2) + "/" + (mStretchArray.size() + 1) / 2);
    }

    private void updateUiProgressBar(int currentStretchRemaining) {
        int width;
        if (mStretchOrBreak == TYPE_STRETCH) {
            width = mScreenWidth - (mScreenWidth * currentStretchRemaining / mCurrentStretch.getTime());
        } else {
            width = (mScreenWidth * currentStretchRemaining / mCurrentStretch.getTime());
        }

        mProgressBar.setLayoutParams(new FrameLayout.LayoutParams(width, 16, Gravity.CENTER));
        Log.v("***", "progress: " + width);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNotificationManager.cancelAll();
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                TimerService.TimerBinder timerBinder = (TimerService.TimerBinder) service;
                mTimerService = timerBinder.getService();
                isBound = true;
                if (!mStretchArray.isEmpty()) {
                    mCurrentStretch = mStretchArray.get(mTimerService.getTimerPos());
                }

                mNotificationManager.cancelAll();
                mTimerService.updateStretches(mStretchArray);
                mTimerService.stopForeground(true);
                Log.v("***", "service connected");

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
                int currentStretchRemaining = (int) intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1);

                updateUiDisplayTime(currentStretchRemaining);
                updateUiTotalTimeRemaining(currentStretchRemaining);
                updateUiProgressBar(currentStretchRemaining);
            }
        };

        mLocalBroadcastManager.registerReceiver(mActivityTickReceiver, new IntentFilter(TimerService.ONTICK_KEY));


    }

    private void updateUiDisplayTime(int timeRemaining) {
        if (timeRemaining < 60) {
            mDisplayText.setText(timeRemaining + "");
        } else if (timeRemaining % 60 >= 10) {
            mDisplayText.setText(timeRemaining / 60 + ":" + timeRemaining % 60);
        } else {
            mDisplayText.setText(timeRemaining / 60 + ":0" + timeRemaining % 60);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocalBroadcastManager.unregisterReceiver(mNotificationTickReciever);

    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActivityTickReceiver);

        if (mTimerService != null && mTimerService.isTicking()) {
            registerNotificationReceiver();
            mTimerService.setForegroundState(true);
        } else {
            mTimerService.stopSelf();
            unbindService(serviceConnection);
        }
    }

    // TODO: if foregrounded, notification should not disappear when time up.

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("Act. LifeCycle", "onDestroy");
        mLocalBroadcastManager.unregisterReceiver(mPosChangeReceiver);
        mLocalBroadcastManager.unregisterReceiver(mAllStretchesCompleteReciever);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putBoolean(IS_BOUND, isBound);
        outState.putBoolean("isRunning", mTimerService.isTicking());
        outState.putString(TIMER_TEXT_KEY, mDisplayText.getText().toString());
        outState.putInt(TIMER_POS_KEY, mTimerPos);
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
        if (mStretchArray.isEmpty()) {
            notifyUserOfNoStretches();
        } else {
            mPlayPauseButton.setImageResource(R.drawable.ic_pause_black_48dp);
            mTimerService.play();
        }

    }

    private void notifyUserOfNoStretches() {
        Toast.makeText(this, "Add A Stretch First", Toast.LENGTH_SHORT).show();
    }

    public void pauseTimer() {
        mPlayPauseButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        mTimerService.pause();
    }

    public void reset() {
        mPlayPauseButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        mTimerService.reset();
        if (mStretchArray.size() == 0) {
            mDisplayText.setText("-");
        } else {
            mDisplayText.setText(String.valueOf(mStretchArray.get(0).getTime()));
        }
    }


    private void setupRecyclerView() {

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mStretchArray = new ArrayList<>();
        mAdapter = new StretchAdapter(mStretchArray, mTimerPos, this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemViewCacheSize(0);
        //initializeLoader must stay at end of this function.
    }

    public void addStretch(String name, int time) {
        // TODO: scroll to bottom of recycler view
        ContentValues cv = new ContentValues();
        cv.put(StretchDbContract.Stretches.NAME, name);
        cv.put(StretchDbContract.Stretches.TIME, time);
        getContentResolver().insert(StretchDbContract.Stretches.CONTENT_URI, cv);
    }

    public void onDeleteStretch(int deletedPosition) {
        int currentPosition = mCurrentStretch.getPosition();
        Log.v("***", "position!f = : " + currentPosition);
        if (deletedPosition == currentPosition) {
            if (mTimerService.isTicking()) {
                mTimerService.stopTicking();
            }
            if (currentPosition < mStretchArray.size() - 1) { //If its not the last stretch
                Handler handler = new Handler();
                handler.postDelayed(() -> mTimerService.play(), 200);
                setCurrentStretch(currentPosition);
            } else if (currentPosition == mStretchArray.size() - 1) { // It it IS the last stretch
                mTimerService.timerFinished(1);
                Log.v("!!!", "msStretchArray size: " + mStretchArray.size());
                Log.v("!!!", "currentPosition: " + currentPosition);
                //mCurrentStretch = mStretchArray.get(currentPosition - 1);
            }
        } else if (deletedPosition < currentPosition) {
                mCurrentStretch = mStretchArray.get(currentPosition - 2);
                mTimerService.adjustTimerPos(-2);
            }
        }

    public void setCurrentStretch(int position) {
        Log.v("***", "position!: " + position);
        if (!mStretchArray.isEmpty()) {
            mCurrentStretch = mStretchArray.get(position);
        }
    }

    private void updateHighlightedStretch(int newPos) {
        int currentPos = mCurrentStretch.getPosition();
        int minVisPos = mLinearLayoutManager.findFirstVisibleItemPosition();
        int maxVisPos = mLinearLayoutManager.findLastVisibleItemPosition();
        if (newPos >= minVisPos && newPos <= maxVisPos) { //Is  new stretch position on-screen
            if (newPos % 2 == 0) {
                RelativeLayout currentStretch = (RelativeLayout) mLinearLayoutManager.findViewByPosition(newPos);
                RelativeLayout relativeLayout = (RelativeLayout) currentStretch.findViewById(R.id.list_item_container);
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorStretchHighlight));
            }
        }
        if (currentPos != newPos) { //check to see if current stretch was deleted by user
            if (currentPos >= minVisPos && currentPos <= maxVisPos) { //Is old stretch position on-screen
                if (currentPos % 2 == 0) {
                    RelativeLayout currentStretch = (RelativeLayout) mLinearLayoutManager.findViewByPosition(currentPos);
                    RelativeLayout relativeLayout = (RelativeLayout) currentStretch.findViewById(R.id.list_item_container);
                    relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorStretch));
                }
            }
            mCurrentStretch = mStretchArray.get(newPos);
        }
        mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, mCurrentStretch.getPosition());

    }

    private int returnTotalStretchTime() {
        int sumOfStretchTime = 0;
        for (int i = mStretchArray.size() - 1; i > mCurrentStretch.getPosition(); i -= 2) {
            sumOfStretchTime = sumOfStretchTime + mStretchArray.get(i).getTime();
        }
        return sumOfStretchTime;
    }


    public int getCurrentStretchPosition() {
        return mCurrentStretch.getPosition();
    }

    private void registerNotificationReceiver() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_grade_black_18dp)
                .setContent(mRemoteView)
                .setContentIntent(pendingIntent).build();

        mTimerService.startForeground(NOTIFICATION_ID, notification);

        mNotificationTickReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mTimerService.isForeground()) {
                    double timeRemaining = intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1);
                    mRemoteView.setTextViewText(R.id.remote_text, timeRemaining + " ");
                    mNotificationManager.notify(1, notification);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).
                registerReceiver(mNotificationTickReciever, new IntentFilter(TimerService.ONTICK_KEY));
    }

    public void showAddStretchDialog() {
        // Create an instance of the dialog fragment and show it
        AddStretchFragment dialog = AddStretchFragment.newInstance(10, null, "Add New Stretch");
        //DialogFragment dialog = new AddStretchFragment();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }
//----------------------- CursorLoader Stuff -----------------------//

    public void initializeLoader() {
        getSupportLoaderManager().initLoader(1, null, this);

    }

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
        int position = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String name = cursor.getString(cursor.getColumnIndex(StretchDbContract.Stretches.NAME));
                int time = cursor.getInt(cursor.getColumnIndex(StretchDbContract.Stretches.TIME));
                int id = cursor.getInt(cursor.getColumnIndex(StretchDbContract.Stretches._ID));
                mStretchArray.add(new Stretch(name, time, position, id));
                position++;
                mStretchArray.add(new Stretch("Change Position", 5, position, id));
                position++;
            } while (cursor.moveToNext());
            mStretchArray.remove(mStretchArray.size() - 1);
            setCurrentStretch(mTimerPos);
            updateUiTotalTimeRemaining(mCurrentStretch.getTime());
        }

        mAdapter.notifyDataSetChanged();
        if (mTimerService != null) {
            mTimerService.updateStretches(mStretchArray);
        }



        // UI List of stretches

        // Display Count
        // Total Time Remaining
    }


    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.notifyDataSetChanged();
    }

    //----------------------- Dialog Fragment Interface methods -----------------------//
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int minValue, int secValue, String stretchName) {
        addStretch(stretchName, secValue + (minValue * 60));
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }


}

//----------------------- Eventually Stuff -----------------------//
// UI - Time progress bars
// Total Time during breaks
// UI - for breaks
// UI for portrait and landscape
// Make landscape layout
// Make tablet layout
// Notification Layout
// Maintain scroll position on orientation change.
// Stretch Positon on Display UI
// Show time on startup

//Remove Log Statemtns
// Strings to resources
// use async task
//settings overflow (about me)

//----------------------- Optional -----------------------//
// Skip to next stretch button - ?
// Notifications when foregrounded
//Grayed out reset button when already reset, or no stretches.
//Grey highlight when paused.


//----------------------- Keep in Mind Stuff -----------------------//

// the services onCreate gets called when service is first started(gets called
// before onStartCommand). If service is already running, it does not get called.

//onLoadFinished of cursor adapter gets called everytime data changes.


//----------------------- Things That Change -----------------------//
// Current Stretch Pos (Pos Change)  -  Timer up, or reset
// Highlighted
// Display - Total Remaining
// Display - Stretch or Break
// Display - Colors
// Display - Progress Bar
// Display - Count
// Stretch Array  - Add, edit or delete stretch  - User adds or deletes
// UI List of stretches
// Display Count
// Total Time Remaining
// Current Time - Timer Ticking
// Display - Time
// Total time remaining


