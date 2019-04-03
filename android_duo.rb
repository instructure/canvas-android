#!/usr/bin/env ruby

# Directions:
# Make sure android-uno is upto date and cleaned of any private data that should exist android-private-data
# Run the script ruby android_duo.rb
# The script will create a directory called android-uno-public in the same directory that the android-uno directory lives.
# The android-uno-public directory is the public version of our code.
# NOTE: Typically unpublished apps should not be included, as of this writing that included Canvas Teacher

require 'fileutils'
require 'date'
require 'find'

puts 'Lets get ready to rumble!!!'

pdf_expires = Date.new(2018, 10, 1)
raise "PSPDFKit trial license has expired. Ping PSPDFKit to renew" unless pdf_expires > Date.today

# Export from master. Remove files not tracked in git.
['git checkout master', 'git clean -dfx', 'git reset --hard'].each do |command|
  raise "command failed: #{command}" unless system(command)
end

def join *args
  args.map!(&:to_s) # File.join doesn't like symbols
  File.join(*args)
end

# FileUtils#rm_r and #rm_rf have a security vulnerability. Use this instead.
# http://ruby-doc.org/stdlib-2.2.2/libdoc/fileutils/rdoc/FileUtils.html#method-c-remove_entry_secure
def rm_rf target
  FileUtils.rm_rf target, secure: true
end

SRC = __dir__
DEST = join(__dir__, 'android-uno-public')

puts 'Removing android-uno-public'
rm_rf DEST

puts 'Making android-uno-public'
FileUtils.mkdir_p DEST

puts 'Making android-uno-public/automation'
FileUtils.mkdir_p join(DEST, 'automation')

def rsync project, new_folder=''
  src = join(SRC, project)
  dest = join(DEST, new_folder)
  raise "src doesn't exist: #{src}" unless File.exist? src
  excludes = %w[build .git .idea].map { |f| "--exclude #{f}" }.join(' ')
  cmd = %Q(rsync -a "#{src}" "#{dest}" #{excludes})
  successful = system cmd
  abort("rsync failed: #{cmd}") unless successful
end

projects = [
    # copy using different name
    ['open_source_data', 'private-data'],
    'androidcalendar',
    'annotations',
    'blueprint',
    'gradle',
    'candroid',
    'canvas-api',
    'canvas-api-2',
    'chips',
    'foosball',
    'login-api',
    'login-api-2',
    'pandautils',
    'parent',
    'polling',
    'rceditor',
    'recyclerview',
    'speedgrader',
    'teacher',
    'tools-teacher',
    'buildSrc',
    'interactions',
    ['automation', 'automation']
]

projects.each_with_index do |project, index|
  if project.is_a?(Array)
    # copy contents of folder with new root name
    # /tmp/a/b => /tmp/c/b
    # rsync -a /tmp/a/. /tmp/c
    source_name = join(project.first, '.')
    dest_name = project.last
    rsync source_name, dest_name
  else
    rsync project
  end

  index += 1
  percent = ((index.to_f / projects.length) * 100).to_i
  prefix = '#' * index
  padding = ' ' * (projects.length + 2 - prefix.length)
  print "\r#{prefix}#{padding}(#{percent}%)"
end

def remove_automation_tools
  %w(tools cloud_build_metrics).each do |tool|
    rm_rf join(DEST, 'automation', tool)
  end
end

def check_for_secrets
  puts 'Checking for secret files'
  secret_file_paths = []
  %w(soseedycli.jar soseedygrpc.jar).each do |secret|
    Find.find(DEST) do |path|
      secret_file_paths << path if path.include? secret
    end
  end

  error = "The following files appear in the open source directory... These cannot be released, ever!\n" + secret_file_paths.join("\n")
  unless secret_file_paths.empty?
    rm_rf DEST
    abort error
  end
end

remove_automation_tools
check_for_secrets

# rewrite pspdfkit dependency for open source
[
  './android-uno-public/buildSrc/src/main/java/GlobalDependencies.kt'
].each do |file|
  raise "Does not exist: #{file}" unless File.exist?(file)
  content = File.read(file).gsub('com.pspdfkit:pspdfkit:', 'com.pspdfkit:pspdfkit-demo:')
  File.write(file, content)
end

# copy open source readme
FileUtils.cp './open_source_readme.md', './android-uno-public/README.md'

puts
puts 'Now officially ready to rumble. Done.'
