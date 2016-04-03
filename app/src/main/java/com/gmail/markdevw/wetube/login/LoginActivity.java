package com.gmail.markdevw.wetube.login;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.login.fragments.LoginFragment;
import com.gmail.markdevw.wetube.login.fragments.SignUpFragment;
import com.gmail.markdevw.wetube.utils.Constants;

import java.util.Map;

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

        Firebase ref = new Firebase(Constants.FIREBASE_URL).child("users");
        ref.createUser(email, pass, new Firebase.ValueResultHandler<Map<String, Object>>() {
            public void onSuccess(Map<String, Object> result) {
                System.out.println("Successfully created user account with uid: " + result.get("uid"));
            }

            @Override
            public void onError(FirebaseError firebaseError) {

            }
        });
    }
}
