package com.gmail.markdevw.wetube.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.gmail.markdevw.wetube.api.model.UserItem;
import com.parse.models.Blocked;

/**
 * Created by Mark on 11/11/2015.
 */
public class YesNoDialog extends DialogFragment {

    public interface onYesNoDialogOptionClickedListener extends DialogDismissInterface {
        void onYesNoDialogFragmentResult(int resultType, int which, Blocked blocked, UserItem user);
    }

    public static class Builder {
        private Bundle args = new Bundle();
        private YesNoDialog dialog = new YesNoDialog();

        public Builder(String title) {
            args.putString("title", title);
        }

        public Builder setYes(String replaceYes){
            args.putString("yes", replaceYes);
            return this;
        }

        public Builder setNo(String replaceNo){
            args.putString("no", replaceNo);
            return this;
        }

        public Builder setResultType(int resultType){
            args.putInt("resultType", resultType);
            return this;
        }

        public Builder setBlocked(Blocked blocked){
            dialog.setBlocked(blocked);
            return this;
        }

        public Builder setUser(UserItem user){
            dialog.setUser(user);
            return this;
        }

        public DialogFragment create(){
            dialog.setArguments(args);
            return dialog;
        }
    }

    private onYesNoDialogOptionClickedListener listener;
    private Blocked blocked;
    private UserItem user;

    public YesNoDialog() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof onYesNoDialogOptionClickedListener) {
            listener = (onYesNoDialogOptionClickedListener) activity;
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
        String yes = args.getString("yes", "Yes");
        String no = args.getString("no", "No");
        final int resultType = args.getInt("resultType", -1);

        //getDialog().setCancelable(false);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onYesNoDialogFragmentResult(resultType, which, blocked, user);
                    }
                })
                .setNegativeButton(no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onYesNoDialogFragmentResult(resultType, which, blocked, user);
                    }
                })
                .create();
    }

    public void setBlocked(Blocked blocked){
        this.blocked = blocked;
    }
    
    public void setUser(UserItem user){
        this.user = user;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        listener.dialogDismiss();
    }
}
