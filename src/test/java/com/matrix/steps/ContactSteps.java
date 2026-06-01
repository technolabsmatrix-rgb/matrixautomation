package com.matrix.steps;

import com.matrix.pages.ContactPage;
import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;

public class ContactSteps extends WebDriverTestBase {

    private ContactPage contactPage = new ContactPage();

    @QAFTestStep(description = "contact form should be displayed")
    public void contactFormShouldBeDisplayed() {
        contactPage.getContactForm().verifyPresent();
    }

    @QAFTestStep(description = "user fills contact form with name {0} email {1} phone {2} and message {3}")
    public void fillContactForm(String name, String email, String phone, String message) {
        contactPage.enterName(name);
        contactPage.enterEmail(email);
        contactPage.enterPhone(phone);
        contactPage.enterMessage(message);
    }

    @QAFTestStep(description = "user submits the contact form")
    public void submitContactForm() {
        contactPage.clickSubmit();
    }

    @QAFTestStep(description = "user submits the contact form without filling any fields")
    public void submitContactFormEmpty() {
        contactPage.clickSubmit();
    }

    @QAFTestStep(description = "success message should be displayed")
    public void successMessageShouldBeDisplayed() {
        contactPage.getSuccessMessage().verifyPresent();
    }

    @QAFTestStep(description = "error or validation message should be displayed")
    public void errorMessageShouldBeDisplayed() {
        contactPage.getErrorMessage().verifyPresent();
    }

    @QAFTestStep(description = "user clicks the hero CTA button")
    public void clickHeroCtaButton() {
        // delegated to navigation — clicking a CTA that scrolls or redirects
        getDriver().findElement(
            org.openqa.selenium.By.xpath(
                "//a[contains(text(),'Get Started') or contains(text(),'Contact Us') or contains(text(),'Let')]"
            )
        ).click();
    }
}
