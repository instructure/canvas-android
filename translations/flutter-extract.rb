require 'fileutils'
require 'json'
require 'mkmf'

projects_json = File.join('translations', 'projects.json')

hub_config = File.join(Dir.home, '.config', 'hub')

# Projects json file is required
unless File.exist? projects_json
  raise 'Missing projects.json; please run again from repository root'
end

# Hub CLI and valid config are required for creating Pull Requests
raise 'Missing Hub CLI' if find_executable('hub').nil?
unless File.exist?(hub_config) || !ENV['GITHUB_TOKEN'].nil?
  raise 'Must specify GITHUB_TOKEN or place Hub config at ~/.config/hub'
end

# Ensure a clean git status before we begin
unless `git status --porcelain`.eql? ''
  raise 'Please commit or remove local changes before proceeding.'
end

# Generate flutter link to translated string files
home_dir = Dir.pwd
puts 'Extracting flutter strings for translators'
json = IO.read(projects_json, encoding: 'utf-8')
projects = JSON.parse json
failed_projects = []
projects.each do |project|
    flutter_dir = project['flutter_dir']
    project_name = project.fetch('name')

    if flutter_dir.nil?
        puts "skipping non flutter project: #{project_name}"
        next
    end

    # Change directories to the flutter project, so we can run the flutter command
    Dir.chdir home_dir # Reset back to home first
    project_dir = File.join(flutter_dir)
    Dir.chdir project_dir

    # Get the input and output locations
    l10n_dir = File.join('lib', 'l10n')
    l10n_input = File.join(l10n_dir, 'app_localizations.dart')
    l10n_output = File.join(l10n_dir, 'res')
    
    # Localization file is required
    unless File.exist? l10n_input
      puts "--No AppLocalizations found at #{l10n_input} for project #{project_name}"
      failed_projects << project_name
      next
    end

    # Make sure the flutter project is up to date
    system("flutter pub get")

    puts "Generating flutter files for #{project_name}"
    system("flutter pub run intl_generator:extract_to_arb --output-dir=#{l10n_output} #{l10n_input}")

    # Verify the strings got extracted
    messages = File.join(l10n_output, 'intl_messages.arb')
    unless File.exist? messages
      puts "--No extracted messages found at #{messages} for project #{project_name}"
      failed_projects << project_name unless File.exist? messages 
      next 
    end

    # Copy over the generated intl_messages.arb into intl_en.arb so we have the english translations
    destination = File.join(l10n_output, 'intl_en.arb')
    FileUtils.cp messages, destination
end

# Return to the home directory to commit
Dir.chdir home_dir

failure = ""
commit_message = 'git commit -m "Extract translations"'
if !failed_projects.empty?
  # Prepend a '-m' here to add another paragraph to our commit message
  failure = "Failed projects:\n"
  failed_projects.each {|project| failure += "- #{project}\n"}
  commit_message += " -m \"#{failure}\""
  
  # Print to the console and make it clear thehre was an error
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
  puts failure
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
end

# Create branch, add, and commit
branch_name = "translations/extract-#{Time.now.strftime('%Y-%m-%d')}"
system("git checkout -b #{branch_name}")
system('git add .')
system(commit_message)

# Push to remote
success = system('git push origin HEAD')
raise 'Failed to push new git branch' unless success

# Create pull request
success = system('hub pull-request -b master -m "Extract translations"')
raise 'Failed to create pull request' unless success

puts 'Translations successfully imported!'
puts 'PLEASE REVIEW THE PULL REQUEST'
