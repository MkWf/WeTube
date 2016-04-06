package com.gmail.markdevw.wetube.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.data.User;
import com.gmail.markdevw.wetube.utils.Constants;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;

/**
 * Created by Mark on 3/23/2016.
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private Firebase ref;
    private GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ref = new Firebase(Constants.FIREBASE_URL);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("361578459714-0gjdvq4pgkem4io1lt9jo3cq7k7mi50r.apps.googleusercontent.com")
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.login_google_sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                status.toString();
                            }
                        });
            }
        });

        findViewById(R.id.revoke).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                status.toString();
                            }
                        });
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connectionResult.toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                account = result.getSignInAccount();
                final String email = account.getEmail();

                new Thread(){
                    String token;
                    public void run(){
                        String scopes = "oauth2:profile email";
                        try{
                            token = GoogleAuthUtil.getToken(getApplicationContext(), email, scopes);
                        }catch(IOException e){

                        }catch(GoogleAuthException e){

                        }

                        ref.authWithOAuthToken("google", token, new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(AuthData authData) {
                                final String userName = (String) authData.getProviderData().get("displayName");
                                final String email = account.getEmail().toLowerCase();
                                final String encodedEmail = com.gmail.markdevw.wetube.utils.Utils.encodeEmail(email);

                                /* If no user exists, make a user */
                                final Firebase userLocation = new Firebase(Constants.FIREBASE_URL_USERS).child(encodedEmail);
                                userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                        /* If nothing is there ...*/
                                            if (dataSnapshot.getValue() == null) {
                                                User newUser = new User(userName, encodedEmail, true, false);
                                                userLocation.setValue(newUser);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {
                                            firebaseError.getMessage();
                                        }
                                    });

//                                if (authData != null) {
//                                /* Go to main activity */
//                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    startActivity(intent);
//                                    finish();
//                                }
                            }
                            @Override
                            public void onAuthenticationError(FirebaseError firebaseError) {
                                switch (firebaseError.getCode()) {
                                    case FirebaseError.INVALID_EMAIL:
                                    case FirebaseError.USER_DOES_NOT_EXIST:
                                        //mEditTextEmailInput.setError(getString(R.string.error_message_email_issue));
                                        break;
                                    case FirebaseError.INVALID_PASSWORD:
                                        //mEditTextPasswordInput.setError(firebaseError.getMessage());
                                        break;
                                    case FirebaseError.NETWORK_ERROR:
                                        //showErrorToast(getString(R.string.error_message_failed_sign_in_no_network));
                                        break;
                                    default:
                                        //showErrorToast(firebaseError.toString());
                                }
                            }
                        });
                    }
                }.start();
            } else {
                data.toString();
            }
        }
    }
}
