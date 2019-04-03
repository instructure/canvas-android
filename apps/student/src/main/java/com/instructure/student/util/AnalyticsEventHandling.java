package com.instructure.student.util;

public interface AnalyticsEventHandling {
    void trackButtonPressed(String buttonName, Long buttonValue);
    void trackScreen(String screenName);
    void trackEnrollment(String enrollmentType);
    void trackDomain(String domain);
    void trackEvent(String category, String action, String label, long value);
    void trackUIEvent(String action, String label, long value);
    void trackTiming(String category, String name, String label, long duration);
}
