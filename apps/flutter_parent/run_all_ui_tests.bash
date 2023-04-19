# It appears that there is no way to just tell "flutter drive" to run all of the tests.
# You have to specify a target.
# This script will find all *_test.dart files in test_driver, and will strip the "_test"
# out of each for a proper target file.
#
# Note that if we started having multiple driver files (e.g., the ones with "_test" in them),
# we might need to tweak this script.  It assumes a 1-1 correspondence between driver files
# and target files.

commonSplunkData="\"workflow\" : \"$BITRISE_TRIGGERED_WORKFLOW_ID\", \"app\" : \"flutter-parent\", \"branch\" : \"$BITRISE_GIT_BRANCH\""

# Common logic to emit a test result
# Arg 1 : test name
# Arg 2 : test status (retry/failed/passed)
# Arg 3 : test time, in seconds
emitTestResult() {
  testName=$1
  testStatus=$2
  runTime=$3

  # If we have no Splunk access, abort.
  # Splunk access = running on bitrise = splunk env var defined
  if [ -z "$SPLUNK_MOBILE_TOKEN" ] 
  then
      echo Aborting test result reporting -- not on Bitrise
      return
  fi

  # TODO: collect runTime info for test
  payload="{\"sourcetype\" : \"mobile-android-qa-testresult\", \"event\" : {\"buildUrl\" : \"$BITRISE_BUILD_URL\", \"status\" : \"$testStatus\", \"testName\": \"$testName\", \"runTime\" : $runTime, $commonSplunkData}}"
  curl -k "https://http-inputs-inst.splunkcloud.com:443/services/collector" -H "Authorization: Splunk $SPLUNK_MOBILE_TOKEN" -d "$payload"
}

passed=0
failed=0
failures=()
for driver in test_driver/*_test.dart
do
        echo "Aggregator: driver = $driver"

        target=${driver/_test}
        echo "Aggregator: target = $target"
	
        # Start the clock
        startTime=`date +"%s"`

        # Run the test
        flutter clean
        flutter pub get
        flutter drive --target=$target

        # Allow for a single retry for a failed test
        if [ $? -ne 0 ]
        then
          # Stop the clock
          endTime=`date +"%s"`
          ((runSecs=endTime-startTime))
          echo "Aggregator: $driver failed; retrying..."
          emitTestResult $driver retry $runSecs
          startTime=`date +"%s"` # restart the clock
          flutter drive --target=$target # rerun the test
        fi

        # Record test result
        if [ $? -eq 0 ]
        then
          # Stop the clock
          endTime=`date +"%s"`
          ((runSecs=endTime-startTime))
          echo Aggregator: $driver Passed, secs=$runSecs
          ((passed=passed+1))
          emitTestResult $driver passed $runSecs
        else
          # Stop the clock
          endTime=`date +"%s"`
          ((runSecs=endTime-startTime))
          echo Aggregator: $driver FAILED, secs=$runSecs
          ((failed=failed+1))
          failures=("${failures[@]}" $driver)
          emitTestResult $driver failed $runSecs
        fi
done

if [ $failed -eq 0 ]
then
  echo Aggregator: All tests \($passed\) passed!
else
  echo Aggregator: $failed failing tests: ${failures[@]}
  exit 1
fi

