package com.gmail.markdevw.wetube.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by Mark on 11/11/2015.
 */
public class YesNoDialog extends DialogFragment {

    public interface onYesNoDialogOptionClickedListener {
        void onYesNoDialogFragmentResult(int resultType, int which);
    }

    onYesNoDialogOptionClickedListener listener;

    public YesNoDialog() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof onYesNoDialogOptionClickedListener) {
            listener = (onYesNoDialogOptionClickedListener) activity;
        }else {
            throw new ClassCastException(activity.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final String title = args.getString("title", "");
        String yes = args.getString("yes", "");
        String no = args.getString("no", "");
        final int resultType = args.getInt("resultType", -1);

        //getDialog().setCancelable(false);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onYesNoDialogFragmentResult(resultType, which);
                    }
                })
                .setNegativeButton(no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onYesNoDialogFragmentResult(resultType, which);
                    }
                })
                .create();
    }
}
