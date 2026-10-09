# Cobrowse.io - Example Android Apps

Cobrowse.io is 100% free and easy to try out in your own apps. Please see full documentation at [https://docs.cobrowse.io](https://docs.cobrowse.io).

Try our **online demo** at the bottom of our homepage at <https://cobrowse.io/demo>.

## Building

This is a demo application for the [Cobrowse.io](https://cobrowse.io/) screen sharing service. See the [Cobrowse.io SDK](https://github.com/cobrowseio/cobrowse-sdk-android-binary) repository for more information.

You can build the example apps with Android Studio:

- [`sample`](/sample) - Kotlin example app
- [`standalone`](/standalone) - Java standalone example
- [`unattended-full-device-demo`](/unattended-full-device-demo) - Kotlin unattended full-device remote control demo

## Add your license key

Please register an account and generate your free License Key at <https://cobrowse.io/dashboard/settings>.

This will associate sessions from your mobile app with your Cobrowse.io account.

- [`sample`](/sample): copy your license key, open up [`MainApplication.kt`](/sample/src/main/java/io/cobrowse/sample/MainApplication.kt) and replace `trial` with your license key:

```kotlin
license("trial")
```

- [`standalone`](/standalone): copy your license key, open up the settings in the app and replace `trial` with your license key.

## Firebase Cloud Messaging

The example apps include preconfigured Firebase push notifications (FCM). To enable it, add your `google-services.json` to [`sample`](/sample) and [`standalone`](/standalone) modules.

Refer to the [Firebase docs](https://firebase.google.com/docs/android/setup) to see how to create a new Firebase project and generate your own `google-services.json`.

## Try it out

Once you have the app running in the Android emulator or on a physical device, navigate to <https://cobrowse.io/dashboard> to see your device listed. You can click the "Connect" button to initiate a Cobrowse session!

## Questions?

Any questions at all? Please email us directly at [hello@cobrowse.io](mailto:hello@cobrowse.io).

## Requirements

* API version 19 (4.4 KitKat) or later
