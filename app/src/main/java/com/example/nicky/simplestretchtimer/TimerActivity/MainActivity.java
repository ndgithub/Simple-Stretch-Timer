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
import android.content.res.Resources;
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
import android.widget.LinearLayout;
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

    private int mTimerPos;

    private LinearLayoutManager mLinearLayoutManager;
    private StretchAdapter mAdapter;
    private ArrayList<Stretch> mStretchArray;
    private RemoteViews mRemoteView;
    private int mCurrentStretchSecsRemaining;

    private LocalBroadcastManager mLocalBroadcastManager;

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
    @BindView(R.id.display)
    LinearLayout mDisplay;
    @BindView(R.id.progress_bar)
    FrameLayout mProgressBar;
    @BindView(R.id.progress_container)
    FrameLayout mProgressContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        mDisplayContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mDisplayContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mDisplayContainer.getWidth();
                // mTimerControls.setMinimumHeight((int) Math.ceil(mDisplayContainer.getHeight() * Math.pow(.618,3)));
                Log.v("***w", "width: " + mDisplayContainer.getMeasuredWidth());
            }
        });


        if (savedInstanceState != null) {
            mDisplayText.setText(savedInstanceState.getString(TIMER_TEXT_KEY));
            mTimerPos = savedInstanceState.getInt(TIMER_POS_KEY);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
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
                mTimerPos = intent.getIntExtra(TimerService.NEW_POSITION_KEY, 0);


                if (mTimerPos % 2 == 1) { //If its a break/change change position
                    setBreakColors();

                } else {
                    setStretchColors();

                }
                updateCurrentStretchDisplay();
                updateHighlightedStretch();
                updateTotalTimeRemaining();


                if (mTimerPos == 0) { //If timer was user reset or finished
                    mTotalTime.setText(returnSumOfStretchTime() + "");
                    mDisplayText.setText(mStretchArray.get(mTimerPos).getTime() + "");
                }
            }
        };

        mAllStretchesCompleteReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v("***", "adfcaca");
                mPlayPauseButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);

            }
        };

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mPosChangeReceiver, new IntentFilter(TimerService.POSITION_CHANGED_KEY));
        mLocalBroadcastManager.registerReceiver(mAllStretchesCompleteReciever, new IntentFilter(TimerService.STRETCHES_COMPLETE_KEY));

    }

    private void setBreakColors() {
        mDisplay.setBackgroundColor(getResources().getColor(R.color.colorDisplayBackgroundBreak));
        mDisplayText.setTextColor(getResources().getColor(R.color.colorDisplayTextBreak));
    }
    private void setStretchColors() {
        mDisplay.setBackgroundColor(getResources().getColor(R.color.colorDisplayBackground));
        mDisplayText.setTextColor(getResources().getColor(R.color.colorDisplayText));
    }


    private void updateCurrentStretchDisplay() {
        if (mStretchArray.isEmpty()) {
            mStretchCountValue.setText("-");
        } else {
            mStretchCountValue.setText(((mTimerPos + 2) / 2) + "/" + (mStretchArray.size() + 1) / 2);
        }
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
    private void updateHighlightedStretch() {
        int minVisPos = mLinearLayoutManager.findFirstVisibleItemPosition();
        int maxVisPos = mLinearLayoutManager.findLastVisibleItemPosition();

        for (int i = minVisPos; i <= maxVisPos; i += 2) { // Clear highlighting from all visisble items
            RelativeLayout currentStretch = (RelativeLayout) mLinearLayoutManager.findViewByPosition(i);
            RelativeLayout relativeLayout = (RelativeLayout) currentStretch.findViewById(R.id.list_item_container);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorStretch));
        }

        if (mTimerPos >= minVisPos && mTimerPos <= maxVisPos) { //Is  new stretch position on-screen
            if (mTimerPos % 2 == 0) {
                RelativeLayout currentStretch = (RelativeLayout) mLinearLayoutManager.findViewByPosition(mTimerPos);
                RelativeLayout relativeLayout = (RelativeLayout) currentStretch.findViewById(R.id.list_item_container);
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorStretchHighlight));
            }
        }
        mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, mTimerPos);

    }

    private int returnSumOfStretchTime() {
        int sumOfStretchTime = 0;
        for (int i = mStretchArray.size() - 1; i >= mTimerPos; i -= 2) {
            sumOfStretchTime = sumOfStretchTime + mStretchArray.get(i).getTime();
        }
        return sumOfStretchTime;
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
                if (mTimerPos % 2 == 1) {
                    mDisplayText.setText("Change Position");
                } else {
                    int secsRemaining = (int) intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1);
                    if (secsRemaining < 60) {
                        mDisplayText.setText(secsRemaining + "");
                    } else if (secsRemaining % 60 >= 10) {
                        mDisplayText.setText(secsRemaining / 60 + ":" + secsRemaining % 60);
                    } else {
                        mDisplayText.setText(secsRemaining / 60 + ":0" + secsRemaining % 60);
                    }
                    mCurrentStretchSecsRemaining = secsRemaining;
                    updateTotalTimeRemaining();
                }

            }
        };

        mLocalBroadcastManager.registerReceiver(mActivityTickReceiver, new IntentFilter(TimerService.ONTICK_KEY));
    }

    private void updateTotalTimeRemaining() {
        if (mTimerPos % 2 == 0) {
            mTotalTime.setText(returnSumOfStretchTime() - (mStretchArray.get(mTimerPos).getTime() - mCurrentStretchSecsRemaining) + "");
        } else {
            mTotalTime.setText(returnSumOfStretchTime() + "");
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
            registerNotificationReciever();
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
        updateTotalTimeRemaining();
    }


    private void setupRecyclerView() {

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                if (!mStretchArray.isEmpty()) {
                    updateHighlightedStretch();
                }
            }
        };


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
        if (deletedPosition == mTimerPos) {
            if (mTimerService.isTicking()) {
                mTimerService.stopTicking();
            }

            if (mTimerPos < mStretchArray.size() - 1) { //If its not the last stretch
                Handler handler = new Handler();
                handler.postDelayed(() -> mTimerService.play(), 200);
                //newCurrentStretch(mTimerPos);
            } else if (mTimerPos == mStretchArray.size() - 1) { // It it IS the last stretch
                mTimerService.timerFinished(1);
                mTimerPos--;
            }
        } else if (deletedPosition < mTimerPos) {
            mTimerPos = mTimerPos - 2;
            mTimerService.adjustTimerPos(-2);
        }

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
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String name = cursor.getString(cursor.getColumnIndex(StretchDbContract.Stretches.NAME));
                int time = cursor.getInt(cursor.getColumnIndex(StretchDbContract.Stretches.TIME));
                int id = cursor.getInt(cursor.getColumnIndex(StretchDbContract.Stretches._ID));
                mStretchArray.add(new Stretch(name, time, 0, id));
            } while (cursor.moveToNext());
        }


        for (int i = 1; i < mStretchArray.size(); i += 2) { //Adds rest breaks (change stretch position) in between stretches
            mStretchArray.add(i, new Stretch("BREAK", 5, 1, null));
        }

        mAdapter.notifyDataSetChanged();
        if (mTimerService != null) {
            mTimerService.updateStretches(mStretchArray);
        }

        updateCurrentStretchDisplay();
        updateTotalTimeRemaining();
    }


    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.notifyDataSetChanged();
    }


    public int getTimerPos() {
        return mTimerPos;
    }


    private void registerNotificationReciever() {
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
                    double secsRemaining = intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1);
                    mRemoteView.setTextViewText(R.id.remote_text, secsRemaining + " ");
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

    //----------------------- Dialoge Fragment Interface methods -----------------------//
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int minValue, int secValue, String stretchName) {
        addStretch(stretchName, secValue + (minValue * 60));
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }


}

//----------------------- Eventually Stuff -----------------------//

// UI for portrait and landscape
// Make landscape layout
// Make tablet layout
// Notification Layout
// Maintain scroll position on orientation change.
// UI - for breaks
// UI - Time progress bars
// Change Hello World on Startup
//Remove Log Statemtns
// Strings to resources
// use async task
//settings overflow (about me)

//----------------------- Optional -----------------------//
// Skip to next stretch button - ?
// Programming to interfaces?
// Notifications when foregrounded
// NEXT STRETCH
//Gray reset button when already reset, or no stretches.

//----------------------- Material Stuff -----------------------//

// A touch target should be at least 48 X 48

//----------------------- Keep in Mind Stuff -----------------------//

// the services onCreate gets called when service is first started(gets called
// before onStartCommand). If service is already running, it does not get called.

//onLoadFinished of cursor adapter gets called everytime data changes.
