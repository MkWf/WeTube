package com.gmail.markdevw.wetube.login.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.gmail.markdevw.wetube.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mark on 3/24/2016.
 */
public class SignUpFragment extends Fragment {

    public interface SignUpListener {
        void onSignUpCompleted(String name, String pass, String email);
    }

    @Bind(R.id.signup_name) EditText name;
    @Bind(R.id.signup_password) EditText password;
    @Bind(R.id.signup_confirm_password) EditText confirmPassword;
    @Bind(R.id.signup_email) EditText email;
    @Bind(R.id.signup_done) Button done;

    private SignUpListener listener;

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    public SignUpFragment(){}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof SignUpListener) {
            listener = (SignUpListener) context;
        }else {
            throw new ClassCastException(context.toString()
                    + " must implement SignUpFragment.SignUpListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.signup_done)
    public void signUp(){
        listener.onSignUpCompleted(name.getText().toString(), password.getText().toString(), email.getText().toString());
    }
}
