# It appears that there is no way to just tell "flutter drive" to run all of the tests.
# You have to specify a target.
# This script will find all *_test.dart files in test_driver, and will strip the "_test"
# out of each for a proper target file.
#
# Note that if we started having multiple driver files (e.g., the ones with "_test" in them),
# we might need to tweak this script.  It assumes a 1-1 correspondence between driver files
# and target files.

passed=0
failed=0
failures=()
for driver in test_driver/*_test.dart
do
	echo "Aggregator: driver = $driver"

	target=${driver/_test}
	echo "Aggregator: target = $target"
	
	flutter drive --target=$target
	if [ $? -eq 0 ]
	then
	  echo Aggregator: $driver Passed
	  ((passed=passed+1))
	else
	  echo Aggregator: $driver FAILED
	  ((failed=failed+1))
	  failures=("${failures[@]}" $driver)
	fi
done

if [ $failed -eq 0 ]
then
  echo Aggregator: All tests \($passed\) passed!
else
  echo Aggregator: $failed failing tests: ${failures[@]}
  exit 1
fi

