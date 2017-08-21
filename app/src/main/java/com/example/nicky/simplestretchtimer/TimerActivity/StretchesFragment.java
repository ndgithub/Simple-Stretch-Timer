package com.example.nicky.simplestretchtimer.TimerActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.nicky.simplestretchtimer.R;
import com.example.nicky.simplestretchtimer.data.Stretch;
import com.example.nicky.simplestretchtimer.data.StretchDbContract;

import java.util.ArrayList;

/**
 * Created by Nicky on 8/19/17.
 */

public class StretchesFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerAdapter mAdapter;
    private ArrayList<Stretch> mStretchArray;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        setHasOptionsMenu(true);

        mStretchArray = new ArrayList<>();

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        dbToArray();
        mAdapter = new RecyclerAdapter(mStretchArray);

        mRecyclerView.setAdapter(mAdapter);


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    //This fragment provides the menu option to add entry.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.stretches_frag,container,false);


    }
    private void dbToArray() {
        mStretchArray.clear();
        String[] projection = {StretchDbContract.Stretches.NAME, StretchDbContract.Stretches.TIME};

        Cursor cursor = getContext().getContentResolver().query(
                StretchDbContract.Stretches.CONTENT_URI,   // The content URI of the words table
                projection,             // The columns to return for each row
                null,                   // Selection criteria
                null,                   // Selection criteria
                null);
        if (cursor == null) {
            Toast.makeText(getContext(), "Cursor is null", Toast.LENGTH_LONG).show();
            return;
        }
        cursor.moveToFirst();
        do {
            String name = cursor.getString(cursor.getColumnIndex(StretchDbContract.Stretches.NAME));
            int time = cursor.getInt(cursor.getColumnIndex(StretchDbContract.Stretches.TIME));
            mStretchArray.add(new Stretch(name, time));
        } while (cursor.moveToNext());

        cursor.close();
    }


    private void addTestEntry() {

        ContentValues cv = new ContentValues();
        cv.put(StretchDbContract.Stretches.NAME, "caca");
        cv.put(StretchDbContract.Stretches.TIME, "23");

        getContext().getContentResolver().insert(StretchDbContract.Stretches.CONTENT_URI, cv);
        dbToArray();
        mAdapter.notifyDataSetChanged();
    }


}
