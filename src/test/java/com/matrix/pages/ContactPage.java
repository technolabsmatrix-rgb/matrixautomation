package com.matrix.pages;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.api.PageLocator;
import com.qmetry.qaf.automation.ui.api.WebDriverTestPage;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;

public class ContactPage extends WebDriverBaseTestPage<WebDriverTestPage> {

    @FindBy(locator = "contact.form.loc")
    private QAFWebElement contactForm;

    @FindBy(locator = "contact.name.field.loc")
    private QAFWebElement nameField;

    @FindBy(locator = "contact.email.field.loc")
    private QAFWebElement emailField;

    @FindBy(locator = "contact.phone.field.loc")
    private QAFWebElement phoneField;

    @FindBy(locator = "contact.message.field.loc")
    private QAFWebElement messageField;

    @FindBy(locator = "contact.subject.field.loc")
    private QAFWebElement subjectField;

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

    public void enterName(String name) {
        nameField.clear();
        nameField.sendKeys(name);
    }

    public void enterEmail(String email) {
        emailField.clear();
        emailField.sendKeys(email);
    }

    public void enterPhone(String phone) {
        phoneField.clear();
        phoneField.sendKeys(phone);
    }

    public void enterMessage(String message) {
        messageField.clear();
        messageField.sendKeys(message);
    }

    public void enterSubject(String subject) {
        if (subjectField.isPresent()) {
            subjectField.clear();
            subjectField.sendKeys(subject);
        }
    }

    public void clickSubmit() {
        submitButton.click();
    }

    public boolean isSuccessMessageDisplayed() {
        return successMessage.isPresent();
    }

    public boolean isErrorMessageDisplayed() {
        return errorMessage.isPresent();
    }

    public void submitContactForm(String name, String email, String phone, String message) {
        enterName(name);
        enterEmail(email);
        enterPhone(phone);
        enterMessage(message);
        clickSubmit();
    }
}
