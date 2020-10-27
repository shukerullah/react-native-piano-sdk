package com.shukerullah.piano;

import android.os.Build;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.facebook.FacebookSdk;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.piano.android.composer.Composer;
import io.piano.android.composer.listeners.EventTypeListener;
import io.piano.android.composer.listeners.ExperienceExecuteListener;
import io.piano.android.composer.listeners.MeterListener;
import io.piano.android.composer.listeners.NonSiteListener;
import io.piano.android.composer.listeners.ShowLoginListener;
import io.piano.android.composer.listeners.ShowTemplateListener;
import io.piano.android.composer.listeners.UserSegmentListener;
import io.piano.android.composer.model.CustomParameters;
import io.piano.android.composer.model.ExperienceRequest;
import io.piano.android.composer.model.events.EventType;
import io.piano.android.composer.showtemplate.ComposerJs;
import io.piano.android.composer.showtemplate.ShowTemplateController;
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

    private ShowTemplateController showTemplateController;

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
                    public void onSuccess(PianoIdToken token) {
                        Composer.getInstance().userToken(token.accessToken);
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
        Composer.init(reactContext, pianoAID, pianoEndpoint);
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

    @ReactMethod
    public void setUserToken(String accessToken) {
        Composer.getInstance().userToken(accessToken);
    }

    @ReactMethod
    public void getExperience(String config, final Callback callback) {
        try {
            this.callback = callback;
            ExperienceRequest.Builder builder = new ExperienceRequest.Builder();
            JSONObject json = new JSONObject(config);
            if(json.has("debug")) {
                builder.debug(json.getBoolean("debug"));
            }
            if(json.has("zone")) {
                builder.zone(json.getString("zone"));
            }
            if(json.has("referer")) {
                builder.referer(json.getString("referer"));
            }
            if(json.has("url")) {
                builder.url(json.getString("url"));
            }
            if(json.has("tag")) {
                builder.tag(json.getString("tag"));
            }
            if(json.has("tags")) {
                JSONArray tagsJSON = json.getJSONArray("tags");
                List<String> tags = new ArrayList<String>();
                for(int i = 0; i < tagsJSON.length(); i++){
                    tags.add(tagsJSON.get(i).toString());
                }
                builder.tags(tags);
            }
            if(json.has("customParameters")) {
                CustomParameters customParameters = new CustomParameters();
                JSONObject params = json.getJSONObject("customParameters");
                Iterator<String> keys = params.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if(key.equals("request")) {
                        String [] values = getValues(params.getJSONObject(key));
                        customParameters.request(values[0], values[1]);
                    } else if(key.equals("user")) {
                        String [] values = getValues(params.getJSONObject(key));
                        customParameters.user(values[0], values[1]);
                    } else if(key.equals("content")) {
                        String [] values = getValues(params.getJSONObject(key));
                        customParameters.content(values[0], values[1]);
                    } else if(key.equals("contents")) {
                        JSONArray contents =  params.getJSONArray(key);
                        for(int i=0; i<contents.length(); i++) {
                            String [] values = getValues(contents.getJSONObject(i));
                            customParameters.content(values[0], values[1]);
                        }
                    }
                }
                builder.customParams(customParameters);
                ExperienceRequest request = builder.build();

                Collection<EventTypeListener<? extends EventType>> listeners = Arrays.asList(
                        (ExperienceExecuteListener) event -> {
                            responseHelper.putString("type", "ExperienceExecuteListener");
                            responseHelper.putString("event", event.toString());
                            responseHelper.invokeResponse(callback);

                        },
                        (UserSegmentListener) event -> {
                            responseHelper.putString("type", "UserSegmentListener");
                            responseHelper.putString("event", event.toString());
                            responseHelper.invokeResponse(callback);
                        },
                        (ShowLoginListener) event -> {
                            responseHelper.putString("type", "UserSegmentListener");
                            responseHelper.putString("event", event.toString());
                            responseHelper.invokeResponse(callback);
                        },
                        (MeterListener) event -> {
                            responseHelper.putString("type", "UserSegmentListener");
                            responseHelper.putString("event", event.toString());
                            responseHelper.invokeResponse(callback);
                        },
                        (ShowTemplateListener) event -> {
                            showTemplateController = ShowTemplateController.show( (FragmentActivity)getCurrentActivity(), event, new ComposerJs() {
                                @JavascriptInterface
                                @Override
                                public void customEvent(@NonNull String eventData) {
                                    responseHelper.putString("type", "ShowTemplateListener");
                                    responseHelper.putString("sub", "CustomEvent");
                                    responseHelper.putString("event", event.toString());
                                    responseHelper.putString("eventData", eventData);
                                    responseHelper.invokeResponse(callback);
                                }

                                @JavascriptInterface
                                @Override
                                public void login(@NonNull String eventData) {
                                    responseHelper.putString("type", "ShowTemplateListener");
                                    responseHelper.putString("sub", "Login");
                                    responseHelper.putString("event", event.toString());
                                    responseHelper.putString("eventData", eventData);
                                    responseHelper.invokeResponse(callback);
                                }
                            });
                        },
                        (NonSiteListener) event -> {
                            responseHelper.putString("type", "NonSiteListener");
                            responseHelper.putString("event", event.toString());
                            responseHelper.invokeResponse(callback);
                        }
                );
                Composer.getInstance().getExperience(request, listeners, exception -> {
                    String error = exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage();
                    responseHelper.invokeError(callback, error);
                });
            }
        } catch (Exception e) {
            responseHelper.invokeError(callback, e.getMessage());
        }
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

            Composer.getInstance().userToken(token.accessToken);
            if (showTemplateController != null) {
                showTemplateController.reloadWithToken(token.accessToken);
            }

        } catch (PianoIdException e) {
            e.printStackTrace();
            responseHelper.putString("error", e.getMessage());
        }

        responseHelper.invokeResponse(callback);
        callback = null;
    }

    @Override
    public void onNewIntent(Intent intent) { }

    private String[] getValues(JSONObject params) {
        try {
            Iterator<String> keys = params.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                return new String[]{key, params.getString(key)};
            }
        } catch (Exception e) {
            responseHelper.invokeError(callback, e.getMessage());
        }
        return new String [] {};
    }
}