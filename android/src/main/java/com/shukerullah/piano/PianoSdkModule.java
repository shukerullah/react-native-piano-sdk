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
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.jetbrains.annotations.NotNull;

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

import io.piano.android.composer.model.Access;
import io.piano.android.composer.model.ActiveMeter;
import io.piano.android.composer.model.DelayBy;
import io.piano.android.composer.model.EventExecutionContext;
import io.piano.android.composer.model.EventModuleParams;
import io.piano.android.composer.model.ExperienceRequest;
import io.piano.android.composer.model.SplitTest;
import io.piano.android.composer.model.User;
import io.piano.android.composer.model.events.EventType;
import io.piano.android.composer.model.events.ShowLogin;
import io.piano.android.composer.model.events.ShowTemplate;

import io.piano.android.composer.showtemplate.ComposerJs;
import io.piano.android.composer.showtemplate.ShowTemplateController;

import io.piano.android.id.PianoId;
import io.piano.android.id.PianoIdCallback;
import io.piano.android.id.PianoIdClient;
import io.piano.android.id.PianoIdException;
import io.piano.android.id.facebook.FacebookOAuthProvider;
import io.piano.android.id.google.GoogleOAuthProvider;
import io.piano.android.id.models.PianoIdToken;

public class PianoSdkModule extends ReactContextBaseJavaModule implements ActivityEventListener
{
    public final String REACT_CLASS = "PianoSdk";

    private final String PIANO_LISTENER_NAME = "PIANO_LISTENER";

    private final int PIANO_ID_REQUEST_CODE = 786;

    private final ReactApplicationContext reactContext;

    private ShowTemplateController showTemplateController;

    private Callback callback;

    private WritableMap response = Arguments.createMap();

    private ComposerJs composerJs;

    public PianoSdkModule(ReactApplicationContext reactContext)
    {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName()
    {
        return REACT_CLASS;
    }

    @ReactMethod
    public void init(@NonNull String aid, @NonNull String endpoint, @Nullable String facebookAppId, @Nullable Callback callback)
    {
        PianoIdClient pianoIdClient = PianoId.init(endpoint, aid)
                .with(new PianoIdCallback<PianoIdToken>()
                {
                    @Override
                    public void onSuccess(PianoIdToken pianoIdToken)
                    {
                        onAccessToken(pianoIdToken, callback);
                    }
                    @Override
                    public void onFailure(PianoIdException exception)
                    {
                        invokeError(exception.getMessage(), callback);
                    }
                })
                .with(new GoogleOAuthProvider());
        if(facebookAppId != null)
        {
            FacebookSdk.setApplicationId(facebookAppId);
            FacebookSdk.sdkInitialize(reactContext);
            pianoIdClient.with(new FacebookOAuthProvider());
        }
        Composer.init(reactContext, aid, endpoint);
        reactContext.addActivityEventListener(this);
    }

    @ReactMethod
    public void signIn(@Nullable Callback callback)
    {
        try
        {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity == null)
            {
                throw new ActivityNotFoundException();
            }
            this.callback = callback;
            Intent intent = PianoId.signIn().widget(PianoId.WIDGET_LOGIN).getIntent(reactContext);
            currentActivity.startActivityForResult(intent, PIANO_ID_REQUEST_CODE);
        }
        catch (ActivityNotFoundException exception)
        {
            invokeError(exception.getMessage(), callback);
        }
    }

