# Instructure Android

Instructure's Open Source Android Code

The open source code provided by the Android Team at Instructure.

## Building

First, install the Flutter SDK using the instructions found [here](https://flutter.dev/docs/get-started/install).

Next, run `./open_source.sh` once. You may now use Gradle to build the apps. Prior to building the Student app for the first time, navigate to `libs/flutter_student_embed` and run the command `flutter pub get`.

### Student and Teacher

1. Open `apps/build.gradle` in Android Studio
```
Android Studio > Import Project > canvas-android/apps/build.gradle
```

2. Select the app from the list of configurations (`student` or `teacher`)
3. Tap 'Run' (`^R`) to run the app

### Parent

1. Open `canvas-android/apps/flutter_parent` in Android Studio.
2. Make sure the `main.dart` configuration is selected
3. Tap 'Run' (`^R`) to run the app

App | Command | Build Status
--- | --- | ---
Student | `./gradle/gradlew -p apps :student:assembleDevDebug` | [![Student build Status](https://app.bitrise.io/app/9417c28328c02b7c/status.svg?token=D7fHdeUlz19PurcEPIQNzw&branch=master)](https://app.bitrise.io/app/9417c28328c02b7c)
Teacher | `./gradle/gradlew -p apps :teacher:assembleDevDebug` | [![Teacher build Status](https://app.bitrise.io/app/4f5339d0ec3436ca/status.svg?token=ATqaYNnYyS4eDUxc0d9EZQ&branch=master)](https://app.bitrise.io/app/4f5339d0ec3436ca)
Parent  | (in apps/flutter_parent) `flutter pub get; flutter build apk` | [![Parent build Status](https://app.bitrise.io/app/39fd3312f33be200/status.svg?token=jiiPeSZlSxrx5lkqccLN1Q&branch=master)](https://app.bitrise.io/app/39fd3312f33be200)

## Running tests

To run unit tests for Student and Teacher
1. Open the Build Variants window and set the variant to `qaDebug` for the app that you wish to test.
2. You can run the tests by tapping on the play button next to the test case or the test class.

## Applications:

#### The Applications we have published on Google Play.

App | Description
--- | ---
[Canvas Student][canvas]      | Used by Students all over the world to be smarter, go faster, and do more.
[Canvas Teacher][teacher]     | User by Teachers all over the world to update course content or grade on the go.
[Canvas Parent][parent]       | Used by Parents all over the world to be parents.

[canvas]: https://play.google.com/store/apps/details?id=com.instructure.candroid
[teacher]: https://play.google.com/store/apps/details?id=com.instructure.teacher
[parent]: https://play.google.com/store/apps/details?id=com.instructure.parentapp

## Modules:

#### These are things that we use internally to create our applications.

Module | Description
   --- | ---
BluePrint    | An MVP Architecture that depends on PandaRecyclerView. 
Canvas-Api   | *Deprecated* - Canvas for Android Api used to talk to Canvas LMS. (deprecated)
Canvas-Api-2 | Canvas for Android Api used to talk to the Canvas LMS and is testable.
dataseedingapi| gRPC wrapper for Canvas that enables creating data to test the apps
Espresso     | The UI testing library built on Espresso.
SoSeedyCLI   | CLI for using data seeding API manually
SoSeedyGRPC  | gRPC server for using data seeding with iOS from Xcode
Foosball     | A Foosball Application created and used interally to boost fun by over 120%.
Login-Api    | *Deprecated* - The Library used to making logging in and getting a token relatively easy. (deprecated)
Login-Api-2  | The libarary used to make logging in and getting a token relative easy and is testable.
PandaUtils   | The core library for new features in the Student and Teacher apps.
PandaRecyclerView | A fancy RecyclerView library that supports expanding and collapsing, pagination, and stuff like that.
Rceditor     | A wrapper for rich content editing used in Canvas Teacher.

#### Our applications are licensed under the GPLv3 License.

```
Copyright (C) 2016 - present  Instructure, Inc.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ```

#### Our Modules are licensed under the Apache v2 License.

```
Copyright (C) 2016 - present Instructure, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
