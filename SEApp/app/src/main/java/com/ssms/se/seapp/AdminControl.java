package com.ssms.se.seapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Eric on 12/9/16.
 */

public class AdminControl extends AppCompatActivity{

    private RegisterTask mRegTask = null;
    private DeleteTask mDelTask = null;

    //UI references
    Button registerButton;
    Button deleteButton;
    private TextView admintextView;
    private EditText regUsernameView;
    private EditText regEmailView;
    private EditText delUsernameView;
    private View mProgressView;
    private View mAdminFormView;

    private Context mContext;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_layout);

        mContext = this;

        admintextView = (TextView) findViewById(R.id.admin_text);

        registerButton = (Button) findViewById(R.id.reg_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegisterUser();
                regUsernameView.setText("");
                regEmailView.setText("");
            }
        });
        deleteButton = (Button) findViewById(R.id.del_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDeleteUser();
                delUsernameView.setText("");
            }
        });

        regUsernameView = (EditText) findViewById(R.id.username_register);
        regEmailView = (EditText) findViewById(R.id.email_register);
        delUsernameView = (EditText) findViewById(R.id.username_delete);

        mAdminFormView = findViewById(R.id.admin_form);
        mProgressView = findViewById(R.id.admin_progress);
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

            mAdminFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAdminFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAdminFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mAdminFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private String generatePw(){

        boolean sc = false;
        boolean up = false;
        boolean lw = false;
        boolean num = false;
        boolean valid = false;
        int pwlength = (int)((25-16)*Math.random()) + 16;
        String specChars = "!@#$%^&*()_-=+[]{}|:;?.,<>";
        SecureRandom rand = new SecureRandom();
        StringBuilder password = new StringBuilder();
        while(password.length() < pwlength){
            char ch = (char) rand.nextInt(Character.MAX_VALUE);
            if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
                    (ch >= '0' && ch <= '9') || specChars.contains(String.valueOf(ch))) {
                password.append(ch);
            }
        }
        for(int x = 0; x < pwlength; x++){
            char ch = password.charAt(x);
            if(ch >= 'a' && ch <= 'z'){
                lw = true;
            }
            else if(ch >= 'A' && ch <= 'Z'){
                up = true;
            }
            else if(ch >= '0' && ch <= '9'){
                num = true;
            }
            else{
                sc = true;
            }
            if(lw && up && num && sc){
                valid = true;
                break;
            }
        }
        if(!valid) {
            generatePw();
        }

        return password.toString();

    }

    private void attemptRegisterUser() {
        if (mRegTask != null){
            return;
        }
        //reset errors
        regUsernameView.setError(null);
        regEmailView.setError(null);
        delUsernameView.setError(null);

        //store values
        String rUsername = regUsernameView.getText().toString();
        String rEmail = regEmailView.getText().toString();
        String rPw = generatePw();
        //String rPw = "password5";

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(rUsername)) {
            regUsernameView.setError(getString(R.string.error_field_required));
            focusView = regUsernameView;
            cancel = true;
        }
        if(TextUtils.isEmpty(rEmail)) {
            regEmailView.setError(getString(R.string.error_field_required));
            focusView = regEmailView;
            cancel = true;
        }
        if(!TextUtils.isEmpty(rEmail) && !isEmailValid(rEmail)) {
            regEmailView.setError(getString(R.string.error_invalid_email));
            focusView = regEmailView;
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
        } else {
            showProgress(true);
            mRegTask = new RegisterTask(rUsername, rEmail, rPw);
            mRegTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        char ch;
        boolean valid = false;
        for(int x = 0; x < email.length(); x++){
            ch = email.charAt(x);
            if(ch == '@'){
                valid = true;
                break;
            }
        }
        return valid;
    }

    private void attemptDeleteUser() {
        String dUsername = delUsernameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(dUsername)){
            delUsernameView.setError(getString(R.string.error_field_required));
            focusView = delUsernameView;
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
        } else {
            showProgress(true);
            mDelTask = new DeleteTask(dUsername);
            mDelTask.execute((Void) null);
        }
    }

    public class RegisterTask extends AsyncTask<Void, Void, Boolean>{
        String usernameString = "";
        String passwordString = "";
        String emailString = "";
        String registered = "";
        Context context;

        RegisterTask(String username, String email, String password){
            usernameString = username;
            emailString = email;
            passwordString = password;
            this.context = mContext;
        }

        @Override
        protected Boolean doInBackground(Void... params){
            try {
                URL url = null;
                try {
                    url = new URL("http://galadriel.cs.utsa.edu/~group6/register.php");
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
                userCredentials.put("email", emailString);
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
                registered = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success){

            if(registered.equals("true")){
                //Toast toast = Toast.makeText(getApplicationContext(), "New user's password is: " + passwordString, Toast.LENGTH_LONG);
                //toast.show();
                AlertDialog.Builder pwdDialog = new AlertDialog.Builder(context);
                pwdDialog.setMessage("New user's password is: " + passwordString);
                pwdDialog.setCancelable(false);
                pwdDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = pwdDialog.create();
                alert.show();
            } else if(registered.equals("false")){
                Toast toast = Toast.makeText(getApplicationContext(), "New user was not able to be registered.", Toast.LENGTH_LONG);
                toast.show();
            }
            showProgress(false);
            mRegTask = null;
        }

        @Override
        protected void onCancelled() {
            mRegTask = null;
            showProgress(false);
        }
    }

    public class DeleteTask extends AsyncTask<Void, Void, Boolean> {
        String usernameString = "";

        String isDeleted = "";

        DeleteTask(String username){
            usernameString = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = null;
                try {
                    url = new URL("http://galadriel.cs.utsa.edu/~group6/delete.php");
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
                //userCredentials.put("password", passwordString);
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
                isDeleted = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success){
            mDelTask = null;

            if(isDeleted.equals("true")){
                Toast toast = Toast.makeText(getApplicationContext(), "User Deleted", Toast.LENGTH_LONG);
                toast.show();
            }
            else if(isDeleted.equals("false")){
                Toast toast = Toast.makeText(getApplicationContext(), "Could not delete user", Toast.LENGTH_LONG);
                toast.show();
            }
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mDelTask = null;
            showProgress(false);
        }
    }


}
