package com.matrix.steps;

import com.matrix.pages.NavigationPage;
import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import org.testng.Assert;

public class NavigationSteps extends WebDriverTestBase {

    private NavigationPage navigationPage = new NavigationPage();

    @QAFTestStep(description = "navigation menu should be displayed")
    public void navigationMenuShouldBeDisplayed() {
        Assert.assertTrue(navigationPage.isNavMenuDisplayed(), "Navigation menu is not displayed");
    }

    @QAFTestStep(description = "Home menu link should be present")
    public void homeMenuLinkShouldBePresent() {
        Assert.assertTrue(navigationPage.isHomeLinkPresent(), "Home menu link is not present");
    }

    @QAFTestStep(description = "About menu link should be present")
    public void aboutMenuLinkShouldBePresent() {
        Assert.assertTrue(navigationPage.isAboutLinkPresent(), "About menu link is not present");
    }

    @QAFTestStep(description = "Services menu link should be present")
    public void servicesMenuLinkShouldBePresent() {
        Assert.assertTrue(navigationPage.isServicesLinkPresent(), "Services menu link is not present");
    }

    @QAFTestStep(description = "Contact menu link should be present")
    public void contactMenuLinkShouldBePresent() {
        Assert.assertTrue(navigationPage.isContactLinkPresent(), "Contact menu link is not present");
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
    public void clickContactMenuItem() {
        navigationPage.clickContact();
    }

    @QAFTestStep(description = "page should contain {0} in title or URL")
    public void pageShouldContainInTitleOrUrl(String text) {
        String title = navigationPage.getPageTitle().toLowerCase();
        String url   = navigationPage.getCurrentUrl().toLowerCase();
        Assert.assertTrue(title.contains(text.toLowerCase()) || url.contains(text.toLowerCase()),
            "Expected page title or URL to contain '" + text + "'. Title: " + title + ", URL: " + url);
    }

    @QAFTestStep(description = "page URL should still be on matrixtechnolabs.in domain")
    public void pageUrlShouldBeOnMatrixDomain() {
        String url = navigationPage.getCurrentUrl();
        Assert.assertTrue(url.contains("matrixtechnolabs.in"),
            "Expected URL to be on matrixtechnolabs.in domain but was: " + url);
    }
}
