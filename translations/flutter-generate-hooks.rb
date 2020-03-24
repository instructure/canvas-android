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
puts 'Linking in flutter translations'
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
    project_dir = File.join(flutter_dir)
    Dir.chdir project_dir

    puts "Generating flutter files for #{project_name}"
    success = system("flutter pub run intl_translation:generate_from_arb --output-dir=lib/l10n/generated --no-use-deferred-loading lib/l10n/app_localizations.dart lib/l10n/res/intl_*.arb")
    failed_projects << project_name unless success

    # Return to the home directory for the next project and to commit
    Dir.chdir home_dir
end

failure = ""
commit_message = 'git commit -m "Generate Flutter translation hooks"'
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
branch_name = "translations/flutter-#{Time.now.strftime('%Y-%m-%d')}"
system("git checkout -b #{branch_name}")
system('git add .')
system(commit_message)

# Push to remote
success = system('git push origin HEAD')
raise 'Failed to push new git branch' unless success

# Create pull request
success = system('hub pull-request -b master -m "Update translations"')
raise 'Failed to create pull request' unless success

puts 'Translations successfully imported!'
puts 'PLEASE REVIEW THE PULL REQUEST'
