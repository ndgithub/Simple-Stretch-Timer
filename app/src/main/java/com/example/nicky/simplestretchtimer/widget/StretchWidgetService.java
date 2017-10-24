package com.example.nicky.simplestretchtimer.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.data.StretchDbContract;

/**
 * Created by Nicky on 10/24/17.
 */

public class StretchWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StretchRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StretchRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private int mWidgetId;
    private Cursor mCursor;

    public StretchRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }
        String[] projection = {StretchDbContract.Stretches._ID, StretchDbContract.Stretches.NAME, StretchDbContract.Stretches.TIME};

        final long token = Binder.clearCallingIdentity();
        try {
            mCursor = mContext.getContentResolver().query(StretchDbContract.Stretches.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }


    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

        mCursor.moveToPosition(position);

        String name = mCursor.getString(mCursor.getColumnIndex(StretchDbContract.Stretches.NAME));
        Integer time = mCursor.getInt(mCursor.getColumnIndex(StretchDbContract.Stretches.TIME));

        rv.setTextViewText(R.id.stretch_name, name);
        rv.setTextViewText(R.id.stretch_time, Integer.toString(time));

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


}
