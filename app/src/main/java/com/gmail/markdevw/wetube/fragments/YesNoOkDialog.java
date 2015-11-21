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

    public interface onYesNoOkDialogOptionClickedListener extends DialogDismissInterface {
        void onYesNoOkDialogFragmentResult(int resultType, int which, String name, String id);
    }

    public static class Builder {
        private Bundle args = new Bundle();
        private YesNoOkDialog dialog = new YesNoOkDialog();

        public Builder(String title) {
            args.putString("title", title);
        }

        public Builder setName(String name){
            args.putString("name", name);
            return this;
        }

        public Builder setId(String id){
            args.putString("id", id);
            return this;
        }

        public Builder setYes(String replaceYes){
            args.putString("yes", replaceYes);
            return this;
        }

        public Builder setNo(String replaceNo){
            args.putString("no", replaceNo);
            return this;
        }

        public Builder setOk(String replaceOk){
            args.putString("ok", replaceOk);
            return this;
        }

        public Builder setResultType(int resultType){
            args.putInt("resultType", resultType);
            return this;
        }

        public DialogFragment create(){
            dialog.setArguments(args);
            return dialog;
        }
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
                    + " must implement onYesNoOkDialogOptionClickedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString("title", "title");
        String yes = args.getString("yes", "yes");
        String no = args.getString("no", "no");
        String ok = args.getString("ok", "ok");
        final String name = args.getString("name", "name");
        final String id = args.getString("id", "id");
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        listener.dialogDismiss();
    }
}
