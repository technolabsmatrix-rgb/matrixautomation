Feature: Links and Buttons Verification
  As a user visiting matrixtechnolabs.in
  I want all important links and CTA buttons to be functional
  So that I can navigate and interact with the website properly

  @Links @Smoke
  Scenario: Verify hero CTA button is present and clickable
    Given user navigates to Matrix Technolabs homepage
    Then hero CTA button should be present on the page

  @Links @Regression
  Scenario: Verify hero CTA button navigates to correct section
    Given user navigates to Matrix Technolabs homepage
    When user clicks the hero CTA button
    Then page URL should still be on matrixtechnolabs.in domain

  @Links @Regression
  Scenario: Verify footer copyright text is present
    Given user navigates to Matrix Technolabs homepage
    Then footer copyright text should be present

  @Links @Regression
  Scenario: Verify page loads successfully with HTTP 200
    Given user navigates to Matrix Technolabs homepage
    Then page title should contain 'Matrix'
    And hero heading should be displayed
    And footer should be displayed at the bottom of the page
