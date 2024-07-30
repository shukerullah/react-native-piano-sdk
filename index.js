import { DeviceEventEmitter, NativeEventEmitter, NativeModules, Platform } from "react-native";
import { createApi, get, post } from "./fetch";

const PianoSdkModule = NativeModules.PianoSdk;
const eventEmitter = new NativeEventEmitter(PianoSdkModule)

export const ENDPOINT = {
  SANDBOX: "https://sandbox.tinypass.com/",
  PRODUCTION: "https://buy.tinypass.com/",
  PRODUCTION_ASIA_PACIFIC: "https://buy-ap.piano.io/",
  PRODUCTION_AUSTRALIA: "https://buy-au.piano.io/",
};

const API_VERSION = "/api/v3";

export const API = {
  PUBLISHER_GPDR_DELETE: `${API_VERSION}/publisher/gdpr/delete`,
  PUBLISHER_TOKEN_REFRESH: `${API_VERSION}/publisher/token/refresh`,
  PUBLISHER_USER_GET: `${API_VERSION}/publisher/user/get`,
  PUBLISHER_USER_UPDATE: `${API_VERSION}/publisher/user/update`,
  PUBLISHER_USER_ACCESS_CHECK: `${API_VERSION}/publisher/user/access/check`,
  PUBLISHER_USER_ACCESS_LIST: `${API_VERSION}/publisher/user/access/list`,
  PUBLISHER_CONVERSATION_EXTERNAL_CREATE: `${API_VERSION}/publisher/conversion/external/create`,
};

const PIANO_LISTENER = "PIANO_LISTENER";

export const LISTENER = {
  EXPERIENCE_EXECUTE: "experienceExecuteListener",
  METER: "meterListener",
  NON_SITE: "nonSiteListener",
  SHOW_LOGIN: "showLoginListener",
  SHOW_TEMPLATE: "showTemplateListener",
  TEMPLATE_EVENT: "templateCustomEvent",
  USER_SEGMENT: "userSegmentListener",
  EXPERIENCE_EXCEPTION: "experienceExceptionListener",
  LOGIN: "login",
  REGISTER: "register",
  OFFER_SUBSCRIBE: "offer-subscribe",
};

