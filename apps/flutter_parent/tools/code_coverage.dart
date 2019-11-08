import 'dart:io';

void main(List<String> args) async {
  // Make sure lcov is installed
  try {
    Process.runSync('lcov', []);
  } catch (e) {
    print("lcov is not installed! Please run 'brew install lcov' first.");
    exit(1);
  }

  // Get list of exclusions
  File file = new File('tools/codecov_exclusions');
  if (!file.existsSync()) {
    print("Could not find exclusions list at ${file.absolute.path}");
    exit(1);
  }
  List<String> exclusions = file.readAsLinesSync().map((line) => line.trim()).toList();
  exclusions.retainWhere((line) => line.isNotEmpty && !line.startsWith('#'));

  // Run tests with coverage
  print("Running tests...");
  await runCommand("flutter", ['test', '--coverage']);

  // Perform exclusions
  print("Performing coverage exclusions...");
  await runCommand('lcov', ['--remove', 'coverage/lcov.info', ...exclusions, '-o', 'coverage/lcov_filtered.info']);

  // Generate HTML coverage report
  print("Generating HTML report");
  await runCommand('genhtml', [
    'coverage/lcov_filtered.info',
    '-o',
    'coverage',
  ]);

  // Open the report
  if (!args.contains('--dont-open')) {
    print("Opening report");
    await runCommand('open', ['coverage/index.html']);
  }
}

Future<int> runCommand(String command, List<String> args) async {
  var process = await Process.start(command, args);
  stdout.addStream(process.stdout);
  stderr.addStream(process.stderr);
  var exitCode = await process.exitCode;
  if (exitCode != 0) {
    print("Command '$command' failed with exit code $exitCode");
    exit(1);
  }
  return exitCode;
}
