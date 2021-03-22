#  EldersFirebaseRemoteConfig

[![Build Status](https://app.bitrise.io/app/726f7ba5e34d6569/status.svg?token=xI0FV7w4uC0r3jjpbWOYFw&branch=master)](https://app.bitrise.io/app/726f7ba5e34d6569)

This library provides the following convenience extensions to the firebase remote config library:

1. LiveData support
2. Predefined keys and values for required and recommended updates
3. CI scripts

## Installation

## Usage

#### LiveData support

#### SwiftUI support

#### Predefined keys and values for required and recommended updates

The library defines a common structure and config access for requried and recommended updates. These are just an optional interface that you can leverage on to quickly implement a force update mechanism in your app.

-  `RemoteConfig.recommendedUpdate` - represents an update of the app that is recommended for install.
-  `RemoteConfig.requiredUpdate` - represents an update of the app that is required to install.

Both returns an instance of `ApplicationUpdate`. You can check whenever an update should be applied trough the `isApplicable` property.

This functionality basically compares your application version and delivers the defined updates from firebase remote config console.
It is wrapped around the convention that your app's `CFBundleVersion` is structured as `X.Y.Z.a` where:
- **X** is the major number
- **Y** is the minor number
- **Z** is the patch number
- **a** is the build number

**How to make use of it?**

Assuming your application's `CFBundleVersion` is `1.2.3.400`

Go to firebase console and define a new remote config with one of the following keys:

- **ios_required_update** - use this key if you wish to publish a required update
- **ios_recommended_update** - use this key if you wish to publish a recommended update

The predefined keys are variables so you can override them if you wish to.

Then put the following value for the desired key.

```
{
    "version": "1.2.3.500"
    "download": "https://your.appstore.link/"
}
```

The `download` is a URL from where the update should be download. This is typically the AppStore link of your production app.

The `version` is the target version based on which the update is reported to be applicable.
In this example, the build number `500` is greater than the application's build number `400`, so the update's `isApplicable` property will return `true`.

You can ommit the build number in the remote config.
For example if you supply `1.2.3`  - this will make `isApplicable` property will return `false`.
For example if you supply `1.2.4`  - this will make `isApplicable` property will return `true`.

After you define and publish your remote config, you can check from your app whenever an update is available and applicable.

```
//check for required update
if let update = RemoteConfig.remoteConfig().requiredUpdate, update.isApplicable {
    
    //show a blocking dialog to the user to inform him about the update
}

//check for recommended update
if let update = RemoteConfig.remoteConfig().recommendedUpdate, update.isApplicable {
    
    //show a dialog to the user to inform him about the update
}
```
Depending on your needs, you can handle each use case accordingly, however here are some recommendations:
- if you use required updates - update the remote config and check for updates when your app becomes active - this way if the user manage to dismiss your blocking dialog, you can recover from it quickly
- if you use recommeded updates - update the remote config and check for updates at least once when your app starts
- based on your needs, you can implement mechanism to allow users to postpone updates, etc ...

The library declares and delivers the remote config for the updates and check whenever an update is applicable - how you will interpred, use and hadnle these updates is in your hands.

#### CI scripts

The library also includes the following swift scripts which you can use on your CI.
They work with a service account JSON key for authentiation. For more information see [Using OAuth 2.0 for Server to Server Applications](https://developers.google.com/identity/protocols/oauth2/service-account)

- `get_service_account_project_id` - this scripts parses the service account JSON and outputs the `project_id`
- `generate_service_account_jws` - this scripts generates the JWS needed to get access token from google, based on the service account JSON
- `get_service_account_token_url` - this scripts parses the service account JSON and outputs the `token_uri`
- `set_remote_config_recommended_update` - this script updates the firbase remote config with provided version, download url, platform and service account JSON
