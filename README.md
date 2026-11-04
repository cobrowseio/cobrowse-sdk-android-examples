# Cobrowse.io - Example Android Apps

Cobrowse.io is 100% free and easy to try out in your own apps. Please see full documentation at [https://docs.cobrowse.io](https://docs.cobrowse.io).

Try our **online demo** at the bottom of our homepage at <https://cobrowse.io/demo>.

## Building

This is a demo application for the [Cobrowse.io](https://cobrowse.io/) screen sharing service. See the [Cobrowse.io SDK](https://github.com/cobrowseio/cobrowse-sdk-android-binary) repository for more information.

You can build the example apps with Android Studio:

- [`sample`](/sample) - Kotlin example app
- [`sample-compose`](/sample-compose) - the same app, with Jetpack Compose UI
- [`standalone`](/standalone) - Java standalone example

The `sample` and `sample-compose` apps share their data and resources through the [`sample-core`](/sample-core) library.

## Add your license key

Please register an account and generate your free License Key at <https://cobrowse.io/dashboard/settings>.

This will associate sessions from your mobile app with your Cobrowse.io account.

- [`sample`](/sample) and [`sample-compose`](/sample-compose): copy your license key, open up [`sample/…/MainApplication.kt`](/sample/src/main/java/io/cobrowse/sample/MainApplication.kt) or [`sample-compose/…/MainApplication.kt`](/sample-compose/src/main/java/io/cobrowse/sample/compose/MainApplication.kt) and replace `trial` with your license key:

```kotlin
license("trial")
```

- [`standalone`](/standalone): copy your license key, open up the settings in the app and replace `trial` with your license key.

## Firebase Cloud Messaging

The example apps include preconfigured Firebase push notifications (FCM). To enable it, add your `google-services.json` to [`sample`](/sample), [`sample-compose`](/sample-compose) and [`standalone`](/standalone) modules.

Refer to the [Firebase docs](https://firebase.google.com/docs/android/setup) to see how to create a new Firebase project and generate your own `google-services.json`.

## Deep links

The [`sample`](/sample) and [`sample-compose`](/sample-compose) apps can be configured with `https://cbrws.io/...` links, handled by [`CobrowseDeepLinks.kt`](/sample-core/src/main/java/io/cobrowse/sample/data/CobrowseDeepLinks.kt):

- `https://cbrws.io/api/https%3A%2F%2Fcobrowse.io` - sets the Cobrowse.io API
- `https://cbrws.io/license/trial` - sets the license key
- `https://cbrws.io/data/?firstKey=firstValue&secondKey=secondValue` - sets custom data
- `https://cbrws.io/code/000000` - starts a session by its 6-digit code
- `https://cbrws.io/s/?id=abc` - starts a session by its ID
- `https://cbrws.io/demo/someDemoId?api=https%3A%2F%2Fcobrowse.io&license=trial` - sets up a demo and logs in. `api`, `license` and the other query parameters are optional, and the other parameters are set as custom data

The values in the path and the query must be URL-encoded. To open a link with adb, name the app's package, `io.cobrowse.demo` or `io.cobrowse.demo.compose`:

```shell
adb shell 'am start -a android.intent.action.VIEW -d "https://cbrws.io/license/trial" io.cobrowse.demo.compose'
```

`cbrws.io` links are verified for the `sample` app only. To open them in `sample-compose` from other apps, enable "Open supported links" in its app settings.

## Try it out

Once you have the app running in the Android emulator or on a physical device, navigate to <https://cobrowse.io/dashboard> to see your device listed. You can click the "Connect" button to initiate a Cobrowse session!

## Questions?

Any questions at all? Please email us directly at [hello@cobrowse.io](mailto:hello@cobrowse.io).

## Requirements

* [`sample`](/sample) and [`sample-compose`](/sample-compose): API version 24 (7.0 Nougat) or later
* [`standalone`](/standalone): API version 19 (4.4 KitKat) or later
