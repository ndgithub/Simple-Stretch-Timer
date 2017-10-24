package com.example.nicky.simplestretchtimer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.timeractivity.MainActivity;

/**
 * Created by Nicky on 10/11/17.
 */

public class WidgetProvider extends AppWidgetProvider {

    public static final String PLAY_TIMER_KEY = "play";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            // StretchWidgetService provideds views to the list collection
            Intent intent = new Intent(context, StretchWidgetService.class);

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);


            rv.setRemoteAdapter(appWidgetId, R.id.widget_list, intent);
            rv.setEmptyView(R.id.widget_list, R.id.empty_text);

            Intent playIntent = new Intent(context, MainActivity.class);
            playIntent.putExtra(PLAY_TIMER_KEY,"play");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.play_button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }
}



