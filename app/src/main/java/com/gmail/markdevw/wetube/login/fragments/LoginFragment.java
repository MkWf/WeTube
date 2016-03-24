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
public class LoginFragment extends Fragment {

    public interface LoginListener {
        void onSignUpClicked();
        void onConnect(String name, String pass);
    }

    @Bind(R.id.login_email) EditText email;
    @Bind(R.id.login_name) EditText name;
    @Bind(R.id.login_password) EditText password;
    @Bind(R.id.login_confirmpassword) EditText confirmPassword;
    @Bind(R.id.login_signup) Button signup;
    @Bind(R.id.login_connect) Button connect;

    private LoginListener listener;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    public LoginFragment(){}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof LoginListener) {
            listener = (LoginListener) context;
        }else {
            throw new ClassCastException(context.toString()
                    + " must implement LoginFragment.LoginListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.login_signup)
    public void signUp(){
        listener.onSignUpClicked();
    }

    @OnClick(R.id.login_connect)
    public void connect(){
        confirmPassword.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);
        signup.setText("Cancel");
    }
}
