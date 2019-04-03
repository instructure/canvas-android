COURSE_NAME = 'droid testing course'
PASSWORD = 'password'
CONSTANT_CONFIG_FILE = '../test/src/com/instructure/candroid/test/utils/TestingConstants.java'
DEFAULT_ACCOUNT = Account.default
@details = {}
@url = ARGV[0] 
@url = "10.0.2.2:3000" if @url == nil

def clean_up
  Course.destroy_all
  User.destroy_all
  DEFAULT_ACCOUNT.allowed_services = '+avatars'
  DEFAULT_ACCOUNT.save!
end

def course_setup
  email = 'droidstudent@example.com'
  courses = []
  assignments = []

  user = User.create!(:name => email)
  user.register!
  user.pseudonyms.create!(:unique_id => email, :password => PASSWORD, :password_confirmation => PASSWORD, :account => DEFAULT_ACCOUNT)
  @details.merge!(:user => user)
  @details.merge!(:access_token => AccessToken.create!(:user => user, :purpose => 'candroid testing', :developer_key => DeveloperKey.default))

  2.times do |i|
    course = Course.create!(:name => COURSE_NAME)
    course.offer!
    course.enroll_user(user, 'StudentEnrollment').accept!
    assignment = course.assignments.create!(:name => 'droid assignment', :points_possible => 10, :submission_types => 'online_url')
    assignment.submit_homework(user)
    assignment.grade_student(user, :grade => "10")
    assignments << assignment
    courses << course
  end

  @details.merge!(:course => courses.first)
  @details.merge!(:course_2 => courses.last)
  @details.merge!(:assignment => assignments.first)
  @details.merge!(:assignment_2 => assignments.last)
end

def discussion_and_conversation_setup
  conversations = []

  2.times do |i|
    conversation = Conversation.initiate([@details[:user]], false)
    conversation_message = conversation.add_message(@details[:user], 'test')
    conversations << conversation
  end
  @details.merge!(:conversations => conversations)
  @details.merge!(:topic => @details[:course].discussion_topics.create!)
  @details.merge!(:entry => @details[:topic].reply_from(:user => @details[:user], :text => 'i am a figment of your imagination'))
end

def write_configs
  test_configs = <<-CONSTANTS
    package com.instructure.candroid.test.utils;
    public class TestingConstants
    {
           public static String TAG = "candroid-test";

           public static String domain = "#{@url}/";
           public static String invalidDomain = "INVALID_DOMAIN/";

           public static String token = "#{@details[:access_token].full_token}";
           public static long userID = #{@details[:user].id};

           public static long courseId = #{@details[:course].id};
           public static long assignmentId = #{@details[:assignment].id};

           public static long firstConversationId = #{@details[:conversations].first.id};
           public static long secondConversationId = #{@details[:conversations].last.id};

           public static long topicId = #{@details[:topic].id};
           public static long entryId = #{@details[:entry].id};
    }
  CONSTANTS

  File.open(CONSTANT_CONFIG_FILE, 'w+') { |f| f.write(test_configs) }
end

def seed
  clean_up
  course_setup
  discussion_and_conversation_setup
  write_configs
  puts "candroid/test/src/com/instructure/candroid/test/utils/TestingConstants.java was overwritten with seed script values"
end

seed

