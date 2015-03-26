package com.gbhrdt.whgonline;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.app.*;
import android.content.*;

import com.loopj.android.http.*;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";

    // Values for email and password at the time of the login attempt.
    private String mUsername;
    private String mPassword;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mUsernameView.setText(mUsername);

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

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        final Context context = this;

        findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                //alert.setTitle("Title here");

                MyWebView webView = new MyWebView(context);
                webView.loadUrl("http://vplan.whgonline.de/index.php?p=register");
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);

                        return true;
                    }
                });

                alert.setView(webView);
                alert.setNegativeButton(Html.fromHtml("Schlie&szlig;en"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alert.show();

            }
        });

        // check group
        VplanApiClient.get("api.php?do=getStatus", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                VplanApiClient.setGroup(response);

                if(!response.equals("GUEST") && !response.isEmpty()) {
                    startActivity(new Intent(getApplicationContext(), TabsFragmentActivity.class));
                    finish();
                }
            }
        });

        if(getPreferences("username") != null && getPreferences("username").length() > 0) {
            mUsernameView.setText(getPreferences("username"));
            if(getPreferences("password") != null && getPreferences("password").length() > 0) {
                mPasswordView.setText(getPreferences("password"));
                attemptLogin();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    public void showAlert(String message) {
        AlertDialog ad = new AlertDialog.Builder(this).create();
        ad.setCancelable(false); // This blocks the 'BACK' button
        ad.setMessage(message);
        ad.setButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    public void savePreferences(String key, String value) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName(), 0);
        prefs.edit().putString(key, value).commit();
    }

    public String getPreferences(String key) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName(), 0);
        return prefs.getString(key, "");
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (mUsername.length() < 4) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
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
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);

            VplanApiClient.get("api.php?do=getStatus", null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    // check if already logged in
                    if(response.equals("GUEST") || !response.isEmpty()) {
                        // get token
                        VplanApiClient.get("api.php?do=getToken", null, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(String response) {
                                String token = response;
                                Log.d(TAG, "TOKEN: " + token);

                                RequestParams postParams = new RequestParams();
                                postParams.put("username", mUsername);
                                postParams.put("password", mPassword);
                                postParams.put("token", token);
                                postParams.put("login", "Einloggen");

                                // post login
                                VplanApiClient.post("api.php", postParams, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(String response) {
                                        showProgress(false);

                                        System.out.println("RESPONSE: " + response);
                                        if(response.startsWith("Login erfolgreich")) {
                                            // check group
                                            VplanApiClient.get("api.php?do=getStatus", null, new AsyncHttpResponseHandler() {
                                                @Override
                                                public void onSuccess(String response) {
                                                    VplanApiClient.setGroup(response);

                                                    if(!response.equals("GUEST") && !response.isEmpty()) {
                                                        savePreferences("username", mUsername);
                                                        savePreferences("password", mPassword);

                                                        startActivity(new Intent(getApplicationContext(), TabsFragmentActivity.class));
                                                        finish();
                                                    }
                                                }
                                            });
                                        } else {
                                            if(response.contains("Benutzername")) {
                                                mUsernameView.setError(response);
                                                mUsernameView.requestFocus();
                                            } else if(response.contains("Passwort")) {
                                                mPasswordView.setError(response);
                                                mPasswordView.requestFocus();
                                            } else {
                                                showAlert(response);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        VplanApiClient.setGroup(response);

                        startActivity(new Intent(getApplicationContext(), TabsFragmentActivity.class));
                        finish();
                    }
                }
            });



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

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
