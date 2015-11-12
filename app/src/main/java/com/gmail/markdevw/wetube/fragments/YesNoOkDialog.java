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

    public YesNoOkDialog(){}

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString("title", "");
        String yes = args.getString("yes", "");
        String no = args.getString("no", "");
        String ok = args.getString("ok", "");

        return new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setPositiveButton(yes, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                }
            })
            .setNegativeButton(no, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                }
            })
            .setNeutralButton(ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_FIRST_USER, null);
                }
            })
            .create();
    }
}
