package com.example.nicky.simplestretchtimer.TimerActivity;

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


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    private final int NOTIFICATION_ID = 1;

    private BroadcastReceiver mTickReceiver;
    private BroadcastReceiver mPosChangeReceiver;
    private TimerService mTimerService;
    boolean isBound;

    private int mTimerPos;
    static final public String CURRENT_POSITION_KEY = "position";

    private MyLinearLayoutManager mLinearLayoutManager;
    private RecyclerAdapter mAdapter;
    private ArrayList<Stretch> mStretchArray;
    private RemoteViews mRemoteView;

    @BindView(R.id.test_view1)
    TextView mTextView;
    @BindView(R.id.button_play_pause)
    Button mPlayButton;
    @BindView(R.id.button_reset)
    Button mResetButton;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("***", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        if (savedInstanceState != null) {
            mTimerPos = savedInstanceState.getInt(CURRENT_POSITION_KEY);

        }


        mLinearLayoutManager = new MyLinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mStretchArray = new ArrayList<>();
        mAdapter = new RecyclerAdapter(mStretchArray,mTimerPos);
        mRecyclerView.setAdapter(mAdapter);

        startService(new Intent(this, TimerService.class));
        bindService(new Intent(this, TimerService.class), serviceConnection, BIND_ABOVE_CLIENT);
        mRemoteView = new RemoteViews(getPackageName(), R.layout.notification);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTimerService.isRunning()) {
                    play();
                } else {
                    pause();
                }
            }
        });

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();

            }
        });

        buildnotification();
        createRegisterBroadcastReceivers();



    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("!!@***", "onResume");
    }

    public void play() {
        mPlayButton.setText("PAUSE");
        mTimerService.play();
        // TODO:

    }

    public void pause() {
        mPlayButton.setText("Play");
        mTimerService.pause();
    }

    public void reset() {
        mPlayButton.setText("Play");
        mTimerService.reset();
        mTextView.setText(String.valueOf(mStretchArray.get(0).getTime()));
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


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_POSITION_KEY,mTimerPos);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder timerBinder = (TimerService.TimerBinder) service;
            mTimerService = timerBinder.getService();
            isBound = true;
            mTimerService.stopForeground(true);
            mTimerService.setForegroundState(false);
            Log.v("*** - MainActivity", "Service is bound");
            Log.v("***", mTimerService.toString());
            initializeLoader();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Log.v("*** - MainActivity", "Service is un-bound");
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (mTimerService.isRunning()) {
            mTimerService.startForeground(NOTIFICATION_ID, mBuilder.build());
            mTimerService.setForegroundState(true);
        }
    }




    public void addStretch(String name, int time) {
        ContentValues cv = new ContentValues();
        cv.put(StretchDbContract.Stretches.NAME, name);
        cv.put(StretchDbContract.Stretches.TIME, time);
        getContentResolver().insert(StretchDbContract.Stretches.CONTENT_URI, cv);
    }


    public void initializeLoader() {
        getSupportLoaderManager().initLoader(1, null, this);

    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.v("**Loader", "onCreateLoader");
        String[] projection = {StretchDbContract.Stretches.NAME, StretchDbContract.Stretches.TIME};
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
            mStretchArray.add(new Stretch(name, time));
        } while (cursor.moveToNext());

        //cursor.close();
        mAdapter.notifyDataSetChanged();
        Log.v("*****Loader", "onLoadFinished");
        mTimerService.updateStretches(mStretchArray);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.notifyDataSetChanged();
        Log.v("******", "onLoaderReset");
    }

    private void createRegisterBroadcastReceivers() {
        mTickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double secsRemaining = intent.getDoubleExtra(TimerService.MILS_UNTIL_FINISHED_KEY, 1);
                mTextView.setText(secsRemaining + " ");

                if (mTimerService.isForeground()) {
                    mRemoteView.setTextViewText(R.id.remote_text, secsRemaining + " ");
                    // TODO: make secsRemainingString
                    mNotificationManager.notify(1, mBuilder.build());
                }
            }
        };

        mPosChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTimerPos = intent.getIntExtra(TimerService.CURRENT_POSITION_KEY,0);
                for (int i = 0; i < mStretchArray.size(); i++) {
                    LinearLayout currentView = (LinearLayout) mLinearLayoutManager.findViewByPosition(i);
                    currentView.setBackgroundColor(0xffffffff);
                }
                highlightCurrentStretch(mTimerPos);

            }
        };

        LocalBroadcastManager.getInstance(this).
                registerReceiver(mTickReceiver, new IntentFilter(TimerService.ONTICK_KEY));
        LocalBroadcastManager.getInstance(this).
                registerReceiver(mPosChangeReceiver, new IntentFilter(TimerService.POSITION_CHANGED_KEY));

    }

    private void highlightCurrentStretch(int pos) {
        LinearLayout currentView = (LinearLayout) mLinearLayoutManager.findViewByPosition(pos);
        currentView.setBackgroundColor(0xffcccccc);
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
}

//******Eventually stuff
//Unregister broadcast recievers





