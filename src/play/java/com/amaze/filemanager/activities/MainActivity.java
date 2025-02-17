/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.filemanager.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.amaze.filemanager.IMyAidlInterface;
import com.amaze.filemanager.Loadlistener;
import com.amaze.filemanager.R;
import com.amaze.filemanager.adapters.DrawerAdapter;
import com.amaze.filemanager.database.Tab;
import com.amaze.filemanager.database.TabHandler;
import com.amaze.filemanager.fragments.AppsList;
import com.amaze.filemanager.fragments.Main;
import com.amaze.filemanager.fragments.ProcessViewer;
import com.amaze.filemanager.fragments.TabFragment;
import com.amaze.filemanager.fragments.ZipViewer;
import com.amaze.filemanager.services.CopyService;
import com.amaze.filemanager.services.DeleteTask;
import com.amaze.filemanager.services.asynctasks.MoveFiles;
import com.amaze.filemanager.ui.Layoutelements;
import com.amaze.filemanager.ui.drawer.EntryItem;
import com.amaze.filemanager.ui.drawer.Item;
import com.amaze.filemanager.ui.drawer.SectionItem;
import com.amaze.filemanager.ui.icons.IconUtils;
import com.amaze.filemanager.ui.icons.Icons;
import com.amaze.filemanager.ui.icons.MimeTypes;
import com.amaze.filemanager.ui.views.RoundedImageView;
import com.amaze.filemanager.ui.views.ScrimInsetsRelativeLayout;
import com.amaze.filemanager.utils.BookSorter;
import com.amaze.filemanager.utils.FileUtil;
import com.amaze.filemanager.utils.Futils;
import com.amaze.filemanager.utils.HFile;
import com.amaze.filemanager.utils.HistoryManager;
import com.amaze.filemanager.utils.MainActivityHelper;
import com.amaze.filemanager.utils.PreferenceUtils;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,OnRequestPermissionsResultCallback  {
    public final int DELETE = 0, COPY = 1, MOVE = 2, NEW_FOLDER = 3, RENAME = 4, NEW_FILE = 5, EXTRACT = 6, COMPRESS = 7;
     final Pattern DIR_SEPARATOR = Pattern.compile("/");
    /* Request code used to invoke sign in user interactions. */
    static final int RC_SIGN_IN = 0;
    public Integer select;
    public DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    public List<String> val;
    public ArrayList<String[]> Servers, accounts;
    public ScrimInsetsRelativeLayout mDrawerLinear;
    public String skin, path = "", launchPath;
    public int theme;
    public ArrayList<String> COPY_PATH = null, MOVE_PATH = null;
    public FrameLayout frameLayout;
    public boolean mReturnIntent = false;
    public ArrayList<Item> list;
    public int theme1;
    public boolean rootmode, aBoolean, openzip = false;
    public boolean mRingtonePickerIntent = false,  colourednavigation = false;
    public Toolbar toolbar;
    public int skinStatusBar;
    public int storage_count = 0;
    public String fabskin;
    public FloatingActionMenu floatingActionButton;
    public LinearLayout pathbar;
    public FrameLayout buttonBarFrame;
    public boolean isDrawerLocked = false;
    public HistoryManager history, grid;
    public ArrayList<String> hiddenfiles, gridfiles, listfiles;
    public String DRIVE = "drive", SMB = "smb", BOOKS = "books", HISTORY = "Table1", HIDDEN = "Table2", LIST = "list", GRID = "grid";
    Futils utils;
    public SharedPreferences Sp;
    public ArrayList<String[]> books;
    MainActivity mainActivity = this;
    public DrawerAdapter adapter;
    IconUtils util;
    Context con = this;
    public MainActivityHelper mainActivityHelper;
    String zippath;
    FragmentTransaction pending_fragmentTransaction;
    String pending_path;
    boolean openprocesses = false;
    int hidemode;
    public int operation=-1;
    public ArrayList<String> oparrayList,opnameList;
    public String oppathe, oppathe1;
    IMyAidlInterface aidlInterface;
    MaterialDialog materialDialog;
    String newPath = null;
    boolean backPressedToExitOnce = false;
    Toast toast = null;
    ActionBarDrawerToggle mDrawerToggle;
    Intent intent;
    GoogleApiClient mGoogleApiClient;
    View drawerHeaderLayout;
    View drawerHeaderView,indicator_layout;
    RoundedImageView drawerProfilePic;
    DisplayImageOptions displayImageOptions;
    int sdk, COUNTER=0;
    TextView mGoogleName, mGoogleId;
    LinearLayout buttons;
    HorizontalScrollView scroll, scroll1;
    CountDownTimer timer;
    IconUtils icons;
    TabHandler tabHandler;
    RelativeLayout drawerHeaderParent;
    static final int image_selector_request_code=31;
    // Check for user interaction for google+ api only once
    boolean mGoogleApiKey = false;
    /* A flag indicating that a PendingIntent is in progress and prevents
   * us from starting further intents.
   */
    boolean mIntentInProgress, topfab = false, showHidden = false;

    // string builder object variables for pathBar animations
    StringBuilder newPathBuilder, oldPathBuilder;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Sp = PreferenceManager.getDefaultSharedPreferences(this);
        initialisePreferences();
        setTheme();
        setContentView(R.layout.main_toolbar);
        initialiseViews();
        tabHandler = new TabHandler(this, null, null, 1);
        utils = new Futils();
        //requesting storage permissions
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            if(!checkStoragePermission())
                requestStoragePermission();

        mainActivityHelper=new MainActivityHelper(this);
        intialiseFab();
        history = new HistoryManager(this, "Table2");
        history.initializeTable(HISTORY,0);
        history.initializeTable(HIDDEN,0);
        grid = new HistoryManager(this, "listgridmodes");
        grid.initializeTable(LIST,0);
        grid.initializeTable(GRID,0);
        grid.initializeTable(BOOKS,1);
        grid.initializeTable(DRIVE,1);
        grid.initializeTable(SMB,1);
        hiddenfiles = history.readTable(HIDDEN);
        gridfiles = grid.readTable(GRID);
        listfiles = grid.readTable(LIST);
        if (!Sp.getBoolean("booksadded", false)) {
            grid.make(BOOKS);
            Sp.edit().putBoolean("booksadded", true).commit();
        }
        // initialize g+ api client as per preferences
        if (Sp.getBoolean("plus_pic", false)) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)

                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .build();
        }
        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.amaze_header)
                .showImageForEmptyUri(R.drawable.amaze_header)
                .showImageOnFail(R.drawable.amaze_header)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        if (!ImageLoader.getInstance().isInited()) {

            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        }

        util = new IconUtils(Sp, this);
        icons = new IconUtils(Sp, this);

        // Toolbar
        timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                crossfadeInverse();
            }
        };
        path = getIntent().getStringExtra("path");
        openprocesses = getIntent().getBooleanExtra("openprocesses", false);
        try {
            intent = getIntent();
            if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)) {

                // file picker intent
                mReturnIntent = true;
                Toast.makeText(this, utils.getString(con, R.string.pick_a_file), Toast.LENGTH_LONG).show();
            } else if (intent.getAction().equals(RingtoneManager.ACTION_RINGTONE_PICKER)) {
                // ringtone picker intent
                mReturnIntent = true;
                mRingtonePickerIntent = true;
                Toast.makeText(this, utils.getString(con, R.string.pick_a_file), Toast.LENGTH_LONG).show();
            } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {

                // zip viewer intent
                Uri uri = intent.getData();
                openzip = true;
                zippath = uri.toString();
            }
        } catch (Exception e) {

        }
        updateDrawer();
        if (savedInstanceState == null) {

            if (openprocesses) {
                android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, new ProcessViewer());
                //   transaction.addToBackStack(null);
                select = 102;
                openprocesses = false;
                //title.setText(utils.getString(con, R.string.process_viewer));
                //Commit the transaction
                transaction.commit();
                supportInvalidateOptionsMenu();
            } else {
                goToMain(path);
            }
        } else {
            oppathe = savedInstanceState.getString("oppathe");
            oppathe1 = savedInstanceState.getString("oppathe1");
            ArrayList<String> k = savedInstanceState.getStringArrayList("oparrayList");
            if (k != null) {
                oparrayList = (k);
                opnameList=savedInstanceState.getStringArrayList("opnameList")!=null?savedInstanceState.getStringArrayList("opnameList"):opnameList;
                operation = savedInstanceState.getInt("operation");
            }
            select = savedInstanceState.getInt("selectitem", 0);
            adapter.toggleChecked(select);
        }
        if (theme1 == 1) {
            mDrawerList.setBackgroundColor(ContextCompat.getColor(this,R.color.holo_dark_background));
        }
        mDrawerList.setDivider(null);
        if (!isDrawerLocked) {
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.drawable.ic_drawer_l,  /* nav drawer image to replace 'Up' caret */
                    R.string.drawer_open,  /* "open drawer" description for accessibility */
                    R.string.drawer_close  /* "close drawer" description for accessibility */
            ) {
                public void onDrawerClosed(View view) {
                    mainActivity.onDrawerClosed();
                }

                public void onDrawerOpened(View drawerView) {
                    //title.setText("Amaze File Manager");
                    // creates call to onPrepareOptionsMenu()
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer_l);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            mDrawerToggle.syncState();
        }/*((ImageButton) findViewById(R.id.drawer_buttton)).setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawerLayout.isDrawerOpen(mDrawerLinear)) {
                    mDrawerLayout.closeDrawer(mDrawerLinear);
                } else mDrawerLayout.openDrawer(mDrawerLinear);
            }
        });*/
        if (mDrawerToggle != null) {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer_l);
        }
        //recents header color implementation
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription("Amaze", ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap(), Color.parseColor(skin));
            ((Activity) this).setTaskDescription(taskDescription);
        }
    }

    /**
     * Returns all available SD-Cards in the system (include emulated)
     * <p/>
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standard way to get it.
     * TODO: Test on future Android versions 4.4+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */


    public List<String> getStorageDirectories() {
        // Final set of paths
        final ArrayList<String> rv = new ArrayList<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkStoragePermission())
            rv.clear();
        String strings[] = FileUtil.getExtSdCardPathsForActivity(this);
        for(String s:strings){
            File f=new File(s);
            if(!rv.contains(s) && utils.canListFiles(f))
                rv.add(s);
        }
        rootmode = Sp.getBoolean("rootmode", false);
        if (rootmode)
            rv.add("/");
        File usb = getUsbDrive();
        if (usb != null && !rv.contains(usb.getPath())) rv.add(usb.getPath());
        return rv;
    }

    @Override
    public void onBackPressed() {
        if (!isDrawerLocked) {
            if (mDrawerLayout.isDrawerOpen(mDrawerLinear)) {
                mDrawerLayout.closeDrawer(mDrawerLinear);
            } else {
                onbackpressed();
            }
        } else onbackpressed();
    }

    void onbackpressed() {
        try {

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            String name = fragment.getClass().getName();
            if (name.contains("TabFragment")) {
                if (floatingActionButton.isOpened()) {
                    floatingActionButton.close(true);
                    revealShow(findViewById(R.id.fab_bg), false);
                } else {
                    TabFragment tabFragment = ((TabFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame));
                    Fragment fragment1 = tabFragment.getTab();
                    Main main = (Main) fragment1;
                    main.goBack();
                }
            } else if (name.contains("ZipViewer")) {
                ZipViewer zipViewer = (ZipViewer) getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (zipViewer.mActionMode == null) {
                    if (zipViewer.cangoBack()) {

                        zipViewer.goBack();
                    } else if (openzip) {
                        openzip = false;
                        finish();
                    } else {

                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.slide_out_bottom, R.anim.slide_out_bottom);
                        fragmentTransaction.remove(zipViewer);
                        fragmentTransaction.commit();
                        supportInvalidateOptionsMenu();
                        floatingActionButton.showMenuButton(true);

                    }
                } else {
                    zipViewer.mActionMode.finish();
                }
            } else
                goToMain("");
        } catch (ClassCastException e) {
            goToMain("");
        }
    }

    public void invalidatePasteButton(MenuItem paste) {
        if (MOVE_PATH != null || COPY_PATH != null) {
            paste.setVisible(true);
        } else {
            paste.setVisible(false);
        }
    }

    public void exit() {
        if (backPressedToExitOnce) {
            finish();
            if (rootmode) {
                try {
                    RootTools.closeAllShells();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.backPressedToExitOnce = true;
            showToast(utils.getString(this, R.string.pressagain));
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backPressedToExitOnce = false;
                }
            }, 2000);
        }
    }

    public void updateDrawer() {
        list = new ArrayList<>();
        val = getStorageDirectories();
        books = new ArrayList<>();
        Servers = new ArrayList<>();
        accounts=new ArrayList<>();
        storage_count = 0;
        for (String file : val) {
            File f = new File(file);
            String name;
            Drawable icon1 = ContextCompat.getDrawable(this, R.drawable.ic_sd_storage_white_56dp);
            if ("/storage/emulated/legacy".equals(file) || "/storage/emulated/0".equals(file)) {
                name = getResources().getString(R.string.storage);

            } else if ("/storage/sdcard1".equals(file)) {
                name = getResources().getString(R.string.extstorage);
            } else if ("/".equals(file)) {
                name = getResources().getString(R.string.rootdirectory);
                icon1 = ContextCompat.getDrawable(this, R.drawable.ic_drawer_root_white);
            } else name = f.getName();
            if (!f.isDirectory() || f.canExecute()) {
                storage_count++;
                list.add(new EntryItem(name, file, icon1));
            }
        }
        list.add(new SectionItem());
            try {
                for (String[] file : grid.readTableSecondary(SMB))
                Servers.add(file);
                if (Servers.size() > 0) {
                    Collections.sort(Servers, new BookSorter());
                    for (String[] file : Servers)
                        list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable
                                .ic_settings_remote_white_48dp)));
                    list.add(new SectionItem());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        try {
            for (String[] file : grid.readTableSecondary(DRIVE)) {
                accounts.add(file);
            }
            if (accounts.size() > 0) {
                Collections.sort(accounts, new BookSorter());
                for (String[] file : accounts)
                    list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable
                            .drive)));
                list.add(new SectionItem());
            }} catch (Exception e) {
                e.printStackTrace();
            }
        try {
            for (String[] file : grid.readTableSecondary(BOOKS)) {
                books.add(file);
            }
            if (books.size() > 0) {
                Collections.sort(books, new BookSorter());
                for (String[] file : books)
                    list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable
                            .folder_fab)));
                list.add(new SectionItem());
            }
        } catch (Exception e) {

        }
        list.add(new EntryItem(getResources().getString(R.string.quick), "5", ContextCompat.getDrawable(this, R.drawable.ic_star_white_18dp)));
        list.add(new EntryItem(getResources().getString(R.string.recent), "6", ContextCompat.getDrawable(this, R.drawable.ic_history_white_48dp)));
        list.add(new EntryItem(getResources().getString(R.string.images), "0", ContextCompat.getDrawable(this, R.drawable.ic_doc_image)));
        list.add(new EntryItem(getResources().getString(R.string.videos), "1", ContextCompat.getDrawable(this, R.drawable.ic_doc_video_am)));
        list.add(new EntryItem(getResources().getString(R.string.audio), "2", ContextCompat.getDrawable(this, R.drawable.ic_doc_audio_am)));
        list.add(new EntryItem(getResources().getString(R.string.documents), "3", ContextCompat.getDrawable(this, R.drawable.ic_doc_doc_am)));
        list.add(new EntryItem(getResources().getString(R.string.apks), "4", ContextCompat.getDrawable(this, R.drawable.ic_doc_apk_grid)));
        adapter = new DrawerAdapter(this, list, MainActivity.this, Sp);
        mDrawerList.setAdapter(adapter);
    }

    public void updateDrawer(String path) {
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                String path = strings[0];
                int k = 0, i = 0;
                for (Item item : list) {
                    if (!item.isSection()) {
                        if (((EntryItem) item).getPath().equals(path))
                            k = i;
                    }
                    i++;
                }
                return k;
            }

            @Override
            public void onPostExecute(Integer integers) {
                if (adapter != null)
                    adapter.toggleChecked(integers);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

    }

    public void goToMain(String path) {
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //title.setText(R.string.app_name);
        TabFragment tabFragment = new TabFragment();
        if (path != null && path.length() > 0) {
            Bundle b = new Bundle();
            b.putString("path", path);
            tabFragment.setArguments(b);
        }
        transaction.replace(R.id.content_frame, tabFragment);
        // Commit the transaction
        select = 0;
        transaction.addToBackStack("tabt" + 1);
        transaction.commitAllowingStateLoss();
        setActionBarTitle(null);
        floatingActionButton.showMenuButton(true);
        if (openzip && zippath != null) {
            if (zippath.endsWith(".zip") || zippath.endsWith(".apk")) openZip(zippath);
            else {
                openRar(zippath);
            }
            zippath = null;
        }
    }

    public void selectItem(final int i) {
        if (!list.get(i).isSection())
            if ((select == null || select >= list.size()) ) {

                TabFragment tabFragment = new TabFragment();
                Bundle a = new Bundle();
                a.putString("path", ((EntryItem) list.get(i)).getPath());
                tabFragment.setArguments(a);

                android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, tabFragment);

                transaction.addToBackStack("tabt1" + 1);
                pending_fragmentTransaction = transaction;
                select = i;
                adapter.toggleChecked(select);
                if (!isDrawerLocked) mDrawerLayout.closeDrawer(mDrawerLinear);
                else onDrawerClosed();
                floatingActionButton.showMenuButton(true);


            } else {
                pending_path = ((EntryItem) list.get(i)).getPath();
                if(pending_path.equals("drive")){
                pending_path=((EntryItem) list.get(i)).getTitle();
                }
                select = i;
                adapter.toggleChecked(select);
                if (!isDrawerLocked) mDrawerLayout.closeDrawer(mDrawerLinear);
                else onDrawerClosed();

            }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_extra, menu);
        return super.onCreateOptionsMenu(menu);
    }
    public void setActionBarTitle(String title){
        if(toolbar!=null)
        toolbar.setTitle(title);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem s = menu.findItem(R.id.view);
        MenuItem search = menu.findItem(R.id.search);
        MenuItem paste = menu.findItem(R.id.paste);
        String f = null;
        Fragment fragment;
        try {
            fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            f = fragment.getClass().getName();
        } catch (Exception e1) {
            return true;
        }
        if (f.contains("TabFragment")) {
            setActionBarTitle("Amaze");
            if (aBoolean) {
                s.setTitle(getResources().getString(R.string.gridview));
            } else {
                s.setTitle(getResources().getString(R.string.listview));
            }
            try {
                TabFragment tabFragment = (TabFragment) fragment;
                Main ma = ((Main) tabFragment.getTab());
                if (ma.IS_LIST) s.setTitle(R.string.gridview);
                else s.setTitle(R.string.listview);
                updatePath(ma.CURRENT_PATH, ma.results, ma.openMode, ma.folder_count, ma.file_count);
            } catch (Exception e) {
            }

            initiatebbar();
            if (Build.VERSION.SDK_INT >= 21) toolbar.setElevation(0);
            invalidatePasteButton(paste);
            search.setVisible(true);
            if(indicator_layout!=null)indicator_layout.setVisibility(View.VISIBLE);
            menu.findItem(R.id.search).setVisible(true);
            menu.findItem(R.id.home).setVisible(true);
            menu.findItem(R.id.history).setVisible(true);
            menu.findItem(R.id.sethome).setVisible(true);

            menu.findItem(R.id.item10).setVisible(true);
            if (showHidden) menu.findItem(R.id.hiddenitems).setVisible(true);
            menu.findItem(R.id.view).setVisible(true);
            menu.findItem(R.id.extract).setVisible(false);
            invalidatePasteButton(menu.findItem(R.id.paste));
            findViewById(R.id.buttonbarframe).setVisibility(View.VISIBLE);
        } else if (f.contains("AppsList") || f.contains("ProcessViewer")) {
            menu.findItem(R.id.sethome).setVisible(false);
            if(indicator_layout!=null)indicator_layout.setVisibility(View.GONE);
            findViewById(R.id.buttonbarframe).setVisibility(View.GONE);
            menu.findItem(R.id.search).setVisible(false);
            menu.findItem(R.id.home).setVisible(false);
            menu.findItem(R.id.history).setVisible(false);
            menu.findItem(R.id.extract).setVisible(false);
            if (f.contains("ProcessViewer")) menu.findItem(R.id.item10).setVisible(false);
            else {
                menu.findItem(R.id.dsort).setVisible(false);
                menu.findItem(R.id.sortby).setVisible(false);
            }
            menu.findItem(R.id.hiddenitems).setVisible(false);
            menu.findItem(R.id.view).setVisible(false);
            menu.findItem(R.id.paste).setVisible(false);
        } else if (f.contains("ZipViewer")) {
            menu.findItem(R.id.sethome).setVisible(false);
            if(indicator_layout!=null)indicator_layout.setVisibility(View.GONE);
            TextView textView = (TextView) mainActivity.pathbar.findViewById(R.id.fullpath);
            pathbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            menu.findItem(R.id.search).setVisible(false);
            menu.findItem(R.id.home).setVisible(false);
            menu.findItem(R.id.history).setVisible(false);
            menu.findItem(R.id.item10).setVisible(false);
            menu.findItem(R.id.hiddenitems).setVisible(false);
            menu.findItem(R.id.view).setVisible(false);
            menu.findItem(R.id.paste).setVisible(false);
            menu.findItem(R.id.extract).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    void showToast(String message) {
        if (this.toast == null) {
            // Create toast if found null, it would he the case of first call only
            this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

        } else if (this.toast.getView() == null) {
            // Toast not showing, so create new one
            this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

        } else {
            // Updating toast message is showing
            this.toast.setText(message);
        }

        // Showing toast finally
        this.toast.show();
    }

    void killToast() {
        if (this.toast != null) {
            this.toast.cancel();
        }
    }

    public void back() {
        super.onBackPressed();
    }

    //// called when the user exits the action mode
//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        Main ma = null;
        try {
            ma = (Main) getFragment().getTab();
        } catch (ClassCastException e) {
        }
        switch (item.getItemId()) {
            case R.id.home:
                ma.home();
                break;
            case R.id.history:
                utils.showHistoryDialog(ma);
                break;
            case R.id.sethome:
                final  Main main=ma;
                if(main.openMode!=0){
                    Toast.makeText(mainActivity,R.string.not_allowed,Toast.LENGTH_SHORT).show();
                    break;
                }
                final MaterialDialog b=utils.showBasicDialog(mainActivity,new String[]{getResources().getString(R.string.questionset),getResources().getString(R.string.setashome),getResources().getString(R.string.yes),getResources().getString(R.string.no),null});
                b.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        main.home = main.CURRENT_PATH;
                        updatepaths(main.no);
                        b.dismiss();
                    }
                });
                b.show();
                break;
            case R.id.item3:
                if (rootmode) {
                    try {
                        RootTools.closeAllShells();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                finish();
                break;
            case R.id.item10:
                Fragment fragment = getDFragment();
                if (fragment.getClass().getName().contains("AppsList"))
                    utils.showSortDialog((AppsList) fragment);

                break;
            case R.id.sortby:
                utils.showSortDialog(ma);
                break;
            case R.id.dsort:
                String[] sort = getResources().getStringArray(R.array.directorysortmode);
                MaterialDialog.Builder a = new MaterialDialog.Builder(mainActivity);
                if (theme == 1) a.theme(Theme.DARK);
                a.title(R.string.directorysort);
                int current = Integer.parseInt(Sp.getString("dirontop", "0"));
                a.items(sort).itemsCallbackSingleChoice(current, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Sp.edit().putString("dirontop", "" + which).commit();
                        dialog.dismiss();
                        return true;
                    }
                });
                a.build().show();
                break;
            case R.id.hiddenitems:
                utils.showHiddenDialog(ma);
                break;
            case R.id.view:
                if (ma.IS_LIST) {
                    grid.addPath(null,ma.CURRENT_PATH,GRID,0);
                    gridfiles.add(ma.CURRENT_PATH);
                    grid.removePath(ma.CURRENT_PATH,LIST);
                } else {
                    if (gridfiles.contains(ma.CURRENT_PATH)) {
                        gridfiles.remove(ma.CURRENT_PATH);
                        grid.removePath(ma.CURRENT_PATH,GRID);
                    }
                    grid.addPath(null,ma.CURRENT_PATH,LIST,0);
                    listfiles.add(ma.CURRENT_PATH);

                }
                ma.switchView();
                break;
            case R.id.search:
                mainActivityHelper.search();
                break;
            case R.id.paste:
                String path = ma.CURRENT_PATH;
                ArrayList<String> arrayList = new ArrayList<String>();
                if (COPY_PATH != null) {
                    arrayList = COPY_PATH;
                    new CheckForFiles(ma, path, false).executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, arrayList);
                } else if (MOVE_PATH != null) {
                    arrayList = MOVE_PATH;
                    new CheckForFiles(ma, path, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            arrayList);
                }
                COPY_PATH = null;
                MOVE_PATH = null;

                invalidatePasteButton(item);
                break;
            case R.id.extract:
                Fragment fragment1 = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (fragment1.getClass().getName().contains("ZipViewer"))
                    mainActivityHelper.extractFile(((ZipViewer) fragment1).f);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) mDrawerToggle.syncState();
    }

    boolean mbound=false;
    public void bindDrive() {
        Intent i = new Intent();
        i.setClassName("com.amaze.filemanager.driveplugin", "com.amaze.filemanager.driveplugin.MainService");
        try {
            bindService((i), mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void unbindDrive(){
        if(mbound!=false)
       unbindService(mConnection);
    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectitem", select);

        if (oppathe != null) {
            outState.putString("oppathe", oppathe);
            outState.putString("oppathe1", oppathe1);
            if(opnameList!=null)
                outState.putStringArrayList("opnameList", (opnameList));
            outState.putStringArrayList("oparraylist", (oparrayList));
            outState.putInt("operation", operation);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mainActivityHelper.mNotificationReceiver);
        killToast();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (materialDialog != null && !materialDialog.isShowing()) {
            materialDialog.show();
            materialDialog = null;
        }
        IntentFilter newFilter = new IntentFilter();
        newFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        newFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        newFilter.addDataScheme(ContentResolver.SCHEME_FILE);
        registerReceiver(mainActivityHelper.mNotificationReceiver, newFilter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            /*ImageView ib = (ImageView) findViewById(R.id.action_overflow);
            if (ib.getVisibility() == View.VISIBLE) {
                ib.performClick();
            }*/
            // perform your desired action here

            // return 'true' to prevent further propagation of the key event
            return true;
        }

        // let the system handle all other key events
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sp.edit().putBoolean("remember", true).apply();
        unbindDrive();
        if (grid != null)
            grid.end();
        if (history != null)
            history.end();
        }

    public void updatepaths(int pos) {
        try {
            getFragment().updatepaths(pos);
        } catch (Exception e) {
        }
    }

    public void openZip(String path) {
        findViewById(R.id.lin).animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom);
        Fragment zipFragment = new ZipViewer();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        zipFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.content_frame, zipFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void openRar(String path) {
        openZip(path);
    }

    public TabFragment getFragment() {
        Fragment fragment= getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(!fragment.getClass().getName().contains("TabFragment"))return null;
        TabFragment tabFragment = (TabFragment)fragment ;
        return tabFragment;
    }

    public Fragment getDFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }

    public void setPagingEnabled(boolean b) {
        getFragment().mViewPager.setPagingEnabled(b);
    }

    public File getUsbDrive() {
        File parent;
        parent = new File("/storage");

        try {
            for (File f : parent.listFiles()) {
                if (f.exists() && f.getName().toLowerCase().contains("usb") && f.canExecute()) {
                    return f;
                }
            }
        } catch (Exception e) {
        }
        parent = new File("/mnt/sdcard/usbStorage");
        if (parent.exists() && parent.canExecute())
            return (parent);
        parent = new File("/mnt/sdcard/usb_storage");
        if (parent.exists() && parent.canExecute())
            return parent;

        return null;
    }

    public void refreshDrawer() {
        if(val==null)
        val = getStorageDirectories();
        list = new ArrayList<>();
        storage_count = 0;
        for (String file : val) {
            File f = new File(file);
            String name;
            Drawable icon1 = ContextCompat.getDrawable(this, R.drawable.ic_sd_storage_white_56dp);
            if ("/storage/emulated/legacy".equals(file) || "/storage/emulated/0".equals(file)) {
                name = getResources().getString(R.string.storage);
            } else if ("/storage/sdcard1".equals(file)) {
                name = getResources().getString(R.string.extstorage);
            } else if ("/".equals(file)) {
                name = getResources().getString(R.string.rootdirectory);
                icon1 = ContextCompat.getDrawable(this, R.drawable.ic_drawer_root_white);
            } else name = f.getName();
            if (!f.isDirectory() || f.canExecute()) {
                storage_count++;
                list.add(new EntryItem(name, file, icon1));
            }
        }
        list.add(new SectionItem());
        if (Servers != null && Servers.size() > 0) {
            for (String[] file : Servers) {
                list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable.ic_settings_remote_white_48dp)));
            }

            list.add(new SectionItem());
        }
        if (accounts != null && accounts.size() > 0) {
            for (String[] file : accounts) {
                list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable.drive)));
            }

            list.add(new SectionItem());
        }
        if (books != null && books.size() > 0) {

            for (String[] file : books) {
                list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable
                        .folder_fab)));
            }
            list.add(new SectionItem());
        }
        list.add(new EntryItem(getResources().getString(R.string.quick), "5", ContextCompat.getDrawable(this, R.drawable.ic_star_white_18dp)));
        list.add(new EntryItem(getResources().getString(R.string.recent), "6", ContextCompat.getDrawable(this, R.drawable.ic_history_white_48dp)));
        list.add(new EntryItem(getResources().getString(R.string.images), "0", ContextCompat.getDrawable(this, R.drawable.ic_doc_image)));
        list.add(new EntryItem(getResources().getString(R.string.videos), "1", ContextCompat.getDrawable(this, R.drawable.ic_doc_video_am)));
        list.add(new EntryItem(getResources().getString(R.string.audio), "2", ContextCompat.getDrawable(this, R.drawable.ic_doc_audio_am)));
        list.add(new EntryItem(getResources().getString(R.string.documents), "3", ContextCompat.getDrawable(this, R.drawable.ic_doc_doc_am)));
        list.add(new EntryItem(getResources().getString(R.string.apks), "4", ContextCompat.getDrawable(this, R.drawable.ic_doc_apk_grid)));
        adapter = new DrawerAdapter(con, list, MainActivity.this, Sp);
        mDrawerList.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // check if user enabled g+ api from preferences
        if (mGoogleApiClient != null) {

            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

            String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
            Person.Image personImage;
            Person.Cover.CoverPhoto personCover;

            try {

                personImage = currentPerson.getImage();
                personCover = currentPerson.getCover().getCoverPhoto();
            } catch (Exception e) {

                personCover = null;
                personImage = null;
            }

            if (personCover != null && personImage != null) {

                String imgUrl = personImage.getUrl();

                // getting full size image
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(imgUrl);
                stringBuilder.delete(imgUrl.length() - 6, imgUrl.length());
                Log.d("G+", stringBuilder.toString());
                mGoogleName.setText(currentPerson.getDisplayName());
                mGoogleId.setText(accountName);
                // setting cover pic
                ImageLoader.getInstance().loadImage(personCover.getUrl(), displayImageOptions, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        drawerHeaderParent.setBackgroundColor(Color.parseColor("#ffffff"));
                        drawerHeaderView.setBackground(new BitmapDrawable(loadedImage));
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        drawerHeaderView.setBackgroundResource(R.drawable.amaze_header);
                        drawerHeaderParent.setBackgroundColor(Color.parseColor(skin));
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        drawerHeaderView.setBackgroundResource(R.drawable.amaze_header);
                        drawerHeaderParent.setBackgroundColor(Color.parseColor(skin));
                    }
                });

                // setting profile pic
                ImageLoader.getInstance().loadImage(stringBuilder.toString(), displayImageOptions, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                        super.onLoadingComplete(imageUri, view, loadedImage);

                        drawerProfilePic.setImageBitmap(loadedImage);
                        drawerProfilePic.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                    }
                });
            } else {
                Toast.makeText(this, getResources().getText(R.string.no_cover_photo), Toast.LENGTH_SHORT).show();
                drawerHeaderView.setBackgroundResource(R.drawable.amaze_header);
                drawerHeaderParent.setBackgroundColor(Color.parseColor(skin));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d("G+", "Connection suspended");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mGoogleApiClient != null) {

                    mGoogleApiClient.connect();
                }
            }
        }).run();
    }

    public void onConnectionFailed(final ConnectionResult result) {
        Log.d("G+", "Connection failed");
        if (!mIntentInProgress && result.hasResolution()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mIntentInProgress = true;
                        startIntentSenderForResult(result.getResolution().getIntentSender(),
                                RC_SIGN_IN, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        // The intent was canceled before it was sent.  Return to the default
                        // state and attempt to connect to get an updated ConnectionResult.
                        mIntentInProgress = false;
                        if (mGoogleApiClient != null) {

                            mGoogleApiClient.connect();
                        }
                    }
                }
            }).run();
        }
    }
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN && !mGoogleApiKey && mGoogleApiClient != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mIntentInProgress = false;
                    mGoogleApiKey = true;
                    // !mGoogleApiClient.isConnecting
                    if (mGoogleApiClient.isConnecting()) {
                        mGoogleApiClient.connect();
                    } else
                        mGoogleApiClient.disconnect();

                }
            }).run();
        }
        else if(requestCode==image_selector_request_code){
            if(Sp!=null && intent!=null && intent.getData()!=null){
                if(Build.VERSION.SDK_INT>=19)
                getContentResolver().takePersistableUriPermission(intent.getData(),Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Sp.edit().putString("drawer_header_path",intent.getData().toString()).commit();
                setDrawerHeaderBackground();
            }
        }
        else if (requestCode == 3) {
            String p = Sp.getString("URI", null);
            Uri oldUri = null;
            if (p != null) oldUri = Uri.parse(p);
            Uri treeUri = null;
            if (responseCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = intent.getData();
                // Persist URI - this is required for verification of writability.
                if (treeUri != null) Sp.edit().putString("URI", treeUri.toString()).commit();
            }

            // If not confirmed SAF, or if still not writable, then revert settings.
            if (responseCode != Activity.RESULT_OK) {
               /* DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder_saf, false,
                        currentFolder);||!FileUtil.isWritableNormalOrSaf(currentFolder)
*/
                if (treeUri != null) Sp.edit().putString("URI", oldUri.toString()).commit();
                return;
            }

            // After confirmation, update stored value of folder.
            // Persist access permissions.
            final int takeFlags = intent.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
            switch (operation) {
                case DELETE://deletion
                    new DeleteTask(null, mainActivity).execute((oparrayList));
                    break;
                case COPY://copying
                    Intent intent1 = new Intent(con, CopyService.class);
                    intent1.putExtra("FILE_PATHS", (oparrayList));
                    intent1.putExtra("COPY_DIRECTORY", oppathe);
                    intent1.putExtra("FILE_NAMES",opnameList);
                    startService(intent1);
                    break;
                case MOVE://moving
                    new MoveFiles(utils.toFileArray(oparrayList),opnameList, ((Main) getFragment().getTab()), ((Main) getFragment().getTab()).getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
                    break;
                case NEW_FOLDER://mkdir
                    Main ma1 = ((Main) getFragment().getTab());
                    mainActivityHelper.mkDir((oppathe), ma1);
                    break;
                case RENAME:
                    mainActivityHelper.rename((oppathe), (oppathe1));
                    Main ma2 = ((Main) getFragment().getTab());
                    ma2.updateList();
                    break;
                case NEW_FILE:
                    Main ma3 = ((Main) getFragment().getTab());
                    mainActivityHelper.mkFile((oppathe), ma3);

                    break;
                case EXTRACT:
                    mainActivityHelper.extractFile(new File(oppathe));
                    break;
                case COMPRESS:
                    mainActivityHelper.compressFiles(new File(oppathe), oparrayList);
            }operation=-1;
        }
    }



    public void bbar(final Main main) {
        final String text = main.CURRENT_PATH;
        try {
            buttons.removeAllViews();
            buttons.setMinimumHeight(pathbar.getHeight());
            Drawable arrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_holo_dark);
            Bundle b = utils.getPaths(text, this);
            ArrayList<String> names = b.getStringArrayList("names");
            ArrayList<String> rnames = new ArrayList<String>();

            for (int i = names.size() - 1; i >= 0; i--) {
                rnames.add(names.get(i));
            }

            ArrayList<String> paths = b.getStringArrayList("paths");
            final ArrayList<String> rpaths = new ArrayList<String>();

            for (int i = paths.size() - 1; i >= 0; i--) {
                rpaths.add(paths.get(i));
            }
            View view = new View(this);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    toolbar.getContentInsetLeft(), LinearLayout.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(params1);
            buttons.addView(view);
            for (int i = 0; i < names.size(); i++) {
                final int k = i;
                ImageView v = new ImageView(this);
                v.setImageDrawable(arrow);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                v.setLayoutParams(params);
                final int index = i;
                if (rpaths.get(i).equals("/")) {
                    ImageButton ib = new ImageButton(this);
                    ib.setImageDrawable(icons.getRootDrawable());
                    ib.setBackgroundColor(Color.parseColor("#00ffffff"));
                    ib.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View p1) {
                            main.loadlist(("/"), false, main.openMode);
                            timer.cancel();
                            timer.start();
                        }
                    });
                    ib.setLayoutParams(params);
                    buttons.addView(ib);
                    if (names.size() - i != 1)
                        buttons.addView(v);
                } else if (isStorage(rpaths.get(i))) {
                    ImageButton ib = new ImageButton(this);
                    ib.setImageDrawable(icons.getSdDrawable());
                    ib.setBackgroundColor(Color.parseColor("#00ffffff"));
                    ib.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View p1) {
                            main.loadlist((rpaths.get(k)), false, main.openMode);
                            timer.cancel();
                            timer.start();
                        }
                    });
                    ib.setLayoutParams(params);
                    buttons.addView(ib);
                    if (names.size() - i != 1)
                        buttons.addView(v);
                } else {
                    Button button = new Button(this);
                    button.setText(rnames.get(index));
                    button.setTextColor(getResources().getColor(android.R.color.white));
                    button.setTextSize(13);
                    button.setLayoutParams(params);
                    button.setBackgroundResource(0);
                    button.setOnClickListener(new Button.OnClickListener() {

                        public void onClick(View p1) {
                            main.loadlist((rpaths.get(k)), false, main.openMode);
                            main.loadlist((rpaths.get(k)), false, main.openMode);
                            timer.cancel();
                            timer.start();
                        }
                    });
                    button.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {

                            File file1 = new File(rpaths.get(index));
                            copyToClipboard(MainActivity.this, file1.getPath());
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.pathcopied), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    });

                    buttons.addView(button);
                    if (names.size() - i != 1)
                        buttons.addView(v);
                }
            }

            scroll.post(new Runnable() {
                @Override
                public void run() {
                    sendScroll(scroll);
                    sendScroll(scroll1);
                }
            });

            if (buttons.getVisibility() == View.VISIBLE) {
                timer.cancel();
                timer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("button view not available");
        }
    }

    boolean isStorage(String path) {
        for (int i = 0; i < storage_count; i++)
            if (((EntryItem) list.get(i)).getPath().equals(path)) return true;
        return false;
    }

    void sendScroll(final HorizontalScrollView scrollView) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_RIGHT);
                    }
                });
            }
        }).start();
    }
    void initialisePreferences(){

        int th = Integer.parseInt(Sp.getString("theme", "0"));
        theme1 = th == 2 ? PreferenceUtils.hourOfDay() : th;

        fabskin = PreferenceUtils.getFabColor(Sp.getInt("fab_skin_color_position", 1));
        boolean random = Sp.getBoolean("random_checkbox", false);
        if (random)
            skin = PreferenceUtils.random(Sp);
        else
            skin = PreferenceUtils.getSkinColor(Sp.getInt("skin_color_position", 4));
        rootmode = Sp.getBoolean("rootmode", false);
        theme = Integer.parseInt(Sp.getString("theme", "0"));
        hidemode = Sp.getInt("hidemode", 0);
        showHidden = Sp.getBoolean("showHidden", false);
        topfab = hidemode == 0 ? Sp.getBoolean("topFab", false) : false;
        skinStatusBar = (PreferenceUtils.getStatusColor(skin));
        aBoolean = Sp.getBoolean("view", true);
    }
    void initialiseViews(){
        buttonBarFrame = (FrameLayout) findViewById(R.id.buttonbarframe);
        buttonBarFrame.setBackgroundColor(Color.parseColor(skin));
        drawerHeaderLayout = getLayoutInflater().inflate(R.layout.drawerheader, null);
        drawerHeaderParent = (RelativeLayout) drawerHeaderLayout.findViewById(R.id.drawer_header_parent);
        drawerHeaderView = (View) drawerHeaderLayout.findViewById(R.id.drawer_header);
        drawerHeaderView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent;
                if (Build.VERSION.SDK_INT < 19){
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                intent.setType("image/*");
                startActivityForResult(intent, image_selector_request_code);
                return false;
            }
        });
        drawerProfilePic = (RoundedImageView) drawerHeaderLayout.findViewById(R.id.profile_pic);
        mGoogleName = (TextView) drawerHeaderLayout.findViewById(R.id.account_header_drawer_name);
        mGoogleId = (TextView) drawerHeaderLayout.findViewById(R.id.account_header_drawer_email);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        indicator_layout=findViewById(R.id.indicator_layout);
        mDrawerLinear = (ScrimInsetsRelativeLayout) findViewById(R.id.left_drawer);
        if (theme1 == 1) mDrawerLinear.setBackgroundColor(Color.parseColor("#303030"));
        else mDrawerLinear.setBackgroundColor(Color.WHITE);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(Color.parseColor(skin));
        mDrawerList = (ListView) findViewById(R.id.menu_drawer);
        drawerHeaderView.setBackgroundResource(R.drawable.amaze_header);
        drawerHeaderParent.setBackgroundColor(Color.parseColor(skin));
        if (findViewById(R.id.tab_frame)!=null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, mDrawerLinear);
            mDrawerLayout.setScrimColor(Color.TRANSPARENT);
            isDrawerLocked = true;
        }
        mDrawerList.addHeaderView(drawerHeaderLayout);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        View v = findViewById(R.id.fab_bg);
        if (theme1 == 1)
            v.setBackgroundColor(Color.parseColor("#a6ffffff"));
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionButton.close(true);
                revealShow(view, false);
            }
        });

        pathbar = (LinearLayout) findViewById(R.id.pathbar);
        buttons = (LinearLayout) findViewById(R.id.buttons);
        scroll = (HorizontalScrollView) findViewById(R.id.scroll);
        scroll1 = (HorizontalScrollView) findViewById(R.id.scroll1);
        scroll.setSmoothScrollingEnabled(true);
        scroll1.setSmoothScrollingEnabled(true);
        ImageView divider = (ImageView) findViewById(R.id.divider1);
        if (theme1 == 0)
            divider.setImageResource(R.color.divider);
        else
            divider.setImageResource(R.color.divider_dark);

        setDrawerHeaderBackground();
        View settingsbutton = findViewById(R.id.settingsbutton);
        if (theme1 == 1) {
            settingsbutton.setBackgroundResource(R.drawable.safr_ripple_black);
            ((ImageView) settingsbutton.findViewById(R.id.settingicon)).setImageResource(R.drawable.ic_settings_white_48dp);
            ((TextView) settingsbutton.findViewById(R.id.settingtext)).setTextColor(getResources().getColor(android.R.color.white));
        }
        settingsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, Preferences.class);
                finish();
                final int enter_anim = android.R.anim.fade_in;
                final int exit_anim = android.R.anim.fade_out;
                Activity s = MainActivity.this;
                s.overridePendingTransition(exit_anim, enter_anim);
                s.finish();
                s.overridePendingTransition(enter_anim, exit_anim);
                s.startActivity(in);
            }

        });
        View appbutton = findViewById(R.id.appbutton);
        if (theme1 == 1) {
            appbutton.setBackgroundResource(R.drawable.safr_ripple_black);
            ((ImageView) appbutton.findViewById(R.id.appicon)).setImageResource(R.drawable.ic_doc_apk_white);
            ((TextView) appbutton.findViewById(R.id.apptext)).setTextColor(getResources().getColor(android.R.color.white));
        }
        appbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.support.v4.app.FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                transaction2.replace(R.id.content_frame, new AppsList());
                findViewById(R.id.lin).animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                pending_fragmentTransaction = transaction2;
                if (!isDrawerLocked) mDrawerLayout.closeDrawer(mDrawerLinear);
                else onDrawerClosed();
                select = list.size() + 1;
                adapter.toggleChecked(false);
            }
        });
        if (topfab) {
            buttonBarFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) floatingActionButton.getLayoutParams();
                    layoutParams.setMargins(layoutParams.leftMargin, findViewById(R.id.lin)
                                    .getBottom() - (floatingActionButton.getMenuIconView().getHeight
                                    ()),
                            layoutParams.rightMargin,
                            layoutParams.bottomMargin);
                    floatingActionButton.setLayoutParams(layoutParams);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        buttonBarFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        buttonBarFrame.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }

            });
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(skin)));


        // status bar0
        sdk = Build.VERSION.SDK_INT;

        if (sdk == 20 || sdk == 19) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(Color.parseColor(skin));
            FrameLayout.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) findViewById(R.id.drawer_layout).getLayoutParams();
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            if (!isDrawerLocked) p.setMargins(0, config.getStatusBarHeight(), 0, 0);
        } else if (Build.VERSION.SDK_INT >= 21) {
            colourednavigation = Sp.getBoolean("colorednavigation", true);

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (isDrawerLocked) {
                window.setStatusBarColor((skinStatusBar));
            } else window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (colourednavigation)
                window.setNavigationBarColor(skinStatusBar);

        }
    }
    void intialiseFab(){
        int icon=Sp.getInt("icon_skin_color_position", -1);
        icon=icon==-1?Sp.getInt("skin_color_position", 4):icon;
        String folder_skin = PreferenceUtils.getSkinColor(icon);
        int fabSkinPressed = PreferenceUtils.getStatusColor(fabskin);
        int folderskin = Color.parseColor(folder_skin);
        int fabskinpressed = (PreferenceUtils.getStatusColor(folder_skin));
        floatingActionButton = !topfab ?
                (FloatingActionMenu) findViewById(R.id.menu) : (FloatingActionMenu) findViewById(R.id.menu_top);
        floatingActionButton.setVisibility(View.VISIBLE);
        floatingActionButton.showMenuButton(true);
        floatingActionButton.setMenuButtonColorNormal(Color.parseColor(fabskin));
        floatingActionButton.setMenuButtonColorPressed(fabSkinPressed);

        //if (theme1 == 1) floatingActionButton.setMen
        floatingActionButton.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean b) {
                View v = findViewById(R.id.fab_bg);
                if (b) revealShow(v, true);
                else revealShow(v, false);
            }
        });

        FloatingActionButton floatingActionButton1 = (FloatingActionButton) findViewById(topfab ? R.id.menu_item_top : R.id.menu_item);
        floatingActionButton1.setColorNormal(folderskin);
        floatingActionButton1.setColorPressed(fabskinpressed);
        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivityHelper.add(0);
                revealShow(findViewById(R.id.fab_bg), false);
                floatingActionButton.close(true);
            }
        });
        FloatingActionButton floatingActionButton2 = (FloatingActionButton) findViewById(topfab ? R.id.menu_item1_top : R.id.menu_item1);
        floatingActionButton2.setColorNormal(folderskin);
        floatingActionButton2.setColorPressed(fabskinpressed);
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivityHelper.add(1);
                revealShow(findViewById(R.id.fab_bg), false);
                floatingActionButton.close(true);
            }
        });
        FloatingActionButton floatingActionButton3 = (FloatingActionButton) findViewById(topfab ? R.id.menu_item2_top : R.id.menu_item2);
        floatingActionButton3.setColorNormal(folderskin);
        floatingActionButton3.setColorPressed(fabskinpressed);
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivityHelper.add(2);
                revealShow(findViewById(R.id.fab_bg), false);
                floatingActionButton.close(true);
            }
        });
        final FloatingActionButton floatingActionButton4 = (FloatingActionButton) findViewById(topfab ? R.id.menu_item3_top : R.id.menu_item3);
        floatingActionButton4.setColorNormal(folderskin);
        floatingActionButton4.setColorPressed(fabskinpressed);
        floatingActionButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivityHelper.add(3);
                revealShow(findViewById(R.id.fab_bg), false);
                floatingActionButton.close(true);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                PackageManager pm = getPackageManager();
                boolean app_installed;
                try {
                    pm.getPackageInfo("com.amaze.filemanager.driveplugin", PackageManager.GET_ACTIVITIES);
                    app_installed = true;
                }
                catch (PackageManager.NameNotFoundException e) {
                    app_installed = false;
                }
                if(!app_installed)floatingActionButton4.setVisibility(View.GONE);
            }
        }).run();
    }
    public void updatePath(@NonNull final String news, boolean results, int
            openmode, int folder_count, int file_count) {

        if (news.length() == 0) return;
        File f = null;
        if (news == null) return;
        if (openmode == 1 && news.startsWith("smb:/"))
            newPath = mainActivityHelper.parseSmbPath(news);
        else if (openmode == 2)
            newPath=mainActivityHelper.getIntegralNames(news);
        else newPath = news;
        try {
            f = new File(newPath);
        } catch (Exception e) {
            return;
        }
        final TextView bapath = (TextView) pathbar.findViewById(R.id.fullpath);
        final TextView animPath = (TextView) pathbar.findViewById(R.id.fullpath_anim);
        if (!results) {
            TextView textView = (TextView) pathbar.findViewById(R.id.pathname);
            textView.setText(folder_count + " " + getResources().getString(R.string.folders) + "" +
                    " " + file_count + " " + getResources().getString(R.string.files));
        }
        final String oldPath = bapath.getText().toString();
        if (oldPath != null && oldPath.equals(newPath)) return;

        // implement animation while setting text
        newPathBuilder = new StringBuilder().append(newPath);
        oldPathBuilder = new StringBuilder().append(oldPath);

        final Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out);

        if (newPath.length() >= oldPath.length() &&
                newPathBuilder.delete(oldPath.length(), newPath.length()).toString().equals(oldPath) &&
                oldPath.length()!=0) {

            // navigate forward
            newPathBuilder.delete(0, newPathBuilder.length());
            newPathBuilder.append(newPath);
            newPathBuilder.delete(0, oldPath.length());
            animPath.setAnimation(slideIn);
            animPath.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animPath.setVisibility(View.GONE);
                    bapath.setText(newPath);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    animPath.setVisibility(View.VISIBLE);
                    animPath.setText(newPathBuilder.toString());
                    //bapath.setText(oldPath);

                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll1.fullScroll(View.FOCUS_RIGHT);
                        }
                    });
                }
            }).setStartDelay(300).start();
        } else if (newPath.length() <= oldPath.length() &&
                oldPathBuilder.delete(newPath.length(), oldPath.length()).toString().equals(newPath)) {

            // navigate backwards
            oldPathBuilder.delete(0, oldPathBuilder.length());
            oldPathBuilder.append(oldPath);
            oldPathBuilder.delete(0, newPath.length());
            animPath.setAnimation(slideOut);
            animPath.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animPath.setVisibility(View.GONE);
                    bapath.setText(newPath);

                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll1.fullScroll(View.FOCUS_RIGHT);
                        }
                    });
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    animPath.setVisibility(View.VISIBLE);
                    animPath.setText(oldPathBuilder.toString());
                    bapath.setText(newPath);

                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll1.fullScroll(View.FOCUS_LEFT);
                        }
                    });
                }
            }).setStartDelay(300).start();
        } else if (oldPath.isEmpty()) {

            // case when app starts
            // FIXME: COUNTER is incremented twice on app startup
            COUNTER++;
            if (COUNTER==2) {

                animPath.setAnimation(slideIn);
                animPath.setText(newPath);
                animPath.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        animPath.setVisibility(View.VISIBLE);
                        bapath.setText("");
                        scroll.post(new Runnable() {
                            @Override
                            public void run() {
                                scroll1.fullScroll(View.FOCUS_RIGHT);
                            }
                        });
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animPath.setVisibility(View.GONE);
                        bapath.setText(newPath);
                    }
                }).setStartDelay(300).start();
            }

        } else {

            // completely different path
            // first slide out of old path followed by slide in of new path
            animPath.setAnimation(slideOut);
            animPath.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    animPath.setVisibility(View.VISIBLE);
                    animPath.setText(oldPath);
                    bapath.setText("");

                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll1.fullScroll(View.FOCUS_LEFT);
                        }
                    });
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);

                    //animPath.setVisibility(View.GONE);
                    animPath.setText(newPath);
                    bapath.setText("");
                    animPath.setAnimation(slideIn);

                    animPath.animate().setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            animPath.setVisibility(View.GONE);
                            bapath.setText(newPath);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            // we should not be having anything here in path bar
                            animPath.setVisibility(View.VISIBLE);
                            bapath.setText("");
                            scroll.post(new Runnable() {
                                @Override
                                public void run() {
                                    scroll1.fullScroll(View.FOCUS_RIGHT);
                                }
                            });
                        }
                    }).start();
                }
            }).setStartDelay(500).start();
        }
    }

    public int dpToPx(double dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)));
        return px;
    }

    public void initiatebbar() {
        View pathbar = findViewById(R.id.pathbar);
        TextView textView = (TextView) findViewById(R.id.fullpath);

        pathbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main m = ((Main) getFragment().getTab());
                if (m.openMode == 0) {
                    bbar(m);
                    crossfade();
                    timer.cancel();
                    timer.start();
                }
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main m = ((Main) getFragment().getTab());
                if (m.openMode == 0) {
                    bbar(m);
                    crossfade();
                    timer.cancel();
                    timer.start();
                }
            }
        });

    }

    public void crossfade() {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        buttons.setAlpha(0f);
        buttons.setVisibility(View.VISIBLE);


        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        buttons.animate()
                .alpha(1f)
                .setDuration(100)
                .setListener(null);
        pathbar.animate()
                .alpha(0f)
                .setDuration(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pathbar.setVisibility(View.GONE);
                    }
                });
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)

    }

    void crossfadeInverse() {


        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.

        pathbar.setAlpha(0f);
        pathbar.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        pathbar.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);
        buttons.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        buttons.setVisibility(View.GONE);
                    }
                });
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
    }
    void setTheme(){
        if (Build.VERSION.SDK_INT >= 21) {

            switch (fabskin) {
                case "#F44336":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_red);
                    else
                        setTheme(R.style.pref_accent_dark_red);
                    break;

                case "#e91e63":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_pink);
                    else
                        setTheme(R.style.pref_accent_dark_pink);
                    break;

                case "#9c27b0":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_purple);
                    else
                        setTheme(R.style.pref_accent_dark_purple);
                    break;

                case "#673ab7":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_deep_purple);
                    else
                        setTheme(R.style.pref_accent_dark_deep_purple);
                    break;

                case "#3f51b5":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_indigo);
                    else
                        setTheme(R.style.pref_accent_dark_indigo);
                    break;

                case "#2196F3":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_blue);
                    else
                        setTheme(R.style.pref_accent_dark_blue);
                    break;

                case "#03A9F4":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_light_blue);
                    else
                        setTheme(R.style.pref_accent_dark_light_blue);
                    break;

                case "#00BCD4":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_cyan);
                    else
                        setTheme(R.style.pref_accent_dark_cyan);
                    break;

                case "#009688":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_teal);
                    else
                        setTheme(R.style.pref_accent_dark_teal);
                    break;

                case "#4CAF50":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_green);
                    else
                        setTheme(R.style.pref_accent_dark_green);
                    break;

                case "#8bc34a":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_light_green);
                    else
                        setTheme(R.style.pref_accent_dark_light_green);
                    break;

                case "#FFC107":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_amber);
                    else
                        setTheme(R.style.pref_accent_dark_amber);
                    break;

                case "#FF9800":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_orange);
                    else
                        setTheme(R.style.pref_accent_dark_orange);
                    break;

                case "#FF5722":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_deep_orange);
                    else
                        setTheme(R.style.pref_accent_dark_deep_orange);
                    break;

                case "#795548":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_brown);
                    else
                        setTheme(R.style.pref_accent_dark_brown);
                    break;

                case "#212121":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_black);
                    else
                        setTheme(R.style.pref_accent_dark_black);
                    break;

                case "#607d8b":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_blue_grey);
                    else
                        setTheme(R.style.pref_accent_dark_blue_grey);
                    break;

                case "#004d40":
                    if (theme1 == 0)
                        setTheme(R.style.pref_accent_light_super_su);
                    else
                        setTheme(R.style.pref_accent_dark_super_su);
                    break;
            }
        } else {
            if (theme1 == 1) {
                setTheme(R.style.appCompatDark);
            } else {
                setTheme(R.style.appCompatLight);
            }
        }

    }
    public boolean copyToClipboard(Context context, String text) {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                    .getSystemService(context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("Path copied to clipboard", text);
            clipboard.setPrimaryClip(clip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    void revealShow(final View view, boolean reveal) {

        if (reveal) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);
            animator.setDuration(300); //ms
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }
            });
            animator.start();
        } else {

            ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f);
            animator.setDuration(300); //ms
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
            animator.start();

        }

    }
    public void invalidateFab(int openmode){
        if(openmode==2)
            floatingActionButton.hideMenuButton(true);
        else floatingActionButton.showMenuButton(true);
    }
    public void renameBookmark(final String title, final String path) {
        if (mainActivityHelper.contains(path, books) != -1 || mainActivityHelper.contains(path, accounts) != -1) {
            final MaterialDialog materialDialog = utils.showNameDialog(this, new String[]{utils.getString(this, R.string.entername), title, utils.getString(this, R.string.rename), utils.getString(this, R.string.save), utils.getString(this, R.string.cancel), utils.getString(this, R.string.delete)});
            materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String t = materialDialog.getInputEditText().getText().toString();
                    int i=-1;
                    if((i= mainActivityHelper.contains(path, books))!=-1){
                    if (!t.equals(title) && t.length() >= 1) {
                        books.remove(i);
                        books.add(i, new String[]{t, path});
                        grid.rename(path, t, BOOKS);
                        Collections.sort(books, new BookSorter());
                        refreshDrawer();
                    }}
                    else if((i=mainActivityHelper.contains(path, accounts))!=-1)
                    {
                        accounts.remove(i);
                        accounts.add(i, new String[]{t, path});
                        grid.rename(path, t,DRIVE);
                        Collections.sort(accounts, new BookSorter());
                        refreshDrawer();
                    }
                    materialDialog.dismiss();

                }
            });
            materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i=-1;
                    if((i=mainActivityHelper.contains(path, books))!=-1){
                    books.remove(i);
                    grid.removePath(path, BOOKS);
                    }
                    else if ((i=mainActivityHelper.contains(path, accounts))!=-1)
                    {
                        accounts.remove(i);
                        grid.removePath(path, DRIVE);
                    }
                    refreshDrawer();
                    materialDialog.dismiss();
                }
            });
            materialDialog.show();
        }
    }

    void onDrawerClosed() {
        if (pending_fragmentTransaction != null) {
            pending_fragmentTransaction.commit();
            pending_fragmentTransaction = null;
        }
        if (pending_path != null) {
            try {
                TabFragment m = getFragment();
                HFile hFile = new HFile(pending_path);
                Main main = ((Main) m.getTab());
                if (main != null)
                    if(hFile.isSimpleFile()) utils.openFile(new File(pending_path), mainActivity);
                    else main.loadlist(pending_path,false,-1);
            } catch (ClassCastException e) {
                select = null;
                goToMain("");
            }
            pending_path = null;
        }
        supportInvalidateOptionsMenu();
    }


    @Override
    public void onNewIntent(Intent i) {
        intent = i;
        path = i.getStringExtra("path");
        if (path != null) {
            if(new File(path).isDirectory()){
                Fragment f=getDFragment();
                if((f.getClass().getName().contains("TabFragment"))){
                Main m = ((Main) getFragment().getTab());
                 m.loadlist(path, false, 0);
                }else goToMain(path);
            }
            else utils.openFile(new File(path),mainActivity);
        }
        else if((openprocesses = i.getBooleanExtra("openprocesses", false))!=false){

            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, new ProcessViewer());
            //   transaction.addToBackStack(null);
            select = 102;
            openprocesses = false;
            //title.setText(utils.getString(con, R.string.process_viewer));
            //Commit the transaction
            transaction.commitAllowingStateLoss();
            supportInvalidateOptionsMenu();
        }
        if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)) {

            // file picker intent
            mReturnIntent = true;
            Toast.makeText(this, utils.getString(con, R.string.pick_a_file), Toast.LENGTH_LONG).show();
        } else if (intent.getAction().equals(RingtoneManager.ACTION_RINGTONE_PICKER)) {
            // ringtone picker intent
            mReturnIntent = true;
            mRingtonePickerIntent = true;
            Toast.makeText(this, utils.getString(con, R.string.pick_a_file), Toast.LENGTH_LONG).show();
        } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {

            // zip viewer intent
            Uri uri = intent.getData();
            zippath = uri.toString();
            openZip(zippath);
        }
    }
    void setDrawerHeaderBackground(){
        new Thread(new Runnable() {
            public void run() {
                if(Sp.getBoolean("plus_pic", false))return;
                String path=Sp.getString("drawer_header_path",null);
                if(path==null)return;
                try {
                    ImageLoader.getInstance().loadImage(path,displayImageOptions, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap b) {
                            if(b==null)return;
                            Drawable d = new BitmapDrawable(getResources(), b);
                            if(d==null)return;
                            drawerHeaderParent.setBackgroundDrawable(d);
                            drawerHeaderView.setBackgroundResource(R.drawable.amaze_header);

                        }

                        @Override
                        public void onLoadingCancelled(String s, View view) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).run();
    }



    public void translateDrawerList(boolean down) {
        if (down)
            mDrawerList.animate().translationY(toolbar.getHeight());
        else mDrawerList.setTranslationY(0);
    }
    Loadlistener loadlistener=new Loadlistener.Stub() {
        @Override
        public void load(final List<Layoutelements> layoutelements, String driveId) throws RemoteException {
            if(layoutelements==null && mainActivityHelper.contains(driveId,accounts)==-1) {
                accounts.add(new String[]{driveId, driveId});
                grid.addPath(driveId, driveId, DRIVE, 1);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshDrawer();
                    }
                });
                unbindDrive();

            }
        }

        @Override
        public void error(final String message, final int mode) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity, "Error " + message+mode, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            aidlInterface = (IMyAidlInterface.Stub.asInterface(service));
            mbound=true;
            try {
                aidlInterface.registerCallback(loadlistener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                aidlInterface.create();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mbound=false;
            Log.d("DriveConnection", "DisConnected");
            aidlInterface = null;
        }
    };
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == 77) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateDrawer();
                TabFragment tabFragment=getFragment();
                if(tabFragment!=null){
                    Fragment main=tabFragment.getTab(0);
                    if(main!=null)
                        ((Main)main).updateList();
                    Fragment main1=tabFragment.getTab(1);
                    if(main1!=null)
                        ((Main)main1).updateList();
                }
            } else {
                Toast.makeText(this,R.string.grantfailed,Toast.LENGTH_SHORT).show();
                requestStoragePermission();
            }

        }}
    public boolean checkStoragePermission() {

        // Verify that all required contact permissions have been granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            final MaterialDialog materialDialog=utils.showBasicDialog(this, new String[]{getResources().getString(R.string.granttext), getResources().getString(R.string.grantper), getResources().getString(R.string.grant), getResources().getString(R.string.cancel),null});
            materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat
                            .requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 77);
                    materialDialog.dismiss();
                }
            });
            materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            materialDialog.setCancelable(false);
            materialDialog.show();

        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 77);
        }
    }
    class CheckForFiles extends AsyncTask<ArrayList<String>, String, ArrayList<String>> {
        Main ma;
        String path;
        Boolean move;
        ArrayList<String> ab, a, b, lol,names;
        int counter = 0;

        public CheckForFiles(Main main, String path, Boolean move) {
            this.ma = main;
            this.path = path;
            this.move = move;
            a = new ArrayList<String>();
            b = new ArrayList<String>();
            lol = new ArrayList<String>();
            names=new ArrayList<>();
        }

        @Override
        public void onProgressUpdate(String... message) {
            Toast.makeText(con, message[0], Toast.LENGTH_LONG).show();
        }

        @Override
        // Actual download method, run in the task thread
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {

            ab = params[0];
            long totalBytes = 0;

            for (int i = 0; i < params[0].size(); i++) {

                HFile f1 = new HFile(params[0].get(i));

                if (f1.isDirectory()) {

                    totalBytes = totalBytes + f1.folderSize();
                } else {

                    totalBytes = totalBytes + f1.length();
                }
            }
            HFile f = new HFile(path);
            if (f.getUsableSpace() > totalBytes) {

                for (String k1[] : f.listFiles(rootmode)) {
                    HFile k = new HFile(k1[0]);
                    for (String j : ab) {

                        if (k.getName().equals(new HFile(j).getName())) {

                            a.add(j);
                        }
                    }
                }
            } else publishProgress(utils.getString(con, R.string.in_safe));

            return a;
        }

        public void showDialog() {

            if (counter == a.size() || a.size() == 0) {

                if (ab != null && ab.size() != 0) {
                    int mode = mainActivityHelper.checkFolder(new File(path), mainActivity);
                    if (mode == 2) {
                        oparrayList = (ab);
                        operation = move ? MOVE : COPY;
                        oppathe = path;

                    } else if (mode == 1 || mode == 0) {

                        ArrayList<String> names=new ArrayList<>();
                        for(String a:ab){
                            names.add(new HFile(a).getName());
                        }
                        if (!move) {

                            Intent intent = new Intent(con, CopyService.class);
                            intent.putExtra("FILE_PATHS", ab);
                            intent.putExtra("COPY_DIRECTORY", path);
                            intent.putExtra("FILE_NAMES",names);
                            startService(intent);
                        } else {

                            new MoveFiles(utils.toFileArray(ab),names, ma, ma.getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
                        }
                    }
                } else {

                    Toast.makeText(MainActivity.this, utils.getString(con, R.string.no_file_overwrite), Toast.LENGTH_SHORT).show();
                }
            } else {

                final MaterialDialog.Builder x = new MaterialDialog.Builder(MainActivity.this);
                LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.copy_dialog, null);
                x.customView(view, true);
                // textView
                TextView textView = (TextView) view.findViewById(R.id.textView);
                textView.setText(utils.getString(con, R.string.fileexist) + "\n" + new File(a.get(counter)).getName());
                // checkBox
                final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                utils.setTint(checkBox, Color.parseColor(fabskin));
                if (theme1 == 1) x.theme(Theme.DARK);
                x.title(utils.getString(con, R.string.paste));
                x.positiveText(R.string.skip);
                x.negativeText(R.string.overwrite);
                x.neutralText(R.string.cancel);
                x.positiveColor(Color.parseColor(fabskin));
                x.negativeColor(Color.parseColor(fabskin));
                x.neutralColor(Color.parseColor(fabskin));
                x.callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {

                        if (counter < a.size()) {

                            if (!checkBox.isChecked()) {

                                ab.remove(a.get(counter));
                                counter++;

                            } else {
                                for (int j = counter; j < a.size(); j++) {

                                    ab.remove(a.get(j));
                                }
                                counter = a.size();
                            }
                            showDialog();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog materialDialog) {

                        if (counter < a.size()) {

                            if (!checkBox.isChecked()) {

                                counter++;
                            } else {

                                counter = a.size();
                            }
                            showDialog();
                        }

                    }
                });
                final MaterialDialog y = x.build();
                y.show();
                if (new File(ab.get(0)).getParent().equals(path)) {
                    View negative = y.getActionButton(DialogAction.NEGATIVE);
                    negative.setEnabled(false);
                }
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            showDialog();
        }
    }
}