const PianoSdk = {
  /**
   * Callback that handles the response
   *
   * @callback responseCallback
   * @param {*} response - The callback that handles the response
   */

  /**
   * The function init(). Initialize ID and Composer
   *
   * @param {string} aid - Application ID
   * @param {string} endpoint - The Endpoint
   * @param {string} [facebookAppId=null] - Facebook App Id required for native Facebook sign on
   * @param {responseCallback} [callback=null] - A callback to run
   */
  init(
    aid: string,
    endpoint: string,
    facebookAppId: string = null,
    callback: Function = null
  ) {
    createApi(endpoint);
    if(Platform.OS === 'ios') {
      PianoSdkModule.initWithAID(aid, endpoint);
    }
    else {
      PianoSdkModule.init(aid, endpoint, facebookAppId, callback);
    }
  },

  /**
   * The function signIn(). Sign in ID and it will return activeToken in a callback which can then be used through the application.
   *
   * @param {responseCallback} [callback=null] - A callback to run
   */
  signIn(callback: Function = null) {
    try {
      PianoSdkModule.signIn(callback);
    } catch (err) {
      callback(err);
    }
  },

  signInIOS(
    googleCID: String,
    widgetType: Int,
    didSignInCallbackHandler = ({payload}) => {},
    didCancelSignInCallbackHandler = () => {})
    {
      PianoSdkModule.signInWithGoogleCID(
        googleCID,
        widgetType,
        didSignInCallbackHandler,
        didCancelSignInCallbackHandler);
    },

  /**
   * The function register(). Register in ID and it will return activeToken in a callback which can then be used through the application.
   *
   * @param {responseCallback} [callback=null] - A callback to run
   */
  register(callback: Function = null) {
    try {
      PianoSdkModule.register(callback);
    } catch (err) {
      callback(err);
    }
  },

  /**
   * The function signOut(). Sign out ID.
   *
   * @param {string} [accessToken=null]
   * @param {responseCallback} [callback=null] - A callback to run
   */
  signOut(accessToken: string = null, callback: Function = null) {
    try {
      PianoSdkModule.signOut(accessToken, callback);
    } catch (err) {
      callback(err);
    }
  },
  
  signOutWithToken(token: String, didSignOutCallbackHandler = (error) => {}) {
    PianoOauthModule.signOutWithToken(token, didSignOutCallbackHandler);
  },

  /**
   * The function refreshToken(). Refresh token.
   *
   * @param {string} accessToken
   * @param {responseCallback} [callback=null] - A callback to run
   */
  // This function is currently disabled as it is only available for Android native method.
  // We are awaiting the implementation of the iOS native method.
  // refreshToken(accessToken: string, callback: Function = null) {
  //   PianoSdkModule.refreshToken(accessToken, callback);
  // },

  /**
   * The function refreshToken() is used to refresh a user's token.
   * @param {string} aid - The Application ID.
   * @param {string} api_token - The API token.
   * @param {string} refresh_token - The user's token that needs to be refreshed.
   * @returns {Object} - An object containing the refreshed user token and related information.
   * @property {string} access_token - User’s access token.
   * @property {string} token_type - The type of token (Bearer).
   * @property {string} refresh_token - User's refresh token.
   * @property {number} expires_in - The expiration time of the access token in seconds.
   * @property {boolean} email_confirmation_required - Indicates if email confirmation is required.
   * @property {boolean} extend_expired_access_enabled - Indicates if extending expired access is enabled.
   */
  refreshToken(aid, api_token, refresh_token) {
    return get(API.PUBLISHER_TOKEN_REFRESH, { aid, api_token, refresh_token });
  },

  /**
   * The function setUserToken(). Set Composer user token
   *
   * @param {string} accessToken
   */
  setUserToken(accessToken: string) {
    PianoSdkModule.setUserToken(accessToken);
  },

  /**
   * The function setGaClientId(). Set Google Analytics Client ID.
   *
   * @param {string} gaClientId
   */
  setGaClientId(gaClientId: string) {
    PianoSdkModule.setGaClientId(gaClientId);
  },

  /**
   * The function clearStoredData(). Clear Composer data.
   */
  clearStoredData() {
    PianoSdkModule.clearStoredData();
  },

  /**
   * The function clearStoredData(). Clear Composer data.
   */
  closeTemplateController() {
    PianoSdkModule.closeTemplateController();
  },

  /**
   * The function getUser(). Gets a user.
   *
   * @param {string} aid - Application ID
   * @param {string} uid - User ID
   * @param {string} api_token - API token
   * @returns User
   */
  getUser(aid: string, uid: string, api_token: string) {
    return get(API.PUBLISHER_USER_GET, { aid, uid, api_token });
  },

  /**
   * The function deleteUser(). Delete a user.
   *
   * @param {string} aid - Application ID
   * @param {string} uid - User ID
   * @param {string} api_token - API token
   * @param {string} scope - Delete scope
   * @returns User
   */
  deleteUser(aid: string, uid: string, api_token: string, scope = "ALL") {
    return get(API.PUBLISHER_GPDR_DELETE, { aid, uid, api_token, scope });
  },

  /**
   * The function updateUser(). Updates a user.
   *
   * @param {string} aid - Application ID
   * @param {string} uid - User ID
   * @param {string} api_token - API token
   * @param {Object} data - The data that you want to update
   * @param {Object} customData - The custom data/fields that you want to update
   * @returns User
   */
  updateUser(
    aid: string,
    uid: string,
    api_token: string,
    data: Object,
    customData: Object
  ) {
    return post(
      API.PUBLISHER_USER_UPDATE,
      { aid, uid, api_token, ...data },
      customData
    );
  },

  /**
   * The function checkUserAccess(). Checks a user access.
   *
   * @param {string} aid - Application ID
   * @param {string} rid - Resource ID
   * @param {string} uid - User ID
   * @param {string} api_token - API token
   * @returns User access
   */
  checkUserAccess(aid: string, rid: string, uid: string, api_token: string) {
    return get(API.PUBLISHER_USER_ACCESS_CHECK, { aid, rid, uid, api_token });
  },

  /**
   * Lists all access that user have (resources)
   *
   * @param {string} aid - Application ID
   * @param {string} uid - User ID
   * @param {string} api_token - API token
   * @returns User access
   */
  listUserAccess(aid: string, uid: string, api_token: string) {
    return get(API.PUBLISHER_USER_ACCESS_LIST, { aid, uid, api_token });
  },

  /**
   * The function submitReceipt(). Submits a receipt.
   *
   * @param {string} aid - Application ID
   * @param {string} uid - User ID
   * @param {string} api_token - API token
   * @param {string} term_id - Term ID
   * @param {Object} fields - Receipt that you want to submit
   * @param {boolean} [check_validity=true] - If check_validity is set to false, the subscription is created without checking the validity of the receipt and the verification process will be skipped
   * @returns User access
   */
  submitReceipt(
    aid: string,
    api_token: string,
    uid: string,
    term_id: string,
    fields: Object,
    check_validity = true
  ) {
    return get(API.PUBLISHER_CONVERSATION_EXTERNAL_CREATE, {
      aid,
      api_token,
      uid,
      term_id,
      fields,
      check_validity,
    });
  },

  /**
   * Callback that handle event listener.
   *
   * @callback eventCallback
   * @param {string} eventName - A name of event.
   * @param {*} event - An event
   */

  /**
   * The function getExperience(). It's Piano Experience :D
   *
   * @param {*} config
   * @param {eventCallback} [showLoginCallback] - A callback to run
   * @param {eventCallback} [showTemplateCallback] - A callback to run
   */
  getExperience(
    config: {},
    showLoginCallback = () => {},
    showTemplateCallback = () => {}
  ) {
    PianoSdkModule.getExperience(
      config,
      showLoginCallback,
      showTemplateCallback
    );
  },

  /**
   * The function addEventListener()
   *
   * @param {responseCallback} [callback] - A callback to run
   */
  addEventListener(callback = () => {}) {
    const subscribe = DeviceEventEmitter.addListener(PIANO_LISTENER, callback);
    return () => {
      subscribe.remove();
    };
  },

  execute(aid: String,
    sandbox: bool = true,
    tags: Array = null,
    zoneID: String = null,
    referrer: String = null,
    url: String = null,
    contentAuthor: String = null,
    contentCreated: String = null,
    contentSection: String = null,
    customVariables: Dictionary = null,
    userToken: String = null,
    showLoginHandler = () => {},
    showTemplateHandler = () => {}
    ) {
        if(tags !== null) {
            tags = tags.filter((element) => {
                return element != null;
            });
        }
        PianoSdkModule.executeWithAID(
            aid,
            sandbox,
            tags,
            zoneID,
            referrer,
            url,
            contentAuthor,
            contentCreated,
            contentSection,
            customVariables,
            userToken,
            showLoginHandler,
            showTemplateHandler
            );
    },

    closeTemplateControllerWithCompleteHandler(completeHandler = () => {}) {
      PianoSdkModule.closeTemplateControllerWithCompleteHandler(completeHandler);
    },

    addEventListenerIOS(eventName, callback = () => {}) {
        const subscribe = eventEmitter.addListener(eventName, callback);
        return subscribe
    },

    removeEventListenerIOS(eventName) {
        eventEmitter.removeAllListeners(eventName);
    }
};

export default PianoSdk;
