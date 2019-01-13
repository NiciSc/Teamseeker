package de.ur.mi.android.teamseeker.helpers;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.ur.mi.android.teamseeker.DatabaseManager;
import de.ur.mi.android.teamseeker.EventActivity;
import de.ur.mi.android.teamseeker.EventData;
import de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener;
import de.ur.mi.android.teamseeker.MainActivity;
import de.ur.mi.android.teamseeker.MapsActivity;
import de.ur.mi.android.teamseeker.MyEventsManager;
import de.ur.mi.android.teamseeker.MyLocationManager;
import de.ur.mi.android.teamseeker.ProfileActivity;
import de.ur.mi.android.teamseeker.R;
import de.ur.mi.android.teamseeker.UserData;
import de.ur.mi.android.teamseeker.adapters.NavigationDrawerItemAdapter;
import de.ur.mi.android.teamseeker.interfaces.OnDataDownloadCompleteListener;

public abstract class OverlayActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * These strings are not in strings.xml because it is easier to use a switch case later and they are easier to maintain and more appropriate here
     */
    //region static strings
    protected static final String DRAWER_ITEM_MAP = "Map";
    protected static final String DRAWER_ITEM_CREATEEVENT = "Create Event";
    protected static final String DRAWER_ITEM_MYEVENTS = "My Events";
    private static final String[] DRAWER_ITEMS = {DRAWER_ITEM_MAP, DRAWER_ITEM_CREATEEVENT, DRAWER_ITEM_MYEVENTS};

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            DRAWER_ITEM_MAP,
            DRAWER_ITEM_CREATEEVENT,
            DRAWER_ITEM_MYEVENTS,
    })
    public @interface DrawerItemDef {
    }

    private Menu toolbarMenu;
    private DrawerLayout navDrawer;
    private Toolbar toolbar;
    private NavigationDrawerItemAdapter navDrawerItemAdapter;
    private ExpandableListView expandableListView;
    private TextView connectivity_error_text;
    private boolean isOverlaySetup = false;
    private BroadcastReceiver networkUpdatReceiver;
    private OnCompleteListener onRequestPermissionResultListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(networkUpdatReceiver);
        super.onDestroy();
    }

    /**
     * Called on all children when the option menu is created
     * When called they know they can now access the action bar
     */
    protected abstract void onOverlayReady();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        toolbarMenu = menu;
        onOverlayReady();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (navDrawer.isDrawerOpen(GravityCompat.START)) {
            navDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //region setup

    //region setup content view
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.baselayout_drawer);
        insertLayout(layoutResID);
        if (!isOverlaySetup) {
            setupActionBar();
            setupNavDrawer();
        }
        setupNetworkCheck();
    }

    public void setContentViewNoOverlay(int layoutResID) {
        super.setContentView(layoutResID);
    }

    /**
     * Inserts the passed view into the base layout in order to have the actionbar and navigation drawer work for any activity without configuration
     *
     * @param layout
     */
    private void insertLayout(@LayoutRes int layout) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layout, (ViewGroup) findViewById(R.id.activity_container), true);
    }
    //endregion

    //region setup overlay
    //region setup toolbar
    private void setupActionBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setToolbarText("");
    }
    //endregion

    //region setup navigation drawer
    private void setupNavDrawer() {
        navDrawer = findViewById(R.id.layout_navdrawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, navDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getHeaderView(0).setOnClickListener(this);
        setupNavHeader(navigationView.getHeaderView(0));
        navDrawer.addDrawerListener(toggle);
        toggle.syncState();
        setupNavDrawerMenu();
    }

    private void setupNavHeader(View navHeader) {
        ImageView headerProfilePicture = navHeader.findViewById(R.id.imageView_profilePic);
        TextView headerUserName = navHeader.findViewById(R.id.textView_headerUserName);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains(UserData.USERNAME_KEY + userID)) {
            headerUserName.setText(prefs.getString(UserData.USERNAME_KEY + userID, getString(R.string.error_usernamenotfound)));
        }
        File file = new File(this.getFilesDir(), userID);
        if (file.exists()) {
            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
            RoundedBitmapDrawable roundedPicture = RoundedBitmapDrawableFactory.create(getResources(), bmp);
            roundedPicture.setCornerRadius(bmp.getHeight());
            headerProfilePicture.setImageDrawable(roundedPicture);
        }
    }

    private void setupNavDrawerMenu() {
        expandableListView = findViewById(R.id.drawer_menu_list);
        navDrawerItemAdapter = new NavigationDrawerItemAdapter(this, DRAWER_ITEMS, getBaseMenu());
        expandableListView.setAdapter(navDrawerItemAdapter);
        updateDrawerMenuItem(DRAWER_ITEM_MYEVENTS, MyEventsManager.getMyEvents());
        setDrawerMenuListeners();
    }
    //endregion

    //endregion

    //endregion

    //region network management
    private void setupNetworkCheck() {
        connectivity_error_text = findViewById(R.id.connectivity_error_text);
        networkUpdatReceiver = new ConnectivityBroadcastReceiver();
        registerReceiver(networkUpdatReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void onConnectivityStatusChanged(boolean isConnected) {
        if (isConnected) {
            connectivity_error_text.setVisibility(View.GONE);
        } else {
            connectivity_error_text.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_slidedown);
            connectivity_error_text.startAnimation(animation);
        }
    }
    //endregion

    //region navigation drawer management
    protected <T> void updateDrawerMenuItem(@DrawerItemDef String header, List<T> children) {
        collapseAllDrawerMenus();
        navDrawerItemAdapter.updateMenu(header, children);
    }

    private void collapseAllDrawerMenus() {
        for (int i = 0; i < DRAWER_ITEMS.length; i++) {
            if (expandableListView.isGroupExpanded(i)) {
                expandableListView.collapseGroup(i);
            }
        }
    }
    //endregion

    //region navigation drawer menu utility
    private HashMap<String, List> getBaseMenu() {
        HashMap<String, List> baseMenu = new HashMap<>();
        for (int i = 0; i < DRAWER_ITEMS.length; i++) {
            baseMenu.put(DRAWER_ITEMS[i], null);
        }
        return baseMenu;
    }

    //endregion

    //region toolbar utility
    public void setToolbarText(String text) {
        getSupportActionBar().setTitle(text);
    }

    public void hideToolbarItem(int index) {
        toolbarMenu.findItem(index).setVisible(false);
    }

    public void showToolbarItem(int index) {
        toolbarMenu.findItem(index).setVisible(true);
    }
    //endregion

    //region listeners
    private void setDrawerMenuListeners() {
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                final String headerName = (String) navDrawerItemAdapter.getGroup(groupPosition);
                switch (headerName) {
                    case DRAWER_ITEM_MAP:
                        switchActivity(MapsActivity.class);
                        break;
                    case DRAWER_ITEM_CREATEEVENT:
                        DatabaseManager.getData(EventData.class, DatabaseManager.DB_KEY_EVENT, EventData.EVENTID_KEY, FirebaseAuth.getInstance().getCurrentUser().getUid(), new OnDataDownloadCompleteListener<EventData>() {
                            @Override
                            public void onDataDownloadComplete(List<EventData> data, int resultCode) {
                                if (resultCode == RESULT_OK) {
                                    Toast.makeText(OverlayActivity.this, R.string.error_alreadyhost, Toast.LENGTH_SHORT).show();
                                } else {
                                    navDrawer.closeDrawer(GravityCompat.START);
                                    Intent newEventIntent = new Intent(OverlayActivity.this, EventActivity.class);
                                    startActivity(newEventIntent);
                                }
                            }
                        });
                        return false;
                    case DRAWER_ITEM_MYEVENTS:
                        if (navDrawerItemAdapter.getChildrenCount(2) == 0) {
                            Toast.makeText(OverlayActivity.this, R.string.error_myeventsempty, Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    default:
                        break;
                }
                navDrawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final String headerName = (String) navDrawerItemAdapter.getGroup(groupPosition);
                switch (headerName) {
                    case DRAWER_ITEM_MAP:
                        break;
                    case DRAWER_ITEM_CREATEEVENT:
                        break;
                    case DRAWER_ITEM_MYEVENTS:
                        EventData eventData = (EventData) navDrawerItemAdapter.getChild(groupPosition, childPosition);
                        Intent eventIntent = new Intent(OverlayActivity.this, EventActivity.class);
                        eventIntent.putExtra(getString(R.string.event_intent_key), eventData);
                        startActivity(eventIntent);
                        break;
                    default:
                        break;
                }
                navDrawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //Switch Case Selector for all menu_toolbar.xmltems and their corresponding interactions
        int id = item.getItemId();
        switch (id) {
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                switchActivity(MainActivity.class);
                return true;
            case R.id.action_filter:
                ((MapsActivity) this).toggleFilterWindow();
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_drawerHeader:
                switchActivity(ProfileActivity.class);
                break;
            default:
                return;
        }
        navDrawer.closeDrawer(GravityCompat.START);
    }
    //endregion

    //region generic utility
    protected void switchActivity(Class<?> activity) {
        Intent i = new Intent(OverlayActivity.this, activity);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    public static @DrawableRes
    int getIconForHeader(@DrawerItemDef String header) {
        switch (header) {
            case DRAWER_ITEM_MAP:
                return R.drawable.ic_map;
            case DRAWER_ITEM_CREATEEVENT:
                return R.drawable.ic_add_marker;
            case DRAWER_ITEM_MYEVENTS:
                return R.drawable.ic_format_list;
            default:
                return Integer.MIN_VALUE;
        }
    }
    //endregion

    //region locationpermission
    protected void requestLocationPermission(OnCompleteListener onCompleteListener) {
        if (MyLocationManager.requestLocationPermission(this)) {
            onCompleteListener.onComplete(RESULT_OK);
        } else {
            this.onRequestPermissionResultListener = onCompleteListener;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MyLocationManager.LOCATION_PERMISSION_REQUEST_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        this.onRequestPermissionResultListener.onComplete(RESULT_CANCELED);
                        return;
                    }
                }
                this.onRequestPermissionResultListener.onComplete(RESULT_OK);
                break;
            default:
                break;
        }
    }
    //endregion
}
