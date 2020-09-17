# react-native-piano-sdk

React Native Piano Sdk

## Installation

**npm:**

    npm install react-native-piano-sdk --save

**Yarn:**

    yarn add react-native-piano-sdk

### iOS

_Underdevelopment, not supported at the moment._

## Usage

```javascript
import PianoSdk from "react-native-piano-sdk";

PianoSdk.init(aid, endPoint, facebookAppId);

PianoSdk.signIn((response) => {
  // TODO: What to do with signIn?
});

PianoSdk.signOut(() => {
  // TODO: What to do with signOut?
});
```
