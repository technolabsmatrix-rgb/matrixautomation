package com.matrix.pages;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.api.PageLocator;
import com.qmetry.qaf.automation.ui.api.WebDriverTestPage;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;
import org.openqa.selenium.JavascriptExecutor;

public class ContactPage extends WebDriverBaseTestPage<WebDriverTestPage> {

    @FindBy(locator = "contact.form.loc")
    private QAFWebElement contactForm;

    @FindBy(locator = "contact.firstname.field.loc")
    private QAFWebElement firstNameField;

    @FindBy(locator = "contact.lastname.field.loc")
    private QAFWebElement lastNameField;

    @FindBy(locator = "contact.email.field.loc")
    private QAFWebElement emailField;

    @FindBy(locator = "contact.company.field.loc")
    private QAFWebElement companyField;

    @FindBy(locator = "contact.service.field.loc")
    private QAFWebElement serviceField;

    @FindBy(locator = "contact.message.field.loc")
    private QAFWebElement messageField;

    @FindBy(locator = "contact.submit.button.loc")
    private QAFWebElement submitButton;

    @FindBy(locator = "contact.success.message.loc")
    private QAFWebElement successMessage;

    @FindBy(locator = "contact.error.message.loc")
    private QAFWebElement errorMessage;

    @Override
    protected void openPage(PageLocator locator, Object... args) {
        new WebDriverTestBase().getDriver().get(locator.getLocator());
    }

    public boolean isContactFormDisplayed() {
        return contactForm.isDisplayed();
    }

    public void enterFirstName(String firstName) {
        firstNameField.waitForPresent();
        firstNameField.clear();
        firstNameField.sendKeys(firstName);
    }

    public void enterLastName(String lastName) {
        lastNameField.waitForPresent();
        lastNameField.clear();
        lastNameField.sendKeys(lastName);
    }

    public void enterEmail(String email) {
        emailField.waitForPresent();
        emailField.clear();
        emailField.sendKeys(email);
    }

    public void enterCompany(String company) {
        companyField.waitForPresent();
        companyField.clear();
        companyField.sendKeys(company);
    }

    public void enterMessage(String message) {
        messageField.waitForPresent();
        messageField.clear();
        messageField.sendKeys(message);
    }

    public void clickSubmit() {
        submitButton.waitForPresent();
        JavascriptExecutor js = (JavascriptExecutor) new WebDriverTestBase().getDriver();
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", submitButton);
        js.executeScript("arguments[0].click();", submitButton);
    }

    public boolean isSuccessMessageDisplayed() {
        return successMessage.isPresent();
    }

    public boolean isErrorMessageDisplayed() {
        return errorMessage.isPresent();
    }

    public void submitContactForm(String firstName, String lastName, String email, String message) {
        enterFirstName(firstName);
        enterLastName(lastName);
        enterEmail(email);
        enterMessage(message);
        clickSubmit();
    }

    public QAFWebElement getContactForm() {
        return contactForm;
    }

    public QAFWebElement getFirstNameField() {
        return firstNameField;
    }

    public QAFWebElement getLastNameField() {
        return lastNameField;
    }

    public QAFWebElement getEmailField() {
        return emailField;
    }

    public QAFWebElement getCompanyField() {
        return companyField;
    }

    public QAFWebElement getServiceField() {
        return serviceField;
    }

    public QAFWebElement getMessageField() {
        return messageField;
    }

    public QAFWebElement getSubmitButton() {
        return submitButton;
    }

    public QAFWebElement getSuccessMessage() {
        return successMessage;
    }

    public QAFWebElement getErrorMessage() {
        return errorMessage;
    }
}
