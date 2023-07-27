Feature: Verify Dashboard in Canvas Teacher
  As a user
  I want to log in to the Canvas Teacher Mobile Application
  So that I can verify if I am landing on the Dashboard Page.

Scenario: Verifying Dashboard
  Given Seed test data.
  When I login with my only test user.
  Then I should be redirected to the dashboard page

