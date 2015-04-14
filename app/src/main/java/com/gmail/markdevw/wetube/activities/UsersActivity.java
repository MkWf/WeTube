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
import com.gmail.markdevw.wetube.api.model.UserItem;
import com.gmail.markdevw.wetube.services.MessageService;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.models.WeTubeUser;
import com.parse.ui.ParseLoginBuilder;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Mark on 4/2/2015.
 */
public class UsersActivity extends ActionBarActivity implements UserItemAdapter.Delegate, View.OnClickListener, AdapterView.OnItemSelectedListener {

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
    private String searchOptionSelected;
    ArrayAdapter<CharSequence> spinnerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationDrawerAdapter navigationDrawerAdapter;
    RecyclerView navigationRecyclerView;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MessageService.MessageServiceInterface messageService;
    private MessageClientListener messageClientListener = new MyMessageClientListener();


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
        drawerLayout.setDrawerListener(drawerToggle);

        navigationDrawerAdapter = new NavigationDrawerAdapter();
        navigationRecyclerView = (RecyclerView) findViewById(R.id.rv_nav_activity_users);
        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        navigationRecyclerView.setAdapter(navigationDrawerAdapter);

        serviceIntent = new Intent(getApplicationContext(), MessageService.class);

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

        handler = new Handler();

        ParseLoginBuilder builder = new ParseLoginBuilder(UsersActivity.this);
        startActivityForResult(builder.build(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 0){
            finish();
        }else{
            startService(serviceIntent);
            showSpinner();
            bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);
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
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("com.gmail.markdevw.wetube.activities.UsersActivity"));
    }

    public void getLoggedInUsers(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId();

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
                    for (int i=0; i<userList.size(); i++) {
                        final WeTubeUser user = (WeTubeUser) userList.get(i);
                        String id = user.getObjectId();
                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("friends", id);
                        query.findInBackground(new FindCallback<ParseUser>() {
                            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                               if(userList.size() == 0){
                                   WeTubeApplication.getSharedDataSource().getUsers()
                                           .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), false));
                                   userItemAdapter.notifyDataSetChanged();
                               }else{
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

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", currentUserId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    WeTubeUser user = (WeTubeUser) userList.get(0);
                    if (user.getList("friends")!=null) {
                        List<String> friends = user.getList("friends");

                        for(int i = 0; i<friends.size(); i++){
                            ParseQuery<ParseUser> query2 = ParseUser.getQuery();
                            query2.whereEqualTo("objectId", friends.get(i));
                            query2.findInBackground(new FindCallback<ParseUser>() {
                                public void done(List<ParseUser> userList, com.parse.ParseException e) {
                                    if(e == null){
                                        WeTubeUser friend = (WeTubeUser) userList.get(0);
                                        WeTubeApplication.getSharedDataSource().getFriends()
                                                .add(new UserItem(friend.getUsername(), friend.getObjectId(),
                                                        friend.getSessionStatus(), friend.getLoggedStatus(), true));
                                    }else{
                                        Toast.makeText(WeTubeApplication.getSharedInstance(),
                                                "Error loading a user",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    navigationDrawerAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }else{
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading friends list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        /*ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        query.orderByDescending("isLoggedIn");
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i=0; i<userList.size(); i++) {
                        WeTubeUser user = (WeTubeUser) userList.get(i);
                        WeTubeApplication.getSharedDataSource().getFriends()
                                .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus()));
                    }
                    navigationDrawerAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });*/
    }

    @Override
    public void onItemClicked(UserItemAdapter itemAdapter, final UserItem userItem) {



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
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.activity_users_send_button:
                if(searchField.getText().toString().isEmpty()){
                    Toast.makeText(this, "Enter a search first", Toast.LENGTH_LONG).show();
                }else{
                    swipeRefreshLayout.setRefreshing(true);
                    if(searchOptionSelected.equals("User")){
                        searchByUser();
                    }else{
                        searchByTag();
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroy(){
        messageService.removeMessageClientListener(messageClientListener);
        unbindService(serviceConnection);
        super.onDestroy();
        WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
        user.setLoggedStatus(false);
        user.saveInBackground();
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
        searchOptionSelected = (String) parent.getItemAtPosition(pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void searchByTag(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        query.whereEqualTo("tags", searchField.getText().toString());
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
                    }
                    WeTubeApplication.getSharedDataSource().getUsers().clear();
                    for (int i=0; i<userList.size(); i++) {
                        final WeTubeUser user = (WeTubeUser) userList.get(i);
                        String id = user.getObjectId();
                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("friends", id);
                        query.findInBackground(new FindCallback<ParseUser>() {
                            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                                if(userList.size() == 0){
                                    WeTubeApplication.getSharedDataSource().getUsers()
                                            .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), false));
                                    userItemAdapter.notifyDataSetChanged();
                                }else{
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

    private void searchByUser() {
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
                                if(userList.size() == 0){
                                    WeTubeApplication.getSharedDataSource().getUsers()
                                            .add(new UserItem(user.getUsername(), user.getObjectId(), user.getSessionStatus(), user.getLoggedStatus(), false));
                                    userItemAdapter.notifyDataSetChanged();
                                }else{
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
            String msg = message.getTextBody();

            AlertDialog.Builder builder = new AlertDialog.Builder(UsersActivity.this);
            builder.setTitle("Invitation from ");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {

        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {

        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }
}
