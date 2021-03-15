# react-native-piano-sdk

React Native Piano Sdk

## Installation

**npm:**

    npm install react-native-piano-sdk --save

**Yarn:**

    yarn add react-native-piano-sdk

### iOS \*\*\*

_Underdevelopment, not supported at the moment._

## Usage

```javascript
import PianoSdk from "react-native-piano-sdk";

PianoSdk.init(aid, endPoint, facebookAppId);

PianoSdk.signIn((response) => {
  // TODO: Do something on signIn.
});

PianoSdk.register((response) => {
  // TODO: Do something on register.
});

PianoSdk.signOut(() => {
  // TODO: Do something on signOut.
});

/**
 * The function getUser(). Gets a user.
 *
 * @param {string} aid - The Application ID
 * @param {string} uid - User's UID
 * @param {string} api_token - The API Token
 * @returns User
 */
PianoSdk.getUser(aid, uid, api_token);

// Example:
const user = await PianoSdk.getUser(aid, uid, api_token);
// TODO: What to do with user?

/**
 * The function updateUser(). Updates a user.
 *
 * @param {string} aid - The Application ID
 * @param {string} uid - User's UID
 * @param {string} api_token - The API Token
 * @param {Object} data - The data that you want to update
 * @param {Object} customData - The custom data/fields that you want to update
 * @returns User
 */
PianoSdk.updateUser(aid, uid, api_token, data, customData);

// Example:
const user = await PianoSdk.updateUser(aid, uid, api_token, data, customData);
// TODO: What to do with user?
```

**_NOTE_:**
_You can get **aid** and **uid** by decoding **accessToken** that you will get on signIn or register._

## Example

```javascript
import React from "react";
import {
  View,
  Button,
  ScrollView,
  StyleSheet,
  SafeAreaView,
} from "react-native";
import PianoSdk, { ENDPOINT } from "react-native-piano-sdk";

import { Header, Colors } from "react-native/Libraries/NewAppScreen";

const AID = "ADD YOUR AID";
const FACEBOOK_AID = "ADD YOUR FACEBOOK APP ID";

const styles = StyleSheet.create({
  scrollView: {
    backgroundColor: Colors.lighter,
  },
  body: {
    backgroundColor: Colors.white,
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
});

class App extends React.PureComponent {
  unsubscribe;
  constructor(props) {
    super(props);
    this.state = {
      data: undefined,
    };
  }

  componentDidMount() {
    PianoSdk.init(AID, ENDPOINT.SANDBOX, FACEBOOK_AID);
    this.unsubscribe = PianoSdk.addEventListener(this._onListener);
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  _onListener = (response) => {
    console.log("====onListener====");
    console.log(response);
  };

  _onShowLoginCallback = (response) => {
    console.log("====onShowLoginCallback====");
    console.log(response);
  };

  _onTemplateCallback = (response) => {
    console.log("====onTemplateCallback====");
    console.log(response);
  };

  _signIn = () => {
    PianoSdk.signIn((data) => {
      this.setState({
        data,
      });
    });
  };

  _register = () => {
    PianoSdk.register((data) => {
      this.setState({
        data,
      });
    });
  };

  _getExperience = () => {
    // accessToken: string
    // contentIsNative: boolean
    // debug: boolean
    // url : string,
    // contentAuthor: string,
    // contentSection: string,
    // customVariables: Object,
    // tag: string,
    // tags: Array<string>,
    // zone: string
    // referer: string
    const config = {
      debug: true,
    };

    PianoSdk.getExperience(
      config,
      this._onShowLoginCallback,
      this._onTemplateCallback
    );
  };

  _signOut = () => {
    const accessToken = this.state.data ? this.state.data.accessToken : "";
    PianoSdk.signOut(accessToken, () => {
      this.setState({
        data: undefined,
      });
    });
  };

  render() {
    const { data } = this.state;
    return (
      <SafeAreaView>
        <ScrollView
          contentInsetAdjustmentBehavior="automatic"
          style={styles.scrollView}
        >
          <Header />
          <View style={styles.body}>
            {!data ? (
              <View style={styles.sectionContainer}>
                <Button title="Sign In" onPress={this._signIn} />
              </View>
            ) : null}

            {!data ? (
              <View style={styles.sectionContainer}>
                <Button title="Register" onPress={this._register} />
              </View>
            ) : null}

            {data ? (
              <View style={styles.sectionContainer}>
                <Button title="Sign Out" onPress={this._signOut} />
              </View>
            ) : null}

            <View style={styles.sectionContainer}>
              <Button
                title="Execute Experience"
                onPress={this._getExperience}
              />
            </View>
          </View>
        </ScrollView>
      </SafeAreaView>
    );
  }
}

export default App;
```
