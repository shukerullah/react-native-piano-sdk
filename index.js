import { NativeModules, DeviceEventEmitter } from "react-native";
import { createApi, post } from "./fetch";

const API_VERSION = "/api/v3";

const API = {
  PUBLISHER_USER_GET: `${API_VERSION}/publisher/user/get`,
};

const PianoSdkModule = NativeModules.PianoSdk;

const PIANO_LISTENER = "PIANO_LISTENER";

export const LISTENER = {
  EXPERIENCE_EXECUTE: "experienceExecuteListener",
  METER: "meterListener",
  NON_STIE: "nonSiteListener",
  SHOW_LOGIN: "showLoginListener",
  SHOW_TEMPLATE: "showTemplateListener",
  TEMPLATE_EVENT: "templateCustomEvent",
  USER_SEGMENT: "userSegmentListener",
  EXPERIENCE_EXCEPTION: "experienceExceptionListener",
  LOGIN: "login",
  REGISTER: "register",
};

export const ENDPOINT = {
  SANDBOX: "https://sandbox.tinypass.com/",
  PRODUCTION: "https://buy.tinypass.com/",
  PRODUCTION_ASIA_PACIFIC: "https://buy-ap.piano.io/",
  PRODUCTION_AUSTRALIA: "https://buy-au.piano.io/",
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
   * @param {string} aid - The Application ID
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
    PianoSdkModule.init(aid, endpoint, facebookAppId, callback);
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

  /**
   * The function refreshToken(). Refresh token.
   *
   * @param {string} accessToken
   * @param {responseCallback} [callback=null] - A callback to run
   */
  refreshToken(accessToken: string, callback: Function = null) {
    PianoSdkModule.refreshToken(accessToken, callback);
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
   * The function getUser(). Gets a user details.
   *
   * @param {string} aid - The Application ID
   * @param {string} uid - User's UID
   * @param {string} api_token - The API Token
   * @returns User's first_name, last_name, email, personal_name, uid, image1, create_date, reset_password_email_sent, custom_fields
   */
  getUser(aid: string, uid: string, api_token: string) {
    return post(API.PUBLISHER_USER_GET, { aid, uid, api_token });
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
};

export default PianoSdk;
