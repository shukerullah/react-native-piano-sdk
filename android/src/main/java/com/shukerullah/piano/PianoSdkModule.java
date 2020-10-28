package com.shukerullah.piano;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.facebook.FacebookSdk;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import io.piano.android.composer.model.events.UserSegment;
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

    private Callback callback;

    public PianoSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void init(@NonNull String aid, @NonNull String endpoint, @Nullable String facebookAppId, @Nullable Callback callback) {
        PianoIdClient pianoIdClient = PianoId.init(endpoint, aid)
                .with(new PianoIdCallback<PianoIdToken>() {
                    @Override
                    public void onSuccess(PianoIdToken token) {
                        callback.invoke(token);
                    }
                    @Override
                    public void onFailure(PianoIdException exception) {
                        callback.invoke(exception);
                    }
                })
                .with(new GoogleOAuthProvider());
        if(facebookAppId != null) {
            FacebookSdk.setApplicationId(facebookAppId);
            FacebookSdk.sdkInitialize(reactContext);
            pianoIdClient.with(new FacebookOAuthProvider());
        }
        Composer.init(reactContext, aid, endpoint);
    }

    @ReactMethod
    public void signIn(@Nullable Callback callback) {
        try {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity == null) {
                throw new ActivityNotFoundException();
            }
            this.callback = callback;
            Intent intent = PianoId.signIn().widget(PianoId.WIDGET_LOGIN).getIntent(reactContext);
            currentActivity.startActivityForResult(intent, PIANO_ID_REQUEST_CODE);
        } catch (ActivityNotFoundException exception) {
            callback.invoke(exception);
        }
    }

    @ReactMethod
    public void register(@Nullable Callback callback) {
        try {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity == null) {
                throw new ActivityNotFoundException();
            }
            this.callback = callback;
            Intent intent = PianoId.signIn().widget(PianoId.WIDGET_REGISTER).getIntent(reactContext);
            currentActivity.startActivityForResult(intent, PIANO_ID_REQUEST_CODE);
        } catch (ActivityNotFoundException exception) {
            callback.invoke(exception);
        }
    }

    @ReactMethod
    public void signOut(@Nullable String accessToken, @Nullable Callback callback) {
        PianoId.signOut(accessToken != null ? accessToken : "tmp", PianoIdCallback.asResultCallback(new PianoIdCallback<Object>() {
            @Override
            public void onSuccess(Object data) {
                deleteCookies();
                callback.invoke(data);
                onAccessToken(null);
            }
            @Override
            public void onFailure(PianoIdException exception) {
                callback.invoke(exception);
            }
        }));
    }

    @ReactMethod
    public void refreshToken(@Nullable String refreshToken, @Nullable Callback callback) {
        PianoId.refreshToken(refreshToken, PianoIdCallback.asResultCallback(new PianoIdCallback<PianoIdToken>() {
            @Override
            public void onSuccess(PianoIdToken token) {
                callback.invoke(token);
                onAccessToken(token.accessToken);
            }
            @Override
            public void onFailure(PianoIdException exception) {
                callback.invoke(exception);
            }
        }));
    }

    @ReactMethod
    public void setUserToken(@Nullable String accessToken) {
        Composer.getInstance().userToken(accessToken);
    }

    @ReactMethod
    public void setGaClientId(@NonNull String gaClientId) {
        Composer.getInstance().gaClientId(gaClientId);
    }

    @ReactMethod
    public void getExperience(@NonNull WritableMap map,
                              @Nullable Callback experienceExecuteListener,
                              @Nullable Callback experienceExceptionListener,
                              @Nullable Callback meterListener,
                              @Nullable Callback nonSiteListener,
                              @Nullable Callback showLoginListener,
                              @Nullable Callback userSegmentListener,
                              @Nullable Callback showTemplateListener,
                              @Nullable Callback showTemplateCustomEvent,
                              @Nullable Callback showTemplateLogin) {
        ExperienceRequest.Builder builder = new ExperienceRequest.Builder();
        ReadableMapKeySetIterator iterator = map.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            if(key.equals("contentCreated")) {
                builder.contentCreated(map.getString("contentCreated"));
            } else if(key.equals("contentCreated")) {
                builder.contentAuthor(map.getString("contentAuthor"));
            } else if(key.equals("contentIsNative")) {
                builder.contentIsNative(map.getBoolean("contentIsNative"));
            } else if(key.equals("contentSection")) {
                builder.contentSection(map.getString("contentSection"));
            } else if(key.equals("customParams")) {
                CustomParameters customParameters = new CustomParameters();
                ReadableMap readableMap = map.getMap(key);
                ReadableMapKeySetIterator cpIterator = readableMap.keySetIterator();
                while (cpIterator.hasNextKey()) {
                    String cpKey = cpIterator.nextKey();
                    if(cpKey.equals("content")) {
                        _Object _object = getObject(readableMap.getMap(cpKey));
                        customParameters.content(_object.key, _object.value);
                    } else if(cpKey.equals("contents")) {
                        ReadableArray array = readableMap.getArray(cpKey);
                        for (int i=0; i<array.size(); i++) {
                            _Object _object = getObject(array.getMap(i).getMap(cpKey));
                            customParameters.content(_object.key, _object.value);
                        }
                    } else if(cpKey.equals("request")) {
                        _Object _object = getObject(readableMap.getMap(cpKey));
                        customParameters.request(_object.key, _object.value);
                    } else if(cpKey.equals("requests")) {
                        ReadableArray array = readableMap.getArray(cpKey);
                        for (int i=0; i<array.size(); i++) {
                            _Object _object = getObject(array.getMap(i).getMap(cpKey));
                            customParameters.request(_object.key, _object.value);
                        }
                    } else if(cpKey.equals("user")) {
                        _Object _object = getObject(readableMap.getMap(cpKey));
                        customParameters.user(_object.key, _object.value);
                    } else if(cpKey.equals("user")) {
                        ReadableArray array = readableMap.getArray(cpKey);
                        for (int i=0; i<array.size(); i++) {
                            _Object _object = getObject(array.getMap(i).getMap(cpKey));
                            customParameters.user(_object.key, _object.value);
                        }
                    }
                }
                builder.customParams(customParameters);
            } else if(key.equals("customVariable")) {
                _Object _object = getObject(map.getMap("customVariable"));
                builder.customVariable(_object.key, _object.value);
            } else if(key.equals("customVariables")) {
                builder.customVariables(getArrayObject(map.getArray("customVariables")).getArrayObject());
            } else if(key.equals("debug")) {
                builder.debug(map.getBoolean("debug"));
            } else if(key.equals("debug")) {
                builder.debug(map.getBoolean("debug"));
            }  else if(key.equals("gaClientId")) {
                // builder.gaClientId(map.getString("gaClientId"));
                setGaClientId(map.getString("gaClientId"));
            } else if(key.equals("referrer")) {
                builder.referer(map.getString("referrer"));
            } else if(key.equals("tag")) {
                builder.tag(map.getString("tag"));
            } else if(key.equals("tags")) {
                builder.tags(getArray(map.getArray("tags")));
            } else if(key.equals("url")) {
                builder.url(map.getString("url"));
            } else if(key.equals("userToken")) {
                // builder.userToken(map.getString("userToken"));
                setUserToken(map.getString("userToken"));
            } else if(key.equals("zone")) {
                builder.zone(map.getString("zone"));
            }
        }
        ExperienceRequest request = builder.build();
        Collection<EventTypeListener<? extends EventType>> listeners = Arrays.asList(
                (ExperienceExecuteListener) event -> {
                    if(experienceExecuteListener != null) {
                        experienceExecuteListener.invoke(event);
                    }
                },
                (MeterListener) event -> {
                    if(meterListener != null) {
                        meterListener.invoke(event);
                    }
                },
                (NonSiteListener) event -> {
                    if(nonSiteListener != null) {
                        nonSiteListener.invoke(event);
                    }
                },
                (ShowLoginListener) event -> {
                    if(showLoginListener != null) {
                        showLoginListener.invoke(event);
                    }
                },
                (ShowTemplateListener) event -> {
                    if(showTemplateListener != null) {
                        showTemplateListener.invoke(event);
                    }
                    showTemplateController = ShowTemplateController.show((FragmentActivity)getCurrentActivity(), event, new ComposerJs() {
                        @JavascriptInterface
                        @Override
                        public void customEvent(@NonNull String eventData) {
                            if(showTemplateCustomEvent != null) {
                                showTemplateCustomEvent.invoke(eventData);
                            }
                        }
                        @JavascriptInterface
                        @Override
                        public void login(@NonNull String eventData) {
                            if(showTemplateLogin != null) {
                                showTemplateLogin.invoke(eventData);
                            }
                        }
                    });
                },
                (UserSegmentListener) event -> {
                    if(userSegmentListener != null) {
                        userSegmentListener.invoke(event);
                    }
                }
        );
        Composer.getInstance().getExperience(request, listeners, exception -> {
            if(experienceExceptionListener != null) {
                experienceExceptionListener.invoke(exception);
            }
        });
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != PIANO_ID_REQUEST_CODE) {
            return;
        }

        if(requestCode == activity.RESULT_OK) {
            PianoIdToken token = PianoId.getPianoIdTokenResult(data);
            onAccessToken(token.accessToken);
            if(callback != null) {
                callback.invoke(token);
            }
            return;
        }

        WritableMap event = Arguments.createMap();
        String message = requestCode == activity.RESULT_CANCELED ? "User canceled OAuth" : "Something went";
        event.putString("message", message);
        event.putInt("resultCode", resultCode);
        if(callback != null) {
            callback.invoke(event);
        }
    }

    private void onAccessToken(@Nullable String accessToken) {
        Composer.getInstance().userToken(accessToken);
        if (showTemplateController != null && accessToken != null) {
            showTemplateController.reloadWithToken(accessToken);
        }
    }

    private void deleteCookies() {
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

    @NotNull
    private ArrayObject getArrayObject(@NotNull ReadableArray readableArray) {
        ArrayObject arrayObject = new ArrayObject();
        for(int i=0; i<readableArray.size(); i++) {
            _Object _object = getObject(readableArray.getMap(i));
            arrayObject.addObject(_object);
        }
        return arrayObject;
    }

    @NotNull
    private _Object getObject(@NotNull ReadableMap readableMap) {
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            return new _Object(key, readableMap.getString(key));
        }
        return new _Object();
    }

    @NotNull
    private List<String> getArray(@NotNull ReadableArray readableArray) {
        List<String> list = new ArrayList<String>();
        for(int i=0; i<readableArray.size(); i++) {
            list.add(readableArray.getString(i));
        }
        return list;
    }

    private class ArrayObject {
        public Map<String, String> arrayObject = new HashMap<>();

        public Map<String, String> getArrayObject() {
            return arrayObject;
        }

        public void addObject(_Object object) {
            arrayObject.put(object.key, object.value);
        }
    }

    private class _Object {
        private String key;
        private String value;

        public _Object() {
        }

        public _Object(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Override
    public void onNewIntent(Intent intent) { }
}