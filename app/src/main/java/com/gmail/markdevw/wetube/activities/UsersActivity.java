package com.gmail.markdevw.wetube.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.adapters.NavigationDrawerAdapter;
import com.gmail.markdevw.wetube.adapters.UserItemAdapter;
import com.gmail.markdevw.wetube.api.model.TagItem;
import com.gmail.markdevw.wetube.api.model.UserItem;
import com.gmail.markdevw.wetube.fragments.DialogDismissInterface;
import com.gmail.markdevw.wetube.fragments.OkDialog;
import com.gmail.markdevw.wetube.fragments.ProfileDialogFragment;
import com.gmail.markdevw.wetube.fragments.YesNoDialog;
import com.gmail.markdevw.wetube.fragments.YesNoOkDialog;
import com.gmail.markdevw.wetube.services.ConnectionService;
import com.gmail.markdevw.wetube.services.MessageService;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.models.Blocked;
import com.parse.models.Friend;
import com.parse.models.WeTubeUser;
import com.parse.ui.ParseLoginBuilder;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Mark on 4/2/2015.
 */
public class UsersActivity extends ActionBarActivity implements UserItemAdapter.Delegate,
        AdapterView.OnItemSelectedListener, PopupMenu.OnMenuItemClickListener,
        NavigationDrawerAdapter.Delegate,
        DrawerLayout.DrawerListener, YesNoDialog.onYesNoDialogOptionClickedListener,
        YesNoOkDialog.onYesNoOkDialogOptionClickedListener, DialogDismissInterface {

    private static final int UNBLOCK = 0;
    private static final int BLOCK = 1;
    private static final int REMOVE_FRIEND = 2;
    private static final int SESSION = 3;
    private static final int FRIEND_ADD = 4;
    private static final int LOGOUT = 5;

    @Bind(R.id.activity_users_search_option)        Spinner searchOptions;
    @Bind(R.id.activity_users_nav_friends_sort)     Spinner friendsSort;
    @Bind(R.id.activity_users_search)               EditText searchField;
    @Bind(R.id.activity_users_send_button)          Button searchButton;
    @Bind(R.id.tb_activity_users)                   Toolbar toolbar;
    @Bind(R.id.srl_activity_users)                  SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.rv_activity_users)                   RecyclerView recyclerView;
    @Bind(R.id.rv_nav_activity_users)               RecyclerView navigationRecyclerView;
    @Bind(R.id.dl_activity_users)                   DrawerLayout drawerLayout;

    private final long LOGIN_TIME = System.currentTimeMillis();

    private Intent mMessageServiceIntent;
    private Intent mConnectionServiceIntent;
    private ProgressDialog mProgressDialog;
    private BroadcastReceiver sinchReceiver;
    private UserItemAdapter mUserItemAdapter;
    private ArrayAdapter<String> mUserTagsAdapter;
    private int mTagSelected;
    private String mSearchOptionSelected;
    private String mSortOptionSelected;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationDrawerAdapter mNavigationDrawerAdapter;
    private ServiceConnection mServiceConnection;
    private MessageService.MessageServiceInterface mMessageService;
    private MessageClientListener mMessageClientListener;
    private UserItem mClickedUser;
    private Queue<Message> mMessageQueue;
    private DialogFragment mDialogFragment;
    boolean mIsFirstMessage = true;
    boolean mIsBlocking;
    private boolean mIsLaunch = true;
    private int mLaunchSpinnerCount;
    private String mMsgSplitter = "=-=-=";
    private HashMap<String, String> mMessages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        ButterKnife.bind(this);

        WeTubeApplication.getSharedDataSource().setUsersActivity(this);

        mMessageQueue = new LinkedBlockingQueue<>();
        mMessages = new HashMap<>();

        initToolbar();
        initUserListRecyclerView();
        initUserSearchSpinner();
        initNavigationDrawer();
        initFriendsListSpinner();
        initUserTagsAdapter();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchField.getText().toString().isEmpty()) {
                    Toast.makeText(UsersActivity.this, R.string.enter_a_search_first, Toast.LENGTH_LONG).show();
                } else {
                    swipeRefreshLayout.setRefreshing(true);
                    if (mSearchOptionSelected.equals(getString(R.string.name))) {
                        searchByName();
                    } else {
                        searchByTag();
                    }
                }
            }
        });

        Intent i = getIntent();
        final int connectionLossCheck = i.getIntExtra(getString(R.string.connection_loss), 0);

        SharedPreferences sharedpreferences = getSharedPreferences(
                getString(R.string.shared_prefs),
                Context.MODE_PRIVATE);
        String userKey = getString(R.string.user_key);
        String passKey = getString(R.string.pass_key);
        if(sharedpreferences.contains(userKey) && (sharedpreferences.contains(passKey))){
            String user = sharedpreferences.getString(userKey, "fa");
            String pass = sharedpreferences.getString(passKey, "fa");
            if(user.equals("fa") || pass.equals("fa")){
                Toast.makeText(this, R.string.failed_to_retrieve_login_info, Toast.LENGTH_LONG).show();
                displayParseLoginUI(connectionLossCheck);
            }else{
                ParseUser.logInInBackground(user, pass, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if(parseUser != null){
                            showSpinner();
                            loginSuccess();
                        }else{
                            if(e != null){
                                loginFail(connectionLossCheck);
                            }
                        }
                    }
                });
            }
        }else{
            displayParseLoginUI(connectionLossCheck);
        }
    }

    public void displayParseLoginUI(int connectionLossCheck) {
        ParseLoginBuilder builder = new ParseLoginBuilder(UsersActivity.this, connectionLossCheck);
        startActivityForResult(builder.build(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 0){
            finish();
        }else{
            drawerLayout.setVisibility(View.VISIBLE);
            mMessageServiceIntent = new Intent(UsersActivity.this, MessageService.class);
            mConnectionServiceIntent = new Intent(UsersActivity.this, ConnectionService.class);
            showSpinner();
            startService(mMessageServiceIntent);
            startService(mConnectionServiceIntent);

            getLoggedInUsers();
            getFriends();
            getUserTags();

            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("user", WeTubeUser.getCurrentUser().getObjectId());
            installation.saveInBackground();

            swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getLoggedInUsers();
                }
            });
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        WeTubeApplication.getSharedDataSource().setVideoActivity(false);
        mIsFirstMessage = true;
    }

    public void loginFail(int connectionLossCheck) {
        Toast.makeText(UsersActivity.this, com.parse.ui.R.string.com_parse_ui_parse_login_failed_unknown_toast, Toast.LENGTH_LONG).show();
        SharedPreferences sharedpreferences = getSharedPreferences
                ("MyPrefs", Context.MODE_PRIVATE);
        sharedpreferences.edit()
                .clear()
                .commit();

        displayParseLoginUI(connectionLossCheck);
    }

    public void loginSuccess() {
        mMessageServiceIntent = new Intent(UsersActivity.this, MessageService.class);
        mConnectionServiceIntent = new Intent(UsersActivity.this, ConnectionService.class);
        startService(mMessageServiceIntent);
        startService(mConnectionServiceIntent);

        getLoggedInUsers();
        getFriends();
        getUserTags();

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", WeTubeUser.getCurrentUser().getObjectId());
        installation.saveInBackground();

        initSwipeRefresh();
    }

    public void initUserTagsAdapter() {
        mUserTagsAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
    }

    private void showSpinner() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.show();

        sinchReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("success", false);
                mProgressDialog.dismiss();
                if (!success) {
                    Toast.makeText(getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
                }else{
                    drawerLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "You are now logged in as " +
                            WeTubeUser.getCurrentUser().getUsername(), Toast.LENGTH_LONG).show();
                    WeTubeUser user = (WeTubeUser) WeTubeUser.getCurrentUser();
                    user.setLoggedStatus(true);
                    user.setSessionStatus(false);
                    user.saveInBackground();

                    mServiceConnection = new MyServiceConnection();
                    bindService(new Intent(UsersActivity.this, MessageService.class), mServiceConnection, BIND_AUTO_CREATE);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(sinchReceiver, new IntentFilter("com.gmail.markdevw.wetube.activities.UsersActivity"));
    }

    public void initSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoggedInUsers();
            }
        });
    }

    public void initUserListRecyclerView() {
        mUserItemAdapter = new UserItemAdapter();
        mUserItemAdapter.setDelegate(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mUserItemAdapter);
    }

    public void initFriendsListSpinner() {
        mSortOptionSelected = getString(R.string.sort_default);
        ArrayAdapter<CharSequence> friendsSpinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                android.R.layout.simple_spinner_item);
        friendsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        friendsSort.setAdapter(friendsSpinnerAdapter);
        friendsSort.setOnItemSelectedListener(this);
    }

    public void initUserSearchSpinner() {
        mSearchOptionSelected = getString(R.string.name);
        ArrayAdapter<CharSequence> searchSpinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.search_options,
                android.R.layout.simple_spinner_item);
        searchSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchOptions.setAdapter(searchSpinnerAdapter);
        searchOptions.setOnItemSelectedListener(this);
    }

    public void initNavigationDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
        drawerLayout.setDrawerListener(this);

        mNavigationDrawerAdapter = new NavigationDrawerAdapter();
        mNavigationDrawerAdapter.setDelegate(this);

        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        navigationRecyclerView.setAdapter(mNavigationDrawerAdapter);
    }

    public void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void getLoggedInUsers(){
        if(WeTubeApplication.getSharedDataSource().getUsers().size() > 0){
            WeTubeApplication.getSharedDataSource().getUsers().clear();
        }

        ParseCloud.callFunctionInBackground("getLoggedInUsers", generateParams(), new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(final List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    int size = userList.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            final WeTubeUser user = (WeTubeUser) userList.get(i);
                            WeTubeApplication.getSharedDataSource().getUsers().add(new UserItem(user.getUsername(), user.getObjectId(),
                                    user.getSessionStatus(), user.getLoggedStatus(), false));
                        }
                    }
                    mUserItemAdapter.notifyDataSetChanged();
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public HashMap<String, Object> generateParams(){
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());

        Random rand = new Random();
        int n = rand.nextInt(7);

        switch(n) {
            case 0:
                params.put("ascending", "objectId");
                break;
            case 1:
                params.put("ascending", "username");
                break;
            case 2:
                params.put("ascending", "updatedAt");
                break;
            case 3:
                params.put("ascending", "createdAt");
                break;
            case 4:
                params.put("descending", "objectId");
                break;
            case 5:
                params.put("descending", "username");
                break;
            case 6:
                params.put("descending", "updatedAt");
                break;
            case 7:
                params.put("descending", "createdAt");
                break;
        }
        return params;
    }

    public void getFriends(){
        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsAvailableTwo", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    int size = userList.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }
                    }

                    ParseCloud.callFunctionInBackground("getFriendsUnavailableTwo", params, new FunctionCallback<List<ParseUser>>() {
                        @Override
                        public void done(List<ParseUser> userList2, com.parse.ParseException e) {
                            if (e == null) {
                                int size = userList2.size();
                                if (size > 0) {
                                    for (int i = 0; i < size; i++) {
                                        WeTubeUser friend = (WeTubeUser) userList2.get(i);

                                        WeTubeApplication.getSharedDataSource().getFriends()
                                                .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                                        friend.getSessionStatus(), friend.getLoggedStatus(), true));
                                    }
                                }

                                ParseCloud.callFunctionInBackground("getFriendsOfflineTwo", params, new FunctionCallback<List<ParseUser>>() {
                                    @Override
                                    public void done(List<ParseUser> userList3, com.parse.ParseException e) {
                                        int size = userList3.size();
                                        if (size > 0) {
                                            for (int i = 0; i < size; i++) {
                                                WeTubeUser friend = (WeTubeUser) userList3.get(i);

                                                WeTubeApplication.getSharedDataSource().getFriends()
                                                        .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                                                friend.getSessionStatus(), friend.getLoggedStatus(), true));
                                            }
                                        }
                                        mNavigationDrawerAdapter.notifyDataSetChanged();
                                        WeTubeApplication.getSharedDataSource().setFriendsSize(WeTubeApplication.getSharedDataSource().getFriends().size());
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getOnlineFriends(){
        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsAvailableTwo", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    int size = userList.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }

                    }

                    ParseCloud.callFunctionInBackground("getFriendsUnavailableTwo", params, new FunctionCallback<List<ParseUser>>() {
                        @Override
                        public void done(List<ParseUser> userList2, com.parse.ParseException e) {
                            int size = userList2.size();
                            if (e == null) {
                                if (size > 0) {
                                    for (int i = 0; i < size; i++) {
                                        WeTubeUser friend = (WeTubeUser) userList2.get(i);

                                        WeTubeApplication.getSharedDataSource().getFriends()
                                                .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                                        friend.getSessionStatus(), friend.getLoggedStatus(), true));
                                    }
                                }
                                mNavigationDrawerAdapter.notifyDataSetChanged();
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                }
                            }
                        }
                    });
                } else {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getOfflineFriends(){
        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsOfflineTwo", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    int size = userList.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }

                    }
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                } else {
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getAvailableFriends(){
        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsAvailableTwo", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    int size = userList.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }

                    }
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                } else {
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getUnavailableFriends(){
        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsUnavailableTwo", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    int size = userList.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }
                    }
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                } else {
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getAlphabeticFriends() {
        if (WeTubeApplication.getSharedDataSource().getFriends().size() > 0) {
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsAtoZTwo", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    int size = userList.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                            .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                    friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }
                    }
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                } else {
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onItemClicked(UserItemAdapter itemAdapter, final UserItem userItem, final View view, final int index) {
        try {
            mClickedUser = userItem;
            ParseQuery<Blocked> query = ParseQuery.getQuery("Blocked");
            query.whereEqualTo("blockedBy", WeTubeUser.getCurrentUser().getObjectId());
            query.whereEqualTo("userId", userItem.getId());
            query.findInBackground(new FindCallback<Blocked>() {
                @Override
                public void done(final List<Blocked> list, ParseException e) {
                    if(list != null){
                        if (list.size() > 0) {
                            mDialogFragment = createYesNoDialog("You have " + mClickedUser.getName() + " blocked. Do you want to unblock this user?",
                                    UNBLOCK,
                                    list.get(0),
                                    null);
                        } else {
                            ParseQuery<Blocked> query = ParseQuery.getQuery("Blocked");
                            query.whereEqualTo("userId", WeTubeUser.getCurrentUser().getObjectId());
                            query.whereEqualTo("blockedBy", userItem.getId());
                            query.findInBackground(new FindCallback<Blocked>() {
                                @Override
                                public void done(List<Blocked> list, ParseException e) {
                                    if (list.size() > 0) {
                                         createOkDialog(mClickedUser.getName() + " has you blocked");
                                    } else {
                                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                                        query.whereEqualTo("objectId", mClickedUser.getId());
                                        query.findInBackground(new FindCallback<ParseUser>() {
                                            @Override
                                            public void done(List<ParseUser> list, ParseException e) {
                                                if (e == null && list.size() > 0) {
                                                    WeTubeUser user = (WeTubeUser) list.get(0);
                                                    mClickedUser.setOnlineStatus(user.getLoggedStatus());
                                                    mClickedUser.setSessionStatus(user.getSessionStatus());
                                                    mUserItemAdapter.notifyItemChanged(index);

                                                    ParseQuery<Friend> q1 = ParseQuery.getQuery("Friend");
                                                    q1.whereEqualTo("friend1", user);
                                                    q1.whereEqualTo("friend2", ParseUser.getCurrentUser());

                                                    ParseQuery<Friend> q2 = ParseQuery.getQuery("Friend");
                                                    q2.whereEqualTo("friend2", user);
                                                    q2.whereEqualTo("friend1", ParseUser.getCurrentUser());

                                                    List<ParseQuery<Friend>> queries = new ArrayList<ParseQuery<Friend>>();
                                                    queries.add(q1);
                                                    queries.add(q2);

                                                    ParseQuery<Friend> query = ParseQuery.or(queries);
                                                    query.findInBackground(new FindCallback<Friend>() {
                                                        @Override
                                                        public void done(List<Friend> friends, ParseException e) {
                                                            if (e == null) {
                                                                if (friends.size() > 0) {
                                                                    mClickedUser.setFriendStatus(true);
                                                                }else{
                                                                    mClickedUser.setFriendStatus(false);
                                                                }
                                                                PopupMenu popMenu = new PopupMenu(UsersActivity.this, view);

                                                                if (mClickedUser.getOnlineStatus() && mClickedUser.getSessionStatus() && mClickedUser.getFriendStatus()) {
                                                                    getMenuInflater().inflate(R.menu.activity_users_popup_friend_unavailable_offline, popMenu.getMenu());
                                                                } else if(mClickedUser.getOnlineStatus() && !mClickedUser.getSessionStatus() && mClickedUser.getFriendStatus()) {
                                                                    getMenuInflater().inflate(R.menu.activity_users_popup_friend, popMenu.getMenu());
                                                                }else if(mClickedUser.getSessionStatus()){
                                                                    getMenuInflater().inflate(R.menu.activity_users_popup_unavailable, popMenu.getMenu());
                                                                }else if(mClickedUser.getOnlineStatus()) {
                                                                    getMenuInflater().inflate(R.menu.activity_users_popup, popMenu.getMenu());
                                                                }else{
                                                                    getMenuInflater().inflate(R.menu.activity_users_popup_offline, popMenu.getMenu());
                                                                }
                                                                popMenu.setOnMenuItemClickListener(UsersActivity.this);
                                                                popMenu.show();

                                                            }else{
                                                                Toast.makeText(UsersActivity.this, "Error searching for " + userItem.getName(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }else{
                        Toast.makeText(UsersActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch(NullPointerException e ){
            //causes crash if user loses connection and tries to click on users
        }
    }

    public void createOkDialog(String title) {
        DialogFragment dialog = OkDialog.newInstance(title);
        dialog.show(getSupportFragmentManager(), "Dialog");

        mDialogFragment = dialog;
    }

    public DialogFragment createYesNoDialog(String title, final int resultType, Blocked blocked, UserItem userItem) {
        YesNoDialog dialog = new YesNoDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("yes", "Yes");
        args.putString("no", "No");
        args.putInt("resultType", resultType);
        dialog.setArguments(args);


        if(blocked != null){
            dialog.setBlocked(blocked);
        }

        if(userItem != null){
            dialog.setUser(userItem);
        }

        dialog.show(getSupportFragmentManager(), "Dialog");

        return dialog;
    }

    @Override
    public void onNavItemClicked(NavigationDrawerAdapter itemAdapter, final UserItem userItem, final View view, final int index) {
        mClickedUser = userItem;

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", mClickedUser.getId());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e == null && list.size() > 0) {
                    WeTubeUser user = (WeTubeUser) list.get(0);
                    mClickedUser.setOnlineStatus(user.getLoggedStatus());
                    mClickedUser.setSessionStatus(user.getSessionStatus());
                    mNavigationDrawerAdapter.notifyItemChanged(index);

                    PopupMenu popMenu = new PopupMenu(UsersActivity.this, view);

                    if (user.getSessionStatus() || !user.getLoggedStatus()) {
                        getMenuInflater().inflate(R.menu.activity_users_popup_friend_unavailable_offline, popMenu.getMenu());
                    } else {
                        getMenuInflater().inflate(R.menu.activity_users_popup_friend, popMenu.getMenu());
                    }

                    popMenu.setOnMenuItemClickListener(UsersActivity.this);
                    popMenu.show();
                } else {
                    Toast.makeText(UsersActivity.this, "Error searching for " + userItem.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        switch (menuItem.getItemId()) {
            case R.id.popup_session :
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("objectId", mClickedUser.getId());
                query.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> parseUsers, ParseException e) {
                       if(parseUsers.size() > 0 && e == null) {
                           WeTubeUser user = (WeTubeUser) parseUsers.get(0);
                           if (user.getLoggedStatus() && !user.getSessionStatus()) {
                               mMessageService.sendMessage(mClickedUser.getId(), mMsgSplitter + "startsession" + mMsgSplitter + ParseUser.getCurrentUser().getUsername() + mMsgSplitter
                                       + ParseUser.getCurrentUser().getObjectId());
                           } else {
                               mClickedUser.setOnlineStatus(user.getLoggedStatus());
                               mClickedUser.setSessionStatus(user.getSessionStatus());
                               mUserItemAdapter.notifyDataSetChanged();
                               mNavigationDrawerAdapter.notifyDataSetChanged();
                               if (!user.getLoggedStatus()) {
                                   Toast.makeText(UsersActivity.this, user.getUsername() + " is offline", Toast.LENGTH_LONG).show();
                               } else {
                                   Toast.makeText(UsersActivity.this, user.getUsername() + " is already in a session", Toast.LENGTH_LONG).show();
                               }
                           }
                       }else {
                           Toast.makeText(UsersActivity.this, "Error finding user: " + e, Toast.LENGTH_SHORT).show();
                       }
                    }
                });
                break;
            case R.id.popup_add :
                if(WeTubeApplication.getSharedDataSource().getFriends().size() < WeTubeApplication.getSharedDataSource().getMaxFriends()){
                    mMessageService.sendMessage(mClickedUser.getId(), mMsgSplitter + "friendadd" + mMsgSplitter + ParseUser.getCurrentUser().getUsername() + mMsgSplitter
                            + ParseUser.getCurrentUser().getObjectId());
                }else{
                    Toast.makeText(this, "Your friends list is full (100)", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.popup_profile:
                params.put("clickedId", mClickedUser.getId());
                params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
                ParseCloud.callFunctionInBackground("commonTags", params, new FunctionCallback<List<String>>() {
                    @Override
                    public void done(final List<String> comTags, com.parse.ParseException e) {
                        if (e == null) {
                            HashMap<String, Object> params = new HashMap<String, Object>();
                            params.put("clickedId", mClickedUser.getId());
                            params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
                            ParseCloud.callFunctionInBackground("uncommonTags", params, new FunctionCallback<List<String>>() {
                                @Override
                                public void done(List<String> uncomTags, com.parse.ParseException e) {
                                    if (e == null) {
                                        WeTubeApplication.getSharedDataSource().getCommonTags().clear();
                                        int size = comTags.size();
                                        for(int i = 0; i < size; i++){
                                            WeTubeApplication.getSharedDataSource().getCommonTags().add(new TagItem(comTags.get(i)));
                                        }

                                        size = uncomTags.size();
                                        WeTubeApplication.getSharedDataSource().getUncommonTags().clear();
                                        for(int i = 0; i < size; i++){
                                            WeTubeApplication.getSharedDataSource().getUncommonTags().add(new TagItem(uncomTags.get(i)));
                                        }

                                        ProfileDialogFragment pdf = new ProfileDialogFragment();
                                        pdf.show(getFragmentManager(), "Profile");
                                    } else {
                                        Toast.makeText(WeTubeApplication.getSharedInstance(),
                                                "Error: " + e + ". Failed to start session with " + mClickedUser.getName(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(WeTubeApplication.getSharedInstance(),
                                    "Error: " + e + ". Failed to start session with " + mClickedUser.getName(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            case R.id.popup_remove :
                final UserItem friend = mClickedUser;
                mDialogFragment = createYesNoDialog("Are you sure you want to remove " + friend.getName() + " ?", REMOVE_FRIEND, null, friend);
        }
        return false;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        try{
            stopService(new Intent(this, MessageService.class));
            stopService(new Intent(this, ConnectionService.class));
            mMessageService.removeMessageClientListener(mMessageClientListener);
            unbindService(mServiceConnection);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(sinchReceiver);
        }catch(NullPointerException e){

        }
    }

    @Override
    public void onRestart(){
        super.onRestart();

        mIsFirstMessage = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_users, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch(item.getItemId()) {
            case R.id.action_profile:
                AlertDialog.Builder categBuilder = new AlertDialog.Builder(this);

                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.tag_dialog, null);

                ImageButton add = (ImageButton) dialogView.findViewById(R.id.tag_dialog_add_button);
                ImageButton minus = (ImageButton) dialogView.findViewById(R.id.tag_dialog_minus_button);

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder newCateg = new AlertDialog.Builder(UsersActivity.this);
                        newCateg.setTitle("Add a new tag");

                        final EditText input = new EditText(UsersActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        newCateg.setView(input);

                        newCateg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                if(mUserTagsAdapter.getPosition(input.getText().toString()) != -1){
                                    Toast.makeText(getApplicationContext(), input.getText().toString() + " already exists", Toast.LENGTH_LONG).show();
                                }else if(input.getText().toString().isEmpty()){
                                    Toast.makeText(getApplicationContext(), "Enter a tag first", Toast.LENGTH_LONG).show();
                                }else{
                                    HashMap<String, Object> params = new HashMap<String, Object>();
                                    params.put("tag", input.getText().toString());
                                    params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
                                    ParseCloud.callFunctionInBackground("addTag", params, new FunctionCallback<String>() {
                                        @Override
                                        public void done(String mapObject, com.parse.ParseException e) {

                                        }
                                    });
                                    mUserTagsAdapter.add(input.getText().toString());
                                    mUserTagsAdapter.notifyDataSetChanged();
                                }
                                InputMethodManager imm = (InputMethodManager)UsersActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                dialog.dismiss();
                            }
                        });
                        newCateg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        newCateg.show();

                        input.requestFocus();
                        InputMethodManager imm = (InputMethodManager)UsersActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                });
                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mUserTagsAdapter.getCount() == 0){
                            Toast.makeText(getApplicationContext(), "No tags to remove", Toast.LENGTH_LONG).show();
                        }else if(mTagSelected >= mUserTagsAdapter.getCount()){
                            Toast.makeText(getApplicationContext(), "Select a tag before deleting", Toast.LENGTH_LONG).show();
                        }else if(mUserTagsAdapter.getCount() > 0) {

                            HashMap<String, Object> params = new HashMap<String, Object>();
                            params.put("tag", mUserTagsAdapter.getItem(mTagSelected));
                            params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
                            ParseCloud.callFunctionInBackground("removeTag", params, new FunctionCallback<String>() {
                                @Override
                                public void done(String mapObject, com.parse.ParseException e) {

                                }
                            });
                            mUserTagsAdapter.remove(mUserTagsAdapter.getItem(mTagSelected));
                            mUserTagsAdapter.notifyDataSetChanged();
                        }
                    }
                });

                categBuilder.setView(dialogView);
                categBuilder.setSingleChoiceItems(mUserTagsAdapter, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mTagSelected = which;
                    }
                });
                categBuilder.show();

        }
        return super.onOptionsItemSelected(item);
    }

    public void getUserTags(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", currentUserId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    WeTubeUser user = (WeTubeUser) userList.get(0);
                    if (user.getList("tags") != null) {
                        List<String> tags = user.getList("tags");
                        mUserTagsAdapter.addAll(tags);
                        mUserTagsAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        if(!mIsLaunch){
            String selection = (String) parent.getItemAtPosition(pos);

            if(selection.equals("Name") || selection.equals("Tag")){
                mSearchOptionSelected = selection;
            }else{
                mSortOptionSelected = selection;

                friendsRefreshProgress();

                if(mSortOptionSelected.equals("Default")){
                    getFriends();
                }else if(mSortOptionSelected.equals("Online")){
                    getOnlineFriends();
                }else if(mSortOptionSelected.equals("Offline")){
                    getOfflineFriends();
                }else if(mSortOptionSelected.equals("Available")){
                    getAvailableFriends();
                }else if(mSortOptionSelected.equals("Unavailable")){
                    getUnavailableFriends();
                } else if(mSortOptionSelected.equals("A-Z")){
                    getAlphabeticFriends();

                }
            }
        }else{
            if(mLaunchSpinnerCount <1){
                mLaunchSpinnerCount++;
            }else{
                mIsLaunch = false;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void searchByTag(){
        final String currentUserId = ParseUser.getCurrentUser().getObjectId();
        final WeTubeUser w = (WeTubeUser) ParseUser.getCurrentUser();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(searchField.getText().toString().split(" ")));
        query.whereContainedIn("tags", tokens);
        query.whereEqualTo("isLoggedIn", true);
        query.orderByAscending("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    int size = userList.size();
                    if (size == 0) {
                        WeTubeApplication.getSharedDataSource().getUsers().clear();
                        mUserItemAdapter.notifyDataSetChanged();
                        Toast.makeText(WeTubeApplication.getSharedInstance(),
                                "Could not find any logged in users with that tag",
                                Toast.LENGTH_LONG).show();
                    }
                    WeTubeApplication.getSharedDataSource().getUsers().clear();
                    for (int i = 0; i < size; i++) {
                        final WeTubeUser user = (WeTubeUser) userList.get(i);

                        WeTubeApplication.getSharedDataSource().getUsers()
                                .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), false));
                    }

                    for(int i = 0; i < size; i++) {
                        final WeTubeUser user = (WeTubeUser) userList.get(i);

                        ParseQuery<Friend> q1 = ParseQuery.getQuery("Friend");
                        q1.whereEqualTo("friend1", user);
                        q1.whereEqualTo("friend2", w);

                        ParseQuery<Friend> q2 = ParseQuery.getQuery("Friend");
                        q2.whereEqualTo("friend2", user);
                        q2.whereEqualTo("friend1", w);

                        List<ParseQuery<Friend>> queries = new ArrayList<ParseQuery<Friend>>();
                        queries.add(q1);
                        queries.add(q2);

                        ParseQuery<Friend> query = ParseQuery.or(queries);
                        final int j = i;
                        query.findInBackground(new FindCallback<Friend>() {
                            @Override
                            public void done(List<Friend> list, ParseException e) {
                                if (list.size() > 0) {
                                    WeTubeApplication.getSharedDataSource().getUsers().get(j).setFriendStatus(true);
                                }
                                if (j == WeTubeApplication.getSharedDataSource().getUsers().size() - 1) {
                                    mUserItemAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void searchByName() {
        String currentUserId = ParseUser.getCurrentUser().getObjectId();
        final WeTubeUser w = (WeTubeUser) ParseUser.getCurrentUser();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        query.whereEqualTo("isLoggedIn", true);
        query.whereStartsWith("username", searchField.getText().toString());
        query.orderByAscending("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    int size = userList.size();
                    if (size == 0) {
                        WeTubeApplication.getSharedDataSource().getUsers().clear();
                        mUserItemAdapter.notifyDataSetChanged();
                        Toast.makeText(WeTubeApplication.getSharedInstance(),
                                "Could not find any logged in users with that name",
                                Toast.LENGTH_LONG).show();
                    }

                    WeTubeApplication.getSharedDataSource().getUsers().clear();
                    for (int i = 0; i < size; i++) {
                        final WeTubeUser user = (WeTubeUser) userList.get(i);

                        WeTubeApplication.getSharedDataSource().getUsers()
                                .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), false));
                    }

                    for (int i = 0; i < size; i++) {
                        final WeTubeUser user = (WeTubeUser) userList.get(i);

                        ParseQuery<Friend> q1 = ParseQuery.getQuery("Friend");
                        q1.whereEqualTo("friend1", user);
                        q1.whereEqualTo("friend2", w);

                        ParseQuery<Friend> q2 = ParseQuery.getQuery("Friend");
                        q2.whereEqualTo("friend2", user);
                        q2.whereEqualTo("friend1", w);

                        List<ParseQuery<Friend>> queries = new ArrayList<ParseQuery<Friend>>();
                        queries.add(q1);
                        queries.add(q2);

                        ParseQuery<Friend> query = ParseQuery.or(queries);
                        final int j = i;
                        query.findInBackground(new FindCallback<Friend>() {
                            @Override
                            public void done(List<Friend> list, ParseException e) {
                                if (list.size() > 0) {
                                    WeTubeApplication.getSharedDataSource().getUsers().get(j).setFriendStatus(true);
                                }
                                if (j == WeTubeApplication.getSharedDataSource().getUsers().size() - 1) {
                                    mUserItemAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        mDialogFragment = createYesNoOkDialog("Are you sure you want to exit?", null, null, "Logout", "No", "Exit", LOGOUT);
    }

    public void sessionEndedDialog(String name){
        createOkDialog("Session End: " + name + " had lost connection");
    }

    @Override
    public void onYesNoDialogFragmentResult(int resultType, int which, Blocked blocked, final UserItem user) {
        switch(resultType){
            case UNBLOCK:
                if (which == -1) {
                    blocked.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Toast.makeText(UsersActivity.this, mClickedUser.getName() + " has been unblocked", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(UsersActivity.this, "Failed to unblock " + mClickedUser.getName(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;
            case BLOCK:
                if(which == -1){
                    final String id = user.getId();
                    clearDialogsById(id);
                    Blocked block = new Blocked(ParseUser.getCurrentUser().getObjectId(), id);
                    block.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            mMessageService.sendMessage(id, mMsgSplitter + "blockuser" + mMsgSplitter + ParseUser.getCurrentUser().getUsername() + mMsgSplitter
                                    + ParseUser.getCurrentUser().getObjectId());
                        }
                    });
                }
                break;
            case REMOVE_FRIEND:
                if (which == -1) {
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("objectId", user.getId());
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> parseUsers, ParseException e) {
                            if(e == null && parseUsers.size() > 0){
                                ParseQuery<Friend> q1 = ParseQuery.getQuery("Friend");
                                q1.whereEqualTo("friend1", parseUsers.get(0));
                                q1.whereEqualTo("friend2", ParseUser.getCurrentUser());

                                ParseQuery<Friend> q2 = ParseQuery.getQuery("Friend");
                                q2.whereEqualTo("friend2", parseUsers.get(0));
                                q2.whereEqualTo("friend1", ParseUser.getCurrentUser());

                                List<ParseQuery<Friend>> queries = new ArrayList<ParseQuery<Friend>>();
                                queries.add(q1);
                                queries.add(q2);

                                ParseQuery<Friend> query = ParseQuery.or(queries);
                                query.findInBackground(new FindCallback<Friend>() {
                                    @Override
                                    public void done(List<Friend> list, ParseException e) {
                                        if(e == null && list.size() > 0){
                                            list.get(0).deleteInBackground(new DeleteCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    WeTubeApplication.getSharedDataSource().getFriends().remove(user);
                                                    WeTubeApplication.getSharedDataSource().setFriendsSize(WeTubeApplication.getSharedDataSource().getFriendsSize()-1);
                                                    mNavigationDrawerAdapter.notifyDataSetChanged();
                                                    Toast.makeText(UsersActivity.this, user.getName() + " has been removed from your friends list", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
                break;
            case FRIEND_ADD:
                final String id = user.getId();
                if(which == -1){
                    mMessageService.sendMessage(id, mMsgSplitter + "friendfull" + mMsgSplitter + ParseUser.getCurrentUser().getUsername());
                }else if(which == -2){
                    clearDialogsById(id);
                    Blocked block = new Blocked(ParseUser.getCurrentUser().getObjectId(), id);
                    block.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            mMessageService.sendMessage(id, mMsgSplitter + "blockuser" + mMsgSplitter + ParseUser.getCurrentUser().getUsername() + mMsgSplitter
                                    + ParseUser.getCurrentUser().getObjectId());
                        }
                    });
                }
                break;
        }
    }

    @Override
    public void onYesNoOkDialogFragmentResult(int resultType, int which, final String name, final String id) {
        switch(resultType){
            case SESSION:
                if (which == -1) {
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("objectId", id);
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> list, ParseException e) {
                            if (list.size() > 0 && e == null) {
                                WeTubeUser recip = (WeTubeUser) list.get(0);
                                if (!recip.getSessionStatus() && recip.getLoggedStatus()) {
                                    mMessageService.sendMessage(id, mMsgSplitter + "sessionaccept" + mMsgSplitter + ParseUser.getCurrentUser().getUsername() + mMsgSplitter +
                                            ParseUser.getCurrentUser().getObjectId());

                                    WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                                    user.setSessionStatus(true);
                                    user.saveInBackground();
                                    WeTubeApplication.getSharedDataSource().setSessionController(false);
                                    WeTubeApplication.getSharedDataSource().setCurrentRecipient(new UserItem(name, id));
                                } else {
                                    if (!recip.getLoggedStatus()) {
                                        Toast.makeText(UsersActivity.this, "Session failed to start. User has gone offline", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(UsersActivity.this, "Session failed to start. User is in another session", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(UsersActivity.this, "Error finding user", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else if(which == -2){
                    mMessageService.sendMessage(id, mMsgSplitter + "sessiondecline" + mMsgSplitter + ParseUser.getCurrentUser().getUsername());
                } else if(which == -3){
                    mIsBlocking = true;
                    mDialogFragment = createYesNoDialog("Are you sure you want to block " + name + " ?", BLOCK, null, new UserItem(name, id));
                }
                break;
            case FRIEND_ADD:
                if(which == -1){
                    final WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                    mMessageService.sendMessage(id, mMsgSplitter + "friendaccept" + mMsgSplitter + user.getUsername() + mMsgSplitter + user.getObjectId());
                }else if(which == -2){
                    mMessageService.sendMessage(id, mMsgSplitter + "frienddecline" + mMsgSplitter + ParseUser.getCurrentUser().getUsername());
                }else if(which == -3){
                    mIsBlocking = true;
                    mDialogFragment = createYesNoDialog("Are you sure you want to block " + name + " ?", BLOCK, null, new UserItem(name, id));
                }
                break;
            case LOGOUT:
                if(which == -1){
                    SharedPreferences sharedpreferences = getSharedPreferences
                            ("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.clear();
                    editor.commit();

                    WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                    user.setLoggedStatus(false);
                    user.setSessionStatus(false);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            ParseUser.logOutInBackground(new LogOutCallback() {
                                @Override
                                public void done(ParseException e) {
                                    UsersActivity.this.moveTaskToBack(true);
                                    UsersActivity.this.finish();
                                }
                            });
                        }
                    });
                }else if(which == -3){
                    WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                    user.setLoggedStatus(false);
                    user.setSessionStatus(false);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            UsersActivity.this.moveTaskToBack(true);
                            UsersActivity.this.finish();
                        }
                    });
                }
        }
    }

    @Override
    public void dialogDismiss() {
        if(!mMessageQueue.isEmpty() && !mIsBlocking){
            showNextMessage();
        }
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mMessageService = (MessageService.MessageServiceInterface) iBinder;
            mMessageClientListener = new MyMessageClientListener();
            mMessageService.addMessageClientListener(mMessageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMessageService = null;
        }
    }

    private class MyMessageClientListener implements MessageClientListener {

        @Override
        public void onMessageFailed(MessageClient client, Message message, MessageFailureInfo failureInfo) {
            String msg = mMessages.get(failureInfo.getMessageId());
            if(msg.startsWith(mMsgSplitter + "friendadd")) {
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(mMsgSplitter)));
                String name = msgSplit.get(1);
                Toast.makeText(UsersActivity.this, "Failed to send friend request to " + name, Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + "friendaccept")){
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(mMsgSplitter)));
                String name = msgSplit.get(1);
                Toast.makeText(UsersActivity.this, "Failed to accept friend request from " + name, Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + "startsession")){
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(mMsgSplitter)));
                String name = msgSplit.get(1);
                Toast.makeText(UsersActivity.this, "Failed to send session request to " + name, Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + "sessionaccept")){
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(mMsgSplitter)));
                String name = msgSplit.get(1);
                Toast.makeText(UsersActivity.this, "Failed to start session with " + name, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            if(!WeTubeApplication.getSharedDataSource().isInVideoActivity()){
                Date time = message.getTimestamp();
                if(LOGIN_TIME < time.getTime()){
                    mMessageQueue.add(message);

                    if(!mIsFirstMessage){
                        if(mDialogFragment != null && !mDialogFragment.isVisible() && !mMessageQueue.isEmpty()){
                            showNextMessage();
                        }
                    }else{
                        mIsFirstMessage = false;
                        showNextMessage();
                    }
                }
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, final String recipientId) {
            mMessages.put(message.getMessageId(), message.getTextBody());
        }

        @Override
        public void onMessageDelivered(MessageClient client, final MessageDeliveryInfo deliveryInfo) {
            String msg = mMessages.get(deliveryInfo.getMessageId());
            if(msg != null){
                ArrayList<String> message = new ArrayList<String>(Arrays.asList(msg.split(mMsgSplitter)));
                if(msg.startsWith(mMsgSplitter + "friendaccept")){
                    WeTubeApplication.getSharedDataSource().setFriendsSize(WeTubeApplication.getSharedDataSource().getFriendsSize()+1);
                    final WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();

                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("objectId", deliveryInfo.getRecipientId());
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> parseUsers, ParseException e) {
                            final WeTubeUser friend = (WeTubeUser) parseUsers.get(0);
                            final String name = friend.getUsername();

                            Friend newFriend = new Friend(user, friend);
                            newFriend.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null){
                                        Toast.makeText(UsersActivity.this, name + " has been added to your friends list", Toast.LENGTH_SHORT).show();
                                        WeTubeApplication.getSharedDataSource().getFriends().add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                                friend.getSessionStatus(), friend.getLoggedStatus(), true));
                                        mNavigationDrawerAdapter.notifyDataSetChanged();

                                        int size = WeTubeApplication.getSharedDataSource().getUsers().size();
                                        for (int i = 0; i < size; i++) {
                                            if (WeTubeApplication.getSharedDataSource().getUsers().get(i).getName().equals(name)){
                                                WeTubeApplication.getSharedDataSource().getUsers().get(i).setFriendStatus(true);
                                                mUserItemAdapter.notifyItemChanged(i);
                                            }
                                        }
                                    }else{
                                        Toast.makeText(UsersActivity.this, "Failed to add " + name + " to your friends list", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });
                }else if(msg.startsWith(mMsgSplitter + "sessionaccept")){
                    Intent intent = new Intent(WeTubeApplication.getSharedInstance(), MainActivity.class);
                    startActivity(intent);
                }
                mMessages.remove(deliveryInfo.getMessageId());
            }
        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }

    public void showNextMessage() {
        if(mMessageQueue != null && mMessageQueue.size() > 0) {
            Message message = mMessageQueue.poll();
            ArrayList<String> msg = new ArrayList<String>(Arrays.asList(message.getTextBody().split(mMsgSplitter)));

            if (msg.get(1).equals("startsession")) {
                final String name = msg.get(2);
                final String id = msg.get(3);

                mDialogFragment = createYesNoOkDialog("Session request from " + name, name, id, "Accept", "Decline", "Block", SESSION);

            } else if (msg.get(1).equals("sessionaccept")) {
                WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                user.setSessionStatus(true);
                user.saveInBackground();

                WeTubeApplication.getSharedDataSource().setSessionController(true);
                WeTubeApplication.getSharedDataSource().setCurrentRecipient(new UserItem(msg.get(2), msg.get(3)));
                Intent intent = new Intent(WeTubeApplication.getSharedInstance(), MainActivity.class);
                startActivity(intent);
            } else if (msg.get(1).equals("sessiondecline")) {
                String name = msg.get(2);
                createOkDialog(name + " has declined your session request");
            } else if (msg.get(1).equals("friendadd")) {
                final String name = msg.get(2);
                final String id = msg.get(3);

                if(WeTubeApplication.getSharedDataSource().getFriendsSize() == WeTubeApplication.getSharedDataSource().getMaxFriends()){
                    DialogFragment dialog = new YesNoDialog();
                    Bundle b = new Bundle();
                    b.putString("title", "Friend request from " + name + " but your friend's list is full");
                    b.putString("yes", "Friend's list full");
                    b.putString("no", "Block");
                    b.putInt("resultType", FRIEND_ADD);

                    dialog.setArguments(b);
                    dialog.show(getSupportFragmentManager(), "Dialog");

                    mDialogFragment = dialog;
                }else{
                    mDialogFragment = createYesNoOkDialog("Friend request from " + name, name, id, "Accept", "Decline", "Block", FRIEND_ADD);
                }
            } else if (msg.get(1).equals("frienddecline")) {
                String name = msg.get(2);
                 createOkDialog(name + " has declined your friend request");
            } else if (msg.get(1).equals("friendfull")) {
                String name = msg.get(2);
                createOkDialog(name + "'s friends list is full");
            } else if (msg.get(1).equals("friendaccept")) {
                WeTubeApplication.getSharedDataSource().setFriendsSize(WeTubeApplication.getSharedDataSource().getFriendsSize()+1);
                final String name = msg.get(2);
                final String id = msg.get(3);

                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("objectId", id);
                query.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> list, ParseException e) {
                        if (list.size() > 0 && e == null) {
                            WeTubeUser friend = (WeTubeUser) list.get(0);

                            WeTubeApplication.getSharedDataSource().getFriends().add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                    friend.getSessionStatus(), friend.getLoggedStatus(), true));
                            mNavigationDrawerAdapter.notifyDataSetChanged();

                            int size = WeTubeApplication.getSharedDataSource().getUsers().size();
                            for (int i = 0; i < size; i++) {
                                if (WeTubeApplication.getSharedDataSource().getUsers().get(i).getName().equals(name)) {
                                    WeTubeApplication.getSharedDataSource().getUsers().get(i).setFriendStatus(true);
                                    mUserItemAdapter.notifyItemChanged(i);
                                }
                            }

                            createOkDialog(name + " accepted your friend request");
                        } else {
                            createOkDialog("Failed to add " + name + " to your friends list");
                        }
                    }
                });
            } else if (msg.get(1).equals("friendremove")) {
                WeTubeApplication.getSharedDataSource().setFriendsSize(WeTubeApplication.getSharedDataSource().getFriendsSize()-1);
                final String id = msg.get(3);

                int size = WeTubeApplication.getSharedDataSource().getFriends().size();
                for (int i = 0; i < size; i++) {
                    if (WeTubeApplication.getSharedDataSource().getFriends().get(i).getId().equals(id)) {
                        WeTubeApplication.getSharedDataSource().getFriends().remove(i);
                        mNavigationDrawerAdapter.notifyItemRemoved(i);
                        break;
                    }
                }

                size = WeTubeApplication.getSharedDataSource().getUsers().size();
                for (int i = 0; i < size; i++) {
                    if (WeTubeApplication.getSharedDataSource().getUsers().get(i).getId().equals(id)) {
                        WeTubeApplication.getSharedDataSource().getUsers().get(i).setFriendStatus(false);
                        mUserItemAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            } else if (msg.get(1).equals("blockuser")) {
                final String name = msg.get(2);

                createOkDialog(name + " has blocked you");
            } else if (msg.get(1).equals("unblock")) {
                final String name = msg.get(2);

                createOkDialog(name + " has unblocked you");
            }
        }
    }

    public DialogFragment createYesNoOkDialog(final String title, final String name, final String id,
                                    final String yes, final String no, final String ok,
                                    final int resultType) {
        YesNoOkDialog dialog = new YesNoOkDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("name", name);
        args.putString("id", id);
        args.putString("yes", yes);
        args.putString("no", no);
        args.putString("ok", ok);
        args.putInt("resultType", resultType);
        dialog.setArguments(args);

        dialog.show(getSupportFragmentManager(), "Dialog");

        return dialog;
    }

    public void clearDialogsById(String id){
        for(Message message : mMessageQueue) {
            ArrayList<String> msg = new ArrayList<String>(Arrays.asList(message.getTextBody().split(mMsgSplitter)));
            if(msg.get(3).equals(id)){
                mMessageQueue.remove(message);
            }
        }
        mIsBlocking = false;
        if(!mMessageQueue.isEmpty()){
            showNextMessage();
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        friendsRefreshProgress();
        switch(mSortOptionSelected){
            case "Default":
                getFriends();
                break;
            case "Online":
                getOnlineFriends();
                break;
            case "Offline":
                getOfflineFriends();
                break;
            case "Available":
                getAvailableFriends();
                break;
            case "Unavailable":
                getUnavailableFriends();
                break;
            case "A-Z":
                getAlphabeticFriends();
                break;
        }
    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    public void friendsRefreshProgress(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Refreshing friends list");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.show();
    }

    public void getMoreUsers(int skip, int limit){
        final WeTubeUser currentUser = (WeTubeUser) ParseUser.getCurrentUser();

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        params.put("skip", skip);
        params.put("limit", limit);
        ParseCloud.callFunctionInBackground("getMoreUsers", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    int size = userList.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            final WeTubeUser user = (WeTubeUser) userList.get(i);

                            ParseQuery<Friend> q1 = ParseQuery.getQuery("Friend");
                            q1.whereEqualTo("friend1", user);
                            q1.whereEqualTo("friend2", currentUser);

                            ParseQuery<Friend> q2 = ParseQuery.getQuery("Friend");
                            q2.whereEqualTo("friend2", user);
                            q2.whereEqualTo("friend1", currentUser);

                            List<ParseQuery<Friend>> queries = new ArrayList<ParseQuery<Friend>>();
                            queries.add(q1);
                            queries.add(q2);

                            ParseQuery<Friend> query = ParseQuery.or(queries);
                            final int j = i;
                            query.findInBackground(new FindCallback<Friend>() {
                                @Override
                                public void done(List<Friend> list, ParseException e) {
                                    List<UserItem> users = WeTubeApplication.getSharedDataSource().getUsers();
                                    users.add(new UserItem(user.getUsername(), user.getObjectId(),
                                            user.getSessionStatus(), user.getLoggedStatus(), false));
                                    if(e == null && list.size() > 0){
                                        users.get(users.size() - 1).setFriendStatus(true);
                                    }
                                    mUserItemAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                    mUserItemAdapter.notifyDataSetChanged();
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    mNavigationDrawerAdapter.notifyDataSetChanged();
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
