# Script to process "flutter test --machine" output.
# Designed to:
#  (1) Make failures easier to identify in the log
#  (2) Emit test failure and test summary info to Splunk

successCount=0
failureCount=0
failures=()
commonSplunkData="\"workflow\" : \"$BITRISE_TRIGGERED_WORKFLOW_ID\", \"app\" : \"flutter_student_embed\", \"branch\" : \"$BITRISE_GIT_BRANCH\""

echo file is $1
while IFS= read -r line
do

  # Process a "test" entry, marking the beginning of the test
  # This is where we capture file name and test name, map test id to each
  # Sample line:
  # {"test":{"id":1799,"name":"internal url handler launches simpleWebView for limitAccessFlag without match","suiteID":1586,"groupIDs":[1731,1786],"metadata":{"skip":false,"skipReason":null},"line":112,"column":3,"url":"package:flutter_test/src/widget_tester.dart","root_line":512,"root_column":5,"root_url":"file:///Users/jhoag/code/projects/canvas-android/apps/flutter_parent/test/router/panda_router_test.dart"},"type":"testStart","time":79561}

  if [[ $line =~ "\"test\":" ]]
  then
    # Parse the test id
    idRegex='\"id\":([0-9]+),'
    [[ $line =~ $idRegex ]]
    id=${BASH_REMATCH[1]}

    # Parse the test name
    nameRegex='\"name\":\"([A-Za-z0-9 -\(\)/\._]+)\",\"suiteID\"'
    [[ $line =~ $nameRegex ]]
    name=${BASH_REMATCH[1]}

    # Parse the file name.  We'll try to grab the root_url field first, then the url field.
    urlRegex='\"root_url\":\"([A-Za-z0-9 -:\(\)/\._]+)\"'
    [[ $line =~ $urlRegex ]]
    url=${BASH_REMATCH[1]}
    if [ -z "$url" ]
    then 
      urlRegex='\"url\":\"([A-Za-z0-9 -:\(\)/\._]+)\"'
      [[ $line =~ $urlRegex ]]
      url=${BASH_REMATCH[1]}
    fi

    # If we've succeeded in getting a url value, parse the test file from that.
    if [ -n "$url" ]
    then
    
      file=`echo $url | rev | cut -d "/" -f 1 | rev`

      #echo id=$id, name=$name, url=$url, file=$file
      nameMap[$id]=$name
      fileMap[$id]=$file

      # Print out the file and test name to show some progress.
      # This is optional.  We can take it out if we want to shrink bitrise logs.
      echo -en "\r\033[K$file - $name"
    fi

  fi

  # Process a test message.
  # There can be many messages in a test, but we'll assume that the final message
  # for a failed test is the error message with stack trace.  So we'll track the
  # last message we see for each test.
  if [[ $line =~ "\"messageType\":\"print\"" ]]
  then

    # Grab the test id field
    idRegex='\"testID\":([0-9]+),'
    [[ $line =~ $idRegex ]]
    id=${BASH_REMATCH[1]}

    # Grab the messsage field
    messageRegex='\"message\":\"(.+)\",\"type\"'
    [[ $line =~ $messageRegex ]]
    message=${BASH_REMATCH[1]}

    # Record this message as the last one received for the test
    messageMap[$id]=$message
  fi

    

  # Process a test result, reporting failures to splunk
  # Sample line:
  # {"testID":1801,"result":"success","skipped":false,"hidden":false,"type":"testDone","time":79662}
  if [[ $line =~ "\"result\":" ]]
  then

    # Grab the test id field
    idRegex='\"testID\":([0-9]+),'
    [[ $line =~ $idRegex ]]
    id=${BASH_REMATCH[1]}

    # Grab our test name and file from our id->name and id->file maps
    name=${nameMap[$id]}
    file=${fileMap[$id]}

    # Grab the result field
    resultRegex='\"result\":\"([A-Za-z]+)\",'
    [[ $line =~ $resultRegex ]]
    result=${BASH_REMATCH[1]}
    
    # Grab the hidden field
    hiddenRegex='\"hidden\":(true|false),'
    [[ $line =~ $hiddenRegex ]]
    hidden=${BASH_REMATCH[1]}
    
    # Skip hidden tests
    if [ $hidden = "false" ]
    then
      # On a fail, send a message to splunk
      if [ $result = "error" ]
      then
        failureMessage=${messageMap[$id]}
        echo -e "\n\ntest FAILED: $file \"$name\"\n\n"
        echo -e "\nfailureMessage: $failureMessage"
        failedTest="$file - \"$name\"\n"
        failures=("${failures[@]}" $failedTest)
        # Emit summary payload message to Splunk if we are on bitrise
        if [ -n "$SPLUNK_MOBILE_TOKEN" ]
        then
          # Put failureMessage last because it may overflow.
          payload="{\"sourcetype\" : \"mobile-android-qa-testresult\", \"event\" : {\"buildUrl\" : \"$BITRISE_BUILD_URL\", \"status\" : \"failed\", \"testName\": \"$name\", \"testClass\" : \"$file\", $commonSplunkData, \"message\":\"$failureMessage\"}}"
          #echo error payload: \"$payload\"
          curl -k "https://http-inputs-inst.splunkcloud.com:443/services/collector" -H "Authorization: Splunk $SPLUNK_MOBILE_TOKEN" -d "$payload"
        fi
        ((failureCount=failureCount+1))
      else
        ((successCount=successCount+1))
      fi
    fi

  fi

  # Capture end-of-run event
  # Sample line:
  # {"success":false,"type":"done","time":114261}
  if [[ $line =~ "\"success\":" ]]
  then
    # Capture success field
    successRegex='\"success\":(true|false),'
    [[ $line =~ $successRegex ]]
    success=${BASH_REMATCH[1]}

    # Capture time field
    timeRegex='\"time\":([0-9]+)'
    [[ $line =~ $timeRegex ]]
    msTime=${BASH_REMATCH[1]}
    
    ((totalCount=successCount+failureCount))
    echo -e "\n"
    echo $successCount of $totalCount tests passed
    echo success: $success, time: $msTime
    if [ $failureCount -ne 0 ]
    then
      echo -e "Failed tests:\n ${failures[@]}"
    fi

    # Emit summary payload message to Splunk if we are on bitrise
    if [ -n "$SPLUNK_MOBILE_TOKEN" ]
    then
      payload="{\"numTests\" : $totalCount, \"numFailures\" : $failureCount, \"runTime\" : $msTime, $commonSplunkData}"
      curl -k "https://http-inputs-inst.splunkcloud.com:443/services/collector" -H "Authorization: Splunk $SPLUNK_MOBILE_TOKEN" -d "{\"sourcetype\" : \"mobile-android-qa-summary\", \"event\" : $payload}"
    fi

  fi
# Reads from either provided file or from stdin.
done < "${1:-/dev/stdin}"
