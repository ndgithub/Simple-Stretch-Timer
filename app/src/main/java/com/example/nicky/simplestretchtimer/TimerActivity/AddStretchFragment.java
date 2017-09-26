package com.example.nicky.simplestretchtimer.TimerActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.example.nicky.simplestretchtimer.R;
import com.squareup.haha.perflib.Main;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

/**
 * Created by Nicky on 9/4/17.
 */

public class AddStretchFragment extends DialogFragment {

    private NoticeDialogListener mListener;
    private String mTitle;


    static AddStretchFragment newInstance(@Nullable Integer time, @Nullable String name, String title) {
        AddStretchFragment addStretchFragment = new AddStretchFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("time", time);
        args.putString("title",title);
        addStretchFragment.setArguments(args);

        return addStretchFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialogueView = inflater.inflate(R.layout.add_stretch_dialogue_fragment, null);
        EditText stretchName = (EditText) dialogueView.findViewById(R.id.stretch_name);

        NumberPicker minPicker = (NumberPicker) dialogueView.findViewById(R.id.min_picker);
        minPicker.setMaxValue(10);
        minPicker.setValue(0);

        NumberPicker secPicker = (NumberPicker) dialogueView.findViewById(R.id.sec_picker);
        secPicker.setMaxValue(55);
        secPicker.setValue(30);

        if (getArguments() != null) {
            secPicker.setValue(getArguments().getInt("time"));
            stretchName.setText(getArguments().getString("name"));
            mTitle = getArguments().getString("title");
        }

        builder.setView(dialogueView);

        builder.setMessage(mTitle)
                .setPositiveButton("Add", (dialog, id) -> mListener.onDialogPositiveClick(
                        null, minPicker.getValue(), secPicker.getValue(), stretchName.getText() + ""))
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // User cancelled the dialog
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }


    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, int minValue, int secValue, String stretchName);

        public void onDialogNegativeClick(DialogFragment dialog);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


}

