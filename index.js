import { NativeModules } from "react-native";
import { createApi, post } from "./fetch";

const API_VERSION = "/api/v3";

const API = {
  PUBLISHER_USER_GET: `${API_VERSION}/publisher/user/get`,
};

const PianoSdkModule = NativeModules.PianoSdk;

export const ENDPOINT = {
  SANDBOX: "https://sandbox.tinypass.com/",
  PRODUCTION: "https://buy.tinypass.com/",
  PRODUCTION_ASIA_PACIFIC: "https://buy-ap.piano.io/",
  PRODUCTION_AUSTRALIA: "https://buy-au.piano.io/",
};

const PianoSdk = {
  /**
   * The function init(). Initialize ID and Composer
   *
   * @param {string} aid - The Application ID
   * @param {string} endpoint - The Endpoint
   * @param {string} [facebookAppId=null] - Facebook App Id required for native Facebook sign on
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
   * Callback that handles the response
   *
   * @callback responseCallback
   * @param {*} response - The callback that handles the response
   */

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
   * The function getExperience(). It's Piano Experience :D
   *
   * @param {*} config
   * @param {responseCallback} [experienceExecuteListener=null] - A callback to run
   * @param {responseCallback} [experienceExceptionListener=null] - A callback to run
   * @param {responseCallback} [meterListener=null] - A callback to run
   * @param {responseCallback} [nonSiteListener=null] - A callback to run
   * @param {responseCallback} [showLoginListener=null] - A callback to run
   * @param {responseCallback} [userSegmentListener=null] - A callback to run
   * @param {responseCallback} [showTemplateListener=null] - A callback to run
   * @param {responseCallback} [showTemplateCustomEvent=null] - A callback to run
   * @param {responseCallback} [showTemplateLogin=null] - A callback to run
   */
  getExperience(
    config: {},
    experienceExecuteListener: Function = null,
    experienceExceptionListener: Function = null,
    meterListener: Function = null,
    nonSiteListener: Function = null,
    showLoginListener: Function = null,
    userSegmentListener: Function = null,
    showTemplateListener: Function = null,
    showTemplateCustomEvent: Function = null,
    showTemplateLogin: Function = null
  ) {
    PianoSdkModule.getExperience(
      config,
      experienceExecuteListener,
      experienceExceptionListener,
      meterListener,
      nonSiteListener,
      showLoginListener,
      userSegmentListener,
      showTemplateListener,
      showTemplateCustomEvent,
      showTemplateLogin
    );
  },
};

export default PianoSdk;
