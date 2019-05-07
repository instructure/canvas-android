require 'fileutils'
require 'json'
require 'mkmf'

projects_json = File.join('translations', 'projects.json')

s3_dest = 's3://instructure-translations/sources/android-canvas/en/'

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

# Create the export directory
export_dir = File.join(temp_dir, 'export')
FileUtils.mkdir_p export_dir

puts 'Gathering source strings'
json = IO.read(projects_json, encoding: 'utf-8')
projects = JSON.parse json

projects.each do |project|
  project_name = project.fetch('name')
  source_path = project.fetch('source_path')

  # Validate source file exists
  unless File.file?(source_path)
    raise "Invalid source file #{source_path} for project '#{project_name}'"
  end

  # Copy source to export directory
  destination = File.join(export_dir, "#{project_name}.xml")
  FileUtils.cp source_path, destination
  puts "#{source_path} => #{project_name}.xml"
end

puts 'Exporting source strings to S3 bucket'
command = %(aws s3 sync "#{export_dir}" "#{s3_dest}" --delete)
success = system(command)
raise 'Failed to export strings to S3' unless success
puts 'Strings successfully exported for translation!'
puts 'PLEASE NOTIFY THE TRANSLATION PROJECT MANAGER'
