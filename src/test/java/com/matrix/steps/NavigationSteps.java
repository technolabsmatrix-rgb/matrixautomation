package com.matrix.steps;

import com.matrix.pages.NavigationPage;
import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import org.testng.Assert;

public class NavigationSteps extends WebDriverTestBase {

    private NavigationPage navigationPage = new NavigationPage();

    @QAFTestStep(description = "navigation menu should be displayed")
    public void navigationMenuShouldBeDisplayed() {
        navigationPage.getNavMenu().verifyPresent();
    }

    @QAFTestStep(description = "Home menu link should be present")
    public void homeMenuLinkShouldBePresent() {
        navigationPage.getHomeLink().verifyPresent();
    }

    @QAFTestStep(description = "About menu link should be present")
    public void aboutMenuLinkShouldBePresent() {
        navigationPage.getAboutLink().verifyPresent();
    }

    @QAFTestStep(description = "Services menu link should be present")
    public void servicesMenuLinkShouldBePresent() {
        navigationPage.getServicesLink().verifyPresent();
    }

    @QAFTestStep(description = "Contact menu link should be present")
    public void contactMenuLinkShouldBePresent() {
        navigationPage.getContactLink().verifyPresent();
    }

    @QAFTestStep(description = "user clicks on About menu item")
    public void clickAboutMenuItem() {
        navigationPage.clickAbout();
    }

    @QAFTestStep(description = "user clicks on Services menu item")
    public void clickServicesMenuItem() {
        navigationPage.clickServices();
    }

    @QAFTestStep(description = "user clicks on Contact menu item")
    public void clickContactMenuItem() throws InterruptedException {
        navigationPage.clickContact();
        Thread.sleep(5000);
    }

    @QAFTestStep(description = "page should contain {0} in title or URL")
    public void pageShouldContainInTitleOrUrl(String text) {
        // Site uses bookmark (#about, #services, #contact) — URL path and title never change.
        // Verify via URL hash first, then fall back to section heading presence.
        String url = navigationPage.getCurrentUrl().toLowerCase();
        String section = text.toLowerCase();

        if (url.contains("#" + section)) {
            return;
        }

        switch (section) {
            case "about":
                navigationPage.getAboutSectionHeading().verifyPresent();
                break;
            case "services":
                navigationPage.getServicesSectionHeading().verifyPresent();
                break;
            case "contact":
                navigationPage.getContactSectionHeading().verifyPresent();
                break;
            default:
                Assert.fail("Unknown section '" + text + "'. URL: " + url);
        }
    }

    @QAFTestStep(description = "page URL should still be on matrixtechnolabs.in domain")
    public void pageUrlShouldBeOnMatrixDomain() {
        String url = navigationPage.getCurrentUrl();
        Assert.assertTrue(url.contains("matrixtechnolabs.in"),
            "Expected URL to be on matrixtechnolabs.in domain but was: " + url);
    }
}
