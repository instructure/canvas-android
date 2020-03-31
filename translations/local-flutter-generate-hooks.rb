require 'fileutils'
require 'json'
require 'mkmf'

projects_json = File.join('translations', 'projects.json')

# Projects json file is required
unless File.exist? projects_json
  raise 'Missing projects.json; please run again from repository root'
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
if !failed_projects.empty?
  # Prepend a '-m' here to add another paragraph to our commit message
  failure = "Failed projects:\n"
  failed_projects.each {|project| failure += "- #{project}\n"}
  
  # Print to the console and make it clear thehre was an error
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
  puts failure
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
  puts "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
else
  puts "Finished hooking in Flutter translations with no errors!"
end
