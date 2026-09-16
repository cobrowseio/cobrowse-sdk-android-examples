# Unattended Full Device Demo

A minimal Kotlin app for experimenting with Cobrowse full-device mode on Android.

Every session is switched to full-device mode as soon as it loads, via
`sessionDidLoad` in `DemoApplication.kt`. The single screen shows the current
session, full-device, remote-control and accessibility-service state.

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
