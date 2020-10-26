package com.shukerullah.piano;

import android.os.Build;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.FacebookSdk;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import org.jetbrains.annotations.NotNull;

import io.piano.android.id.PianoId;
import io.piano.android.id.PianoIdCallback;
import io.piano.android.id.PianoIdClient;
import io.piano.android.id.PianoIdException;
import io.piano.android.id.facebook.FacebookOAuthProvider;
import io.piano.android.id.google.GoogleOAuthProvider;
import io.piano.android.id.models.PianoIdToken;

public class PianoSdkModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    public final String REACT_CLASS = "PianoSdk";

    private final int PIANO_ID_REQUEST_CODE = 786;

    private final ReactApplicationContext reactContext;

    protected Callback callback;

    private ResponseHelper responseHelper = new ResponseHelper();

    public PianoSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    void init(String pianoAID, String pianoEndpoint, @Nullable String facebookAppId) {
        PianoIdClient pianoIdClient = PianoId.init(pianoEndpoint, pianoAID)
                .with(new PianoIdCallback<PianoIdToken>() {
                    @Override
                    public void onSuccess(PianoIdToken data) {
                        // TODO: Add Success Callback
                    }
                    @Override
                    public void onFailure(PianoIdException exception) {
                        // TODO: Add Failure Callback
                        // showError(exception);
                    }
                })
                .with(new GoogleOAuthProvider());
        if(facebookAppId != null) {
            FacebookSdk.setApplicationId(facebookAppId);
            FacebookSdk.sdkInitialize(reactContext);
            pianoIdClient.with(new FacebookOAuthProvider());
        }
        reactContext.addActivityEventListener(this);
    }

    @ReactMethod
    public void signIn(final Callback callback) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            responseHelper.invokeError(callback, "Can't find current Activity");
            return;
        }

        this.callback = callback;

        Intent intent = PianoId.signIn().widget(PianoId.WIDGET_LOGIN).getIntent(reactContext);

        try {
            currentActivity.startActivityForResult(intent, PIANO_ID_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            responseHelper.invokeError(callback, "Cannot launch Piano ID");
        }
    }

    @ReactMethod
    public void signOut(@Nullable String accessToken, final Callback callback) {
        PianoIdCallback<Object> pianoIdCallback = new PianoIdCallback<Object>() {
            @Override
            public void onSuccess(Object data) {
                responseHelper.cleanResponse();
                responseHelper.invokeResponse(callback);
            }

            @Override
            public void onFailure(@NotNull PianoIdException exception) {
                responseHelper.invokeError(callback, exception.getMessage());
            }
        };
        PianoId.signOut(accessToken != null ? accessToken : "tmp", PianoIdCallback.asResultCallback(pianoIdCallback));

        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(reactContext);
            cookieSyncManager.startSync();
            cookieManager.removeAllCookie();
            cookieSyncManager.stopSync();
        } else {
            cookieManager.removeAllCookies(null);
        }
    }

    @ReactMethod
    public void refreshToken(String refreshToken, final Callback callback) {
        PianoId.refreshToken(refreshToken, PianoIdCallback.asResultCallback(new PianoIdCallback<PianoIdToken>() {
            @Override
            public void onSuccess(@NonNull PianoIdToken data) {
                responseHelper.cleanResponse();
                responseHelper.invokeResponse(callback);
            }
            @Override
            public void onFailure(@NotNull PianoIdException exception) {
                responseHelper.invokeError(callback, exception.getMessage());
            }
        }));
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != PIANO_ID_REQUEST_CODE) {
            return;
        }

        responseHelper.cleanResponse();

        // user cancelled Authorization process
        if (resultCode == activity.RESULT_CANCELED) {
            responseHelper.invokeCancel(callback);
            callback = null;
            return;
        }

        try {
            PianoIdToken token = PianoId.getPianoIdTokenResult(data);
            responseHelper.putString("accessToken", token.accessToken);
            responseHelper.putString("expiresIn", token.expiresIn.toString());
            responseHelper.putString("refreshToken", token.refreshToken);
        } catch (PianoIdException e) {
            e.printStackTrace();
            responseHelper.putString("error", e.getMessage());
        }

        responseHelper.invokeResponse(callback);
        callback = null;
    }

    @Override
    public void onNewIntent(Intent intent) { }
}