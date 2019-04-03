# Print the currently supported APIs

def grpc_endpoints
  list = []
  grpc_path = File.join(__dir__, 'src/main/proto/*.proto')
  Dir.glob(grpc_path) do |file|
    data = File.read(file)
    list << data.scan(/rpc (.*?)\s*\(/)
  end
  list.flatten
end

def grpc_tests
  tests = []
  test_path = File.join(__dir__, 'src/test/kotlin/com/instructure/dataseeding/soseedy/*.kt')
  Dir.glob(test_path) do |file|
    data = File.read(file)
    results = data.scan(/(@Test)\s*(fun)\s{1}(\S+)\(\)/).flatten
    results.delete('@Test')
    results.delete('fun')
    # Remove substring from underscore onwards
    results.each_with_index {|value, index| results[index] = value.split('_')[0] }
    # Use only unique lowercase values
    tests << results.uniq.map(&:downcase)
  end
  tests.flatten
end

def rest_endpoints
  list = []
  api_path = File.join(__dir__, 'src/main/kotlin/com/instructure/dataseeding/api/*.kt')
  Dir.glob(api_path) do |file|
    data = File.read(file)
    results = data.scan(/^\s*(@.*\))$/).flatten
    list << results.reject {|e| e.include?('SerializedName')}
  end
  list.flatten
end

def get_list_item_substring(collection)
  output = ''
  if collection.nil? || collection.empty?
    output = '<li>None</li>'
  else
    collection.each do |endpoint|
      output.concat "<li>#{endpoint}</li>"
    end
  end
  output
end

def output_file_path
  File.join(__dir__, 'coverage_report.html')
end

def generate_report
  grpcs = grpc_endpoints
  tests = grpc_tests
  covered_endpoints = []
  not_covered_endpoints = []
  grpcs.each do |endpoint|
    if tests.include?(endpoint.downcase)
      covered_endpoints << endpoint
    else
      not_covered_endpoints << endpoint
    end
  end

  percentage = ((covered_endpoints.length.to_f / (grpcs.length.to_f)) * 100.0).round(2)
  content = "
    <html>
      <head>
          <title>DataSeedingAPI Code Coverage</title>
      </head>
      <body>
          <h1>Data Seeding API Coverage Report</h1>
          <div>
              <h2>Stats</h2>
              <ul>
                  <li>Total API endpoints: #{grpcs.length}</li>
                  <li>Total Tests: #{tests.length}</li>
                  <li>Total Covered Endpoints: #{covered_endpoints.length}</li>
                  <li>Total Uncovered Endpoints: #{not_covered_endpoints.length}</li>
                  <li>Percentage Covered: #{percentage}%</li>
              </ul>
          </div>
          <div>
              <h2>Tested gRPC APIs</h2>
              <ul>
                  #{get_list_item_substring(covered_endpoints)}
              </ul>
          </div>
          <div>
              <h2>Untested gRPC APIs</h2>
              <ul>
                  #{get_list_item_substring(not_covered_endpoints)}
              </ul>
          </div>
          <div>
              <h2>Supported Canvas REST APIs</h2>
              <ul>
                  #{get_list_item_substring(rest_endpoints)}
              </ul>
          </div>
      </body>
    </html>"

  File.write(output_file_path, content)
end

generate_report
puts "Generated HTML report: #{output_file_path}"
