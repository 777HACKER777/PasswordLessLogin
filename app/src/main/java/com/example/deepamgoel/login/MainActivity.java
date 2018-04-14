package com.example.deepamgoel.login;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class MainActivity extends AppCompatActivity {

    public static int APP_REQUEST_CODE = 1;
    LoginButton facebookLoginButton;
    CallbackManager callbackManager;

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.background_video);
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.night_traffic_time_lapse);
        videoView.setVideoURI(uri);
        videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        FontHelper.setCustomTypeface(findViewById(R.id.view_root));

        facebookLoginButton = findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions("email");

        callbackManager = CallbackManager.Factory.create();
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                launchAccountActivity();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                String toastMessage = error.getMessage();
                Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
            }
        });

        AccessToken passwordLessAccessToken = AccountKit.getCurrentAccessToken();
        com.facebook.AccessToken facebookLoginToken = com.facebook.AccessToken.getCurrentAccessToken();
        if (passwordLessAccessToken != null || facebookLoginToken != null) {
            launchAccountActivity();
        }
    }

    public void onResume() {
        super.onResume();
        videoView.start();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);


        if (requestCode == APP_REQUEST_CODE) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (loginResult.getError() != null) {

                String toastMessage = loginResult.getError().getErrorType().getMessage();
                Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
            } else if (loginResult.getAccessToken() != null) {

                launchAccountActivity();
            }
        }
    }

    private void onLogin(final LoginType loginType) {

        final Intent intent = new Intent(this, AccountKitActivity.class);


        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        loginType,
                        AccountKitActivity.ResponseType.TOKEN
                );
        final AccountKitConfiguration configuration = configurationBuilder.build();

        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configuration);
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    public void onPhoneLogin(View view) {
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        logger.logEvent("onPhoneLogin");

        onLogin(LoginType.PHONE);
    }

    public void onEmailLogin(View view) {
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        logger.logEvent("onEmailLogin");

        onLogin(LoginType.EMAIL);
    }

    private void launchAccountActivity() {
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
        finish();
    }

}
