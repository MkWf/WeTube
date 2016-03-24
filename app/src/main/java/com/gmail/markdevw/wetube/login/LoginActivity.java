package com.gmail.markdevw.wetube.login;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.login.fragments.LoginFragment;
import com.gmail.markdevw.wetube.login.fragments.SignUpFragment;

/**
 * Created by Mark on 3/23/2016.
 */
public class LoginActivity extends AppCompatActivity implements LoginFragment.LoginListener, SignUpFragment.SignUpListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fl_fragment_container, LoginFragment.newInstance(), "tag")
                .commit();
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 1){
            getSupportFragmentManager().popBackStack();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onSignUpClicked() {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(getSupportFragmentManager().findFragmentByTag("tag"))
                .addToBackStack(null)
                .add(R.id.fl_fragment_container, SignUpFragment.newInstance(), "tag2")
                .commit();
    }

    @Override
    public void onConnect(String name, String pass) {

    }

    @Override
    public void onSignUpCompleted(String name, String pass, String email) {

    }
}
