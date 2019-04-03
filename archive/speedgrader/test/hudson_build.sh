#!/bin/bash

function log_step {
  echo -e "\n\n---------------------------------------------\n$(date) # $1\n"
}

function variable_configuration_setup {
  start_timestamp=$(date +%s)
  run_tests=0
  compile_release=0

  canvas_branch="master"
}

function cleanup {
  log_step "Cleaning Up"
  build_status_file=""
  if [ "$BUILD_TAG" != "" ]; then
    build_status_file="/tmp/$BUILD_TAG.status"
    if [[ -f $build_status_file ]]; then
      log_step "Removing existing build status file..."
      rm -f $build_status_file
    fi
  fi
  git clean -dfx
  git reset --hard
  rm -rf output_files || true
  mkdir output_files
#  set +e
#  kill -9 `lsof -t -i:3000`
#  rm -rf canvas-lms
#  sudo -su postgres psql -d "canvas_development" -c "select pg_terminate_backend(procpid) from pg_stat_activity where datname='canvas_development'"
#  set -e
}

function validate_output {
  if [[ $1 == *"$2"* ]]; then
    echo "SUCCESS"
    if [ "$build_status_file" != "" ]; then
      log_step "Writing test status to status file $build_status_file..."
      echo "build:0" >> $build_status_file
    fi
  else
    echo "Failures detected"
    if [ "$build_status_file" != "" ]; then
      log_step "Writing test status to status file $build_status_file..."
      echo "build:1" >> $build_status_file
    fi
    exit 1
  fi
}

function compile_droid {
  `cp test/local.properties local.properties`
  if [ $compile_release == 1 ] ; then
    log_step "Compile Release Build"
    `cd release_build; python removeUAS.py`
    `./gradlew changeToNightlyAppIcon`
    `./gradlew clean assembleNightly --info > output_files/main_compile_output.log 2>&1`
    `./gradlew lintRelease`
  else
    log_step "Compile Debug Build."
    `chmod 775 ./gradlew && ./gradlew clean assembleDebug --info > output_files/main_compile_output.log 2>&1`
  fi


  MAIN_OUTPUT=`cat output_files/main_compile_output.log`
  validate_output "$MAIN_OUTPUT" 'BUILD SUCCESSFUL'
}

function install_apks {
  /home/jenkins/Downloads/adt-bundle-linux-x86-20130219/sdk/platform-tools/adb install -r candroid/build/outputs/apk/candroid-debug-unaligned.apk
  # /home/jenkins/Downloads/adt-bundle-linux-x86-20130219/sdk/platform-tools/adb install -r test/bin/test-debug.apk
}

#function instrument_runner {
#  `/home/jenkins/Downloads/adt-bundle-linux-x86-20130219/sdk/platform-tools/adb shell am instrument -w com.instructure.candroid.test/android.test.InstrumentationTestRunner > output_files/instrument_output.log 2>&1`
#  INSTRUMENT_OUTPUT=`cat output_files/instrument_output.log`
#  validate_output "$INSTRUMENT_OUTPUT" 'INSTRUMENTATION_CODE: 0'
#}

function run_unit_tests {
`./gradlew clean connectedInstrumentTest > output_files/test_compile_output.log 2>&1`
ROBOTIUM_OUTPUT=`cat output_files/test_compile_output.log`
validate_output "$ROBOTIUM_OUTPUT" 'BUILD SUCCESSFUL'
}


log_step "Setting Up..."
variable_configuration_setup

while test $# -gt 0; do
  case "$1" in
    --run-tests=*) run_tests=${1#--run-tests=} ;;
    --compile-release=*) compile_release=${1#--compile-release=} ;;
    --canvas-branch=*) canvas_branch=${1#--canvas-branch=} ;;
    *) echo "Unknown option $1" ; exit 1 ;;
  esac
  shift
done

cleanup
compile_droid
if [ $run_tests == 1 ]; then
  install_apks
  run_unit_tests
fi
