#!/usr/bin/env bash
# fail if any commands fails
set -e
# debug log
# set -x

# Script post-process our test results and reports the results to Observe.
# Only works on Bitrise, as certain secrets (like the Observe token) will not be defined locally.

# Capture our command line arguments
if [[ $# < 2 ]]
then
  echo "usage: $0 <appName> <resultsDir>"
  echo "Arg count: $#"
  echo "Args: $@"
  exit 1
fi
appName=$1
resultsDir=$2
reportFile="$resultsDir/JUnitReport.xml"
costFile="$resultsDir/CostReport.txt"

# Make this global so that we can remember it when reporting on passed tests.
suiteName=""

# Common JSON parameters for all event types
commonData="\"workflow\" : \"$BITRISE_TRIGGERED_WORKFLOW_ID\", \"app\" : \"$appName\", \"branch\" : \"$BITRISE_GIT_BRANCH\""

# A running collection of info for all passed tests.  JSON object strings are just concatenated together.
successReport=""
successCount=0

# Emits collected successful test data to splunk, and zeroes out the running trackers.
emitSuccessfulTestData () {
        #echo -e "\nSuccess payload: $successReport\n"
        curl -k "https://103443579803.collect.observeinc.com/v1/http" -H "Authorization: Bearer $OBSERVE_MOBILE_TOKEN" -d "$successReport"
        successReport="" # Reset the successReport after emitting it
        successCount=0
}

# Process the test report (JUnitReport.xml)
while IFS= read -r line
do
    # For <testsuite> lines, emit a deviceSummary event and remember the suiteName
    # Sample line: <testsuite name="NexusLowRes-29-en_US-portrait" tests="275" failures="3" errors="0" skipped="0" time="3577.053" timestamp="2020-01-08T15:32:23" hostname="localhost">
    if [[ $line =~ "testsuite name" ]]
    then
      suiteName=`echo $line | cut -d " " -f 2 | cut -d = -f 2`
      numTests=`echo $line | cut -d " " -f 3 | cut -d = -f 2 | tr -d '"'`
      numFailures=`echo $line | cut -d " " -f 4 | cut -d = -f 2 | tr -d '"'`
      runTime=`echo $line | cut -d " " -f 7 | cut -d = -f 2 | tr -d '"'`
      
      payload="{\"deviceConfig\" : $suiteName, \"numTests\" : $numTests, \"numFailures\" : $numFailures, \"runTime\" : $runTime, $commonData}"
      echo -e "\nsummary payload: $payload"
      curl -k "https://103443579803.collect.observeinc.com/v1/http" -H "Authorization: Bearer $OBSERVE_MOBILE_TOKEN" -d "{\"sourcetype\" : \"mobile-android-qa-summary\", \"event\" : $payload}"
    fi

    # For <testcase> lines, create a "test passed" payload.  We won't include it in our "successReport" until we've
    # verified that the test didn't fail.
    # Sample line: <testcase name="displaysLoadingState" classname="com.instructure.student.ui.renderTests.SubmissionDetailsRenderTest" time="4.346">
    if [[ $line =~ "testcase name" ]]
    then
      # Remove the '<' and '>' from around the line
      line=`echo  $line | tr -d "<>"`
      # Extract various fields from the line
      testName=`echo $line | cut -d " " -f 2 | cut -d = -f 2`
      className=`echo $line | cut -d " " -f 3 | cut -d = -f 2`
      runTime=`echo $line | cut -d " " -f 4 | cut -d = -f 2 | tr -d '"'`
      payload="{\"sourcetype\" : \"mobile-android-qa-testresult\", \"event\" : {\"buildUrl\" : \"$BITRISE_BUILD_URL\", \"status\" : \"passed\", \"testName\": $testName, \"testClass\" : $className, \"deviceConfig\" : $suiteName, \"runTime\" : $runTime, $commonData}}"
      failureEncountered=false
    fi

    # For failures, record the failure so that we don't emit a "passed" event for this test.
    if [[ $line =~ '<failure>' ]]
    then
       failureEncountered=true
    fi

    # If we get to the end of a testcase and no failure has been recorded, then include the test info 
    # in our "successReport".
    if [[ $line =~ "</testcase>" ]]
    then
        if [[ $failureEncountered = false ]]
        then
            successReport="$successReport $payload"
            ((successCount=successCount+1))
            # Emit successful test data to Splunk every 100 tests
            if [ $successCount -eq 100 ]
            then
              emitSuccessfulTestData
            fi    
        fi
    fi
done < "$reportFile"

# Take care of any straggling successful test reports
if [ $successCount -gt 0 ]
then
    emitSuccessfulTestData
fi

# Globals for parsing time/cost info
cost=0
hours=0
minutes=0

# Process the cost report (CostReport.txt)
# Sample file:
#   CostReport
#     Virtual devices
#       $1.15 for 1h 9m


while IFS= read -r line
do
    # We're only interested in the line with the cost / time info in it
    if [[ $line =~ '$' ]]
    then
      #echo "cost report line:$line"
      #if [[ $line =~ '.*[0-9]h.*' ]] ## Argghh -- why wouldn't this work?
      if [[ $line =~ 'h' ]]
      then
         #The line has an 'hours' component.  Parse accordingly.
         #echo "Doing H+M path"
         regex='\$([0-9]\.[0-9]+) for ([0-9])h ([0-9]+)m'
         [[ $line =~ $regex ]]
         cost=${BASH_REMATCH[1]}
         hours=${BASH_REMATCH[2]}
         minutes=${BASH_REMATCH[3]}
      else
         #The line has no 'hours' component.  Parse accordingly.
         #echo "doing M path"
         regex='\$([0-9]\.[0-9]+) for ([0-9]+)m'
         [[ $line =~ $regex ]]
         cost=${BASH_REMATCH[1]}
         minutes=${BASH_REMATCH[2]}
     fi # done with conditional parsing logic

     totalMinutes=$((hours * 60 + minutes))
     #echo "totalMinutes: $totalMinutes"
     payload="{\"minutes\" : $totalMinutes, \"cost\" : $cost, $commonData}"
     echo -e "\ncost payload: $payload"
     #curl -X POST -H "Content-Type: application/json" -d "$payload" $SUMOLOGIC_ENDPOINT
     curl -k "https://103443579803.collect.observeinc.com/v1/http" -H "Authorization: Bearer $OBSERVE_MOBILE_TOKEN" -d "{\"sourcetype\" : \"mobile-android-qa-cost\", \"event\" : $payload}"
    fi
done < "$costFile"
