import { NativeModules } from "react-native";

const PianoSdkModule = NativeModules.PianoSdk;

const PianoSdk = {
  init(aid: string, endPoint: string, facebookAppId: string = null) {
    PianoSdkModule.init(aid, endPoint, facebookAppId);
  },
  signIn(callback = () => {}) {
    PianoSdkModule.signIn(callback);
  },
  signOut(callback = () => {}, accessToken: string = null) {
    PianoSdkModule.signOut(callback, accessToken);
  },
};

export default PianoSdk;
