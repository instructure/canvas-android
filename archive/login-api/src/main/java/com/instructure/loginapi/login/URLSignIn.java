/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.loginapi.login;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.instructure.canvasapi.api.AccountDomainAPI;
import com.instructure.canvasapi.model.AccountDomain;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.adapter.AccountAdapter;
import com.instructure.loginapi.login.adapter.PreviouslySignedInUserAdapter;
import com.instructure.loginapi.login.api.CanvasAPI;
import com.instructure.loginapi.login.model.Account;
import com.instructure.loginapi.login.model.Locations;
import com.instructure.loginapi.login.model.SignedInUser;
import com.instructure.loginapi.login.util.Const;
import com.instructure.loginapi.login.util.SoftKeyboardUtil;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import retrofit.client.Response;

public abstract class URLSignIn extends FragmentActivity implements
        URLSigninInterface,
        PreviouslySignedInUserAdapter.SignedInUserCallback,
        SoftKeyboardUtil.OnSoftKeyBoardHideListener,
        APIStatusDelegate {

    public static String loggedInIntent = "loggedIn";
    private static final String POSITION = "position";

    private String messageToUser;

    private GestureDetector gesture;

	private long first = 0;
	private long second = 0;
	private boolean first_free = true;
    private int canvas_login = 0;
    private Uri passedURI = null;

    private AutoCompleteTextView urlEnter;
    public final static String URL_ENTRIES = "url entries";

    private PreviouslySignedInUserAdapter previouslySignedInUserAdapter;
    private AccountAdapter accountAdapter;
    private ListView listView;

    private static final int MESSAGE_TYPE_FAILURE = 3000;
    private static final int MESSAGE_TYPE_WARNING = 2000;
    private static final int MESSAGE_TYPE_SUCCESS = 1000;

    private ArrayList<AccountDomain> accounts = new ArrayList<>();

    private View mHeaderView;
    private View mFooterView;
    private View mRightHelpIcon;
    private View mCanvasLogo;
    private View mTopDivider;
    private ImageView mConnect;

    private CanvasCallback<AccountDomain[]> accountDomainCanvasCallback;
    private Location currentLocation;

    private static boolean mShowWhenDownloaded = true;

    ///////////////////////////////////////////////////////////////////////////
    // Abstract Methods
    ///////////////////////////////////////////////////////////////////////////
    public abstract ErrorDelegate getErrorDelegate();

    public abstract String getPrefsFileName();

    public abstract String getPrefsPreviousDomainKey();

    public abstract String getPrefsOtherSignedInUsersKey();

    public abstract String getPrefsMultiUserKey();

    /**
     * Function : startNextActivity
     * After a successfull login, this method will be called. If a passedURI is available, this method will be called along with it.
     * *
     */
    public abstract void startNextActivity();

    public abstract void startNextActivity(Uri passedURI);

    /**
     * Function    : initializeLoggingForUser
     * Description : If the login is successful, this will return the signed in user object and a boolean
     * indicating whether or not the domain is from {anonymousDomain -- see Private-Data}. If so, initlizing helpshift and crashlytics
     * without adding user info for deviceIdentifier, email, and username.
     * *
     */
    public abstract void initializeLoggingForUser(boolean isAnonymousDomain, User signedInUser);

    /**
     * Function    : startCrashlytics & startHelpShift
     * Description : start crashlytics on app by overriding this method and calling : Crashlytics.start(this)
     * *
     */
    public abstract void startCrashlytics();

    public abstract void startHelpShift();

    public abstract void startGoogleAnalytics();

    /**
     * Function    : showHelpShiftSupport
     * Description : While on the login page, this method gets called when the user selects the helpLink,
     * Override this method and call new HelpShift().showSupport()
     * *
     */
    public abstract void showHelpShiftSupport();

    /**
     * Function    : trackAppFlow
     * Description : Google Analytics, track the flow of the app by passing in an activity.
     */
    public abstract void trackAppFlow(Activity activity);

    /**
     * Function    : displayMessage
     * Description : Display a message to the user.
     */
    public abstract void displayMessage(String message, int messageType);

    ///////////////////////////////////////////////////////////////////////////
    // Optional Overrides
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Function    : handleNightlyBuilds
     * Description : If this app is a nightly build, we want to get the newer build.
     * *
     */
    @Override
    public void handleNightlyBuilds() {
    }

    /**
     * Function    : refreshWidgets
     * Description : Notify widgets that login was successful
     * *
     */
    @Override
    public void refreshWidgets() {
    }

    /**
     * Function    : deleteCachedFiles
     * Description : Delete all files in the external cache directory that are 14 or more days old.
     */
    @Override
    public void deleteCachedFiles() {
    }

    @Override
    public String getUserAgent() {
        return "candroid";
    }

    @Override
    public int getRootLayout() {
        return R.layout.url_sign_in;
    }

    /**
     * Function    : shouldShowHelpButton
     * Description : Some canvas apps do not include the helpshift button, allow apps to disable it.
     */
    @Override
    public boolean shouldShowHelpButton() {
        return true;
    }


    /**
     * This is the setup process for the list adapters. If the device has previously signed in users then we show
     * the previously signed in user adapter.
     *
     * We automatically, and always download the domain file. A boolean mShowWhenDownloaded will keep track of if the
     * domains should be shown after a download has completed.
     *
     * It's complicated :)
     */
    public void beginAdapterSetupProcess() {
        previouslySignedInUserAdapter = new PreviouslySignedInUserAdapter(this, this, OAuthWebLogin.getPreviouslySignedInUsers(URLSignIn.this));
        if (previouslySignedInUserAdapter.getCount() > 0) {
            setupPreviouslySignedInUsers();
            mShowWhenDownloaded = false;
        } else {
            //Download the accounts file
            mShowWhenDownloaded = true;
        }
    }


    public void onAccountsRetrieved(ArrayList<AccountDomain> accounts) {
        this.accounts = accounts;
        accountAdapter = new AccountAdapter(this, accounts, Account.scrubList(accounts));
        if (mShowWhenDownloaded) {
            Utils.d("setting account adapter");
            setupAdapterWithHeaders(accountAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (view.getId() == R.id.canvasNetworkHeader) {
                        urlEnter.setText(Const.URL_CANVAS_NETWORK);
                        connectToURL();
                    } else if (view.getId() == R.id.canvasHelpFooter) {
                        showHelpShiftSupport();
                    } else {

                        //Make sure the headers are not counting as items for our item clicks
                        AccountDomain account = (AccountDomain) accountAdapter.getItem(Math.abs(position - listView.getHeaderViewsCount()));
                        if (account != null) {
                            urlEnter.setText(account.getDomain());
                            connectToURL();
                        } else {
                            urlEnter.setText(Const.URL_CANVAS_NETWORK);
                            connectToURL();
                        }
                    }
                }
            });

            final int textLength = urlEnter.getText().toString().length();
            //Fixes a requirement to have the header hidden and shown at funky times
            if (urlEnter.hasFocus() && textLength == 0) {
                removeHeaderViews();
                listView.addHeaderView(getCanvasNetworkView());
            } else if (textLength > 0) {
                //Fixes a rotation issue with no filtered results
                urlEnter.setText(urlEnter.getText());
                urlEnter.setSelection(textLength);
            }


            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setVisibleListItem();
                }
            }, 300);

            if (accountAdapter != null) {
                accountAdapter.notifyDataSetChanged();
            }
        }
    }

    private void setupAdapterWithHeaders(BaseAdapter adapter) {
        //Note: headers and footers need to be added before calling setAdapter.
        removeHeaderViews();
        removeFooterViews();
        listView.addHeaderView(getCanvasNetworkView());
        listView.addFooterView(getCanvasHelpView());
        listView.setAdapter(adapter);
        removeHeaderViews();
        removeFooterViews();
    }

    private void removeHeaderViews() {
        listView.removeHeaderView(getCanvasNetworkView());
        for (int i = 0; i < listView.getHeaderViewsCount(); i++) {
            listView.removeHeaderView(getCanvasNetworkView());
        }
    }

    private void removeFooterViews() {
        listView.removeFooterView(getCanvasHelpView());
        for (int i = 0; i < listView.getFooterViewsCount(); i++) {
            listView.removeFooterView(getCanvasHelpView());
        }
    }

    private void setupPreviouslySignedInUsers() {
        Utils.d("setting user adapter");
        setupAdapterWithHeaders(previouslySignedInUserAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if (view.getId() == R.id.canvasNetworkHeader) {
                    urlEnter.setText(Const.URL_CANVAS_NETWORK);
                    connectToURL();
                } else if (view.getId() == R.id.canvasHelpFooter) {
                    showHelpShiftSupport();
                } else {
                    SignedInUser signedInUser = (SignedInUser) previouslySignedInUserAdapter.getItem(Math.abs(position - listView.getHeaderViewsCount()));

                    APIHelpers.setProtocol(signedInUser.protocol, URLSignIn.this);
                    APIHelpers.setCacheUser(URLSignIn.this, signedInUser.user);
                    CanvasRestAdapter.setupInstance(URLSignIn.this, signedInUser.token, signedInUser.domain);

                    //Set previously signed in domain.
                    OAuthWebLogin.setLastSignedInDomain(signedInUser.domain, URLSignIn.this);
                    previouslySignedInUserAdapter.setSelectedUserGlobalId(OAuthWebLogin.getGlobalUserId(signedInUser.domain, signedInUser.user), signedInUser, getContext());

                    checkSignedIn(false);
                }
            }
        });
    }

    public View getCanvasNetworkView() {
        if (mHeaderView == null) {
            View header = getLayoutInflater().inflate(R.layout.accounts_adapter_item, null);
            header.setId(R.id.canvasNetworkHeader);
            ((TextView) header.findViewById(R.id.name)).setText(R.string.loginCanvasNetwork);
            ((TextView) header.findViewById(R.id.distance)).setText(R.string.loginRightBehindYou);
            (header.findViewById(R.id.image)).setVisibility(View.VISIBLE);
            mHeaderView = header;
        }
        return mHeaderView;
    }

    public View getCanvasHelpView() {
        if (mFooterView == null) {
            View header = getLayoutInflater().inflate(R.layout.accounts_adapter_item_help, null);
            header.setId(R.id.canvasHelpFooter);
            mFooterView = header;
        }
        return mFooterView;
    }


    //get a reference to the help icon that is on the page. This shouldn't be null, but the view might be GONE
    public View getCanvasHelpIconView() {
        return mRightHelpIcon;
    }

    @Override
    public void onSoftKeyBoardVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            mCanvasLogo.setVisibility(View.GONE);
            mRightHelpIcon.setVisibility(View.GONE);
        } else {
            mCanvasLogo.setVisibility(View.VISIBLE);
            String urlText = urlEnter.getText().toString();
            if (TextUtils.isEmpty(urlText) && shouldShowHelpButton()) {
                mRightHelpIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupUrlEnter() {
        //Set up the URLEnter object.
        urlEnter = (AutoCompleteTextView) findViewById(R.id.enterURL);
        urlEnter.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                connectToURL();
                return true;
            }
        });

        //pull up results after 1 letter
        urlEnter.setThreshold(1);
        urlEnter.addTextChangedListener(mFinderTextWatcher);
        urlEnter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    removeHeaderViews();
                    mTopDivider.setVisibility(View.VISIBLE);
                    listView.addHeaderView(getCanvasNetworkView());
                    mShowWhenDownloaded = true;
                    onAccountsRetrieved(accounts);
                }
            }
        });
    }

    @Override
    public void onUserDelete() {
        beginAdapterSetupProcess();
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getRootLayout());
        mTopDivider = findViewById(R.id.topDivider);
        listView = (ListView) findViewById(R.id.listview);
        mConnect = (ImageView)findViewById(R.id.connect);
        mRightHelpIcon = findViewById(R.id.help_button);
        mCanvasLogo = findViewById(R.id.canvas_logo);
        SoftKeyboardUtil.observeSoftKeyBoard(this, this);

        //Log to GA.
        trackAppFlow(URLSignIn.this);
        setupUrlEnter();

        //Handle Intent. This will set the URLEnter if it was passed in. So it has to be AFTER it's initialized and set.
        handleIntent();

        //Handle the Crouton if it exists.
        if (messageToUser != null && messageToUser.length() > 1) {
            displayMessage(messageToUser, MESSAGE_TYPE_WARNING);
        }

        if(mConnect != null) {
            mConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectToURL();
                }
            });
        }

        if(getErrorDelegate() != null) {
            // Set Retrofit Error Handling Delegate
            APIHelpers.setDefaultErrorDelegateClass(URLSignIn.this, getErrorDelegate().getClass().getName());
        }

        //Start crashlytics
        startCrashlytics();

        //Set our user agent for CanvasKit.
        APIHelpers.setUserAgent(URLSignIn.this, CanvasAPI.getCandroidUserAgent(getUserAgent(), URLSignIn.this));

        //Start helpShift
        startHelpShift();

        //Delete old externally cached files
        deleteCachedFiles();

        //Help Layout
        findViewById(R.id.help_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpShiftSupport();
            }
        });

        //Set up gesture
        gesture = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            public boolean onDown(MotionEvent event) {
                return true;
            }
        });

        //Handle canvas_login stuff.
        findViewById(R.id.rootView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });

        setupCallbacks();

        //See if the user is already signed in.
        checkSignedIn(true);

        beginAdapterSetupProcess();

        handleNightlyBuilds();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION, listView.getFirstVisiblePosition());
    }

    private void setVisibleListItem() {
        listView.clearFocus();
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelectionAfterHeaderView();
            }
        });
    }

    @Override
    protected void onResume() {
        //init recent entries
        previouslySignedInUserAdapter.clearSelectedGlobalId();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check if they've signed in since this page was shown.
        if (requestCode == OAuthWebLogin.SIGNED_IN && data != null && data.getBooleanExtra(loggedInIntent, false)) {
            if (data.hasExtra(Const.PASSED_URI)) {
                passedURI = (Uri) data.getParcelableExtra(Const.PASSED_URI);
            }
            checkSignedIn(true);
        }

        //We want no ProgressDialog to be visible.
        previouslySignedInUserAdapter.clearSelectedGlobalId();
    }


    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////
    public void handleIntent() {
        if (getIntent().hasExtra(Const.HOST)) {
            urlEnter.setText(getIntent().getStringExtra(Const.HOST));
            if (getIntent().hasExtra(Const.URI)) {
                passedURI = (Uri) getIntent().getParcelableExtra(Const.URI);
                connectToURL();
            }
        }
        if (getIntent().getBooleanExtra(Const.SHOW_MESSAGE, false)) {
            messageToUser = getIntent().getStringExtra(Const.MESSAGE_TO_USER);
        }
    }

    public static Intent createIntent(Context context, boolean showMessage, String message) {
        Intent intent = new Intent(context, URLSignIn.class);
        intent.putExtra(Const.SHOW_MESSAGE, showMessage);
        intent.putExtra(Const.MESSAGE_TO_USER, message);
        return intent;
    }

    public static Intent createIntent(Context context, String host) {
        Intent intent = new Intent(context, URLSignIn.class);
        intent.putExtra(Const.HOST, host);
        return intent;
    }

    public static Intent createIntent(Context context, String host, Uri uri, Class myClass) {
        Intent intent = new Intent(context, myClass);
        intent.putExtra(Const.URI, uri);
        intent.putExtra(Const.HOST, host);
        return intent;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Function   : clearUserContextIfNecessary
     * Description: for some domains, we need to clear out student data.
     * *
     */
    private User clearUserContextIfNecessary(String domain, User signedInUser) {
        if (domain.endsWith(BuildConfig.ANONYMOUS_SCHOOL_DOMAIN)) {

        }
        return signedInUser;
    }

    /**
     * This function checks whether or not the current user is signed in.
     * <p/>
     * If they are, it sets up all of our analytics, helpshift, crashlytics, etc.
     */
    void checkSignedIn(boolean runOnUIThread) {
        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Now get it from the new place. This will be the true token whether they signed into dev/retrofit or the old way.
                String token = APIHelpers.getToken(URLSignIn.this);

                if (token != null && !token.equals("")) {
                    //Get the signed in user.
                    User signedInUser = APIHelpers.getCacheUser(URLSignIn.this);

                    boolean anonymousSchoolDomain = APIHelpers.getDomain(URLSignIn.this).endsWith(BuildConfig.ANONYMOUS_SCHOOL_DOMAIN);
                    if (!anonymousSchoolDomain) {
                        startGoogleAnalytics();
                    }

                    //make sure we have a valid user
                    if (signedInUser != null) {
                        initializeLoggingForUser(anonymousSchoolDomain, signedInUser);
                    }

                    if (passedURI != null) {
                        startNextActivity(passedURI);
                    } else {
                        startNextActivity();
                    }

                    finish();
                } else {
                    requestLocation();
                }
                refreshWidgets();
            }
        });

        if (runOnUIThread) {
            backgroundThread.run();
        } else {
            backgroundThread.start();
        }
    }

    @TargetApi(23)
    private void requestLocation() {
        AccountDomainAPI.getAllAccountDomains(accountDomainCanvasCallback);
        //To be added back when/if we get location information from web... in 3.2 years.
//        if(PermissionUtils.hasPermissions(URLSignIn.this, PermissionUtils.LOCATION_PERMISSION)) {
//            currentLocation = Locations.getCurrentLocation(URLSignIn.this);
//            AccountDomainAPI.getAllAccountDomains(accountDomainCanvasCallback);
//        } else {
//            requestPermissions(PermissionUtils.makeArray(PermissionUtils.LOCATION_PERMISSION), PermissionUtils.PERMISSION_REQUEST_CODE);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if(PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                currentLocation = Locations.getCurrentLocation(URLSignIn.this);
                AccountDomainAPI.getAllAccountDomains(accountDomainCanvasCallback);
            } else {
                AccountDomainAPI.getAllAccountDomains(accountDomainCanvasCallback);
            }
        }
    }

    /**
     * Function    : connectToURL
     * Description : When the user enters a canvas url, gets the url and then calls OAuthWebLogin with
     * the passed in URI if available. Since we're starting a library's activity, the project
     * using this library will need to add the activity to it's activitymanifest.xml, with this
     * project's full package name: name="com.instructure.loginapi.login.OAuthWebLogin"
     * *
     */
    void connectToURL() {
        String url = urlEnter.getText().toString().toLowerCase().replace(" ", "");

        //if the user enters nothing, try to connect to canvas.instructure.com
        if (url.trim().length() == 0) {
            url = "canvas.instructure.com";
        }
        //if there are no periods, append .instructure.com
        if (!url.contains(".")) {
            url += ".instructure.com";
        }

        //URIs need to to start with a scheme.
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        //Get just the host.
        Uri uri = Uri.parse(url);
        url = uri.getHost();

        //Strip off www. if they typed it.
        if (url.startsWith("www.")) {
            url = url.substring(4);
        }

        if (passedURI == null) {
            startActivityForResult(OAuthWebLogin.createIntent(
                    this, url, canvas_login, getPrefsFileName(),
                    getPrefsPreviousDomainKey(), getPrefsOtherSignedInUsersKey(),
                    getPrefsMultiUserKey()), OAuthWebLogin.SIGNED_IN);
        } else {
            startActivityForResult(OAuthWebLogin.createIntent(
                    this, url, canvas_login, getPrefsFileName(),
                    getPrefsPreviousDomainKey(), getPrefsOtherSignedInUsersKey(),
                    getPrefsMultiUserKey(), passedURI), OAuthWebLogin.SIGNED_IN);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////

    private void setupCallbacks() {
        accountDomainCanvasCallback = new CanvasCallback<AccountDomain[]>(this) {
            @Override
            public void cache(AccountDomain[] accountDomains) {

            }

            @Override
            public void firstPage(AccountDomain[] accountDomains, LinkHeaders linkHeaders, Response response) {
                ArrayList<AccountDomain> domains = new ArrayList<>(Arrays.asList(accountDomains));

                boolean isDebuggable = 0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);

                if(isDebuggable) {
                    //put these domains first
                    domains.add(0, createAccountForDebugging("mobiledev.instructure.com"));
                    domains.add(1, createAccountForDebugging("mobiledev.beta.instructure.com"));
                    domains.add(2, createAccountForDebugging("mobileqa.instructure.com"));
                    domains.add(3, createAccountForDebugging("mobileqat.instructure.com"));
                    domains.add(4, createAccountForDebugging("ben-k.instructure.com"));
                    domains.add(5, createAccountForDebugging("clare.instructure.com"));
                }

                //we have a location, sort the results
                if(currentLocation != null) {
                    //Order the locations so the closest is the first
                    Collections.sort(domains, new Comparator<AccountDomain>() {
                        @Override
                        public int compare(AccountDomain lhs, AccountDomain rhs) {
                            if (lhs.getDistance() == null && rhs.getDistance() == null) {
                                return 0;
                            } else if (lhs.getDistance() == null && rhs.getDistance() != null) {
                                return 1;
                            } else if (lhs.getDistance() != null && rhs.getDistance() == null) {
                                return -1;
                            } else {
                                return lhs.getDistance().compareTo(rhs.getDistance());
                            }
                        }
                    });
                }
                onAccountsRetrieved(domains);
            }
        };
    }

    private AccountDomain createAccountForDebugging(String domain) {
        AccountDomain account = new AccountDomain();
        account.setDomain(domain);
        account.setName("-- " + domain);
        account.setDistance(null);
        return account;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            //capture the event when the user lifts their fingers, not on the down press
            //to make sure they're not long pressing
            if (action == MotionEvent.ACTION_POINTER_UP) {
                //timer to get difference between clicks
                Calendar now = Calendar.getInstance();

                //detect number of fingers, change to 1 for a single-finger double-click, 3 for a triple-finger double-click!
                if (event.getPointerCount() == 2) {
                    first_free = !first_free;

                    if (first_free) {
                        //if this is the first click, then there hasn't been a second
                        //click yet, also record the time
                        first = now.getTimeInMillis();
                    } else {
                        //if this is the second click, record its time
                        second = now.getTimeInMillis();
                    }

                    //if the difference between the 2 clicks is less than 500 ms (1/2 second)
                    //Math.abs() is used because you need to be able to detect any sequence of clicks, rather than just in pairs of two
                    //(e.g. click1 could be registered as a second click if the difference between click1 and click2 > 500 but
                    //click2 and the next click1 is < 500)

                    if (Math.abs(second - first) < 500) {
                        Resources r = getResources();

                        canvas_login++;

                        //cycle between 0, 1, and 2
                        /**
                         * 0 == no special login
                         * 1 == canvas login
                         * 2 == site admin
                         */
                        if(canvas_login > 2) {
                            canvas_login = 0;
                        }
                        if(canvas_login == 0) {
                            displayMessage(getString(R.string.canvasLoginOff), MESSAGE_TYPE_SUCCESS);
                        } else if(canvas_login == 1){
                            displayMessage(getString(R.string.canvasLoginOn), MESSAGE_TYPE_SUCCESS);
                        } else {
                            displayMessage(getString(R.string.siteAdminLogin), MESSAGE_TYPE_SUCCESS);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return true;
    }

    private TextWatcher mFinderTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (accountAdapter != null) {
                accountAdapter.getFilter().filter(s);
            }

            if (TextUtils.isEmpty(s)) {
                onAccountsRetrieved(accounts);

                removeFooterViews();
                removeHeaderViews();
                listView.addHeaderView(getCanvasNetworkView());
                if (shouldShowHelpButton()) {
                    mRightHelpIcon.setVisibility(View.VISIBLE);
                }

                if(mConnect != null) {
                    mConnect.setVisibility(View.GONE);
                }
            } else {
                final Locale locale = Locale.getDefault();
                if (!Const.URL_CANVAS_NETWORK.toLowerCase(locale).contains(s.toString().toLowerCase(locale))) {
                    removeHeaderViews();
                }
                removeFooterViews();
                if (shouldShowHelpButton()) {
                    listView.addFooterView(getCanvasHelpView());
                }
                mRightHelpIcon.setVisibility(View.GONE);

                if(mConnect != null) {
                    mConnect.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            setVisibleListItem();
        }
    };

    @Override
    public void onCallbackStarted() {

    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {

    }

    @Override
    public void onNoNetwork() {

    }

    @Override
    public Context getContext() {
        return this;
    }
}