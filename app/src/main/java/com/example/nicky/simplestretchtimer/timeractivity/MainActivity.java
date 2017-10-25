package com.example.nicky.simplestretchtimer.timeractivity;

import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
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
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.Utils;
import com.example.nicky.simplestretchtimer.widget.WidgetProvider;
import com.example.nicky.simplestretchtimer.aboutactivity.AboutActivity;
import com.example.nicky.simplestretchtimer.data.Stretch;
import com.example.nicky.simplestretchtimer.data.StretchDbContract;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AddStretchFragment.NoticeDialogListener {


    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mActivityTickReceiver;
    private BroadcastReceiver mNotificationTickReciever;
    private BroadcastReceiver mPosChangeReceiver;
    private BroadcastReceiver mAllStretchesCompleteReciever;

    private TimerService mTimerService;
    ServiceConnection serviceConnection;
    boolean isBound;

    private final String TIMER_TEXT_KEY = "Timer Text";
    private final String TIMER_POS_KEY = "Timer Position";
    private final String PROGRESS_BAR_KEY = "Progress Bar Width";
    private final String CURRENT_STRETCH_REMAINING_KEY = "Current Stretch Remaining Key";
    private final String CURRENT_STRETCH_TENTHS_KEY = "Current Stretch Tenths Key";
    private final String TOTAL_TIME_REMAINING_KEY = "Total Time Remaining";
    private int mTimerPos;
    private Integer mCurrentStretchSecsRemaining;
    private Integer mCurrentTenthsRemaining;

    private RemoteViews mRemoteView;

    private NotificationManager mNotificationManager;
    private final int NOTIFICATION_ID = 1;

    private LinearLayoutManager mLinearLayoutManager;
    private StretchAdapter mAdapter;
    private ArrayList<Stretch> mStretchArray;

    private int mScreenWidth;

    @BindView(R.id.display_container)
    CardView mDisplayContainer;
    @BindView(R.id.settings)
    ImageView mSettingsButton;
    @BindView(R.id.display)
    LinearLayout mDisplay;
    @BindView(R.id.display_time)
    TextView mDisplayText;
    @BindView(R.id.stretch_count_label)
    TextView mStretchCountLabel;
    @BindView(R.id.stretch_count_value)
    TextView mStretchCountValue;
    @BindView(R.id.total_time_label)
    TextView mTotalTimeLabel;
    @BindView(R.id.total_time_value)
    TextView mTotalTimeValue;
    @BindView(R.id.progress_bar)
    FrameLayout mProgressBar;
    @BindView(R.id.progress_container)
    FrameLayout mProgressContainer;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.timer_controls)
    CardView mTimerControls;
    @BindView(R.id.button_play_pause)
    ImageView mPlayPauseButton;
    @BindView(R.id.button_reset)
    ImageView mResetButton;
    @BindView(R.id.add_button)
    ImageView mAddButton;

    private FirebaseAnalytics mFirebaseAnalytics;
    private String FB_BUTTON_CLICKED = "button_clicked";
    private String FB_BUTTON_TYPE_KEY = "button_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (savedInstanceState != null) {
            mDisplayText.setText(savedInstanceState.getString(TIMER_TEXT_KEY));
            mTimerPos = savedInstanceState.getInt(TIMER_POS_KEY);
            mCurrentStretchSecsRemaining = savedInstanceState.getInt(CURRENT_STRETCH_REMAINING_KEY);
            mCurrentTenthsRemaining = savedInstanceState.getInt(CURRENT_STRETCH_TENTHS_KEY);
            mTotalTimeValue.setText(savedInstanceState.getString(TOTAL_TIME_REMAINING_KEY));
        }

        mDisplayContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mDisplayContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mScreenWidth = mDisplayContainer.getWidth();
            }
        });
        setupRecyclerView();
        getSupportLoaderManager().initLoader(1, null, this);

        mRemoteView = new RemoteViews(getPackageName(), R.layout.notification);

        mPlayPauseButton.setOnClickListener(v -> {
            onPlayPauseClick();
            fbButtonClicked("Play/Pause");
        });
        mAddButton.setOnClickListener(v -> {
            showAddStretchDialog();
            fbButtonClicked("Add");
        });
        mResetButton.setOnClickListener(v -> {
            reset();
            fbButtonClicked("Reset");
        });
        mSettingsButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, mSettingsButton);
            popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            });
            popup.show();
        });

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mPosChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTimerPos = intent.getIntExtra(TimerService.NEW_POSITION_KEY, 0);
                if (mTimerPos % 2 == 0) { //If its a "Change Position"
                    setStretchColors();
                } else {
                    setBreakColors();
                }
                mCurrentStretchSecsRemaining = mStretchArray.get(mTimerPos).getTime();
                updateUiCurrentStretchDisplay();
                updateUiHighlightedStretch();
                updateTotalTimeRemaining();
                updateUiProgressBar();
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
    }

    private void setupRecyclerView() {

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                if (!mStretchArray.isEmpty()) {
                    updateUiHighlightedStretch();
                    updateUiProgressBar();
                    updateUiCurrentStretchDisplay();
                }
            }
        };

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mStretchArray = new ArrayList<>();
        mAdapter = new StretchAdapter(mStretchArray, mTimerPos, this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemViewCacheSize(0);
    }

    private void updateTotalTimeRemaining() {
        if (mStretchArray.size() == 0) {
            mTotalTimeValue.setText("-");
        } else {
            if (mTimerPos % 2 == 0) {
                mTotalTimeValue.setText(Utils.formatTime(returnSumOfStretchTime() - (mStretchArray.get(mTimerPos).getTime() - mCurrentStretchSecsRemaining)));
            } else {
                mTotalTimeValue.setText(Utils.formatTime(returnSumOfStretchTime()));
            }
        }
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
                mNotificationManager.cancelAll();
                mTimerService.updateStretches(mStretchArray);
                mTimerService.stopForeground(true);

                if (mTimerService.isTicking()) {
                    mPlayPauseButton.setImageResource(R.drawable.ic_pause_black_48dp);
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
            }
        };
        startService(new Intent(this, TimerService.class));
        bindService(new Intent(this, TimerService.class), serviceConnection, BIND_ABOVE_CLIENT);
        mActivityTickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                mCurrentTenthsRemaining = (int) intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1);
                if (mTimerPos % 2 == 1) {
                    mDisplayText.setText(R.string.change_position);
                } else {
                    if (mCurrentTenthsRemaining % 10 == 0) {
                        mCurrentStretchSecsRemaining = mCurrentTenthsRemaining / 10;
                        mDisplayText.setText(Utils.formatTime(mCurrentStretchSecsRemaining));
                        Log.v("***", "tenthsRemaining: " + mCurrentTenthsRemaining);
                        Log.v("***", "secsRemaining: " + mCurrentStretchSecsRemaining);

                        updateTotalTimeRemaining();
                    }
                }
                updateUiProgressBar();
            }
        };

        mLocalBroadcastManager.registerReceiver(mActivityTickReceiver, new IntentFilter(TimerService.ONTICK_KEY));
    }


    private int returnSumOfStretchTime() {
        int sumOfStretchTime = 0;
        for (int i = mStretchArray.size() - 1; i >= mTimerPos; i -= 2) {
            sumOfStretchTime = sumOfStretchTime + mStretchArray.get(i).getTime();
        }
        return sumOfStretchTime;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocalBroadcastManager.unregisterReceiver(mNotificationTickReciever);
        if (getIntent() != null) {
            if (getIntent().getStringExtra(WidgetProvider.PLAY_TIMER_KEY) != null && getIntent().getStringExtra(WidgetProvider.PLAY_TIMER_KEY).equals("play")) {
                Handler handler = new Handler();
                handler.postDelayed(() -> playTimer(), 200);
            }
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mPosChangeReceiver);
        mLocalBroadcastManager.unregisterReceiver(mAllStretchesCompleteReciever);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRunning", mTimerService.isTicking());
        outState.putString(TIMER_TEXT_KEY, mDisplayText.getText().toString());
        outState.putInt(TIMER_POS_KEY, mTimerPos);
        outState.putInt(PROGRESS_BAR_KEY, mProgressBar.getWidth());
        outState.putInt(CURRENT_STRETCH_REMAINING_KEY, mCurrentStretchSecsRemaining);
        outState.putString(TOTAL_TIME_REMAINING_KEY, mTotalTimeValue.getText().toString());
        outState.putInt(CURRENT_STRETCH_TENTHS_KEY, mCurrentTenthsRemaining);

    }

    private void setStretchColors() {
        mDisplay.setBackgroundColor(getResources().getColor(R.color.colorDisplayBackground));
        mProgressBar.setBackgroundColor(getResources().getColor(R.color.progressBar));
        mProgressContainer.setBackgroundColor(getResources().getColor(R.color.progressBarContainer));

        int DisplayTextColor = getResources().getColor(R.color.colorDisplayText);
        mDisplayText.setTextColor(DisplayTextColor);

        mStretchCountLabel.setTextColor(DisplayTextColor);
        mStretchCountValue.setTextColor(DisplayTextColor);
        mTotalTimeLabel.setTextColor(DisplayTextColor);
        mTotalTimeValue.setTextColor(DisplayTextColor);

    }

    private void setBreakColors() {
        mDisplay.setBackgroundColor(getResources().getColor(R.color.colorDisplayBackgroundBreak));
        mProgressBar.setBackgroundColor(getResources().getColor(R.color.progressBarBreak));
        mProgressContainer.setBackgroundColor(getResources().getColor(R.color.progressBarContainerBreak));

        int DisplayTextColorBreak = getResources().getColor(R.color.colorDisplayTextBreak);

        mDisplayText.setTextColor(DisplayTextColorBreak);
        mStretchCountLabel.setTextColor(DisplayTextColorBreak);
        mStretchCountValue.setTextColor(DisplayTextColorBreak);
        mTotalTimeLabel.setTextColor(DisplayTextColorBreak);
        mTotalTimeValue.setTextColor(DisplayTextColorBreak);
    }

    private void updateUiCurrentStretchDisplay() {
        if (mStretchArray.isEmpty()) {
            mStretchCountValue.setText("-");
        } else {
            mStretchCountValue.setText(((mTimerPos + 2) / 2) + " " + getString(R.string.of) + " " + (mStretchArray.size() + 1) / 2);
        }
    }

    private void updateUiHighlightedStretch() {
        int minVisPos = mLinearLayoutManager.findFirstVisibleItemPosition();
        int maxVisPos = mLinearLayoutManager.findLastVisibleItemPosition();
        for (int i = minVisPos; i <= maxVisPos; i += 2) { // Clear highlighting from all visisble items
            if (i >= 0) {
                RelativeLayout currentStretch = (RelativeLayout) mLinearLayoutManager.findViewByPosition(i);
                RelativeLayout relativeLayout = currentStretch.findViewById(R.id.list_item_container);
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.stretchItemBg));
                ((TextView) currentStretch.findViewById(R.id.stretch_time)).setTextColor(getResources().getColor(R.color.stretchItemText));
                ((ImageView) currentStretch.findViewById(R.id.delete_button)).setColorFilter(getResources().getColor(R.color.stretchItemDelete));
            }
        }

        if (mTimerPos >= minVisPos && mTimerPos <= maxVisPos) { //Is  new stretch position on-screen
            if (mTimerPos % 2 == 0) {
                RelativeLayout currentStretch = (RelativeLayout) mLinearLayoutManager.findViewByPosition(mTimerPos);
                RelativeLayout relativeLayout = currentStretch.findViewById(R.id.list_item_container);
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.stretchItemBgCurrent));
                ((TextView) currentStretch.findViewById(R.id.stretch_time)).setTextColor(getResources().getColor(R.color.stretchItemTextCurrent));
                ((ImageView) currentStretch.findViewById(R.id.delete_button)).setColorFilter(getResources().getColor(R.color.stretchItemDeleteCurrent));

            }
        }
        mLinearLayoutManager.smoothScrollToPosition(mRecyclerView, null, mTimerPos);

    }

    private void updateUiProgressBar() {
        int width;
        if (mStretchArray.size() == 0) {
            width = 0;
        } else {
            if (mTimerPos % 2 == 0) {
                width = mScreenWidth - ((mScreenWidth * (mCurrentTenthsRemaining)) / (mStretchArray.get(mTimerPos).getTime() * 10));
            } else {
                width = (mScreenWidth * (mCurrentTenthsRemaining) / (mStretchArray.get(mTimerPos).getTime() * 10));
            }
        }
//        ValueAnimator anim = ValueAnimator.ofInt(mProgressBar.getMeasuredWidth(), width);
//        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                int val = (Integer) valueAnimator.getAnimatedValue();
//                ViewGroup.LayoutParams layoutParams = mProgressBar.getLayoutParams();
//                layoutParams.width = val;
//                mProgressBar.setLayoutParams(layoutParams);
//            }
//        });
//        anim.setDuration(1000);
//        anim.setInterpolator(new LinearInterpolator());
//        anim.start();

        mProgressBar.setLayoutParams(new FrameLayout.LayoutParams(width, 16, Gravity.CENTER));

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
            if (!mTimerService.isTicking()) {
                mPlayPauseButton.setImageResource(R.drawable.ic_pause_black_48dp);
                mTimerService.play();
            }
        }

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

    public void addStretch(String name, int time) {
        ContentValues cv = new ContentValues();
        cv.put(StretchDbContract.Stretches.NAME, name);
        cv.put(StretchDbContract.Stretches.TIME, time);
        getContentResolver().insert(StretchDbContract.Stretches.CONTENT_URI, cv);
    }

    public void editStretch(String name, int time, int id) {
        ContentValues cv = new ContentValues();
        cv.put(StretchDbContract.Stretches.NAME, name);
        cv.put(StretchDbContract.Stretches.TIME, time);
        Uri uri = ContentUris.withAppendedId(StretchDbContract.Stretches.CONTENT_URI, id);
        getContentResolver().update(uri, cv, null, null);

    }

    public void onDeleteStretch(int deletedPosition) {
        boolean wasTicking = mTimerService.isTicking();
        if (deletedPosition == mTimerPos) { //If current stretch was deleted
            if (wasTicking) {
                mTimerService.stopTicking();
            }
            if (mTimerPos < mStretchArray.size() - 1) { //If its not the last stretch
                Handler handler = new Handler();
                if (wasTicking) {
                    handler.postDelayed(() -> mTimerService.play(), 200);
                }
            } else if (mTimerPos == mStretchArray.size() - 1) { // If it IS the last stretch
                mTimerService.timerFinished();
                mTimerPos--;
            }
        } else if (deletedPosition < mTimerPos) {
            if (mTimerPos == 1) { // If the timer pos is on first break
                mTimerPos = 0;
                mTimerService.adjustTimerPos(-2);
            } else {
                mTimerPos = mTimerPos - 2;
                mTimerService.adjustTimerPos(-2);
            }
        }

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
                    mCurrentTenthsRemaining = (int) intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1);
                    if (mTimerPos % 2 == 1) {
                        mRemoteView.setTextViewText(R.id.remote_text, getResources().getString(R.string.change_position));
                        mNotificationManager.notify(1, notification);
                    } else {
                        if (mCurrentTenthsRemaining % 10 == 0) {
                            mCurrentStretchSecsRemaining = mCurrentTenthsRemaining / 10;
                            mRemoteView.setTextViewText(R.id.remote_text, Utils.formatTime(mCurrentStretchSecsRemaining));
                            mNotificationManager.notify(1, notification);
                        }
                    }



                }
            }
        };
        LocalBroadcastManager.getInstance(this).
                registerReceiver(mNotificationTickReciever, new IntentFilter(TimerService.ONTICK_KEY));
    }

    private void notifyUserOfNoStretches() {
        Toast.makeText(this, R.string.add_a_stretch_first, Toast.LENGTH_SHORT).show();
    }

    //----------------------- CursorLoader Stuff -----------------------//

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
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
        if (mCurrentStretchSecsRemaining == null) {
            mCurrentStretchSecsRemaining = mStretchArray.get(mTimerPos).getTime();
        }
        if (mCurrentTenthsRemaining == null) {
            mCurrentTenthsRemaining = mStretchArray.get(mTimerPos).getTime() * 10;

        }


        updateTotalTimeRemaining();
    }

    private void fbButtonClicked(String buttonType) {
        Bundle bundle = new Bundle();
        bundle.putString(FB_BUTTON_TYPE_KEY, buttonType);
        mFirebaseAnalytics.logEvent(FB_BUTTON_CLICKED, bundle);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.notifyDataSetChanged();
    }


    //----------------------- Dialogue Fragment Interface methods -----------------------//
    public void showAddStretchDialog() {
        AddStretchFragment dialog = AddStretchFragment.newInstance(10, null, null, AddStretchFragment.TYPE_ADD);
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int minValue, int secValue, String stretchName, Integer id, int type) {
        switch (type) {
            case AddStretchFragment.TYPE_ADD:
                addStretch(stretchName, secValue + (minValue * 60));
                break;
            case AddStretchFragment.TYPE_EDIT:
                editStretch(stretchName, secValue + (minValue * 60), id);
                break;
        }

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

}

// Focusable


