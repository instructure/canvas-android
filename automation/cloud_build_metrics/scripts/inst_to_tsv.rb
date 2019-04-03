# Converts Firebase Test Lab instrumentation.results
# to Tab Separated Value for Google Sheets

class ParseInstrumentation
  def initialize

    instrumentation_path = 'instrumentation.results.txt'
    raise "File doesn't exist #{instrumentation_path}" unless File.exist?(instrumentation_path)

    @data   = File.read instrumentation_path
    @output = StringIO.new
  end

  def reset
    @test  = nil
    @class = nil
    @stack = nil
  end

  def run
    @data.each_line do |line|

      case line
      when /^INSTRUMENTATION_STATUS: test=(.+)/
        @test = $1
      when /^INSTRUMENTATION_STATUS: class=(.+)/
        @class = $1
      when /^INSTRUMENTATION_STATUS: stack=(.+)/
        @stack = $1
      end

      if @test && @class && @stack
        @output.puts [@class, @test, @stack].join("\t")
        reset
      end
    end

    File.write('xml.csv', @output.string)
  end
end


ParseInstrumentation.new.run



=begin

start test:
INSTRUMENTATION_STATUS: numtests=154
INSTRUMENTATION_STATUS: stream=
com.instructure.teacher.ui.AddMessagePageTest:
INSTRUMENTATION_STATUS: id=AndroidJUnitRunner
INSTRUMENTATION_STATUS: test=addReply
INSTRUMENTATION_STATUS: class=com.instructure.teacher.ui.AddMessagePageTest
INSTRUMENTATION_STATUS: current=1
INSTRUMENTATION_STATUS_CODE: 1

stop test:
INSTRUMENTATION_STATUS: numtests=154
INSTRUMENTATION_STATUS: stream=.
INSTRUMENTATION_STATUS: id=AndroidJUnitRunner
INSTRUMENTATION_STATUS: test=addReply
INSTRUMENTATION_STATUS: class=com.instructure.teacher.ui.AddMessagePageTest
INSTRUMENTATION_STATUS: current=1
INSTRUMENTATION_STATUS_CODE: 0

error test:
INSTRUMENTATION_STATUS: numtests=154
INSTRUMENTATION_STATUS: stream=
<snip>
INSTRUMENTATION_STATUS: id=AndroidJUnitRunner
INSTRUMENTATION_STATUS: test=filterPendingReviewSubmissions
INSTRUMENTATION_STATUS: class=com.instructure.teacher.ui.QuizSubmissionListPageTest
INSTRUMENTATION_STATUS: stack=android.support.test.espresso.NoMatchingViewException: No views in hierarchy found matching: with string from resource id: <2131755508>[havent_been_graded] value: Haven't Been Graded
<snip>
INSTRUMENTATION_STATUS: current=117
INSTRUMENTATION_STATUS_CODE: -2

=end