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
  init(aid: string, endpoint: string, facebookAppId: string = null) {
    createApi(endpoint);
    PianoSdkModule.init(aid, endpoint, facebookAppId);
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
   * @param {responseCallback} [callback=() => {}] - A callback to run
   */
  signIn(callback = () => {}) {
    PianoSdkModule.signIn(callback);
  },

  /**
   * The function signOut(). Sign out ID.
   *
   * @param {string} [accessToken=null]
   * @param {responseCallback} [callback=() => {}] - A callback to run
   */
  signOut(accessToken: string = null, callback: Function = () => {}) {
    PianoSdkModule.signOut(accessToken, callback);
  },

  /**
   * The function refreshToken(). Refresh token.
   *
   * @param {string} accessToken
   * @param {responseCallback} [callback=() => {}] - A callback to run
   */
  refreshToken(accessToken: string, callback: Function = () => {}) {
    PianoSdkModule.refreshToken(accessToken, callback);
  },

  /**
   * The function setUserToken(). Set Composer user token
   *
   * @param {string} accessToken
   */
  setUserToken(accessToken: string) {
    PianoSdk.setUserToken(accessToken);
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
};

export default PianoSdk;
