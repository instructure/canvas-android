require 'fileutils'
require 'json'
require 'mkmf'

projects_json = File.join('translations', 'projects.json')

s3_source = 's3://instructure-translations/translations/android-canvas/'

# Projects json file is required
unless File.exist? projects_json
  raise 'Missing projects.json; please run again from repository root'
end

# AWS CLI and credentials are required for accessing the S3 bucket
raise 'Missing AWS CLI' if find_executable('aws').nil?
raise 'Missing AWS access key ID' if ENV['AWS_ACCESS_KEY_ID'].nil?
raise 'Missing AWS secret access key' if ENV['AWS_SECRET_ACCESS_KEY'].nil?

# Clean out the temp directory
temp_dir = File.join('translations', 'tmp')
FileUtils.remove_entry_secure(temp_dir) if File.exist? temp_dir

# Create the import directory
import_dir = File.join(temp_dir, 'import')
FileUtils.mkdir_p import_dir

# Import translations from S3
puts 'Importing translations from S3 bucket'
sync_command = %(aws s3 sync "#{s3_source}" "#{import_dir}")
success = system(sync_command)
raise 'Failed to import translations from S3' unless success

# Parse destination resource directories
resource_dirs = {}
json = IO.read(projects_json, encoding: 'utf-8')
projects = JSON.parse json
projects.each do |project|
  project_name = project.fetch('name')
  resource_dir = project.fetch('resource_dir')
  FileUtils.mkdir_p resource_dir
  resource_dirs[project_name] = resource_dir
end

# Move translated files to correct location
puts 'Organizing translations'
Dir.glob("#{import_dir}/*/") do |src_dir|
  next unless File.directory? src_dir
  Dir.glob("#{src_dir}/*.{xml,arb}") do |file|
    language = File.basename(src_dir).gsub('_', '-')
    if language.include? '-x-'
      # BCP 47 private-use subtag. Convert to new Android format and
      # prepend subtag with 'inst'
      # if prepended subtag becomes greater than 8 characters, remove inst
      language = language
        .sub('-x-', "-inst")
        .sub(/\-inst(\w{5,})/, '-\1')

        # Android specific naming convention
        if file.end_with?('.xml')
            language = language.gsub('-', '+').prepend('b+')
        end
    elsif file.end_with?('.xml')
      language = language.gsub('-', '-r') # Android specific naming convention
    end
    if language.eql? 'en'
      puts "Skipping redundant 'en' resource"
      next
    end
    project = File.basename(file, '.*')
    res_dir = resource_dirs[project]
    if res_dir.nil?
      puts "Skipping #{language} translation for untracked project #{project}"
      next
    end

    # Preserve arb file extensions (flutter also names resources slightly different)
    if file.end_with?('.arb')
      language = language.gsub('-', '_') # Flutter specific naming convention
      destination = File.join(res_dir, "intl_#{language}.arb")
    else
      destination = File.join(res_dir, "values-#{language}", 'strings.xml')
    end
    puts "#{language}: Importing #{project} strings to #{destination}"
    FileUtils.mkdir_p File.dirname(destination)
    FileUtils.mv file, destination
  end
end

puts 'Translations successfully imported!'
puts '' # New line for slightly better formatting of flutter message
puts "!!!!!!!!!!!!\nFlutter translations still need to have language hooks generated. Consider running `ruby translations/local-flutter-generate-hooks.rb`\n!!!!!!!!!!!!"
