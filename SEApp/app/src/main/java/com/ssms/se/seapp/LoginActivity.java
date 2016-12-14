package com.ssms.se.seapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    public final static String EXTRA_MESSAGE = "com.ssms.se.seapp";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private PasswordResetTask mResetTask = null;
    private int attempts = 0;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private int loginAttempts = 0;
    private long lockoutTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mForgotPasswordButton = (Button) findViewById(R.id.forgot_password);
        mForgotPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void forgotPassword()
    {
        String username = mUsernameView.getText().toString();

        mUsernameView.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mResetTask = new PasswordResetTask(username);
            mResetTask.execute((Void) null);
        }
    }
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
/*        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mUsernameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } */
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        loginAttempts++;
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(loginAttempts>3){
            if(lockoutTime==0){
                lockoutTime=System.currentTimeMillis();
            }
            if(System.currentTimeMillis()>lockoutTime+60000){
                lockoutTime=0;
                loginAttempts=0;
            }
            mPasswordView.setError(getString(R.string.error_attempt_limit));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if(TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        boolean length = false;
        boolean lc = false;
        boolean uc = false;
        boolean num = false;
        boolean sc = false;
        char ch;
        if(password.equals("admin"))
            return true;

        if(password.length() > 15){
            length = true;
        }
        for(int x = 0; x < password.length(); x++){
            ch = password.charAt(x);
            if(Character.isLowerCase(ch)){
                lc = true;
            }
            else if(Character.isUpperCase(ch)){
                uc = true;
            }
            else if(Character.isDigit(ch)){
                num = true;
            }
            else if(ch == ' ' || ch == '\t'){
                continue;
            }
            else {
                sc = true;
            }
            if(lc && uc && num && sc){
                break;
            }
        }
        if(length && lc && uc && num && sc){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUsernameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        String returnString = "";

        String usernameString = "";
        // TextView temp2 = (TextView) findViewById(R.id.password);
        String passwordString = "";

        UserLoginTask(String username, String password) {
            usernameString = username;
            passwordString = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            if(loginAttempts>=3) {
                if (System.currentTimeMillis() > lockoutTime + 60000) {
                    System.out.println("UNLOCKED");
                    loginAttempts = 0;
                    lockoutTime = 0;
                }
            }

            try {
                URL url = null;
                try {
                    url = new URL("http://galadriel.cs.utsa.edu/~group6/users.php");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection client = null;
                try {
                    client = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JSONObject userCredentials = new JSONObject();

                userCredentials.put("username", usernameString);
                userCredentials.put("password", passwordString);
                System.out.println(userCredentials.toString());
                String postArray = "user_credentials=" + userCredentials.toString();
                try {
                    client.setRequestMethod("POST");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    client.setRequestProperty("charset", "utf-8");
                    client.setRequestProperty("Content-Length", Integer.toString(postArray.length()));
                    client.setUseCaches(false);
                    client.setDoOutput(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                OutputStreamWriter out = null;
                try {
                    out = new OutputStreamWriter(client.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    out.write(postArray);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line);
                }
                returnString = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }


            return true;
        }

            @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(EXTRA_MESSAGE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("USERNAME", usernameString);
                editor.putString("ACCOUNT_TYPE", returnString);
                editor.commit();

                if (returnString.equals("user")) {
                    Intent intent = new Intent(getApplicationContext(), MessageList.class);
                    startActivity(intent);
                    finish();
                } else if(returnString.equals("false")){
                    loginAttempts++;
                    if(loginAttempts>=3){
                        System.out.println("LOCKED");
                        lockoutTime = System.currentTimeMillis();
                    }
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                } else if(returnString.equals("admin")) {
                    Intent intent = new Intent(getApplicationContext(), MessageList.class);
                    startActivity(intent);
                    finish();
                } else {
                    System.out.println("Hit else in onPostExecute");
                    System.out.println("Response: "+returnString);

                }

        }



        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class PasswordResetTask extends AsyncTask<Void, Void, Boolean> {

        String mUsername = "";
        String returnString = "";
        PasswordResetTask(String username) {
            mUsername = username;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = null;
                try {
                    url = new URL("http://galadriel.cs.utsa.edu/~group6/password_reset.php");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection client = null;
                try {
                    client = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JSONObject userCredentials = new JSONObject();
                userCredentials.put("username", mUsername);
                System.out.println(userCredentials.toString());
                String postArray = "user_credentials=" + userCredentials.toString();
                try {
                    client.setRequestMethod("POST");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    client.setRequestProperty("charset", "utf-8");
                    client.setRequestProperty("Content-Length", Integer.toString(postArray.length()));
                    client.setUseCaches(false);
                    client.setDoOutput(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                OutputStreamWriter out = null;
                try {
                    out = new OutputStreamWriter(client.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    out.write(postArray);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line);
                }
                returnString = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mResetTask = null;
            showProgress(false);
            if(returnString.equals("true")){
                Toast toast = Toast.makeText(getApplicationContext(), "System admin notified of password reset request.", Toast.LENGTH_LONG);
                toast.show();
            }
            else if(returnString.equals("false")){
                Toast toast = Toast.makeText(getApplicationContext(), "Your request was not able to be processed.", Toast.LENGTH_LONG);
                toast.show();
            }
            else if(returnString.equals("user not found")){
                Toast toast = Toast.makeText(getApplicationContext(), "User not found.", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}

