package com.gmail.markdevw.wetube.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by Mark on 11/12/2015.
 */
public class OkDialog extends DialogFragment {

    public interface Empty extends DialogDismissInterface{

    }

    public static OkDialog newInstance(String title){
        OkDialog dialog = new OkDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        dialog.setArguments(args);

        return dialog;
    }

    private DialogDismissInterface listener;

    public OkDialog() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof DialogDismissInterface) {
            listener = (DialogDismissInterface) activity;
        }else {
            throw new ClassCastException(activity.toString()
                    + " must implement onYesNoDialogOptionClickedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final String title = args.getString("title", "");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        listener.dialogDismiss();
    }
}

