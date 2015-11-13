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
public class YesNoOkDialog extends DialogFragment {

    public interface onYesNoOkDialogOptionClickedListener {
        void onYesNoOkDialogFragmentResult(int resultType, int which, String name, String id);
    }

    private onYesNoOkDialogOptionClickedListener listener;

    public YesNoOkDialog() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof onYesNoOkDialogOptionClickedListener) {
            listener = (onYesNoOkDialogOptionClickedListener) activity;
        }else {
            throw new ClassCastException(activity.toString()
                    + " must implement MyListFragment.onYesNoOkDialogOptionClickedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString("title", "");
        String yes = args.getString("yes", "");
        String no = args.getString("no", "");
        String ok = args.getString("ok", "");
        final String name = args.getString("name", "");
        final String id = args.getString("id", "");
        final int resultType = args.getInt("resultType", -1);

        return new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setPositiveButton(yes, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onYesNoOkDialogFragmentResult(resultType, which, name, id);
                }
            })
            .setNegativeButton(no, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    listener.onYesNoOkDialogFragmentResult(resultType, which, name, id);
                }
            })
            .setNeutralButton(ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    listener.onYesNoOkDialogFragmentResult(resultType, which, name, id);
                }
            })
            .create();
    }
}
