Feature: Homepage Content Verification
  As a user visiting matrixtechnolabs.in
  I want key content elements to be present on the homepage
  So that I can trust the website is fully loaded and functional

  Background:
    Given browser is launched and maximized
    And user navigates to Matrix Technolabs homepage

  @Homepage @Smoke
  Scenario: Verify homepage title contains Matrix Technolabs
    Given user navigates to Matrix Technolabs homepage
    Then page title should contain 'Matrix'

  @Homepage @Smoke
  Scenario: Verify company logo is displayed
    Given user navigates to Matrix Technolabs homepage
    Then company logo should be displayed

  @Homepage @Smoke
  Scenario: Verify hero section heading is visible
    Given user navigates to Matrix Technolabs homepage
    Then hero heading should be displayed

  @Homepage @Regression
  Scenario: Verify services section is present
    Given user navigates to Matrix Technolabs homepage
    Then services section should be present on the page

  @Homepage @Regression
  Scenario: Verify about section is present
    Given user navigates to Matrix Technolabs homepage
    Then about section should be present on the page

  @Homepage @Regression
  Scenario: Verify footer is displayed
    Given user navigates to Matrix Technolabs homepage
    Then footer should be displayed at the bottom of the page
