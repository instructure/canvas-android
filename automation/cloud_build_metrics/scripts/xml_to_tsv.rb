# Converts Firebase Test Lab JUnit XML result
# to Tab Separated Value for Google Sheets

require 'nokogiri'

xml = '/path/to/2018-01-08_16_44_26.055529_qmFt%2FNexusLowRes-26-en-portrait%2Ftest_result_0.xml'
raise "XML path doesn't exist #{xml}" unless File.exist?(xml)

xml = Nokogiri::XML(File.read(xml))
output = StringIO.new

xml.css('testcase').each do |test|

  failure = test.css('failure')

  unless failure&.text&.empty?
    error = failure.text.split("\n").first
    name = test.attr('name')
    klass = test.attr('classname')

    output.puts [klass, name, error].join("\t")
  end
end

File.write('xml.csv', output.string)