    @ReactMethod
    public void register(@Nullable Callback callback)
    {
        try
        {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity == null)
            {
                throw new ActivityNotFoundException();
            }
            this.callback = callback;
            Intent intent = PianoId.signIn().widget(PianoId.WIDGET_REGISTER).getIntent(reactContext);
            currentActivity.startActivityForResult(intent, PIANO_ID_REQUEST_CODE);
        }
        catch (ActivityNotFoundException exception)
        {
            invokeError(exception.getMessage(), callback);
        }
    }

    @ReactMethod
    public void signOut(@Nullable String accessToken, @Nullable Callback callback)
    {
        PianoId.signOut(accessToken != null ? accessToken : "tmp", PianoIdCallback.asResultCallback(new PianoIdCallback<Object>()
        {
            @Override
            public void onSuccess(Object data)
            {
                cleanResponse();
                response.putBoolean("success", true);
                invokeResponse(callback);
            }
            @Override
            public void onFailure(PianoIdException exception)
            {
                invokeError(exception.getMessage(), callback);
            }
        }));

        deleteCookies();
        setUserToken(null);
        clearStoredData();
    }

    @ReactMethod
    public void refreshToken(@Nullable String refreshToken, @Nullable Callback callback)
    {
        PianoId.refreshToken(refreshToken, PianoIdCallback.asResultCallback(new PianoIdCallback<PianoIdToken>()
        {
            @Override
            public void onSuccess(PianoIdToken pianoIdToken) {
                onAccessToken(pianoIdToken, callback);
            }
            @Override
            public void onFailure(PianoIdException exception)
            {
                invokeError(exception.getMessage(), callback);
            }
        }));
    }

    @ReactMethod
    public void setUserToken(String accessToken)
    {
        Composer.getInstance().userToken(accessToken);
    }

    @ReactMethod
    public void setGaClientId(@NonNull String gaClientId)
    {
        Composer.getInstance().gaClientId(gaClientId);
    }

    @ReactMethod
    public void clearStoredData()
    {
        Composer.getInstance().clearStoredData();
    }

    @ReactMethod
    public void closeTemplateController()
    {
        composerJs.close("close-modal");
    }

    @ReactMethod
    public void getExperience(@NonNull ReadableMap config, @Nullable Callback showLoginCallback, @Nullable Callback showTemplateCallback)
    {
        ExperienceRequest.Builder builder = new ExperienceRequest.Builder();
        ReadableMapKeySetIterator iterator = config.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();

            if(key.equals("accessToken")) {
                setUserToken(config.getString(key));
            }
            else if(key.contains("debug"))
            {
                builder.debug(config.getBoolean(key));
            }
            else if(key.equals("contentCreated"))
            {
                builder.contentCreated(config.getString(key));
            }
            else if(key.equals("contentAuthor")) {
                builder.contentAuthor(config.getString(key));
            }
            else if(key.equals("contentIsNative")) {
                builder.contentIsNative(config.getBoolean(key));
            }
            else if(key.equals("contentSection")) {
                builder.contentSection(config.getString(key));
            }
            else if(key.equals("referer")) {
                builder.referer(config.getString(key));
            }
            else if(key.equals("url")) {
                builder.url(config.getString(key));
            }
            else if(key.equals("tag")) {
                builder.tag(config.getString(key));
            }
            else if(key.equals("tags")) {
                builder.tags(readableArrayToArrayList(config.getArray(key)));
            }
            else if(key.equals("zone")) {
                builder.zone(config.getString(key));
            }
        }
        ExperienceRequest request = builder.build();
        Collection<EventTypeListener<? extends EventType>> listeners = Arrays.asList(
                (ExperienceExecuteListener) event -> {
                    // TODO
                },
                (MeterListener) event -> {
                    // TODO
                },
                (NonSiteListener) event -> {
                    // TODO
                },
                (ShowLoginListener) event -> {
                    WritableMap map = Arguments.createMap();
                    map.putString("eventName", "showLogin");
                    map.putMap("eventModuleParams", eventModuleParamsToMap(event.eventModuleParams));
                    map.putMap("eventExecutionContext", eventExecutionContextToMap(event.eventExecutionContext));
                    map.putMap("eventData", showLoginToMap(event.eventData));
                    sendEvent(map, showLoginCallback);
                },
                (ShowTemplateListener) event -> {
                    boolean showTemplateControllerIfCancelable = true;
                    if(config.hasKey("showTemplateControllerIfCancelable"))
                    {
                        showTemplateControllerIfCancelable = config.getBoolean("showTemplateControllerIfCancelable")
                                || event.eventData.showCloseButton;
                    }

                    boolean showTemplate = true;
                    if(config.hasKey("showTemplateController"))
                    {
                        showTemplate = config.getBoolean("showTemplateController");
                    }

                    if(showTemplate && showTemplateControllerIfCancelable)
                    {
                        composerJs = new ComposerJs() {
                            @JavascriptInterface
                            @Override
                            public void customEvent(@NonNull String eventData) {
                                WritableMap map = Arguments.createMap();
                                map.putString("eventName", "templateCustomEvent");
                                map.putString("eventData", eventData);
                                sendEvent(map, null);
                            }
                        };
                        showTemplateController = ShowTemplateController.show((FragmentActivity) getCurrentActivity(), event, composerJs);
                    }

                    WritableMap map = Arguments.createMap();
                    map.putString("eventName", "showTemplate");
                    map.putMap("eventModuleParams", eventModuleParamsToMap(event.eventModuleParams));
                    map.putMap("eventExecutionContext", eventExecutionContextToMap(event.eventExecutionContext));
                    map.putMap("eventData", showTemplateToMap(event.eventData));
                    sendEvent(map, showTemplateCallback);
                },
                (UserSegmentListener) event -> {
                    // TODO
                }
        );
        Composer.getInstance().getExperience(request, listeners, exception -> {
            // TODO
        });
    }

    private void deleteCookies()
    {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(reactContext);
            cookieSyncManager.startSync();
            cookieManager.removeAllCookie();
            cookieSyncManager.stopSync();
        }
        else {
            cookieManager.removeAllCookies(null);
        }
    }

    private void onAccessToken(@Nullable final PianoIdToken pianoIdToken, @Nullable final Callback callback)
    {
        cleanResponse();
        if(pianoIdToken != null && pianoIdToken.accessToken != null)
        {
            setUserToken(pianoIdToken.accessToken);

            response.putString("accessToken", pianoIdToken.accessToken);
            response.putString("refreshToken", pianoIdToken.refreshToken);
            response.putString("expiresIn", pianoIdToken.expiresIn.toString());
            response.putString("expiresInTimestamp", pianoIdToken.expiresInTimestamp + "");

            if (showTemplateController != null)
            {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        showTemplateController.reloadWithToken(pianoIdToken.accessToken);
                    }
                });
            }
        }

        invokeResponse(callback);
    }

    private void cleanResponse()
    {
        response = Arguments.createMap();
    }

    private void invokeCancel(@Nullable final Callback callback)
    {
        cleanResponse();
        response.putBoolean("didCancel", true);
        invokeResponse(callback);
    }

    private void invokeError(final String error, @Nullable final Callback callback)
    {
        cleanResponse();
        response.putString("error", error);
        invokeResponse(callback);
    }

    private void invokeResponse(@Nullable final Callback callback)
    {
        if (callback != null)
        {
            callback.invoke(response);
        }
    }

    @NotNull
    private List<String> readableArrayToArrayList(@NotNull ReadableArray readableArray)
    {
        List<String> arrayList = new ArrayList<>();
        for(int i=0; i<readableArray.size(); i++)
        {
            try {
                arrayList.add(readableArray.getString(i));
            } catch (Exception e) {
            }
        }
        return arrayList;
    }

    @NotNull
    private WritableMap showLoginToMap(@NotNull ShowLogin eventData)
    {
        WritableMap map = Arguments.createMap();

        // userProvider: String
        map.putString("userProvider", eventData.userProvider);

        return map;
    }

    @NotNull
    private WritableMap showTemplateToMap(@NotNull ShowTemplate eventData)
    {
        WritableMap map = Arguments.createMap();

        // templateId: String
        map.putString("templateId", eventData.templateId);

        // templateVariantId: String?
        map.putString("templateVariantId", eventData.templateVariantId);

        // displayMode: DisplayMode
        map.putString("displayMode", eventData.displayMode.getMode());

        // containerSelector: String?
        map.putString("containerSelector", eventData.containerSelector);

        // delayBy: DelayBy
        map.putMap("delayBy", delayByToMap(eventData.delayBy));

        // showCloseButton: Boolean
        map.putBoolean("showCloseButton", eventData.showCloseButton);

        // url: String?
        map.putString("url", eventData.url);

        return map;
    }

    @NotNull
    private WritableMap delayByToMap(@NotNull DelayBy delayBy)
    {
        WritableMap map = Arguments.createMap();

        // type: String
        map.putString("type", delayBy.type.name());

        // value: Int
        map.putInt("value", delayBy.value);

        return map;
    }

    @NotNull
    private WritableMap eventModuleParamsToMap(@NotNull EventModuleParams eventModuleParams)
    {
        WritableMap map = Arguments.createMap();

        // moduleId: String
        map.putString("moduleId", eventModuleParams.moduleId);

        // moduleName: String
        map.putString("moduleName", eventModuleParams.moduleName);

        return map;
    }

    @NotNull
    private WritableMap eventExecutionContextToMap(@NotNull EventExecutionContext eventExecutionContext)
    {
        WritableMap map = Arguments.createMap();

        // experienceId: String
        map.putString("experienceId", eventExecutionContext.experienceId);

        // executionId: String
        map.putString("executionId", eventExecutionContext.executionId);

        // trackingId: String
        map.putString("trackingId", eventExecutionContext.trackingId);

        // splitTests: List<SplitTest>?
        map.putArray("splitTests", splitTestListToArray(eventExecutionContext.splitTests.iterator()));

        // currentMeterName: String?
        map.putString("currentMeterName", eventExecutionContext.currentMeterName);

        // user: User?
        map.putMap("user", userToMap(eventExecutionContext.user));

        // region: String
        map.putString("region", eventExecutionContext.region);

        // countryCode: String,
        map.putString("countryCode", eventExecutionContext.countryCode);

        // accessList: List<Access>?
        map.putArray("accessList", accessListToArray(eventExecutionContext.accessList.iterator()));

        // activeMeters: List<ActiveMeter>?
        map.putArray("activeMeters", activeMeterListToArray(eventExecutionContext.activeMeters.iterator()));

        return map;
    }

    @NotNull
    private WritableMap accessToMap(@NotNull Access access)
    {
        WritableMap map = Arguments.createMap();

        // resourceId: String
        map.putString("resourceId", access.resourceId);

        // resourceName: String
        map.putString("resourceName", access.resourceName);

        // daysUntilExpiration: Int
        map.putInt("daysUntilExpiration", access.daysUntilExpiration);

        // expireDate: Int
        map.putInt("expireDate", access.expireDate);

        return map;
    }

    @NotNull
    private WritableMap splitTestToMap(@NotNull SplitTest splitTest)
    {
        WritableMap map = Arguments.createMap();

        // variantId: String
        map.putString("variantId", splitTest.variantId);

        // variantName: String
        map.putString("variantName", splitTest.variantName);

        return map;
    }

    @NotNull
    private WritableMap userToMap(@Nullable User user)
    {
        WritableMap map = Arguments.createMap();

        // userId: String
        map.putString("userId", user.userId);

        // firstName: String?
        map.putString("firstName", user.firstName);

        // lastName: String?
        map.putString("lastName", user.lastName);

        // email: String
        map.putString("email", user.email);

        return map;
    }

    @NotNull
    private WritableMap activeMeterToMap(@NotNull ActiveMeter activeMeter)
    {
        WritableMap map = Arguments.createMap();

        // meterName: String
        map.putString("meterName", activeMeter.meterName);

        // views: Int
        map.putInt("views", activeMeter.views);

        // viewsLeft: Int
        map.putInt("viewsLeft", activeMeter.viewsLeft);

        // maxViews: Int
        map.putInt("maxViews", activeMeter.maxViews);

        // totalViews: Int
        map.putInt("totalViews", activeMeter.totalViews);

        return map;
    }

    @NotNull
    private WritableArray activeMeterListToArray (@Nullable Iterator<ActiveMeter> iterator)
    {
        WritableArray array = Arguments.createArray();

        while (iterator.hasNext()) {
            array.pushMap(activeMeterToMap(iterator.next()));
        }

        return array;
    }

    @NotNull
    private WritableArray accessListToArray(@Nullable Iterator<Access> iterator)
    {
        WritableArray array = Arguments.createArray();

        while (iterator.hasNext()) {
            array.pushMap(accessToMap(iterator.next()));
        }

        return array;
    }

    @NotNull
    private WritableArray splitTestListToArray(@Nullable Iterator<SplitTest> iterator)
    {
        WritableArray array = Arguments.createArray();

        while (iterator.hasNext()) {
            array.pushMap (splitTestToMap(iterator.next()));
        }
        return array;
    }

    @Override
    public void onNewIntent(Intent intent) { }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data)
    {
        if (requestCode != PIANO_ID_REQUEST_CODE) {
            return;
        }

        if (resultCode == activity.RESULT_CANCELED) {
            invokeCancel(callback);
            return;
        }

        try
        {
            onAccessToken(PianoId.getPianoIdTokenResult(data), callback);
        }
        catch (PianoIdException exception)
        {
            invokeError(exception.getMessage(), callback);
        }
    }

    private void sendEvent(WritableMap map, @Nullable Callback callback)
    {
        if(callback != null)
        {
            callback.invoke(map);
        }
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(PIANO_LISTENER_NAME, map);
    }
}