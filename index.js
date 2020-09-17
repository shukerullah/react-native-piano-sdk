import { NativeModules } from "react-native";
import { createApi, post } from "./fetch";

const API_VERSION = "/api/v3";

const API = {
  PUBLISHER_USER_GET: `${API_VERSION}/publisher/user/get`,
};

const PianoSdkModule = NativeModules.PianoSdk;

const PianoSdk = {
  /**
   * Initializing Piano ID
   *
   * @param {string} aid The Application ID
   * @param {string} endpoint Piano API Endpoint
   * @param {string} [facebookAppId=null] Facebook App Id required for native Facebook sign on
   */
  init(aid: string, endpoint: string, facebookAppId: string = null) {
    createApi(endpoint);
    PianoSdkModule.init(aid, endpoint, facebookAppId);
  },

  /**
   * The Logging. It will return activeToken in a callback which can then be used through the application.
   *
   * @param {*} [callback=() => {}]
   */
  signIn(callback = () => {}) {
    PianoSdkModule.signIn(callback);
  },

  /**
   * The Logging out using.
   *
   * @param {Function} [callback=() => {}]
   * @param {string} [accessToken=null]
   */
  signOut(callback: Function = () => {}, accessToken: string = null) {
    PianoSdkModule.signOut(callback, accessToken);
  },

  /**
   * The function getUser(). Gets a user details.
   *
   * @param {string} aid The Application ID
   * @param {string} uid User's UID
   * @param {string} api_token The API Token
   * @returns User's first_name, last_name, email, personal_name, uid, image1, create_date, reset_password_email_sent, custom_fields
   * }
   */
  getUser(aid: string, uid: string, api_token: string) {
    return post(API.PUBLISHER_USER_GET, { aid, uid, api_token });
  },
};

export default PianoSdk;
