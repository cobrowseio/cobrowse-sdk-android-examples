# Unattended Full Device Demo

A minimal Kotlin app for experimenting with Cobrowse full-device mode on Android.

Every session is switched to full-device mode as soon as it loads, via
`sessionDidLoad` in `DemoApplication.kt`. The single screen shows the current
session, full-device, remote-control and accessibility-service state.

The app is designed to work while in the background, as a device that sits idle
until an agent connects. Session consent is a five-second countdown drawn over
whatever is on screen, which auto-accepts unless the user taps **Decline**. It is
driven from the session state in `sessionDidUpdate` rather than from the SDK's
`handleSessionRequest` delegate, because the SDK only calls that delegate while
one of the app's activities is in the foreground. The countdown only appears when
the account's "require consent" setting is on in the dashboard; with it off, the
SDK activates sessions immediately.

## Run

Deploy from the terminal rather than with Android Studio's Run button:

```
./gradlew :unattended-full-device-demo:installDebug
adb shell am start -n io.cobrowse.unattended/.MainActivity
```

Android Studio force-stops the app before reinstalling it, and Android clears a
force-stopped app's accessibility service from the enabled list. You would have to
re-enable the service in system settings after every run. Gradle's install task
reinstalls without a force-stop, so the service stays enabled.

Then connect to the device from <https://cobrowse.io/dashboard>. The session
starts with no interaction on the device; **End session** appears while one is active.

Full-device remote control also needs the Cobrowse accessibility service enabled
in system settings. Use **Open accessibility settings** to get there.

## Session overlay

While a session is active the app draws a small "Screen shared with support" badge
over other apps, from `SessionOverlay.kt`, so the person holding the device can see
the session even while the agent is in another app. This needs the draw-over-apps
permission. Use **Allow drawing over other apps** to grant it, or on the emulator:

```
adb shell appops set io.cobrowse.unattended SYSTEM_ALERT_WINDOW allow
```

Android hides application overlays while apps that opt out of them are in the
foreground, and the system Settings app is one of those. Expect the badge to
disappear while the agent is in Settings and return when they leave. An indicator
drawn by an accessibility service is not affected by this.
