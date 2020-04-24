
successCount=0
failureCount=0
commonSplunkData="\"workflow\" : \"$BITRISE_TRIGGERED_WORKFLOW_ID\", \"app\" : \"flutter-parent\", \"branch\" : \"$BITRISE_GIT_BRANCH\""

echo file is $1
while IFS= read -r line
do
  if [[ $line =~ "\"test\":" ]]
  then
    idRegex='\"id\":([0-9]+),'
    [[ $line =~ $idRegex ]]
    id=${BASH_REMATCH[1]}

    nameRegex='\"name\":\"([A-Za-z0-9 -\(\)/\._]+)\",\"suiteID\"'
    [[ $line =~ $nameRegex ]]
    name=${BASH_REMATCH[1]}

    #urlRegex='\"url\":\"([A-Za-z0-9_:/\-\,]+)\"'
    urlRegex='\"root_url\":\"([A-Za-z0-9 -:\(\)/\._]+)\"'
    [[ $line =~ $urlRegex ]]
    url=${BASH_REMATCH[1]}
    file=`echo $url | rev | cut -d "/" -f 1 | rev`

    #echo id=$id, name=$name, url=$url, file=$file
    nameMap[$id]=$name
    fileMap[$id]=$file

  fi

  if [[ $line =~ "\"result\":" ]]
  then
    idRegex='\"testID\":([0-9]+),'
    [[ $line =~ $idRegex ]]
    id=${BASH_REMATCH[1]}

    name=${nameMap[$id]}
    file=${fileMap[$id]}

    resultRegex='\"result\":\"([A-Za-z]+)\",'
    [[ $line =~ $resultRegex ]]
    result=${BASH_REMATCH[1]}
    
    if [ $result = "error" ]
    then
      echo -e "\ntest FAILED: $file \"$name\"\n"
      # Emit summary payload message to Splunk if we are on bitrise
      if [ -n "$SPLUNK_MOBILE_TOKEN" ]
      then
        payload="{\"sourcetype\" : \"mobile-android-qa-testresult\", \"event\" : {\"buildUrl\" : \"$BITRISE_BUILD_URL\", \"status\" : \"failed\", \"testName\": \"$name\", \"testClass\" : \"$file\", $commonSplunkData}}"
        echo error payload: \"$payload\"
        curl -k "https://http-inputs-inst.splunkcloud.com:443/services/collector" -H "Authorization: Splunk $SPLUNK_MOBILE_TOKEN" -d "$payload"
      fi
      ((failureCount=failureCount+1))
    else
      ((successCount=successCount+1))
    fi

    #echo id=$id, result=$result, name=$name, file=$file

  fi

  if [[ $line =~ "\"success\":" ]]
  then
    successRegex='\"success\":(true|false),'
    [[ $line =~ $successRegex ]]
    success=${BASH_REMATCH[1]}

    timeRegex='\"time\":([0-9]+)'
    [[ $line =~ $timeRegex ]]
    msTime=${BASH_REMATCH[1]}
    
    ((totalCount=successCount+failureCount))
    echo $successCount of $totalCount tests passed
    echo success: $success, time: $msTime

    # Emit summary payload message to Splunk if we are on bitrise
    if [ -n "$SPLUNK_MOBILE_TOKEN" ]
    then
      payload="{\"numTests\" : $totalCount, \"numFailures\" : $failureCount, \"runTime\" : $msTime, $commonSplunkData}"
      curl -k "https://http-inputs-inst.splunkcloud.com:443/services/collector" -H "Authorization: Splunk $SPLUNK_MOBILE_TOKEN" -d "{\"sourcetype\" : \"mobile-android-qa-summary\", \"event\" : $payload}"
    fi

  fi
#done < $1
done < "${1:-/dev/stdin}"
