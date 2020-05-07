require 'fileutils'
require 'json'
require 'mkmf'

hub_config = File.join(Dir.home, '.config', 'hub')

# Hub CLI and valid config are required for creating Pull Requests
raise 'Missing Hub CLI' if find_executable('hub').nil?
unless File.exist?(hub_config) || !ENV['GITHUB_TOKEN'].nil?
  raise 'Must specify GITHUB_TOKEN or place Hub config at ~/.config/hub'
end

# File setup
flutter_dir = File.join('apps', 'flutter_student_embed')
Dir.chdir flutter_dir

l10n_dir = File.join('lib', 'l10n')
l10n_input = File.join(l10n_dir, 'app_localizations.dart')
l10n_output = File.join(l10n_dir, 'res')

# Localization file is required
unless File.exist? l10n_input
  raise "Missing #{l10n_input}; please run again from repository root"
end

# Get dependencies so we can run the command correctly
system("flutter pub get")

# Extract the strings running the flutter command
puts 'Running extract_to_arb'
system("flutter pub run intl_translation:extract_to_arb --output-dir=#{l10n_output} #{l10n_input}")

# Verify the strings got extracted
messages = File.join(l10n_output, 'intl_messages.arb')
unless File.exist? messages
  raise "Missing #{messages}; pleases verify the extraction was run"
end

# Copy over the generated intl_messages.arb into intl_en.arb so we have the english translations
destination = File.join(l10n_output, 'intl_en.arb')
FileUtils.cp messages, destination

# Create branch, add, and commit
branch_name = "translations/extract-#{Time.now.strftime('%Y-%m-%d')}"
system("git checkout -b #{branch_name}")
system('git add .')
system('git commit -m "Extract translations"')

# Push to remote
success = system('git push origin HEAD')
raise 'Failed to push new git branch' unless success

# Create pull request
success = system('hub pull-request -b master -m "Extract translations"')
raise 'Failed to create pull request' unless success

puts 'Translations successfully imported!'
puts 'PLEASE REVIEW THE PULL REQUEST'
