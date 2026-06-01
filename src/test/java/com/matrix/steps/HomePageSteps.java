package com.matrix.steps;

import com.matrix.pages.HomePage;
import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import org.testng.Assert;

public class HomePageSteps extends WebDriverTestBase {

    private HomePage homePage = new HomePage();

    @QAFTestStep(description = "user navigates to Matrix Technolabs homepage")
    public void navigateToHomePage() {
        getDriver().get("https://matrixtechnolabs.in/");
    }

    @QAFTestStep(description = "page title should contain {0}")
    public void pageTitleShouldContain(String text) {
        String title = homePage.getPageTitle();
        Assert.assertTrue(title.contains(text),
            "Expected page title to contain '" + text + "' but was: " + title);
    }

    @QAFTestStep(description = "company logo should be displayed")
    public void logoShouldBeDisplayed() {
        homePage.getLogo().verifyPresent();
    }

    @QAFTestStep(description = "hero heading should be displayed")
    public void heroHeadingShouldBeDisplayed() {
        homePage.getHeroHeading().verifyPresent();
    }

    @QAFTestStep(description = "services section should be present on the page")
    public void servicesSectionShouldBePresent() {
        homePage.getServicesSection().verifyPresent();
    }

    @QAFTestStep(description = "about section should be present on the page")
    public void aboutSectionShouldBePresent() {
        homePage.getAboutSection().verifyPresent();
    }

    @QAFTestStep(description = "footer should be displayed at the bottom of the page")
    public void footerShouldBeDisplayed() {
        homePage.getFooter().verifyPresent();
    }

    @QAFTestStep(description = "footer copyright text should be present")
    public void footerCopyrightShouldBePresent() {
        homePage.getFooterCopyright().verifyPresent();
    }

    @QAFTestStep(description = "hero CTA button should be present on the page")
    public void heroCtaButtonShouldBePresent() {
        homePage.getHeroCtaButton().verifyPresent();
    }
}
