Feature: Contact Form Submission
  As a potential client visiting matrixtechnolabs.in
  I want to fill and submit the contact form
  So that I can get in touch with Matrix Technolabs

  @Contact @Smoke
  Scenario: Verify contact form is displayed
    Given user navigates to Matrix Technolabs homepage
    When user clicks on Contact menu item
    Then contact form should be displayed

  @Contact @Regression
  Scenario: Submit contact form with valid details
    Given user navigates to Matrix Technolabs homepage
    When user clicks on Contact menu item
    And user fills contact form with name 'John Doe' email 'john@example.com' phone '9876543210' and message 'I am interested in your services'
    And user submits the contact form
    Then success message should be displayed

  @Contact @Regression
  Scenario: Verify contact form validation on empty submission
    Given user navigates to Matrix Technolabs homepage
    When user clicks on Contact menu item
    And user submits the contact form without filling any fields
    Then error or validation message should be displayed

  @Contact @Regression
  Scenario: Submit contact form with invalid email
    Given user navigates to Matrix Technolabs homepage
    When user clicks on Contact menu item
    And user fills contact form with name 'Jane Doe' email 'invalidemail' phone '9876543210' and message 'Test message'
    And user submits the contact form
    Then error or validation message should be displayed
