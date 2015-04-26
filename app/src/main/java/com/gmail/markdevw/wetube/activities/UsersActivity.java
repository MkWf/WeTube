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
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import com.gmail.markdevw.wetube.fragments.ProfileDialogFragment;
import com.gmail.markdevw.wetube.services.MessageService;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
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
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Mark on 4/2/2015.
 */
public class UsersActivity extends ActionBarActivity implements UserItemAdapter.Delegate, View.OnClickListener,
        AdapterView.OnItemSelectedListener, PopupMenu.OnMenuItemClickListener,
        NavigationDrawerAdapter.Delegate, DialogInterface.OnDismissListener,
        DrawerLayout.DrawerListener{

    private Intent serviceIntent;
    private ProgressDialog progressDialog;
    private BroadcastReceiver receiver = null;
    private RecyclerView recyclerView;
    private Button logout;
    private UserItemAdapter userItemAdapter;
    private Handler handler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private ArrayAdapter<String> adapter;
    private EditText searchField;
    private Button searchButton;
    private int tagSelect;
    private Spinner searchOptions;
    private Spinner friendsSort;
    private String searchOptionSelected = "Name";
    private String sortOptionSelected = "Default";
    ArrayAdapter<CharSequence> spinnerAdapter;
    ArrayAdapter<CharSequence> friendsAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationDrawerAdapter navigationDrawerAdapter;
    RecyclerView navigationRecyclerView;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MessageService.MessageServiceInterface messageService;
    private MessageClientListener messageClientListener = new MyMessageClientListener();
    private UserItem clickedUser;
    private Queue<Message> messageQueue = new LinkedBlockingQueue<>();
    private AlertDialog dialog;
    boolean isFirstMessage = true;
    boolean isBlocking = false;
    private boolean isLaunch = true;
    private int launchSpinnerCount = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);

        toolbar = (Toolbar) findViewById(R.id.tb_activity_users);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.dl_activity_blocly);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
        drawerLayout.setDrawerListener(this);

        navigationDrawerAdapter = new NavigationDrawerAdapter();
        navigationDrawerAdapter.setDelegate(this);
        navigationRecyclerView = (RecyclerView) findViewById(R.id.rv_nav_activity_users);
        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        navigationRecyclerView.setAdapter(navigationDrawerAdapter);

        serviceIntent = new Intent(this, MessageService.class);

        userItemAdapter = new UserItemAdapter();
        userItemAdapter.setDelegate(this);

        logout = (Button) findViewById(R.id.activity_main_logout);
        logout.setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.rv_activity_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(userItemAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_activity_users);

        searchField = (EditText) findViewById(R.id.activity_users_search);
        searchButton = (Button) findViewById(R.id.activity_users_send_button);
        searchButton.setOnClickListener(this);

        searchOptions = (Spinner) findViewById(R.id.activity_users_search_option);
        spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.search_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchOptions.setAdapter(spinnerAdapter);
        searchOptions.setOnItemSelectedListener(this);

        friendsSort = (Spinner) findViewById(R.id.activity_users_nav_friends_sort);
        friendsAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        friendsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        friendsSort.setAdapter(friendsAdapter);
        friendsSort.setOnItemSelectedListener(this);

        handler = new Handler();

        if(savedInstanceState == null){
            ParseLoginBuilder builder = new ParseLoginBuilder(UsersActivity.this);
            startActivityForResult(builder.build(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 0){
            finish();
        }else{
            startService(serviceIntent);
            showSpinner();

            getLoggedInUsers();
            //getFriends();
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

    private void showSpinner() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();
                if (!success) {
                    Toast.makeText(getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "You are now logged in", Toast.LENGTH_LONG).show();
                    WeTubeUser user = (WeTubeUser) WeTubeUser.getCurrentUser();
                    user.setLoggedStatus(true);
                    user.setSessionStatus(false);
                    user.saveInBackground();
                    bindService(new Intent(UsersActivity.this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("com.gmail.markdevw.wetube.activities.UsersActivity"));
    }

    public void getLoggedInUsers(){
        final String currentUserId = ParseUser.getCurrentUser().getObjectId();

        if(WeTubeApplication.getSharedDataSource().getUsers().size() > 0){
            WeTubeApplication.getSharedDataSource().getUsers().clear();
        }

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        query.whereEqualTo("isLoggedIn", true);
        query.orderByAscending("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < userList.size(); i++) {
                        final WeTubeUser user = (WeTubeUser) userList.get(i);
                        String id = user.getObjectId();
                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("objectId", currentUserId);
                        query.whereEqualTo("friends", id);
                        query.findInBackground(new FindCallback<ParseUser>() {
                            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                                if (userList.size() == 0) {
                                    WeTubeApplication.getSharedDataSource().getUsers()
                                            .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), false));
                                    userItemAdapter.notifyDataSetChanged();
                                } else {
                                    WeTubeApplication.getSharedDataSource().getUsers()
                                            .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), true));
                                    userItemAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                        swipeRefreshLayout.setRefreshing(false);
                    }

                } else {
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public void getFriends(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId();

        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsDefault", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    if(userList.size() > 0) {
                        for (int i = 0; i < userList.size(); i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }
                        navigationDrawerAdapter.notifyDataSetChanged();
                        if(progressDialog != null){
                            progressDialog.dismiss();
                        }
                    }
                }else{
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getOnlineFriends(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId();

        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsOnline", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    if(userList.size() > 0) {
                        for (int i = 0; i < userList.size(); i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }
                        navigationDrawerAdapter.notifyDataSetChanged();
                        if(progressDialog != null){
                            progressDialog.dismiss();
                        }
                    }
                }else{
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getOfflineFriends(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId();

        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsOffline", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    if(userList.size() > 0) {
                        for (int i = 0; i < userList.size(); i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }
                        navigationDrawerAdapter.notifyDataSetChanged();
                        if(progressDialog != null){
                            progressDialog.dismiss();
                        }
                    }
                }else{
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getAvailableFriends(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId();

        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsAvailable", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    if(userList.size() > 0) {
                        for (int i = 0; i < userList.size(); i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }
                        navigationDrawerAdapter.notifyDataSetChanged();
                        if(progressDialog != null){
                            progressDialog.dismiss();
                        }
                    }
                }else{
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getUnavailableFriends(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId();

        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsUnavailable", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    if(userList.size() > 0) {
                        for (int i = 0; i < userList.size(); i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }
                        navigationDrawerAdapter.notifyDataSetChanged();
                        if(progressDialog != null){
                            progressDialog.dismiss();
                        }
                    }
                }else{
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getAlphabeticFriends(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId();

        if(WeTubeApplication.getSharedDataSource().getFriends().size() > 0){
            WeTubeApplication.getSharedDataSource().getFriends().clear();
        }


        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("getFriendsAtoZ", params, new FunctionCallback<List<ParseUser>>() {
            @Override
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    if(userList.size() > 0) {
                        for (int i = 0; i < userList.size(); i++) {
                            WeTubeUser friend = (WeTubeUser) userList.get(i);

                            WeTubeApplication.getSharedDataSource().getFriends()
                                    .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                            friend.getSessionStatus(), friend.getLoggedStatus(), true));
                        }
                        navigationDrawerAdapter.notifyDataSetChanged();
                        if(progressDialog != null){
                            progressDialog.dismiss();
                        }
                    }
                }else{
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onItemClicked(UserItemAdapter itemAdapter, final UserItem userItem, final View view) {

        clickedUser = userItem;

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("clickedId", clickedUser.getId());
        params.put("userId", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("isBlocked", params, new FunctionCallback<String>() {
            @Override
            public void done(String mapObject, com.parse.ParseException e) {
                if(e == null){
                    if(mapObject.equals("You blocked this user")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                        builder.setTitle("You have " + clickedUser.getName() + " blocked. Do you want to unblock this user?");

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                messageService.sendMessage(clickedUser.getId(), "unblock-" + ParseUser.getCurrentUser().getUsername() + "-" + ParseUser.getCurrentUser().getObjectId());
                                dialog.cancel();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    }else if(mapObject.equals("You are blocked by this user")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                        builder.setTitle(clickedUser.getName() + " has you blocked");

                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    }else if(mapObject.equals("No block")){
                        PopupMenu popMenu = new PopupMenu(UsersActivity.this, view);
                        if(userItem.getFriendStatus()){
                            getMenuInflater().inflate(R.menu.activity_users_popup_friend, popMenu.getMenu());
                        }else{
                            getMenuInflater().inflate(R.menu.activity_users_popup, popMenu.getMenu());
                        }
                        popMenu.setOnMenuItemClickListener(UsersActivity.this);
                        popMenu.show();
                    }
                }else{
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error: " + e + ". Failed to start session with " + clickedUser.getName(),
                            Toast.LENGTH_LONG).show();
                }

            }
        });
       /* HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("recipientId", userItem.getId());
        params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("startSession", params, new FunctionCallback<String>() {
            @Override
            public void done(String mapObject, com.parse.ParseException e) {
                if (e == null) {
                    WeTubeApplication.getSharedDataSource().setCurrentRecipient(userItem.getId());
                    Intent intent = new Intent(WeTubeApplication.getSharedInstance(), MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error: " + e + ". Failed to start session with " + userItem.getName(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });*/
    }

    @Override
    public void onNavItemClicked(NavigationDrawerAdapter itemAdapter, UserItem userItem, View view) {
        clickedUser = userItem;

        PopupMenu popMenu = new PopupMenu(this, view);
        getMenuInflater().inflate(R.menu.activity_users_popup_friend, popMenu.getMenu());

        popMenu.setOnMenuItemClickListener(this);
        popMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        switch (menuItem.getItemId()) {
            case R.id.popup_session :
                messageService.sendMessage(clickedUser.getId(), "startsession-" + ParseUser.getCurrentUser().getUsername() + "-" + ParseUser.getCurrentUser().getObjectId());
                break;
            case R.id.popup_add :
                messageService.sendMessage(clickedUser.getId(), "friendadd-" + ParseUser.getCurrentUser().getUsername() + "-" + ParseUser.getCurrentUser().getObjectId());
                break;
            case R.id.popup_profile:
                params.put("clickedId", clickedUser.getId());
                params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
                ParseCloud.callFunctionInBackground("commonTags", params, new FunctionCallback<List<String>>() {
                    @Override
                    public void done(final List<String> comTags, com.parse.ParseException e) {
                        if (e == null) {
                            HashMap<String, Object> params = new HashMap<String, Object>();
                            params.put("clickedId", clickedUser.getId());
                            params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
                            ParseCloud.callFunctionInBackground("uncommonTags", params, new FunctionCallback<List<String>>() {
                                @Override
                                public void done(List<String> uncomTags, com.parse.ParseException e) {
                                    if (e == null) {
                                        WeTubeApplication.getSharedDataSource().getCommonTags().clear();
                                        for(int i = 0; i<comTags.size(); i++){
                                            WeTubeApplication.getSharedDataSource().getCommonTags().add(new TagItem(comTags.get(i)));
                                        }

                                        WeTubeApplication.getSharedDataSource().getUncommonTags().clear();
                                        for(int i = 0; i<uncomTags.size(); i++){
                                            WeTubeApplication.getSharedDataSource().getUncommonTags().add(new TagItem(uncomTags.get(i)));
                                        }

                                        ProfileDialogFragment pdf = new ProfileDialogFragment();
                                        pdf.show(getFragmentManager(), "Profile");
                                    } else {
                                        Toast.makeText(WeTubeApplication.getSharedInstance(),
                                                "Error: " + e + ". Failed to start session with " + clickedUser.getName(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(WeTubeApplication.getSharedInstance(),
                                    "Error: " + e + ". Failed to start session with " + clickedUser.getName(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            case R.id.popup_remove :
                AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                builder.setTitle("Are you sure you want to remove " + clickedUser.getName() + " ?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        messageService.sendMessage(clickedUser.getId(), "friendremove-" + ParseUser.getCurrentUser().getUsername() + "-" + ParseUser.getCurrentUser().getObjectId());
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                builder.show();
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.activity_users_send_button:
                if(searchField.getText().toString().isEmpty()){
                    Toast.makeText(this, "Enter a search first", Toast.LENGTH_LONG).show();
                }else{
                    swipeRefreshLayout.setRefreshing(true);
                    if(searchOptionSelected.equals("Name")){
                        searchByName();
                    }else{
                        searchByTag();
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopService(new Intent(this, MessageService.class));
        messageService.removeMessageClientListener(messageClientListener);
        unbindService(serviceConnection);

        WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
        user.setLoggedStatus(false);
        user.saveInBackground();
        ParseUser.logOut();
    }

    @Override
    public void onRestart(){
        super.onRestart();

        isFirstMessage = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_users, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch(item.getItemId()) {
            case R.id.action_profile:
                AlertDialog.Builder categBuilder = new AlertDialog.Builder(this);

                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.category_dialog, null);

                ImageButton add = (ImageButton) dialogView.findViewById(R.id.category_dialog_add_button);
                ImageButton minus = (ImageButton) dialogView.findViewById(R.id.category_dialog_minus_button);

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
                                if(adapter.getPosition(input.getText().toString()) != -1){
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
                                    adapter.add(input.getText().toString());
                                    adapter.notifyDataSetChanged();
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
                        if(adapter.getCount() == 0){
                            Toast.makeText(getApplicationContext(), "No tags to remove", Toast.LENGTH_LONG).show();
                        }else if(tagSelect >= adapter.getCount()){
                            Toast.makeText(getApplicationContext(), "Select a tag before deleting", Toast.LENGTH_LONG).show();
                        }else if(adapter.getCount() > 0) {

                            HashMap<String, Object> params = new HashMap<String, Object>();
                            params.put("tag", adapter.getItem(tagSelect));
                            params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
                            ParseCloud.callFunctionInBackground("removeTag", params, new FunctionCallback<String>() {
                                @Override
                                public void done(String mapObject, com.parse.ParseException e) {

                                }
                            });
                            adapter.remove(adapter.getItem(tagSelect));
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

                categBuilder.setView(dialogView);
                categBuilder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        tagSelect = which;
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
                        adapter.addAll(tags);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        if(!isLaunch){
            String selection = (String) parent.getItemAtPosition(pos);

            if(selection.equals("Name") || selection.equals("Tag")){
                searchOptionSelected = selection;
            }else{
                sortOptionSelected = selection;

                friendsRefreshProgress();

                if(sortOptionSelected.equals("Default")){
                    getFriends();
                }else if(sortOptionSelected.equals("Online")){
                    getOnlineFriends();
                }else if(sortOptionSelected.equals("Offline")){
                    getOfflineFriends();
                }else if(sortOptionSelected.equals("Available")){
                    getAvailableFriends();
                }else if(sortOptionSelected.equals("Unavailable")){
                    getUnavailableFriends();
                } else if(sortOptionSelected.equals("A-Z")){
                    getAlphabeticFriends();

                }
            }
        }else{
            if(launchSpinnerCount<1){
                launchSpinnerCount++;
            }else{
                isLaunch = false;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void searchByTag(){
        final String currentUserId = ParseUser.getCurrentUser().getObjectId();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(searchField.getText().toString().split(" ")));
        query.whereContainedIn("tags", tokens);
        query.whereEqualTo("isLoggedIn", true);
        query.orderByAscending("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    if(userList.size() == 0){
                        WeTubeApplication.getSharedDataSource().getUsers().clear();
                        userItemAdapter.notifyDataSetChanged();
                        Toast.makeText(WeTubeApplication.getSharedInstance(),
                                "Could not find any logged in users with that tag",
                                Toast.LENGTH_LONG).show();
                    }else{
                        WeTubeApplication.getSharedDataSource().getUsers().clear();
                        for (int i=0; i<userList.size(); i++) {
                            final WeTubeUser user = (WeTubeUser) userList.get(i);
                            String id = user.getObjectId();
                            ParseQuery<ParseUser> query = ParseUser.getQuery();
                            query.whereEqualTo("objectId", currentUserId);
                            query.whereEqualTo("friends", id);
                            query.findInBackground(new FindCallback<ParseUser>() {
                                public void done(List<ParseUser> userList, com.parse.ParseException e) {
                                    if (userList.size() == 0) {
                                        WeTubeApplication.getSharedDataSource().getUsers()
                                                .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), false));
                                        userItemAdapter.notifyDataSetChanged();
                                    } else {
                                        WeTubeApplication.getSharedDataSource().getUsers()
                                                .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), true));
                                        userItemAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
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
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        query.whereEqualTo("isLoggedIn", true);
        query.whereStartsWith("username", searchField.getText().toString());
        query.orderByAscending("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    if(userList.size() == 0){
                        WeTubeApplication.getSharedDataSource().getUsers().clear();
                        userItemAdapter.notifyDataSetChanged();
                        Toast.makeText(WeTubeApplication.getSharedInstance(),
                                "Could not find any logged in users with that name",
                                Toast.LENGTH_LONG).show();
                    }
                    WeTubeApplication.getSharedDataSource().getUsers().clear();
                    for (int i=0; i<userList.size(); i++) {
                        final WeTubeUser user = (WeTubeUser) userList.get(i);
                        String id = user.getObjectId();
                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("friends", id);
                        query.findInBackground(new FindCallback<ParseUser>() {
                            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                                if (userList.size() == 0) {
                                    WeTubeApplication.getSharedDataSource().getUsers()
                                            .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), false));
                                    userItemAdapter.notifyDataSetChanged();
                                } else {
                                    WeTubeApplication.getSharedDataSource().getUsers()
                                            .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), true));
                                    userItemAdapter.notifyDataSetChanged();
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
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(messageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;
        }
    }

    private class MyMessageClientListener implements MessageClientListener {

        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo) {
            Toast.makeText(UsersActivity.this, "Message failed to send.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            messageQueue.add(message);

            if(!isFirstMessage){
                if(dialog != null && !dialog.isShowing() && !messageQueue.isEmpty()){
                    showNextMessage();
                }
            }else{
                //isFirstMessage = false;
                showNextMessage();
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, final String recipientId) {
            Toast.makeText(UsersActivity.this, "Message sent.", Toast.LENGTH_SHORT).show();
            ArrayList<String> msg = new ArrayList<String>(Arrays.asList(message.getTextBody().split("-")));
            if (msg.get(0).equals("friendremove")) {
                WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();

                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("friend", recipientId);
                params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
                ParseCloud.callFunctionInBackground("removeFriend", params, new FunctionCallback<String>() {
                    @Override
                    public void done(String mapObject, com.parse.ParseException e) {
                        Toast.makeText(UsersActivity.this, "Friend removed", Toast.LENGTH_SHORT).show();

                        for(int i=0; i<WeTubeApplication.getSharedDataSource().getFriends().size(); i++){
                            if(WeTubeApplication.getSharedDataSource().getFriends().get(i).getId().equals(recipientId)){
                                WeTubeApplication.getSharedDataSource().getFriends().remove(i);
                                navigationDrawerAdapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                        for(int i=0; i<WeTubeApplication.getSharedDataSource().getUsers().size(); i++){
                            if(WeTubeApplication.getSharedDataSource().getUsers().get(i).getId().equals(recipientId)){
                                WeTubeApplication.getSharedDataSource().getUsers().get(i).setFriendStatus(false);
                                userItemAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                });
            }else if(msg.get(0).equals("unblock")){
                WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();

                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("clickedId", recipientId);
                params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
                ParseCloud.callFunctionInBackground("unblock", params, new FunctionCallback<String>() {
                    @Override
                    public void done(String mapObject, com.parse.ParseException e) {
                        Toast.makeText(UsersActivity.this, "User unblocked", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
            Toast.makeText(UsersActivity.this, "Message delivered.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }

    public void showNextMessage() {
        Message message = messageQueue.poll();

        ArrayList<String> msg = new ArrayList<String>(Arrays.asList(message.getTextBody().split("-")));
            if(msg.get(0).equals("friendadd")){
                final String name = msg.get(1);
                final String id = msg.get(2);

                AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                builder.setTitle("Friend request from " + name);

                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                        messageService.sendMessage(id, "friendaccept-" + user.getUsername() + "-" + user.getObjectId());

                        user.add("friends", id);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                ParseQuery<ParseUser> query = ParseUser.getQuery();
                                query.whereEqualTo("objectId", id);
                                query.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> user, ParseException e) {
                                        WeTubeUser friend = (WeTubeUser) user.get(0);
                                        WeTubeApplication.getSharedDataSource().getFriends().add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                                friend.getSessionStatus(), friend.getLoggedStatus(), true));
                                        navigationDrawerAdapter.notifyDataSetChanged();

                                        for(int i=0; i<WeTubeApplication.getSharedDataSource().getUsers().size(); i++){
                                            if(WeTubeApplication.getSharedDataSource().getUsers().get(i).getName().equals(name)){
                                                WeTubeApplication.getSharedDataSource().getUsers().get(i).setFriendStatus(true);
                                                userItemAdapter.notifyItemChanged(i);
                                            }
                                        }
                                    }
                                });
                            }
                        });

                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        messageService.sendMessage(id, "frienddecline-" + ParseUser.getCurrentUser().getUsername());
                        dialog.cancel();
                    }
                });
                builder.setNeutralButton("Block User", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isBlocking = true;
                        AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                        builder.setTitle("Are you sure you want to block " + name + " ?");

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                messageService.sendMessage(id, "blockuser-" + ParseUser.getCurrentUser().getUsername() + "-" + ParseUser.getCurrentUser().getObjectId());
                                WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                                user.add("blockedUsers", id);
                                user.saveInBackground();

                                clearDialogsById(id);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setCancelable(false);
                        dialog = builder.create();
                        dialog.show();
                    }
                });
                builder.setCancelable(false);
                dialog = builder.create();
                dialog.show();
            }else if(msg.get(0).equals("frienddecline")){
                String name = msg.get(1);
                AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                builder.setTitle(name + " has declined your friend request");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                dialog = builder.create();
                dialog.show();
            }else if(msg.get(0).equals("friendaccept")){
                final String name = msg.get(1);
                final String id = msg.get(2);

                WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                user.add("friends", id);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("objectId", id);
                        query.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> user, ParseException e) {
                                WeTubeUser friend = (WeTubeUser) user.get(0);
                                WeTubeApplication.getSharedDataSource().getFriends().add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                        friend.getSessionStatus(), friend.getLoggedStatus(), true));
                                navigationDrawerAdapter.notifyDataSetChanged();

                                for(int i=0; i<WeTubeApplication.getSharedDataSource().getUsers().size(); i++){
                                    if(WeTubeApplication.getSharedDataSource().getUsers().get(i).getName().equals(name)){
                                        WeTubeApplication.getSharedDataSource().getUsers().get(i).setFriendStatus(true);
                                        userItemAdapter.notifyItemChanged(i);
                                    }
                                }
                            }
                        });
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                builder.setTitle(name + " accepted your friend request");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                dialog = builder.create();
                dialog.show();
            }else if(msg.get(0).equals("friendremove")){
                final String id = msg.get(2);
                WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();

                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("friend", id);
                params.put("userId", user.getObjectId());
                ParseCloud.callFunctionInBackground("removeFriend", params, new FunctionCallback<String>() {
                    @Override
                    public void done(String mapObject, com.parse.ParseException e) {
                        for(int i=0; i<WeTubeApplication.getSharedDataSource().getFriends().size(); i++){
                            if(WeTubeApplication.getSharedDataSource().getFriends().get(i).getId().equals(id)){
                                WeTubeApplication.getSharedDataSource().getFriends().remove(i);
                                navigationDrawerAdapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                        for(int i=0; i<WeTubeApplication.getSharedDataSource().getUsers().size(); i++){
                            if(WeTubeApplication.getSharedDataSource().getUsers().get(i).getId().equals(id)){
                                WeTubeApplication.getSharedDataSource().getUsers().get(i).setFriendStatus(false);
                                userItemAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                });
            }else if(msg.get(0).equals("blockuser")){
                final String name = msg.get(1);
                final String id = msg.get(2);

                AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                builder.setTitle(name + " has blocked you");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                dialog = builder.create();
                dialog.show();

                WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                user.add("blockedBy", id);
                user.saveInBackground();
            }else if(msg.get(0).equals("unblock")){
                final String name = msg.get(1);
                final String id = msg.get(2);

                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("clickedId", id);
                params.put("userId", WeTubeUser.getCurrentUser().getObjectId());
                ParseCloud.callFunctionInBackground("unblockedBy", params, new FunctionCallback<String>() {
                    @Override
                    public void done(String mapObject, com.parse.ParseException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                        builder.setTitle(name + " has unblocked you");

                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setCancelable(false);
                        dialog = builder.create();
                        dialog.show();
                    }
                });
            }else if(msg.get(0).equals("startsession")){
                final String name = msg.get(1);
                final String id = msg.get(2);

                AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                builder.setTitle("Session request from " + name);

                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                        user.setSessionStatus(true);
                        user.saveInBackground();
                        messageService.sendMessage(id, "sessionaccept-" + user.getUsername() + "-" + user.getObjectId());
                        dialog.cancel();

                        WeTubeApplication.getSharedDataSource().setCurrentRecipient(new UserItem(name, id));
                        Intent intent = new Intent(WeTubeApplication.getSharedInstance(), MainActivity.class);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        messageService.sendMessage(id, "sessiondecline-" + ParseUser.getCurrentUser().getUsername());
                        dialog.cancel();
                    }
                });
                builder.setNeutralButton("Block User", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isBlocking = true;
                        AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                        builder.setTitle("Are you sure you want to block " + name + " ?");

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                messageService.sendMessage(id, "blockuser-" + ParseUser.getCurrentUser().getUsername() + "-" + ParseUser.getCurrentUser().getObjectId());
                                WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                                user.add("blockedUsers", id);
                                user.saveInBackground();
                                dialog.cancel();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setCancelable(false);
                        dialog = builder.create();
                        dialog.show();
                    }
                });
                builder.setCancelable(false);
                dialog = builder.create();
                dialog.show();
            }else if(msg.get(0).equals("sessiondecline")){
                String name = msg.get(1);
                AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
                builder.setTitle(name + " has declined your session request");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                dialog = builder.create();
                dialog.show();
            }else if(msg.get(0).equals("sessionaccept")){
                WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                user.setSessionStatus(true);
                user.saveInBackground();

                WeTubeApplication.getSharedDataSource().setCurrentRecipient(new UserItem(msg.get(1), msg.get(2)));
                Intent intent = new Intent(WeTubeApplication.getSharedInstance(), MainActivity.class);
                startActivity(intent);
            }
        if(dialog != null){
            dialog.setOnDismissListener(this);
        }
    }

    public void clearDialogsById(String id){

        Message msgToRemove;

        for(Message message : messageQueue) {
            ArrayList<String> msg = new ArrayList<String>(Arrays.asList(message.getTextBody().split("-")));
            if(msg.get(2).equals(id)){
                messageQueue.remove(message);
            }
        }
        isBlocking = false;
        if(!messageQueue.isEmpty()){
            showNextMessage();
        }
    }


    @Override
    public void onDismiss(DialogInterface dialogInterface) {

        if(!messageQueue.isEmpty() && !isBlocking){
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
        if(sortOptionSelected.equals("Default")){
            getFriends();
        }else if(sortOptionSelected.equals("Online")){
            getOnlineFriends();
        }else if(sortOptionSelected.equals("Offline")){
            getOfflineFriends();
        }else if(sortOptionSelected.equals("Available")){
            getAvailableFriends();
        }else if(sortOptionSelected.equals("Unavailable")){
            getUnavailableFriends();
        } else if(sortOptionSelected.equals("A-Z")){
            getAlphabeticFriends();
        }
    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    public void friendsRefreshProgress(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Refreshing friends list");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
    }
}
