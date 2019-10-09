# Android App Bundles

Bundles are the new way to make apps smaller when downloading from the Google Play Store. This separates the app into bundled components, from core, to screen density, to system architecture. 

### Unique for Canvas Android
By default, languages are also included in this bundle split. For Canvas, this is not ideal as there are many language scenarios that need to be supported (users can set language on account, institutions can set custom languages, etc...). This has been turned off for our builds to avoid any issues when trying to download languages dynamically. The overall size increase when keeping languages in was about 2 MB. If many more custom languages are being added, this could be looked at again to offer users an even smaller APK.

### Local Development
To be able to install bundles locally, you need to install `bundletool` from [Googles github](https://github.com/google/bundletool/releases).
Once downloaded, move the jar to some static place in your directory to be referenced from anywhere. Create an alias to use the jar to make it easier on yourself during development:

`alias bundletool='java -jar /Users/$USER/Documents/Code/bundletool/bundletool-all-0.10.3.jar'`

Here's an example creating and installing a `prodRelease` bundle from command line (signing is not required, it will use the debug keystore by default):
```
// Go to the project root and into the apps directory (to be able to run the gradle task)
$ cd {project_root}/apps

// Build the app bundle (this could also be done in Android Studio through Build -> Build Bundles, though the output directory is slightly different)
$ ./../gradle/gradlew :student:bundleProdRelease -Pandroid.injected.signing.store.file="/your/path/to/release.keystore" -Pandroid.injected.signing.store.password='keystorePassword' -Pandroid.injected.signing.key.alias="alias" -Pandroid.injected.signing.key.password='aliasPassword'

// Build the apks for every device configuration from the app bundle
$ bundletool build-apks --bundle=student/build/outputs/bundle/prodRelease/student.aab --output=student/build/outputs/bundle/prodRelease/student.apks --ks=~/keys/candroid/release.keystore --ks-pass=file:/Users/$USER/keys/candroid/key.pwd --ks-key-alias=candroid --key-pass=file:/Users/$USER/keys/candroid/key.pwd

// Install the app bundle on a connected device
$ bundletool install-apks --apks=student/build/outputs/bundle/prodRelease/student.apks
```
### But what does the end APK _look_ like?
When you run the `bundletool build-apks ...` command, it outputs a zipped file full of different apks. Running the `bundletool install-apks ...` will gather the apks that are needed for the destined device and install them. If you look at the unzipped contents from the build-apks command it looks like this (captured at student version 6.6.3):
```
12M  - base-master.apk
12M  - base-master_2.apk
7.6M - base-arm64_v8a.apk
23M  - base-arm64_v8a_2.apk
6.9M - base-armeabi_v7a.apk
17M  - base-armeabi_v7a_2.apk
7.9M - base-x86.apk
22M  - base-x86_2.apk
7.9M - base-x86_64.apk
23M  - base-x86_64_2.apk
1.6M - base-tvdpi.apk
624K - base-mdpi.apk
1.0M - base-ldpi.apk
1.1M - base-hdpi.apk
1.5M - base-xhdpi.apk
2.3M - base-xxhdpi.apk
2.7M - base-xxxhdpi.apk
```
So an x86 hdpi device would need the master apk, the x86 apk, and the hdpi apk, totaling in an app size of  around 35 MB. Wow, what a savings!
(Note: not sure what the `_2` versions are, but doesn't seem like they're required all the time so it doesn't seem like multidex apks)

### Sources

bundletool: https://developer.android.com/studio/command-line/bundletool
