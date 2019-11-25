# It appears that there is no way to just tell "flutter drive" to run all of the tests.
# You have to specify a target.
# This script will find all *_test.dart files in test_driver, and will strip the "_test"
# out of each for a proper target file.
#
# Note that if we started having multiple driver files (e.g., the ones with "_test" in them),
# we might need to tweak this script.  It assumes a 1-1 correspondence between driver files
# and target files.

for driver in test_driver/*_test.dart
do
	echo "driver = $driver"

	target=${driver/_test}
	echo "target = $target"
	
	flutter drive --target=$target
done

