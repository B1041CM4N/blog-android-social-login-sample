package auth0.com.socialloginsample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
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
import android.util.Log;
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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via multiple providers.
 */
public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    // UI references.
    private SignInButton mGoogleSignInButton;
    private LoginButton mFacebookSignInButton;
    private Button mTwitterSignInButton;
    private Button mInstagramSignInButton;

    // Vars
    private GoogleApiClient mGoogleApiClient;

    private CallbackManager mFacebookCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        mFacebookCallbackManager = CallbackManager.Factory.create();

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.google_sign_in_button:
                        signInWithGoogle();
                        break;
                    case R.id.facebook_sign_in_button:
                        signInWithFacebook();
                        break;
                    case R.id.twitter_sign_in_button:
                        break;
                    case R.id.instagram_sign_in_button:
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected button id");
                }
            }
        };

        mGoogleSignInButton = (SignInButton)findViewById(R.id.google_sign_in_button);
        mGoogleSignInButton.setOnClickListener(listener);

        mFacebookSignInButton = (LoginButton)findViewById(R.id.facebook_sign_in_button);
        mFacebookSignInButton.registerCallback(mFacebookCallbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(final LoginResult loginResult) {
                    handleSignInResult(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            LoginManager.getInstance().logOut();
                            return null;
                        }
                    });
                }

                @Override
                public void onCancel() {
                    handleSignInResult(null);
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(LoginActivity.class.getCanonicalName(), error.getMessage());
                    handleSignInResult(null);
                }
            }
        );

        mTwitterSignInButton = (Button)findViewById(R.id.twitter_sign_in_button);
        mTwitterSignInButton.setOnClickListener(listener);

        mInstagramSignInButton = (Button)findViewById(R.id.instagram_sign_in_button);
        mInstagramSignInButton.setOnClickListener(listener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if(result.isSuccess()) {
                final GoogleApiClient client = mGoogleApiClient;

                handleSignInResult(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        if (client != null) {

                            Auth.GoogleSignInApi.signOut(client).setResultCallback(
                                new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        Log.d(LoginActivity.class.getCanonicalName(),
                                                status.getStatusMessage());

                                        /* TODO: handle logout failures */
                                    }
                                }
                            );

                        }

                        return null;
                    }
                });

            } else {
                handleSignInResult(null);
            }
        } else {
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void signInWithGoogle() {
        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInWithFacebook() {
    }

    private void handleSignInResult(Callable<Void> logout) {
        if(logout == null) {
            /* Login error */
            Toast.makeText(getApplicationContext(), R.string.login_error, Toast.LENGTH_SHORT).show();
        } else {
            /* Login success */
            Application.getInstance().setLogoutCallable(logout);
            startActivity(new Intent(this, LoggedInActivity.class));
        }
    }
}
