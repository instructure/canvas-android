#!/bin/bash

room="#mobile"
add_commit_info=1
message="Succeeded"

git fetch origin $GERRIT_REFSPEC
USERNAME=$(git log -1 --pretty=format:%aE $GERRIT_PATCHSET_REVISION | cut -d@ -f1)
SUBJECT=$(git log -1 --pretty=format:%s $GERRIT_PATCHSET_REVISION)

build_status_file="/tmp/$BUILD_TAG.status"
if [ -f $build_status_file ]; then
  echo "status file exists"
else
  echo "build:1" > $build_status_file
fi

prefix=""
if [ "$JOB_NAME" == "android-speedgrader" ]; then
  prefix=" Android SpeedGrader"
elif [ "$JOB_NAME" == "android-speedgrader" ]; then
  prefix=" Android SpeedGrader"
  room="#mobile"
  add_commit_info=0
else
  prefix=" $JOB_NAME"
fi

failed_step=0

# "What am I reading from?", you ask? See the end of the while loop.
while read step; do 

    echo "parsing step results: $step"
    if [ "$step" == "1" ]; then
        step_name="build"
        step_status="1"
    else
        # format is step_name:step_status
        step_name=${step%%:*}
        step_status=${step#*:}
    fi
    
    echo $step_name
    echo $step_status

    if [ "$step_status" != "0" ]; then
        failed_step=$step_name
        break
    fi
done < $build_status_file
# We read the input from a file instead of piping it in so
# that `cat #build_status_file | while …` doesn't create
# a sub-shell and keep our `$failed_step` from working.
#
# Bash is so ugly.

function notify {
  if [ "$1" == "Succeeded" ]; then
    step_name=""
  else
    message=$1
  fi
  curl -F "room=$room" \
       -F "$2" \
       -F 'token=ehvU5GqqmVFj' https://irc.instructure.com/message
}

function send_info {
  build_status=$1
  if [ "$add_commit_info" == "1" ]; then
    notify "$build_status" "msg=[[15]] ● [[]]$prefix ($step_name) $build_status[[15]] ● [[2]]$USERNAME [[15]] ● [[14]]$SUBJECT [[15]] ● [[6]]$BUILD_URL [[15]]●"
  else
    notify "$build_status" "msg=[[15]] ● [[]]$prefix ($step_name) $build_status[[15]] ● [[6]]$BUILD_URL [[15]]●"
  fi
}

if [ $failed_step == "0" ]; then
  echo "Sending success message."
  send_info "[[3]]Succeeded"
else
  echo "Sending failure message."
  send_info "[[4]]FAILED"
fi
