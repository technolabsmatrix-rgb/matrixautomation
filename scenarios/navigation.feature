Feature: Navigation Menu Verification
  As a user visiting matrixtechnolabs.in
  I want all navigation menu items to be present and functional
  So that I can browse through all sections of the website

  Background:
    Given browser is launched and maximized
    And user navigates to Matrix Technolabs homepage

  @Navigation @Smoke
  Scenario: Verify navigation menu is displayed on homepage
    Given user navigates to Matrix Technolabs homepage
    Then navigation menu should be displayed

  @Navigation @Smoke
  Scenario: Verify all menu items are present
    Given user navigates to Matrix Technolabs homepage
    Then Home menu link should be present
    And About menu link should be present
    And Services menu link should be present
    And Contact menu link should be present

  @Navigation @Regression
  Scenario: Navigate to About section via menu
    Given user navigates to Matrix Technolabs homepage
    When user clicks on About menu item
    Then page should contain 'About' in title or URL

  @Navigation @Regression
  Scenario: Navigate to Services section via menu
    Given user navigates to Matrix Technolabs homepage
    When user clicks on Services menu item
    Then page should contain 'Services' in title or URL

  @Navigation @Regression
  Scenario: Navigate to Contact section via menu
    Given user navigates to Matrix Technolabs homepage
    When user clicks on Contact menu item
    Then page should contain 'Contact' in title or URL
